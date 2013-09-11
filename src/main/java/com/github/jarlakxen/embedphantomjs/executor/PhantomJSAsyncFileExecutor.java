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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.github.jarlakxen.embedphantomjs.PhantomJSReference;

public class PhantomJSAsyncFileExecutor extends PhantomJSFileExecutor<Future<String>> {

	private ExecutorService executorService;
	
	public PhantomJSAsyncFileExecutor(PhantomJSReference phantomReference){
		this(phantomReference, Executors.newSingleThreadExecutor());
	}
	
	public PhantomJSAsyncFileExecutor(PhantomJSReference phantomReference, ExecutorService executorService) {
		super(phantomReference);
		this.executorService = executorService;
	}

	@Override
	public Future<String> execute(final File sourceFile, final String... args) {
		return executorService.submit(new Callable<String>() {
			@Override
			public String call() throws Exception {
				return doExecute(sourceFile, args);
			}
		});
	}
}
