/*
 * Copyright (c) 2019 Petr Langr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.palawan.gradle

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.Stepwise

import java.nio.file.Files
import java.nio.file.Path

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

/**
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
@Stepwise
class AngularPluginFunctionalTests extends Specification {

	static Path testProjectDir
	static File buildFile

	def setupSpec() {
		testProjectDir = Files.createTempDirectory("junit")
		buildFile = Files.createFile(testProjectDir.resolve("build.gradle")).toFile()
		buildScript(buildFile)

		File settings = Files.createFile(testProjectDir.resolve("settings.gradle")).toFile()
		settings << """
			rootProject.name = 'sample-app'
		"""

	}

	def cleanupSpec() throws Exception {
		if (testProjectDir != null) {
			testProjectDir.toFile().deleteDir()
		}
	}

	def "can only initialize project"() {
		when:
		def result = GradleRunner.create()
				.withProjectDir(testProjectDir.toFile())
				.withArguments('tasks')
				.withPluginClasspath()
				.build()

		then:
		result.output.contains("angularInit")
		result.output.contains("angularCli")
		!result.output.contains("compileAngular")

	}

	def "can initialize project"() {

		when:
		def result = GradleRunner.create()
				.withProjectDir(testProjectDir.toFile())
				.withArguments('angularInit', '--routing', '--style=scss', '--skipGit')
				.withPluginClasspath()
				.build()

		then:
		result.task(":angularInit").outcome == SUCCESS
		Files.exists(testProjectDir.resolve("node_modules"))
		Files.exists(testProjectDir.resolve("ng"))
		Files.exists(testProjectDir.resolve("npm"))
		Files.exists(testProjectDir.resolve("ng.cmd"))
		Files.exists(testProjectDir.resolve("npm.cmd"))
		Files.exists(testProjectDir.resolve("angular.json"))

	}

	def "can compile project"() {

		when:
		def result = GradleRunner.create()
				.withProjectDir(testProjectDir.toFile())
				.withArguments('compileAngular')
				.withPluginClasspath()
				.build()

		then:
		result.task(":compileAngular").outcome == SUCCESS
		Files.exists(testProjectDir.resolve("build").resolve("angular").resolve("main"))

	}

	def "can add library"() {
		when:
		def result = GradleRunner.create()
				.withProjectDir(testProjectDir.toFile())
				.withArguments('angularCli', '--cmd=generate', '--args=library components')
				.withPluginClasspath()
				.build()

		then:
		result.task(":angularCli").outcome == SUCCESS
		Files.exists(testProjectDir.resolve("projects").resolve("components"))
	}

	def "can compile library"() {
		when:
		def result = GradleRunner.create()
				.withProjectDir(testProjectDir.toFile())
				.withArguments("compileComponentsAngular")
				.withPluginClasspath()
				.build()

		then:
		result.task(':compileComponentsAngular').outcome == SUCCESS
		Files.exists(testProjectDir.resolve("build").resolve("angular").resolve("components"))

	}

	def "can build project"() {
		when:
		def result = GradleRunner.create()
				.withProjectDir(testProjectDir.toFile())
				.withArguments("build")
				.withPluginClasspath()
				.build()

		then:
		result.task(":build").outcome == SUCCESS
		Files.exists(testProjectDir.resolve("build").resolve("distributions")
				.resolve("sample-app-1.0.0-SNAPSHOT.zip"))

	}

	def "can distribute library"() {
		when:
		def result = GradleRunner.create()
				.withProjectDir(testProjectDir.toFile())
				.withArguments("componentsDistZip")
				.withPluginClasspath()
				.build()

		then:
		result.task(":componentsDistZip").outcome == UP_TO_DATE // should be done from build
		Files.exists(testProjectDir.resolve("build").resolve("distributions")
				.resolve("sample-app-components-1.0.0-SNAPSHOT.zip"))

	}

	def "can publish to node_modules"() {
		when:
		def result = GradleRunner.create()
				.withProjectDir(testProjectDir.toFile())
				.withArguments("publishComponentsToNodeModules")
				.withPluginClasspath()
				.build()

		then:
		result.task(":publishComponentsToNodeModules").outcome == SUCCESS
		Files.exists(testProjectDir.resolve("node_modules").resolve("@sample").resolve("components"))

	}

	def "components as gradle plugin"() {
		given:
		buildScript(Files.createFile(testProjectDir.resolve("projects").resolve("components")
				.resolve("build.gradle")).toFile())
		File settings = testProjectDir.resolve("settings.gradle").toFile()
		settings << """
			include 'components'
			project(':components').projectDir = new File('projects/components')
		"""

		when:
		def result = GradleRunner.create()
				.withProjectDir(testProjectDir.toFile())
				.withArguments(":components:build")
				.withPluginClasspath()
				.build()

		then:
		result.task(":components:build").outcome == SUCCESS
		Files.exists(testProjectDir.resolve("projects").resolve("components").resolve("build")
				.resolve("distributions").resolve("components-1.0.0-SNAPSHOT.zip"))

	}

	def "project clean"() {
		when:
		def result = GradleRunner.create()
				.withProjectDir(testProjectDir.toFile())
				.withArguments("clean")
				.withPluginClasspath()
				.build()

		then:
		result.task(":clean").outcome == SUCCESS
		result.task(":components:clean").outcome == SUCCESS
		!Files.exists(testProjectDir.resolve("projects").resolve("components").resolve("build")
				.resolve("distributions").resolve("components-1.0.0-SNAPSHOT.zip"))

	}

	def "depends on components"() {
		given:
		buildFile << """
			dependencies {
				angular project( ':components' )
			}
		"""

		when:
		def result = GradleRunner.create()
				.withProjectDir(testProjectDir.toFile())
				.withArguments(":build")
				.withPluginClasspath()
				.build()

		then:
		result.task(":build").outcome == SUCCESS
		Files.exists(testProjectDir.resolve("projects").resolve("components").resolve("build")
				.resolve("distributions").resolve("components-1.0.0-SNAPSHOT.zip"))

	}

	def "increase version"() {
		given:
		buildScript(buildFile, '1.0.1')

		when:
		def result = GradleRunner.create()
				.withProjectDir(testProjectDir.toFile())
				.withArguments(":build")
				.withPluginClasspath()
				.build()

		then:
		result.task(":build").outcome == SUCCESS
		Files.exists(testProjectDir.resolve("build").resolve("distributions")
				.resolve("sample-app-1.0.1.zip"))
	}

	def buildScript(File buildFile) {
		buildScript(buildFile, '1.0.0-SNAPSHOT')
	}

	def buildScript(File buildFile, String version) {
		buildFile.text = """
			plugins {
				id 'com.palawanframe.angular'
			}
			
			group = 'com.palawanframe.sample'
			version = '${version}'

			angular {
				group = '@sample'
				nodeVersion = '12.12.0'
				npmVersion = '6.12.0'
				download = true
				workDir = rootProject.file( '.gradle/nodejs' )
				npmWorkDir = rootProject.file( '.gradle/npm' )
				nodeModulesDir = rootProject.projectDir
			}
		"""
	}

}
