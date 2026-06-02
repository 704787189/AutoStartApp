package com.example.autostartapp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

/**
 * 主界面
 * 倒计时10秒后启动目标应用
 */
public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    // 目标应用信息
    private static final String TARGET_PACKAGE = "com.hinacom.miviewzfp";
    private static final String TARGET_ACTIVITY = "com.example.miviewzfp.MainActivity";

    // 倒计时参数
    private static final int COUNTDOWN_SECONDS = 10;
    private static final int DELAY_SLEEP_SECONDS = 5;

    private TextView tvCountdown;
    private int currentCountdown = COUNTDOWN_SECONDS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvCountdown = (TextView) findViewById(R.id.tv_countdown);

        Log.d(TAG, "APP启动，开始倒计时...");

        // 开始倒计时
        startCountdown();
    }

    private void startCountdown() {
        // 立即显示初始值
        updateCountdownText(currentCountdown);

        // 使用Handler每1秒更新一次
        handler.sendEmptyMessageDelayed(1, 1000);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            currentCountdown--;

            if (currentCountdown > 0) {
                updateCountdownText(currentCountdown);
                // 1秒后再检查
                handler.sendEmptyMessageDelayed(1, 1000);
            } else {
                // 倒计时结束，执行目标应用启动
                updateCountdownText(0);
                executeTargetApp();
            }
        }
    };

    private void updateCountdownText(int seconds) {
        String text = "倒计时: " + seconds + " 秒";
        tvCountdown.setText(text);
        Log.d(TAG, text);
    }

    private void executeTargetApp() {
        Log.d(TAG, "倒计时结束，准备启动目标应用...");

        try {
            // 构建启动Intent
            Intent launchIntent = new Intent(Intent.ACTION_MAIN);
            launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            launchIntent.setClassName(TARGET_PACKAGE, TARGET_ACTIVITY);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // 先尝试直接启动
            startActivity(launchIntent);

            Log.d(TAG, "已发送启动意图到: " + TARGET_PACKAGE + "/" + TARGET_ACTIVITY);

            // 显示提示
            tvCountdown.setText("已启动目标应用！");

            // 2秒后关闭本APP
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 2000);

        } catch (Exception e) {
            Log.e(TAG, "启动失败: " + e.getMessage());

            // 如果直接启动失败，尝试使用am命令
            try {
                // 构造am命令
                String cmd = "am start -S -n " + TARGET_PACKAGE + "/" + TARGET_ACTIVITY;
                Log.d(TAG, "尝试执行命令: " + cmd);

                Runtime.getRuntime().exec(cmd);
                tvCountdown.setText("已通过am命令启动！");

            } catch (Exception e2) {
                Log.e(TAG, "am命令执行失败: " + e2.getMessage());
                tvCountdown.setText("启动失败: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
