package info.androidhive.floatingview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import static android.view.View.VISIBLE;

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
