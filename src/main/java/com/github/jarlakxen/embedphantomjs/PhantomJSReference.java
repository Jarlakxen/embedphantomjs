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
package com.github.jarlakxen.embedphantomjs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;

public class PhantomJSReference {

	private static final Logger LOGGER = Logger.getLogger(PhantomJSReference.class);

	public static final String PHANTOMJS_NATIVE_CMD = "/usr/bin/phantomjs";

	public static final String PHANTOMJS_DATA_FILE = "phantomjs/data.properties";

	public static final String PHANTOMJS_DOWNLOAD_BINARY_PATH = "/bin/phantomjs";

	public static PhantomJSReferenceBuilder create() {
		return new PhantomJSReferenceBuilder();
	}

	/***
	 * This inner builder is defined because we want immutable BusinessDomain
	 * object. Ref. Effective Java Second Edition Pag. 14.
	 * 
	 */
	public static class PhantomJSReferenceBuilder {
		private Version version = Version.v_1_9_2;
		private String architecture = System.getProperty("os.arch").toLowerCase();
		private String hostOs;

		private String downloadUrl = "http://phantomjs.googlecode.com/files/";
		private String targetInstallationFolder = System.getProperty("user.home") + "/.embedphantomjs";

		private String commandLineOptions;
		
		public PhantomJSReferenceBuilder withVersion(Version version) {
			this.version = version;
			return this;
		}

		public PhantomJSReferenceBuilder withArchitecture(String architecture) {
			this.architecture = architecture;
			return this;
		}

		public PhantomJSReferenceBuilder withHostOS(String hostOs) {
			this.hostOs = hostOs;
			return this;
		}

		public PhantomJSReferenceBuilder useDownloadUrl(String downloadUrl) {
			this.downloadUrl = downloadUrl;
			return this;
		}

		public PhantomJSReferenceBuilder addCommandLineOptions(final String... commandLineOptions){
			this.commandLineOptions = StringUtils.join(commandLineOptions, " ");
			return this;
		}
		public PhantomJSReferenceBuilder useTargetInstallationFolder(String targetInstallationFolder) {
			this.targetInstallationFolder = targetInstallationFolder;
			return this;
		}

		public PhantomJSReference build() {
			return new PhantomJSReference(this);
		}
		
		
		public PhantomJSReferenceBuilder()
		{
			String os = System.getProperty("os.name").toLowerCase();
			if (os.contains("win"))
				hostOs = "win";
			else if (os.contains("mac"))
				hostOs = "macosx";
			else
				hostOs = "linux";
		}
	}

	private Version version;
	private String architecture;
	private String hostOs;
	private String downloadUrl;
	private String targetInstallationFolder;
	private String binaryPath;
	private String commandLineOptions;

	private PhantomJSReference(PhantomJSReferenceBuilder builder) {
		this.version = builder.version;
		this.architecture = builder.architecture;
		this.hostOs = builder.hostOs;
		this.downloadUrl = builder.downloadUrl;
		this.targetInstallationFolder = builder.targetInstallationFolder;
		this.commandLineOptions = builder.commandLineOptions ==null?"":builder.commandLineOptions;
	}

	public String getBinaryPath() {
		if (binaryPath == null) {
			ensureBinary();
		}
		return binaryPath;
	}

