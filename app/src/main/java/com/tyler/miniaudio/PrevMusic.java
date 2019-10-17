package com.tyler.miniaudio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class PrevMusic extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        MusicPlayer.prevSong();
        Toast.makeText(context, "Previous Song", Toast.LENGTH_SHORT).show();
    }
}

