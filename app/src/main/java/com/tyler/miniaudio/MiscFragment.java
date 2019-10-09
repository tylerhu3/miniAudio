package com.tyler.miniaudio;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import static com.tyler.miniaudio.MainBottomNavActivity.blackOn;


/**
 * A simple {@link Fragment} subclass.
 */
public class MiscFragment extends Fragment {

    private Button destroyWidgetButton;

    public MiscFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_misc, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        destroyWidgetButton = view.findViewById(R.id.buttonDestroyWidget);
        destroyWidgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FloatingViewService.serviceAlive == true){
                    FloatingViewService.getInstance().destroyMusicPlayer();
                    Toast.makeText(getActivity(), "Music closed", Toast.LENGTH_SHORT).show();
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
