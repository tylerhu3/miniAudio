package com.tyler.miniaudio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;

import static android.view.View.VISIBLE;
import static com.tyler.miniaudio.MainBottomNavActivity.blackOn;
import static com.tyler.miniaudio.MainBottomNavActivity.myReceiver;

public class HomeFragment extends Fragment {

    private Button destroyWidgetButton;
    public HomeFragment() {
        new MusicPlayer();
        // Required empty public constructor
        loadSongs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_home, container, false);
    }



    ////////// Load ALL audio from Storage
    private void loadSongs(){

        Context applicationContext = MainBottomNavActivity.getContextOfApplication();
        applicationContext.getContentResolver();
        MainBottomNavActivity._songs = new ArrayList<>();
        Toast.makeText(MainBottomNavActivity.mContext, "Loading Songs...", Toast.LENGTH_SHORT).show();

        //grab music files from "sdcard":
//        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        // Grab music from internal storage:
        Uri uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;

        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Cursor cursor = applicationContext.getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    SongInfo s = new SongInfo(name, artist, url);
                    try {
                        MainBottomNavActivity._songs.add(s);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainBottomNavActivity.mContext, "add song error", Toast.LENGTH_SHORT).show();

                    }
                } while (cursor.moveToNext());
            }
            Toast.makeText(MainBottomNavActivity.mContext, "Size of MainBottomNavActivity._songs" + MainBottomNavActivity._songs.size(), Toast.LENGTH_SHORT).show();

            cursor.close();
            MainBottomNavActivity.songAdapter = new SongAdapter(MainBottomNavActivity.mContext, MainBottomNavActivity._songs);
            MainBottomNavActivity.songAdapter.setOnItemClickListener(new SongAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Context context,/*final Button b,*/ View view, final SongInfo obj, final int position) {
                    MusicPlayer.musicPlayerSongChange(position);
//                view.setBackgroundColor(Color.MAGENTA);
//                String songName = obj.getArtistname() + " - " + obj.getSongname();
//                songText.setText(songName);
                }
            });
        }
    }





    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new BubbleHead(view);
        view.findViewById(R.id.launchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FloatingViewService.serviceAlive){
                    Toast.makeText(getActivity(),"Already running", Toast.LENGTH_SHORT).show();
                    FloatingViewService.getInstance().mParentView.setVisibility(VISIBLE);
                    return;
                }
                Log.d("XXX", "initializeVIew");
                if(getActivity() != null) {
                    getActivity().startService(new Intent(getActivity(), FloatingViewService.class));
                    getActivity().moveTaskToBack(true);
                }
//                getActivity().finish();

            }
        });


        destroyWidgetButton = view.findViewById(R.id.buttonDestroyWidget);
        destroyWidgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FloatingViewService.serviceAlive){
                    FloatingViewService.getInstance().destroyMusicPlayer();
                    Toast.makeText(getActivity(), "Shut download", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getActivity(), "Music player not active", Toast.LENGTH_SHORT).show();
                }
            }
        });

        final Switch imySwitch = view.findViewById(R.id.themeSwitch);
        if(blackOn == 1){
            imySwitch.setChecked(true);
        }
        imySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                if(imySwitch.isChecked()){
                    blackOn = 1;
                }
                else
                {
                    blackOn = 0;
                }
            }
        });


    }

}
