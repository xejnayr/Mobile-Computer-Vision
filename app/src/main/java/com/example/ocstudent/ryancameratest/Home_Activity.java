package com.example.ocstudent.ryancameratest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Home_Activity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(this);
        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(this);
        Button button3 = findViewById(R.id.button3);
        button3.setOnClickListener(this);
        Button button4 = findViewById(R.id.button4);
        button4.setOnClickListener(this);
        Button button5 = findViewById(R.id.button5);
        button5.setOnClickListener(this);
        Button button6 = findViewById(R.id.button6);
        button6.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.button6:
                MainActivity.effectNumber = 0;
                break;
            case R.id.button:
                MainActivity.effectNumber = 1;
                break;
            case R.id.button2:
                MainActivity.effectNumber = 2;
                break;
            case R.id.button3:
                MainActivity.effectNumber = 3;
                break;
            case R.id.button4:
                MainActivity.effectNumber = 4;
                break;
            case R.id.button5:
                MainActivity.effectNumber = 5;
                break;
        }
        callEffect(MainActivity.effectNumber);
    }



    public void callEffect(int val) {
        Intent CAM = new Intent(this, MainActivity.class);
        Bundle extras = new Bundle();
        extras.putInt("CAMERA_EFFECT", val);

        CAM.putExtras(extras);
        startActivity(CAM);
    }
}
