package com.tyler.miniaudio;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
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

import java.util.ArrayList;
import java.util.Random;

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
    Notification notification;

    //Context of this service:
    public  Context mContext;

    //MediaPlayer SetUp:
    public ArrayList<SongInfo> _songs;
    public RecyclerView recyclerView;
    public  SongAdapter songAdapter;
    public  MediaPlayer mediaPlayer;
    public Random randomNumberGenerator = new Random();// For shuffling songs
    public Handler myHandler = new Handler();
    public int shuffleOn = 0; //variable to choose whether to shuffle Music or not
    private int currentSong = 0;
    private float volumeChange = 1;
    private MusicIntentReceiver myReceiver;
    //For Thread work
    //volatile means that our threads always access the
    //most up to date version of the variable
    public volatile Thread seekBarProgression;
    public volatile Boolean isMusicPlaying = true;


    //Mediaplayer image configuration
    public SeekBar seekBar, volumeBar;
    public ImageView playButton, nextButton,  prevButton, albumart, minimizeButton, maximizeButton, closeButton,
            shuffleButton;
    public long lastTouchTime = 0; //used to measure double tap for albumartView

    //The two variable savedPlayDrawableID, savedPausedDrawableID are basically for when I have
    //multiple themes for the media player and since the playButton is the only button that changes
    //from play to pause, Im using the variables
    int savedPlayDrawableID, savedPausedDrawableID;
    public static int themeNumber = 0;
    int seekBarColor = Color.RED;



    @Override
    public void onCreate() {
        super.onCreate();
        serviceAlive = true;
        mfloatViewService = this;
        mContext = this;
        setupView();
        setUpMediaPlayer();
        startForegroundService();

    }

    public void setUpMediaPlayer(){
        headPhoneDetection();
        loadSongs();
        setUpMediaPlayerViews();
        setButtonsMusicButtons();
        setUpRecycler();
        themeSetter();
        moveSeekBarWhilePlayingMusic();
    }

    /////////////Set up Music Buttons ///////////////
    public void setButtonsMusicButtons() {

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer == null) {
                    musicPlayerSongChange(currentSong);
                    return;
                }
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playButton.setImageResource(savedPlayDrawableID);
                } else {
                    mediaPlayer.start();
                    playButton.setImageResource(savedPausedDrawableID);
                }
            }
        });

        //Set the next button.
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer == null)
                    return;
                ++currentSong;
                //insert shuffle code here:
                if (shuffleOn == 1) {
                    currentSong = randomNumberGenerator.nextInt(_songs.size());
                }



                musicPlayerSongChange(currentSong);

            }
        });

        //Set the prev button.
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer == null)
                    return;
                musicPlayerSongChange(--currentSong);
            }
        });

    }


    //////////// Function for changing songs
    public void musicPlayerSongChange(int position) {

        currentSong = position;
        if (currentSong >= _songs.size()) {
            currentSong = 0;
            return;
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }

                    mediaPlayer = new MediaPlayer();

//                    Log.d("XXX", "\n_songsName: " + _songs.get(currentSong).getSongname() +
//                            "Artist Name: " + _songs.get(currentSong).getArtistname());
                    mediaPlayer.setVolume(volumeChange, volumeChange);
                    mediaPlayer.setDataSource(_songs.get(currentSong).getSongUrl());
                    mediaPlayer.prepareAsync();
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {

                            seekBar.setProgress(0);
                            seekBar.setMax(mediaPlayer.getDuration());
                            mp.start();
//                            Log.d("Prog", "run: " + mediaPlayer.getDuration());
                        }
                    });
                    playButton.setImageResource(savedPausedDrawableID);

                    //When the current mediafile is finish playing do this:
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            playButton.setImageResource(savedPlayDrawableID);
                            if (currentSong + 1 != _songs.size()) { //We are not final track
//                                Log.d("XXX", "Next Song");

                                if(shuffleOn == 0) {
                                    ++currentSong;
                                }
                                else if (shuffleOn == 1) {
                                    currentSong = randomNumberGenerator.nextInt(_songs.size());
                                }
                                musicPlayerSongChange(currentSong);
                            } else { //We are on final track
                                if (shuffleOn == 1) {
                                    currentSong = randomNumberGenerator.nextInt(_songs.size());
                                }
                                else{
                                    currentSong = 0;
                                }
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
//                    Log.d("XXX", e.getMessage());
                    //This statement below
                    Toast.makeText(FloatingViewService.this, "Error Playing Song", Toast.LENGTH_SHORT).show();
                }
            }
        };

        /*We have a Toast message inside the runnable so we need a handler to take send information
        back into the runnable to be able to make that Toast Message work
         Also: Anything that runs on the main thread needs a handler, the handler will execute
         on the thread it was created, main in our case*/
