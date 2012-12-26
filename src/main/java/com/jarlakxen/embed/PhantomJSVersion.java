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
