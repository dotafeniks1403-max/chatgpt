package rs.ucimokrozigru.body;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
public class MainActivity extends Activity {
 private WebView web;
 public void onCreate(Bundle b){super.onCreate(b);setVolumeControlStream(android.media.AudioManager.STREAM_MUSIC);web=new WebView(this);web.setBackgroundColor(0xfffff9ee);web.getSettings().setJavaScriptEnabled(true);web.getSettings().setMediaPlaybackRequiresUserGesture(false);web.getSettings().setAllowFileAccess(true);web.getSettings().setAllowContentAccess(false);web.getSettings().setAllowFileAccessFromFileURLs(false);web.getSettings().setAllowUniversalAccessFromFileURLs(false);setContentView(web);web.loadUrl("file:///android_asset/index.html");}
 protected void onPause(){web.evaluateJavascript("stop()",null);web.onPause();super.onPause();}protected void onResume(){super.onResume();if(web!=null)web.onResume();}protected void onDestroy(){web.destroy();super.onDestroy();}
}