//        new Thread(runnable).start();
        myHandler.postDelayed(runnable, 1000);
    }


    public void moveSeekBarWhilePlayingMusic(){
        seekBarProgression = new SeekBarTracker();
        seekBarProgression.start();
    }

    public void headPhoneDetection(){
        //For detecting headphone disconnects we will new a MusicIntentReciever
        // check " private class MusicIntentReceiver extends BroadcastReceiver" below:
        myReceiver = new MusicIntentReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(myReceiver, filter);
    }

    public void setUpRecycler(){
        recyclerView = mFloatingView.findViewById(R.id.recyclerView);
        //Create the List:
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);
        songAdapter = new SongAdapter(this, _songs);

        songAdapter.setOnItemClickListener(new SongAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Context context,/*final Button b,*/ View view, final SongInfo obj, final int position) {
                musicPlayerSongChange(position);
//                view.setBackgroundColor(Color.MAGENTA);
//                String songName = obj.getArtistname() + " - " + obj.getSongname();
//                songText.setText(songName);
            }
        });
        recyclerView.setAdapter(songAdapter);
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
                volumeChange = ((float) progress / (float) seekBar.getMax());
                //Display the newly selected number from picker
                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(volumeChange, volumeChange);
                }
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
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
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
//                    if (params.x < midScreenWidthSize) {
//                        moveChatHead(Math.round(params.x), 0);
//                    } else {
//
//                        int orientation = getResources().getConfiguration().orientation;
//                        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                            // In landscape
//                            moveChatHead(Math.round(params.x), Resources.getSystem().getDisplayMetrics().heightPixels);
//                            Log.d("XXX", "landscape " + Resources.getSystem().getDisplayMetrics().heightPixels);
//                        } else {
//                            // In portrait
//                            moveChatHead(Math.round(params.x), Resources.getSystem().getDisplayMetrics().widthPixels);
//
//                            Log.d("XXX", "wid " + Resources.getSystem().getDisplayMetrics().heightPixels);
//
//                        }
//                    }
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
//                if (params.x < midScreenWidthSize) {
//                    //animations missing here
//                    moveChatHead(Math.round(params.x), 0);
//                } else {
                //animations missing here
