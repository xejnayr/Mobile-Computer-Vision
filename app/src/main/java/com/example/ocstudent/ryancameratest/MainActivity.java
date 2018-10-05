package com.example.ocstudent.ryancameratest;

//Ryan Jex
//Mobile Computer Vision Application WIP

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.JavaCameraView;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.Core.BORDER_DEFAULT;

// OpenCV Classes

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2 {

    // Used for logging success or failure messages
    private static final String TAG = "OCVSample::Activity";

    public static Integer effectNumber = 0;

    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
    private CameraBridgeViewBase mOpenCvCameraView;
    //CameraBridgeViewBase is what is needed to view and alter vision

    // Used in Camera selection from menu (when implemented)
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;

    // These variables are used (at the moment) to fix camera orientation from 270degree to 0degree
    Mat mRgba;
    Mat mRgbaF;
    Mat mRgbaT;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.show_camera);

        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.show_camera_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
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

        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, width, CvType.CV_8UC4);

    }

    public void onCameraViewStopped() {
        mRgba.release();
    }


    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        Intent intent = getIntent();

        Bundle extras = intent.getExtras();

        //Mat baseImage = new Mat();
        //Mat alterImage = new Mat();
        Mat baseImage = inputFrame.rgba();
        Mat cameraEffect = baseImage;

        int morphSize = 25;
        int blurKernel = 35;
        int dilateKernel = 10;

        switch(effectNumber) {
            case 0:
                cameraEffect = baseImage;
                break;
            case 1:
                Imgproc.cvtColor(baseImage, cameraEffect, Imgproc.COLOR_RGB2GRAY);
                break;
            case 2:
                Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_BLACKHAT, new Size(2*morphSize+1, 2*morphSize+1), new Point(morphSize, morphSize));
                Imgproc.morphologyEx(baseImage, cameraEffect, Imgproc.MORPH_TOPHAT, element);
                break;
                //cameraEffect = alterImage;
            case 3:
                Mat invert = new Mat(baseImage.rows(), baseImage.cols(), baseImage.type(), new Scalar(255,255,255));
                Core.subtract(invert, baseImage, cameraEffect);
                break;
                //cameraEffect = alterImage;
            case 4:
                Imgproc.medianBlur(mRgba, cameraEffect, blurKernel);
                break;
            case 5:
                Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2*dilateKernel+1, 2*dilateKernel));
                Imgproc.dilate(baseImage, cameraEffect, dilateElement);
                break;
        }


        return cameraEffect; // This function must return
    }
}
