package com.jarlakxen.embed;

public enum PhantomJSVersion {
	v_1_7_0("1.7.0", true), v_1_6_1("1.6.1", true), v_1_6_0("1.6.0", true), v_1_5_0("1.5.0", true), v_1_4_1("1.4.1", true), v_1_4_0("1.4.0", false), v_1_3_0("1.3.0", false);

	public static PhantomJSVersion fromValue(String version) {

		for (PhantomJSVersion value : PhantomJSVersion.values()) {
			if (value.getDescription().equals(version)) {
				return value;
			}
		}

		return null;
	}

	private String description;
	private boolean downloadSopported;

	private PhantomJSVersion(String description, boolean downloadSopported) {
		this.description = description;
		this.downloadSopported = downloadSopported;
	}

	public String getDescription() {
		return description;
	}
	
	public boolean isDownloadSopported() {
		return downloadSopported;
	}

	@Override
	public String toString() {
		return getDescription();
	}

}
