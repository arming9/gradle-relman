package com.github.gradle.plugins.relman;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencyResolveDetails;
import org.gradle.api.artifacts.ModuleVersionSelector;
import org.gradle.api.artifacts.ResolutionStrategy;
import org.gradle.api.plugins.ExtensionContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The registry for all version ties and versioning schemes. 
 * A single instance is bound to all projects applying the @see {@link ReleaseManagerPlugin} and the root project.
 *
 * @author Armin Ebert Groll
 */
public class ReleaseManagerExtension {

  private static final String RELEASE_MANAGER_EXTENSION = "releaseManager";
  private static final Logger log = LoggerFactory.getLogger(ReleaseManagerExtension.class);

  private Map<UnversionedArtifactName, VersionedArtifactName> artifactNameToVersion = new HashMap<UnversionedArtifactName, VersionedArtifactName>();
  private Map<String, String> groupIdToVersion = new HashMap<String, String>();
  private String[] forcedVersionsArray;
  private boolean initialized = false;
  private boolean versionsForced = false;

  private LinkedHashSet<VersionedArtifactName> untiedDependencies = new LinkedHashSet<VersionedArtifactName>();

  public ReleaseManagerExtension() {
  }

  Action<Configuration> configSetDependencyResolutionAction = new Action<Configuration>() {
    public void execute(Configuration configuration) {
      ResolutionStrategy resolutionStrategy = configuration.getResolutionStrategy();
      resolutionStrategy.eachDependency(dependencyResolveAction);
    }
  };

  Action<DependencyResolveDetails> dependencyResolveAction = new Action<DependencyResolveDetails>() {
    public void execute(DependencyResolveDetails dependencyResolveDetails) {
      ModuleVersionSelector requested = dependencyResolveDetails.getRequested();
      String requestedGroup = requested.getGroup();
      String requestedName = requested.getName();
      String requestedVersion = requested.getVersion();
      UnversionedArtifactName unversionedArtifactName = new UnversionedArtifactName(requestedGroup, requestedName);
      VersionedArtifactName versionedArtifactName = artifactNameToVersion.get(unversionedArtifactName);
      if (versionedArtifactName != null) {
        if (!requestedGroup.equals(versionedArtifactName.getGroupId()) || requestedName.equals(versionedArtifactName.getArtifactId()) || requestedVersion.equals(versionedArtifactName.getVersion())) {
          dependencyResolveDetails.useTarget(versionedArtifactName.getCompleteGradleDependencyString());
        } else {
          // nothing to do, it was defined as targeted
        }
      } else {
        // try to resolve by groupId
        String versionByGroupId = groupIdToVersion.get(requestedGroup);
        if (versionByGroupId != null) {
          if (requestedVersion.equals(versionByGroupId)) {
            // nothing to do, it was defined as targeted
          } else {
            dependencyResolveDetails.useTarget(requestedGroup + ":" + requestedName + ":" + versionByGroupId);
          }
        } else {
          // no tie specified. put that to the log
          untiedDependencies.add(new VersionedArtifactName(requestedGroup, requestedName, requestedVersion));
        }
      }
    }
  };

  private Action<? super Configuration> configForceVersionsAction = new Action<Configuration>() {
    public void execute(Configuration configuration) {
      configuration.getResolutionStrategy().setForcedModules((Object[]) getForcedVersionsArray());
    }
  };

