package rs.ucimokrozigru.kamioni;

import android.app.Activity;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.Locale;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {
    private TextToSpeech tts;
    private ToneGenerator tones;
    private LearningView learningView;
    private boolean ttsReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideSystemUi();
        tones = new ToneGenerator(AudioManager.STREAM_MUSIC, 75);
        tts = new TextToSpeech(this, this);
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

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(new Locale("sr", "RS"));
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.setLanguage(new Locale("hr", "HR"));
            }
            tts.setSpeechRate(0.86f);
            tts.setPitch(1.06f);
            ttsReady = true;
            if (learningView != null) learningView.onVoiceReady();
        }
    }

    public void speak(String text) {
        if (!ttsReady || !learningView.isSoundEnabled()) return;
        tts.stop();
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "kamioni-glas");
    }

    public void successSound() {
        if (learningView.isSoundEnabled()) {
            tones.startTone(ToneGenerator.TONE_PROP_ACK, 180);
        }
    }

    public void wrongSound() {
        if (learningView.isSoundEnabled()) {
            tones.startTone(ToneGenerator.TONE_PROP_NACK, 170);
        }
    }

    @Override
    public void onBackPressed() {
        if (learningView.goHome()) return;
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (tones != null) tones.release();
        super.onDestroy();
    }
}
