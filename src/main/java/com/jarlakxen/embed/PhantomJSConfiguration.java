package com.jarlakxen.embed;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class PhantomJSConfiguration {

	private PhantomJSVersion version = PhantomJSVersion.v_1_7_0;
	private String architecture = System.getProperty("os.arch").toLowerCase();
	private String hostOs = System.getProperty("os.name").toLowerCase();
	
	private String downloadUrl = "http://phantomjs.googlecode.com/files/";
	private String downloadExtension = "tar.bz2";
	private String targetInstallationFolder = System.getProperty("user.home") + "/.embedphantomjs";
	
	private boolean checkNativeInstallation = true;

	public PhantomJSVersion getVersion() {
		return version;
	}

	public PhantomJSConfiguration setVersion(PhantomJSVersion version) {
		this.version = version;
		return this;
	}
	
	public String getHostOs() {
		return hostOs;
	}
	
	public PhantomJSConfiguration setHostOs(String hostOs) {
		this.hostOs = hostOs;
		return this;
	}

	public String getArchitecture() {
		return architecture;
	}
	
	public PhantomJSConfiguration setArchitecture(String architecture) {
		this.architecture = architecture;
		return this;
	}
	
	public String getDownloadUrl() {
		return downloadUrl;
	}

	public PhantomJSConfiguration setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
		return this;
	}

	public String getDownloadExtension() {
		return downloadExtension;
	}

	public PhantomJSConfiguration setDownloadExtension(String downloadExtension) {
		this.downloadExtension = downloadExtension;
		return this;
	}
	
	public String getTargetInstallationFolder() {
		return targetInstallationFolder;
	}
	
	public PhantomJSConfiguration setTargetInstallationFolder(String targetInstallationFolder) {
		this.targetInstallationFolder = targetInstallationFolder;
		return this;
	}

	public boolean getCheckNativeInstallation() {
		return checkNativeInstallation;
	}

	public PhantomJSConfiguration setCheckNativeInstallation(boolean checkNativeIsntallation) {
		this.checkNativeInstallation = checkNativeIsntallation;
		return this;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
