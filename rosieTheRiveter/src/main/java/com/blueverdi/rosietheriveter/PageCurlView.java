/*
@author Moritz 'Moss' Wundke (b.thax.dcg@gmail.com)
https://github.com/moritz-wundke/android-page-curl
Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions
   and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice, this list of
   conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
3. Neither the name of the copyright holder nor the names of its contributors may be used
   to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.

IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
OF SUCH DAMAGE.

This is a modified version of the original software.
*/

package com.blueverdi.rosietheriveter;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FilenameUtils;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.blueverdi.rosietheriveter.AlbumPageFactory.AlbumPage;
/**
*
* @author Moritz 'Moss' Wundke (b.thax.dcg@gmail.com)
*
*/
public class PageCurlView extends View {
	/** Our Log tag */
	
	private enum Page {
		Previous, Current, Next;		
	}
	
	private AlbumPage page[] = new AlbumPage[3];
	
	private final static String TAG = "PageCurlView";
	private static final float DEAD_ZONE = 10;
	// Debug text paint stuff
	private Paint mTextPaint;
	private TextPaint mTextPaintShadow;
	/** Px / Draw call */
	private int mCurlSpeed;
	/** Fixed update time used to create a smooth curl animation */
	private int mUpdateRate;
	/** The initial offset for x and y axis movements */
	private int mInitialEdgeOffset;
	/** The mode we will use */
	private int mCurlMode;
	/** Simple curl mode. Curl target will move only in one axis. */
	public static final int CURLMODE_SIMPLE = 0;
	/** Dynamic curl mode. Curl target will move on both X and Y axis. */
	public static final int CURLMODE_DYNAMIC = 1;
	/** Enable/Disable debug mode */
	private boolean bEnableDebugMode = false;
	/** Handler used to auto flip time based */
	private FlipAnimationHandler mAnimationHandler;
	/** Maximum radius a page can be flipped, by default it's the width of the view */
	private float mFlipRadius;
	/** Point used to move */
	private Vector2D mMovement;
	/** Page curl edge */
	private Paint mCurlEdgePaint;
	/** Our points used to define the current clipping paths in our draw call */
	private Vector2D mA, mB, mC, mD, mE, mF, mOrigin;
	/** If false no draw call has been done */
	private boolean bViewDrawn;
	/** Defines the flip direction that is currently considered */
	private boolean bFlipRight;
	/** If TRUE we are currently auto-flipping */
	private boolean bFlipping;
	/** Used to control touch input blocking */
	private boolean bBlockTouchInput = false;
	/** Enable input after the next draw event */
	private boolean bEnableInputAfterDraw = false;
	/** LAGACY The current foreground */
	private Bitmap mForeground = null;
	private Bitmap mCenteredForeground = null;
	/** LAGACY The current background */
	private Bitmap mBackground = null;
	private Bitmap mCenteredBackground = null;
	private int pageIndex = 0;
	AlbumPageFactory albumPageFactory;
	private Context context;
	private MediaPlayer mediaPlayer;
	private Canvas tempCanvas = new Canvas();
	Paint paint = new Paint();
	private AtomicBoolean paging = new AtomicBoolean(false);
	
	/**
	* Inner class used to represent a 2D point.
	*/
	private class Vector2D
	{
		public float x,y;
		public Vector2D(float x, float y)
		{
			this.x = x;
			this.y = y;
		}
		@Override
		public String toString() {
			return "("+this.x+","+this.y+")";
		}

		public boolean equals(Object o) {
			if (o instanceof Vector2D) {
				Vector2D p = (Vector2D) o;
				return p.x == x && p.y == y;
			}
			return false;
		}
		public Vector2D sum(Vector2D b) {
			return new Vector2D(x+b.x,y+b.y);
		}
		public Vector2D sub(Vector2D b) {
			return new Vector2D(x-b.x,y-b.y);
		}	
		public float distanceSquared(Vector2D other) {
			float dx = other.x - x;
			float dy = other.y - y;
			return (dx * dx) + (dy * dy);
		}
		public float distance(Vector2D other) {
			return (float) Math.sqrt(distanceSquared(other));
		}
		public float dotProduct(Vector2D other) {
			return other.x * x + other.y * y;
		}
		public Vector2D normalize() {
			float magnitude = (float) Math.sqrt(dotProduct(this));
			return new Vector2D(x / magnitude, y / magnitude);
		}
			public Vector2D mult(float scalar) {
			return new Vector2D(x*scalar,y*scalar);
		}
	}
	/**
	* Inner class used to make a fixed timed animation of the curl effect.
	*/
	class FlipAnimationHandler extends Handler {
			@Override
			public void handleMessage(Message msg) {
			PageCurlView.this.FlipAnimationStep();
		}
		public void sleep(long millis) {
			this.removeMessages(0);
			sendMessageDelayed(obtainMessage(0), millis);
		}
	}
	
