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

import com.github.jarlakxen.embedphantomjs.PhantomJSReference;

public class PhantomJSSyncFileExecutor extends PhantomJSFileExecutor<String> {

	public PhantomJSSyncFileExecutor(PhantomJSReference phantomReference) {
		super(phantomReference);
	}

	@Override
	public String execute(File sourceFile, String... args) {
		return doExecute(sourceFile, args);
	}
}
