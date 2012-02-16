/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.powerwidget;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
/* import android.net.wimax.WimaxHelper; */
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.widget.GridLayout;
import android.widget.TextView;
import android.view.ViewGroup;

import com.android.systemui.R;

import java.util.ArrayList;
import java.util.HashMap;

public class PowerWidget extends FrameLayout {
    private static final String TAG = "PowerWidget";

    public static final String BUTTON_DELIMITER = "|";

    private static final String BUTTONS_DEFAULT = PowerButton.BUTTON_BRIGHTNESS
			     + BUTTON_DELIMITER + PowerButton.BUTTON_WIFI
			     + BUTTON_DELIMITER + PowerButton.BUTTON_BLUETOOTH
			     + BUTTON_DELIMITER + PowerButton.BUTTON_GPS
			     + BUTTON_DELIMITER + PowerButton.BUTTON_MOBILEDATA
			     + BUTTON_DELIMITER + PowerButton.BUTTON_AIRPLANE
                             + BUTTON_DELIMITER + PowerButton.BUTTON_SYNC
                             + BUTTON_DELIMITER + PowerButton.BUTTON_WIFIAP
                             + BUTTON_DELIMITER + PowerButton.BUTTON_SLEEP
                             + BUTTON_DELIMITER + PowerButton.BUTTON_SCREENTIMEOUT
                             + BUTTON_DELIMITER + PowerButton.BUTTON_AUTOROTATE
                             + BUTTON_DELIMITER + PowerButton.BUTTON_LOCKSCREEN
                             + BUTTON_DELIMITER + PowerButton.BUTTON_ISPTYPE
                             + BUTTON_DELIMITER + PowerButton.BUTTON_NETWORKMODE;

    private static final ViewGroup.LayoutParams WIDGET_LAYOUT_PARAMS = new ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT, // width = match_parent
                                        ViewGroup.LayoutParams.MATCH_PARENT  // height = wrap_content
                                        );

    private static final ViewGroup.LayoutParams BUTTON_LAYOUT_PARAMS = new ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT, // width = wrap_content
                                        ViewGroup.LayoutParams.WRAP_CONTENT // height = match_parent
                                        );

    private static final ViewGroup.LayoutParams SEEKBAR_LAYOUT_PARAMS = new ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT, // width = wrap_content
                                        ViewGroup.LayoutParams.WRAP_CONTENT // height = match_parent
                                        );
	
    private static final int LAYOUT_SCROLL_BUTTON_THRESHOLD = 5;
    private Context mContext;
    private LayoutInflater mInflater;
    private WidgetBroadcastReceiver mBroadcastReceiver = null;
    private WidgetSettingsObserver mObserver = null;

