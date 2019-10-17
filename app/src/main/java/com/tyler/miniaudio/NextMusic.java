package com.tyler.miniaudio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NextMusic extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        MusicPlayer.nextSong();
        Toast.makeText(context, "Next Song", Toast.LENGTH_SHORT).show();
    }
}

