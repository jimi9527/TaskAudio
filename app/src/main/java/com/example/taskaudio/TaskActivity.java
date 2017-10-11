package com.example.taskaudio;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * author: daxiong9527
 * mail : 15570350453@163.com
 */

public class TaskActivity extends AppCompatActivity {

    private static final String VIDEO_RECORDER_FOLDER = "VideoRecorder";
    private static final String VIDEO_RECORDER_TEMP_FILE = "video_temp.mp4";
    private static final String TAG =  "TaskActivity";
    SurfaceView mSurfaceView;
    Button mBtnStart , mBtnStop ;
    Camera mCamera ;
    MediaRecorder mMediaRecorder;
    ImageView mImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initSurview();
    }

    private void initSurview() {

        mSurfaceView.setKeepScreenOn(true);

        mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                mCamera =  Camera.open() ;
                mMediaRecorder = new MediaRecorder();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                dochange(holder);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

    }

    private void initView() {

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        mBtnStart = (Button) findViewById(R.id.start);
        mBtnStop = (Button) findViewById(R.id.stop);
        mImageView = (ImageView) findViewById(R.id.image);

        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TaskActivity.this, "开始录制", Toast.LENGTH_SHORT).show();
                startRecord();
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TaskActivity.this, "结束录制", Toast.LENGTH_SHORT).show();
                stopRecord();
            }
        });


    }

     private void dochange(SurfaceHolder holder){
         if(mCamera != null){
             try {
                 mCamera.setPreviewDisplay(holder);
                 mCamera.setDisplayOrientation(getDegree());
                 mCamera.startPreview();
                 //mCamera.setOneShotPreviewCallback(this);

             } catch (IOException e) {
                 e.printStackTrace();
             }

         }

     }

     public int getDegree() {
        //获取当前屏幕旋转的角度
        int rotating = this.getWindowManager().getDefaultDisplay().getRotation();
        int degree = 0;
        //根据手机旋转的角度，来设置surfaceView的显示的角度
        switch (rotating) {
            case Surface.ROTATION_0:
                degree = 90;
                break;
            case Surface.ROTATION_90:
                degree = 0;
                break;
            case Surface.ROTATION_180:
                degree = 270;
                break;
            case Surface.ROTATION_270:
                degree = 180;
                break;
        }
        return degree;
    }


    //视频文件的路径
    private String getTempFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,VIDEO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }

        File tempFile = new File(filepath,VIDEO_RECORDER_TEMP_FILE);

        if(tempFile.exists())
            tempFile.delete();

        return (file.getAbsolutePath() + "/" + VIDEO_RECORDER_TEMP_FILE);
    }

    //开始录制视频

    private void startRecord(){
        if(mCamera != null ){

            mCamera.unlock();
            mMediaRecorder.setCamera(mCamera);
            mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
            //设置视频帧速率
            mMediaRecorder.setVideoFrameRate(40);
            mMediaRecorder.setVideoEncodingBitRate(1024*1024);
            //设置输出视频格式
            mMediaRecorder.setOrientationHint(90);
            mMediaRecorder.setVideoSize(640,480);
            mMediaRecorder.setOutputFile(getTempFilename());
            Log.d(TAG,"getTempFilename:"+getTempFilename());


            try {
                mMediaRecorder.prepare();
                mMediaRecorder.start();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    //结束录制视频

    private void stopRecord(){

        if(mCamera != null ){

            mMediaRecorder.release();
            mCamera.release();
            mMediaRecorder = null;
            mCamera =  Camera.open();
            mMediaRecorder = new MediaRecorder();
            dochange(mSurfaceView.getHolder());
        }


    }

  /*  @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        Camera.Size size  = mCamera.getParameters().getPreviewSize() ;

        Log.d(TAG,"size.width:"+size.width);
        Log.d(TAG,"size.height:"+size.height);

        YuvImage image = new YuvImage(data, ImageFormat.NV21,size.width,size.height,null);

        if(null != image){
            ByteArrayOutputStream  stram = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(0,0,size.width,size.height),80,stram);
            Bitmap bitmap = BitmapFactory.decodeByteArray(stram.toByteArray(),0,stram.size());
            mImageView.setImageBitmap(bitmap);
            stram.size();
        }
    }*/
}
