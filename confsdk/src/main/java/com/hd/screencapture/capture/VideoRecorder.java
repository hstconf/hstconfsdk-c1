package com.hd.screencapture.capture;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.hd.screencapture.callback.RecorderCallback;
import com.hd.screencapture.config.ScreenCaptureConfig;
import com.hd.screencapture.config.VideoConfig;
import com.hd.screencapture.observer.CaptureObserver;

import java.io.IOException;

/**
 * Created by hd on 2018/5/20 .
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public final class VideoRecorder extends Recorder {

    private Surface surface;

    private VideoConfig videoConfig;

    VideoRecorder(@NonNull CaptureObserver observer, @NonNull ScreenCaptureConfig config,//
                         @NonNull RecorderCallback callback) {
        super(VIDEO_RECORDER, observer, config, callback);
        TAG = "VideoRecorder";
        videoConfig = config.getVideoConfig();
    }

    @Override
    public boolean prepare() {
        try {
            initMediaCodec(createMediaFormat());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void release() {
        super.release();
        if (surface != null) {
            surface.release();
            surface = null;
        }
        if (mEncoder != null) {
            mEncoder.stop();
            mEncoder.reset();
            mEncoder.release();
            mEncoder = null;
        }
    }

    public Surface getSurface() {
        return surface;
    }

    @Override
    MediaFormat createMediaFormat() {
        final String MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC;
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, videoConfig.getWidth(), videoConfig.getHeight());
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 2* 1000000);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 25);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        //设置码流模式
        mediaFormat.setInteger(MediaFormat.KEY_BITRATE_MODE,
                MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
        MediaCodecInfo.CodecProfileLevel codecProfileLevel = videoConfig.getLevel();
        if (codecProfileLevel != null && codecProfileLevel.profile != 0 && codecProfileLevel.level != 0) {
            mediaFormat.setInteger(MediaFormat.KEY_PROFILE, codecProfileLevel.profile);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mediaFormat.setInteger(MediaFormat.KEY_LEVEL, codecProfileLevel.level);
            } else {
                mediaFormat.setInteger("level", codecProfileLevel.level);
            }
        }
        if (config.allowLog())
            Log.d(TAG, "created video format: " + mediaFormat);
        return mediaFormat;
    }
    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {
                continue;
            }

            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }
    @Override
    void initMediaCodec(MediaFormat format) throws IOException {
        super.initMediaCodec(format);
        mEncoder = createEncoder("video/avc");
        mEncoder.setCallback(mCodecCallback);
//        mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        surface = mEncoder.createInputSurface();
    }

    private MediaCodec.Callback mCodecCallback = new MediaCodec.Callback() {
        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
            callback.onInputBufferAvailable(index);
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
            callback.onOutputBufferAvailable(index, info);
        }

        @Override
        public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
            callback.onError(e);
        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
            callback.onOutputFormatChanged(format);
        }
    };
}
