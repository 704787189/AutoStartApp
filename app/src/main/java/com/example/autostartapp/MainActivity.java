package com.example.autostartapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "AutoStartAppPrefs";
    private static final String KEY_COUNTDOWN = "countdown_seconds";
    private static final String KEY_FIRST_RUN = "first_run";
    private static final String KEY_TARGET_PACKAGE = "target_package";
    private static final String KEY_TARGET_ACTIVITY = "target_activity";
    private static final int DEFAULT_COUNTDOWN = 10;

    private String targetPackage = "";
    private String targetActivity = "";

    private SharedPreferences prefs;
    private TextView tvCountdown;
    private TextView tvSettingLabel;
    private EditText etCountdown;
    private Button btnSave;
    private View layoutSettings;
    private View layoutAppSelect;
    private View layoutCountdown;
    private ListView lvApps;

    private int countdownSeconds;
    private int currentCountdown;
    private Handler handler;

    private List<AppInfo> appList;
    private AppListAdapter appAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        initViews();

        boolean isFirstRun = prefs.getBoolean(KEY_FIRST_RUN, true);

        if (isFirstRun) {
            showSettingsView();
        } else {
            countdownSeconds = prefs.getInt(KEY_COUNTDOWN, DEFAULT_COUNTDOWN);
            targetPackage = prefs.getString(KEY_TARGET_PACKAGE, "");
            targetActivity = prefs.getString(KEY_TARGET_ACTIVITY, "");
            
            if (!targetPackage.isEmpty() && !targetActivity.isEmpty()) {
                showCountdownView();
                startCountdown();
            } else {
                showSettingsView();
            }
        }
    }

    private void initViews() {
        tvCountdown = (TextView) findViewById(R.id.tv_countdown);
        tvSettingLabel = (TextView) findViewById(R.id.tv_setting_label);
        etCountdown = (EditText) findViewById(R.id.et_countdown);
        btnSave = (Button) findViewById(R.id.btn_save);
        layoutSettings = findViewById(R.id.layout_settings);
        layoutAppSelect = findViewById(R.id.layout_app_select);
        layoutCountdown = findViewById(R.id.layout_countdown);
        lvApps = (ListView) findViewById(R.id.lv_apps);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettingsAndShowAppList();
            }
        });

        lvApps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AppInfo selectedApp = appList.get(position);
                targetPackage = selectedApp.getPackageName();
                targetActivity = selectedApp.getLaunchActivity();
                saveAppSelectionAndStartCountdown();
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
        layoutAppSelect.setVisibility(View.GONE);
        layoutCountdown.setVisibility(View.GONE);
        tvSettingLabel.setText("首次启动，请设置倒计时时间（秒）：");
        etCountdown.setText(String.valueOf(DEFAULT_COUNTDOWN));
    }

    private void showAppSelectView() {
        layoutSettings.setVisibility(View.GONE);
        layoutAppSelect.setVisibility(View.VISIBLE);
        layoutCountdown.setVisibility(View.GONE);
        
        loadAppList();
    }

    private void showCountdownView() {
        layoutSettings.setVisibility(View.GONE);
        layoutAppSelect.setVisibility(View.GONE);
        layoutCountdown.setVisibility(View.VISIBLE);
    }

    private void loadAppList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                appList = getAllInstalledApps();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        appAdapter = new AppListAdapter(MainActivity.this, appList);
                        lvApps.setAdapter(appAdapter);
                    }
                });
            }
        }).start();
    }

    private List<AppInfo> getAllInstalledApps() {
        List<AppInfo> apps = new ArrayList<>();
        PackageManager pm = getPackageManager();
        
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        
        for (ResolveInfo info : resolveInfos) {
            AppInfo appInfo = new AppInfo();
            try {
                appInfo.setAppName(info.loadLabel(pm).toString());
                appInfo.setPackageName(info.activityInfo.packageName);
                appInfo.setLaunchActivity(info.activityInfo.name);
                appInfo.setAppIcon(info.loadIcon(pm));
                apps.add(appInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        Collections.sort(apps, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
                return o1.getAppName().compareToIgnoreCase(o2.getAppName());
            }
        });
        
        return apps;
    }

    private void saveSettingsAndShowAppList() {
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
            editor.apply();

            countdownSeconds = seconds;
            Toast.makeText(this, "倒计时已设置为：" + seconds + "秒", Toast.LENGTH_SHORT).show();

            showAppSelectView();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的数字", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveAppSelectionAndStartCountdown() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TARGET_PACKAGE, targetPackage);
        editor.putString(KEY_TARGET_ACTIVITY, targetActivity);
        editor.putBoolean(KEY_FIRST_RUN, false);
        editor.apply();

        Toast.makeText(this, "已选择应用，开始倒计时！", Toast.LENGTH_SHORT).show();
        showCountdownView();
        startCountdown();
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

    private void killTargetApp() {
        if (targetPackage.isEmpty()) {
            Log.e(TAG, "目标应用包名未设置");
            return;
        }
        
        Log.d(TAG, "正在杀掉目标应用进程...");
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        
        try {
            am.killBackgroundProcesses(targetPackage);
            Log.d(TAG, "已使用 killBackgroundProcesses 尝试结束: " + targetPackage);
            
            try {
                String cmd = "am force-stop " + targetPackage;
                Log.d(TAG, "尝试执行命令: " + cmd);
                Runtime.getRuntime().exec(cmd);
                Log.d(TAG, "已通过 am force-stop 命令尝试结束: " + targetPackage);
            } catch (Exception e) {
                Log.w(TAG, "am force-stop 命令执行失败 (这是正常的，普通应用通常没有此权限): " + e.getMessage());
            }
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "杀进程失败: " + e.getMessage());
        }
    }

    private void executeTargetApp() {
        if (targetPackage.isEmpty() || targetActivity.isEmpty()) {
            Log.e(TAG, "目标应用未设置");
            tvCountdown.setText("目标应用未设置！");
            return;
        }
        
        Log.d(TAG, "倒计时结束，先杀进程再启动目标应用...");
        tvCountdown.setText("正在重启目标应用...");

        killTargetApp();

        try {
            Intent launchIntent = new Intent(Intent.ACTION_MAIN);
            launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            launchIntent.setClassName(targetPackage, targetActivity);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(launchIntent);

            Log.d(TAG, "已发送启动意图到: " + targetPackage + "/" + targetActivity);
            tvCountdown.setText("已重启目标应用！");

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 2000);

        } catch (Exception e) {
            Log.e(TAG, "启动失败: " + e.getMessage());

            try {
                String cmd = "am start -S -n " + targetPackage + "/" + targetActivity;
                Log.d(TAG, "尝试执行命令: " + cmd);
                Runtime.getRuntime().exec(cmd);
                tvCountdown.setText("已通过am命令重启！");
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
