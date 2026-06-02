package com.example.autostartapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
/**
 * 开机广播接收器
 * 收到BOOT_COMPLETED广播后启动MainActivity
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || "android.intent.action.QUICKBOOT_POWERON".equals(action)) {

            // 启动MainActivity，MainActivity中会执行倒计时
            Intent mainIntent = new Intent(context, MainActivity.class);
            // 使用NEW_TASK标志，确保在新任务栈中启动
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainIntent);
        }
    }
}
