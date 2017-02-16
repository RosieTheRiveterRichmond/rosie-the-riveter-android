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

import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class ImageZoomActivity extends Activity {
	
	private static final String TAG = "ImageViewActivity";
	public static final String IMAGE_FQN = "PathToImage";
	private RosieHelpMenu rosieHelpMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle b = getIntent().getExtras();
		String image = b.getString(IMAGE_FQN);
		if (image == null) {
			MyLog.d(TAG, "no image passed");
			finish();
		}
		setContentView(R.layout.activity_image_zoom);
		it.sephiroth.android.library.imagezoom.ImageViewTouch view = (it.sephiroth.android.library.imagezoom.ImageViewTouch) findViewById(R.id.image);
		   Bitmap bit = null;
		   try {
		        InputStream bitmap=getAssets().open(image);
		        bit=BitmapFactory.decodeStream(bitmap);
		    } catch (IOException e1) {
		        e1.printStackTrace();
		        finish();
		    }	
		   view.setDisplayType(DisplayType.FIT_TO_SCREEN);
		   view.setImageBitmap(bit);
		   rosieHelpMenu = new RosieHelpMenu(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return rosieHelpMenu.createOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return rosieHelpMenu.processMenuItem(item);
	}
	
	
}