package com.example.opencvalgorithms;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button orbButton;
    private Button briskButton;
    private Button siftButton;
    private Button kazeButton;
    private Button akazeButton;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;

        orbButton = findViewById(R.id.orbButton);
        briskButton = findViewById(R.id.briskButton);
        siftButton = findViewById(R.id.siftButton);
        kazeButton = findViewById(R.id.kazeButton);
        akazeButton = findViewById(R.id.akazeButton);

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

        kazeButton.setOnClickListener(view -> {
            intent.putExtra("SELECTED_ALGORITHM", "KAZE");
            startActivity(intent);
        });

        akazeButton.setOnClickListener(view -> {
            intent.putExtra("SELECTED_ALGORITHM", "AKAZE");
            startActivity(intent);
        });

    }

}