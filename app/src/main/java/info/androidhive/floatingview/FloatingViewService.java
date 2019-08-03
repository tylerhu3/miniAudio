package info.androidhive.floatingview;
/*The base code is borrowed from android hive, the mediaplayer and everything else is just me
* building upon the original code*/
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.Random;

import static android.view.View.VISIBLE;

public class FloatingViewService extends Service {
    public static Boolean serviceAlive = false; // variable to check whether this service is active
    public boolean shuffleOn = false;
    //For headphone detection:
    private final String TAG = "FloatViewService";
    private MusicIntentReceiver myReceiver;
    Random randomNumberGenerator = new Random();// For shuffling songs
    //Imported from musicPlayer example from pawankumar:
    public ArrayList<SongInfo> _songs = new ArrayList<>();
    public RecyclerView recyclerView;
    private SeekBar seekBar;
    private int currentSong = 0;
    private SeekBar volumeBar;
    private float volumeChange = 1;
    public static SongAdapter songAdapter;
    public static MediaPlayer mediaPlayer;
    public ImageView playButton, nextButton, prevButton, albumart, minimizeButton, maximizeButton, closeButton,
            shuffleButton;
    public long lastTouchTime = 0; //used to measure double tap
    //    private TextView songText;
    //Old variables
    public WindowManager mWindowManager;
    public View mFloatingView;
    public Handler myHandler = new Handler();
    //These 3 variables use to be final and existed only in onCreate:
    public WindowManager.LayoutParams params;
    //The root element of the expanded view layout
    public static View collapsedView, expandedView;
    public ImageView chatHeadImage;
    //For Animations:
    public int midScreenWidthSize = Resources.getSystem().getDisplayMetrics().widthPixels / 2;

    public static ViewGroup mParentView;
    //For Thread work
    private volatile Thread t;
    private volatile Boolean threading = true;
    public static Context mContext;
    static FloatingViewService mfloatViewService;
    int seekBarColor = Color.RED; // Variable to decide seekBar colors
    public static int themeNumber = 0;
    int savedPlayDrawableID, savedPausedDrawableID;
    Notification notification;

    public static FloatingViewService getInstance() {
        return mfloatViewService;
    }

    public void themeSetter() {
        TextView volumeBarText, seekBarText;
        chatHeadImage = mFloatingView.findViewById(R.id.collapsed_iv);

        if (themeNumber == 1) { //Dark Theme
            chatHeadImage.setImageResource(R.drawable.ic_android_circle2);
            albumart.setImageResource(R.drawable.music_player3);
            playButton.setImageResource(R.drawable.play2);
            nextButton.setImageResource(R.drawable.next2);
            prevButton.setImageResource(R.drawable.prev2);
            savedPausedDrawableID = R.drawable.pause2;
            savedPlayDrawableID = R.drawable.play2;
            volumeBarText = mFloatingView.findViewById(R.id.voluneBarText);
            volumeBarText.setTextColor(Color.WHITE);
            seekBarText = mFloatingView.findViewById(R.id.seekBarText);
            seekBarText.setTextColor(Color.WHITE);
            expandedView.setBackgroundResource(R.drawable.round_corners_black);
        } else { //White Theme
            chatHeadImage.setImageResource(R.drawable.ic_android_circle);
            albumart.setImageResource(R.drawable.music_player);
            playButton.setImageResource(R.drawable.play);
            nextButton.setImageResource(R.drawable.next);
            prevButton.setImageResource(R.drawable.prev);
            savedPausedDrawableID = R.drawable.pause;
            savedPlayDrawableID = R.drawable.play;
            volumeBarText = mFloatingView.findViewById(R.id.voluneBarText);
            volumeBarText.setTextColor(Color.BLACK);
            seekBarText = mFloatingView.findViewById(R.id.seekBarText);
            seekBarText.setTextColor(Color.BLACK);
            expandedView.setBackgroundResource(R.drawable.round_corners);

        }
    }

    public FloatingViewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*onStartCommand  is called every time a client starts the service
    using startService(Intent intent). This means that onStartCommand() can get called multiple
    times. You should do the things in this method that are needed each time a client requests
    something from your service. This depends a lot on what your service does and how it communicates
    with the clients (and vice-versa).*/
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        loadSongs();
        themeNumber = MainActivity.blackOn;
        Log.d("XXX", "theme Number: " + themeNumber);
        serviceAlive = true;
        mfloatViewService = this;
        mContext = this;
        //Fix to Android above 7.0
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        //For detecting headphone disconnects we will new a MusicIntentReciever
        // check " private class MusicIntentReceiver extends BroadcastReceiver" below:
        myReceiver = new MusicIntentReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(myReceiver, filter);
        //Inflate the floating view layout we created
        //TODO: figure out whether it would make a difference to have mParentView be the inflater
        //over mFloatingView for animations
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
        Log.d("FloatingView", "Gravities top :" + Gravity.TOP + " and left" + Gravity.LEFT);
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
        recyclerView = mFloatingView.findViewById(R.id.recyclerView);


        seekBar = mFloatingView.findViewById(R.id.seekBar);
        seekBar.getProgressDrawable().setColorFilter(seekBarColor, PorterDuff.Mode.SRC_IN);
        seekBar.getThumb().setColorFilter(seekBarColor, PorterDuff.Mode.SRC_IN);
        //Title of song being played:
//        songText = mFloatingView.findViewById(R.id.playingSong);

        //Volume Bar instantiation
        volumeBar = mFloatingView.findViewById(R.id.volumeBar);
        //Setting bar and dot(thumb) color
        volumeBar.getProgressDrawable().setColorFilter(seekBarColor, PorterDuff.Mode.SRC_IN);
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

