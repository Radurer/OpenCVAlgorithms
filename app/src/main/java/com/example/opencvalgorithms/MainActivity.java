package com.example.opencvalgorithms;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button orbButton;
    private Button briskButton;
    private Button siftButton;
    private Button asiftButton;
    private Button kazeButton;
    private Button akazeButton;
    private Button mserButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        orbButton = findViewById(R.id.orbButton);
        briskButton = findViewById(R.id.briskButton);
        siftButton = findViewById(R.id.siftButton);
        asiftButton = findViewById(R.id.asiftButton);
        kazeButton = findViewById(R.id.kazeButton);
        akazeButton = findViewById(R.id.akazeButton);
        mserButton = findViewById(R.id.mserButton);

        Intent intent = new Intent(this, OpenCVActivity.class);

        orbButton.setOnClickListener(view -> {
            intent.putExtra("SELECTED_ALGORITHM", "ORB");
            startActivity(intent);
        });

        briskButton.setOnClickListener(view -> {
            intent.putExtra("SELECTED_ALGORITHM", "BRISK");
            startActivity(intent);
        });

        siftButton.setOnClickListener(view -> {
            intent.putExtra("SELECTED_ALGORITHM", "SIFT");
            startActivity(intent);
        });

        asiftButton.setOnClickListener(view -> {
            intent.putExtra("SELECTED_ALGORITHM", "ASIFT");
            startActivity(intent);
        });

        kazeButton.setOnClickListener(view -> {
            intent.putExtra("SELECTED_ALGORITHM", "KAZE");
            startActivity(intent);
        });

        akazeButton.setOnClickListener(view -> {
            intent.putExtra("SELECTED_ALGORITHM", "AKAZE");
            startActivity(intent);
        });

        mserButton.setOnClickListener(view -> {
            intent.putExtra("SELECTED_ALGORITHM", "MSER");
            startActivity(intent);
        });


    }



}