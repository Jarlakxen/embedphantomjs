/**
 * Copyright (C) 2013
 *   Facundo Viale
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
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.github.jarlakxen.embedphantomjs.ExecutionTimeout;
import com.github.jarlakxen.embedphantomjs.PhantomJSReference;

public class PhantomJSFileExecutor {

	private static final Logger LOGGER = Logger.getLogger(PhantomJSFileExecutor.class);

	private final ExecutorService processExecutorService;

	private final PhantomJSReference phantomReference;

	private final ExecutionTimeout executionTimeout;

	public PhantomJSFileExecutor(PhantomJSReference phantomReference, ExecutionTimeout executionTimeout) {
		this(phantomReference, Executors.newCachedThreadPool(), executionTimeout);
	}

	public PhantomJSFileExecutor(final PhantomJSReference phantomReference, final ExecutorService executorService,
			final ExecutionTimeout executionTimeout) {
		this.phantomReference = phantomReference;
		this.executionTimeout = executionTimeout;
		this.processExecutorService = executorService;
	}

	public CompletableFuture<String> execute(final String fileContent, final String... args) {
		try {
			final File tmp = File.createTempFile(RandomStringUtils.randomAlphabetic(10), ".js");
			FileUtils.write(tmp, fileContent, Charset.defaultCharset());
			final CompletableFuture<String> result = execute(tmp, args);
			
			return result.handle((value, error) -> {
				if (tmp != null) {
					tmp.delete();
				}
				
		        if (error == null){
		            return CompletableFuture.completedFuture(value);
		        }
		        
		        LOGGER.error("", error);
		        final CompletableFuture<String> errorFuture = new CompletableFuture<>();
		        errorFuture.completeExceptionally(error);
		        return errorFuture;
		    }).thenCompose(Function.identity());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Invokes the instances of PhantomJS
	 * 
	 * @param sourceFile
	 *            JavaScript source file that is executed by PhantomJA
	 * @param args
	 *            Parameters that are read by sourceFile
	 * @return The result that the process has output in console
	 */
	public CompletableFuture<String> execute(final File sourceFile, final String... args) {
		final String cmd = this.phantomReference.getBinaryPath() + " " + this.phantomReference.getCommandLineOptions()
				+ " " + sourceFile.getAbsolutePath() + " " + StringUtils.join(args, " ");
		try {
			final Process process = Runtime.getRuntime().exec(cmd);
			LOGGER.info("Command to execute: " + cmd);

			final CompletableFuture<String> action = CompletableFuture.supplyAsync(() -> {
				try {
					LOGGER.info("Command to execute: " + cmd);
					final String output = IOUtils.toString(process.getInputStream(), Charset.defaultCharset());
					process.waitFor();
					LOGGER.debug("Command " + cmd + " output:" + output);
					return output;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}, processExecutorService);

			CompletableFuture.runAsync(() -> {
				try {
					action.get(executionTimeout.getTimeout(), executionTimeout.getUnit());
				} catch (Exception ex) {
					action.completeExceptionally(ex);
					process.destroy();
				}
			});

			return action;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
