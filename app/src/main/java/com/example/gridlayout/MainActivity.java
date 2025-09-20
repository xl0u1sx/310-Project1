package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
//import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int COLUMN_COUNT = 10;
    private static final int ROW_COUNT = 10;
    private static final int MINE_COUNT = 5;

    //store mine cells
    private boolean[][] minePlaced;
    private boolean[][] exposed;
    private boolean[][] flagged;
    private int[][] displayedMineCount;

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
        minePlaced = new boolean[ROW_COUNT][COLUMN_COUNT];
        exposed = new boolean[ROW_COUNT][COLUMN_COUNT];
        flagged = new boolean[ROW_COUNT][COLUMN_COUNT];
        displayedMineCount = new int[ROW_COUNT][COLUMN_COUNT];

        // Method (2): add four dynamically created cells
        GridLayout grid = (GridLayout) findViewById(R.id.gridLayout01);
        for (int i=0; i<COLUMN_COUNT; i++) {
            for (int j=0; j<ROW_COUNT; j++) {
                TextView tv = new TextView(this);
                tv.setHeight( dpToPixel(32) );
                tv.setWidth( dpToPixel(32) );
                tv.setTextSize( 16 );//dpToPixel(32) );
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

        //place mines
        //compute and display nearby mine counts
        placeMine();
        nearbyMineCount();

    }

    private void placeMine(){
        Random gen = new Random();
        int placed = 0;
        while(placed<MINE_COUNT){
            int i = gen.nextInt(ROW_COUNT);
            int j = gen.nextInt(COLUMN_COUNT);
            if(!minePlaced[i][j]){
                minePlaced[i][j] = true;
                ++placed;
            }
        }
    }

    private void nearbyMineCount(){
        int[] d = {-1, 0, 1}; //directions to explore
        for(int i=0; i<ROW_COUNT; ++i){
            for(int j=0; j<COLUMN_COUNT; ++j){
                if(minePlaced[i][j]){
                    displayedMineCount[i][j] = -1; //no mine count because this is where mine is
                }
                int count = 0;
                for(int k: d) for (int l: d){
                    if(k == 0 && l == 0) continue;
                    int ik = i+k;
                    int jl = j+l;
                    if(ik < ROW_COUNT && ik >= 0 && jl < COLUMN_COUNT && jl >= 0 && minePlaced[ik][jl]){
                        count++;
                    }
                }
                displayedMineCount[i][j] = count;
            }
        }
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

        int i = n/COLUMN_COUNT;
        int j = n%COLUMN_COUNT;

        if(exposed[i][j]) return;
        exposed[i][j] = true;

        if(minePlaced[i][j]){
            tv.setText("💣");
            tv.setBackgroundColor(Color.RED);
            tv.setTextColor(Color.BLACK);
            //reveal all mines
            revealAllMines();
            //end the game and not allow further clicks
            disableBoard();
            return;
        }

//        tv.setBackgroundColor(Color.GRAY);
//        tv.setTextColor(Color.BLACK);
        if(displayedMineCount[i][j] >0) {
            tv.setText(String.valueOf(displayedMineCount[i][j]));
        } else {
            tv.setText("");
        }

//        tv.setText(String.valueOf(i)+String.valueOf(j)); // display num in cell
//        if (tv.getCurrentTextColor() == Color.GRAY) {
//            tv.setTextColor(Color.GREEN);
//            tv.setBackgroundColor(Color.parseColor("lime"));
//        }else {
//            tv.setTextColor(Color.GRAY);
//            tv.setBackgroundColor(Color.LTGRAY);
//        }
    }

    // reveal all mines (then call game over screen after anothe click)
    private void revealAllMines() {
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                if (minePlaced[i][j]) {
                    TextView cell = cell_tvs.get(i * COLUMN_COUNT + j);
                    cell.setText("💣");
                    cell.setTextColor(Color.BLACK);
                    if (!exposed[i][j]) {
                        cell.setBackgroundColor(Color.RED);
                    }
                }
            }
        }
    }

    // disable further clicks after game over
    private void disableBoard() {
        for (TextView cell : cell_tvs) {
            cell.setOnClickListener(null);
        }
    }
}