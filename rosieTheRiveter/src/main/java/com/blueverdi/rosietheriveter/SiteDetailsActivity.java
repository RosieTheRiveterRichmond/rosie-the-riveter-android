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

import android.app.Activity;
import android.app.Fragment.SavedState;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;

public class SiteDetailsActivity extends Activity implements OnClickListener {
	
	private static final String TAG = "SiteDetails";
	
	private static final String PLAYING_AUDIO = "playingAudio";

	private SiteDetails details = null;
	private Activity context;
	private boolean audioPlaying = false;
	private Button audioButton;
	private RosieHelpMenu rosieHelpMenu;
	private TextToSpeech textToSpeech;
   
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_site_details);
		context = this;
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
		if (details.website != null) {
			LinearLayout buttons = (LinearLayout) findViewById(R.id.buttons);
//			LayoutInflater inflater = (LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			Button wb = new Button(this);
			wb.setText(getString(R.string.website));
			wb.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT,1));
			wb.setBackgroundColor(Color.WHITE);
			wb.setOnClickListener(new OnClickListener()
			{
			     @Override
			     public void onClick(View v) {
			          if (details.website != null)  {
			        	  if (Utils.isNetworkAvailable(context)) {
				        	  String url = details.website;
				        	  Intent i = new Intent(Intent.ACTION_VIEW);
				        	  i.setData(Uri.parse(url));
				        	  startActivity(i);	
				        	  context.overridePendingTransition(R.anim.zoom_in, 0);
				           }				        	  
				          else {
				            	Toast.makeText(context, getString(R.string.requires_network), Toast.LENGTH_LONG).show();
				          }
			          }
			     }
			});		
			buttons.addView(wb);
		}
	     TextView tv = (TextView) findViewById(R.id.text);
	     tv.setText(getString(details.textID));
	     tv = (TextView) findViewById(R.id.name);
	     tv.setText(details.name);
	     
	     Button pb = (Button) findViewById(R.id.photosButton);
	     pb.setOnClickListener(this);
	     audioButton = (Button) findViewById(R.id.audioButton);
	     audioButton.setOnClickListener(this);


         // If this Activity is being recreated due to a config change (e.g. 
         // screen rotation), check for the saved state.
         SavedState savedState = (SavedState)getLastNonConfigurationInstance();
         if (savedInstanceState != null) {
        	 audioPlaying = savedInstanceState.getBoolean(PLAYING_AUDIO);
        	 if (audioPlaying) {
        		 audioButton.setText(getString(R.string.stop_audio));
        	 }
         }
         rosieHelpMenu = new RosieHelpMenu(this);
	}
	
	
	@Override 
	public final void onClick(View v) {
		switch (v.getId()) {
			case R.id.photosButton:
				Intent i = new Intent(this, PhotoViewActivity.class);
				i.putExtra(SiteDetails.PARCEL_NAME, details);
	             startActivity(i); 
	             context.overridePendingTransition(R.anim.zoom_in, 0);	
	             break;
			case R.id.audioButton:
				if (!audioPlaying) {
					textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
						@Override
						public void onInit(int status) {
							if(status != TextToSpeech.ERROR) {
//								textToSpeech.setLanguage(Locale.US);
								textToSpeech.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
									@Override
									public void onUtteranceCompleted(String utteranceId) {
										audioPlaying = false;
										audioButton.setText(R.string.stop_audio);
									}
								});
								textToSpeech.speak(getString(details.textID),  TextToSpeech.QUEUE_FLUSH, null);
							}
						}
					});
					audioPlaying = true;
					audioButton.setText(getString(R.string.stop_audio));
				}
				else {
					try {
						textToSpeech.stop();
					}
					catch (Exception e) {
						MyLog.d(TAG, "exception stopping text to speech");
					}
					audioPlaying = false;
					audioButton.setText(R.string.audio);
					textToSpeech = null;
				}

		}
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
		try {
			textToSpeech.stop();
		}
		catch (Exception e) {
			MyLog.d(TAG, "exception stopping text to speech");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

    @Override
    public void onSaveInstanceState(Bundle out) {
    	super.onSaveInstanceState(out);
    	out.putBoolean(PLAYING_AUDIO, audioPlaying);
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
