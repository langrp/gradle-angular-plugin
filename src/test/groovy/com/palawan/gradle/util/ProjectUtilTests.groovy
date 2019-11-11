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

import com.palawan.gradle.AngularBasePlugin
import org.gradle.api.Project
import org.gradle.api.plugins.PluginContainer
import spock.lang.Specification
/**
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
class ProjectUtilTests extends Specification {

	Project project

	def "GetTopLevelProject"() {
		given:
		project = Mock(Project)
		project.getRootProject() >> project

		when:
		def result = ProjectUtil.getTopLevelProject(project)

		then:
		result == project

	}

	def "GetTopLevelProject2"() {
		given:
		Project root = Mock(Project)
		PluginContainer plugins = Mock()
		root.getRootProject() >> root
		root.getPlugins() >> plugins
		plugins.hasPlugin(AngularBasePlugin.class) >> true

		and:
		project = Mock(Project)
		project.getParent() >> root
		project.getRootProject() >> root

		when:
		def result = ProjectUtil.getTopLevelProject(project)

		then:
		result == root

	}

	def "GetTopLevelProject3"() {
		given:
		Project root = Mock(Project)
		PluginContainer plugins = Mock()
		root.getRootProject() >> root
		root.getPlugins() >> plugins
		plugins.hasPlugin(AngularBasePlugin.class) >> false

		and:
		Project ngRoot = Mock(Project)
		PluginContainer ngPlugins = Mock()
		ngRoot.getRootProject() >> root
		ngRoot.getParent() >> root
		ngRoot.getPlugins() >> ngPlugins
		ngPlugins.hasPlugin(AngularBasePlugin.class) >> true

		and:
		project = Mock(Project)
		project.getRootProject() >> root
		project.getParent() >> ngRoot

		when:
		def result = ProjectUtil.getTopLevelProject(project)

		then:
		result == ngRoot

	}

	def "IsTopLevelProject"() {
		given:
		project = Mock(Project)
		project.getRootProject() >> project

		when:
		def result = ProjectUtil.isTopLevelProject(project)

		then:
		result

	}

	def "IsTopLevelProject2"() {
		given:
		Project root = Mock(Project)
		root.getRootProject() >> root

		and:
		project = Mock(Project)
		project.getRootProject() >> root

		when:
		def result = ProjectUtil.isTopLevelProject(project)

		then:
		!result

	}
}
