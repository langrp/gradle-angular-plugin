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

package com.palawan.gradle.util

import org.gradle.api.GradleException
import org.gradle.api.Project
import spock.lang.Specification
import spock.lang.Stepwise

import java.nio.file.Files
import java.nio.file.Paths

/**
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
@Stepwise
class AngularJsonHelperTests extends Specification {

	AngularJsonHelper tester

	def setup() {
		tester = AngularJsonHelper.getInstance()
	}

	def "GetAngularJson"() {

		given:
		Project project = Mock(Project)
		project.getRootProject() >> project
		project.file("angular.json") >> new File(getClass().getResource("/angular.json").toURI())

		when:
		def result = tester.getAngularJson(project)

		then:
		result.isPresent()
		result.get().getDefaultProject().isPresent()
		result.get().getDefaultProject().get().getName() == "ng-library"
		result.get().getProject("ng-library").isPresent()
		result.get().getProject("components").isPresent()
		result.get().getProject("example").isPresent()
		result.get().getProjects().size() == 3
		result.get().getFile() != null

	}

	def "GetAngularJson_error"() {

		given:
		def project = Mock(Project)
		project.getRootProject() >> project
		project.file("angular.json") >> new File("/tmp/package.json")

		when:
		def result = tester.getAngularJson(project)

		then:
		!result.isPresent()

	}

	def "GetNgPackageJson"() {

		given:
		def ngPackage = new File(getClass().getResource("/ng-package.json").toURI())

		when:
		def result = tester.getNgPackageJson(ngPackage)

		then:
		result.getFile() != null

	}

	def "GetNgPackageJson_error"() {

		given:
		def ngPackage = new File("/tmp/unknown.json")

		when:
		tester.getNgPackageJson(ngPackage)

		then:
		thrown(GradleException)

	}

	def "GetPackageJson"() {

		given:
		def packageJson = Paths.get(getClass().getResource("/package.json").toURI())

		when:
		def result = tester.getPackageJson(packageJson.parent)

		then:
		result.isPresent()
		result.get().getVersion() == '1.0.0'

	}

	def "GetPackageJson_error"() {

		given:
		def packageJson = Paths.get(getClass().getResource("/package.json").toURI())

		when:
		def result = tester.getPackageJson(packageJson)

		then:
		!result.isPresent()

	}

	def "UpdateAngularJson"() {

		given: "Angular file"
		def path = Paths.get(getClass().getResource("/angular.json").toURI())
		def time = Files.getLastModifiedTime(path)

		and: "Project with angular.json"
		Project project = Mock(Project)
		project.getRootProject() >> project
		project.file("angular.json") >> path.toFile()

		and: "Parsed angular.json"
		def angular = tester.getAngularJson(project)

		when: "Updating angular.json"
		tester.updateAngularJson(angular.get())

		then: "File would be updated"
		Files.getLastModifiedTime(path) > time

	}

	def "UpdateNgPackageJson"() {

		given: "NgPackage file"
		def path = Paths.get(getClass().getResource("/ng-package.json").toURI())
		def time = Files.getLastModifiedTime(path)

		and: "Parsed ng-package.json"
		def ngPackage = tester.getNgPackageJson(path.toFile())

		when: "Updating ng-package.json"
		tester.updateNgPackageJson(ngPackage)

		then: "File would be modified"
		Files.getLastModifiedTime(path) > time

	}

	def "UpdatePackageJson"() {

		given: "package.json file"
		def path = Paths.get(getClass().getResource("/package.json").toURI())
		def time = Files.getLastModifiedTime(path)

		and: "Parsed package.json"
		def packageJson = tester.getPackageJson(path.parent)

		when: "Updating package.json"
		tester.updatePackageJson(packageJson.get())

		then: "File would be modified"
		Files.getLastModifiedTime(path) > time

	}

	def "ArtifactUpdated"() {
	}

	def "TestArtifactUpdated"() {
	}

	def "GenerateTimestamp"() {
	}

}
