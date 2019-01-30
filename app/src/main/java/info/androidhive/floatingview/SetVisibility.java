package info.androidhive.floatingview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import static android.view.View.VISIBLE;

public class SetVisibility extends AppCompatActivity {

    @Override
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
