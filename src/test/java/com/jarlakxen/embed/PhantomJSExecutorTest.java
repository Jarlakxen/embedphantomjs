package com.jarlakxen.embed;

import org.junit.Assert;
import org.junit.Test;

import com.jarlakxen.embed.PhantomJSExecutor;

public class PhantomJSExecutorTest {
	
	@Test
	public void test(){
		PhantomJSExecutor ex = new PhantomJSExecutor(new PhantomJSConfiguration().setCheckNativeInstallation(false));
		Assert.assertTrue(true);
	}

}
