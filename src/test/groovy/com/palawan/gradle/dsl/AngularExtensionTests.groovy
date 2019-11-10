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

package com.palawan.gradle.dsl

import com.moowork.gradle.node.NodeExtension
import com.palawan.gradle.internal.AngularSourceSetContainer
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionContainer
import spock.lang.Specification
/**
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
class AngularExtensionTests extends Specification {

	private AngularExtension extension;


	private SourceSetContainer sourceSetContainer;

	def setup() {
		Project project = Mock()
		project.getProjectDir() >> new File("/tmp")

		def objFactory = Mock(ObjectFactory)
		sourceSetContainer = Mock(SourceSetContainer)
		objFactory.newInstance(AngularSourceSetContainer.class) >> sourceSetContainer

		NodeExtension node = new NodeExtension( project )
		extension = new AngularExtension(node, null, objFactory)
	}

	def "Get"() {

		given:
		def project = Mock(Project)
		def extensions = Mock(ExtensionContainer)
		extensions.findByType(_ as Class) >> extension
		project.getExtensions() >> extensions

		when:
		def result = AngularExtension.get(project)

		then:
		result != null
		result.getAngularJson() == null

	}

	def "Get_null"() {

		given:
		def project = Mock(Project)
		def extensions = Mock(ExtensionContainer)
		project.getExtensions() >> extensions

		when:
		AngularExtension.get(project)

		then:
		thrown(GradleException)

	}

	def "GetVersion"() {

		when:
		def result = extension.getVersion()

		then:
		result == null

	}

	def "SetVersion"() {

		when:
		extension.setVersion("8.13.1")

		then:
		extension.getVersion() == "8.13.1"

	}

	def "GetGroup"() {

		when:
		def result = extension.getGroup()

		then:
		result == null

	}

	def "SetGroup"() {

		when:
		extension.setGroup("@palawan")

		then:
		"@palawan" == extension.getGroup()

	}

	def "GetOutput"() {

		when:
		def result = extension.getOutput()

		then:
		result == null

	}

	def "SetOutput"() {

		when:
		extension.setOutput("build/resources/main/static/ng")

		then:
		"build/resources/main/static/ng" == extension.getOutput()

	}

	def "GetAngularJson"() {

		when:
		def result = extension.getAngularJson()

		then:
		result == null

	}

	def "GetSources"() {

		when:
		def result = extension.getSources()

		then:
		result != null

	}

	def "Sources"() {

		when:
		extension.sources {
			main {

			}
		}

		then:
		1 * sourceSetContainer.configure(_)

	}

	def "GetNodeModules"() {

		when:
		def result = extension.getNodeModules()

		then:
		result == new File("/tmp/node_modules")

	}

	def "GetNodeModulesTarget"() {

		when:
		def result = extension.getNodeModulesTarget("com.palawanframe.core", "core")

		then:
		result == new File("/tmp/node_modules/com.palawanframe.core/core")

	}

	def "GetNodeModulesTarget_group"() {

		given:
		extension.setGroup("@palawan")

		when:
		def result = extension.getNodeModulesTarget("com.palawanframe.core", "core")

		then:
		result == new File("/tmp/node_modules/@palawan/core")

	}

}
