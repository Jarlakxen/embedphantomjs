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
package com.github.jarlakxen.embedphantomjs;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.jarlakxen.embedphantomjs.exception.UnexpectedProcessEndException;
import com.github.jarlakxen.embedphantomjs.executor.PhantomJSConsoleExecutor;
import com.github.jarlakxen.embedphantomjs.executor.PhantomJSFileExecutor;

public class PhantomJSExecutorTest {

    private static final String DEFAULT_FILE_JS = "console.log('TEST1');phantom.exit();";
    private static final String DEFAULT_CONSOLE_BOOTSTRAP = "var system = require('system');";
	private static final String DEFAULT_CONSOLE_JS = "system.standardout.writeLine('TEST1')";
			
    private static final File test1 = new File(System.getProperty("java.io.tmpdir") + "/embedphantomjs.test1.js");
    
    private static final PhantomJSReference phantomJSRef = PhantomJSReference.create().build();

    @BeforeClass
    public static void setUpClass() throws IOException {
        test1.createNewFile();
        FileUtils.write(test1, DEFAULT_FILE_JS, Charset.defaultCharset());
        phantomJSRef.ensureBinary();
    }

    @AfterClass
    public static void tearDownClass() throws IOException {
        test1.delete();
    }
    
    @Test
    public void test_FileExecutor_FromString() throws InterruptedException, ExecutionException {
    	PhantomJSFileExecutor ex = new PhantomJSFileExecutor(phantomJSRef, new ExecutionTimeout(5, TimeUnit.SECONDS));
        assertEquals("TEST1"+String.format("%n"), ex.execute(DEFAULT_FILE_JS).get());
    }
    
    @Test
    public void test__FileExecutor_FromFile() throws InterruptedException, ExecutionException {
    	PhantomJSFileExecutor ex = new PhantomJSFileExecutor(phantomJSRef, new ExecutionTimeout(5, TimeUnit.SECONDS));
        assertEquals("TEST1"+String.format("%n"), ex.execute(test1).get());
    }
    
    @Test
    public void test_FileExecutor_FromString_Timeout() throws InterruptedException, ExecutionException {
    	PhantomJSFileExecutor ex = new PhantomJSFileExecutor(phantomJSRef, new ExecutionTimeout(100, TimeUnit.MILLISECONDS));
    	CompletableFuture<String> result = ex.execute("while(true){};");
    	Thread.sleep(200);
    	assertEquals(true,result.isDone());
    	assertEquals(true,result.isCompletedExceptionally());
    }
 
    @Test
    public void test_executor_FromConsole() throws UnexpectedProcessEndException, InterruptedException, ExecutionException {
    	PhantomJSConsoleExecutor ex = new PhantomJSConsoleExecutor(phantomJSRef);
    	ex.start();
    	ex.execute(DEFAULT_CONSOLE_BOOTSTRAP).get();
        assertEquals("TEST1", ex.execute(DEFAULT_CONSOLE_JS, "true").get());
        assertEquals("TEST1", ex.execute(DEFAULT_CONSOLE_JS, "true").get());
        ex.destroy();
    }
}
