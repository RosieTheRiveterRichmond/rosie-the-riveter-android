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
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotoViewActivity extends Activity implements OnClickListener{

	private static final String TAG = "PhotoViewActivity";
	private static final String PHOTO_BASE = "site_images";
	private static String INDEX = "index";
	private SiteDetails details = null;
	private TextView caption;
	private ImageView photoView;
	private String[] imageFiles;
	private String[] captions;
	private String imageSource;
	private int index = 0;
    private boolean showHint;
	private AlertDialog hintDialog = null;
	private SharedPreferences mPrefs;  
	private RosieHelpMenu rosieHelpMenu;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle b = getIntent().getExtras();
		try {
			details = b.getParcelable(SiteDetails.PARCEL_NAME);
		}
		catch (Exception e) {
			MyLog.d(TAG, "exception retrieving site info");
			finish();
		}
		if (details == null)  {
			MyLog.d(TAG, "no details");
			finish();
		}
		setContentView(R.layout.activity_photo_view);
		View view = findViewById(R.id.photoView);
		view.setOnTouchListener(new OnSwipeTouchListener(this) {
		    @Override
		    public void onSwipeLeft() {
		        nextPhoto();
		    }
		    @Override
		    public void onSwipeRight() {
		        lastPhoto();
		    }
		    @Override
		    public void onTap() {
		    	zoomIn();
		    }
		});
		view = findViewById(R.id.leftArrow);
		view.setOnClickListener(this);
		view = findViewById(R.id.rightArrow);
		view.setOnClickListener(this);
		TextView tv = (TextView)findViewById(R.id.name);
		tv.setText(details.name);
		caption = (TextView) findViewById(R.id.caption);
		photoView = (ImageView) findViewById(R.id.photoView);
		imageSource = PHOTO_BASE + File.separator + details.photoDirectory;
		try {			
			imageFiles = this.getAssets().list(imageSource);
		}
		catch (Exception e) {
			MyLog.d(TAG, e.toString()); 
			e.printStackTrace();
			imageFiles = new String[0];
		}	
		if (imageFiles.length == 0) {
			MyLog.d(TAG, "no photos to display");
			finish();
		}
		captions = getResources().getStringArray(details.captionArrayID);
		if (savedInstanceState != null) {
			index = savedInstanceState.getInt(INDEX);
		}
		displayImage(index);
		rosieHelpMenu = new RosieHelpMenu(this);
		hintScreen();
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return rosieHelpMenu.createOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return rosieHelpMenu.processMenuItem(item);
	}
	
	@Override
	public void onClick (View v) {
		switch(v.getId()) {
			case R.id.leftArrow:
				lastPhoto();
				break;
			case R.id.rightArrow:
				nextPhoto();
				break;
			case R.id.exitPhotoHintButton:
	    		SharedPreferences.Editor editor = mPrefs.edit();
	    		editor.putBoolean(GalleryFragment.showHintPref, showHint);
	    		editor.commit(); // Very important to save the preference 
	    		hintDialog.dismiss();  
	    		hintDialog = null;	    		
				
		}		
	}
	private void lastPhoto() {
		if (--index < 0) {
			index = imageFiles.length - 1;
		}
//		caption.setText(captions[index]);
		slideRight(photoView,getBitmapFromAssets(imageFiles[index]),caption,captions[index]);
	}
	
	private void nextPhoto() {
		if (++index == imageFiles.length) {
			index = 0;
		}
//		caption.setText(captions[index]);
		slideLeft(photoView,getBitmapFromAssets(imageFiles[index]), caption, captions[index]);		
	}
	
	private void displayImage(int i) {
		caption.setText(captions[index]);
		Bitmap bm = getBitmapFromAssets(imageFiles[index]);
		photoView.setImageBitmap(bm);
	}
	
	@Override
	public void onBackPressed()
	{
	    finish();  
        overridePendingTransition(0,R.anim.zoom_out);
	}

	 private  Bitmap getBitmapFromAssets(String fileName) {
		   String fn = FilenameUtils.concat(imageSource, fileName);
		   Bitmap bit = null;
		   try {
		        InputStream bitmap=getAssets().open(fn);
		        bit=BitmapFactory.decodeStream(bitmap);
		    } catch (IOException e1) {
		        // TODO Auto-generated catch block
		        e1.printStackTrace();
		    }		    
		    return bit;
		}	

	 private void slideLeft(final ImageView v, final Bitmap new_image, final TextView view, final String string) {
		 slide(v, new_image, R.anim.slide_in_left, R.anim.slide_out_left, view, string);
	}

	 private void slideRight(final ImageView v, final Bitmap new_image, final TextView view, final String string) {
		 slide(v, new_image, R.anim.slide_in_right, R.anim.slide_out_right, view, string);
	}

	 private void slide(final ImageView v, final Bitmap new_image, final int in, final int out, final TextView view, final String string) {
		 final Animation anim_out = AnimationUtils.loadAnimation(this, out); 
	     final Animation anim_in  = AnimationUtils.loadAnimation(this, in); 
	     anim_out.setAnimationListener(new AnimationListener()
	     {
	            @Override public void onAnimationStart(Animation animation) {}
	            @Override public void onAnimationRepeat(Animation animation) {}
	            @Override public void onAnimationEnd(Animation animation)
	            {
	                v.setImageBitmap(new_image); 
	                anim_in.setAnimationListener(new AnimationListener() {
	                    @Override public void onAnimationStart(Animation animation) {}
	                    @Override public void onAnimationRepeat(Animation animation) {}
	                    @Override public void onAnimationEnd(Animation animation) {
	                    	view.setText(string);
	                    }
	                });
	                v.startAnimation(anim_in);
	            }
	     });
	     v.startAnimation(anim_out);
	}
	 
	@Override
	public void onSaveInstanceState(Bundle out) {
		super.onSaveInstanceState(out);
		out.putInt(INDEX, index);
	}
	
	void zoomIn() {
		Intent i = new Intent(this,ImageZoomActivity.class);
		String fn = FilenameUtils.concat(imageSource, imageFiles[index]);
		i.putExtra(ImageZoomActivity.IMAGE_FQN, fn);
		startActivity(i);
		
		
	}
	
	private void hintScreen() {			
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		showHint = mPrefs.getBoolean(GalleryFragment.showHintPref, true);
		if (showHint) {
			LayoutInflater li = LayoutInflater.from(this) ;
			View view = li.inflate(R.layout.photo_hint_screen,null);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			CheckBox cb = (CheckBox) view.findViewById(R.id.chkWelcome);
			cb.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            		if (((CheckBox) buttonView).isChecked()) {
            			showHint = false;
            		}
            		else {
            			showHint = true;			
            		}
               	
                }
				
			});
			Button b = (Button) view.findViewById(R.id.exitPhotoHintButton);
			b.setOnClickListener(this);
			builder.setTitle(null);
			builder.setView(view);
			hintDialog = builder.show();
	    }	
		
	}	    
}