	public synchronized void ensureBinary() {

		if (binaryPath != null) {
			// The binary is already ensure
			return;
		}

		// Check if phantomjs is installed locally
		if (Version.NATIVE.equals(version)) {

			LOGGER.debug("Checking PhantomJS native installation");
			if (this.checkPhantomJSBinaryAnyVersion(PHANTOMJS_NATIVE_CMD)) {
				LOGGER.debug("Native installation founded");
				binaryPath = PHANTOMJS_NATIVE_CMD;
				return;
			} else {
				throw new RuntimeException("Invalid native installation!");
			}
		}

		if (!getVersion().isDownloadSopported()) {
			throw new RuntimeException("Unsopported version for downloading!");
		}

		File binaryFile = new File(this.getTargetInstallationFolder() + "/" + this.getVersion().getDescription() + "/phantomjs");
		String binaryFilePath = binaryFile.getAbsolutePath();

		// Check if phantomjs is already installed in target path
		LOGGER.debug("Checking PhantomJS installation in " + binaryFilePath);
		if (this.checkPhantomJSBinaryVersion(binaryFilePath, getVersion())) {
			LOGGER.debug("PhantomJS founded in " + binaryFilePath);
			binaryPath = binaryFilePath;
		} else {
			LOGGER.debug("PhantomJS not founded in " + binaryFilePath);

			try {
				downloadPhantomJS(binaryFile);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			if (!this.checkPhantomJSBinaryVersion(binaryFilePath, getVersion())) {
				throw new RuntimeException("Invalid download");
			}

			binaryPath = binaryFilePath;
		}
	}

	public String getCommandLineOptions() {
		return commandLineOptions;
	}

	private void downloadPhantomJS(File binaryFile) throws IOException {
		Properties properties = new Properties();
		properties.load(this.getClass().getClassLoader().getResourceAsStream(PHANTOMJS_DATA_FILE));

		String name = properties.getProperty(this.getVersion().getDescription() + "." + this.getHostOs() + ".name");

		String architecture = this.getArchitecture().indexOf("64") >= 0 ? "x86_64" : "i686";

		LOGGER.debug("System Data: Arch [" + architecture + "] - OS [" + this.getHostOs() + "]");

		if (this.getHostOs().equals("linux")) {
			name = String.format(name, architecture);
		}

		// Download PhantomJS
		URL downloadPath = new URL(this.getDownloadUrl() + name);
		File phantomJsCompressedFile = new File(System.getProperty("java.io.tmpdir") + "/" + name);

		LOGGER.info("Downloading " + downloadPath.getPath() + " ...");

		FileUtils.copyURLToFile(downloadPath, phantomJsCompressedFile);

		ArchiveInputStream archiveInputStream = null;

		if (phantomJsCompressedFile.getName().endsWith(".zip")) {

			archiveInputStream = new ZipArchiveInputStream(new FileInputStream(phantomJsCompressedFile));

		} else if (phantomJsCompressedFile.getName().endsWith(".bz2")) {

			archiveInputStream = new TarArchiveInputStream(new BZip2CompressorInputStream(new FileInputStream(phantomJsCompressedFile)));

		} else if (phantomJsCompressedFile.getName().endsWith(".gz")) {

			archiveInputStream = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(phantomJsCompressedFile)));

		}

		ArchiveEntry entry;
		while ((entry = archiveInputStream.getNextEntry()) != null) {
			if (entry.getName().endsWith(PHANTOMJS_DOWNLOAD_BINARY_PATH) || entry.getName().toLowerCase().endsWith("phantomjs.exe")) {

				// Create target folder
				new File(this.getTargetInstallationFolder() + "/" + this.getVersion().getDescription()).mkdirs();

				FileUtils.forceMkdir(new File(binaryFile.getParent()));

				if (!binaryFile.exists()) {
					binaryFile.createNewFile();
				}

				binaryFile.setExecutable(true);
				binaryFile.setReadable(true);

				// Untar the binary file
				FileOutputStream outputBinary = new FileOutputStream(binaryFile);

				LOGGER.info("Un-compress download to " + downloadPath.getPath() + " ...");
				IOUtils.copy(archiveInputStream, outputBinary);

				outputBinary.close();
			}
		}

		archiveInputStream.close();
	}

	private String checkPhantomJSBinary(String path) {
		try {
			Process process = Runtime.getRuntime().exec(path + " --version");
			process.waitFor();

			String processOutput = IOUtils.toString(process.getInputStream());

			return processOutput.substring(0, 5);

		} catch (Exception e) {
			LOGGER.warn(e);
		}

		return null;
	}

	private Boolean checkPhantomJSBinaryAnyVersion(String path) {
		String outputVersion = checkPhantomJSBinary(path);
		return Version.fromValue(outputVersion) != null;
	}

	private Boolean checkPhantomJSBinaryVersion(String path, Version version) {
		String outputVersion = checkPhantomJSBinary(path);
		return version.getDescription().equals(outputVersion);
	}

	public Version getVersion() {
		return this.version;
	}

	public String getHostOs() {
		return this.hostOs;
	}

	public String getArchitecture() {
		return this.architecture;
	}

	public String getDownloadUrl() {
		return this.downloadUrl;
	}

	public String getTargetInstallationFolder() {
		return this.targetInstallationFolder;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
