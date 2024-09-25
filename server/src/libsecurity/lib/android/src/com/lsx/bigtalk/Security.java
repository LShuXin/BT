/*
 * @LastEditors: liushuxin
 * @LastEditTime: 2024-06-26 22:23:51
 * @FilePath: /BT/server/src/libsecurity/lib/android/src/com/lsx/bigtalk/Security.java
 * @Description: 
 * 
 * Copyright (c) 2024 by liushuxina@gmail.com All Rights Reserved. 
 */
package com.lsx.bigtalk;

public class Security {
	public native byte[] EncryptMsg(String strMsg);
	public native byte[] DecryptMsg(String strMsg);

	public native byte[] EncryptPwd(String strPwd);
	
	static{
		System.loadLibrary("security");
	}
	
	private static Security instance;
	
	public static Security getInstance() {
		synchronized (Security.class) {
			if (instance == null) {
				instance = new Security();
			}

			return instance;
		}
	}
}
