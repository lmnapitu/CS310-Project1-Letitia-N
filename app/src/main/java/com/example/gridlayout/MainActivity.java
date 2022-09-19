package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int COLUMN_COUNT = 8;
    private static final int ROW_COUNT = 10;
    public static final int BOMB_COUNT = 4;

    private int flagcount = 4;
    private int clock = 0;
    private boolean running = false;
    private Set<Integer> bombs = new HashSet();

    // save the TextViews of all cells in an array, so later on,
    // when a TextView is clicked, we know which cell it is
    private ArrayList<TextView> cell_tvs;

    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cell_tvs = new ArrayList<TextView>();

        // Method (2): add four dynamically created cells
        GridLayout grid = (GridLayout) findViewById(R.id.gridLayout01);
        for (int i = 0; i<=9; i++) {
            for (int j=0; j<=7; j++) {
                TextView tv = new TextView(this);
                tv.setHeight( dpToPixel(32) );
                tv.setWidth( dpToPixel(32) );
                tv.setTextSize( 20 );//dpToPixel(32) );
                tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                tv.setTextColor(Color.GRAY);
                tv.setBackgroundColor(Color.GRAY);
                tv.setOnClickListener(this::onClickTV);

                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.setMargins(dpToPixel(2), dpToPixel(2), dpToPixel(2), dpToPixel(2));
                lp.rowSpec = GridLayout.spec(i);
                lp.columnSpec = GridLayout.spec(j);

                grid.addView(tv, lp);

                cell_tvs.add(tv);
            }
        }
        createBombs();
        ToggleButton myToggleButton = (ToggleButton) findViewById(R.id.mymode); // initiate a toggle button
        myToggleButton.setChecked(true);

        //timer stuff
        running = true;
        runTimer();
    }

    private int findIndexOfCellTextView(TextView tv) {
        for (int n=0; n<cell_tvs.size(); n++) {
            if (cell_tvs.get(n) == tv)
                return n;
        }
        return -1;
    }

    public void onClickTV(View view){
        TextView tv = (TextView) view;
        int n = findIndexOfCellTextView(tv);

//        TextView mode = (TextView) findViewById(R.id.mymode);

        //testers
//        int count = adjacentBombs(n);
//        tv.setTextColor(Color.GRAY);
//        tv.setBackgroundColor(Color.LTGRAY);
//        if (bombs.contains(n)) {
//            tv.setText(R.string.mine);
//        } else {
//            tv.setText(String.valueOf(count));
//        }

//        ToggleButton toggle = (ToggleButton) findViewById(R.id.mymode);
//        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    // The toggle is enabled
//                } else {
//                    // The toggle is disabled
//                }
//            }
//        });

        ToggleButton toggle = (ToggleButton) findViewById(R.id.mymode); // initiate a toggle button
        Boolean tstate = toggle.isChecked();

        //actual
//        System.out.println(mode.getText());
//        if (mode.getText().toString().equals(R.string.pick)) {
//        boolean checkmode = mode.isEnabled();

        System.out.println(tstate);
        if (tstate == true) {
            System.out.println("pick");
            tv.setTextColor(Color.GRAY);
            tv.setBackgroundColor(Color.LTGRAY);
            if (bombs.contains(n)) {
                tv.setText(R.string.mine); //shows bomb
                running = false; //stops timer
            } else {
                // find adjacent
                int count = adjacentBombs(n);
                tv.setText(String.valueOf(count));
            }
        } else { // if on flag mode
            System.out.println("flaggg");
            if (tv.getCurrentTextColor() == Color.GRAY) {
                tv.setTextColor(Color.GREEN);
                tv.setBackgroundColor(Color.parseColor("lime"));

                tv.setText(R.string.flag);
                TextView timeView = (TextView) findViewById(R.id.flagcount);
                int count = new Integer(timeView.getText().toString());
                count--;
                timeView.setText(String.valueOf(count));
                System.out.println("new flaggggg");

//                if (tv.getText().toString().equals(getResources().getString(R.string.flag))){
////                    if (tv.getText().toString().equals(getResources().getString(R.string.flag))) {
//                    tv.setText("");
//                    TextView timeView = (TextView) findViewById(R.id.flagcount);
//                    int count = new Integer(timeView.getText().toString());
//                    count++;
//                    timeView.setText(String.valueOf(count));
//                    System.out.println("removeflaggg");
//                } else {
//                    tv.setText(R.string.flag);
//                    TextView timeView = (TextView) findViewById(R.id.flagcount);
//                    int count = new Integer(timeView.getText().toString());
//                    count--;
//                    timeView.setText(String.valueOf(count));
//                    System.out.println("new flaggggg");
//                }

//                else if (tv.getText().equals(getString(R.string.flag))){
////                    if (tv.getText().toString().equals(getResources().getString(R.string.flag))) {
//                    tv.setText("");
//                    TextView timeView = (TextView) findViewById(R.id.flagcount);
//                    int count = new Integer(timeView.getText().toString());
//                    count++;
//                    timeView.setText(String.valueOf(count));
//                    System.out.println("removeflaggg");
//                }
//
//                if (!tv.getText().equals("")) {
//                    tv.setText("");
//                    TextView timeView = (TextView) findViewById(R.id.flagcount);
//                    int count = new Integer(timeView.getText().toString());
//                    count++;
//                    timeView.setText(String.valueOf(count));
//                    System.out.println("removeflaggg");
//                }
            } else if (tv.getCurrentTextColor() == Color.GREEN) {
                tv.setTextColor(Color.GRAY);
                tv.setBackgroundColor(Color.GRAY);
                tv.setText("");
                TextView timeView = (TextView) findViewById(R.id.flagcount);
                int count = new Integer(timeView.getText().toString());
                count++;
                timeView.setText(String.valueOf(count));
                System.out.println("removeflaggg");
            }
        }
//
//        if (mode.getText().equals(R.string.flag)) { // how to check if pick/flag mode
//            if (tv.getCurrentTextColor() == Color.GRAY) {
//                tv.setTextColor(Color.GREEN);
//                tv.setBackgroundColor(Color.parseColor("lime"));
//
//                if (tv.getText().equals(null)) {
//                    tv.setText(R.string.flag);
//                    flagcount--;
//                } else if (tv.getText().equals(R.string.flag)) {
//                    tv.setText(null);
//                    flagcount++;
//                }
//            }
//        }






//        if (bombs.contains(n)) {
//            tv.setText(R.string.mine); //shows bomb
//        } else {
//            // find adjacent
//            int count = adjacentBombs(n);
//            tv.setText(count);
//        }

//        if (tv.getCurrentTextColor() == Color.GRAY) {
//            tv.setTextColor(Color.GREEN);
//            tv.setBackgroundColor(Color.parseColor("lime"));
//        }else {
//            tv.setTextColor(Color.GRAY);
//            tv.setBackgroundColor(Color.LTGRAY);
//        }
    }

    private int adjacentBombs (int index) {
        int source = index;
        int count = 0;

        if (bombs.contains(source - 1)) count++; //left of source
        if (bombs.contains(source + 1)) count++; //right of source
        if (bombs.contains(source + 8)) count++; //below of source
        if (bombs.contains(source + 7)) count++; //belowleft of source
        if (bombs.contains(source + 9)) count++; //belowright of source
        if (bombs.contains(source - 8)) count++; //top of source
        if (bombs.contains(source - 9)) count++; //topleft of source
        if (bombs.contains(source - 7)) count++; //topright of source
        return count;
    }

    // timer stuff
    private void runTimer() {
        final TextView timeView = (TextView) findViewById(R.id.timer);
        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                int seconds = clock;
                String time = String.valueOf(seconds);
                timeView.setText(time);

                if (running) {
                    clock++;
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    // bomb stuff
    public void createBombs() {
        int currBombs = 0;
        while (currBombs < BOMB_COUNT) {

            int i = new Random().nextInt(79-0+1)+0;
            if (!bombs.contains(i)) {
                bombs.add(i);
                System.out.println(i + " " + currBombs);
                currBombs++;
            }
        }
    }




}