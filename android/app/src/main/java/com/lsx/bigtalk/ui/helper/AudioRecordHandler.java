package com.lsx.bigtalk.ui.helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Message;

import androidx.core.app.ActivityCompat;

import com.lsx.bigtalk.AppConstant;

import com.lsx.bigtalk.service.support.audio.SpeexEncoder;
import com.lsx.bigtalk.ui.activity.MessageActivity;
import com.lsx.bigtalk.logs.Logger;

public class AudioRecordHandler implements Runnable {

    private final Logger logger = Logger.getLogger(AudioRecordHandler.class);
    private volatile boolean isRecording;
    private final Object mutex = new Object();
    private static final int frequency = 8000;
    private static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    public static int packagesize = 160;// 320
    private String fileName = null;
    private float recordTime = 0;
    private long startTime = 0;
    private long endTime = 0;
    private long maxVolumeStart = 0;
    private long maxVolumeEnd = 0;
    private static AudioRecord recordInstance = null;

    public AudioRecordHandler(String fileName) {
        super();
        this.fileName = fileName;
    }

    @SuppressLint("MissingPermission")
    public void run() {
        try {
            logger.d("chat#audio#in audio thread");
            SpeexEncoder encoder = new SpeexEncoder(this.fileName);
            Thread encodeThread = new Thread(encoder);
            encoder.setRecording(true);
            logger.d("chat#audio#encoder thread starts");
            encodeThread.start();

            synchronized (mutex) {
                while (!this.isRecording) {
                    try {
                        mutex.wait();
                    } catch (InterruptedException e) {
                        throw new IllegalStateException("Wait() interrupted!", e);
                    }
                }
            }
            android.os.Process
                    .setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            int bufferRead = 0;
            int bufferSize = AudioRecord.getMinBufferSize(frequency,
                    AudioFormat.CHANNEL_IN_MONO, audioEncoding);

            short[] tempBuffer = new short[packagesize];
            try {
                if (null == recordInstance) {
                    recordInstance = new AudioRecord(MediaRecorder.AudioSource.MIC,
                            frequency, AudioFormat.CHANNEL_IN_MONO, audioEncoding,
                            bufferSize);
                }

                recordInstance.startRecording();
                recordTime = 0;
                startTime = System.currentTimeMillis();
                maxVolumeStart = System.currentTimeMillis();
                while (this.isRecording) {
                    endTime = System.currentTimeMillis();
                    recordTime = (endTime - startTime) / 1000.0f;
                    if (recordTime >= AppConstant.SysConstant.MAX_SOUND_RECORD_TIME) {
                        MessageActivity.getRecordMsgHandler().sendEmptyMessage(
                                AppConstant.HandlerConstant.RECORD_AUDIO_TOO_LONG);
                        break;
                    }

                    bufferRead = recordInstance.read(tempBuffer, 0, packagesize);
                    if (bufferRead == AudioRecord.ERROR_INVALID_OPERATION) {
                        throw new IllegalStateException(
                                "read() returned AudioRecord.ERROR_INVALID_OPERATION");
                    } else if (bufferRead == AudioRecord.ERROR_BAD_VALUE) {
                        throw new IllegalStateException(
                                "read() returned AudioRecord.ERROR_BAD_VALUE");
                    } else if (bufferRead == AudioRecord.ERROR_INVALID_OPERATION) {
                        throw new IllegalStateException(
                                "read() returned AudioRecord.ERROR_INVALID_OPERATION");
                    }
                    encoder.putData(tempBuffer, bufferRead);
                    maxVolumeEnd = System.currentTimeMillis();
                    setMaxVolume(tempBuffer, bufferRead);
                }
            } catch (Exception e) {
                logger.e(e.getMessage());
            } finally {
                encoder.setRecording(false);
                if (recordInstance != null) {
                    recordInstance.stop();
                    recordInstance.release();
                    recordInstance = null;
                } else {
                }
            }
        } catch (Exception e) {
            logger.e(e.getMessage());
        }
    }

    private void setMaxVolume(short[] buffer, int readLen) {
        try {
            if (maxVolumeEnd - maxVolumeStart < 100) {
                return;
            }
            maxVolumeStart = maxVolumeEnd;
            int max = 0;
            for (int i = 0; i < readLen; i++) {
                if (Math.abs(buffer[i]) > max) {
                    max = Math.abs(buffer[i]);
                }
            }
            Message Msg = new Message();
            Msg.what = AppConstant.HandlerConstant.RECEIVE_MAX_VOLUME;
            Msg.obj = max;
            MessageActivity.getRecordMsgHandler().sendMessage(Msg);
        } catch (Exception e) {
            logger.e(e.getMessage());
        }
    }

    public float getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(float len) {
        recordTime = len;
    }

    public void setRecording(boolean isRec) {
        synchronized (mutex) {
            this.isRecording = isRec;
            if (this.isRecording) {
                mutex.notify();
            }
        }
    }

    public boolean isRecording() {
        synchronized (mutex) {
            return isRecording;
        }
    }
}
