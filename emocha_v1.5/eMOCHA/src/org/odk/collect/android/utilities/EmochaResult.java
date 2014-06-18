package org.odk.collect.android.utilities;

/**
 * EmochaResult is used to provide return values when saving data into 
 * string. Look for who is calling it and what the content will be shown.
 * @author 
 *
 */
public class EmochaResult {
	public int code;
	public String content;
	
	public EmochaResult(int code, String c) {
		this.code = code;
		this.content = c;
	}
}
