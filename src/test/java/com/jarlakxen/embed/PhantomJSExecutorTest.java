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
