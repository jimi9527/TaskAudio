package com.example.taskaudio;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

/**
 * author: daxiong9527
 * mail : 15570350453@163.com
 */

public class TaskTwoActivity extends AppCompatActivity {

    private static final String VIDEO_RECORDER_FOLDER = "VideoRecorder";
    private static final String VIDEO_RECORDER_TEMP_FILE = "video_temp.mp4";
    TextureView mTextureView;
    Button mBtnStart, mBtnStop;
    ImageView mImageView;

    Camera mCamera ;
    MediaRecorder mMediaRecorder ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasktwo);

        initview();
    }

    private void initview() {


        mTextureView = (TextureView) findViewById(R.id.textureview);

        mBtnStart = (Button) findViewById(R.id.start);
        mBtnStop = (Button) findViewById(R.id.stop);
        mImageView = (ImageView) findViewById(R.id.image);

        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(prepareVideoRecorder()){
                    mMediaRecorder.start();
                }
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecord();
            }
        });

    }

    class MediaPrepareTask extends AsyncTask<Void,Void,Boolean>{

        @Override
        protected Boolean doInBackground(Void... params) {

            return null;
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

        }


    }


    private boolean prepareVideoRecorder(){
        mCamera = Camera.open() ;
        mMediaRecorder = new MediaRecorder();
        mCamera.setDisplayOrientation(getDegree());
       // mCamera.startPreview();

        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        try {
            mCamera.setPreviewTexture(mTextureView.getSurfaceTexture());

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

            mMediaRecorder.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return true ;
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

}
