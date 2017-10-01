package com.xcx.system.util;

public class PublicUtil {

	public static String convertPicDate(int picDate) {
		String picDateStr = picDate + "";
		String year = picDateStr.substring(0, 4);
		String month = picDateStr.substring(4, 6);
		String day = picDateStr.substring(6, 8);
		return year + "年" + month + "月" + day + "日";
	}
}
