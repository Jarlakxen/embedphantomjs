package com.github.jarlakxen.embedphantomjs;

import java.util.concurrent.TimeUnit;

public class ExecutionTimeout {

	private long timeout;
	private TimeUnit unit;

	public ExecutionTimeout(long timeout, TimeUnit unit) {
		this.timeout = timeout;
		this.unit = unit;
	}

	public long getTimeout() {
		return timeout;
	}

	public TimeUnit getUnit() {
		return unit;
	}
}
