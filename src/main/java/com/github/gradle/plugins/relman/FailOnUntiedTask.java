package com.github.gradle.plugins.relman;

import java.util.Set;

import org.gradle.api.tasks.TaskAction;

public class FailOnUntiedTask extends PrintUntiedTask {

	public FailOnUntiedTask() {
		super();
		setDescription("Enforces a build failure if there are untied dependencies in the current build run.");
	}

	@TaskAction
	public void failOnUntied() {
        ReleaseManagerExtension orAddReleaseManagerExtension = ReleaseManagerExtension.getOrAddReleaseManagerExtension(getProject());
		Set<VersionedArtifactName> untiedDependencies = orAddReleaseManagerExtension.getUntiedDependencies();
        if (untiedDependencies.size() != 0) {
          throw new RuntimeException("There are untied dependencies, see report above.");
        }
	}
}