//    private HorizontalScrollView mScrollView;
    private ScrollView mScrollView;

    public PowerWidget(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // get an initial width
        updateButtonLayoutWidth();
        setupWidget();
        updateVisibility();

    }

    public void setupWidget() {
        Log.i(TAG, "Clearing any old widget stuffs");
        // remove all views from the layout
        removeAllViews();

        // unregister our content receiver
        if(mBroadcastReceiver != null) {
            mContext.unregisterReceiver(mBroadcastReceiver);
        }
        // unobserve our content
        if(mObserver != null) {
            mObserver.unobserve();
        }

        // clear the button instances
        PowerButton.unloadAllButtons();

        Log.i(TAG, "Setting up widget");

        String buttons = Settings.System.getString(mContext.getContentResolver(), Settings.System.WIDGET_BUTTONS);
        if(buttons == null) {
            Log.i(TAG, "Default buttons being loaded");
            buttons = BUTTONS_DEFAULT;
            // Add the WiMAX button if it's supported
            /* TODO: Fix after WiMax Fixed
            if (WimaxHelper.isWimaxSupported(mContext)) {
                buttons += BUTTON_DELIMITER + PowerButton.BUTTON_WIMAX;
            }
            */
        }
        Log.i(TAG, "Button list: " + buttons);

        // create a linearlayout to hold our buttons
        GridLayout ll = new GridLayout(mContext);
	ll.setColumnCount(LAYOUT_SCROLL_BUTTON_THRESHOLD);
	ll.setLayoutParams(new LayoutParams(
		LinearLayout.LayoutParams.MATCH_PARENT,
		LinearLayout.LayoutParams.WRAP_CONTENT));


        // create a linearlayout to hold our buttons
        LinearLayout mainLayout = new LinearLayout(mContext);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER_HORIZONTAL);


        int buttonCount = 0;
        boolean containBrightness = false;
        for(String button : buttons.split("\\|")) {
            Log.i(TAG, "Setting up button: " + button);
            // inflate our button, we don't add it to a parent and don't do any layout shit yet
            View buttonView = mInflater.inflate(R.layout.power_widget_button, null, false);
            TextView textView = (TextView)buttonView.findViewById(R.id.power_widget_button_text);

            Resources res = mContext.getResources();
            int iStringId = res.getIdentifier(button,"string",mContext.getPackageName());
            textView.setText(mContext.getString(iStringId));
            

            GridLayout.LayoutParams p = new GridLayout.LayoutParams();
            p.width = 108;
            p.height = 120;    

            if(PowerButton.loadButton(button, buttonView)) {
                // add the button here
                ll.addView(buttonView, p);
                buttonCount++;

            	if(button == PowerButton.BUTTON_BRIGHTNESS) {
		    containBrightness = true;
	    	}

            } else {
                Log.e(TAG, "Error setting up button: " + button);
            }
        }

        // we determine if we're using a horizontal scroll view based on a threshold of button counts
        /*if(buttonCount > LAYOUT_SCROLL_BUTTON_THRESHOLD) {*/
            // we need our horizontal scroll view to wrap the linear layout
            //mScrollView = new HorizontalScrollView(mContext);
            //mScrollView = new ScrollView(mContext);
            // make the fading edge the size of a button (makes it more noticible that we can scroll
            //mScrollView.setFadingEdgeLength(mContext.getResources().getDisplayMetrics().heightPixels / LAYOUT_SCROLL_BUTTON_THRESHOLD);
            //mScrollView.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
            //mScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            // set the padding on the linear layout to the size of our scrollbar, so we don't have them overlap
            //ll.setPadding(ll.getPaddingLeft(), ll.getPaddingTop(), ll.getPaddingRight(), /*mScrollView.getVerticalScrollbarWidth()*/4);
            //mScrollView.addView(ll/*, WIDGET_LAYOUT_PARAMS*/);
            //updateScrollbar();
        mainLayout.addView(ll);

        Log.e(TAG, "loading brightnessAdjView");
        View brightnessAdjView = mInflater.inflate(R.layout.power_widget_button, null, false);
        if(containBrightness && PowerButton.loadButton(PowerButton.BUTTON_MANUAL_BRIGHTNESS, brightnessAdjView)) {
                Log.e(TAG, "loading brightnessAdjView success,add it");
		mainLayout.addView(brightnessAdjView,SEEKBAR_LAYOUT_PARAMS);
	}

        addView(mainLayout, WIDGET_LAYOUT_PARAMS);
            
        /*} else {
            // not needed, just add the linear layout
            addView(ll, WIDGET_LAYOUT_PARAMS);
        }//*/

        // set up a broadcast receiver for our intents, based off of what our power buttons have been loaded
        setupBroadcastReceiver();
        IntentFilter filter = PowerButton.getAllBroadcastIntentFilters();
        // we add this so we can update views and such if the settings for our widget change
        filter.addAction(Settings.SETTINGS_CHANGED);
        // we need to detect orientation changes and update the static button width value appropriately
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        // register the receiver
        mContext.registerReceiver(mBroadcastReceiver, filter);
        // register our observer
        if(mObserver != null) {
            mObserver.observe();
        }
    }

    public void updateWidget() {
        PowerButton.updateAllButtons();
    }

    public void setupSettingsObserver(Handler handler) {
        if(mObserver == null) {
            mObserver = new WidgetSettingsObserver(handler);
        }
    }

    public void setGlobalButtonOnClickListener(View.OnClickListener listener) {
        PowerButton.setGlobalOnClickListener(listener);
    }

    public void setGlobalButtonOnLongClickListener(View.OnLongClickListener listener) {
        PowerButton.setGlobalOnLongClickListener(listener);
    }

    private void setupBroadcastReceiver() {
        if(mBroadcastReceiver == null) {
            mBroadcastReceiver = new WidgetBroadcastReceiver();
        }
    }

    private void updateButtonLayoutWidth() {
        // use our context to set a valid button width
        BUTTON_LAYOUT_PARAMS.width = mContext.getResources().getDisplayMetrics().widthPixels / LAYOUT_SCROLL_BUTTON_THRESHOLD;
    }

    private void updateVisibility() {
        // now check if we need to display the widget still
        boolean displayPowerWidget = Settings.System.getInt(mContext.getContentResolver(),
                   Settings.System.EXPANDED_VIEW_WIDGET, 1) == 1;
        if(!displayPowerWidget) {
            setVisibility(View.GONE);
        } else {
            setVisibility(View.VISIBLE);
        }
    }

    private void updateScrollbar() {
        if (mScrollView == null) return;
        boolean hideScrollBar = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.EXPANDED_HIDE_SCROLLBAR, 0) == 1;
        mScrollView.setHorizontalScrollBarEnabled(!hideScrollBar);
    }

    // our own broadcast receiver :D
    private class WidgetBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
                updateButtonLayoutWidth();
                setupWidget();
            } else {
                // handle the intent through our power buttons
                PowerButton.handleOnReceive(context, intent);
            }

            // update our widget
            updateWidget();
        }
    };

    // our own settings observer :D
    private class WidgetSettingsObserver extends ContentObserver {
        public WidgetSettingsObserver(Handler handler) {
            super(handler);
        }

        public void observe() {
            ContentResolver resolver = mContext.getContentResolver();

            // watch for display widget
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.EXPANDED_VIEW_WIDGET),
                            false, this);

            // watch for scrollbar hiding
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.EXPANDED_HIDE_SCROLLBAR),
                            false, this);

            // watch for haptic feedback
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.EXPANDED_HAPTIC_FEEDBACK),
                            false, this);

            // watch for changes in buttons
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.WIDGET_BUTTONS),
                            false, this);

            // watch for changes in color
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.EXPANDED_VIEW_WIDGET_COLOR),
                            false, this);

            // watch for changes in indicator visibility
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.EXPANDED_HIDE_INDICATOR),
                            false, this);

            // watch for power-button specifc stuff that has been loaded
            for(Uri uri : PowerButton.getAllObservedUris()) {
                resolver.registerContentObserver(uri, false, this);
            }
        }

        public void unobserve() {
            ContentResolver resolver = mContext.getContentResolver();

            resolver.unregisterContentObserver(this);
        }

        @Override
        public void onChangeUri(Uri uri, boolean selfChange) {
            ContentResolver resolver = mContext.getContentResolver();
            Resources res = mContext.getResources();

            // first check if our widget buttons have changed
            if(uri.equals(Settings.System.getUriFor(Settings.System.WIDGET_BUTTONS))) {
                setupWidget();
            // now check if we change visibility
            } else if(uri.equals(Settings.System.getUriFor(Settings.System.EXPANDED_VIEW_WIDGET))) {
                updateVisibility();
            // now check for scrollbar hiding
            } else if(uri.equals(Settings.System.getUriFor(Settings.System.EXPANDED_HIDE_SCROLLBAR))) {
                updateScrollbar();
            }

            // do whatever the individual buttons must
            PowerButton.handleOnChangeUri(uri);

            // something happened so update the widget
            updateWidget();
        }
    }
}
