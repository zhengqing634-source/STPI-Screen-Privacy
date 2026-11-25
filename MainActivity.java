package com.stpi.screenprivacy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.media.projection.MediaProjectionConfig;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final int REQUEST_APP_ONLY_PICKER = 4107;
    private TextView status;

    private int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView text(String value, float size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(0xFF18212F);
        t.setPadding(0, dp(8), 0, dp(8));
        return t;
    }

    private Button button(String label, Runnable action) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setOnClickListener(v -> action.run());
        return b;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(24), dp(20), dp(24));

        content.addView(text("STPI Screen Privacy", 26));
        content.addView(text("Android 17 · 隐私辅助工具", 14));
        content.addView(text(
            "本工具不接管其他应用的屏幕共享，也不修改学习通权限。"
            + "它可以打开 Android 系统的“仅选择应用窗口”共享选择流程，"
            + "供你验证设备是否支持单应用共享。",
            16
        ));

        status = text("状态：尚未启动系统选择器。", 15);
        content.addView(status);

        content.addView(button("打开系统单应用共享选择器", this::openAppOnlyPicker));
        content.addView(button("打开系统隐私设置", () -> {
            try {
                startActivity(new Intent(Settings.ACTION_PRIVACY_SETTINGS));
            } catch (Exception e) {
                status.setText("无法打开隐私设置：" + e.getMessage());
            }
        }));
        content.addView(button("如何停止当前共享", () -> new AlertDialog.Builder(this)
            .setTitle("停止屏幕共享")
            .setMessage("下拉通知栏，找到 Android 的屏幕共享/录制状态提示或状态栏共享指示，点击后选择停止。"
                + "如果设备没有显示该入口，请回到发起共享的应用结束共享。")
            .setPositiveButton("知道了", null)
            .show()));

        content.addView(text(
            "重要限制：此选择器只约束由 STPI 工具发起的选择流程，"
            + "不能强制学习通或其他第三方应用采用单应用共享。"
            + "若学习通自身提供系统共享选择器，请在系统界面中明确选择“应用窗口”，"
            + "不要选择“整个屏幕”。如果系统界面没有应用窗口选项，请取消共享。",
            14
        ));

        ScrollView scroll = new ScrollView(this);
        scroll.addView(content);
        setContentView(scroll);
    }

    private void openAppOnlyPicker() {
        if (Build.VERSION.SDK_INT < 37) {
            status.setText("当前系统 API 低于 37，无法使用本工具的 API 37 受限选择器。");
            return;
        }
        try {
            MediaProjectionManager manager =
                (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

            // This config is for sessions requested by THIS helper app only.
            // Enable app-window source and disable whole-display source.
            MediaProjectionConfig config = new MediaProjectionConfig.Builder()
                .setSourceEnabled(MediaProjectionConfig.PROJECTION_SOURCE_APP, true)
                .setSourceEnabled(MediaProjectionConfig.PROJECTION_SOURCE_DISPLAY, false)
                .build();

            Intent intent = manager.createScreenCaptureIntent(config);
            status.setText("已请求系统显示应用窗口选择器。请只选择目标应用窗口；若仍出现整屏选项，请取消。");
            startActivityForResult(intent, REQUEST_APP_ONLY_PICKER);
        } catch (Throwable t) {
            status.setText("系统不支持或调用失败：" + t.getClass().getSimpleName()
                + ": " + String.valueOf(t.getMessage()));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_APP_ONLY_PICKER) {
            if (resultCode == RESULT_OK) {
                status.setText("系统选择流程已返回授权结果。本工具不会读取或转发屏幕内容；"
                    + "这不代表学习通的共享方式已被限制。");
            } else {
                status.setText("已取消系统选择流程，未启动本工具的屏幕采集。");
            }
        }
    }
}
