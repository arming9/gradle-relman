package com.github.gradle.plugins.relman;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;

/**
 *
 * @author Armin Ebert Groll
 */
public class ReleaseManagerPlugin implements Plugin<Project> {

	public static final String FAIL_ON_UNTIED_DEPENDENCIES_TASK_NAME = "failOnUntiedDependencies";
	public static final String PRINT_UNTIED_DEPENDENCIES_TASK_NAME = "printUntiedDependencies";
	public static final String RELEASE_MGMT_TASK_GROUP = "Release Management";

	public void apply(Project project) {
		ReleaseManagerExtension.getOrAddReleaseManagerExtension(project);

		TaskContainer tasks = project.getTasks();

		tasks.create(PRINT_UNTIED_DEPENDENCIES_TASK_NAME, PrintUntiedTask.class);
		tasks.create(FAIL_ON_UNTIED_DEPENDENCIES_TASK_NAME,
				FailOnUntiedTask.class);
	}
}
