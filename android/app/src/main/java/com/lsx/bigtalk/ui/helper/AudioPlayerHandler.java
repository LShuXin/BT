
package com.lsx.bigtalk.ui.helper;

import android.content.Context;
import android.media.AudioManager;

import com.lsx.bigtalk.service.event.AudioEvent;
import com.lsx.bigtalk.service.support.audio.SpeexDecoder;
import com.lsx.bigtalk.logs.Logger;

import java.io.File;
import java.util.Objects;

import de.greenrobot.event.EventBus;

public class AudioPlayerHandler{
    private String currentPlayPath = null;
    private SpeexDecoder speexdec = null;
    private Thread th = null;

    private static AudioPlayerHandler instance = null;
    private final Logger logger = Logger.getLogger(AudioPlayerHandler.class);

    public static  AudioPlayerHandler getInstance() {
        if (null == instance) {
            synchronized(AudioPlayerHandler.class){
                instance = new AudioPlayerHandler();
                 EventBus.getDefault().register(instance);
            }
        }
        return instance;
    }


    //语音播放的模式
    public  void setAudioMode(int mode,Context ctx) {
        if (mode != AudioManager.MODE_NORMAL && mode != AudioManager.MODE_IN_CALL) {
            return;
        }
        AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(mode);
    }

    /**messagePop调用*/
    public int getAudioMode(Context ctx) {
        AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getMode();
    }

    public void clear(){
        if (isPlaying()){
            stopPlayer();
        }
        EventBus.getDefault().unregister(instance);
        instance = null;
    }


    private AudioPlayerHandler() {
    }

    /**
     * yingmu modify
     * speexdec 由于线程模型
     * */
    public interface AudioListener{
        void onStop();
    }

    private AudioListener audioListener;

    public void setAudioListener(AudioListener audioListener) {
        this.audioListener = audioListener;
    }

    private void stopAnimation(){
        if(audioListener!=null){
            audioListener.onStop();
        }
    }

    public void onEventMainThread(AudioEvent audioEvent){
        if (Objects.requireNonNull(audioEvent) == AudioEvent.AUDIO_STOP_PLAY) {
            currentPlayPath = null;
            stopPlayer();
        }
    }

    public void stopPlayer() {
        try {
            if (null != th) {
                th.interrupt();
                th = null;
                Thread.currentThread().interrupt();
            } else {
            }
        } catch (Exception e) {
            logger.e(e.getMessage());
        }finally {
            stopAnimation();
        }
    }

    public boolean isPlaying() {
        return null != th;
    }

    public void startPlay(String filePath) {
        this.currentPlayPath = filePath;
        try {
            speexdec = new SpeexDecoder(new File(this.currentPlayPath));
            RecordPlayThread rpt = new RecordPlayThread();
            if (null == th)
                th = new Thread(rpt);
            th.start();
        } catch (Exception e) {
            // 关闭动画很多地方需要写，是不是需要重新考虑一下@yingmu
            logger.e(e.getMessage());
            stopAnimation();
        }
    }



    class RecordPlayThread extends Thread {
        public void run() {
            try {
                if (null != speexdec)
                    speexdec.decode();

            } catch (Exception e) {
                logger.e(e.getMessage());
                stopAnimation();
            }
        }
    }

    public String getCurrentPlayPath() {
        return currentPlayPath;
    }
}
