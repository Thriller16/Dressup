package com.jwbinc.app.dressupapk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ShakeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(null != intent && intent.getAction().equals("shake.detector")){

        }
    }
}
