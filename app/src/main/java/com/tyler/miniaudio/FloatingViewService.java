package com.tyler.miniaudio;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import static android.view.View.VISIBLE;
/*
*
* 10/3/2019, Trying to get color ripple effect on mediaButton pressed
* Couldn't attempt it because no internet, check one line 543 tomorrow
*
* 10/8/2019, Still no ripple color so we giving up on that*/
public class FloatingViewService extends Service {
    // variable to check whether this service is active
    public static Boolean serviceAlive = false;

    //View setup:
    public WindowManager mWindowManager;
    public View mFloatingView;
    public WindowManager.LayoutParams params;
    public  View collapsedView, expandedView;
    public ImageView chatHeadImage;
    public  ViewGroup mParentView;
    static FloatingViewService mfloatViewService;
    public Notification notification;
    public SongAdapter songAdapter;
    public static int lockIcon = 0;
    //Context of this service:
    public  Context mContext;
    public RecyclerView recyclerView;
//    MusicPlayer mPlayer;
    //Mediaplayer image configuration
    public SeekBar seekBar, volumeBar;

    public ImageView playButton, nextButton,  prevButton, albumart, minimizeButton, maximizeButton, closeButton,
            shuffleButton;
    public long lastTouchTime = 0; //used to measure double tap for albumartView

    //The two variable savedPlayDrawableID, savedPausedDrawableID are basically for when I have
    //multiple themes for the media player and since the playButton is the only button that changes
    //from play to pause, Im using the variables
    int savedPlayDrawableID, savedPausedDrawableID;
    public static int themeNumber = 0, headChoice = 0;
    int seekBarColor = Color.RED;



    @Override
    public void onCreate() {
        super.onCreate();
        serviceAlive = true;
        mfloatViewService = this;
        mContext = this;
        setupView();
        setUpMediaPlayerViews();
        themeSetter();
        setUpRecycler();
        setButtonsMusicButtons();
        startForegroundService();
    }



