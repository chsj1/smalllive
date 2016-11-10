package com.tencent.rtmp.demo.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.rtmp.ITXLivePushListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.demo.R;
import com.tencent.rtmp.ui.TXCloudVideoView;

public class LivePublisherActivity extends RTMPBaseActivity implements View.OnClickListener, ITXLivePushListener, SeekBar.OnSeekBarChangeListener/*, ImageReader.OnImageAvailableListener*/ {
    private static final String TAG = LivePublisherActivity.class.getSimpleName();

    private TXLivePushConfig mLivePushConfig;
    private TXLivePusher mLivePusher;
    private TXCloudVideoView mCaptureView;

    private LinearLayout mFaceBeautyLayout;
    private SeekBar mBeautySeekBar;
    private SeekBar mWhiteningSeekBar;
    private ScrollView mScrollView;
    private RadioGroup mRadioGroupBitrate;
    private Button mBtnPlay;

    private boolean mVideoPublish;
    private int mBeautyLevel = 0;
    private int mWhiteningLevel = 0;

    private Handler mHandler = new Handler();

    private Bitmap mBitmap;

//    private final int           REQUEST_CODE_CS = 10001;

//    private MediaProjectionManager      mProjectionManager;
//    private MediaProjection             mMediaProjection;
//    private VirtualDisplay mVirtualDisplay         = null;
//    private ImageReader mImageReader            = null;

    private Bitmap decodeResource(Resources resources, int id) {
        TypedValue value = new TypedValue();
        resources.openRawResource(id, value);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inTargetDensity = value.density;
        return BitmapFactory.decodeResource(resources, id, opts);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLivePusher = new TXLivePusher(getActivity());
        mLivePushConfig = new TXLivePushConfig();

        mBitmap = decodeResource(getResources(), R.drawable.watermark);
    }

//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private void StartScreenCapture() {
//        if(mProjectionManager == null) mProjectionManager = (MediaProjectionManager) getActivity().getSystemService
//                (Context.MEDIA_PROJECTION_SERVICE);
//
//        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE_CS);
//    }
//
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private void StopScreenCapture() {
//        if(mVirtualDisplay != null) mVirtualDisplay.release();
//        if(mMediaProjection!= null) mMediaProjection.stop();
//        if (mImageReader != null) mImageReader.close();
//
//        mVirtualDisplay = null;
//        mMediaProjection = null;
//        mImageReader = null;
//    }
//
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode != REQUEST_CODE_CS) {
//            Log.e(TAG, "Unknown request code: " + requestCode);
//            return;
//        }
//        if (resultCode != Activity.RESULT_OK) {
//            Log.e(TAG, "Screen Cast Permission Denied, resultCode:" + resultCode);
//            return;
//        }
//
//        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
//
//        int W = 720, H = W/9*16;
//
//        mImageReader = ImageReader.newInstance(W, H, PixelFormat.RGBA_8888, 2);
//
//        Surface imageReaderSurface = null;
//        if(mImageReader != null)
//        {
//            mImageReader.setOnImageAvailableListener(this, null);
//            imageReaderSurface =  mImageReader.getSurface();
//        }
//
//        mVirtualDisplay = mMediaProjection.createVirtualDisplay("TXScreenCapture",
//                W, H, 1,
//                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
//        imageReaderSurface, null /*Callbacks*/, null
//                    /*Handler*/);
//
//        mVideoPublish = startPublishRtmp();
//    }
//
//    private byte [] mGetRGBA = null;
//
//    @Override
//    public void onImageAvailable(ImageReader var1)
//    {
//        Image image = var1.acquireNextImage();
//
//        if(image != null)
//        {
//            Image.Plane [] planes = image.getPlanes();
//
//            ByteBuffer rgbaPlane = planes[0].getBuffer();
//
//            if(mGetRGBA == null || mGetRGBA.length != rgbaPlane.remaining())
//            {
//                mGetRGBA = new byte[rgbaPlane.remaining()];
//            }
//            rgbaPlane.get(mGetRGBA);
//
//            if(mLivePusher != null) mLivePusher.sendCustomVideoData(mGetRGBA, TXLivePusher.RGB_RGBA, image.getWidth(), image.getHeight());
//
//            image.close();
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_publish, null);

