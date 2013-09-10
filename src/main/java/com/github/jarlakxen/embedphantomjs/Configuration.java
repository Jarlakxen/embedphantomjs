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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Configuration {

	public static ConfigurationBuilder create(){
		return new ConfigurationBuilder();
	}
	
    /***
     * This inner builder is defined because we want immutable BusinessDomain object.
     * Ref. Effective Java Second Edition
     * Pag. 14.
     * 
     */
	public static class ConfigurationBuilder {
		private Version version = Version.v_1_9_2;
		private String architecture = System.getProperty("os.arch").toLowerCase();
		private String hostOs = System.getProperty("os.name").toLowerCase();

		private String downloadUrl = "http://phantomjs.googlecode.com/files/";
		private String targetInstallationFolder = System.getProperty("user.home") + "/.embedphantomjs";

		private boolean checkNativeInstallation = true;
		
		private ExecutorService executor = Executors.newSingleThreadExecutor();
		
		public ConfigurationBuilder withVersion(Version version){
			this.version = version;
			return this;
		}
		
		public ConfigurationBuilder withArchitecture(String architecture){
			this.architecture = architecture;
			return this;
		}
		
		public ConfigurationBuilder withHostOS(String hostOs){
			this.hostOs = hostOs;
			return this;
		}

		public ConfigurationBuilder useDownloadUrl(String downloadUrl){
			this.downloadUrl = downloadUrl;
			return this;
		}
		
		public ConfigurationBuilder useTargetInstallationFolder(String targetInstallationFolder){
			this.targetInstallationFolder = targetInstallationFolder;
			return this;
		}
		
		public ConfigurationBuilder withThreadExecutor(ExecutorService executor){
			this.executor = executor;
			return this;
		}
		
		public ConfigurationBuilder useNativeInstallation(boolean checkNativeInstallation){
			this.checkNativeInstallation = checkNativeInstallation;
			return this;
		}
		
		public Configuration build(){
			return new Configuration(this);
		}
	}
	
	private Version version;
	private String architecture;
	private String hostOs;
	private String downloadUrl;
	private String targetInstallationFolder;
	private boolean checkNativeInstallation;
	private ExecutorService executor;
	
	private Configuration(ConfigurationBuilder builder) {
		this.version = builder.version;
		this.architecture = builder.architecture;
		this.hostOs = builder.hostOs;
		this.downloadUrl = builder.downloadUrl;
		this.targetInstallationFolder = builder.targetInstallationFolder;
		this.checkNativeInstallation = builder.checkNativeInstallation;
		this.executor = builder.executor;
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

	public boolean getCheckNativeInstallation() {
		return this.checkNativeInstallation;
	}
	
	public ExecutorService getExecutor() {
		return executor;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
