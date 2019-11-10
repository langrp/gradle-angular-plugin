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

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

/**
 * Base support for Jackson parser implementation.
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
public abstract class JsonBase {

    /**
     * Search for JSON field with given path under root node. The
     * path is defined with '.' separator for sub-fields name
     * just as known from javascript notation.
     * @param root  Root node as starting point for the field
     * @param path  Field path under given root node. Path contains
     *              multiple fields separated by '.' similarly
     *              to javascript notation.
     * @return  Optional of located field.
     */
    Optional<JsonNode> getByPath(JsonNode root, String path) {
        String[] fields = path.split("\\.");
        for (String field : fields) {
            root = root.get(field);
            if (root == null) {
                return Optional.empty();
            }
        }
        return Optional.of(root);
    }

}