        initView(view);
        mCaptureView = (TXCloudVideoView) view.findViewById(R.id.video_view);

        mVideoPublish = false;
        mLogViewStatus.setVisibility(View.GONE);
        mLogViewStatus.setMovementMethod(new ScrollingMovementMethod());
        mLogViewEvent.setMovementMethod(new ScrollingMovementMethod());
        mScrollView = (ScrollView) view.findViewById(R.id.scrollview);
        mScrollView.setVisibility(View.GONE);

        mRtmpUrlView.setHint(" 请扫码输入推流地址...");
        mRtmpUrlView.setText("");

        //美颜部分
        mFaceBeautyLayout = (LinearLayout) view.findViewById(R.id.layoutFaceBeauty);
        mBeautySeekBar = (SeekBar) view.findViewById(R.id.beauty_seekbar);
        mBeautySeekBar.setOnSeekBarChangeListener(this);

        mWhiteningSeekBar = (SeekBar) view.findViewById(R.id.whitening_seekbar);
        mWhiteningSeekBar.setOnSeekBarChangeListener(this);


        //播放部分
        mBtnPlay = (Button) view.findViewById(R.id.btnPlay);
        mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mVideoPublish) {
                    stopPublishRtmp();
                    mVideoPublish = false;
                } else {
                    FixOrAdjustBitrate();  //根据设置确定是“固定”还是“自动”码率
                    mVideoPublish = startPublishRtmp();
//                    StartScreenCapture();
                }
            }
        });







        mRadioGroupBitrate = (RadioGroup) view.findViewById(R.id.resolutionRadioGroup);
        mRadioGroupBitrate.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                FixOrAdjustBitrate();
            }
        });



        view.setOnClickListener(this);
        mLogViewStatus.setText("Log File Path:" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/txRtmpLog");
        return view;
    }

    protected void HWListConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LivePublisherActivity.this.getActivity());
        builder.setMessage("警告：当前机型不在白名单中,是否继续尝试硬编码？");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                throw new RuntimeException();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                throw new RuntimeException();
            }
        });
        builder.setCancelable(false);
        builder.create().show();
        try {
            Looper.loop();
        } catch (Exception e) {
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                mFaceBeautyLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mCaptureView != null) {
            mCaptureView.onResume();
        }

        if (mVideoPublish && mLivePusher != null) {
            mLivePusher.resumePusher();
            mLivePusher.startCameraPreview(mCaptureView);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mCaptureView != null) {
            mCaptureView.onPause();
        }

        if (mVideoPublish && mLivePusher != null) {
            mLivePusher.stopCameraPreview(false);
            mLivePusher.pausePusher();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPublishRtmp();
        if (mCaptureView != null) {
            mCaptureView.onDestroy();
        }
    }

    private boolean startPublishRtmp() {
        String rtmpUrl = "rtmp://2000.livepush.myqcloud.com/live/2000_44c6e64e79af11e69776e435c87f075e?bizid=2000";

        if (TextUtils.isEmpty(rtmpUrl) || (!rtmpUrl.trim().toLowerCase().startsWith("rtmp://"))) {
            mVideoPublish = false;
            Toast.makeText(getActivity().getApplicationContext(), "推流地址不合法，目前支持rtmp推流!", Toast.LENGTH_SHORT).show();
            return false;
        }

        mCaptureView.setVisibility(View.VISIBLE);
        mLivePushConfig.setWatermark(mBitmap, 10, 10);

        int customModeType = 0;

        mLivePushConfig.setVideoFPS(25);

        //【示例代码1】设置自定义视频采集逻辑 （自定义视频采集逻辑不要调用startPreview）
//        customModeType |= TXLiveConstants.CUSTOM_MODE_VIDEO_CAPTURE;
//        mLivePushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_1280_720);
//        mLivePushConfig.setAutoAdjustBitrate(false);
//        mLivePushConfig.setVideoBitrate(1300);
//        mLivePushConfig.setVideoFPS(25);
//        mLivePushConfig.setVideoEncodeGop(3);
//        new Thread() {  //视频采集线程
//            @Override
//            public void run() {
//                while (true) {
//                    try {
//                        FileInputStream in = new FileInputStream("/sdcard/dump_1280_720.yuv");
//                        int len = 1280 * 720 * 3 / 2;
//                        byte buffer[] = new byte[len];
//                        int count;
//                        while ((count = in.read(buffer)) != -1) {
//                            if (len == count) {
//                                mLivePusher.sendCustomVideoData(buffer, TXLivePusher.YUV_420SP);
//                            } else {
//                                break;
//                            }
//                            sleep(50, 0);
//                        }
//                        in.close();
//                    } catch (Exception e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }.start();

        //【示例代码2】设置自定义音频采集逻辑（音频采样位宽必须是16）
//        mLivePushConfig.setAudioSampleRate(44100);
//        mLivePushConfig.setAudioChannels(1);
//        customModeType |= TXLiveConstants.CUSTOM_MODE_AUDIO_CAPTURE;
//        new Thread() {  //音频采集线程
//            @Override
//            public void run() {
//                while (true) {
//                    try {
//                        FileInputStream in = new FileInputStream("/sdcard/dump.pcm");
//                        int len = 2048;
//                        byte buffer[] = new byte[len];
//                        int count;
//                        while ((count = in.read(buffer)) != -1) {
//                            if (len == count) {
//                                mLivePusher.sendCustomPCMData(buffer);
//                            } else {
//                                break;
//                            }
//                            sleep(10, 0);
//                        }
//                        in.close();
//                    } catch (Exception e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }.start();

        //【示例代码3】设置自定义视频预处理逻辑
//        customModeType |= TXLiveConstants.CUSTOM_MODE_VIDEO_PREPROCESS;
//        String path = this.getActivity().getApplicationInfo().dataDir + "/lib";
//        mLivePushConfig.setCustomVideoPreProcessLibrary(path +"/libvideo.so", "tx_video_process");

        //【示例代码4】设置自定义音频预处理逻辑
//        customModeType |= TXLiveConstants.CUSTOM_MODE_AUDIO_PREPROCESS;
//        String path = this.getActivity().getApplicationInfo().dataDir + "/lib";
//        mLivePushConfig.setCustomAudioPreProcessLibrary(path +"/libvideo.so", "tx_audio_process");


        mLivePushConfig.setCustomModeType(customModeType);

        mLivePushConfig.setPauseImg(300, 10);
        Bitmap bitmap = decodeResource(getResources(), R.drawable.pause_publish);
        ;
        mLivePushConfig.setPauseImg(bitmap);
        mLivePusher.setConfig(mLivePushConfig);
        mLivePusher.setPushListener(this);
        mLivePusher.startCameraPreview(mCaptureView);
//        mLivePusher.startScreenCapture();
        mLivePusher.startPusher(rtmpUrl.trim());
        mLivePusher.setLogLevel(TXLiveConstants.LOG_LEVEL_DEBUG);

        enableQRCodeBtn(false);
        clearLog();
        int[] ver = TXLivePusher.getSDKVersion();
        if (ver != null && ver.length >= 3) {
            mLogMsg.append(String.format("rtmp sdk version:%d.%d.%d ", ver[0], ver[1], ver[2]));
            mLogViewEvent.setText(mLogMsg);
        }

        mBtnPlay.setBackgroundResource(R.drawable.play_pause);

        appendEventLog(0, "点击推流按钮！");

        return true;
    }

    private void stopPublishRtmp() {

//        StopScreenCapture();

        mLivePusher.stopCameraPreview(true);
        mLivePusher.stopScreenCapture();
        mLivePusher.setPushListener(null);
        mLivePusher.stopPusher();
        mCaptureView.setVisibility(View.GONE);

        enableQRCodeBtn(true);
        mBtnPlay.setBackgroundResource(R.drawable.play_start);

        if (mLivePushConfig != null) {
            mLivePushConfig.setPauseImg(null);
        }
    }


    public void FixOrAdjustBitrate() {
        if (mRadioGroupBitrate == null || mLivePushConfig == null || mLivePusher == null) {
            return;
        }

        RadioButton rb = (RadioButton) getActivity().findViewById(mRadioGroupBitrate.getCheckedRadioButtonId());
        int mode = Integer.parseInt((String) rb.getTag());

        switch (mode) {
            case 4: /*720p*/
                if (mLivePusher != null) {
                    mLivePushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_720_1280);
                    mLivePushConfig.setAutoAdjustBitrate(false);
                    mLivePushConfig.setVideoBitrate(1500);
                    mLivePusher.setConfig(mLivePushConfig);
                }
                break;
            case 3: /*540p*/
                if (mLivePusher != null) {
                    mLivePushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_540_960);
                    mLivePushConfig.setAutoAdjustBitrate(false);
                    mLivePushConfig.setVideoBitrate(1000);
                    mLivePusher.setConfig(mLivePushConfig);
                }
                break;
            case 2: /*360p*/
                if (mLivePusher != null) {
                    mLivePushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_360_640);
                    mLivePushConfig.setAutoAdjustBitrate(false);
                    mLivePushConfig.setVideoBitrate(700);
                    mLivePusher.setConfig(mLivePushConfig);
                }
                break;

            case 1: /*自动*/
                if (mLivePusher != null) {
                    mLivePushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_360_640);
                    mLivePushConfig.setAutoAdjustBitrate(true);
                    mLivePushConfig.setMaxVideoBitrate(1000);
                    mLivePushConfig.setMinVideoBitrate(500);
                    mLivePushConfig.setVideoBitrate(700);
                    mLivePusher.setConfig(mLivePushConfig);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onPushEvent(int event, Bundle param) {
        String msg = param.getString(TXLiveConstants.EVT_DESCRIPTION);
        appendEventLog(event, msg);
        if (mScrollView.getVisibility() == View.VISIBLE) {
            mLogViewEvent.setText(mLogMsg);
            scroll2Bottom(mScrollView, mLogViewEvent);
        }
//        if (mLivePusher != null) {
//            mLivePusher.onLogRecord("[event:" + event + "]" + msg + "\n");
//        }
        //错误还是要明确的报一下
        if (event < 0) {
            Toast.makeText(getActivity().getApplicationContext(), param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
        }

        if (event == TXLiveConstants.PUSH_ERR_NET_DISCONNECT) {
            stopPublishRtmp();
            mVideoPublish = false;
        } else if (event == TXLiveConstants.PUSH_WARNING_HW_ACCELERATION_FAIL) {
            Toast.makeText(getActivity().getApplicationContext(), param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
            mLivePushConfig.setHardwareAcceleration(false);
            mLivePusher.setConfig(mLivePushConfig);
        } else if (event == TXLiveConstants.PUSH_ERR_SCREEN_CAPTURE_UNSURPORT) {
            stopPublishRtmp();
        } else if (event == TXLiveConstants.PUSH_ERR_SCREEN_CAPTURE_START_FAILED) {
            stopPublishRtmp();
        }
    }

    @Override
    public void onNetStatus(Bundle status) {
        String str = getNetStatusString(status);
        mLogViewStatus.setText(str);
        Log.d(TAG, "Current status: " + status.toString());
//        if (mLivePusher != null){
//            mLivePusher.onLogRecord("[net state]:\n"+str+"\n");
//        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == R.id.beauty_seekbar) {
            mBeautyLevel = progress;
        } else if (seekBar.getId() == R.id.whitening_seekbar) {
            mWhiteningLevel = progress;
        }

        if (mLivePusher != null) {
            if (!mLivePusher.setBeautyFilter(mBeautyLevel, mWhiteningLevel)) {
                Toast.makeText(getActivity().getApplicationContext(), "当前机型的性能无法支持美颜功能", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //如果您允许了横竖屏切换，请在切换时重启一下摄像头预览，以便SDK可以计算到正确的渲染角度
        //mLivePusher.stopCameraPreview(false);
        //mLivePusher.startCameraPreview(mCaptureView);
    }
}