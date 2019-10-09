package com.tyler.miniaudio;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import static android.view.View.VISIBLE;

public class SetVisibility extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Toast.makeText(this,"Player Reactivated", Toast.LENGTH_SHORT).show();

        if (FloatingViewService.serviceAlive == true) {
//            Toast.makeText(this,"Player Reactivated 2", Toast.LENGTH_SHORT).show();
            FloatingViewService.getInstance().mParentView.setVisibility(VISIBLE);
            FloatingViewService.getInstance().expandedView.setVisibility(VISIBLE);
            finish();
        }
        finish();
    }

}