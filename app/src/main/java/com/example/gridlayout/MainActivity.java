package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
//import android.view.LayoutInflater;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int COLUMN_COUNT = 10;
    private static final int ROW_COUNT = 10;
    private static final int MINE_COUNT = 5;
    private static final int CELL_SIZE_DP = 32;
    private static final int CELL_MARGIN_DP = 2;


    //store mine cells
    private boolean[][] minePlaced;
    private boolean[][] exposed;
    private boolean[][] flagged;
    private int[][] displayedMineCount;

    // save the TextViews of all cells in an array, so later on,
    // when a TextView is clicked, we know which cell it is
    private ArrayList<TextView> cell_tvs;
    private TextView timerText;

    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    //for elapsed time
    private String formatElapsed(long ms) {
        long totalSeconds = ms / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private boolean timerRunning = false;
    private long startTimeMs = 0L;
    private final Handler timerHandler = new Handler();
    private final Runnable timerTick = new Runnable() {
        @Override public void run() {
            if (!timerRunning) return;
            long elapsed = System.currentTimeMillis() - startTimeMs;
            timerText.setText(formatElapsed(elapsed));
            // update ~4 times per second (smooth but cheap)
            timerHandler.postDelayed(this, 250);
        }
    };

    private void timerStart() {
        if (timerRunning) return;
        timerRunning = true;
        startTimeMs = System.currentTimeMillis();
        timerHandler.post(timerTick);
    }

    private void timerStop() {
        if (!timerRunning) return;
        timerRunning = false;
        timerHandler.removeCallbacksAndMessages(null);
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
        grid.setRowCount(ROW_COUNT);
        grid.setColumnCount(COLUMN_COUNT);
        grid.setUseDefaultMargins(false);
        grid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);

        final int cellPx = dpToPixel(CELL_SIZE_DP);
        final int marginPx = dpToPixel(CELL_MARGIN_DP);

        //initialize time
        timerText = findViewById(R.id.timerText);
        if (timerText != null) {
            timerText.setText("00:00");
        }

        for (int i=0; i<COLUMN_COUNT; i++) {
            for (int j=0; j<ROW_COUNT; j++) {
                TextView tv = new TextView(this);
                GridLayout.LayoutParams lp = new GridLayout.LayoutParams(GridLayout.spec(i), GridLayout.spec(j));
                lp.width = cellPx;
                lp.height = cellPx;
                lp.setMargins(marginPx, marginPx, marginPx, marginPx);

                tv.setText("");
                tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                tv.setTextSize(16);
                tv.setBackgroundColor(Color.GRAY);
                tv.setTextColor(Color.GRAY);
                tv.setOnClickListener(this::onClickTV);
                tv.setOnLongClickListener(this::flagCells);
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
                    continue;
                }
                int count = 0;
                for(int k: d) for (int l: d){
                    if(k == 0 && l == 0) continue;
                    int ik = i+k;
                    int jl = j+l;
                    if(ik < ROW_COUNT && ik >= 0 && jl < COLUMN_COUNT && jl >= 0){
                        if(minePlaced[ik][jl]) count++;
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
        if(n<0) return;

        int i = n/COLUMN_COUNT;
        int j = n%COLUMN_COUNT;

        if(exposed[i][j] || flagged[i][j]) return;
        exposed[i][j] = true;

        timerStart();

        if(minePlaced[i][j]){
            tv.setText("💣");
            tv.setBackgroundColor(Color.RED);
            tv.setTextColor(Color.BLACK);
            //reveal all mines
            revealAllMines();
            //end the game and not allow further clicks
            disableBoard();
            timerStop();
            //redirect to end game page with time spent
            return;
        }

        tv.setBackgroundColor(Color.LTGRAY);
        tv.setTextColor(Color.BLACK);
        if(displayedMineCount[i][j] >0) {
            tv.setText(String.valueOf(displayedMineCount[i][j]));
        } else {
            tv.setText("");
        }

        revealEmptyCells(i,j);

        if(ifWin()){
            disableBoard();
            timerStop();
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

    public boolean flagCells(View view) {
        TextView tv = (TextView) view;
        int n = findIndexOfCellTextView(tv);
        if (n < 0) return true;

        int i = n / COLUMN_COUNT;
        int j = n % COLUMN_COUNT;

//        if (exposed[i][j]) return true;

        flagged[i][j] = !flagged[i][j];
        if (flagged[i][j]) {
            tv.setText("🚩");
//            tv.setTextColor(Color.RED);
//            tv.setBackgroundColor(Color.GRAY);
        } else {
            tv.setText("");
            tv.setTextColor(Color.GRAY);
            tv.setBackgroundColor(Color.GRAY);
        }
        return true; // consume long click
    }

    // Reveal logic with BFS flood-fill from zero cells
    private void revealEmptyCells(int si, int sj) {
        ArrayDeque<int[]> q = new ArrayDeque<>();

        //small lambda to reveal one cell
        java.util.function.BiConsumer<Integer, Integer> show = (ri, rj) -> {
            TextView cell = cell_tvs.get(ri * COLUMN_COUNT + rj);
            cell.setBackgroundColor(Color.LTGRAY);
            cell.setTextColor(Color.BLACK);
            int c = displayedMineCount[ri][rj];
            cell.setText(c > 0 ? String.valueOf(c) : "");
        };

        if (displayedMineCount[si][sj] > 0) {
            exposed[si][sj] = true;
            show.accept(si, sj);
            return;
        }

        //flood-fill
        q.add(new int[]{si, sj});
        exposed[si][sj] = true;
        show.accept(si, sj);

        int[] d = {-1, 0, 1};
        while (!q.isEmpty()) {
            int[] cur = q.removeFirst();
            int ci = cur[0], cj = cur[1];

            for (int di : d) for (int dj : d) {
                if (di == 0 && dj == 0) continue;
                int ni = ci + di, nj = cj + dj;
                if (ni < 0 || ni >= ROW_COUNT || nj < 0 || nj >= COLUMN_COUNT) continue;
                if (exposed[ni][nj] || flagged[ni][nj] || minePlaced[ni][nj]) continue;

                exposed[ni][nj] = true;
                show.accept(ni, nj);

                if (displayedMineCount[ni][nj] == 0) {
                    q.add(new int[]{ni, nj});
                }
            }
        }
    }

    private boolean ifWin() {
        int nonMineCells = ROW_COUNT * COLUMN_COUNT - MINE_COUNT;
        int revealedCells = 0;
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                if (!minePlaced[i][j] && exposed[i][j]) revealedCells++;
            }
        }
        return revealedCells == nonMineCells;
    }


    // disable further clicks after game over
    private void disableBoard() {
        for (TextView cell : cell_tvs) {
            cell.setOnClickListener(null);
            cell.setOnLongClickListener(null);
        }
    }
}