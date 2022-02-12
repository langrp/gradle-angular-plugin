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

import org.gradle.api.file.FileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.SourceSetOutput
import spock.lang.Specification

/**
 *
 * @author Langr, Petr
 * @since 1.0.0 */
class AngularProjectSourceSetTests extends Specification {

	private AngularProjectSourceSet testSourceSet
	private ObjectFactory objectFactory

	def setup() {
		SourceDirectorySet directorySet = Mock()
		objectFactory = Mock()
		objectFactory.sourceDirectorySet(_ as String, _ as String) >> directorySet
	}

	def "GetName_main"() {
		given:
		testSourceSet = new AngularProjectSourceSet("main", objectFactory)

		when:
		def result = testSourceSet.getName()

		then:
		result == "main"

	}

	def "GetName"() {
		given:
		testSourceSet = new AngularProjectSourceSet("sample-app", objectFactory)

		when:
		def result = testSourceSet.getName()

		then:
		result == "sample-app"

	}

	def "GetBaseName_main"() {

		given:
		testSourceSet = new AngularProjectSourceSet("main", objectFactory)

		when:
		def result = testSourceSet.getBaseName()

		then:
		result == "main"

	}

	def "GetBaseName"() {

		given:
		testSourceSet = new AngularProjectSourceSet("sample-app", objectFactory)

		when:
		def result = testSourceSet.getBaseName()

		then:
		result == "sampleApp"

	}

	def "GetCompilePath"() {

		given:
		testSourceSet = new AngularProjectSourceSet("sample-app", objectFactory)

		when:
		def result = testSourceSet.getCompilePath()

		then:
		result == null

	}

	def "SetCompilePath"() {

		given:
		def compilePath = Mock(FileCollection)

		and:
		testSourceSet = new AngularProjectSourceSet("sample-app", objectFactory)

		when:
		testSourceSet.setCompilePath(compilePath)

		then:
		testSourceSet.getCompilePath() == compilePath

	}

	def "GetCompileConfigurationName_main"() {

		given:
		testSourceSet = new AngularProjectSourceSet("main", objectFactory)

		when:
		def result = testSourceSet.getCompileConfigurationName()

		then:
		result == "angular"

	}

	def "GetCompileConfigurationName"() {

		given:
		testSourceSet = new AngularProjectSourceSet("sample-app", objectFactory)

		when:
		def result = testSourceSet.getCompileConfigurationName()

		then:
		result == "sampleAppAngular"

	}

	def "GetCompileTaskName_main"() {

		given:
		testSourceSet = new AngularProjectSourceSet("main", objectFactory)

		when:
		def result = testSourceSet.getCompileTaskName()

		then:
		result == "compileAngular"

	}

	def "GetCompileTaskName"() {

		given:
		testSourceSet = new AngularProjectSourceSet("sample-app", objectFactory)

		when:
		def result = testSourceSet.getCompileTaskName()

		then:
		result == "compileSampleAppAngular"

	}

	def "GetDistributionTaskName_main"() {

		given:
		testSourceSet = new AngularProjectSourceSet("main", objectFactory)

		when:
		def result = testSourceSet.getDistributionTaskName()

		then:
		result == "distZip"

	}

	def "GetDistributionTaskName"() {

		given:
		testSourceSet = new AngularProjectSourceSet("sample-app", objectFactory)

		when:
		def result = testSourceSet.getDistributionTaskName()

		then:
		result == "sampleAppDistZip"

	}

	def "GetPublishToNodeModulesTaskName_main"() {

		given:
		testSourceSet = new AngularProjectSourceSet("main", objectFactory)

		when:
		def result = testSourceSet.getPublishToNodeModulesTaskName()

		then:
		result == "publishToNodeModules"

	}

	def "GetPublishToNodeModulesTaskName"() {

		given:
		testSourceSet = new AngularProjectSourceSet("sample-app", objectFactory)

		when:
		def result = testSourceSet.getPublishToNodeModulesTaskName()

		then:
		result == "publishSampleAppToNodeModules"

	}

	def "GetDirectory"() {

		given:
		testSourceSet = new AngularProjectSourceSet("main", objectFactory)

		when:
		def result = testSourceSet.getDirectory()

		then:
		result != null

	}

	def "GetOutput"() {

		given:
		testSourceSet = new AngularProjectSourceSet("main", objectFactory)

		when:
		def result = testSourceSet.getOutput()

		then:
		result == null

	}

	def "SetOutput"() {

		given:
		testSourceSet = new AngularProjectSourceSet("main", objectFactory)

		and:
		def output = Mock(SourceSetOutput)

		when:
		testSourceSet.setOutput(output)

		then:
		testSourceSet.getOutput() == output

	}

	def "ToString"() {

		given:
		testSourceSet = new AngularProjectSourceSet("sample-app", objectFactory)

		when:
		def result = testSourceSet.toString();

		then:
		result == "source set sample app"

	}

	def "Capitalize"() {

		when:
		def result = AngularProjectSourceSet.capitalize("")

		then:
		result == ""

	}

	def "Capitalize2"() {

		when:
		def result = AngularProjectSourceSet.capitalize("main")

		then:
		result == "Main"

	}

	def "Uncapitalize"() {

		when:
		def result = AngularProjectSourceSet.uncapitalize("")

		then:
		result == ""

	}

	def "Uncapitalize2"() {

		when:
		def result = AngularProjectSourceSet.uncapitalize("Main")

		then:
		result == "main"

	}
}
