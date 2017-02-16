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

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brad on 1/23/17.
 */
public class RosieMapView extends MapView {
    RosieMapView rmv;
    GoogleMap map = null;
    boolean layoutChanged = false;
    boolean mapReady = false;
    List<OnMapReadyCallback> callbacks = new ArrayList<OnMapReadyCallback>();

    public RosieMapView(Context context) {
        super(context);
        rmv = this;
        installCallbacks();
    }

    public RosieMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        rmv = this;
        installCallbacks();
    }

    public RosieMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        rmv = this;
        installCallbacks();
    }

    public RosieMapView(Context context, GoogleMapOptions options) {
        super(context, options);
        rmv = this;
        installCallbacks();
    }

    private void callStoredCallbacks() {
        int called = 0;
        for (OnMapReadyCallback callback : callbacks) {
            callback.onMapReady(map);
            called++;
        }
        callbacks.clear();
    }

    @Override
    public void getMapAsync(OnMapReadyCallback callback) {
        synchronized (rmv) {
            if (mapReady) {
                callback.onMapReady(map);
            } else {
                callbacks.add(callback);
            }
        }
    }

    private void installCallbacks() {
        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top,
                                       int right, int bottom, int oldLeft, int oldTop,
                                       int oldRight, int oldBottom) {
                synchronized(rmv) {
                    layoutChanged = true;
                    if (map != null) {
                        mapReady = true;
                        callStoredCallbacks();
                    }
                }
            }
        });
        super.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                synchronized (rmv) {
                    map = googleMap;
                    if (layoutChanged) {
                        mapReady = true;
                        callStoredCallbacks();
                    }
                }
            }
        });
    }
}