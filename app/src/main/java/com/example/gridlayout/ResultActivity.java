package com.example.gridlayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ResultActivity extends AppCompatActivity {
    public static final String EXTRA_ELAPSED_MS = "elapsed_ms";
    public static final String EXTRA_WON = "won";

    private TextView timeText;
    private TextView resultText;
    private TextView goodJobText;
    private Button playAgainBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        timeText = findViewById(R.id.timeText);
        resultText = findViewById(R.id.resultText);
        goodJobText = findViewById(R.id.goodJobText);
        playAgainBtn = findViewById(R.id.playAgainBtn);

        long elapsedMs = getIntent().getLongExtra(EXTRA_ELAPSED_MS, 0L);
        boolean won = getIntent().getBooleanExtra(EXTRA_WON, false);

        long seconds = (elapsedMs + 500) / 1000; // round to nearest second
        timeText.setText("Used " + seconds + " seconds.");

        if(won){
            resultText.setText("You won.");
            goodJobText.setVisibility(View.VISIBLE);
        } else {
            resultText.setText("You lost.");
            goodJobText.setVisibility(View.GONE);
        }

        playAgainBtn.setOnClickListener(v -> {
            //back to the game
            Intent i = new Intent(ResultActivity.this, MainActivity.class);
            //clear all activities so when back start a new game
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }
}