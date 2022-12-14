package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.Intent;
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private static final int NODES = 80;
    public static final int BOMB_COUNT = 4;

    private int flagcount = 4;
    private int clock = 0;
    private int result = 0;
    private boolean running = false;
    private Set<Integer> bombs = new HashSet();
    private Set<Integer> revealed = new HashSet();
//    private Queue<Integer> queue = new LinkedList<Integer>();
    Stack<Integer> stack = new Stack<Integer>();
    private boolean done = false;

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
                tv.setBackgroundColor(Color.parseColor("lime"));
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

        if (done == true) {
            Intent intent = new Intent(MainActivity.this, ResultsPage.class);
            intent.putExtra("clock", clock);
            intent.putExtra("win", result);
            startActivity(intent);
        }

        TextView tv = (TextView) view;
        int n = findIndexOfCellTextView(tv);

        ToggleButton toggle = (ToggleButton) findViewById(R.id.mymode); // initiate a toggle button
        Boolean tstate = toggle.isChecked();

//        System.out.println(tstate);
        if (tstate == true) { // on pick mode
            System.out.println("pick");

                tv.setTextColor(Color.GRAY);
                tv.setBackgroundColor(Color.LTGRAY);
                if (bombs.contains(n)) {
                    tv.setText(R.string.mine); //shows bomb
                    running = false; //stops timer
                    done = true;
                    result = 0; // zero is lose

                    //reveal bombs
                    for (int i=0; i<cell_tvs.size(); i++) {
                        if (bombs.contains(i)) {
                            cell_tvs.get(i).setText(R.string.mine);
                        }
                    }
                } else {
                    // find adjacent
                    int count = adjacentBombs(n);
                    if (count == 0) {
                        DFS(n);
                        cell_tvs.get(n).setTextColor(Color.GRAY);
                        cell_tvs.get(n).setBackgroundColor(Color.LTGRAY);
                        tv.setText("");
                        revealed.add(n);
                    } else {
                        tv.setText(String.valueOf(count));
                        cell_tvs.get(n).setTextColor(Color.GRAY);
                        cell_tvs.get(n).setBackgroundColor(Color.LTGRAY);
                        revealed.add(n);
                    }
                    revealed.add(n);

                    if (revealed.size() == 76) { // how to win
                        running = false;
                        result = 1; // zero is lose
                        done = true;

                        //reveal bombs
                        for (int i=0; i<cell_tvs.size(); i++) {
                            if (bombs.contains(i)) {
                                cell_tvs.get(i).setText(R.string.mine);
                            }
                        }
                    }
                }

        } else { // if on flag mode
//            System.out.println("flaggg");
            if (!revealed.contains(n)) {
                if (tv.getCurrentTextColor() == Color.GRAY) {
                    tv.setTextColor(Color.GREEN);
                    tv.setText(R.string.flag);

                    TextView timeView = (TextView) findViewById(R.id.flagcount);
                    int count = new Integer(timeView.getText().toString());
                    count--;
                    timeView.setText(String.valueOf(count));

                } else if (tv.getCurrentTextColor() == Color.GREEN) {
                    tv.setTextColor(Color.GRAY);
                    tv.setText("");

                    TextView timeView = (TextView) findViewById(R.id.flagcount);
                    int count = new Integer(timeView.getText().toString());
                    count++;
                    timeView.setText(String.valueOf(count));
                }
            }
        }
    }

    private int adjacentBombs (int index) {
        int source = index;
        int count = 0;

        if (index == 0) { // top left corner
            if (bombs.contains(source + 1)) count++; //right of source
            if (bombs.contains(source + 8)) count++; //below of source
            if (bombs.contains(source + 9)) count++; //belowright of source
            return count;
        } else if (index == 7) { // top right corner
            if (bombs.contains(source - 1)) count++; //left of source
            if (bombs.contains(source + 8)) count++; //below of source
            if (bombs.contains(source + 7)) count++; //belowleft of source
            return count;
        } else if (index == 72) { // bottom left corner
            if (bombs.contains(source - 8)) count++; //top of source
            if (bombs.contains(source - 7)) count++; //topright of source
            if (bombs.contains(source + 1)) count++; //right of source
            return count;
        } else if (index == 79) { // bottom right corner
            if (bombs.contains(source - 8)) count++; //top of source
            if (bombs.contains(source - 9)) count++; //topleft of source
            if (bombs.contains(source - 1)) count++; //left of source
            return count;
        } else if (index < 7) { // top row
            if (bombs.contains(source - 1)) count++; //left of source
            if (bombs.contains(source + 1)) count++; //right of source
            if (bombs.contains(source + 8)) count++; //below of source
            if (bombs.contains(source + 7)) count++; //belowleft of source
            if (bombs.contains(source + 9)) count++; //belowright of source
            return count;
        } else if (index > 72) { // bottom row
            if (bombs.contains(source - 1)) count++; //left of source
            if (bombs.contains(source + 1)) count++; //right of source
            if (bombs.contains(source - 8)) count++; //top of source
            if (bombs.contains(source - 9)) count++; //topleft of source
            if (bombs.contains(source - 7)) count++; //topright of source
            return count;
        } else if (index % 8 == 0) { // on the left
            if (bombs.contains(source + 1)) count++; //right of source
            if (bombs.contains(source + 8)) count++; //below of source
            if (bombs.contains(source + 9)) count++; //belowright of source
            if (bombs.contains(source - 8)) count++; //top of source
            if (bombs.contains(source - 7)) count++; //topright of source
            return count;
        } else if (index % 8 == 7) { // on the right
            if (bombs.contains(source - 1)) count++; //left of source
            if (bombs.contains(source + 8)) count++; //below of source
            if (bombs.contains(source + 7)) count++; //belowleft of source
            if (bombs.contains(source - 8)) count++; //top of source
            if (bombs.contains(source - 9)) count++; //topleft of source
            return count;
        } else {
            if (bombs.contains(source - 1)) count++; //left of source
            if (bombs.contains(source + 1)) count++; //right of source
            if (bombs.contains(source + 8)) count++; //below of source
            if (bombs.contains(source + 7)) count++; //belowleft of source
            if (bombs.contains(source + 9)) count++; //belowright of source
            if (bombs.contains(source - 8)) count++; //top of source
            if (bombs.contains(source - 9)) count++; //topleft of source
            if (bombs.contains(source - 7)) count++; //topright of source
        }

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

            int i = new Random().nextInt(79+1);
            if (!bombs.contains(i)) {
                bombs.add(i);
                System.out.println(i + " " + currBombs);
                currBombs++;
            }
        }
    }

    private void changesetting (int n) {
        cell_tvs.get(n).setTextColor(Color.GRAY);
        cell_tvs.get(n).setBackgroundColor(Color.LTGRAY);
        revealed.add(n);
    }

    public void DFS (int index) {
        stack.push(index);
        int store = 0;

        while (!stack.empty()) {
            index = stack.pop();

            System.out.println("curr index: " + index);
            System.out.println("mystack: " + stack);


            if (index == 0) { // top left corner
                //right of source
                store = adjacentBombs(index + 1);
                if (store == 0) {
                    if (!revealed.contains(index+1)) {
                        stack.push(index + 1);
                        cell_tvs.get(index + 1).setText("");
                        changesetting(index + 1);
                    }
                } else {
                    if (!revealed.contains(index+1)) {
                        cell_tvs.get(index + 1).setText(String.valueOf(store));
                        changesetting(index + 1);
                    }
                }
                //below of source
                store = adjacentBombs(index + 8);
                if (store == 0) {
                    if (!revealed.contains(index+8)) {
                        stack.push(index + 8);
                        cell_tvs.get(index + 8).setText("");
                        changesetting(index + 8);
                    }
                } else {
                    if (!revealed.contains(index+8)) {
                        cell_tvs.get(index + 8).setText(String.valueOf(store));
                        changesetting(index + 8);
                    }
                }
                //belowright of source
                store = adjacentBombs(index + 9);
                if (store == 0) {
                    if (!revealed.contains(index+9)) {
                        stack.push(index + 9);
                        cell_tvs.get(index + 9).setText("");
                        changesetting(index + 9);
                    }
                } else {
                    if (!revealed.contains(index+9)) {
                        cell_tvs.get(index + 9).setText(String.valueOf(store));
                        changesetting(index + 9);
                    }
                }
                continue;

            }

            if (index == 7) { // top right corner
                //left of source
                store = adjacentBombs(index - 1);
                if (store == 0) {
                    if (!revealed.contains(index-1)) {
                        stack.push(index - 1);
                        cell_tvs.get(index - 1).setText("");
                        changesetting(index - 1);
                    }
                } else {
                    if (!revealed.contains(index-1)) {
                        cell_tvs.get(index - 1).setText(String.valueOf(store));
                        changesetting(index - 1);
                    }
                }
                //below of source
                store = adjacentBombs(index + 8);
                if (store == 0) {
                    if (!revealed.contains(index+8)) {
                        stack.push(index + 8);
                        cell_tvs.get(index + 8).setText("");
                        changesetting(index + 8);
                    }
                } else {
                    if (!revealed.contains(index+8)) {
                        cell_tvs.get(index + 8).setText(String.valueOf(store));
                        changesetting(index + 8);
                    }
                }
                //belowleft of source
                store = adjacentBombs(index + 7);
                if (store == 0) {
                    if (!revealed.contains(index+7)) {
                        stack.push(index + 7);
                        cell_tvs.get(index + 7).setText("");
                        changesetting(index + 7);
                    }
                } else {
                    if (!revealed.contains(index+7)) {
                        cell_tvs.get(index + 7).setText(String.valueOf(store));
                        changesetting(index + 7);
                    }
                }
                continue;

            }
            else if (index == 72) { // bottom left corner
                //top of source
                store = adjacentBombs(index - 8);
                if (store == 0) {
                    if (!revealed.contains(index-8)) {
                        stack.push(index - 8);
                        cell_tvs.get(index - 8).setText("");
                        changesetting(index - 8);
                    }
                } else {
                    if (!revealed.contains(index-8)) {
                        cell_tvs.get(index - 8).setText(String.valueOf(store));
                        changesetting(index - 8);
                    }
                }
                //topright of source
                store = adjacentBombs(index - 7);
                if (store == 0) {
                    if (!revealed.contains(index-7)) {
                        stack.push(index - 7);
                        cell_tvs.get(index - 7).setText("");
                        changesetting(index - 7);
                    }
                } else {
                    if (!revealed.contains(index-7)) {
                        cell_tvs.get(index - 7).setText(String.valueOf(store));
                        changesetting(index - 7);
                    }
                }
                //right of source
                store = adjacentBombs(index + 1);
                if (store == 0) {
                    if (!revealed.contains(index+1)) {
                        stack.push(index + 1);
                        cell_tvs.get(index + 1).setText("");
                        changesetting(index + 1);
                    }
                } else {
                    if (!revealed.contains(index+1)) {
                        cell_tvs.get(index + 1).setText(String.valueOf(store));
                        changesetting(index + 1);
                    }
                }
                continue;

            }
            else if (index == 79) { // bottom right corner
                //top of source
                store = adjacentBombs(index - 8);
                if (store == 0) {
                    if (!revealed.contains(index-8)) {
                        stack.push(index - 8);
                        cell_tvs.get(index - 8).setText("");
                        changesetting(index - 8);
                    }
                } else {
                    if (!revealed.contains(index-8)) {
                        cell_tvs.get(index - 8).setText(String.valueOf(store));
                        changesetting(index - 8);
                    }
                }
                //topleft of source
                store = adjacentBombs(index - 9);
                if (store == 0) {
                    if (!revealed.contains(index-9)) {
                        stack.push(index - 9);
                        cell_tvs.get(index - 9).setText("");
                        changesetting(index - 9);
                    }
                } else {
                    if (!revealed.contains(index-9)) {
                        cell_tvs.get(index - 9).setText(String.valueOf(store));
                        changesetting(index - 9);
                    }
                }
                //left of source
                store = adjacentBombs(index - 1);
                if (store == 0) {
                    if (!revealed.contains(index-1)) {
                        stack.push(index - 1);
                        cell_tvs.get(index - 1).setText("");
                        changesetting(index - 1);
                    }
                } else {
                    if (!revealed.contains(index-1)) {
                        cell_tvs.get(index - 1).setText(String.valueOf(store));
                        changesetting(index - 1);
                    }
                }
                continue;

            }
            else if (index < 7 && index > 0) { // top row
                //left of source
                store = adjacentBombs(index - 1);
                if (store == 0) {
                    if (!revealed.contains(index-1)) {
                        stack.push(index - 1);
                        cell_tvs.get(index - 1).setText("");
                        changesetting(index - 1);
                    }
                } else {
                    if (!revealed.contains(index-1)) {
                        cell_tvs.get(index - 1).setText(String.valueOf(store));
                        changesetting(index - 1);
                    }
                }
                //right of source
                store = adjacentBombs(index + 1);
                if (store == 0) {
                    if (!revealed.contains(index+1)) {
                        stack.push(index + 1);
                        cell_tvs.get(index + 1).setText("");
                        changesetting(index + 1);
                    }
                } else {
                    if (!revealed.contains(index+1)) {
                        cell_tvs.get(index + 1).setText(String.valueOf(store));
                        changesetting(index + 1);
                    }
                }
                //below of source
                store = adjacentBombs(index + 8);
                if (store == 0) {
                    if (!revealed.contains(index+8)) {
                        stack.push(index + 8);
                        cell_tvs.get(index + 8).setText("");
                        changesetting(index + 8);
                    }
                } else {
                    if (!revealed.contains(index+8)) {
                        cell_tvs.get(index + 8).setText(String.valueOf(store));
                        changesetting(index + 8);
                    }
                }
                //belowleft of source
                store = adjacentBombs(index + 7);
                if (store == 0) {
                    if (!revealed.contains(index+7)) {
                        stack.push(index + 7);
                        cell_tvs.get(index + 7).setText("");
                        changesetting(index + 7);
                    }
                } else {
                    if (!revealed.contains(index+7)) {
                        cell_tvs.get(index + 7).setText(String.valueOf(store));
                        changesetting(index + 7);
                    }
                }
                //belowright of source
                store = adjacentBombs(index + 9);
                if (store == 0) {
                    if (!revealed.contains(index+9)) {
                        stack.push(index + 9);
                        cell_tvs.get(index + 9).setText("");
                        changesetting(index + 9);
                    }
                } else {
                    if (!revealed.contains(index+9)) {
                        cell_tvs.get(index + 9).setText(String.valueOf(store));
                        changesetting(index + 9);
                    }
                }
                continue;

            }
            else if (index > 72 && index < 79) { // bottom row
                //left of source
                store = adjacentBombs(index - 1);
                if (store == 0) {
                    if (!revealed.contains(index-1)) {
                        stack.push(index - 1);
                        cell_tvs.get(index - 1).setText("");
                        changesetting(index - 1);
                    }
                } else {
                    if (!revealed.contains(index-1)) {
                        cell_tvs.get(index - 1).setText(String.valueOf(store));
                        changesetting(index - 1);
                    }
                }
                //right of source
                store = adjacentBombs(index + 1);
                if (store == 0) {
                    if (!revealed.contains(index+1)) {
                        stack.push(index + 1);
                        cell_tvs.get(index + 1).setText("");
                        changesetting(index + 1);
                    }
                } else {
                    if (!revealed.contains(index+1)) {
                        cell_tvs.get(index + 1).setText(String.valueOf(store));
                        changesetting(index + 1);
                    }
                }
                //top of source
                store = adjacentBombs(index - 8);
                if (store == 0) {
                    if (!revealed.contains(index-8)) {
                        stack.push(index - 8);
                        cell_tvs.get(index - 8).setText("");
                        changesetting(index - 8);
                    }
                } else {
                    if (!revealed.contains(index-8)) {
                        cell_tvs.get(index - 8).setText(String.valueOf(store));
                        changesetting(index - 8);
                    }
                }
                //topleft of source
                store = adjacentBombs(index - 9);
                if (store == 0) {
                    if (!revealed.contains(index-9)) {
                        stack.push(index - 9);
                        cell_tvs.get(index - 9).setText("");
                        changesetting(index - 9);
                    }
                } else {
                    if (!revealed.contains(index-9)) {
                        cell_tvs.get(index - 9).setText(String.valueOf(store));
                        changesetting(index - 9);
                    }
                }
                //topright of source
                store = adjacentBombs(index - 7);
                if (store == 0) {
                    if (!revealed.contains(index-7)) {
                        stack.push(index - 7);
                        cell_tvs.get(index - 7).setText("");
                        changesetting(index - 7);
                    }
                } else {
                    if (!revealed.contains(index-7)) {
                        cell_tvs.get(index - 7).setText(String.valueOf(store));
                        changesetting(index - 7);
                    }
                }
                continue;

            }
            else if (index % 8 == 0 && index > 0 && index < 72) { // on the left
                //top of source
                store = adjacentBombs(index - 8);
                if (store == 0) {
                    if (!revealed.contains(index-8)) {
                        stack.push(index - 8);
                        cell_tvs.get(index - 8).setText("");
                        changesetting(index - 8);
                    }
                } else {
                    if (!revealed.contains(index-8)) {
                        cell_tvs.get(index - 8).setText(String.valueOf(store));
                        changesetting(index - 8);
                    }
                }
                //topright of source
                store = adjacentBombs(index - 7);
                if (store == 0) {
                    if (!revealed.contains(index-7)) {
                        stack.push(index - 7);
                        cell_tvs.get(index - 7).setText("");
                        changesetting(index - 7);
                    }
                } else {
                    if (!revealed.contains(index-7)) {
                        cell_tvs.get(index - 7).setText(String.valueOf(store));
                        changesetting(index - 7);
                    }
                }
                //right of source
                store = adjacentBombs(index + 1);
                if (store == 0) {
                    if (!revealed.contains(index+1)) {
                        stack.push(index + 1);
                        cell_tvs.get(index + 1).setText("");
                        changesetting(index + 1);
                    }
                } else {
                    if (!revealed.contains(index+1)) {
                        cell_tvs.get(index + 1).setText(String.valueOf(store));
                        changesetting(index + 1);
                    }
                }
                //belowright of source
                store = adjacentBombs(index + 9);
                if (store == 0) {
                    if (!revealed.contains(index+9)) {
                        stack.push(index + 9);
                        cell_tvs.get(index + 9).setText("");
                        changesetting(index + 9);
                    }
                } else {
                    if (!revealed.contains(index+9)) {
                        cell_tvs.get(index + 9).setText(String.valueOf(store));
                        changesetting(index + 9);
                    }
                }
                //below of source
                store = adjacentBombs(index + 8);
                if (store == 0) {
                    if (!revealed.contains(index+8)) {
                        stack.push(index + 8);
                        cell_tvs.get(index + 8).setText("");
                        changesetting(index + 8);
                    }
                } else {
                    if (!revealed.contains(index+8)) {
                        cell_tvs.get(index + 8).setText(String.valueOf(store));
                        changesetting(index + 8);
                    }
                }
                continue;

            }
            else if (index % 8 == 7 && index > 7 && index < 79) { // on the right
                //top of source
                store = adjacentBombs(index - 8);
                if (store == 0) {
                    if (!revealed.contains(index-8)) {
                        stack.push(index - 8);
                        cell_tvs.get(index - 8).setText("");
                        changesetting(index - 8);
                    }
                } else {
                    if (!revealed.contains(index-8)) {
                        cell_tvs.get(index - 8).setText(String.valueOf(store));
                        changesetting(index - 8);
                    }
                }
                //topleft of source
                store = adjacentBombs(index - 9);
                if (store == 0) {
                    if (!revealed.contains(index-9)) {
                        stack.push(index - 9);
                        cell_tvs.get(index - 9).setText("");
                        changesetting(index - 9);
                    }
                } else {
                    if (!revealed.contains(index-9)) {
                        cell_tvs.get(index - 9).setText(String.valueOf(store));
                        changesetting(index - 9);
                    }
                }
                //left of source
                store = adjacentBombs(index - 1);
                if (store == 0) {
                    if (!revealed.contains(index-1)) {
                        stack.push(index - 1);
                        cell_tvs.get(index - 1).setText("");
                        changesetting(index - 1);
                    }
                } else {
                    if (!revealed.contains(index-1)) {
                        cell_tvs.get(index - 1).setText(String.valueOf(store));
                        changesetting(index - 1);
                    }
                }
                //belowleft of source
                store = adjacentBombs(index + 7);
                if (store == 0) {
                    if (!revealed.contains(index+7)) {
                        stack.push(index + 7);
                        cell_tvs.get(index + 7).setText("");
                        changesetting(index + 7);
                    }
                } else {
                    if (!revealed.contains(index+7)) {
                        cell_tvs.get(index + 7).setText(String.valueOf(store));
                        changesetting(index + 7);
                    }
                }
                //below of source
                store = adjacentBombs(index + 8);
                if (store == 0) {
                    if (!revealed.contains(index+8)) {
                        stack.push(index + 8);
                        cell_tvs.get(index + 8).setText("");
                        changesetting(index + 8);
                    }
                } else {
                    if (!revealed.contains(index+8)) {
                        cell_tvs.get(index + 8).setText(String.valueOf(store));
                        changesetting(index + 8);
                    }
                }
                continue;

            }
            else {
                //topleft of source
                store = adjacentBombs(index - 9);
                if (store == 0) {
                    if (!revealed.contains(index-9)) {
                        stack.push(index - 9);
                        cell_tvs.get(index - 9).setText("");
                        changesetting(index - 9);
                    }
                } else {
                    if (!revealed.contains(index-9)) {
                        cell_tvs.get(index - 9).setText(String.valueOf(store));
                        changesetting(index - 9);
                    }
                }
                //top of source
                store = adjacentBombs(index - 8);
                if (store == 0) {
                    if (!revealed.contains(index-8)) {
                        stack.push(index - 8);
                        cell_tvs.get(index - 8).setText("");
                        changesetting(index - 8);
                    }
                } else {
                    if (!revealed.contains(index-8)) {
                        cell_tvs.get(index - 8).setText(String.valueOf(store));
                        changesetting(index - 8);
                    }
                }
                //topright of source
                store = adjacentBombs(index - 7);
                if (store == 0) {
                    if (!revealed.contains(index-7)) {
                        stack.push(index - 7);
                        cell_tvs.get(index - 7).setText("");
                        changesetting(index - 7);
                    }
                } else {
                    if (!revealed.contains(index-7)) {
                        cell_tvs.get(index - 7).setText(String.valueOf(store));
                        changesetting(index - 7);
                    }
                }
                //right of source
                store = adjacentBombs(index + 1);
                if (store == 0) {
                    if (!revealed.contains(index+1)) {
                        stack.push(index + 1);
                        cell_tvs.get(index + 1).setText("");
                        changesetting(index + 1);
                    }
                } else {
                    if (!revealed.contains(index+1)) {
                        cell_tvs.get(index + 1).setText(String.valueOf(store));
                        changesetting(index + 1);
                    }
                }
                //belowright of source
                store = adjacentBombs(index + 9);
                if (store == 0) {
                    if (!revealed.contains(index+9)) {
                        stack.push(index + 9);
                        cell_tvs.get(index + 9).setText("");
                        changesetting(index + 9);
                    }
                } else {
                    if (!revealed.contains(index+9)) {
                        cell_tvs.get(index + 9).setText(String.valueOf(store));
                        changesetting(index + 9);
                    }
                }
                //below of source
                store = adjacentBombs(index + 8);
                if (store == 0) {
                    if (!revealed.contains(index+8)) {
                        stack.push(index + 8);
                        cell_tvs.get(index + 8).setText("");
                        changesetting(index + 8);
                    }
                } else {
                    if (!revealed.contains(index+8)) {
                        cell_tvs.get(index + 8).setText(String.valueOf(store));
                        changesetting(index + 8);
                    }
                }
                //belowleft of source
                store = adjacentBombs(index + 7);
                if (store == 0) {
                    if (!revealed.contains(index+7)) {
                        stack.push(index + 7);
                        cell_tvs.get(index + 7).setText("");
                        changesetting(index + 7);
                    }
                } else {
                    if (!revealed.contains(index+7)) {
                        cell_tvs.get(index + 7).setText(String.valueOf(store));
                        changesetting(index + 7);
                    }
                }
                //left of source
                store = adjacentBombs(index - 1);
                if (store == 0) {
                    if (!revealed.contains(index-1)) {
                        stack.push(index - 1);
                        cell_tvs.get(index - 1).setText("");
                        changesetting(index - 1);
                    }
                } else {
                    if (!revealed.contains(index-1)) {
                        cell_tvs.get(index - 1).setText(String.valueOf(store));
                        changesetting(index - 1);
                    }
                }
            }

        }
    }


//    void addEdge(int v,int w) {
//        adj[v].add(w);
//    }

//    void BFS(int index) {
//
//        if (cell_tvs.)
//
//        boolean nodes[] = new boolean[NODES];       //initialize boolean array for holding the data
//        int a = 0;
//
//        nodes[index] = true;
//        queue.add(index);                   //root node is added to the top of the queue
//
//        while (queue.size() != 0) {
//            index = queue.poll();             //remove the top element of the queue
//            System.out.print(index + " ");           //print the top element of the queue
//
//            for (int i = 0; i < 8; i++) {
//
//            }
//
//            for (int i = 0; i < adj[n].size(); i++)  {//iterate through the linked list and push all neighbors into queue
//
//                a = adj[n].get(i);
//                if (!nodes[a])                    //only insert nodes into queue if they have not been explored already
//                {
//                    nodes[a] = true;
//                    queue.add(a);
//                }
//            }
//        }
//    }




}