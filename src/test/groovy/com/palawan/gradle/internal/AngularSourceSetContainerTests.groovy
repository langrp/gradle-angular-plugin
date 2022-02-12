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

import com.palawan.gradle.dsl.SourceSetContainer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.internal.CollectionCallbackActionDecorator
import org.gradle.api.internal.file.FileCollectionFactory
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.tasks.DefaultSourceSetOutput
import org.gradle.api.model.ObjectFactory
import org.gradle.internal.reflect.Instantiator
import spock.lang.Specification

/**
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
class AngularSourceSetContainerTests extends Specification {

	private AngularSourceSetContainer testContainer

	private Instantiator instantiator
	private CollectionCallbackActionDecorator callbackActionDecorator
	private FileResolver fileResolver
	private FileCollectionFactory fileCollectionFactory
	private ObjectFactory objectFactory

	def setup() {
		instantiator = Mock()
		callbackActionDecorator = Mock()
		fileResolver = Mock()
		fileCollectionFactory = Mock()
		objectFactory = Mock()

		fileCollectionFactory.configurableFiles(_ as String) >> Mock(ConfigurableFileCollection)

		instantiator.newInstance(AngularProjectSourceSet.class, _ as String, objectFactory) >> { args ->
			new AngularProjectSourceSet(args[1][0] as String, objectFactory)
		}

		instantiator.newInstance(DefaultSourceSetOutput.class, _ as String, fileResolver, fileCollectionFactory) >>
				{ args -> new DefaultSourceSetOutput(args[1][0] as String, fileResolver, fileCollectionFactory) }
	}

	def "DoCreate_main"() {

		given:
		testContainer = new AngularSourceSetContainer(
				instantiator,
				callbackActionDecorator,
				fileResolver,
				fileCollectionFactory,
				objectFactory)

		when:
		def result = testContainer.doCreate("main")

		then:
		result != null
		result.getName() == "main"

	}

	def "DoCreate"() {

		given:
		testContainer = new AngularSourceSetContainer(
				instantiator,
				callbackActionDecorator,
				fileResolver,
				fileCollectionFactory,
				objectFactory)

		when:
		def result = testContainer.doCreate("sample-app")

		then:
		result != null
		result.getName() == "sample-app"

	}

	def "GetPublicType"() {

		given:
		testContainer = new AngularSourceSetContainer(
				instantiator,
				callbackActionDecorator,
				fileResolver,
				fileCollectionFactory,
				objectFactory)

		when:
		def result = testContainer.getPublicType()

		then:
		result != null
		result.getConcreteClass() == SourceSetContainer

	}
}
