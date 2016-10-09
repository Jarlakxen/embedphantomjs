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

import static java.util.Arrays.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.github.jarlakxen.embedphantomjs.PhantomJSReference;
import com.github.jarlakxen.embedphantomjs.exception.UnexpectedProcessEndException;

public class PhantomJSConsoleExecutor {

	private static final Logger LOGGER = Logger.getLogger(PhantomJSConsoleExecutor.class);

	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	private static final int EOF = -1;

	private static final char SYSTEM_NEWLINE[] = System.getProperty("line.separator").toCharArray();

	private static final String DEFAULT_PHANTOMJS_CONSOLE_PREFIX = "phantomjs> ";
	private static final List<String> DEFAULT_PHANTOMJS_CONSOLE_POSTFIXS = asList("{}", "undefined");
	private static final String PHANTOMJS_PARSER_ERROR_PREFIX = "Parse error";

	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private final PhantomJSReference phantomReference;
	private final File scriptFile;
	private final String scriptArgs[];
	private final String consolePrefix;
	private final List<String> consolePostfix;
	private Process process;

	public PhantomJSConsoleExecutor(final PhantomJSReference phantomReference) {
		this(phantomReference, null);
	}

	public PhantomJSConsoleExecutor(final PhantomJSReference phantomReference, final File scriptFile, final String... scriptArgs) {
		this(phantomReference, DEFAULT_PHANTOMJS_CONSOLE_PREFIX, DEFAULT_PHANTOMJS_CONSOLE_POSTFIXS, scriptFile, scriptArgs);
	}
	
	public PhantomJSConsoleExecutor(final PhantomJSReference phantomReference, final String consolePrefix, final List<String> consolePostfix, final File scriptFile, final String... scriptArgs) {
		this.phantomReference = phantomReference;
		this.scriptFile = scriptFile;
		this.scriptArgs = scriptArgs;
		this.consolePrefix = consolePrefix;
		this.consolePostfix = consolePostfix;
	}

	public int getPid() {
		if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
			/* get the PID on unix/linux systems */
			try {
				Field f = process.getClass().getDeclaredField("pid");
				f.setAccessible(true);
				return f.getInt(process);
			} catch (Throwable e) {
			}
		}

		return -1;
	}

	public boolean isAlive() {
		try {
			process.exitValue();
			return false;
		} catch (IllegalThreadStateException ex) {
			return true;
		}
	}

	public void start() {
		try {
			String cmd = this.phantomReference.getBinaryPath();

			if (scriptFile != null) {
				cmd = cmd + " " + scriptFile.getAbsolutePath();
			}

			if (scriptArgs != null && scriptArgs.length > 0) {
				cmd = cmd + " " + StringUtils.join(scriptArgs, " ");
			}

			process = Runtime.getRuntime().exec(cmd);
			if (StringUtils.isNotBlank(consolePrefix)) {
				process.getInputStream().read(new byte[consolePrefix.length()]);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public int destroy() {
		try {
			process.destroy();
		} catch (Exception e) {
		}

		if (isAlive()) {
			try {
				return process.waitFor();
			} catch (InterruptedException e) {

			}
		}

		return process.exitValue();
	}

	public CompletableFuture<String> execute(final String scriptSource) {
		return this.execute(IOUtils.toInputStream(scriptSource, Charset.defaultCharset()), consolePostfix);
	}

	public CompletableFuture<String> execute(final String scriptSource, String... endLines) {
		return this.execute(IOUtils.toInputStream(scriptSource, Charset.defaultCharset()), asList(endLines));
	}

	public CompletableFuture<String> execute(final InputStream scriptSourceInputStream, String... endLines) {
		return this.execute(scriptSourceInputStream, asList(endLines));
	}

	public CompletableFuture<String> execute(final InputStream scriptSourceInputStream, final List<String> endLines)
			throws UnexpectedProcessEndException {
		return CompletableFuture.supplyAsync(() -> doExecute(scriptSourceInputStream, endLines), executorService);
	}
	
	private String doExecute(final InputStream scriptSourceInputStream, final List<String> endLines) {

		if (!isAlive()) {
			throw new UnexpectedProcessEndException();
		}

		try {
			final String input = copy(scriptSourceInputStream, process.getOutputStream());
			// Append Enter to the input

			if (!endWithNewLine(input)) {
				for (char c : SYSTEM_NEWLINE) {
					process.getOutputStream().write(c);
				}
			}

			process.getOutputStream().flush();

			final String output = readPhantomJSOutput(process.getInputStream(), endLines);

			LOGGER.debug("Program output: " + output);

			return output;
		} catch (IOException e) {
			throw new UnexpectedProcessEndException(e);
		}
	}

	private String readPhantomJSOutput(InputStream processInput, List<String> endLines) throws IOException {
		final StringBuilder out = new StringBuilder();
		final BufferedReader in = new BufferedReader(new InputStreamReader(processInput, "UTF-8"));

		while (true) {
			final String line = in.readLine();

			LOGGER.trace("Incoming line from process: " + line);
			
			if (line.equals(PHANTOMJS_PARSER_ERROR_PREFIX)) {
				return line;
			}

			if (line == null || endLines.contains(line)) {
				if (StringUtils.isNotBlank(consolePrefix)) {
					in.skip(consolePrefix.length());
				}
				break;
			}

			if (out.length() > 0) {
				out.append("\n");
			}

			out.append(line);
		}

		return out.toString();
	}

	private boolean endWithNewLine(final String input) {
		return input.endsWith(String.valueOf(SYSTEM_NEWLINE));
	}

	private String copy(final InputStream input, final OutputStream output) throws IOException {
		final StringBuilder inputString = new StringBuilder();
		final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int n = 0;
		while (EOF != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			inputString.append(new String(buffer, 0, n));
		}
		return inputString.toString();
	}
	
	public String getConsolePrefix() {
		return consolePrefix;
	}
	public List<String> getConsolePostfix() {
		return consolePostfix;
	}
}
