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
class JacksonPackageJsonTests extends Specification {

	private JacksonPackageJson packageJson

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
		File file = new File(getClass().getResource("/package.json").toURI())
		packageJson = new JacksonPackageJson(new ObjectMapper().readTree(file) as ObjectNode, file)
	}

	def "GetVersion"() {

		when:
		def result = packageJson.getVersion()

		then:
		result == "1.0.0"

	}

	def "SetVersion_noChange"() {

		when:
		packageJson.setVersion("1.0.0")

		then:
		packageJson.getVersion() == "1.0.0"

	}

	def "SetVersion"() {

		given:
		def file = Files.copy(Paths.get(getClass().getResource("/package.json").toURI()),
				temp.resolve("package.json")).toFile()

		and:
		packageJson = new JacksonPackageJson(new ObjectMapper().readTree(file) as ObjectNode, file)

		when:
		packageJson.setVersion("1.0.1")

		then:
		noExceptionThrown()
		packageJson.getVersion() == "1.0.1"

	}

	def "GetFile"() {

		when:
		def result = packageJson.getFile()

		then:
		result != null
		result.getName() == "package.json"

	}

	def "GetRoot"() {

		when:
		def result = packageJson.getRoot()

		then:
		result != null

	}
}
