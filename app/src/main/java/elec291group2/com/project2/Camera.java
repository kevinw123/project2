package elec291group2.com.project2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.Snackbar;
import android.view.View;
import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;
import android.app.Activity;
import android.widget.Toast;
import android.text.TextUtils;
import android.view.View;
import android.view.Menu;

public class Camera extends AppCompatActivity
{
    // Declare variables
    private static final String TAG = "Camera";
    private String path;
    private VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (!LibsChecker.checkVitamioLibs(this))
            return;
        //Set view as the camera.xml layout
        setContentView(R.layout.camera);
        //Set title for this Activity
        getSupportActionBar().setTitle("Camera");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        /*Using the ID for Vitamio's VideoView in camera.xml, creating object
         *to play video
        */
        mVideoView = (VideoView) findViewById(R.id.surface_view);
        //Setting path to play video
        path = "rtmp://192.168.1.40:1935/live/myStream";


        //Set the video path to play video
        mVideoView.setVideoPath(path);
        //Creating a MediaController to adjust video if necessary
        mVideoView.setMediaController(new MediaController(this));
        mVideoView.requestFocus();

        // Listener to play video.
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                //Set the playback speed for video
                mediaPlayer.setPlaybackSpeed(1.0f);
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId())
        {
            //Pressing back arrow on app returns to main menu screen.
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed()
    {
        //For the tab to slide into the activity.
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}
