package com.example.taskaudio;

import android.os.Environment;

import java.io.File;

/**
 * author: daxiong9527
 * mail : 15570350453@163.com
 */

public class Util {

    private static final String VIDEO_RECORDER_FOLDER = "VideoRecorder";
    private static final String VIDEO_RECORDER_TEMP_FILE = "video_temp.mp4";

    // 存放文件的目录路径

    public static String getDicFile(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,VIDEO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }

        return (file.getAbsolutePath());

    }



    //视频文件
    public static String getTempFilename(){
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
