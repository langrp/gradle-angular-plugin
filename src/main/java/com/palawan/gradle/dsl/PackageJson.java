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

package com.palawan.gradle.dsl;

import java.util.function.BinaryOperator;

/**
 * Interface representing 'package.json' file of NodeJS component and
 * operations done on this particular file.
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
public interface PackageJson {

	/**
	 * Gets package version
	 * @return Package version
	 */
	String getVersion();

	/**
	 * Sets new package version;
	 * @param version New version
	 */
	void setVersion(String version);

	/**
	 * Updates each script using provided operator accepting name and current script value.
	 * @param updater Script updater accepting script name and current value, producing updated content
	 */
	void updateScripts(BinaryOperator<String> updater);
}
