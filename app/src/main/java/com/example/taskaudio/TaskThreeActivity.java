package com.example.taskaudio;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * author: daxiong9527
 * mail : 15570350453@163.com
 */

public class TaskThreeActivity extends AppCompatActivity {
    private static final String TAG = "TaskThreeActivity" ;

    MediaExtractor mMediaExtractor = null;
    MediaMuxer mMediaMuxer = null ;
    Button mBtnOne,mBtnTwo,mBtnThree;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taskthree);
        mMediaExtractor = new MediaExtractor();
        mBtnOne = (Button) findViewById(R.id.btn_one);
        mBtnTwo = (Button) findViewById(R.id.btn_two);
        mBtnThree = (Button) findViewById(R.id.btn_three);

        mBtnOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exactorMedia();
            }
        });

        mBtnTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                extractorVideo();
            }
        });
        mBtnThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewVideo();
            }
        });
    }


    // 分离音频
    void exactorMedia() {

        try {

                mMediaExtractor.setDataSource(Util.getTempFilename());
                int numTracks = mMediaExtractor.getTrackCount();

                int audioTrackIndex = -1 ;
                for(int i = 0 ; i < numTracks ; i++){
                    MediaFormat format = mMediaExtractor.getTrackFormat(i);
                    String mime = format.getString(MediaFormat.KEY_MIME);
                    Log.d(TAG, "mine:" + mime);
                    //音频信道
                    if (mime.startsWith("audio/")) {
                        audioTrackIndex = i ;
                        mMediaExtractor.selectTrack(audioTrackIndex);
                        MediaFormat trackformat = mMediaExtractor.getTrackFormat(audioTrackIndex);
                        mMediaMuxer = new MediaMuxer(Util.getDicFile()+"/out_audio.mp3",
                                MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                        int writeAudioIndex = mMediaMuxer.addTrack(trackformat);
                        mMediaMuxer.start();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
                        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

                        long stampTime = getFrameTime(mMediaExtractor,byteBuffer) ;


                        mMediaExtractor.unselectTrack(audioTrackIndex);
                        mMediaExtractor.selectTrack(audioTrackIndex);

                        while (true){
                            int readSampleSize = mMediaExtractor.readSampleData(byteBuffer,0);
                            if(readSampleSize < 0 ){
                                break;
                            }
                            mMediaExtractor.advance();

                            bufferInfo.size = readSampleSize ;
                            bufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                            bufferInfo.offset = 0 ;
                            bufferInfo.presentationTimeUs += stampTime;

                            mMediaMuxer.writeSampleData(writeAudioIndex,byteBuffer,bufferInfo);
                        }
                            mMediaMuxer.stop();
                            mMediaMuxer.release();
                            mMediaExtractor.release();
                        Log.d(TAG,"finsh");
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            Log.d(TAG,"FIEL NOT FIND");
            }finally {
            mMediaExtractor.release();

        }

    }

    // 分离视频
    void extractorVideo(){
        try {
            mMediaExtractor.setDataSource(Util.getTempFilename());
            int numTracks = mMediaExtractor.getTrackCount();

            int videoTrackIndex = -1 ;
            for(int i = 0 ; i < numTracks ; i++) {
                MediaFormat format = mMediaExtractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                Log.d(TAG, "mine:" + mime);
                if(mime.startsWith("video/")){
                    videoTrackIndex = i;

                }
            }

            mMediaExtractor.selectTrack(videoTrackIndex);
            MediaFormat trackformat = mMediaExtractor.getTrackFormat(videoTrackIndex);
            mMediaMuxer = new MediaMuxer(Util.getDicFile()+"/newout_video.mp4",
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            int trackIndex = mMediaMuxer.addTrack(trackformat);
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 500);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            mMediaMuxer.start();
            long videostampTime  = getFrameTime(mMediaExtractor,byteBuffer);

            mMediaExtractor.unselectTrack(videoTrackIndex);
            mMediaExtractor.selectTrack(videoTrackIndex);

            while (true){
                int readSampleSize = mMediaExtractor.readSampleData(byteBuffer,0);
                if(readSampleSize < 0){
                    break;
                }
                mMediaExtractor.advance();
                bufferInfo.size = readSampleSize;
                bufferInfo.offset = 0 ;
                bufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                bufferInfo.presentationTimeUs += videostampTime;

                mMediaMuxer.writeSampleData(trackIndex,byteBuffer,bufferInfo);
            }
            mMediaMuxer.stop();
            mMediaExtractor.release();
            mMediaMuxer.release();

            Log.d(TAG,"finish");





        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG,"IOException");
        }

    }

    //获得每帧的时间
    public long getFrameTime(MediaExtractor mediaExtractor ,ByteBuffer byteBuffer ){
        long stampTime ;
        mediaExtractor.readSampleData(byteBuffer,0);
        if(mediaExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC){
            mediaExtractor.advance();
        }
        mediaExtractor.readSampleData(byteBuffer,0);
        long secondTime = mediaExtractor.getSampleTime();
        mediaExtractor.advance();
        mediaExtractor.readSampleData(byteBuffer,0);
        long thridTime = mediaExtractor.getSampleTime();
        stampTime = Math.abs(thridTime - secondTime);
        Log.d(TAG,"stampTime:" + stampTime);

        return stampTime ;

    }


    //生成新的视频
    void createNewVideo(){

        try {
            //初始化视频
            MediaExtractor videoExtractor = new MediaExtractor();
            videoExtractor.setDataSource(Util.getDicFile()+"/newout_video.mp4");
            int numVideoTracks = videoExtractor.getTrackCount();
            int videoTrackIndex = -1 ;
            for(int i = 0 ; i < numVideoTracks ; i++) {
                MediaFormat videoformat = videoExtractor.getTrackFormat(i);
                String mime = videoformat.getString(MediaFormat.KEY_MIME);
                Log.d(TAG, "mine:" + mime);
                if(mime.startsWith("video/")){
                    videoTrackIndex = i;
                    break;
                }
            }

            //初始化音频
            MediaExtractor audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(Util.getDicFile()+"/hhh.mp3");
            int numAudioTracks = audioExtractor.getTrackCount();

            int audioTrackIndex = -1 ;
            for(int i = 0 ; i < numAudioTracks ; i++) {
                MediaFormat format = audioExtractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                Log.d(TAG, "mine:" + mime);
                //音频信道
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i;
                    break;
                }
            }
            videoExtractor.selectTrack(videoTrackIndex);
            audioExtractor.selectTrack(audioTrackIndex);

            MediaCodec.BufferInfo videoufferInfo = new MediaCodec.BufferInfo();
            MediaCodec.BufferInfo audiobufferInfo = new MediaCodec.BufferInfo();
            MediaFormat videotrackformat = videoExtractor.getTrackFormat(videoTrackIndex);
            MediaFormat audiotrackformat = audioExtractor.getTrackFormat(audioTrackIndex);
            MediaMuxer mMediaMuxer = new MediaMuxer(Util.getDicFile()+"/new_video.mp4",
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int trackVideoIndex = mMediaMuxer.addTrack(videotrackformat);
            int trackAudioIndex = mMediaMuxer.addTrack(audiotrackformat);

            ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 500);
            mMediaMuxer.start();

            long sampleTime = getFrameTime(videoExtractor,byteBuffer);
            videoExtractor.unselectTrack(videoTrackIndex);
            videoExtractor.selectTrack(videoTrackIndex);

            while (true){
                int readSampleSize = videoExtractor.readSampleData(byteBuffer,0);
                if(readSampleSize < 0){
                    break;
                }
                videoufferInfo.size = readSampleSize;
                videoufferInfo.offset = 0 ;
                videoufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                videoufferInfo.presentationTimeUs += sampleTime;

                mMediaMuxer.writeSampleData(trackVideoIndex,byteBuffer,videoufferInfo);
                videoExtractor.advance();
            }

            while (true){
                int readSampleSize = audioExtractor.readSampleData(byteBuffer,0);
                if(readSampleSize < 0 ){
                    break;
                }
                audiobufferInfo.size = readSampleSize ;
                audiobufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                audiobufferInfo.offset = 0 ;
                audiobufferInfo.presentationTimeUs += sampleTime;

                mMediaMuxer.writeSampleData(trackAudioIndex,byteBuffer,audiobufferInfo);
                audioExtractor.advance();
            }

            Log.d(TAG,"finish");
            mMediaMuxer.stop();
            mMediaMuxer.release();
            videoExtractor.release();
            audioExtractor.release();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }



}