//                    moveChatHead(Math.round(params.x), Resources.getSystem().getDisplayMetrics().widthPixels);
//                }
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
        //TODO: The shuffle button comes on as empty blank by default
        //so this statement below just fills it in with a loop icon
        shuffleButton.setImageResource(R.drawable.ic_repeat_black_24dp);
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*This is honestly better off as a circular linkedList but I wont both with
                * the trouble, just a possible future revision*/
                if (shuffleOn == 0) {
                    shuffleButton.setImageResource(R.drawable.ic_repeat_one_black_24dp);
                    Toast.makeText(mContext, "Repeat Mode", Toast.LENGTH_SHORT).show();
                    shuffleOn = 2;
                } else if (shuffleOn == 2){
                    shuffleButton.setImageResource(R.drawable.ic_shuffle_black_24dp);
                    Toast.makeText(mContext, "Shuffle Mode", Toast.LENGTH_SHORT).show();
                    shuffleOn = 1;
                }
                else{
                    shuffleButton.setImageResource(R.drawable.ic_repeat_black_24dp);
                    Toast.makeText(mContext, "Normal Mode", Toast.LENGTH_SHORT).show();
                    shuffleOn = 0;
                }

            }
        });

    }

    //This allows external classes to access this class
    public static FloatingViewService getInstance() {
        return mfloatViewService;
    }

    ////////// Load ALL audio from Storage
    private void loadSongs() {

        _songs = new ArrayList<>();
        Toast.makeText(this, "Loading Songs...", Toast.LENGTH_SHORT).show();

        //grab music files from "sdcard":
//        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        // Grab music from internal storage:
        Uri uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;

        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    SongInfo s = new SongInfo(name, artist, url);

                    try {
                        _songs.add(s);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(mContext, "add song error", Toast.LENGTH_SHORT).show();

                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
            songAdapter = new SongAdapter(FloatingViewService.this, _songs);
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



/*public void themeSetter() - associated all the imageView variables
* with the correct layout id in layout_floating_widget.xml*/
    public void themeSetter() {

        //Important :: Please notice that setting the textcolor of the recyclerView text is located
        //in songAdapter class line 50
        TextView volumeBarText, seekBarText;
        chatHeadImage = mFloatingView.findViewById(R.id.collapsed_iv);
        themeNumber = MainBottomNavActivity.blackOn;
        if (themeNumber == 1) { //Dark Theme


            chatHeadImage.setImageResource(R.drawable.ic_android_circle2);
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
            chatHeadImage.setImageResource(R.drawable.ic_android_circle);
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


        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "MiniAudio",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.noticon2)
                    .setContentTitle("MiniAudio")
                    .setContentText("Touch here to Expand")
                    .addAction(R.drawable.ic_zoom_out_map_black_24dp, "On",pi)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0))
                    .setContentIntent(pi).build();
            startForeground(1337, notification);
        } else {
            notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.noticon2)
                    .setContentTitle("MiniAudio")
                    .setContentText("Touch here to expand")
                    .addAction(R.drawable.ic_zoom_out_map_black_24dp, "On",pi)
                    .setContentIntent(pi).build();
            //The ID needs to be unique, right now it's 1337
            startForeground(1337, notification);
        }
    }









    ///This is for headphone disconnect
    private class MusicIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
//                        Log.d(TAG, "Headset is unplugged");
                        if (mediaPlayer == null)
                            break;
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            playButton.setImageResource(savedPlayDrawableID);
                        }

                        break;
                    case 1:
//                        Log.d(TAG, "Headset is plugged");
                        if (mediaPlayer == null)
                            break;
                        if (!mediaPlayer.isPlaying()) {
                            mediaPlayer.start();
                            playButton.setImageResource(savedPausedDrawableID);
                        }
                        break;
                    default:
//                        Log.d(TAG, "I have no idea what the headset state is");
                }
            }
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
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        recyclerView = null;
        _songs = null;
        seekBar = null;
        volumeBar = null;
        songAdapter = null;
        playButton  = prevButton = null;
        nextButton = null;
        stopSelf();
    }








    @Override
    public void onDestroy() {
        super.onDestroy();
//        Log.d("XXX", "onDestroy Called");
        unregisterReceiver(myReceiver);
        isMusicPlaying = false; //stop the thread
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

                        //The code below moves the chathead back to the side of the screen
//                        if (event.getRawX() < midScreenWidthSize) {
//                            moveChatHead(Math.round(event.getRawX()), 0);
//                        } else {
//                            moveChatHead(Math.round(event.getRawX()), 1080);
//                        }
                        //////////////////////////////// For Animation
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
//                        Log.d("Floating View Service", "x: " + params.x + " y: " + params.x);

//                        //This if statement below is to fix a strange draw delay
//                        if (initialX > midScreenWidthSize && expandedView.getVisibility() == View.GONE) {
//                            params.x -= 120;
//                        }
//                        if (initialX > midScreenWidthSize && expandedView.getVisibility() != View.GONE) {
//                            params.x -= 600;
//                        }
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


