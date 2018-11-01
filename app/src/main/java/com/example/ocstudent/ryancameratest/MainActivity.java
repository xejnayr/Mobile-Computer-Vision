package com.example.ocstudent.ryancameratest;

//Ryan Jex
//Mobile Computer Vision Application WIP

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
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
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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

    CascadeClassifier faceClassifier;
    CascadeClassifier eyesClassifier;
    int absoluteFaceSize;

    int blurKernel = 35; //variables for some of the camera effects
    int dilateKernel = 10;
    double edgeThreshold = 28;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    initializeOpenCVDependencies();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    private void initializeOpenCVDependencies() {

        try {
            InputStream face_input = getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
            InputStream eyes_input = getResources().openRawResource(R.raw.haarcascade_eye_tree_eyeglasses);
            File cascadeDir = getDir("cascades", Context.MODE_PRIVATE);
            File faceCascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt.xml");
            File eyesCascadeFile = new File(cascadeDir, "haarcascade_eye_tree_eyeglasses.xml");
            FileOutputStream face_output;
            face_output = new FileOutputStream(faceCascadeFile);
            FileOutputStream eyes_output = new FileOutputStream(eyesCascadeFile);

            byte[] faceBuffer = new byte[4096];
            byte[] eyesBuffer = new byte[4096];
            int bytesRead;
            int bytesRead2;
            while ((bytesRead = face_input.read(faceBuffer)) != -1){
                face_output.write(faceBuffer, 0, bytesRead);
            }
            while ((bytesRead2 = eyes_input.read(eyesBuffer)) != -1){
                eyes_output.write(eyesBuffer, 0, bytesRead2);
            }

            face_input.close();
            face_output.close();
            eyes_input.close();
            eyes_output.close();

            faceClassifier = new CascadeClassifier(faceCascadeFile.getAbsolutePath());
            eyesClassifier = new CascadeClassifier(eyesCascadeFile.getAbsolutePath());

        } catch (Exception e) {
            Log.e("MainActivity", "Error loading cascade", e);
        }

        mOpenCvCameraView.enableView();

    }

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
        absoluteFaceSize = (int) (height * 0.2);

    }

    public void onCameraViewStopped() {
        mRgba.release();
    }


    public Mat faceDetection(Mat baseImage){

        Mat grayImage = new Mat();
        Imgproc.cvtColor(baseImage, grayImage, Imgproc.COLOR_RGB2GRAY);

        MatOfRect faces = new MatOfRect();
        MatOfRect eyes = new MatOfRect();

        if (faceClassifier != null) {
            faceClassifier.detectMultiScale(grayImage, faces, 1.1, 2, 2, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }

        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++){
            Imgproc.rectangle(baseImage, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 255, 255), 3);
        }

        Rect[] eyesArray = eyes.toArray();
        for (int i = 0; i < eyesArray.length; i++){
            Imgproc.rectangle(baseImage, eyesArray[i].tl(), eyesArray[i].br(), new Scalar(0, 255, 255, 255), 2);
        }


        return baseImage;
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        Intent intent = getIntent();

        Bundle extras = intent.getExtras();

        Mat baseImage = inputFrame.rgba();
        Mat cameraEffect = baseImage;
        /*
        String appPath = getApplicationContext().getFilesDir().getAbsolutePath();
        System.out.println(appPath);
        These are unneeded : used for testing in the past */


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
            case 7: //Face detection!!!!
                cameraEffect = faceDetection(baseImage);
                break;


        }


        return cameraEffect; // This function must return
    }
}
