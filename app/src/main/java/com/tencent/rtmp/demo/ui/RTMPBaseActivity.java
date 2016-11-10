package com.tencent.rtmp.demo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.demo.R;

import java.text.SimpleDateFormat;

public class RTMPBaseActivity extends Fragment {
    private static final String TAG = RTMPBaseActivity.class.getSimpleName();
    static final int ACTIVITY_TYPE_PUBLISH      = 1;
    static final int ACTIVITY_TYPE_LIVE_PLAY    = 2;
    static final int ACTIVITY_TYPE_VOD_PLAY     = 3;

    protected EditText    mRtmpUrlView;
    public TextView       mLogViewStatus;
    public TextView       mLogViewEvent;
    protected int         mActivityType;

    StringBuffer          mLogMsg = new StringBuffer("");
    private final int mLogMsgLenLimit = 3000;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 100 || data ==null || data.getExtras() == null || TextUtils.isEmpty(data.getExtras().getString("result"))) {
            return;
        }
        String result = data.getExtras().getString("result");
        if (mRtmpUrlView != null) {
            mRtmpUrlView.setText(result);
        }
    }

    protected void initView(View view) {
        mRtmpUrlView   = (EditText) getActivity().findViewById(R.id.rtmpUrl);
        mLogViewEvent  = (TextView) view.findViewById(R.id.logViewEvent);
        mLogViewStatus = (TextView) view.findViewById(R.id.logViewStatus);

        Button scanBtn = (Button)getActivity().findViewById(R.id.btnScan);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(RTMPBaseActivity.this.getActivity(), QRCodeScanActivity.class);
//                startActivityForResult(intent, 100);
            }
        });
        scanBtn.setEnabled(true);
    }

    // 重载android标准实现函数
    protected void enableQRCodeBtn(boolean bEnable) {
        //disable qrcode
        if (getActivity() != null) {
            Button btnScan = (Button) getActivity().findViewById(R.id.btnScan);
            if (btnScan != null) {
                btnScan.setEnabled(bEnable);
            }
        }
    }

    //公用打印辅助函数
    protected void appendEventLog(int event, String message) {
        String str = "receive event: " + event + ", " + message;
        Log.d(TAG, str);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        String date = sdf.format(System.currentTimeMillis());
        while(mLogMsg.length() >mLogMsgLenLimit ){
            int idx = mLogMsg.indexOf("\n");
            if (idx == 0)
                idx = 1;
            mLogMsg = mLogMsg.delete(0,idx);
        }
        mLogMsg = mLogMsg.append("\n" + "["+date+"]" + message);
    }

    //公用打印辅助函数
    protected String getNetStatusString(Bundle status) {
        String str = String.format("%-14s %-14s %-12s\n%-14s %-14s %-12s\n%-14s %-14s %-12s\n%-14s",
                "CPU:"+status.getString(TXLiveConstants.NET_STATUS_CPU_USAGE),
                "RES:"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH)+"*"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT),
                "SPD:"+status.getInt(TXLiveConstants.NET_STATUS_NET_SPEED)+"Kbps",
                "JIT:"+status.getInt(TXLiveConstants.NET_STATUS_NET_JITTER),
                "FPS:"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_FPS),
                "ARA:"+status.getInt(TXLiveConstants.NET_STATUS_AUDIO_BITRATE)+"Kbps",
                "QUE:"+status.getInt(TXLiveConstants.NET_STATUS_CODEC_CACHE)+"|"+status.getInt(TXLiveConstants.NET_STATUS_CACHE_SIZE),
                "DRP:"+status.getInt(TXLiveConstants.NET_STATUS_CODEC_DROP_CNT)+"|"+status.getInt(TXLiveConstants.NET_STATUS_DROP_SIZE),
                "VRA:"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_BITRATE)+"Kbps",
                "SVR:"+status.getString(TXLiveConstants.NET_STATUS_SERVER_IP));
        return str;
    }

    protected void clearLog() {
        mLogMsg.setLength(0);
        mLogViewEvent.setText("");
        mLogViewStatus.setText("");
    }



    /**
     * 实现EVENT VIEW的滚动显示
     * @param scroll
     * @param inner
     */
    public static void scroll2Bottom(final ScrollView scroll, final View inner) {
        if (scroll == null || inner == null) {
            return;
        }
        int offset = inner.getMeasuredHeight() - scroll.getMeasuredHeight();
        if (offset < 0) {
            offset = 0;
        }
        scroll.scrollTo(0, offset);
    }

    public void setActivityType(int type) {
        mActivityType = type;
    }
}
