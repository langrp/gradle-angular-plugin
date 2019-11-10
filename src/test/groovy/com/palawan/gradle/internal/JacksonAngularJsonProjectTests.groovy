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

package com.palawan.gradle.internal

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.palawan.gradle.dsl.AngularJsonProject
import spock.lang.Specification
import spock.lang.Stepwise

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
@Stepwise
class JacksonAngularJsonProjectTests extends Specification {

	private JacksonAngularJsonProject testProject

	private JacksonAngularJson angularJson

	private static Path temp;

	def setupSpec() {
		temp = Files.createTempDirectory("junit")
	}

	def cleanupSpec() {
		if (temp != null) {
			temp.toFile().deleteDir()
		}
	}


	def setup() {
		File file = getResource("/angular.json").toFile()
		createProject(file, "ng-library")
	}

	def "Type.from_library"() {

		when:
		def result = AngularJsonProject.Type.from("library")

		then:
		result == AngularJsonProject.Type.LIBRARY

	}

	def "Type.from_app"() {

		when:
		def result = AngularJsonProject.Type.from("application")

		then:
		result == AngularJsonProject.Type.APPLICATION

	}

	def "Type.from_unknown"() {

		when:
		def result = AngularJsonProject.Type.from("unknown")

		then:
		result == AngularJsonProject.Type.UNKNOWN

	}

	def "GetName"() {

		when:
		def result = testProject.getName()

		then:
		result == "ng-library"

	}

	def "GetProjectType"() {

		when:
		def result = testProject.getProjectType()

		then:
		result == AngularJsonProject.Type.APPLICATION

	}

	def "GetRoot"() {

		when:
		def result = testProject.getRoot()

		then:
		result != null

	}

	def "SetRoot"() {
		// Incubating
	}

	def "GetSourceRoot"() {

		when:
		def result = testProject.getSourceRoot()

		then:
		result != null
		result.getFileName().toString() == "src"

	}

	def "GetSourceRoot_missing"() {

		given:
		(angularJson.getRoot().get("projects").get("ng-library") as ObjectNode).remove("sourceRoot")

		when:
		def result = testProject.getSourceRoot()

		then:
		result == angularJson.getFile().getParentFile().toPath()
	}

	def "GetNgPackageFile"() {

		when:
		def result = testProject.getNgPackageFile()

		then:
		!result.isPresent()

	}

	def "GetNgPackageFile_library"() {

		given:
		ObjectNode root = angularJson.getRoot().get("projects").get("components") as ObjectNode
		testProject = new JacksonAngularJsonProject(
				angularJson,
				root,
				"components",
				angularJson.getFile().toPath().getParent(),
				false
		)

		when:
		def result = testProject.getNgPackageFile()

		then:
		result.isPresent()

	}

	def "SetOutputPath"() {

		given:
		def file = Files.copy(getResource("/angular.json"), temp.resolve("angular.json"))
		createProject(file.toFile(), "ng-library")

		when:
		testProject.setOutputPath(file.getParent().resolve("ng-library"))

		then:
		noExceptionThrown()

	}

	def "SetOutputPath_unchanged"() {

		given:
		def file = temp.resolve("angular.json")
		createProject(file.toFile(), "ng-library")

		when:
		testProject.setOutputPath(file.getParent().resolve("ng-library"))

		then:
		noExceptionThrown()

	}

	def "SetOutputPath_library"() {

		given:
		Files.createDirectories(temp.resolve("projects/components"))
		Files.copy(getResource("/projects/components/ng-package.json"), temp.resolve("projects/components/ng-package.json"))
		def file = temp.resolve("angular.json")
		createProject(file.toFile(), "components")

		when:
		testProject.setOutputPath(file.getParent().resolve("build/angular/components"))

		then:
		noExceptionThrown()

	}

	def "SetOutputPath_library_unchanged"() {

		given:
		Files.createDirectories(temp.resolve("projects/components"))
		def file = temp.resolve("angular.json")
		createProject(file.toFile(), "components")

		when:
		testProject.setOutputPath(file.getParent().resolve("build/angular/components"))

		then:
		noExceptionThrown()

	}

	def "IsDefault"() {

		when:
		def result = testProject.isDefault()

		then:
		result

	}

	def "GetVersion"() {

		when:
		def result = testProject.getVersion()

		then:
		result == "1.0.0"

	}

	def "GetVersion_library"() {

		given:
		ObjectNode root = angularJson.getRoot().get("projects").get("components") as ObjectNode
		testProject = new JacksonAngularJsonProject(
				angularJson,
				root,
				"components",
				angularJson.getFile().toPath().getParent(),
				false
		)

		when:
		def result = testProject.getVersion()

		then:
		result == "1.0.0"

	}

	def "SetVersion"() {

		when:
		testProject.setVersion("1.0.0")

		then:
		testProject.getVersion() == "1.0.0"

	}

	def "GetProjectFiles"() {

		when:
		def result = testProject.getProjectFiles()

		then:
		result != null
		result.count() == 13L

	}

	private void createProject(File file, String projectName) {
		ObjectNode angularJsonRoot = new ObjectMapper().readTree(file) as ObjectNode
		angularJson = new JacksonAngularJson(file, angularJsonRoot)
		ObjectNode root = angularJsonRoot.get("projects").get(projectName) as ObjectNode
		testProject = new JacksonAngularJsonProject(
				angularJson,
				root,
				projectName,
				angularJson.getFile().toPath().getParent(),
				true
		)
	}

	private Path getResource(String path) {
		return Paths.get(getClass().getResource(path).toURI());
	}

}
