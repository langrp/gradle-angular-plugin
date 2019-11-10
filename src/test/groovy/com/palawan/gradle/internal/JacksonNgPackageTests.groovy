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
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
class JacksonNgPackageTests extends Specification {

	private JacksonNgPackage testJson

	private Path jsonPath

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
		jsonPath = Paths.get(getClass().getResource("/projects/components/ng-package.json").toURI())
		File file = jsonPath.toFile()
		testJson = new JacksonNgPackage(file, new ObjectMapper().readTree(file) as ObjectNode)
	}

	def "GetFile"() {

		when:
		def result = testJson.getFile()

		then:
		result != null
		result.getName() == "ng-package.json"

	}

	def "SetDestination"() {

		given:
		def path = jsonPath.getParent().getParent().getParent().resolve("build")

		when:
		def result = testJson.setDestination(path)

		then:
		result
		testJson.getRoot().get("dest").asText() == "../../build"

	}

	def "SetDestination_noChange"() {

		given:
		def path = jsonPath.getParent().resolve("build/angular/main")

		when:
		def result = testJson.setDestination(path)

		then:
		!result

	}

	def "SetDestination_noDest"() {

		given:
		def file = jsonPath.toFile()
		def root = new ObjectMapper().readTree(file) as ObjectNode
		root.remove("dest")

		and:
		testJson = new JacksonNgPackage(file, root)

		when:
		def result = testJson.setDestination(jsonPath.getParent().resolve("build/angular/main"))

		then:
		result

	}

	def "Update"() {

		given:
		def file = Files.copy(jsonPath, temp.resolve("ng-package.json")).toFile()

		and:
		def root = new ObjectMapper().readTree(file) as ObjectNode
		root.remove("dest")
		testJson = new JacksonNgPackage(file, root)

		when:
		testJson.update()

		then:
		noExceptionThrown()

	}

	def "GetRoot"() {

		when:
		def result = testJson.getRoot()

		then:
		result != null

	}
}
