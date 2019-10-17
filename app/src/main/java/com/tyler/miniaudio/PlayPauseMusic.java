package com.tyler.miniaudio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class PlayPauseMusic extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        MusicPlayer.playPause();
        Toast.makeText(context, "Play/Pause Song", Toast.LENGTH_SHORT).show();
    }
}
