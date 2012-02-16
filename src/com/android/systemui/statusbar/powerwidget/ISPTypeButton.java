package com.android.systemui.statusbar.powerwidget;

import com.android.systemui.R;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.view.View;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings;
import android.util.Log;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.TelephonyIntents;
import android.content.BroadcastReceiver;

import java.util.ArrayList;
import java.util.List;

public class ISPTypeButton extends PowerButton{
    private static final String TAG = "ISPTypeButton";

    public final static String ACTION_CM_SWITCH_ISPTYPE = "com.android.intent.action.CM_SWITCH_ISPTYPE";
    public final static String ACTION_CM_QUERY_ISPTYPE = "com.android.intent.action.CM_QUERY_ISPTYPE";
    public static final String EXTRA_ISP_TYPE = "ispType";
    private static int NETWORK_MODE = -99;

    public ISPTypeButton() { 
	mType = BUTTON_ISPTYPE; 
    }

    @Override
    protected void updateState() {

        // need to do lp,2012/02/15,20:10
        switch (NETWORK_MODE) {
           case Phone.PHONE_TYPE_GSM:
               mIcon = R.drawable.g;
               break;
           case Phone.PHONE_TYPE_CDMA:
               mIcon = R.drawable.c;
               break;
	   default:
	       mIcon = R.drawable.unkown;
	       break;
        }
    }

    @Override
    protected void toggleState() {
	Intent intent = new Intent(ACTION_CM_SWITCH_ISPTYPE);
	mView.getContext().sendBroadcast(intent);	
    }

    @Override
    protected boolean handleLongClick() {
        // it may be better to make an Intent action for this or find the appropriate one
        // we may want to look at that option later
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.android.settings", "com.android.settings.RadioInfo");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mView.getContext().startActivity(intent);
        return true;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

	String action = intent.getAction();
            if (action.equals(TelephonyIntents.ACTION_RADIO_TECHNOLOGY_CHANGED)) {
                String newPhone = intent.getStringExtra(Phone.PHONE_NAME_KEY);
                Log.d(TAG, "LP mark user  Radio technology switched. Now " + newPhone + " active");
		if(newPhone.equals("GSM")) {
		    NETWORK_MODE = Phone.PHONE_TYPE_GSM;
		} else if(newPhone.equals("CDMA")) {
		    NETWORK_MODE = Phone.PHONE_TYPE_CDMA;
		}
		updateState();
            } else if(action.equals(ACTION_CM_QUERY_ISPTYPE)) {
		String networkMode = intent.getStringExtra(EXTRA_ISP_TYPE);
		Log.d(TAG,"LP mark current networkmode -- " + networkMode);
		NETWORK_MODE = Integer.parseInt(networkMode);
		updateState();
	    }
    }

    @Override
    protected IntentFilter getBroadcastIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyIntents.ACTION_RADIO_TECHNOLOGY_CHANGED);
	filter.addAction(ACTION_CM_QUERY_ISPTYPE);
        return filter;
    }

}
