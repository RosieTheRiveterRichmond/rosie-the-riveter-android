/* Copyright (c) 2014 - 2017 Bradley Justice
MIT LICENSE
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.blueverdi.rosietheriveter;

import java.io.File;

import android.app.Activity;
import android.content.Context;

public class MyMapsforgeTheme {

	private static final String TAG = "MyMapsforgeTheme";
	private static final String THEME = "elegant";
	private static final String THEME_240 = "Elegant_City.xml";
	private static final String THEME_320 = "Elegant_City_L.xml";
	private static final String THEME_480 = "Elegant_City_XL.xml";
	private static final String RESOURCES_240 = "ele_res.zip";
	private static final String RESOURCES_320 = "ele_res_l.zip";
	private static final String RESOURCES_480 = "ele_res_xl.zip";
	public enum ThemeState {
		CREATED, INITIALIZING, READY, ERROR
	};
	private static  ThemeState themeState = ThemeState.CREATED;

	MyMapsforgeTheme(Context context) {
		
		if (themeState != ThemeState.CREATED) {
			return;
		}
		if (!FileUtils.unzip(context,THEME + ".zip" )) {
			MyLog.d(TAG, "error unzipping theme");
			themeState = ThemeState.ERROR;
			return;
		}
		if (!FileUtils.unzip(context,RESOURCES_240)) {
			MyLog.d(TAG, "error unzipping RESOURCES_240");
			themeState = ThemeState.ERROR;
			return;
		}
		if (!FileUtils.unzip(context,RESOURCES_320)) {
			MyLog.d(TAG, "error unzipping RESOURCES_320");
			themeState = ThemeState.ERROR;
			return;
		}
		if (!FileUtils.unzip(context,RESOURCES_480)) {
			MyLog.d(TAG, "error unzipping RESOURCES_480");
			themeState = ThemeState.ERROR;
			return;
		}
		themeState = ThemeState.READY;

	}
	
	public File getTheme(Activity context) {

		if (themeState != ThemeState.READY) {
			return null;
		}
		int dpi = Utils.getDpi(context);
		String dirPath = context.getFilesDir().getAbsolutePath() + File.separator + THEME;
		File dirFile = new File(dirPath);
		if (!dirFile.exists()) {
			return null;
		}
		File f;
		if (dpi < 320) {
			f = new File(dirFile, THEME_240);
		}
		else if (dpi < 480) {
			f = new File(dirFile, THEME_320);
		}
		else {
			f = new File(dirFile, THEME_480);
		}
		if (f.exists()) {
			return f;

		}
		else {
			return null;
		}

	}
	
	public static ThemeState getState() {
		return themeState;
	}
	
}
