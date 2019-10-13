package com.tyler.miniaudio;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

public class BubbleHead {

    ImageView prev, h1, h2, h3, h4;


    BubbleHead(View V){

        h1 = V.findViewById(R.id.head1);
        h1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (prev!= null){
                    prev.setBackgroundColor(Color.WHITE);
                }
                prev = h1;
                FloatingViewService.headChoice = 1;
                h1.setBackgroundColor(Color.GRAY);

            }
        });

        h2 = V.findViewById(R.id.head2);
        h2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (prev!= null){
                    prev.setBackgroundColor(Color.WHITE);
                }
                prev = h2;
                FloatingViewService.headChoice = 2;
                h2.setBackgroundColor(Color.GRAY);
            }
        });

        h3 = V.findViewById(R.id.head3);
        h3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (prev!= null){
                    prev.setBackgroundColor(Color.WHITE);
                }
                prev = h3;
                FloatingViewService.headChoice = 3;
                h3.setBackgroundColor(Color.GRAY);
            }
        });

        h4 = V.findViewById(R.id.head4);
        h4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (prev!= null){
                    prev.setBackgroundColor(Color.WHITE);
                }
                prev = h4;
                FloatingViewService.headChoice = 4;
                h4.setBackgroundColor(Color.GRAY);
            }
        });
    }
}
