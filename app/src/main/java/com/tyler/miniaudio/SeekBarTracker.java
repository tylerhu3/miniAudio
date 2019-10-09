package com.tyler.miniaudio;


public class SeekBarTracker extends Thread {
    @Override
    public void run() {
//            Log.d("XXX", "thread run");
        while (FloatingViewService.getInstance().isMusicPlaying) {
            try {
                Thread.sleep(500);
                if (FloatingViewService.getInstance().mediaPlayer != null) {
                    FloatingViewService.getInstance().seekBar.post(new Runnable() {
                        @Override
                        public void run() {
                            FloatingViewService.getInstance().seekBar.
                                    setProgress(FloatingViewService.getInstance().mediaPlayer.getCurrentPosition());
//                                Log.d("XXX", "Seek Bar position set");
                        }
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
//                    Log.d("RXunwa", "Thread Interruptted");
                FloatingViewService.getInstance().seekBarProgression = null;
            }
//                Log.d("Runwa", "run: " + 1);
        }

    }

}
