package com.example.TypeRacer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements TextWatcher {

    TextView tv_text, tv_wpm, tv_countdown;
    String generatedText, userText;
    SpannableString ss;
    ForegroundColorSpan fcsGreen;
    BackgroundColorSpan bcsRed;
    EditText et_input;
    Intent intent;
    int tot_index, curr_index, lastCorrect, wpm, randomLine;
    double startTime, endTime;
    boolean isCorrect, firstTime;
    BufferedReader reader;
    Random rnd;
    final int secInMinute = 60;
    final int lettersInWord = 5;
    InputMethodManager manager;
    ActionBar bar;
    BroadcastBattery bb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bar = getSupportActionBar();
        assert bar != null;
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#404040")));

        et_input = findViewById(R.id.input);
        et_input.addTextChangedListener(this);

        tv_text = findViewById(R.id.text);
        tv_wpm = findViewById(R.id.wpm);
        tv_countdown = findViewById(R.id.countdown);

        rnd = new Random();
        randomLine = rnd.nextInt(36);

        try {
            chooseText();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ss = new SpannableString(generatedText);

        bcsRed = new BackgroundColorSpan(Color.RED);
        fcsGreen = new ForegroundColorSpan(Color.GREEN);

        tot_index = 0;
        curr_index = 0;
        lastCorrect = -1;

        startTime = 0;
        endTime = 0;

        isCorrect = true;
        firstTime = true;

        manager = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE
        );

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        countDown();

        bb = new BroadcastBattery();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.item1:
                openDialog();
                return true;
            case R.id.item2:
                restartGame();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void restartGame() {

        hideKeyboard();
        getIntent().putExtra("sameText", generatedText);
        finish();
        startActivity(getIntent());
    }

    public void openDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Are you sure you want to quit?")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    Intent intent = new Intent(MainActivity.this, HomePage.class);
                    startActivity(intent);
                }).setNegativeButton("No", (dialogInterface, i) -> {

                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void chooseText() throws IOException {

        if(getIntent().getExtras() == null) {

            List<String> listOfStrings = new ArrayList<String>();
            final InputStream file = getAssets().open("paragraph.txt");
            reader = new BufferedReader(new InputStreamReader(file));
            String line = reader.readLine();

            while (line != null) {
                listOfStrings.add(line);
                line = reader.readLine();
            }

            String[] array = listOfStrings.toArray(new String[0]);

            tv_text.setText(array[randomLine]);
        }
        else {
            tv_text.setText(getIntent().getExtras().getString("sameText"));
        }
        generatedText = tv_text.getText().toString();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if(view != null)
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void showKeyboard(EditText et_input) {
        manager.showSoftInput(et_input.getRootView()
                ,InputMethodManager.SHOW_IMPLICIT);
        et_input.setEnabled(true);
        et_input.setPrivateImeOptions("nm");
        et_input.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            et_input.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        }
        et_input.requestFocus();
    }

    public void changeGreen(int index, int length){
        ss.setSpan(fcsGreen, index, index + length + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_text.setText(ss);
    }

    public void changeRed(int index, int length){
        ss.setSpan(bcsRed, index, index + length + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_text.setText(ss);
    }

    public int getCurWordLength(String generatedText, int total_index){
        int count = 0;
        while(generatedText.charAt(total_index) != ' ') {
            count++;
            if(total_index == generatedText.length() - 1)
                return count;
            total_index++;
        }
        return count;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    public void calculateWPM(int correctLetters)
    {
        endTime = LocalTime.now().toNanoOfDay();
        double elapsedTime = (endTime - startTime) / 1000000000.0; //convert nano-seconds to seconds
        //WPM formula: (characters / 5) / Time(min) = WPM
        wpm = (int)((((double)(correctLetters + 1) / lettersInWord) / elapsedTime) * secInMinute);
        tv_wpm.setText("WPM: " + wpm);
    }

    public void countDown() {
        tv_countdown.setText("3");
        new Handler().postDelayed(() -> {
            tv_countdown.setText("2");
            new Handler().postDelayed(() -> {
                tv_countdown.setText("1");
                new Handler().postDelayed(() -> {
                    tv_countdown.setText("");
                    showKeyboard(et_input); //display keyboard
                    et_input.setHint("");
                    startTime = LocalTime.now().toNanoOfDay(); //start timer
                }, 1000);
            }, 1000);
        }, 1100);
    }

    @Override
    public void afterTextChanged(Editable editable) {
        userText = editable.toString();

        curr_index = editable.length() - 1;

        if(editable.length() == 0 && !firstTime) //if box is empty (apart from erasing box)
        {
            isCorrect = true;
            lastCorrect = -1;
            changeGreen(0, tot_index - 1); //clear green and keep previous green words
            changeRed(tot_index, -1);            //clear red
        }
        if(curr_index >= 0) //if box isn't empty
        {
            firstTime = false;
            if((userText.toLowerCase().charAt(curr_index) == generatedText.charAt(tot_index + curr_index) || userText.toUpperCase().charAt(curr_index) == generatedText.charAt(tot_index + curr_index)) && isCorrect) //if letter is correct
            {
                lastCorrect = curr_index;
                changeGreen(0, tot_index + lastCorrect);
            }
            else
            {
                isCorrect = false;
                changeRed(tot_index + lastCorrect + 1, curr_index - lastCorrect - 1);
                if(curr_index == lastCorrect)
                    isCorrect = true;
            }
        }
        if(curr_index == getCurWordLength(generatedText, tot_index) && isCorrect) //if finished word correctly
        {
            firstTime = true;
            tot_index += curr_index + 1;
            curr_index = 0;
            lastCorrect = -1;
            changeGreen(0, tot_index - 1); //clear green and keep previous green words
            changeRed(tot_index, -1);            //clear red
            editable.clear();
        }
        if(isCorrect)
            calculateWPM(tot_index + curr_index);

        gameOver();
    }

    public void gameOver(){
        if(tot_index + lastCorrect == generatedText.length() - 1) {
            intent = new Intent(this, ResultActivity.class);
            intent.putExtra("wpm", String.valueOf(wpm));
            intent.putExtra("generatedText", tv_text.getText().toString());
            startActivity(intent);
        }
    }

    private class BroadcastBattery extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int battery = intent.getIntExtra("level", 0);
            Toast.makeText(context, "battery is " + battery + "%", Toast.LENGTH_SHORT).show();
        }

    }
    protected void onResume(){
        super.onResume();
        registerReceiver(bb, new IntentFilter(intent.ACTION_BATTERY_CHANGED));
    }
    protected void onPause(){
        super.onPause();
        unregisterReceiver(bb);
    }


}