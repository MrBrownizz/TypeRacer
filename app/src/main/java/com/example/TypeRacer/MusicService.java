package com.example.TypeRacer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;


public class MusicService extends Service {

    private MediaPlayer player;

    public int onStartCommand(Intent intent, int flags, int startId) {
        if(player == null){
            player = MediaPlayer.create( this, R.raw.minecraft_theme_music);
        }
        player.start();
        return START_STICKY;
    }
    @Override

    public void onDestroy() {
        super.onDestroy();
        player.pause();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
