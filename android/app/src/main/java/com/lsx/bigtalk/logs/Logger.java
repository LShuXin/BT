package com.lsx.bigtalk.logs;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Logger {
	private final String tag = "BTLoggerUtil";
	private static int logLevel = Log.ERROR;
	private static Logger instance;
	private final Lock lock;

	private Logger() {
		lock = new ReentrantLock();
	}

	public static synchronized Logger getLogger(Class<?> key) {
		if (null == instance) {
			instance = new Logger();
		}
		return instance;
	}

	private String getFunctionName() {
		StackTraceElement[] sts = Thread.currentThread().getStackTrace();

		if (null == instance) {
			return null;
		}

		for (StackTraceElement st : sts) {
			if (st.isNativeMethod()) {
				continue;
			}

			if (st.getClassName().equals(Thread.class.getName())) {
				continue;
			}

			if (st.getClassName().equals(this.getClass().getName())) {
				continue;
			}

			return "[" + st.getFileName() + ":" + st.getLineNumber() + "]";
		}

		return null;
	}

	private String createMessage(String msg) {
		String functionName = getFunctionName();
		long threadId = Thread.currentThread().getId();
		String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date());
		String message = (functionName == null ? msg : (functionName + " - "
				+ threadId + " - " + msg));
        return currentTime + " - " + message;
	}

	private String getInputString(String format, Object... args) {
		if (format == null) {
			return "null log format";
		}

		return String.format(format, args);
	}

	/**
	 * log.i
	 */
	public void i(String format, Object... args) {
		if (logLevel <= Log.INFO) {
			lock.lock();
			try {
				String message = createMessage(getInputString(format, args));
				Log.i(tag, message);
			} finally {
				lock.unlock();
			}
		}
	}

	/**
	 * log.v
	 */
	public void v(String format, Object... args) {
		if (logLevel <= Log.VERBOSE) {
			lock.lock();
			try {
				String message = createMessage(getInputString(format, args));
				Log.v(tag, message);
			} finally {
				lock.unlock();
			}
		}
	}

	/**
	 * log.d
	 */
	public void d(String format, Object... args) {
		if (logLevel <= Log.DEBUG) {
			lock.lock();
			try {
				String message = createMessage(getInputString(format, args));
				Log.d(tag, message);
			} finally {
				lock.unlock();
			}
		}
	}

	/**
	 * log.e
	 */
	public void e(String format, Object... args) {
		if (logLevel <= Log.ERROR) {
			lock.lock();
			try {
				String message = createMessage(getInputString(format, args));
				Log.e(tag, message);
			} finally {
				lock.unlock();
			}
		}
	}

	/**
	 * log.error
	 */
	public void error(Exception e) {
		if (logLevel <= Log.ERROR) {
			StringBuilder sb = new StringBuilder();
			lock.lock();
			try {
				String name = getFunctionName();
				StackTraceElement[] sts = e.getStackTrace();

				if (name != null) {
					sb.append(name).append(" - ").append(e).append("\r\n");
				} else {
					sb.append(e).append("\r\n");
				}
				if (instance != null) {
					for (StackTraceElement st : sts) {
						if (st != null) {
							sb.append("[ ")
									.append(st.getFileName())
									.append(":")
									.append(st.getLineNumber())
									.append(" ]\r\n");
						}
					}
				}
				Log.e(tag, sb.toString());
			} finally {
				lock.unlock();
			}
		}
	}

	/**
	 * log.d
	 */
	public void w(String format, Object... args) {
		if (logLevel <= Log.WARN) {
			lock.lock();
			try {
				String message = createMessage(getInputString(format, args));
				Log.w(tag, message);
			} finally {
				lock.unlock();
			}
		}
	}

	/**
	 * set log level
	 */
	public void setLevel(int l) {
		lock.lock();
		try {
			logLevel = l;
		} finally {
			lock.unlock();
		}
	}

}
