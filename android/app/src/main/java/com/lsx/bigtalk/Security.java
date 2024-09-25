package com.lsx.bigtalk;

public class Security {
	public native byte[] DecryptMsg(String strMsg);
	public native byte[] EncryptMsg(String strMsg);

	public native byte[] EncryptPwd(String strPwd);
	
	static {
		System.loadLibrary("security");
	}
	
	private static Security instance;
	
	public static Security getInstance() {
		synchronized (Security.class) {
			if (null == instance) {
				instance = new Security();
			}

			return instance;
		}
	}
}
