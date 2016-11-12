package com.example.myapplication.utils;

public class MyException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 653990493857230528L;

	/**
	 * 错误
	 */
	public static final int SHOW_ERR_LOG = 0;
	public static final int SHOW_ERR_TOAST = 1;
	public static final int SHOW_ERR_NOTIFICATION = 1;
	
	/**
	 * Error in English
	 */
	public static final String ERR_LOGIN_ACCOUNT 	= "cannot find the user";
	public static final String ERR_LOGIN_PASSWD 	= "password not match";
	
	/**
	 * 中文提示
	 */
	public static final String ERR_LOGIN_ACCOUNT_ZHCN 	= "账号错误";
	public static final String ERR_LOGIN_PASSWD_ZHCN 	= "密码错误";
	public static final String ERR_NO_FARMINFO 			= "获取农场信息失败";
	public static final String ERR_NO_TRANSDUCERINFO 	= "获取传感器信息失败";
	public static final String ERR_NO_TRANSDUCERDATA 	= "获取传感器信息失败";
	
	
	public MyException(String message) {
		super(message);
	}

}
