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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.jarlakxen.embedphantomjs.executor.PhantomJSAsyncFileExecutor;
import com.github.jarlakxen.embedphantomjs.executor.PhantomJSConsoleExecutor;
import com.github.jarlakxen.embedphantomjs.executor.PhantomJSFileExecutor;
import com.github.jarlakxen.embedphantomjs.executor.PhantomJSSyncFileExecutor;

public class PhantomJSExecutorTest {

    private static String DEFAULT_FILE_JS = "console.log('TEST1');phantom.exit();";
	
    private static String DEFAULT_CONSOLE_BOOTSTRAP = "var system = require('system');system.stdout.writeLine('TEST1');";
    
	private static String DEFAULT_CONSOLE_JS = "system.stdout.writeLine('TEST1');";
			
    private static File test1 = new File(System.getProperty("java.io.tmpdir") + "/embedphantomjs.test1.js");

    @BeforeClass
    public static void setUpClass() throws IOException {
        test1.createNewFile();
        FileUtils.write(test1, DEFAULT_FILE_JS);
    }

    @AfterClass
    public static void tearDownClass() throws IOException {
        test1.delete();
    }

    @Test
    public void test_SyncExecutor_FromFile() {
    	PhantomJSFileExecutor<String> ex = new PhantomJSSyncFileExecutor(PhantomJSReference.create().build());
        assertEquals("TEST1\n", ex.execute(test1));
    }
    
    @Test
    public void test_AsyncExecutor_FromFile() throws InterruptedException, ExecutionException {
    	PhantomJSFileExecutor<Future<String>> ex = new PhantomJSAsyncFileExecutor(PhantomJSReference.create().build());
        assertEquals("TEST1\n", ex.execute(test1).get());
    }
    
    @Test
    public void test_executor_FromConsole() {
    	PhantomJSConsoleExecutor ex = new PhantomJSConsoleExecutor(PhantomJSReference.create().build());
    	ex.start();
    	ex.execute(DEFAULT_CONSOLE_BOOTSTRAP);
        assertEquals("TEST1\n", ex.execute(DEFAULT_CONSOLE_JS));
        assertEquals("TEST1\n", ex.execute(DEFAULT_CONSOLE_JS));
        ex.destroy();
    }
}
