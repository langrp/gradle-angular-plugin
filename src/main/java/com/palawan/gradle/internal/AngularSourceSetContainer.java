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

package com.palawan.gradle.internal;

import org.gradle.api.internal.AbstractValidatingNamedDomainObjectContainer;
import org.gradle.api.internal.CollectionCallbackActionDecorator;
import org.gradle.api.internal.file.FileCollectionFactory;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.internal.tasks.DefaultSourceSetOutput;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.reflect.TypeOf;
import org.gradle.internal.reflect.Instantiator;
import com.palawan.gradle.dsl.SourceSet;
import com.palawan.gradle.dsl.SourceSetContainer;

import javax.inject.Inject;

/**
 * Implementation of {@link SourceSetContainer} for angular source set
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
public class AngularSourceSetContainer extends AbstractValidatingNamedDomainObjectContainer<SourceSet> implements SourceSetContainer {

    private final FileResolver fileResolver;
    private final FileCollectionFactory fileCollectionFactory;
    private final Instantiator instantiator;
    private final ObjectFactory objectFactory;

    @Inject
    public AngularSourceSetContainer(
                Instantiator instantiator,
                CollectionCallbackActionDecorator callbackActionDecorator,
                FileResolver fileResolver,
                FileCollectionFactory fileCollectionFactory,
                ObjectFactory objectFactory) {
        super(SourceSet.class, instantiator, SourceSet::getName, callbackActionDecorator);
        this.fileResolver = fileResolver;
        this.fileCollectionFactory = fileCollectionFactory;
        this.instantiator = instantiator;
        this.objectFactory = objectFactory;
    }

    @Override
    protected SourceSet doCreate(String name) {
        AngularProjectSourceSet sourceSet = instantiator.newInstance(AngularProjectSourceSet.class, name, objectFactory);
        sourceSet.setOutput(instantiator.newInstance(DefaultSourceSetOutput.class, sourceSet.getName(), this.fileResolver, this.fileCollectionFactory));
        return sourceSet;
    }

    @Override
    public TypeOf<?> getPublicType() {
        return TypeOf.typeOf(SourceSetContainer.class);
    }
}
