package com.example.TypeRacer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import android.os.Handler;

public class HomePage extends AppCompatActivity {

    Intent intent;
    Button btn_single;
    ImageButton btn_music;
    BroadcastReceiver br;
    LottieAnimationView lottieAnimationView;
    Animation scaleAnim;
    int musicIndicator; // 0 = music is on, 1 = music is off

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        lottieAnimationView = findViewById(R.id.animation);

        intent = new Intent(this, MainActivity.class);

        btn_single = findViewById(R.id.singlePlayer);
        btn_music = findViewById(R.id.btn_music);

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (Intent.ACTION_HEADSET_PLUG.equals(action))
                    Toast.makeText(context, "Headphones plugged in", Toast.LENGTH_SHORT).show();
            }
        };

        lottieAnimationView.playAnimation();
        lottieAnimationView.setRepeatCount(LottieDrawable.INFINITE);

        scaleAnim = AnimationUtils.loadAnimation(this, R.anim.anim1);

        musicIndicator = 0;

        startService(new Intent( this, MusicService.class ) );
    }

    public void StartGame(View view){
        btn_single.startAnimation(scaleAnim);
        new Handler().postDelayed(() -> startActivity(intent), 200);
    }

    public void changeMusic(View view){
        if(musicIndicator == 0) {
            btn_music.setImageResource(R.drawable.ic_baseline_music_off_24); //change icon to music off
            musicIndicator++;
            stopService(new Intent( this, MusicService.class ) );
        }
        else
        {
            btn_music.setImageResource(R.drawable.ic_baseline_music_note_24); //change icon to music on
            musicIndicator--;
            startService(new Intent( this, MusicService.class ) );
        }
    }
}