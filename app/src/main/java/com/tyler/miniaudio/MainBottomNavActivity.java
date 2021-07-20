package com.tyler.miniaudio;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.MenuItem;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;
import java.util.ArrayList;


public class MainBottomNavActivity extends AppCompatActivity {
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    public static Context mContext;
    // a static variable to get a reference of our application context
    public static Context contextOfApplication;
    public static MusicIntentReceiver myReceiver;
    public static String TAG="Background Audio Player";
    private static final int PERMISSION_REQUEST_CODE = 1;
    SavedPreferences savedPreferences;


    ///This is for headphone disconnect
    private class MusicIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                if (!MusicPlayer.isPlaying()) //If music is not playing when unplug don't bother
                    return;
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        Log.d(TAG, "Headset is unplugged");

                        if (MusicPlayer.isPlaying()) {
                            MusicPlayer.playPause();
                        }
                        break;
                    case 1:
                        Log.d(TAG, "Headset is plugged");
                        break;
                    default:
                        Log.d(TAG, "I have no idea what the headset state is");
                }
            }
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.navigation_instructions:
                    selectedFragment = new InstructionFragment();
                    break;
                case R.id.navigation_misc:
                    selectedFragment = new MiscFragment();
                    break;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    selectedFragment).commit();
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        SavedPreferences.init(mContext);
        savedPreferences = SavedPreferences.getInstance();
        contextOfApplication = getApplicationContext();
        SavedPreferences.init(contextOfApplication);
        setContentView(R.layout.activity_main_bottom_nav);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //I added this if statement to keep the selected fragment when rotating the device
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new HomeFragment()).commit();
        checkUserPermission();
        headPhoneDetection();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    savedPreferences.put(SavedPreferences.PERMISION_TO_STORAGE, true);
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    savedPreferences.put(SavedPreferences.PERMISION_TO_STORAGE, false);
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

    private void headPhoneDetection(){
        //For detecting headphone disconnects we will new a MusicIntentReciever
        // check " private class MusicIntentReceiver extends BroadcastReceiver" below:
        myReceiver = new MusicIntentReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(myReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                savedPreferences.put(SavedPreferences.PERMISION_TO_STORAGE, true);
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                savedPreferences.put(SavedPreferences.PERMISION_TO_STORAGE, false);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            savedPreferences.put(SavedPreferences.PERMISION_TO_STORAGE, true);
            return true;
        }
    }

    public void checkUserPermission() {
        //Check for storage Permissions
        isStoragePermissionGranted();
        //Check if the application has draw over other apps permission or not?
        //This permission is by default available for API<23. But for API > 23
        //you have to ask for the permission in runtime.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this,
                    "Draw over other app permission not available. Turn on permission and " +
                            "reopen app",
                    Toast.LENGTH_SHORT).show();
            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {

            //Check if the permission is granted or not.
            if (resultCode == RESULT_OK) {
            } else { //Permission is not available
                Toast.makeText(this,
                        "Draw over other app permission not available. Turn on permission and " +
                                "reopen app",
                        Toast.LENGTH_SHORT).show();

                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
