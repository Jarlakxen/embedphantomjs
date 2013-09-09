/**
 * Copyright (C) 2012
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
package com.fviale.embed.phantomjs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class PhantomJSExecutor {

	private static final Logger LOGGER = Logger.getLogger(PhantomJSExecutor.class);

	public static final String PHANTOMJS_CONSOLE_PREFIX = "phantomjs> ";
	public static final String PHANTOMJS_CONSOLE_POSTFIX = "undefined\n";
	public static final String PHANTOMJS_NATIVE_CMD = "phantomjs";
	public static final String PHANTOMJS_DATA_FILE = "phantomjs/data.properties";

	private Configuration configuration;
	private String phantomJSExecutablePath;

	public PhantomJSExecutor() {
		this(new Configuration());
	}

	public PhantomJSExecutor(Configuration configuration) {
		this.configuration = configuration;
		this.phantomJSExecutablePath = this.getPhantomJSExecutablePath();
	}

	public String execute(File sourceFile, String... args) {
		try {
			String cmd = this.phantomJSExecutablePath + " " + sourceFile.getAbsolutePath() + " " + StringUtils.join(args, " ");
			LOGGER.debug("Command to execute: " + cmd);
			Process process = Runtime.getRuntime().exec(cmd);
			String output = IOUtils.toString(process.getInputStream());
			process.waitFor();
			return output;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String execute(String source) {
		return this.execute(IOUtils.toInputStream(source));
	}

	public String execute(InputStream sourceInputStream) {
		try {
			Process process = Runtime.getRuntime().exec(this.phantomJSExecutablePath);
			IOUtils.copy(sourceInputStream, process.getOutputStream());
			process.getOutputStream().close();
			String output = IOUtils.toString(process.getInputStream());
			process.waitFor();
			// Remove the console at the beginning of the output

			boolean hasPrefix = output.startsWith(PHANTOMJS_CONSOLE_PREFIX);
			boolean hasPostfix = output.endsWith(PHANTOMJS_CONSOLE_POSTFIX);

			if (hasPrefix && hasPostfix) {
				output = output.substring(PHANTOMJS_CONSOLE_PREFIX.length(), output.length() - PHANTOMJS_CONSOLE_POSTFIX.length());
			} else if (hasPrefix && !hasPostfix) {
				output = output.substring(PHANTOMJS_CONSOLE_PREFIX.length());
			} else if (!hasPrefix && hasPostfix) {
				output = output.substring(0, output.length() - PHANTOMJS_CONSOLE_POSTFIX.length());
			}

			return output;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String getPhantomJSExecutablePath() {

		// Check if phantomjs is installed locally
		if (this.configuration.getCheckNativeInstallation()) {
			LOGGER.debug("Checking PhantomJS native installation");
			if (this.checkPhantomJSInstall(PHANTOMJS_NATIVE_CMD)) {
				LOGGER.debug("Native installation founded");
				return PHANTOMJS_NATIVE_CMD;
			}
		}

		if (!this.configuration.getVersion().isDownloadSopported()) {
			throw new RuntimeException("Unsopported version for downloading!");
		}

		// Check if phantomjs is already installed in target path
		String targetPath = this.configuration.getTargetInstallationFolder() + "/" + this.configuration.getVersion().getDescription()
				+ "/phantomjs";
		LOGGER.debug("Checking PhantomJS installation in " + targetPath);
		if (this.checkPhantomJSInstall(targetPath)) {
			LOGGER.debug("PhantomJS founded in " + targetPath);
			return targetPath;
		}

		LOGGER.debug("PhantomJS not founded in " + targetPath);

		// Try download phantomjs
		try {

			Properties properties = new Properties();
			properties.load(this.getClass().getClassLoader().getResourceAsStream(PHANTOMJS_DATA_FILE));

			String osHost = this.getOsHost();

			String name = properties.getProperty(this.configuration.getVersion().getDescription() + "." + osHost + ".name");

			String architecture = this.configuration.getArchitecture().indexOf("64") >= 0 ? "x86_64" : "i686";

			LOGGER.debug("System Data: Arch [" + architecture + "] - OS [" + osHost + "]");

			if (osHost.equals("linux")) {
				name = String.format(name, architecture);
			}

			// Download PhantomJS
			URL downloadPath = new URL(this.configuration.getDownloadUrl() + name);
			File phantomJsCompressedFile = new File(System.getProperty("java.io.tmpdir") + "/" + name);

			LOGGER.debug("Downloading " + downloadPath.getPath() + " ...");

			FileUtils.copyURLToFile(downloadPath, phantomJsCompressedFile);

			ArchiveInputStream archiveInputStream = null;

			if (phantomJsCompressedFile.getName().endsWith(".zip")) {

				archiveInputStream = new ZipArchiveInputStream(new FileInputStream(phantomJsCompressedFile));

			} else if (phantomJsCompressedFile.getName().endsWith(".bz2")) {

				archiveInputStream = new TarArchiveInputStream(new BZip2CompressorInputStream(new FileInputStream(phantomJsCompressedFile)));

			} else if (phantomJsCompressedFile.getName().endsWith(".gz")) {

				archiveInputStream = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(phantomJsCompressedFile)));

			}

			String outputBinaryPath = null;

			ArchiveEntry entry;
			while ((entry = archiveInputStream.getNextEntry()) != null) {
				if (entry.getName().endsWith("/bin/phantomjs")) {

					// Create target folder
					new File(this.configuration.getTargetInstallationFolder() + "/" + this.configuration.getVersion().getDescription())
							.mkdirs();

					// Create empty binary file
					File output = new File(this.configuration.getTargetInstallationFolder() + "/"
							+ this.configuration.getVersion().getDescription() + "/phantomjs");
					if (!output.exists()) {
						output.createNewFile();
						output.setExecutable(true);
						output.setReadable(true);
					}

					// Untar the binary file
					FileOutputStream outputBinary = new FileOutputStream(output);

					IOUtils.copy(archiveInputStream, outputBinary);

					outputBinary.close();

					outputBinaryPath = output.getAbsolutePath();
				}
			}

			archiveInputStream.close();

			return outputBinaryPath;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Boolean checkPhantomJSInstall(String path) {
		try {
			Process process = Runtime.getRuntime().exec(path + " --version");
			process.waitFor();

			String processOutput = IOUtils.toString(process.getInputStream());

			if (this.configuration.getVersion().equals(Version.fromValue(processOutput.substring(0, 5)))) {
				return true;
			}
		} catch (Exception e) {
			LOGGER.warn(e);
		}

		return false;
	}

	private String getOsHost() {
		if (this.configuration.getHostOs().indexOf("linux") >= 0) {
			return "linux";
		} else if (this.configuration.getHostOs().indexOf("win") >= 0) {
			return "win";
		} else if (this.configuration.getHostOs().indexOf("mac") >= 0) {
			return "macosx";
		} else {
			throw new RuntimeException("Unsopported operation system!");
		}
	}

	public Configuration getConfiguration() {
		return this.configuration;
	}
}