    /////////////Set up Music Buttons ///////////////
    public void setButtonsMusicButtons() {

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayer.playPause();
            }
        });

        //Set the next button.
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayer.nextSong();
            }
        });

        //Set the prev button.
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayer.prevSong();
            }
        });
    }



    public void setUpRecycler(){
        recyclerView = mFloatingView.findViewById(R.id.recyclerView);
        //Create the List:
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(MainBottomNavActivity.songAdapter);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setUpMediaPlayerViews(){

        seekBar = mFloatingView.findViewById(R.id.seekBar);
        seekBar.getProgressDrawable().setColorFilter(seekBarColor, PorterDuff.Mode.SRC_IN);
        if (Build.VERSION.SDK_INT >= 16)
        seekBar.getThumb().setColorFilter(seekBarColor, PorterDuff.Mode.SRC_IN);
        //Title of song being played:
//        songText = mFloatingView.findViewById(R.id.playingSong);

        //Volume Bar instantiation
        volumeBar = mFloatingView.findViewById(R.id.volumeBar);
        //Setting bar and dot(thumb) color
        volumeBar.getProgressDrawable().setColorFilter(seekBarColor, PorterDuff.Mode.SRC_IN);
        if (Build.VERSION.SDK_INT >= 16)
        volumeBar.getThumb().setColorFilter(seekBarColor, PorterDuff.Mode.SRC_IN);
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                float newVolume = ((float) progress / (float) seekBar.getMax());
                //Display the newly selected number from picker
                MusicPlayer.setVolume(newVolume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Display the newly selected number from picker
                    MusicPlayer.setMediaPlayerProgress(progress);
            }
        });
        //Attach variables to media buttons
        playButton = mFloatingView.findViewById(R.id.play_btn);
        nextButton = mFloatingView.findViewById(R.id.next_btn);
        prevButton = mFloatingView.findViewById(R.id.prev_btn);
        albumart = mFloatingView.findViewById(R.id.albumart);

        //This sets up a double click action for the albumArt
        //As of current it minimizes music player to chat head
        albumart.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                long thisTime = event.getEventTime();


                // The below if statement simulates a double tap
                // On first touch, we record the time and then we
                // follow
                if (event.getAction() == MotionEvent.ACTION_DOWN &&
                        (thisTime - lastTouchTime < 250)) {
//                    Log.d("XXX", "DoubleTap Action");
                    lastTouchTime = -1;
                    collapsedView.setVisibility(VISIBLE);
                    expandedView.setVisibility(View.GONE);

                    return true;
                } else {
                    lastTouchTime = thisTime;
                    return false;
                }
            }
        });

        //Set the close button
        closeButton = mFloatingView.findViewById(R.id.buttonClose);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "Player Shutdown", Toast.LENGTH_SHORT).show();
                destroyMusicPlayer();
            }
        });

        //Set the minimize button
        minimizeButton = mFloatingView.findViewById(R.id.buttonMinimize);
        minimizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapsedView.setVisibility(VISIBLE);
                expandedView.setVisibility(View.GONE);
                if(lockIcon == 1){ //snap icon to side of screen
                    lockHead(Math.round(params.x));
                }
            }
        });

        maximizeButton = mFloatingView.findViewById(R.id.buttonMax);
        maximizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mParentView.setVisibility(View.INVISIBLE);
                Toast.makeText(mContext, "Minimize to Notification", Toast.LENGTH_SHORT).show();
            }
        });

        shuffleButton = mParentView.findViewById(R.id.buttonShuffle);
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicPlayer.shuffleSongs();

            }
        });

    }


    public void lockHead(int x){
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        if (x < screenWidth/2) {
            moveChatHead(Math.round(x), 0);
        } else {
            moveChatHead(Math.round(x), screenWidth);
        }
    }

    //////////////////////////////// For Animation Start

    public void moveChatHead(int start, int finish){
        AnimatorSet buttonAnimator = new AnimatorSet();
        ValueAnimator buttonAnimatorX = ValueAnimator.ofFloat(start,finish
        );
        buttonAnimatorX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int newX = Math.round(Float.valueOf(animation.getAnimatedValue().toString()));
                params.x = newX;
                mWindowManager.updateViewLayout(mParentView, params);
            }
        });
        buttonAnimatorX.setDuration(250);
        buttonAnimator.play(buttonAnimatorX);
        buttonAnimator.start();
    }


    //////////////////////////////// For Animation End

    //We make this class a singleton with the below

    public static FloatingViewService getInstance() {
        return mfloatViewService;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void changeBubbleHead(){
        switch(headChoice){
            case 1 : chatHeadImage.setImageResource(R.drawable.ic_android_circle); break;

            case 2 : chatHeadImage.setImageResource(R.drawable.ic_android_circle2); break;

            case 3 : chatHeadImage.setImageResource(R.drawable.ic_android_circle3); break;

            case 4 : chatHeadImage.setImageResource(R.drawable.ic_android_circle4); break;

            case 5 : chatHeadImage.setImageResource(R.drawable.ic_android_circle5); break;

            case 6 : chatHeadImage.setImageResource(R.drawable.ic_android_circle6); break;

            case 7 : chatHeadImage.setImageResource(R.drawable.ic_android_circle7); break;

            case 8 : chatHeadImage.setImageResource(R.drawable.ic_android_circle8); break;

            case 9 : chatHeadImage.setImageResource(R.drawable.ic_android_circle9); break;

            case 10 : chatHeadImage.setImageResource(R.drawable.ic_android_circle10); break;
            default:
                chatHeadImage.setImageResource(R.drawable.ic_android_circle);
        }
    }

/*public void themeSetter() - associated all the imageView variables
* this has nothing to do with the MusicPlayer class. The ImageViews associated with that are set up
* in setUpMediaPlayerViews() method
* with the correct layout id in layout_floating_widget.xml*/
    public void themeSetter() {

        //Important :: Please notice that setting the textcolor of the recyclerView text is located
        //in songAdapter class line 50
        TextView volumeBarText, seekBarText;
        chatHeadImage = mFloatingView.findViewById(R.id.collapsed_iv);
        changeBubbleHead();


        if (themeNumber == 1) { //Dark Theme
            shuffleButton.setImageResource(R.drawable.ic_repeat_white_24dp);
            albumart.setImageResource(R.drawable.album_art_2);
            playButton.setImageResource(R.drawable.play2);
            nextButton.setImageResource(R.drawable.next2);
            prevButton.setImageResource(R.drawable.prev2);
            savedPausedDrawableID = R.drawable.pause2;
            savedPlayDrawableID = R.drawable.play2;
            volumeBarText = mFloatingView.findViewById(R.id.volumeBarText);
            volumeBarText.setTextColor(Color.WHITE);
            seekBarText = mFloatingView.findViewById(R.id.seekBarText);
            seekBarText.setTextColor(Color.WHITE);
            expandedView.setBackgroundResource(R.drawable.round_corners_black);
        } else { //White Theme
            shuffleButton.setImageResource(R.drawable.ic_repeat_black_24dp);
            albumart.setImageResource(R.drawable.album_art_1);
            playButton.setImageResource(R.drawable.play);
            nextButton.setImageResource(R.drawable.next);
            prevButton.setImageResource(R.drawable.prev);
            savedPausedDrawableID = R.drawable.pause;
            savedPlayDrawableID = R.drawable.play;
            volumeBarText = mFloatingView.findViewById(R.id.volumeBarText);
            volumeBarText.setTextColor(Color.BLACK);
            seekBarText = mFloatingView.findViewById(R.id.seekBarText);
            seekBarText.setTextColor(Color.BLACK);
            expandedView.setBackgroundResource(R.drawable.round_corners);
        }
    }

    /*starForeground Service - creates a notification intent */
    public void startForegroundService() {

        Intent intent = new Intent(getApplicationContext(), SetVisibility.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent broadcastIntent = new Intent(this, PlayPauseMusic.class);
        PendingIntent playPauseIntent = PendingIntent.getBroadcast(this,
                0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent BI_Next = new Intent(this, NextMusic.class);
        PendingIntent nextIntent = PendingIntent.getBroadcast(this,
                0, BI_Next, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent BI_Prev = new Intent(this, PrevMusic.class);
        PendingIntent prevIntent = PendingIntent.getBroadcast(this,
                0, BI_Prev, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "MiniAudio",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);


            notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.noticon2)
                    .setContentTitle("Working in Background")
                    .setContentText("")
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0))
                    .addAction(R.drawable.ic_last_page_black_24dp, "Play/Pause",playPauseIntent)
                    .addAction(R.drawable.ic_chevron_right_black_24dp, "Next",nextIntent)
                    .addAction(R.drawable.ic_chevron_left_black_24dp, "Prev",prevIntent)
                    .setContentIntent(pi).build();
            startForeground(1337, notification);

        } else {
            notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.noticon2)
                    .setContentTitle("Working in Background")
                    .setContentText("")
                    .addAction(R.drawable.ic_last_page_black_24dp, "Play/Pause", playPauseIntent)
                    .addAction(R.drawable.ic_chevron_right_black_24dp, "Next",nextIntent)
                    .addAction(R.drawable.ic_chevron_left_black_24dp, "Prev",prevIntent)
                    .setContentIntent(pi).build();
            //The ID needs to be unique, right now it's 1337
            startForeground(1337, notification);
        }
    }







    /**
     * Detect if the floating view is collapsed or expanded.
     * returns true if the floating view is collapsed.
     */
    private boolean isViewCollapsed() {
        return mParentView == null || mFloatingView.findViewById(R.id.collapse_view).getVisibility() == VISIBLE;
    }





    public void destroyMusicPlayer() {
        serviceAlive = false;
        recyclerView = null;
        seekBar = null;
        volumeBar = null;
        songAdapter = null;
        playButton  = prevButton = null;
        nextButton = null;
        MusicPlayer.stopMusic();
        stopSelf();
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
//        Log.d("XXX", "onDestroy Called");
        MusicPlayer.isMusicPlaying = false; //stop the thread
        if (mParentView != null) mWindowManager.removeView(mParentView);
    }




    public void setupView(){
        //The below is to prevent Android above 7.0 crash
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);
        mFloatingView.setVisibility(View.GONE);

        mParentView = new FrameLayout(this) {
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    // handle the back button code;
                    Toast.makeText(mContext, "Back Button Pressed", Toast.LENGTH_SHORT).show();
                    expandedView.setVisibility(View.GONE);
                    collapsedView.setVisibility(VISIBLE);
                    return true;
                }
                return super.dispatchKeyEvent(event);
            }

            //if pressed home key,
            public void onCloseSystemDialogs(String reason) {
                //The Code Want to Perform.
                System.out.println("System dialog " + reason);
                if (reason.equals("homekey")) {
                    expandedView.setVisibility(View.GONE);
                    collapsedView.setVisibility(VISIBLE);
//                    Toast.makeText(mContext,"Home Button Pressed", Toast.LENGTH_SHORT).show();
                    // handle home button
                }
            }

        };
        mParentView.addView(mFloatingView);
        //Add the view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        //Add the view to the window.
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, //this was not focusable flag prior
                PixelFormat.TRANSLUCENT);

        //Specify the view position
        params.gravity = Gravity.TOP | Gravity.LEFT;        //Initially view will be added to top-left corner
