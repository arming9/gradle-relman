# Gradle Release Manager Plugin

## Introduction

This gradle plugin is used for tying versions of dependencies amongst all projects of a gradle multi-project build run. You use it when you are required to enforce control of what versions are used when building your software - and enforce that centrally. This is the case if
- your final product is built from more than ~10 parts. In a combinatioin with transitive dependency resolution, it's getting hard to track which version of a 3rd party library is used
- the parts of your final product are built in separate build runs. Each build run has it's own version resolution, and in such a scenario, each of them may come up with a different version of the same 3rd party dependency
- your final product is built from parts that do their releasing and versioning separately, because they are developed by separate teams in your company   

The proposed solution here is to have a gradle project that uses this plugin. The versions to be used for certain dependencies are defined in that project. All build runs that do provide content for your final product include this project then as part of a gradle multi project build. Usually, you will have a person or role in your company that is the owner of that project and sets the versions upon request from the teams.

The 'versioning' project will consist of
- a build.gradle file
- one or more .csv files defining the versions for all dependencies 

Compared with Maven, this is comparable to a parent pom with a dependencyManagement element - with some subtle advantages in handling:
- No need to build a hierarchy of builds
- The versioning project can be used from multiple other projects/build runs, not just those that are part of the parent pom inheritance blood line
- The versioning project can be used to control the in-house dependency versions as well, stripping all other build files of version information, making an end to 
  - the tedious dependency version avalanche (project A increments its version, project B uses A and must change the dependency-version, project B therefore must release a new version of B, project C uses B and this goes on all the way down through the dependency graph 
  - the notorious everything-1.0.0-SNAPSHOTs 

## `build.gradle`

Your versioning project (I suggest to call it `yourproductname-releasemanagement`) will have a build.gradle file like this:

```
apply plugin: 'releasemanager'

releaseManager {
// tie possibility #1 - directly in the build file, specific artifact to specific version
  tie 'commons-io','commons-io','1.2'
  tie 'commons-lang','commons-lang','1.0'
  tie 'commons-logging:commons-logging:1.0'
// tie possibility #2 - directly in the build file, a complete groupId is set to a specific version
  tie 'org.eclipse.jetty:*:7.9.5'
// tie possibility #3 - tie from a .csv file, typically easier for non-developers as release manager persons are
  fromCSV file('versions.txt')
}
```

When tying the version, you can also use \* as a wildcard for the artifactId. Specific ties with a artifactId will override any artifactId \* tie.

## Tying from a .csv

You can either tie the version directly in the build.gradle or put that to a .csv file (see line 11 above). .csv file will look like this:

```
commons-io,commons-io,1.2
commons-lang,commons-lang,1.0
commons-logging,commons-logging,1.0
junit,junit,4.11
org.springframework,*,4.1.1.RELEASE
```

Semi-colons and : are also supported as separators.

A good practice is to have two .csv files, one for the external/3rd party libs and the other for the internal libs.

# Tasks

Version tying is done without a specific gradle task, just add a project that has the releasemanager plugin applied to the build run as part of a multi-project-build. 

This will add some overhead to the configuration phase and it will not work with the gradle 2.x `--configure-on-demand` switch.

The plugin does 
- set the [ResolutionStrategy](http://gradle.org/docs/current/dsl/org.gradle.api.artifacts.ResolutionStrategy.html) on every [Configuration](http://gradle.org/docs/current/dsl/org.gradle.api.artifacts.Configuration.html) that is part of the build run
- in doing so, it will specifically force (see 'force' under  [ResolutionStrategy](http://gradle.org/docs/current/dsl/org.gradle.api.artifacts.ResolutionStrategy.html))  resolution strategy for the dependency versions that are specified (and this is what causes an overhead in the build run). This prevents us from transitive dependencies overruling our desired dependency versions just because they use higher versions. 

## printUntiedDependencies

To print out a report of dependencies which are not tied using the releasemanager plugin, the task 'printUntiedDependencies' is your choice. The printout is in a form that you can directly use in your .csv file with copy paste. 

## failOnUntiedDependencies

This task will fail the build run if an untied dependency exists. Beforehand, it will print out the untied dependencies. This task is typically added to 
a release build when starting the build run to ensure that versioning is all set, like so:

gradlew clean failOnUntiedDependencies release

(assuming your 'release' task does what it takes to build a real release)

# Todos and open issues of the plugin
- Add a feature to create a release, computing the next versions from the ties, providing a mechanism to easily propose the new version(s) to the release manager
- Write an elaborate test suite

