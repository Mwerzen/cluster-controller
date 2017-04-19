package com.mikewerzen.servers.cluster.lifecycle.messaging;

import java.text.SimpleDateFormat

public class DateUtil {

	static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static String getCurrentDate() {
		return formatter.format(new Date());
	}

	public static Date convertStringToDate(String date) {
		return formatter.parse(date);
	}
}