//        Log.d("FloatingView", "Gravities top :" + Gravity.TOP + " and left" + Gravity.LEFT);
        params.x = 0;
        params.y = 100;

        //We will add mParentView instead of mFloatingView because it has mFloatingView in
        //mParentView
        mWindowManager.addView(mParentView, params);
        //The root element of the collapsed view layout:
        collapsedView = mFloatingView.findViewById(R.id.collapse_view);

        //The root element of the expanded view layout:
        expandedView = mFloatingView.findViewById(R.id.expanded_container);
//        expandedView.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // Defined Array values to show in ListView
        //Drag and move floating view using user's touch action.
        mFloatingView.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //remember the initial position.
                        initialX = params.x;
                        initialY = params.y;

                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);
                        Xdiff = Math.abs(Xdiff);
                        Ydiff = Math.abs(Ydiff);
                        //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                        //So that is click event.
                        //////////////////////////////// For Animation
                        //This below will prevent automatically moving the View back to a corner
                        if (expandedView.getVisibility() != View.GONE)
                            return true;

                        if(lockIcon == 1){ //snap icon to side of screen
                            lockHead(Math.round(event.getRawX()));
                        }

                        if (Xdiff < 10 && Ydiff < 10) {
                            if (isViewCollapsed()) {
                                //When user clicks on the image view of the collapsed layout,
                                //visibility of the collapsed layout will be changed to "View.GONE"
                                //and expanded view will become visible.
                                //collapsedView.setVisibility(View.GONE);
                                expandedView.setVisibility(VISIBLE);
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:

                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);

                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mParentView, params);
                        return true;
                }
                return false;
            }
        });

        final Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        in.setDuration(500);
        mFloatingView.setVisibility(VISIBLE);
        mFloatingView.startAnimation(in);
    }


    public FloatingViewService() {
    }
}


