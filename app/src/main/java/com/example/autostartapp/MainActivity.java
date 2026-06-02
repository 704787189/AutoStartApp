package com.example.autostartapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 主界面
 * 首次启动可设置倒计时时间，之后使用保存的时间
 */
public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "AutoStartAppPrefs";
    private static final String KEY_COUNTDOWN = "countdown_seconds";
    private static final String KEY_FIRST_RUN = "first_run";
    private static final int DEFAULT_COUNTDOWN = 10;

    // 目标应用信息
    private static final String TARGET_PACKAGE = "com.hinacom.miviewzfp";
    private static final String TARGET_ACTIVITY = "com.example.miviewzfp.MainActivity";

    private SharedPreferences prefs;
    private TextView tvCountdown;
    private TextView tvSettingLabel;
    private EditText etCountdown;
    private Button btnSave;
    private View layoutSettings;
    private View layoutCountdown;

    private int countdownSeconds;
    private int currentCountdown;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        initViews();

        boolean isFirstRun = prefs.getBoolean(KEY_FIRST_RUN, true);

        if (isFirstRun) {
            // 首次启动：显示设置界面
            showSettingsView();
        } else {
            // 非首次启动：读取保存的时间并开始倒计时
            countdownSeconds = prefs.getInt(KEY_COUNTDOWN, DEFAULT_COUNTDOWN);
            showCountdownView();
            startCountdown();
        }
    }

    private void initViews() {
        tvCountdown = findViewById(R.id.tv_countdown);
        tvSettingLabel = findViewById(R.id.tv_setting_label);
        etCountdown = findViewById(R.id.et_countdown);
        btnSave = findViewById(R.id.btn_save);
        layoutSettings = findViewById(R.id.layout_settings);
        layoutCountdown = findViewById(R.id.layout_countdown);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                currentCountdown--;

                if (currentCountdown > 0) {
                    updateCountdownText(currentCountdown);
                    handler.sendEmptyMessageDelayed(1, 1000);
                } else {
                    updateCountdownText(0);
                    executeTargetApp();
                }
            }
        };
    }

    private void showSettingsView() {
        layoutSettings.setVisibility(View.VISIBLE);
        layoutCountdown.setVisibility(View.GONE);
        tvSettingLabel.setText("首次启动，请设置倒计时时间（秒）：");
        etCountdown.setText(String.valueOf(DEFAULT_COUNTDOWN));
    }

    private void showCountdownView() {
        layoutSettings.setVisibility(View.GONE);
        layoutCountdown.setVisibility(View.VISIBLE);
    }

    private void saveSettings() {
        String input = etCountdown.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(this, "请输入倒计时时间", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int seconds = Integer.parseInt(input);
            if (seconds < 1 || seconds > 300) {
                Toast.makeText(this, "请输入1-300之间的数字", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(KEY_COUNTDOWN, seconds);
            editor.putBoolean(KEY_FIRST_RUN, false);
            editor.apply();

            countdownSeconds = seconds;
            Toast.makeText(this, "设置已保存：" + seconds + "秒", Toast.LENGTH_SHORT).show();

            layoutSettings.setVisibility(View.GONE);
            showCountdownView();
            startCountdown();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的数字", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCountdown() {
        currentCountdown = countdownSeconds;
        updateCountdownText(currentCountdown);
        Log.d(TAG, "开始倒计时：" + countdownSeconds + "秒");

        if (currentCountdown > 0) {
            handler.sendEmptyMessageDelayed(1, 1000);
        } else {
            executeTargetApp();
        }
    }

    private void updateCountdownText(int seconds) {
        String text = "倒计时: " + seconds + " 秒";
        tvCountdown.setText(text);
        Log.d(TAG, text);
    }

    private void executeTargetApp() {
        Log.d(TAG, "倒计时结束，准备启动目标应用...");

        try {
            Intent launchIntent = new Intent(Intent.ACTION_MAIN);
            launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            launchIntent.setClassName(TARGET_PACKAGE, TARGET_ACTIVITY);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(launchIntent);

            Log.d(TAG, "已发送启动意图到: " + TARGET_PACKAGE + "/" + TARGET_ACTIVITY);
            tvCountdown.setText("已启动目标应用！");

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 2000);

        } catch (Exception e) {
            Log.e(TAG, "启动失败: " + e.getMessage());

            try {
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
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
