package org.emocha.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Date {
	
	private DateFormat df;
	
	public Date() {
		
	}
	
	public String[] getCurrentDate() {
		df = new SimpleDateFormat("EEEEEEEEEE,dd MMM yyyy");
		String date = df.format(Calendar.getInstance().getTime());
		String[] s =  date.split(",");
		return s;
	}
}
