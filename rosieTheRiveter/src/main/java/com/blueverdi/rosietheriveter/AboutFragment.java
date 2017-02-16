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

import java.io.IOException;
import java.io.InputStream;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class AboutFragment extends Fragment implements AnimationListener, OnClickListener{
	private static final String TAG = "AboutFragment";
	private Animation photoFadeIn;
	private View view;
	private int bitmapHeight;
	private int bitmapWidth;
	
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	MyLog.d(TAG, "onCreateView called");
    	setRetainInstance(false);
    	view = inflater.inflate(R.layout.about_fragment, container, false);
    	
    	Button b = (Button)view.findViewById(R.id.acknowledgementButton);
//    	b.setBackgroundResource(R.drawable.button_back);
    	b.setOnClickListener(this);
		b = (Button)view.findViewById(R.id.privacyButton);
//    	b.setBackgroundResource(R.drawable.button_back);
		b.setOnClickListener(this);
		View v = (View) view.findViewById(R.id.emailAddress);;
       	v.setOnClickListener(this);
        ImageView image = (ImageView)view.findViewById(R.id.aboutImageView);
        Utils.forcePhonePortrait(getActivity());
        InputStream is = null;
        try {
          is = this.getResources().getAssets().open("site_images/richkaiser.jpg");
        } catch (IOException e) {
        	MyLog.d(TAG,"could not open background photo");
        	return view;
        }
        
        Bitmap bit = BitmapFactory.decodeStream(is);
        bitmapHeight = bit.getHeight();
        bitmapWidth = bit.getWidth();
        image.setImageBitmap(bit);
        
        photoFadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fadein);
        photoFadeIn.setAnimationListener(this);       
        image.startAnimation(photoFadeIn);
  
    	return view;
    }
    
    
    @Override
    public void onAnimationEnd(Animation animation) {
        // Take any action after completing the animation
        // check for fade in animation
        if (animation == photoFadeIn) {
        	Handler handlerTimer = new Handler();
        	handlerTimer.postDelayed(new Runnable() {
        		public void run() {
        	        ImageView image = (ImageView)view.findViewById(R.id.aboutImageView);
        	        double bitmapRatio  = ((double)bitmapWidth)/((double)bitmapHeight);
        	        double imageViewRatio  = ((double)image.getWidth())/image.getHeight();
        	        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(0,0);
        	        if(bitmapRatio > imageViewRatio)
        	        {
        	          params.leftMargin = 0;
        	          double drawHeight = (imageViewRatio/bitmapRatio) * image.getHeight();
        	          params.topMargin = (int)(image.getHeight() - drawHeight)/2;
        	          params.width = image.getWidth();
        	          params.height = (int) drawHeight;
        	        }
        	        else
        	        {
        	          params.topMargin = 0;
        	          double drawWidth = (bitmapRatio/imageViewRatio) * image.getWidth();
        	          params.leftMargin = (int)(image.getWidth() - drawWidth)/2;
        	          params.height = image.getHeight();
        	          params.width = (int) drawWidth;       	          
        	        }  
        	        Animation animationFadeIn;
        	        try{
        	        	animationFadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fadein);
        	        }
        	        catch (Exception e) {
        	        	MyLog.d(TAG, "getActivity failed!");
        	        	return;
        	        }
                    LinearLayout ll = (LinearLayout) view.findViewById(R.id.aboutTextBlock); 
                    ll.setLayoutParams(params);
                    ll.startAnimation(animationFadeIn);
                    ll.setVisibility(View.VISIBLE);
        		}
        	}, 750);
            
           
        }
 
    }
 
    @Override
    public void onAnimationRepeat(Animation animation) {
        // Animation is repeating
    }
 
    @Override
    public void onAnimationStart(Animation animation) {
        // Animation started
    }
    
    @Override 
    public void onDetach() {
    	super.onDetach();
    	MyLog.d(TAG, "onDetach");
    }
    
    @Override
    public void onStart() {
    	super.onStart();
        Utils.forcePhonePortrait(getActivity());
    }
 
    @Override
    public void onPause() {
    	super.onPause();
    	Utils.stopForcePortrait(getActivity());
    }
    @Override 
    public void onStop() {
    	super.onStop();
    	
    }
    
    @Override
    public void finalize() {
    	MyLog.d(TAG, "finalize");
    }
    
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.acknowledgementButton:
		        Intent i = new Intent(getActivity(), AcknowlegementActivity.class);
		        startActivity(i);   	   	
		        getActivity().overridePendingTransition(R.anim.zoom_in, 0);
		        break;
	        
			case R.id.emailAddress:
				Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND); 
				emailIntent.setType("plain/text");
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"bradley.justice@blueverdi.com"});
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
				startActivity(Intent.createChooser(emailIntent, getString(R.string.email_me2)));	
				break;

			case R.id.privacyButton:
				i = new Intent(getActivity(), PrivacyPolicyActivity.class);
				startActivity(i);
				getActivity().overridePendingTransition(R.anim.zoom_in, 0);
				break;
		}
    }
	


}
