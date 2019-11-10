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

package com.palawan.gradle.util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Path utility methods.
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
public class PathUtil {

    private PathUtil() {
    }

    /**
     * Normalize path for NodeJS descriptor files.
     * @param path  Path to be normalized for descriptors of NodeJS.
     * @return Normalized file path as string
     */
    public static String normalize(Path path) {
        return normalize(path.toString());
    }

    /**
     * Normalize path for NodeJS descriptor files.
     * @param path  Path to be normalized for descriptors of NodeJS.
     * @return Normalized file path as string
     */
    public static String normalize(String path) {
        return path.replaceAll("\\\\", "/");
    }

	/**
	 * Join elements of path into normalized path for NodeJS descriptors.
	 * @param elements	Path elements to be joined
	 * @return	Joined path elements
	 */
	public static String join(String... elements) {
		return normalize(Paths.get("", elements));
	}

	/**
	 * Normalize windows path. Typical usage is to support windows commands
	 * path replacement.
	 * @param path	Path to be normalized for windows
	 * @return	Windows path format
	 */
	public static String windows(Path path) {
    	return windows(path.toString());
	}

	/**
	 * Normalize windows path. Typical usage is to support windows commands
	 * path replacement.
	 * @param path	Path to be normalized for windows
	 * @return	Windows path format
	 */
	public static String windows(String path) {
    	return path.replaceAll("/", "\\\\");
	}

}
