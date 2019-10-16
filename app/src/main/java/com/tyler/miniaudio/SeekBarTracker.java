package com.tyler.miniaudio;


public class SeekBarTracker extends Thread {
    @Override
    public void run() {
//            Log.d("XXX", "thread run");
        while (MusicPlayer.isMusicPlaying) {
            try {
                Thread.sleep(500);
                if (FloatingViewService.serviceAlive && MusicPlayer.isMediaPlayerAlive()) {
                    FloatingViewService.getInstance().seekBar.post(new Runnable() {
                        @Override
                        public void run() {
                            if(FloatingViewService.serviceAlive) {
                                FloatingViewService.getInstance().seekBar.
                                        setProgress(MusicPlayer.getProgress());
                            }
//                                Log.d("XXX", "Seek Bar position set");
                        }
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
//                    Log.d("RXunwa", "Thread Interruptted");
//                MusicPlayer.seekBarProgression = null;
            }
        }

    }

}
