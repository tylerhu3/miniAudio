package info.androidhive.floatingview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import static android.view.View.VISIBLE;

/**
 * Created by tyler on 8/2/2019.
 */

public class PlayPause extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Toast.makeText(this,"Player Reactivated", Toast.LENGTH_SHORT).show();

        if(FloatingViewService.serviceAlive == true){
//            Toast.makeText(this,"Player Reactivated 2", Toast.LENGTH_SHORT).show();
            FloatingViewService.mParentView.setVisibility(VISIBLE);
            FloatingViewService.expandedView.setVisibility(VISIBLE);
            finish();
        }
        finish();
    }
}
