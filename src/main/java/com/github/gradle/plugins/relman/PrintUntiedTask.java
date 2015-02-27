package com.github.gradle.plugins.relman;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskAction;

public class PrintUntiedTask extends DefaultTask {

	public PrintUntiedTask() {
		super();
		setDescription("Prints out all untied dependencies in the build run. Will enforce dependency resolution.");
		setGroup(ReleaseManagerPlugin.RELEASE_MGMT_TASK_GROUP);
	}

	@TaskAction
	public void printUntied() {
		// firstly force all projects to resolve their dependencies
		Project project = getProject();
		Project root = Utils.getRoot(project);
		Collection<Project> children = root.getChildProjects().values();
		Action<Configuration> resolveConfigurationAction = new Action<Configuration>() {
			public void execute(Configuration config) {
				config.getFiles();
			}
		};
		for (Project child : children) {
			child.getConfigurations().all(resolveConfigurationAction);
		}

		ReleaseManagerExtension orAddReleaseManagerExtension = ReleaseManagerExtension
				.getOrAddReleaseManagerExtension(project);
		Set<VersionedArtifactName> untiedDependencies = orAddReleaseManagerExtension
				.getUntiedDependencies();
		if (untiedDependencies.size() == 0) {
			System.out.println("No untied dependencies found.");
		} else {
			String rootName = root.getName();
			System.out.println("Untied dependencies found:");
			artifactLoop: for (VersionedArtifactName untiedDependency : Utils
					.sort(untiedDependencies)) {
				if(rootName.equals(untiedDependency.getGroupId())){
					if(root.findProject(untiedDependency.getArtifactId())!=null){
						// skip this artifact, because it was a project to project dependency and
						// shall not be listed as it is part of the build run
						continue artifactLoop;
					}
				}
				System.out.println(untiedDependency);
			}
		}
	}
}
