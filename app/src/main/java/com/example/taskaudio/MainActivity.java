package com.example.taskaudio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 2. 在 Android 平台使用 AudioRecord 和 AudioTrack API 完成音频 PCM 数据的采集和播放，并实现读写音频 wav 文件

 */

public class MainActivity extends ActionBarActivity {
    private static final int RECORDER_BPP = 16;
    //private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";    //默认录音文件的存储位置
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    private static final String TAG = "MainActivity" ;
    private static int frequency = 22050;
    private static int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_STEREO;//单声道
    private static int EncodingBitRate = AudioFormat.ENCODING_PCM_16BIT;    //音频数据格式：脉冲编码调制（PCM）每个样品16位
    private AudioRecord audioRecord = null;
    private int recBufSize = 0;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    private Button start1,stop1,mStartPlay,mStopPlay;;
    private TextView txt;

    private int trackBufSize = 0 ;
    private AudioTrack audioTrack = null ;
    private boolean isPlaying = false ;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taskone);
        start1=(Button)findViewById(R.id.start_record);
        stop1=(Button)findViewById(R.id.stop_record);
        txt=(TextView)findViewById(R.id.status);

        mStartPlay = (Button) findViewById(R.id.play);
        mStopPlay = (Button)findViewById(R.id.stop_play);

        start1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startRecord();
                txt.setText("录音中");
            }
        });
        stop1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                stopRecord();
                txt.setText("結束了");

            }
        });


        mStartPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txt.setText("播放中");
                createAudioTrack();
            }
        });

        mStopPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txt.setText("播放结束");
                isPlaying = false ;
            }
        });

    }





    private String getFilename(){
        String filepath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if(file.exists()){
            file.delete();
        }

        return (file.getAbsolutePath() + "/speaker.wav" );
    }

    private String getTempFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }

        File tempFile = new File(filepath,AUDIO_RECORDER_TEMP_FILE);

        if(tempFile.exists())
            tempFile.delete();

        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
    }

    private void startRecord(){

        createAudioRecord();
        audioRecord.startRecording();

        isRecording = true;

        recordingThread = new Thread(new Runnable() {
            public void run() {
                writeAudioDataToFile();
            }
        },"AudioRecorder Thread");

        recordingThread.start();
    }

    private void writeAudioDataToFile(){
        byte data[] = new byte[recBufSize];
        String filename = getTempFilename();
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int read = 0;

        if(null != os){
            while(isRecording){
                read = audioRecord.read(data, 0, recBufSize);

                if(AudioRecord.ERROR_INVALID_OPERATION != read){
                    try {
                        os.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopRecord(){
        if(null != audioRecord){
            isRecording = false;

            audioRecord.stop();
            audioRecord.release();

            audioRecord = null;
            recordingThread = null;
        }

        Log.d(TAG,"getFilename():"+getFilename());
        copyWaveFile(getTempFilename(),getFilename());
        deleteTempFile();
    }

    private void deleteTempFile() {
        File file = new File(getTempFilename());

        file.delete();
    }

    private void copyWaveFile(String inFilename,String outFilename){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = frequency;
        int channels = 1;
        long byteRate = RECORDER_BPP * frequency * channels/8;

        byte[] data = new byte[recBufSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            //AppLog.logString("File size: " + totalDataLen);

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while(in.read(data) != -1){
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //转化为wav格式
    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (1 * 16 / 8);  // block align
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }



    public void createAudioRecord(){
        recBufSize = AudioRecord.getMinBufferSize(frequency,
                channelConfiguration, EncodingBitRate);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,
                channelConfiguration, EncodingBitRate, recBufSize);
        Log.d(TAG,"AudioRecord成功");
    }

    public void createAudioTrack(){

        trackBufSize = AudioTrack.getMinBufferSize(frequency,AudioFormat.CHANNEL_CONFIGURATION_STEREO ,EncodingBitRate);

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC , frequency , AudioFormat.CHANNEL_CONFIGURATION_STEREO ,
                EncodingBitRate,trackBufSize,AudioTrack.MODE_STREAM );

        audioTrack.play();
        isPlaying = true ;

        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(getFilename()));

            while(dis.available() > 0 && isPlaying) {
                int i = 0 ;
                byte[] s = new byte[trackBufSize];
                while ( (i = dis.read(s,0,trackBufSize)) > -1){
                    audioTrack.write(s,0,i);
                }
            }

            audioTrack.stop();
            audioTrack.release();
            dis.close();

        } catch (FileNotFoundException e) {
            Log.d(TAG,"FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG,"available");
            e.printStackTrace();
        }
    }

}