        //Create the List:
        songAdapter = new SongAdapter(this, _songs);
        recyclerView.setAdapter(songAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);
        songAdapter.setOnItemClickListener(new SongAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Context dog,/*final Button b,*/ View view, final SongInfo obj, final int position) {
                musicPlayerSongChange(position);
//                view.setBackgroundColor(Color.MAGENTA);
//                String songName = obj.getArtistname() + " - " + obj.getSongname();
//                songText.setText(songName);
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
                    Log.d("XXX", "DoubleTap Action");
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
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shuffleOn == false) {
                    shuffleButton.setImageResource(R.drawable.button_shuffle);
                    Toast.makeText(mContext, "Shuffle On", Toast.LENGTH_SHORT).show();
                } else {
                    shuffleButton.setImageResource(R.drawable.button_loop);
                    Toast.makeText(mContext, "Shuffle Off", Toast.LENGTH_SHORT).show();
                }
                shuffleOn = !shuffleOn;
            }
        });

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
                                collapsedView.setVisibility(View.GONE);
                                expandedView.setVisibility(VISIBLE);
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:

                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        Log.d("Floating View Service", "x: " + params.x + " y: " + params.x);

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

        setButtons();
        t = new runThread();
        t.start();
        themeSetter();
        ///// Intro Animation
        final Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        in.setDuration(500);
        mFloatingView.setVisibility(VISIBLE);
        mFloatingView.startAnimation(in);
        startForegroundService(); // makes the service a foreground activity so it doesn't get
        // kill by the OS
//        regBroadcast();
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
                    .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
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
        playButton = nextButton = prevButton = null;
        stopSelf();
    }

    //////////////////////////////// For Animation Start

    public void moveChatHead(int start, int finish) {
        AnimatorSet buttonAnimator = new AnimatorSet();
        ValueAnimator buttonAnimatorX = ValueAnimator.ofFloat(start, finish
        );
        buttonAnimatorX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                Log.d("onAnimationUpdateXXX ", "animatedValue: " + animation.getAnimatedValue());

                Log.d("onAnimationUpdateXXX ", "animatedFloat: " + Math.round(Float.valueOf(animation.getAnimatedValue().toString())));

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

    /////////////Set up Music Buttons ///////////////
    public void setButtons() {

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
                if (shuffleOn == true) {
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

                    Log.d("XXX", "\n_songsName: " + _songs.get(currentSong).getSongname() +
                            "Artist Name: " + _songs.get(currentSong).getArtistname());
                    mediaPlayer.setVolume(volumeChange, volumeChange);
                    mediaPlayer.setDataSource(_songs.get(currentSong).getSongUrl());
                    mediaPlayer.prepareAsync();
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {

                            seekBar.setProgress(0);
                            seekBar.setMax(mediaPlayer.getDuration());
                            mp.start();
                            Log.d("Prog", "run: " + mediaPlayer.getDuration());
                        }
                    });
                    playButton.setImageResource(savedPausedDrawableID);

                    //When the current mediafile is finish playing do this:
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            playButton.setImageResource(savedPlayDrawableID);
                            if (currentSong + 1 != _songs.size()) {
                                Log.d("XXX", "Next Song");
                                ++currentSong;
                                if (shuffleOn == true) {
                                    currentSong = randomNumberGenerator.nextInt(_songs.size());
                                }
                                musicPlayerSongChange(currentSong);
                            } else {
                                Log.d("XXX", "Last Song");
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("XXX", e.getMessage());
                    Toast.makeText(FloatingViewService.this, "Error Playing Song", Toast.LENGTH_SHORT).show();
                }
            }
        };
        myHandler.postDelayed(runnable, 100);
    }


    ///////////// Playing music should be handled on a separate thread
    public class runThread extends Thread {

        @Override
        public void run() {
            Log.d("XXX", "thread run");
            while (threading) {
                try {
                    Thread.sleep(1000);
                    if (mediaPlayer != null) {
                        seekBar.post(new Runnable() {
                            @Override
                            public void run() {
                                seekBar.setProgress(mediaPlayer.getCurrentPosition());
//                                Log.d("XXX", "Seek Bar position set");
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
//                    Log.d("RXunwa", "Thread Interruptted");
                    t = null;
                }
//                Log.d("Runwa", "run: " + 1);
            }

        }

    }


    ////////// Load ALL audio from Storage
    private void loadSongs() {
        Toast.makeText(this, "Loading Songs...", Toast.LENGTH_SHORT).show();

        //grab music files from "sdcard":
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        // Grab music from internal storage:
//        Uri uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;

        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    SongInfo s = new SongInfo(name, artist, url);


                    //TODO: figure out why this broke:
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

    /**
     * Detect if the floating view is collapsed or expanded.
     * returns true if the floating view is collapsed.
     */
    private boolean isViewCollapsed() {
        return mParentView == null || mFloatingView.findViewById(R.id.collapse_view).getVisibility() == VISIBLE;
    }


    private class MusicIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        Log.d(TAG, "Headset is unplugged");
                        if (mediaPlayer == null)
                            break;
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            playButton.setImageResource(savedPlayDrawableID);
                        }

                        break;
                    case 1:
                        Log.d(TAG, "Headset is plugged");
                        if (mediaPlayer == null)
                            break;
                        if (!mediaPlayer.isPlaying()) {
                            mediaPlayer.start();
                            playButton.setImageResource(savedPausedDrawableID);
                        }
                        break;
                    default:
                        Log.d(TAG, "I have no idea what the headset state is");
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("XXX", "onDestroy Called");
        unregisterReceiver(myReceiver);
        threading = false; //stop the thread
        if (mParentView != null) mWindowManager.removeView(mParentView);
    }
}
