package com.example.scankitdemo.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.huawei.hms.hmsscankit.ScanKitActivity;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;

import java.io.Serializable;
import java.util.Map;

public class CodeScanHuaweiActivity extends ScanKitActivity {

    private static final String TAG = "CodeScanHuaweiActivity";

    private static final String keyOfScanTitle = "ScanTitle";

    private ScanCallback myCallback = null;

    private ActivityCustomizer myActivityCustomizer = null;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        myCallback = CodeScanHuaweiActivity.currentCallback;
        myActivityCustomizer = ActivityCustomizer.pop();

        // adb shell uiautomator dump /sdcard/window_dump.xml ; adb pull /sdcard/window_dump.xml  ./

        Intent intent = getIntent();

        // 标题文字
        String scanTitle = "请对准车架条形码，耐心等待";
        String title = intent.getStringExtra(keyOfScanTitle);
        if (title != null) {
            scanTitle = title;
        }

        // 隐藏选择图片的按钮
        // View ll_top = findViewById(com.huawei.hms.scankit.R.id.ll_top);
        View gallery_Layout = findViewById(com.huawei.hms.scankit.R.id.gallery_Layout);
        if (gallery_Layout != null) {
            gallery_Layout.setVisibility(View.GONE);
            // ((ViewGroup)gallery_Layout.getParent()).removeView(gallery_Layout);
        }

        // 主标题改一下文案
        View title_scan = findViewById(com.huawei.hms.scankit.R.id.title_scan);
        if (title_scan instanceof TextView) {
            TextView titleScan = (TextView) title_scan;
            titleScan.setTextSize(16);
            titleScan.setText(scanTitle);
        }
        // 副标题隐藏
        View title_scan_level_two = findViewById(com.huawei.hms.scankit.R.id.title_scan_level_two);
        if (title_scan_level_two instanceof TextView) {
            title_scan_level_two.setVisibility(View.GONE);
        }

        // 手电筒图标显示出来
        View flash_light_ll = findViewById(com.huawei.hms.scankit.R.id.flash_light_ll);
        flash_light_ll.setVisibility(View.VISIBLE);
        // 手电筒图标需要改变的话，这里需要修改图标
        View ivFlash = findViewById(com.huawei.hms.scankit.R.id.ivFlash);
        if (ivFlash instanceof ImageView) {
            // nothing now ...
        }
        View flash_light_text = findViewById(com.huawei.hms.scankit.R.id.flash_light_text);
        flash_light_text.setVisibility(View.GONE);

        if (myActivityCustomizer != null) myActivityCustomizer.onCreate(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (myActivityCustomizer != null) myActivityCustomizer.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (myActivityCustomizer != null) myActivityCustomizer.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myCallback != null && !myCallback.isInvoked()) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                myCallback.doInvoke(true, null);
                myCallback = null;
            }, 100);
        } else {
            myCallback = null;
        }
        if (myActivityCustomizer != null) myActivityCustomizer.onDestroy(this);
    }

    /// ----------------------------- Static members -----------------------------

    /**
     * 扫码结果回调接口
     */
    public static abstract class ScanCallback {
        private boolean isInvoked = false;

        public boolean isInvoked() {
            return isInvoked;
        }

        public void doInvoke(boolean isCancel, @Nullable String result) {
            if (isInvoked) return;
            isInvoked = true;
            onScanResult(isCancel, result);
        }

        protected abstract void onScanResult(boolean isCancel, @Nullable String result);
    }

    // 扫码页面结果请求码
    public static final int kRequestCode = 177135;

    // 防止短时间内多次启动扫码页面
    private static long lastStartTime = 0;

    // 当前扫码结果回调
    private static ScanCallback currentCallback = null;

    public static void invokeCurrentCallback(@Nullable String result) {
        if (currentCallback != null) {
            currentCallback.doInvoke(false, result);
            currentCallback = null;
        }
    }

    /**
     * 处理扫码页面返回结果: 在调用扫码页面的 Activity 的 onActivityResult 方法中调用此方法, 目前由 ReactNative 和 Flutter 的 Activity 接收
     */
    public static void onActivityResult(@Nullable Activity activity, int requestCode, int resultCode, @Nullable Intent data) {
        Log.i(TAG, "On activity result from " + "activity: " + activity + ", requestCode:" + requestCode + ", resultCode:" + resultCode + ", data:" + data);
        if (requestCode == CodeScanHuaweiActivity.kRequestCode) {
            HmsScan obj = data != null ? data.getParcelableExtra(ScanUtil.RESULT) : null;
            CodeScanHuaweiActivity.invokeCurrentCallback(obj != null ? obj.getShowResult() : null);
        }
        lastStartTime = 0;
    }


    /**
     * 启动扫码页面
     */
    public static void start(@NonNull Activity activity, @Nullable ScanCallback callback) {
        start(activity, null, null, callback);
    }

    public static void start(@NonNull Activity activity, @Nullable Map<?, ?> parameters, @Nullable ScanCallback callback) {
        start(activity, parameters, null, callback);
    }

    public static void start(
            @NonNull Activity activity,
            @Nullable Map<?, ?> parameters,
            @Nullable ActivityCustomizer customizer,
            @Nullable ScanCallback callback
    ) {
        // 限制 500ms 内不能多次调用
        long currentTimeMillis = System.currentTimeMillis();
        long millisTimes = currentTimeMillis - lastStartTime;
        if (millisTimes < 500) {
            Log.i(TAG, "Too many start in a short time, ignore this call. " + millisTimes + " ms since last call.");
            return;
        }
        lastStartTime = currentTimeMillis;

        currentCallback = callback;
        Intent intent = new Intent(activity, CodeScanHuaweiActivity.class);

        // intent.putExtra("ScanGuide", true);

        try {
            // 传递参数
            if (parameters != null) {
                Object tips = parameters.get("tips");
                if (tips == null) tips = parameters.get("tip");
                if (tips != null) intent.putExtra(keyOfScanTitle, String.valueOf(tips));

                // 其他参数透传
                for (Object key : parameters.keySet()) {
                    Object value = parameters.get(key);
                    if (value instanceof Serializable) intent.putExtra(String.valueOf(key), (Serializable) value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Handling parameters error: " + e);
        }

        // 传递Activity实例定制者
        ActivityCustomizer.clear();
        if (customizer != null) {
            ActivityCustomizer.push(customizer);
        }

        activity.startActivityForResult(intent, kRequestCode);
    }


}
