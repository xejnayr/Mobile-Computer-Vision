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
import org.opencv.objdetect.CascadeClassifier;

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

    /*
    Mat morphElement;
    Size morphSize;
    Point morphPoint;
    */

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
        //int morphVal = 10;
        int blurKernel = 35;
        int dilateKernel = 10;
        double edgeThreshold = 28;
        /*
        morphSize = new Size(2*morphVal+1, 2*morphVal+1);
        morphPoint = new Point(morphVal, morphVal);
        */

        switch(effectNumber) {
            case 0: //Default Camera
                cameraEffect = baseImage;
                break;
            case 1: //Grayscale
                Imgproc.cvtColor(baseImage, cameraEffect, Imgproc.COLOR_RGB2GRAY);
                break;
            case 2: //Edge Detection
                Imgproc.cvtColor(baseImage, cameraEffect, Imgproc.COLOR_RGB2GRAY);
                Imgproc.Canny(cameraEffect, cameraEffect, edgeThreshold, edgeThreshold * 3, 3, false);
                break;
            /*case 2: //Morphology was broken so I'm putting edge detection in its place
                morphElement = Imgproc.getStructuringElement(Imgproc.MORPH_BLACKHAT, morphSize, morphPoint);
                Imgproc.morphologyEx(baseImage, cameraEffect, Imgproc.MORPH_BLACKHAT, morphElement);
                break; */
            case 3: //Inversion
                //Mat invert = new Mat(baseImage.rows(), baseImage.cols(), baseImage.type(), new Scalar(255, 255, 255));
                //Core.subtract(invert, baseImage, cameraEffect);
                Core.bitwise_not(baseImage, cameraEffect);
              //invert = null;
                break;
            case 4: //Median Blur
                Imgproc.medianBlur(baseImage, cameraEffect, blurKernel);
                break;
            case 5: //Dilation
                Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2*dilateKernel+1, 2*dilateKernel));
                Imgproc.dilate(baseImage, cameraEffect, dilateElement);
                break;
            case 6: //Scharr
                Imgproc.Scharr(baseImage, cameraEffect, Imgproc.CV_SCHARR, 0, 1);
                break;
            case 7: //Working on face detection
                Imgproc.cvtColor(baseImage, cameraEffect, Imgproc.COLOR_RGB2GRAY);
                //Imgproc.equalizeHist(cameraEffect, cameraEffect);
                //CascadeClassifier();
                break;

        }


        return cameraEffect; // This function must return
    }
}
