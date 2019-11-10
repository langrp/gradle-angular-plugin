# Gradle Angular Plugin

[![Build Status](https://travis-ci.com/langrp/gradle-angular-plugin.svg?branch=master)](https://travis-ci.com/langrp/gradle-angular-plugin)
[![License](https://img.shields.io/github/license/langrp/gradle-angular-plugin.svg)](https://opensource.org/licenses/MIT)
![Version](https://img.shields.io/badge/Version-0.1.0-orange.svg)

> **INFO**: The feature preview 'GRADLE_METADATA' must be enabled for the plugin to load dependencies from repository!
> Adjust your ```settings.gradle``` file
> ```groovy
> enableFeaturePreview(FeaturePreviews.Feature.GRADLE_METADATA.name())
> ```

This plugin enables you to run build of your backend along side of your angular base frontend. In order to do so the
plugin uses [NodeJs](https://github.com/srs/gradle-node-plugin/blob/2.0/docs/node.md) plugin, which enables additional
features. The angular plugin handles:
* Initialization of new angular project
* Distribution and versioning
* Dependency management
* Artifact publishing

To start using the plugin add this into your `build.gradle` file.
```groovy
plugins {
    id "com.palawanframe.angular" version "1.0.0"
}
```

Based on configuration stage the plugin enables certain tasks. For gradle project without angular files only enables
task is
* angularInit - initializes angular project.

Once angular project is fully initialized the plugin enables full set of tasks:
* _angularCli_ - task to execute angular cli from gradle. Arguments:
  * _cmd_ - defines one of supported commands 'new', 'generate', etc.
  * _args_ - specifies additional arguments to the command e.g. "library components" to generate new library
* _compileAngular_ - compiles main angular source set (more below)
* _distZip_ - wraps compiled main source set into zip file
* _publishToNodeModules_ - publishes main angular source set into specified node_modules directory

## Configuring Plugin
You can configure basic extension block of [NodeJs](https://github.com/srs/gradle-node-plugin/blob/2.0/docs/node.md)
plugin with additional parameters as shown below. The plugin extends NodeJs parameters for simple configuration, but
you can still use node extension block.
```groovy
angular {
    // Node module group used as parent folder in node_modules directory for all dependencies,
    // default uses gradle project group
    group = "com.palawanframe.sample"

    // Angular build output location, default '${buildDir}/angular/main' 
    output = "${buildDir}/resources/main/static/ng"

    // Angular CLI version for initialization of the project. If omitted the latest will be used
    version = '8.3.17'
    
    // The rest of NodeJs parameters
    nodeVersion = libraries.nodeVersion
    npmVersion = libraries.npmVersion
    download = true
    workDir = rootProject.file( '.gradle/nodejs' )
    npmWorkDir = rootProject.file( '.gradle/npm' )
    nodeModulesDir = rootProject.projectDir

}
```

## Initialize Project
Once gradle project is configured with above parameters the plugin can initialize angular project. This will happen
using angular CLI command of `ng new`. The task supports basic parameters of CLI, in which the `style` parameter
is mandatory to create new angular application. If omitted only angular CLI will be initialized in NodeJs project.
```shell script
./gradlew angularInit --style=scss --routing --skipGit
```
This task will create file structure of NodeJs project with angular cli and quick shell scripts `npm` and `ng`
depending whether local copy of `node` needs to be downloaded. From here we initialize angular project using cli
```shell script
./ng new application --directory=.
```

### Gradle Structure
> Still incubating feature

Structures angular project into multi project build where main application would be placed in specific directory,
similarly to gradle structure. This allows to multi project build with main angular application, libraries as well as
backend projects, all built from single command. Use additional parameter `mainProject` on `angularInit` task.
```shell script
./gradlew angularInit --style=scss --routing --skipGit --mainProject=frontend-app
./gradlew angularCli --cmd=generate --args="library components"
```

The task will initialize structure for main angular application called `frontend-app` and place it into same named
directory. All necessary files will be moved along with update on angular.json file. Adding additional angular
libraries/application will be placed next to the main application (notice library directory components).
```
project
├─── gradle
├─── node_modules  
├─── components
|    ├─── src
|    ├─── karma.conf.js
|    └─── ng-package.json
├─── frontend-app
│    ├─── e2e
|    ├─── src
|    |    karma.conf.js
│    │    tsconfig.app.json
|    |    tsconfig.spec.json
│    └─── tslint.json
│   ng
|   npm
│   angular.json
|   package.json
└───README.md
```

For multi project builds the node dependencies can and should be shared across all applications and components. As such
the top level gradle project will handle node dependencies of [NodeJs](https://github.com/srs/gradle-node-plugin/blob/2.0/docs/node.md)
plugin. Every angular build depends on the node configuration build steps. It may seem blocked for the first build, but
any other execution will be faster.

### Source Set
The plugin works with source set container for each angular project, which is not managed as gradle project. This does
not require to define gradle project for each angular project, but rather use source sets only. Those source sets are
initialized automatically by the plugin. Each source set supports:
1. Compilation using `compile<Name>Angular` task
2. Dependency management using `<name>Angular` configuration

Where name is camel case angular project name as defined in angular.json.

## Versioning
The plugin can modify NodeJs descriptor file ```package.json``` to define version as defined in gradle project. The
file will be modified only when gradle project version changes.

## Dependency Management
The plugin manages dependencies specific to angular project to allow modularization of an application. You can create
gradle multi-project with angular libraries and main angular application, in which main application depends on libraries
built by other gradle projects. Or publish your library into your repository and depend on the artifact.
```groovy
dependencies {
    angular project( ':product-page' )
    angular 'com.palawanframe.sample:components:1.0.0'
}
```
Before the project would be built, gradle resolves all dependencies and publish them into node_modules directory for
angular build.

Angular lazy loading of library modules can also be supported without use of wrapper modules (aot build). Such approach
requires library source code to be part of the build. How to publish component source and depend on it for lazy
loading build is show below (note for successful compilation the tsconfig paths and include must be updated).
```groovy
task sourcesNg(type: Zip) {
    from project.angular.sources.main.directory
    archiveClassifier.set( 'sources' )
}

LazyPublishArtifact sourceArtifact = new LazyPublishArtifact(tasks.named('sourcesNg'))
configurations.nodeLibrary.outgoing.artifacts.add(sourceArtifact)

dependencies {
    angular project( ':product-page' )
    angular 'com.palawanframe.sample:components:1.0.0:sources@zip'
}
```

The plugin works with source set container for each angular project, which is not managed as gradle project. This does
not require to define gradle project for each angular project, but rather use source sets only. Those source sets are
initialized automatically by the plugin. To define specific dependency for angular project called 'product-page'
the following can be used:
```groovy
dependencies {
    productPageAngular 'com.palawanframe.sample:first-product:1.0.0'
}
```

## Distribution
Gradle [distribution plugin](https://docs.gradle.org/current/userguide/distribution_plugin.html) is being used to wrap
compiled angular code for publishing. As such the task `<name>DistZip` can be executed to generate zip file, where
name is source set name (for main source set distZip task is registered).

## Artifact Publishing
The plugin uses [Distribution](https://docs.gradle.org/current/userguide/distribution_plugin.html) plugin to produce
artifact output for each source set defined. The plugin works only with zip files therefore tar tasks are disabled
to not produce any files. Example of maven publish configuration.
```groovy
publishing {

    publications {
        maven(MavenPublication) {
            from components.angular
        }
    }
}
```

## Java Resource Component
For some application may be required to manage angular resources inside dependent jar file or along side of java
backend implementation. For this purpose the angular output can be redirected to java resource directory as shown
on example below.
```groovy
plugins {
    id 'java'
    id 'com.palawanframe.angular'
}

angular {
    output = "${buildDir}/resources/main/static/ng"
}

jar.dependsOn( compileAngular )
```
