package nl.rmokveld.castnotifications.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;

import java.util.Random;

import nl.rmokveld.castnotifications.CastNotificationManager;
import nl.rmokveld.castnotifications.sample.gcm.RegisterIntentService;

public class MainActivity extends AppCompatActivity {

    private VideoCastManager mInstance;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ((TextView) findViewById(R.id.token)).setText(intent.getStringExtra("token"));
            }
        }, new IntentFilter("gcm_token"));

        findViewById(R.id.add_notification).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaInfo mediaInfo = getSampleMediaInfo();
                CastNotificationManager.getInstance().notify(new Random().nextInt(4), "test", "test", mediaInfo);
            }
        });

        startService(new Intent(this, RegisterIntentService.class));
        mInstance = VideoCastManager.getInstance();
    }

    public static MediaInfo getSampleMediaInfo() {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, "test");
        return new MediaInfo.Builder("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")
                .setContentType("video/mp4")
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_activity, menu);

        mInstance.addMediaRouterButton(menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mInstance.incrementUiCounter();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mInstance.decrementUiCounter();
    }
}
