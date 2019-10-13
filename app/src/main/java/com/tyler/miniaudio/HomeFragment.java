package com.tyler.miniaudio;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import static android.view.View.VISIBLE;
import static com.tyler.miniaudio.MainBottomNavActivity.blackOn;


public class HomeFragment extends Fragment {

    private Button destroyWidgetButton;
    public int headChoice = 0;
    public HomeFragment() {
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
                if(FloatingViewService.serviceAlive == true){
                    Toast.makeText(getActivity(),"Already running", Toast.LENGTH_SHORT).show();
                    FloatingViewService.getInstance().mParentView.setVisibility(VISIBLE);
                    return;
                }
                Log.d("XXX", "initializeVIew");
                getActivity().startService(new Intent(getActivity(), FloatingViewService.class));
                getActivity().finish();
            }
        });


        destroyWidgetButton = view.findViewById(R.id.buttonDestroyWidget);
        destroyWidgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FloatingViewService.serviceAlive == true){
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