  public static ReleaseManagerExtension getOrAddReleaseManagerExtension(Project project) {
    final Project superRoot = Utils.getRoot(project);
    ExtensionContainer superRootExtensions = superRoot.getExtensions();
    Object releaseManagerExtensionByName = superRootExtensions.findByName(RELEASE_MANAGER_EXTENSION);
    if (releaseManagerExtensionByName != null && !(releaseManagerExtensionByName instanceof ReleaseManagerExtension)) {
      throw new RuntimeException("Root project extension " + RELEASE_MANAGER_EXTENSION + " is not an instance of " + ReleaseManagerExtension.class + " which I did not expect in "
          + ReleaseManagerPlugin.class);
    }
    ReleaseManagerExtension releaseManagerExtensionTmp = null;
    if (releaseManagerExtensionByName != null) {
      releaseManagerExtensionTmp = (ReleaseManagerExtension) releaseManagerExtensionByName;
    } else {
      releaseManagerExtensionTmp = superRootExtensions.create(RELEASE_MANAGER_EXTENSION, ReleaseManagerExtension.class);
    }
    final ReleaseManagerExtension releaseManagerExtension = releaseManagerExtensionTmp;

    if (superRoot != project) {
      ExtensionContainer extensions = project.getExtensions();
      if(extensions.findByName(RELEASE_MANAGER_EXTENSION)!=null){
    	  extensions.add(RELEASE_MANAGER_EXTENSION, releaseManagerExtension);
      }
    }

    releaseManagerExtension.ensureInit(superRoot);
    
    project.afterEvaluate(new Action<Project>() {
      public void execute(Project project) {
        releaseManagerExtension.ensureForceVersions(superRoot);
      }
    });
    return releaseManagerExtension;
  }

  public void fromCSV(String file) {
    fromCSV(new File(file));
  }

  public void fromCSV(File file) {
    BufferedReader in = null;
    try {
      in = new BufferedReader(new FileReader(file));
      String line = null;
      int linecount = 1;
      while (null != (line = in.readLine())) {
        line = line.trim();
        if (line.length() > 0) {
          String[] split = line.split("[\"';,:]");
          if (split.length != 3) {
            log.warn("Line " + linecount + " in file " + file.getAbsolutePath() + ":'" + line + "' has been ignored.");
          } else {
            String groupId = split[0].trim();
            String artifactId = split[1].trim();
            String version = split[2].trim();
            tie(groupId, artifactId, version);
          }
        }
        linecount++;
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  public void tie(String gradleDepString) {
    String[] split = gradleDepString.split(":");
    if (split.length != 3) {
      throw new RuntimeException("Tied versions must be specified like a gradle dependency, not like this: '" + gradleDepString + "' but rather like this: 'org.slf4j:slf4j-api:1.7.10'");
    }
    tie(split[0], split[1], split[2]);
  }

  public void tie(String groupId, String artifactId, String version) {
    if ("*".equals(artifactId)) {
      groupIdToVersion.put(groupId, version);
    } else {
      tie(new UnversionedArtifactName(groupId, artifactId), version);
    }
  }

  public void tie(UnversionedArtifactName unversionedArtifactName, String version) {
    if (artifactNameToVersion.containsKey(unversionedArtifactName)) {
      throw new RuntimeException("Artifact " + unversionedArtifactName + " has already been tied to version " + artifactNameToVersion.get(unversionedArtifactName).getVersion());
    }
    artifactNameToVersion.put(unversionedArtifactName, new VersionedArtifactName(unversionedArtifactName, version));
  }

  public void ensureForceVersions(Project superRoot) {
    System.out.println("force versions");
    if (!versionsForced) {
      HashSet<String> forcedVersions = new HashSet<String>();
      for (VersionedArtifactName versionedArtifactName : artifactNameToVersion.values()) {
        forcedVersions.add(versionedArtifactName.getCompleteGradleDependencyString());
      }
      forcedVersionsArray = forcedVersions.toArray(new String[forcedVersions.size()]);

      for (Project childProject : superRoot.getAllprojects()) {
        childProject.getConfigurations().all(configForceVersionsAction);
      }

      versionsForced = true;
    } else {
      log.error("Reforcing versions");
    }
  }

  public void ensureInit(Project superRoot) {
    if (!initialized) {
      for (Project childProject : superRoot.getAllprojects()) {
        childProject.getConfigurations().all(configSetDependencyResolutionAction);
      }
      initialized = true;
    }
  }

  public String[] getForcedVersionsArray() {
    return forcedVersionsArray;
  }

  public Set<VersionedArtifactName> getUntiedDependencies() {
    return Collections.unmodifiableSet(untiedDependencies);
  }
}
