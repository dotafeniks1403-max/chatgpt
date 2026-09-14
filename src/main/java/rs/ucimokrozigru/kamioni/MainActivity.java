package rs.ucimokrozigru.kamioni;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {
    private MediaPlayer player;
    private ToneGenerator tones;
    private LearningView learningView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideSystemUi();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        tones = new ToneGenerator(AudioManager.STREAM_MUSIC, 70);
        learningView = new LearningView(this);
        setContentView(learningView);
    }

    private void hideSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUi();
    }

    public void playVoice(int resourceId) {
        if (learningView == null || !learningView.isSoundEnabled()) return;
        stopVoice();
        player = MediaPlayer.create(this, resourceId);
        if (player == null) return;
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.release();
                if (player == mediaPlayer) player = null;
            }
        });
        player.start();
    }

    public void stopVoice() {
        if (player != null) {
            try { if (player.isPlaying()) player.stop(); } catch (IllegalStateException ignored) { }
            player.release();
            player = null;
        }
    }

    public void successSound() {
        if (learningView != null && learningView.isSoundEnabled()) {
            tones.startTone(ToneGenerator.TONE_PROP_ACK, 190);
        }
    }

    public void wrongSound() {
        if (learningView != null && learningView.isSoundEnabled()) {
            tones.startTone(ToneGenerator.TONE_PROP_NACK, 170);
        }
    }

    @Override
    public void onBackPressed() {
        if (learningView != null && learningView.goHome()) return;
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        stopVoice();
        if (tones != null) tones.release();
        super.onDestroy();
    }
}
