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

    private final static String ACTION_CM_SWITCH_ISPTYPE = "com.android.intent.action.CM_SWITCH_ISPTYPE";
    private final static String ACTION_CM_QUERY_ISPTYPE = "com.android.intent.action.CM_QUERY_ISPTYPE";
    private final static String ACTION_CM_QUERY_ISPTYPE_DONE = "com.android.intent.action.CM_QUERY_ISPTYPE_DONE";
    private final static String PHONE_NT_MODE = "networkMode";
    private static int NETWORK_MODE = -99;

    public ISPTypeButton() { 
	mType = BUTTON_ISPTYPE; 
    }

    @Override
    protected void updateState() {

        // need to do lp,2012/02/15,20:10
        switch (NETWORK_MODE) {
            case Phone.NT_MODE_WCDMA_PREF:
            case Phone.NT_MODE_WCDMA_ONLY:
            case Phone.NT_MODE_GSM_UMTS:
            case Phone.NT_MODE_GSM_ONLY:

                mIcon = R.drawable.g;
		break;

            case Phone.NT_MODE_CDMA:
            case Phone.NT_MODE_CDMA_NO_EVDO:
            case Phone.NT_MODE_EVDO_NO_CDMA:

                mIcon = R.drawable.c;
		break;

            case Phone.NT_MODE_GLOBAL:

                mIcon = R.drawable.w;
		break;

	    default:
		mIcon = R.drawable.unknown;
		break;
        }
    }

    @Override	
    protected void setupButton(View view) {
        super.setupButton(view);
	if(mView != null) {
	    Intent intent = new Intent(ACTION_CM_QUERY_ISPTYPE);
            mView.getContext().sendBroadcast(intent);
	}
    }

    @Override
    protected void toggleState() {
	Intent intent = new Intent(ACTION_CM_SWITCH_ISPTYPE);
	
        switch (NETWORK_MODE) {
            case Phone.NT_MODE_WCDMA_PREF:
            case Phone.NT_MODE_WCDMA_ONLY:
            case Phone.NT_MODE_GSM_UMTS:
            case Phone.NT_MODE_GSM_ONLY:
		Log.e(TAG,"set G to C");
                intent.putExtra(PHONE_NT_MODE,"" + Phone.NT_MODE_CDMA);
		break;

            case Phone.NT_MODE_CDMA:
            case Phone.NT_MODE_CDMA_NO_EVDO:
            case Phone.NT_MODE_EVDO_NO_CDMA:
		Log.e(TAG,"set C to W");
                intent.putExtra(PHONE_NT_MODE,"" + Phone.NT_MODE_GLOBAL);
		break;

            case Phone.NT_MODE_GLOBAL:
		Log.e(TAG,"set W to G");
                intent.putExtra(PHONE_NT_MODE,"" + Phone.NT_MODE_GSM_UMTS);
		break;

	    default:
		return;
        }

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

            } else if(action.equals(ACTION_CM_QUERY_ISPTYPE_DONE)) {

		String networkMode = intent.getStringExtra(PHONE_NT_MODE);
		Log.d(TAG,"LP mark ACTION_CM_QUERY_ISPTYPE_DONE -- " + networkMode);
		NETWORK_MODE = Integer.parseInt(networkMode);
	    }
    }

    @Override
    protected IntentFilter getBroadcastIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyIntents.ACTION_RADIO_TECHNOLOGY_CHANGED);
	filter.addAction(ACTION_CM_QUERY_ISPTYPE_DONE);
        return filter;
    }

}
