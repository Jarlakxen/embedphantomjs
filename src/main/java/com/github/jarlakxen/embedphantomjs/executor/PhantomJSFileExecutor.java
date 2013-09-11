/**
 * Copyright (C) 2013
 *   Facundo Viale <fviale@despegar.com>
 *
 * with contributions from
 * 	Facundo Viale (Jarlakxen@github)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jarlakxen.embedphantomjs.executor;

import java.io.File;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.github.jarlakxen.embedphantomjs.PhantomJSReference;

public abstract class PhantomJSFileExecutor<T> {

	private static final Logger LOGGER = Logger.getLogger(PhantomJSFileExecutor.class);
	
	private PhantomJSReference phantomReference;
	
	public PhantomJSFileExecutor(PhantomJSReference phantomReference) {
		this.phantomReference = phantomReference;
	}
	
	public abstract T execute(final File sourceFile, final String... args);
	
	public String doExecute(final File sourceFile, final String... args) {
		try {
			String cmd = this.phantomReference.getBinaryPath() + " " + sourceFile.getAbsolutePath() + " " + StringUtils.join(args, " ");
			LOGGER.info("Command to execute: " + cmd);
			Process process = Runtime.getRuntime().exec(cmd);
			String output = IOUtils.toString(process.getInputStream());
			process.waitFor();
			LOGGER.debug("Command " + cmd + " output:" + output);
			return output;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
