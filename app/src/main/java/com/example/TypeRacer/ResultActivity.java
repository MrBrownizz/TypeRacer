package com.example.TypeRacer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    TextView tv_wpm, tv_generatedText;
    Button btn_menu, btn_playAgain;
    Bundle extras;
    String wpm, generatedText;
    Intent menu, restart;
    TextToSpeech t1;
    BroadcastReceiver br;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tv_generatedText = findViewById(R.id.tv_generatedText);
        tv_wpm = findViewById(R.id.tv_wpm);
        btn_menu = findViewById(R.id.btn_menu);
        btn_playAgain = findViewById(R.id.btn_restart);

        menu = new Intent(this, HomePage.class);
        restart = new Intent(this, MainActivity.class);

        screen();

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (Intent.ACTION_HEADSET_PLUG.equals(action))
                    Toast.makeText(context, "Headphones Detected", Toast.LENGTH_SHORT).show();
            }
        };
    }

    public void screen(){

        extras = getIntent().getExtras();

        if(extras != null) {
            wpm = extras.getString("wpm");
            generatedText = extras.getString("generatedText");
        }

        tv_generatedText.setText(generatedText);

        SharedPreferences maxWPM = getSharedPreferences("GAME_DATA", Context.MODE_PRIVATE);
        int highWPM = maxWPM.getInt("HIGH_WPM", 0);
        if(Integer.parseInt(wpm) > highWPM)
        {
            SharedPreferences.Editor editor = maxWPM.edit();
            editor.putInt("HIGH_WPM", Integer.parseInt(wpm));
            editor.commit();
            tv_wpm.setText("NEW RECORD! " + wpm +" WPM");
        }
        else {
            tv_wpm.setText("Your WPM is " + wpm);
        }
    }

    public void speakText(View view) {
        t1 = new TextToSpeech(getApplicationContext(), status -> {
            t1.setLanguage(Locale.UK);
            t1.speak(generatedText, TextToSpeech.QUEUE_FLUSH, null, null);
        });
    }

    public void restart(View view){
        startActivity(restart);
    }

    public void menu(View view){
        startActivity(menu);
    }

}