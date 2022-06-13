package com.example.opencvalgorithms;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.SurfaceView;
import android.view.WindowManager;

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
import org.opencv.features2d.AffineFeature;
import org.opencv.features2d.BRISK;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.Feature2D;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KAZE;
import org.opencv.features2d.MSER;
import org.opencv.features2d.ORB;
import org.opencv.features2d.SIFT;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Array;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class OpenCVActivity<features2d> extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private int w, h;
    private CameraBridgeViewBase mOpenCvCameraView;
    Scalar RED = new Scalar(255, 0, 0);
    Scalar GREEN = new Scalar(0, 255, 0);

    String chosenAlgorithm;

    int goodCount = 0;
    int descriptorsCount = 0;
    int featuresCount = 0;

    Feature2D chosenAlgorithmVariable;
    double time;
    double previousTime;
    double frameRate;
    long tempStart;
    long tempEnd;
    long cumulativeTime;

    long secondStart;
    long secondEnd;

    long thirdStart;
    long thirdEnd;


    DescriptorMatcher matcher;
    Mat descriptors1;
    Mat img1;
    MatOfKeyPoint keypoints1;

    List<MatOfDMatch> matchesList = new ArrayList<>();
    LinkedList<DMatch> good_matches = new LinkedList<>();
    MatOfDMatch goodMatches = new MatOfDMatch();
    Mat outputImg = new Mat();
    MatOfByte drawnMatches = new MatOfByte();
    MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
    Mat descriptors2 = new Mat();
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
        time = SystemClock.elapsedRealtimeNanos();
        Log.i(TAG, "View Enabled");

        //initializing the variables necessary to process the input image
        img1 = new Mat();
        AssetManager assetManager = getAssets();
        InputStream istr = assetManager.open("a.jpeg");
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        Utils.bitmapToMat(bitmap, img1);
        Imgproc.cvtColor(img1, img1, Imgproc.COLOR_RGB2GRAY);
        img1.convertTo(img1, 0); //converting the image to match with the type of the cameras image
        descriptors1 = new Mat();
        keypoints1 = new MatOfKeyPoint();
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

//        chosenAlgorithmVariable.detect(img1, keypoints1);
//        chosenAlgorithmVariable.compute(img1, keypoints1, descriptors1);
        chosenAlgorithmVariable.detectAndCompute(img1, new Mat(), keypoints1, descriptors1);

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

        Bundle bundle = getIntent().getExtras();
        chosenAlgorithm = bundle.getString("SELECTED_ALGORITHM");

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
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
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        w = width;
        h = height;
    }

    public void onCameraViewStopped() {
    }

    public Mat recognize(Mat aInputFrame) {
        Imgproc.cvtColor(aInputFrame, aInputFrame, Imgproc.COLOR_RGB2GRAY);
        Log.i(TAG, "\n\n");
        long start = System.nanoTime();
        chosenAlgorithmVariable.detectAndCompute(aInputFrame, new Mat(), keypoints2, descriptors2);
        long end = System.nanoTime();
        Log.i(TAG, "detectAndCompute length: " + (double)TimeUnit.NANOSECONDS.toMicros(end-start)/1000
                );
        start = System.nanoTime();
        matcher.knnMatch(descriptors1, descriptors2, matchesList, 2);
        end = System.nanoTime();
        Log.i(TAG, "knnMatch length: " + (double)TimeUnit.NANOSECONDS.toMicros(end-start)/1000 +
                " descriptors2 size: " + descriptors2.size() + " matches " + matchesList.size());

        for (int i = 0; i < matchesList.size(); i++) {
            if(matchesList.get(i).rows() > 1){
                DMatch[] match = matchesList.get(i).toArray();
                if(match[0].distance <= ratio * match[1].distance){//&& match[0].distance < 80){
                    good_matches.add(match[0]);
                }
            }
        }

        Log.i(TAG, "goodMatches: " + good_matches.size());

        goodMatches.fromList(good_matches);
        Features2d.drawMatches(img1, keypoints1, aInputFrame, keypoints2, goodMatches, outputImg, GREEN, RED, drawnMatches, Features2d.DrawMatchesFlags_NOT_DRAW_SINGLE_POINTS);
        Imgproc.resize(outputImg, outputImg, aInputFrame.size());

        return outputImg;
    }

    public void resetVariables(){
        descriptors2 = new Mat();
        keypoints2 = new MatOfKeyPoint();
        matchesList = new ArrayList<>();
        good_matches = new LinkedList<>();
        goodMatches = new MatOfDMatch();
        outputImg = new Mat();
        drawnMatches = new MatOfByte();
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        previousTime = time;
        time = SystemClock.elapsedRealtimeNanos();
        frameRate = 1000000000/(time-previousTime);
        Log.i(TAG, "Frames: " + frameRate + " msec per frame: " + 1000/frameRate);
        Mat result = recognize(inputFrame.rgba());

        resetVariables();

        //Log.i(TAG, "TOTAL RECOGNIZE() METHOD TIME FOR ORB: " + TimeUnit.NANOSECONDS.toMicros(end-start) + " ms.");
        //Log.i(TAG, "POTENTIAL RECOGNIZE() METHOD TIME IMPROVEMENT: " + (double)TimeUnit.NANOSECONDS.toMicros(end-start-(tempEnd - tempStart) - (secondEnd - secondStart)
           //                                                                                 - (thirdEnd - thirdStart))/1000 + " ms.");

        return result;
    }

}