package com.example.opencvalgorithms;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.AKAZE;
import org.opencv.features2d.BRISK;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.Feature2D;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KAZE;
import org.opencv.features2d.ORB;
import org.opencv.features2d.SIFT;
import org.opencv.imgproc.Imgproc;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OpenCVActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private int w, h;
    private CameraBridgeViewBase mOpenCvCameraView;
    private Button pauseButton;
    private Button statsButton;
    private Dialog statsPopup;
    private Button closeStatsButton;
    private TextView statsInformation;
    private boolean isProcessingPaused = false;
    Scalar RED = new Scalar(255, 0, 0);
    Scalar GREEN = new Scalar(0, 255, 0);

    String chosenAlgorithm;

    Feature2D chosenAlgorithmVariable;
    double frameTimestamp;
    double previousFrameTimestamp;
    double frameRate;

    long detectAndComputeStart;
    long detectAndComputeEnd;

    long matchStart;
    long matchEnd;

    long drawMatchesStart;
    long drawMatchesEnd;

    float minimumDistance = Float.MAX_VALUE;
    float maximumDistance = 0;

    final long NANOS_TO_SECONDS = 1000000000;
    final long MILLIS_TO_SECONDS = 1000;
    final long MICROS_TO_MILLIS = 1000;

    DescriptorMatcher matcher;
    Mat inputImageDescriptors;
    Mat inputImage;
    MatOfKeyPoint inputImageKeypoints;

    List<MatOfDMatch> matchesList = new ArrayList<>();
    LinkedList<DMatch> goodMatchesList = new LinkedList<>();
    MatOfDMatch goodMatchesMat = new MatOfDMatch();
    Mat outputImage = new Mat();
    MatOfByte drawnMatches = new MatOfByte();
    MatOfKeyPoint liveFrameKeypoints = new MatOfKeyPoint();
    Mat liveFrameDescriptors = new Mat();
    double ratio = 0.7;

    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    try {
                        initializeOpenCVDependencies();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    private void initializeOpenCVDependencies() throws IOException {
        mOpenCvCameraView.setCameraPermissionGranted();
        mOpenCvCameraView.enableView();
        frameTimestamp = SystemClock.elapsedRealtimeNanos();
        Log.i(TAG, "View Enabled");

        //initializing the variables necessary to process the input image
        inputImage = new Mat();
        AssetManager assetManager = getAssets();
        InputStream inputImageName = assetManager.open("dollar.jpeg");
        Bitmap bitmap = BitmapFactory.decodeStream(inputImageName);
        Utils.bitmapToMat(bitmap, inputImage);
        Imgproc.cvtColor(inputImage, inputImage, Imgproc.COLOR_RGB2GRAY);
        inputImage.convertTo(inputImage, 0); //converting the image to match with the type of the cameras image
        inputImageDescriptors = new Mat();
        inputImageKeypoints = new MatOfKeyPoint();
        //end of input image variable initialization

        switch(chosenAlgorithm) {
            case "ORB":
                chosenAlgorithmVariable = ORB.create();
                matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
                break;
            case "SIFT":
                chosenAlgorithmVariable = SIFT.create();
                matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
                break;
            case "KAZE":
                chosenAlgorithmVariable = KAZE.create();
                matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_SL2);
                break;
            case "AKAZE":
                chosenAlgorithmVariable = AKAZE.create();
                matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
                break;
            case "BRISK":
                chosenAlgorithmVariable = BRISK.create();
                matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
                break;
        }

        chosenAlgorithmVariable.detectAndCompute(inputImage, new Mat(), inputImageKeypoints, inputImageDescriptors);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_opencvactivity);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }

        mOpenCvCameraView = findViewById(R.id.javaCameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        pauseButton = findViewById(R.id.pauseScanningButton);
        statsButton = findViewById(R.id.showStatsButton);

        pauseButton.setOnClickListener(view -> {
            if(isProcessingPaused){
                statsButton.setVisibility(View.INVISIBLE);
                pauseButton.setText("PAUSE");
                mOpenCvCameraView.enableView();
                isProcessingPaused = false;
            }
            else{
                statsButton.setVisibility(View.VISIBLE);
                pauseButton.setText("RESUME");
                mOpenCvCameraView.disableView();
                isProcessingPaused = true;
            }

        });

        statsButton.setOnClickListener(view -> {

            statsPopup = new Dialog(OpenCVActivity.this);
            statsPopup.setContentView(R.layout.stats_popup);
            statsPopup.setCancelable(true);
            statsPopup.setCanceledOnTouchOutside(false);
            statsPopup.show();

            closeStatsButton = statsPopup.findViewById(R.id.popupCloseButton);
            statsInformation = statsPopup.findViewById(R.id.statsInformation);

            String stats = "";

            stats += "Chosen algorithm: " + chosenAlgorithm + "\n";
            stats += "Key points initial image: " + inputImageKeypoints.size() + "\n";
            stats += "Descriptors initial image: " + inputImageDescriptors.size() + "\n";
            stats += "Key points this frame: " + liveFrameKeypoints.size() + "\n";
            stats += "Descriptors this frame: " + liveFrameDescriptors.size() + "\n";
            stats += "Total matches: " + matchesList.size() + "\n";
            stats += "KNN ratio value: " + ratio + "\n";
            stats += "Matches kept after KNN ratio test: " + goodMatchesList.size() + "\n";
            stats += "Minimum distance for kept matches: " + BigDecimal.valueOf(minimumDistance).setScale(2, RoundingMode.HALF_UP) + "\n";
            stats += "Maximum distance for kept matches: " + BigDecimal.valueOf(maximumDistance).setScale(2, RoundingMode.HALF_UP) + "\n";
            stats += "Time spent computing this frame: " + BigDecimal.valueOf(MILLIS_TO_SECONDS/frameRate).setScale(2, RoundingMode.HALF_UP) + " ms.\n";
            stats += "Time spent for detectAndCompute(): " + BigDecimal.valueOf((double)TimeUnit.NANOSECONDS.toMicros(detectAndComputeEnd-detectAndComputeStart)/MICROS_TO_MILLIS).setScale(2, RoundingMode.HALF_UP) + " ms.\n";
            stats += "Time spent for knnMatch(): " + BigDecimal.valueOf((double)TimeUnit.NANOSECONDS.toMicros(matchEnd-matchStart)/MICROS_TO_MILLIS).setScale(2, RoundingMode.HALF_UP) + " ms.\n";
            stats += "Time spent for drawMatches(): " + BigDecimal.valueOf((double)TimeUnit.NANOSECONDS.toMicros(drawMatchesEnd-drawMatchesStart)/MICROS_TO_MILLIS).setScale(2, RoundingMode.HALF_UP) + " ms.\n";

            statsInformation.setText(stats);

            closeStatsButton.setOnClickListener(view1 -> {
                statsPopup.dismiss();
            });

        });

        Bundle bundle = getIntent().getExtras();
        chosenAlgorithm = bundle.getString("SELECTED_ALGORITHM");

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    public void onCameraViewStarted(int width, int height) {
        w = width;
        h = height;
    }

    public void onCameraViewStopped() {
    }

    public Mat recognize(Mat aInputFrame) {
        Imgproc.cvtColor(aInputFrame, aInputFrame, Imgproc.COLOR_RGB2GRAY);
        detectAndComputeStart = System.nanoTime();
        chosenAlgorithmVariable.detectAndCompute(aInputFrame, new Mat(), liveFrameKeypoints, liveFrameDescriptors);
        detectAndComputeEnd = System.nanoTime();
        matchStart = System.nanoTime();
        matcher.knnMatch(inputImageDescriptors, liveFrameDescriptors, matchesList, 2);
        matchEnd = System.nanoTime();

        for (int i = 0; i < matchesList.size(); i++) {
            if(matchesList.get(i).rows() > 1){
                DMatch[] match = matchesList.get(i).toArray();
                if(match[0].distance <= ratio * match[1].distance){
                    goodMatchesList.add(match[0]);
                    if(match[0].distance < minimumDistance){
                        minimumDistance = match[0].distance;
                    }
                    if(match[0].distance > maximumDistance){
                        maximumDistance = match[0].distance;
                    }
                }
            }
        }

        goodMatchesMat.fromList(goodMatchesList);
        drawMatchesStart = System.nanoTime();
        Features2d.drawMatches(inputImage, inputImageKeypoints, aInputFrame, liveFrameKeypoints, goodMatchesMat, outputImage, GREEN, RED, drawnMatches, Features2d.DrawMatchesFlags_NOT_DRAW_SINGLE_POINTS);
        drawMatchesEnd = System.nanoTime();
        Imgproc.resize(outputImage, outputImage, aInputFrame.size());

        return outputImage;
    }

    public void resetVariables(){
        liveFrameDescriptors = new Mat();
        liveFrameKeypoints = new MatOfKeyPoint();
        matchesList = new ArrayList<>();
        goodMatchesList = new LinkedList<>();
        goodMatchesMat = new MatOfDMatch();
        outputImage = new Mat();
        drawnMatches = new MatOfByte();
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        previousFrameTimestamp = frameTimestamp;
        frameTimestamp = SystemClock.elapsedRealtimeNanos();
        frameRate = NANOS_TO_SECONDS/(frameTimestamp-previousFrameTimestamp);
        resetVariables();

        return recognize(inputFrame.rgba());
    }

}