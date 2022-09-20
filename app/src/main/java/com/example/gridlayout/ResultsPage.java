package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ResultsPage extends AppCompatActivity {

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results_page);

        button = (Button) findViewById(R.id.playAgain);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResultsPage.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Intent intent = getIntent();
        int clock = intent.getIntExtra("clock", 0);
        int result = intent.getIntExtra("win", 0);
        String message = "";

        TextView tv = (TextView) findViewById(R.id.noOfSeconds);
        message = "Used " + String.valueOf(clock) + " seconds.";
        System.out.println(message);
        tv.setText(message);

        if (result == 0) {
            tv = (TextView) findViewById(R.id.finalMessage);
            message = "Try Again!";
            tv.setText(message);
            System.out.println(message);

            tv = (TextView) findViewById(R.id.finalResult);
            message = "You Lost.";
            tv.setText(message);
            System.out.println(message);
        }
    }

//    public void openActivityMain() {
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
//    }
}