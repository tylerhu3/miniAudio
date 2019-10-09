package com.tyler.miniaudio;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


public class Shutdown extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_shutdown);
        Toast.makeText(this,"Player Destroy", Toast.LENGTH_SHORT).show();

        if(FloatingViewService.serviceAlive == true){
            FloatingViewService.getInstance().destroyMusicPlayer();
            finish();
        }
        else{
            Toast.makeText(this,"Player Already Destroy", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