	/**
	* Base
	* @param context
	*/
	public PageCurlView(Context context, String imagePath, String audioPath, int pageIndex) {
		super(context);
		this.pageIndex = pageIndex;
		init(context);
		initSources(imagePath, audioPath);
		ResetClipEdge();
	}
	
	/**
	* Construct the object from an XML file. Valid Attributes:
	*
	* @see android.view.View#View(android.content.Context, android.util.AttributeSet)
	*/
	public PageCurlView(Context context, String imagePath, String audioPath,  AttributeSet attrs, int pageIndex) {
		super(context, attrs);
		this.context = context;
		this.pageIndex = pageIndex;
		init(context);
		initSources(imagePath, audioPath);
		ResetClipEdge();
	}

	public void silence() {
		try {
			if (mediaPlayer.isPlaying()) {
	            mediaPlayer.stop();
			}
		}
		catch (Exception e) {
			
		}
		
	}
	
	public int getCurrentPageIndex() {
		return pageIndex;
	}
	
	/**
	* Initialize the view
	*/
	private final void initSources(String imagePath, String audioPath) {
		albumPageFactory = new AlbumPageFactory(context,imagePath,audioPath);
		if (albumPageFactory.totalPages() == 0) {
			MyLog.d(TAG, "there are no images in the album");
		}
		else {
			try {
				page[Page.Current.ordinal()] = albumPageFactory.getAlbumPage(pageIndex);
			}
			catch (Exception e) {
				MyLog.d(TAG, "cannot display first page, this should not happen");
			}
			try {
				page[Page.Next.ordinal()] = albumPageFactory.getAlbumPage(pageIndex + 1);
			}
			catch (Exception e) {
				page[Page.Next.ordinal()] = null;
			}
			if (pageIndex == 0) {
				page[Page.Previous.ordinal()] = null;
			}
			else {
				try {
					page[Page.Previous.ordinal()] = albumPageFactory.getAlbumPage(pageIndex - 1);
				}
				catch (Exception e) {
					page[Page.Previous.ordinal()] = null;
				}
			}
		}
		mediaPlayer = new MediaPlayer();
		setViews(page[Page.Current.ordinal()], page[Page.Current.ordinal()]);
	}
	/**
	* Initialize the view
	*/
	private final void init(Context context) {
//		this.context = context;
	// Foreground text paint
		this.context = context;
		mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTextSize(16);
		mTextPaint.setColor(0xFF000000);
		// The shadow
		mTextPaintShadow = new TextPaint();
		mTextPaintShadow.setAntiAlias(true);
		mTextPaintShadow.setTextSize(16);
		mTextPaintShadow.setColor(0x00000000);
		// Base padding
		setPadding(3, 3, 3, 3);
		// The focus flags are needed
		setFocusable(true);
		setFocusableInTouchMode(true);
		mMovement = new Vector2D(0,0);
		// Create our curl animation handler
		mAnimationHandler = new FlipAnimationHandler();
		// Create our edge paint
		mCurlEdgePaint = new Paint();
		mCurlEdgePaint.setColor(Color.BLACK);
		mCurlEdgePaint.setAntiAlias(true);
		mCurlEdgePaint.setStyle(Paint.Style.FILL);
		mCurlEdgePaint.setShadowLayer(10, -5, 5, 0x99000000);
		// Set the default props, those come from an XML :D
//		mCurlSpeed = 30;
		mCurlSpeed = 165;
//		mUpdateRate = 33;
		mUpdateRate = 4;
		mInitialEdgeOffset = 20;
//		mInitialEdgeOffset = 40;
		mCurlMode = 1;
		// LEGACY PAGE HANDLING!
		// Create pages
		this.setOnTouchListener(new OnSwipeTouchListener(context) {
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
	}
	

	/**
	* Reset points to it's initial clip edge state
	*/
	public void ResetClipEdge()
	{
		// Set our base movement
		mMovement.x = mInitialEdgeOffset;
		mMovement.y = mInitialEdgeOffset;	
		// Now set the points
		// TODO: OK, those points MUST come from our measures and
		// the actual bounds of the view!
		mA = new Vector2D(mInitialEdgeOffset, 0);
		mB = new Vector2D(this.getWidth(), this.getHeight());
		mC = new Vector2D(this.getWidth(), 0);
		mD = new Vector2D(0, 0);
		mE = new Vector2D(0, 0);
		mF = new Vector2D(0, 0);	
		// The movement origin point
		mOrigin = new Vector2D(this.getWidth(), 0);
	}


	/**
	* Set the update rate for the curl animation
	* @param updateRate - Fixed animation update rate in fps
	* @throws IllegalArgumentException if updateRate < 1
	*/
	public void SetUpdateRate(int updateRate)
	{
		if ( updateRate < 1 )
			throw new IllegalArgumentException("updateRate must be greated than 0");
		mUpdateRate = updateRate;
	}
	/**
	* Get the current animation update rate
	* @return int - Fixed animation update rate in fps
	*/
	public int GetUpdateRate()
	{
		return mUpdateRate;
	}
	/**
	* Set the initial pixel offset for the curl edge
	* @param initialEdgeOffset - px offset for curl edge
	* @throws IllegalArgumentException if initialEdgeOffset < 0
	*/
	public void SetInitialEdgeOffset(int initialEdgeOffset)
	{
		if ( initialEdgeOffset < 0 )
			throw new IllegalArgumentException("initialEdgeOffset can not negative");
		mInitialEdgeOffset = initialEdgeOffset;
	}
	/**
	* Get the initial pixel offset for the curl edge
	* @return int - px
	*/
	public int GetInitialEdgeOffset()
	{
		return mInitialEdgeOffset;
	}
	/**
	* Set the curl mode.
	* <p>Can be one of the following values:</p>
	* <table>
	* <colgroup align="left" />
	* <colgroup align="left" />
	* <tr><th>Value</th><th>Description</th></tr>
	* <tr><td><code>{@link #CURLMODE_SIMPLE com.dcg.pagecurl:CURLMODE_SIMPLE}</code></td><td>Curl target will move only in one axis.</td></tr>
	* <tr><td><code>{@link #CURLMODE_DYNAMIC com.dcg.pagecurl:CURLMODE_DYNAMIC}</code></td><td>Curl target will move on both X and Y axis.</td></tr>
	* </table>
	* @see #CURLMODE_SIMPLE
	* @see #CURLMODE_DYNAMIC
	* @param curlMode
	* @throws IllegalArgumentException if curlMode is invalid
	*/
	public void SetCurlMode(int curlMode)
	{
		if ( curlMode != CURLMODE_SIMPLE && curlMode != CURLMODE_DYNAMIC )
			throw new IllegalArgumentException("Invalid curlMode");
		mCurlMode = curlMode;
	}
	/**
	* Return an integer that represents the current curl mode.
	* <p>Can be one of the following values:</p>
	* <table>
	* <colgroup align="left" />
	* <colgroup align="left" />
	* <tr><th>Value</th><th>Description</th></tr>
	* <tr><td><code>{@link #CURLMODE_SIMPLE com.dcg.pagecurl:CURLMODE_SIMPLE}</code></td><td>Curl target will move only in one axis.</td></tr>
	* <tr><td><code>{@link #CURLMODE_DYNAMIC com.dcg.pagecurl:CURLMODE_DYNAMIC}</code></td><td>Curl target will move on both X and Y axis.</td></tr>
	* </table>
	* @see #CURLMODE_SIMPLE
	* @see #CURLMODE_DYNAMIC
	* @return int - current curl mode
	*/
	public int GetCurlMode()
	{
		return mCurlMode;
	}
	/**
	* Enable debug mode. This will draw a lot of data in the view so you can track what is happening
	* @param bFlag - boolean flag
	*/
	public void SetEnableDebugMode(boolean bFlag)
	{
		bEnableDebugMode = bFlag;
	}
	/**
	* Check if we are currently in debug mode.
	* @return boolean - If TRUE debug mode is on, FALSE otherwise.
	*/
	public boolean IsDebugModeEnabled()
	{
		return bEnableDebugMode;
	}
	/**
	* @see android.view.View#measure(int, int)
	*/
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int finalWidth, finalHeight;
		finalWidth = measureWidth(widthMeasureSpec);
		finalHeight = measureHeight(heightMeasureSpec);
		setMeasuredDimension(finalWidth, finalHeight);
	}
	/**
	* Determines the width of this view
	* @param measureSpec A measureSpec packed into an int
	* @return The width of the view, honoring constraints from measureSpec
	*/
	private int measureWidth(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		if (specMode == MeasureSpec.EXACTLY) {
			// We were told how big to be
			result = specSize;
		} else {
			// Measure the text
			result = specSize;
		}
		return result;
	}
	/**
	* Determines the height of this view
	* @param measureSpec A measureSpec packed into an int
	* @return The height of the view, honoring constraints from measureSpec
	*/
	private int measureHeight(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		if (specMode == MeasureSpec.EXACTLY) {
			// We were told how big to be
			result = specSize;
		} else {
			// Measure the text (beware: ascent is a negative number)
			result = specSize;
		}
		return result;
	}
	/**
	* Render the text
	*
	* @see android.view.View#onDraw(android.graphics.Canvas)
	*/
	
	/**
	* Make sure we never move too much, and make sure that if we
	* move too much to add aqyp
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     * displacement so that the movement will
	* be still in our radius.
	* @param radius - radius form the flip origin
	* @param bMaintainMoveDir - Cap movement but do not change the
	* current movement direction
	* @return Corrected point
	*/
	private Vector2D CapMovement(Vector2D point, boolean bMaintainMoveDir)
	{
		// Make sure we never ever move too much
		if (point.distance(mOrigin) > mFlipRadius)
		{
			if ( bMaintainMoveDir )
			{
			// Maintain the direction
				point = mOrigin.sum(point.sub(mOrigin).normalize().mult(mFlipRadius));
			}
			else
			{
				// Change direction
				if ( point.x > (mOrigin.x+mFlipRadius))
					point.x = (mOrigin.x+mFlipRadius);
				else if ( point.x < (mOrigin.x-mFlipRadius) )
					point.x = (mOrigin.x-mFlipRadius);
				point.y = (float) (Math.sin(Math.acos(Math.abs(point.x-mOrigin.x)/mFlipRadius))*mFlipRadius);
			}
		}
		return point;
	}
	/**
	* Execute a step of the flip animation
	*/
	public void FlipAnimationStep() {
		if ( !bFlipping )
			return;
		int width = getWidth();
		// No input when flipping
		bBlockTouchInput = true;
		// Handle speed 
		float curlSpeed = mCurlSpeed;
		if ( !bFlipRight )
			curlSpeed *= -1;
		// Move us
		mMovement.x += curlSpeed;
		mMovement = CapMovement(mMovement, false);
		// Create values
		DoPageCurl();
		// Check for endings :D
		if (mA.x < 1 || mA.x > width - 1) {
			bFlipping = false;
			if (bFlipRight) {
				this.mForeground = this.mBackground;
				this.mCenteredForeground = this.mCenteredBackground;
			}
			ResetClipEdge();
			// Create values
			DoPageCurl();
			// Enable touch input after the next draw event
				bEnableInputAfterDraw = true;
		}
		else
		{
				mAnimationHandler.sleep(mUpdateRate);
		}
		// Force a new draw call
		this.invalidate();
	}
	
	/**
	* Do the page curl depending on the methods we are using
	*/
	private void DoPageCurl()
	{
		if (!paging.getAndSet(true)) {
			if(bFlipping){
					doSimpleCurl();
			}
			else {
					doSimpleCurl();
			}
			paging.set(false);
		}
	}
	
	/**
	* Do a simple page curl effect
	*/
	private void doSimpleCurl() {
		int width = getWidth();
		int height = getHeight();
		// Calculate point A
		mA.x = width - mMovement.x;
		mA.y = height;
		// Calculate point D
		mD.x = 0;
		mD.y = 0;
		if (mA.x > width / 2) {
			mD.x = width;
			mD.y = height - (width - mA.x) * height / mA.x;
		} else {
			mD.x = 2 * mA.x;
			mD.y = 0;
		}
		// Now calculate E and F taking into account that the line
		// AD is perpendicular to FB and EC. B and C are fixed points.
		double angle = Math.atan((height - mD.y) / (mD.x + mMovement.x - width));
		double _cos = Math.cos(2 * angle);
		double _sin = Math.sin(2 * angle);
		// And get F
		mF.x = (float) (width - mMovement.x + _cos * mMovement.x);
		mF.y = (float) (height - _sin * mMovement.x);
		// If the x position of A is above half of the page we are still not
		// folding the upper-right edge and so E and D are equal.
		if (mA.x > width / 2) {
			mE.x = mD.x;
			mE.y = mD.y;
		}
		else
		{
			// So get E
			mE.x = (float) (mD.x + _cos * (width - mD.x));
			mE.y = (float) -(_sin * (width - mD.x));
		}
	}
	

	/**
	* Swap to next view
	*/
	private boolean nextView() {
		if (page[Page.Next.ordinal()] == null) {
			Toast.makeText(context, context.getText(R.string.no_more_photos), Toast.LENGTH_SHORT).show();
			 return false;			
		}
		pageIndex++;
		if (page[Page.Previous.ordinal()] != null) {
			try {
				page[Page.Previous.ordinal()].bitmap.recycle();
			}
			catch (Exception e) {
				
			}
		}
		page[Page.Previous.ordinal()] = page[Page.Current.ordinal()];		
		page[Page.Current.ordinal()] = page[Page.Next.ordinal()];
		try {
			page[Page.Next.ordinal()] = albumPageFactory.getAlbumPage(pageIndex+1);
		}
		catch (Exception e) {
			page[Page.Next.ordinal()] = null;			
		}
//		setViews(page[Page.Current.ordinal()], page[Page.Previous.ordinal()]);
		setViews(page[Page.Previous.ordinal()], page[Page.Current.ordinal()]);
		playAudio(page[Page.Current.ordinal()]);
		return true;
	}
	/**
	* Swap to previous view
	*/
	private boolean previousView() { 
		if (page[Page.Previous.ordinal()] == null) {
			Toast.makeText(context, context.getText(R.string.no_previous_photos), Toast.LENGTH_SHORT).show();
			 return false;			
		}
		pageIndex--;
		if (page[Page.Next.ordinal()] != null) {
			try {
				page[Page.Next.ordinal()].bitmap.recycle();
			}
			catch (Exception e) {
				
			}
		}
		page[Page.Next.ordinal()] = page[Page.Current.ordinal()];		
		page[Page.Current.ordinal()] = page[Page.Previous.ordinal()];
		try {
			page[Page.Previous.ordinal()] = albumPageFactory.getAlbumPage(pageIndex-1);
		}
		catch (Exception e) {
			page[Page.Previous.ordinal()] = null;			
		}
		setViews(page[Page.Current.ordinal()], page[Page.Next.ordinal()]);
		playAudio(page[Page.Current.ordinal()]);
		return true;
	}
	/**
	* Set current fore and background
	* @param foreground - Foreground view index
	* @param background - Background view index
	*/
	private void setViews(AlbumPage foreground, AlbumPage background) {
		mForeground = foreground.bitmap;
		mBackground = background.bitmap; 
	}
	//---------------------------------------------------------------
	// Drawing methods
	//---------------------------------------------------------------
	@Override
	protected void onDraw(Canvas canvas) {
		// Translate the whole canvas
		//canvas.translate(mCurrentLeft, mCurrentTop);
		// We need to initialize all size data when we first draw the view
		if ( !bViewDrawn ) {
			bViewDrawn = true;
			onFirstDrawEvent(canvas);
		}
		canvas.drawColor(Color.BLACK);
		// Curl pages
		//DoPageCurl();
		// TODO: This just scales the views to the current
		// width and height. We should add some logic for:
		// 1) Maintain aspect ratio
		// 2) Uniform scale
		// 3) ...
		Rect rect = new Rect();
		rect.left = 0;
		rect.top = 0;
		rect.bottom = getHeight();
		rect.right = getWidth();
//		Paint paint = new Paint();
		Rect scaledRect;
		try {
			scaledRect = getScaledRect(getHeight(), getWidth(),mForeground.getHeight(),mForeground.getWidth());
		}
		catch (Exception e) {
			// tearing down view
			return;
		}
		//vas = new Canvas();
		try {
			if (mCenteredForeground != null) {
				mCenteredForeground.recycle();
			}
		}
		catch (Exception e) {
			
		}
		try {
			mCenteredForeground = Bitmap.createBitmap(getWidth(), getHeight(), mForeground.getConfig());
		}
		catch (Exception e) {
			// tearing down support
			return;
		}
		tempCanvas.setBitmap(mCenteredForeground);
		tempCanvas.drawColor(Color.BLACK);
		tempCanvas.drawBitmap(mForeground, null, scaledRect, paint);
		
		// First Page render
//		Paint paint = new Paint();
		// Draw our elements
		drawForeground(canvas, rect, paint);
		
//		canvas.drawColor(Color.WHITE);
		try {
			scaledRect = getScaledRect(getHeight(), getWidth(),mBackground.getHeight(),mBackground.getWidth());
		}
		catch (Exception e) {
			// tearing down support
			return;
		}
		try {
			if (mCenteredBackground != null) {
				mCenteredBackground.recycle();
			}
		}
		catch (Exception e) {
			
		}
		try {
			mCenteredBackground = Bitmap.createBitmap(getWidth(), getHeight(), mBackground.getConfig());
			tempCanvas.setBitmap(mCenteredBackground);
			tempCanvas.drawColor(Color.BLACK);
			tempCanvas.drawBitmap(mBackground, null, scaledRect, paint);
			drawBackground(canvas, rect, paint);
			drawCurlEdge(canvas);
		}
		catch (Exception e) {
			// tearing down support
			return;
		}
		// Draw any debug info once we are done
		if ( bEnableDebugMode )
		drawDebug(canvas);
		// Check if we can re-enable input
		if ( bEnableInputAfterDraw )
		{
		bBlockTouchInput = false;
		bEnableInputAfterDraw = false;
	}
	// Restore canvas
	//canvas.restore();
	}
	
	Rect getScaledRect(int viewHeight, int viewWidth, int imageHeight, int imageWidth) {
		Rect ret = null;
		float imageTrans = (float)viewHeight/(float)imageHeight;
		if ((((float)imageWidth) * imageTrans) <= ((float) viewWidth)) {
			int rectWidth = (int)(imageWidth * imageTrans);
			ret = new Rect();
			ret.top = 0;
			ret.bottom = viewHeight;
			ret.left = (viewWidth - rectWidth)/2;
			ret.right = ret.left + rectWidth;
		}
		else {
			imageTrans = (float)viewWidth/(float)imageWidth;
			int rectHeight = (int)(imageHeight * imageTrans);
			ret = new Rect();
			ret.left = 0;
			ret.right = viewWidth;
			ret.top = (viewHeight - rectHeight)/2;
			ret.bottom = ret.top + rectHeight;
	
		}
		return ret;
	}
	/**
	* Called on the first draw event of the view
	* @param canvas
	*/
	protected void onFirstDrawEvent(Canvas canvas) {
		mFlipRadius = getWidth();
		ResetClipEdge();
		DoPageCurl();
		playAudio(page[Page.Current.ordinal()]);
	}
	/**
	* Draw the foreground
	* @param canvas
	* @param rect
	* @param paint
	*/
	private void drawForeground( Canvas canvas, Rect rect, Paint paint ) {
		canvas.drawBitmap(mCenteredForeground, null, rect, paint);
		// Draw the page number (first page is 1 in real life :D
		// there is no page number 0 hehe)
		drawPageNum(canvas, pageIndex+1);
	}
	/**
	* Create a Path used as a mask to draw the background page
	* @return
	*/
	private Path createBackgroundPath() {
		Path path = new Path();
		path.moveTo(mA.x, mA.y);
		path.lineTo(mB.x, mB.y);
		path.lineTo(mC.x, mC.y);
		path.lineTo(mD.x, mD.y);
		path.lineTo(mA.x, mA.y);
		return path;
	}
	/**
	* Draw the background image.
	* @param canvas
	* @param rect
	* @param paint
	*/
	private void drawBackground( Canvas canvas, Rect rect, Paint paint ) {
		Path mask = createBackgroundPath();
		// Save current canvas so we do not mess it up
		canvas.save();
		canvas.clipPath(mask);
		canvas.drawBitmap(mCenteredBackground, null, rect, paint);
		// Draw the page number (first page is 1 in real life :D
		// there is no page number 0 hehe)
		drawPageNum(canvas, pageIndex+1);
		canvas.restore();
	}
	/**
	* Creates a path used to draw the curl edge in.
	* @return
	*/
	private Path createCurlEdgePath() {
		Path path = new Path();
		path.moveTo(mA.x, mA.y);
		path.lineTo(mD.x, mD.y);
		path.lineTo(mE.x, mE.y);
		path.lineTo(mF.x, mF.y);
		path.lineTo(mA.x, mA.y);
		return path;
	}
	/**
	* Draw the curl page edge
	* @param canvas
	*/
	private void drawCurlEdge( Canvas canvas )
	{
		Path path = createCurlEdgePath();
		canvas.drawPath(path, mCurlEdgePaint);
	}
	/**
	* Draw page num (let this be a bit more custom)
	* @param canvas
	* @param pageNum
	*/
	private void drawPageNum(Canvas canvas, int pageNum)
	{
		mTextPaint.setColor(Color.WHITE);
		String pageNumText = "- "+pageNum+" -";
		drawCentered(canvas, pageNumText,canvas.getHeight()-mTextPaint.getTextSize()-5,mTextPaint,mTextPaintShadow);
//		drawRight(canvas, context.getString(R.string.next),25,mTextPaint,mTextPaintShadow);
//		drawLeft(canvas, context.getString(R.string.prev),25,mTextPaint,mTextPaintShadow);
		
	}
	//---------------------------------------------------------------
	// Debug draw methods
	//---------------------------------------------------------------
	/**
	* Draw a text with a nice shadow
	*/
	public static void drawTextShadowed(Canvas canvas, String text, float x, float y, Paint textPain, Paint shadowPaint) {
		canvas.drawText(text, x-1, y, shadowPaint);
		canvas.drawText(text, x, y+1, shadowPaint);
		canvas.drawText(text, x+1, y, shadowPaint);
		canvas.drawText(text, x, y-1, shadowPaint);
		canvas.drawText(text, x, y, textPain);
	}
	/**
	* Draw a text with a nice shadow centered in the X axis
	* @param canvas
	* @param text
	* @param y
	* @param textPain
	* @param shadowPaint
	*/
	public static void drawCentered(Canvas canvas, String text, float y, Paint textPain, Paint shadowPaint)
	{
		float posx = (canvas.getWidth() - textPain.measureText(text))/2;
		drawTextShadowed(canvas, text, posx, y, textPain, shadowPaint);
	}
	public static void drawLeft(Canvas canvas, String text, float y, Paint textPain, Paint shadowPaint)
	{
		float posx = 5;
		drawTextShadowed(canvas, text, posx, y, textPain, shadowPaint);
	}
	public static void drawRight(Canvas canvas, String text, float y, Paint textPain, Paint shadowPaint)
	{
		float posx = (canvas.getWidth() - (textPain.measureText(text) + 5));
		drawTextShadowed(canvas, text, posx, y, textPain, shadowPaint);
	}
	/**
	* Draw debug info
	* @param canvas
	*/
	private void drawDebug(Canvas canvas)
	{
		float posX = 10;
		float posY = 20;
		Paint paint = new Paint();
		paint.setStrokeWidth(5);
		paint.setStyle(Style.STROKE);
		paint.setColor(Color.BLACK);	
		canvas.drawCircle(mOrigin.x, mOrigin.y, getWidth(), paint);
		paint.setStrokeWidth(3);
		paint.setColor(Color.RED);	
		canvas.drawCircle(mOrigin.x, mOrigin.y, getWidth(), paint);
		paint.setStrokeWidth(5);
		paint.setColor(Color.BLACK);
		canvas.drawLine(mOrigin.x, mOrigin.y, mMovement.x, mMovement.y, paint);
		paint.setStrokeWidth(3);
		paint.setColor(Color.RED);
		canvas.drawLine(mOrigin.x, mOrigin.y, mMovement.x, mMovement.y, paint);
		posY = debugDrawPoint(canvas,"A",mA,Color.RED,posX,posY);
		posY = debugDrawPoint(canvas,"B",mB,Color.GREEN,posX,posY);
		posY = debugDrawPoint(canvas,"C",mC,Color.BLUE,posX,posY);
		posY = debugDrawPoint(canvas,"D",mD,Color.CYAN,posX,posY);
		posY = debugDrawPoint(canvas,"E",mE,Color.YELLOW,posX,posY);
		posY = debugDrawPoint(canvas,"F",mF,Color.LTGRAY,posX,posY);
		posY = debugDrawPoint(canvas,"Mov",mMovement,Color.DKGRAY,posX,posY);
		posY = debugDrawPoint(canvas,"Origin",mOrigin,Color.MAGENTA,posX,posY);
		// Draw some curl stuff (Just some test)
		/*
		canvas.save();
		Vector2D center = new Vector2D(getWidth()/2,getHeight()/2);
		//canvas.rotate(315,center.x,center.y);
		// Test each lines
		//float radius = mA.distance(mD)/2.f;
		//float radius = mA.distance(mE)/2.f;
		float radius = mA.distance(mF)/2.f;
		//float radius = 10;
		float reduction = 4.f;
		RectF oval = new RectF();
		oval.top = center.y-radius/reduction;
		oval.bottom = center.y+radius/reduction;
		oval.left = center.x-radius;
		oval.right = center.x+radius;
		canvas.drawArc(oval, 0, 360, false, paint);
		canvas.restore();
		/**/
	}
	private float debugDrawPoint(Canvas canvas, String name, Vector2D point, int color, float posX, float posY) {	
		return debugDrawPoint(canvas,name+" "+point.toString(),point.x, point.y, color, posX, posY);
	}
	private float debugDrawPoint(Canvas canvas, String name, float X, float Y, int color, float posX, float posY) {
		mTextPaint.setColor(color);
		drawTextShadowed(canvas,name,posX , posY, mTextPaint,mTextPaintShadow);
		Paint paint = new Paint();
		paint.setStrokeWidth(5);
		paint.setColor(color);	
		canvas.drawPoint(X, Y, paint);
		return posY+15;
	}
	
	public void playAudio(AlbumPage page) {
	    try {
	        if (mediaPlayer.isPlaying()) {
	            mediaPlayer.stop();
	            mediaPlayer.release();
	            mediaPlayer = new MediaPlayer();
	        }
	        if (page.mp3 != null) {
	        	AssetFileDescriptor descriptor = context.getAssets().openFd(page.mp3);
		        mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
		        descriptor.close();
	
		        mediaPlayer.prepare();
		        mediaPlayer.setVolume(1f, 1f);
		        mediaPlayer.setLooping(true);
		        mediaPlayer.start();
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public int currentPage() {
		return pageIndex;
	}
	
	public void recycleBitmaps() {
		mForeground = null;
		mBackground = null;
		mCenteredForeground  = null;
		mCenteredBackground = null;
		tempCanvas = null;
		albumPageFactory = null;
		mCurlEdgePaint = null;
		mTextPaint = null;
		mTextPaintShadow = null;
		
		if (page[Page.Previous.ordinal()] != null) {
			try {
				WeakReference<Bitmap> wrbm = new WeakReference<Bitmap>(page[Page.Previous.ordinal()].bitmap);
				page[Page.Previous.ordinal()].bitmap = null;			
				wrbm.get().recycle();
			}
			catch (Exception e) {
				
			}
		}
		if (page[Page.Current.ordinal()] != null) {
			try {
				WeakReference<Bitmap> wrbm = new WeakReference<Bitmap>(page[Page.Current.ordinal()].bitmap);
				page[Page.Current.ordinal()].bitmap = null;			
				wrbm.get().recycle();
			}
			catch (Exception e) {
				
			}
		}
		if (page[Page.Next.ordinal()] != null) {
			try {
				WeakReference<Bitmap> wrbm = new WeakReference<Bitmap>(page[Page.Next.ordinal()].bitmap);
				page[Page.Next.ordinal()].bitmap = null;			
				wrbm.get().recycle();
			}
			catch (Exception e) {
				
			}
		}
		super.destroyDrawingCache();
	}
	
	
	private void zoomIn() {
		Intent i = new Intent(context,ImageZoomActivity.class);
		i.putExtra(ImageZoomActivity.IMAGE_FQN, page[Page.Current.ordinal()].BitmapName);
		context.startActivity(i);		
	}	
	
	private void lastPhoto() {
		if (previousView()) {
			bFlipRight = false;
			mMovement.x = getWidth(); 
			mMovement.y = mInitialEdgeOffset;
			bFlipping=true;
			FlipAnimationStep();
		}
	}
	
	private void nextPhoto() {
		if (nextView()) {
			bFlipRight = true;
			mMovement.x = mInitialEdgeOffset;
			mMovement.y = mInitialEdgeOffset;
			bFlipping=true;
			FlipAnimationStep();
		}		
	}
}