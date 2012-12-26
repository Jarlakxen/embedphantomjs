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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jarlakxen.embed.PhantomJSExecutor;

public class PhantomJSExecutorTest {

	private static File test1 = new File(System.getProperty("java.io.tmpdir") + "/embedphantomjs.test1.js");
	
	@BeforeClass
	public static void setUpClass() throws IOException{
		test1.createNewFile();
		FileUtils.write(test1, "console.log('TEST1'); phantom.exit();");
	}
	
	@AfterClass
	public static void tearDownClass() throws IOException{
		test1.delete();
	}
	
	@Test
	public void test(){
		PhantomJSExecutor ex = new PhantomJSExecutor(new PhantomJSConfiguration().setCheckNativeInstallation(false));
		Assert.assertEquals("TEST1\n", ex.execute(test1.getAbsolutePath()));
	}

}
