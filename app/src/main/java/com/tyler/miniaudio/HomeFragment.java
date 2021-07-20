package com.tyler.miniaudio;

import android.content.Context;
import android.content.Intent;
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
import static com.tyler.miniaudio.MainBottomNavActivity.mContext;

public class HomeFragment extends Fragment {

    SavedPreferences savedPreferences = SavedPreferences.getInstance();
    private Button destroyWidgetButton;
    public HomeFragment() {
        new MusicPlayer();
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_home, container, false);
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

                if (savedPreferences.get(SavedPreferences.PERMISION_TO_STORAGE, false) == false){
                    Toast.makeText(getActivity(),"App Needs Permission To Read Storage", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d("XXX", "initializeVIew");
                if(getActivity() != null) {
                    getActivity().startService(new Intent(getActivity(), FloatingViewService.class));
                    getActivity().moveTaskToBack(true);
                }

                getActivity().moveTaskToBack(true);
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

        final Switch lightThemeSwitch = view.findViewById(R.id.themeSwitch);

        Boolean lightOn = savedPreferences.get(SavedPreferences.LIGHT_MODE, true);
        if(lightOn){
            lightThemeSwitch.setChecked(true);
        }

        //this screen is to allow the floating icon to snap to the side of the screen
        final Switch lockSwitch = view.findViewById(R.id.lockSwitch);

        Boolean lockToSideOn = savedPreferences.get(SavedPreferences.SNAP_TO_GRIP, true);

        lockSwitch.setChecked(lockToSideOn);

        lockSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                // do something, the isChecked will be
                // true if the switch is in the On position
                if(lockSwitch.isChecked()){
                    savedPreferences.put(SavedPreferences.SNAP_TO_GRIP, true);
                }
                else
                {
                    savedPreferences.put(SavedPreferences.SNAP_TO_GRIP, false);
                }
            }
        });


        lightThemeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position

                if(FloatingViewService.serviceAlive){
                    Toast.makeText(mContext, "Close and Reopen music player for changes", Toast.LENGTH_SHORT).show();
                }

                if(lightThemeSwitch.isChecked()){
                    savedPreferences.put(SavedPreferences.LIGHT_MODE, true);
                    if(FloatingViewService.serviceAlive){
                        FloatingViewService.getInstance().themeSetter();
                    }
                }
                else
                {
                    savedPreferences.put(SavedPreferences.LIGHT_MODE, false);
                    if(FloatingViewService.serviceAlive){
                        FloatingViewService.getInstance().themeSetter();
                    }
                }
            }
        });


    }

}
