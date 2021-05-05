package com.tyler.miniaudio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class NotificationReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getStringExtra("toastMessage");
        Log.d(MainBottomNavActivity.TAG, "Notification is ready");
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
