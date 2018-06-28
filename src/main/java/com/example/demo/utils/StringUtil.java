package com.example.demo.utils;


public class StringUtil {

	/**
	 * 判断字符串是否为NULL和空串。
	 * 
	 * @param str
	 *            字符串
	 * @return boolean
	 */
	public static boolean isEmpty(String str) {
		return !notEmpty(str);
	}
	/**
	 * 判断字符串是否不为NULL和空串。
	 * 
	 * @param str
	 *            字符串
	 * @return boolean
	 */
	public static boolean notEmpty(String str) {
		return str != null && str.trim().length() > 0;
	}


	
}
