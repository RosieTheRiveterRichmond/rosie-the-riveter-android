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

import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

	public class GalleryFragment extends Fragment implements OnClickListener{
		private static final String TAG = "GalleryFragment";
		public static final String showHintPref = "photoHintShown";
		private RelativeLayout galleryLayout;
		private PageCurlView pcv;
		private static int initialPage = 0;
	    private boolean showHint;
		private AlertDialog hintDialog = null;
		private SharedPreferences mPrefs;  
		private GalleryFragment thisFragment;

		public static final GalleryFragment newInstance(int page) {
		    	initialPage = page;	
		    	return new GalleryFragment();
		}
		
	    public RelativeLayout onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    	int pageIndex = 0;
	    	setRetainInstance(false);
	    	thisFragment = this;
	    	galleryLayout = (RelativeLayout)inflater.inflate(R.layout.gallery_fragment, container, false);
	    	LinearLayout myLayout = (LinearLayout)galleryLayout.findViewById(R.id.gallery);
	    	if (savedInstanceState != null) {
	    		pageIndex = savedInstanceState.getInt("pageIndex");
	    	}
	    	pcv = new PageCurlView(getActivity(),"photos","audio", initialPage);
//	    	pcv = new PageCurlView(getActivity(),"photos","audio", pageIndex);
	    	pcv.setLayoutParams(new LinearLayout.LayoutParams(
	    	                                     LinearLayout.LayoutParams.MATCH_PARENT,
	    	                                     LinearLayout.LayoutParams.MATCH_PARENT));
	    	myLayout.addView(pcv);
//	    	Toast.makeText(getActivity(), getActivity().getString(R.string.swipe_screen), Toast.LENGTH_SHORT).show();	
	    	hintScreen();
	    	return galleryLayout;
	    }

	    @Override 
	    public void onPause() {
	    	super.onPause();
	    	pcv.silence();
	    }
	    
	    @Override
	    public void onAttach(Activity activity) {
	        super.onAttach(activity);
	    }	    
	    
	    Bitmap getImage(String filename) {
	    	Bitmap bitmap;
            AssetManager assetManager = getActivity().getAssets();

            try {
	            InputStream istr = assetManager.open(filename);
	            bitmap = BitmapFactory.decodeStream(istr);
            }
            catch (Exception e)
            {
            	MyLog.d(TAG, "error accessing file " + filename);
            	return null;
            }
        	return bitmap;
	    }
	    
	    private class PhotoSpace extends ImageView{
	    	PhotoSpace(Context c, int size) {
	    		super(c);
	    		this.setBackgroundColor(Color.BLACK);
	    		this.setPadding(5, 5,5 , 5);
	    		this.getLayoutParams().height = size;
	    		this.getLayoutParams().width = size;
	    	}	    	
	    }	 

	    @Override
	    public void onDestroyView() {
	    	super.onDestroyView();
	    	MyLog.d(TAG, "onDestroyView");
	    	freeMemory();
    		try {
    			pcv = null;
    		}
    		catch (Exception e) {
    			
    		}
	    }
	    
	    public int currentPage() {
	    	return pcv.currentPage();
	    }
	    
	    public void freeMemory() {
	    	MyLog.d(TAG, "freeMemory");
    		if (pcv != null) {
		    	try {
		    		pcv.recycleBitmaps();
		    	}
		    	catch (Exception e) {
		    		MyLog.d(TAG, "exception from recycleBitmaps");
		    	}
    		}
	    }

	    @Override
	    public void onDestroy() {
	    	super.onDestroy();
			galleryLayout = null;
			pcv = null;

	    }
	    
	    public void onClick(View v) {
    		SharedPreferences.Editor editor = mPrefs.edit();
    		editor.putBoolean(showHintPref, showHint);
    		editor.commit(); // Very important to save the preference 
    		hintDialog.dismiss();  
    		hintDialog = null;	    		
	    	
	    }


		private void hintScreen() {			
			mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			showHint = mPrefs.getBoolean(showHintPref, true);
			if (showHint) {
				LayoutInflater li = LayoutInflater.from(getActivity()) ;
				View view = li.inflate(R.layout.photo_hint_screen,null);
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
				b.setOnClickListener(thisFragment);
				builder.setTitle(null);
				builder.setView(view);
				hintDialog = builder.show();
		    }	
			
		}	    
}
