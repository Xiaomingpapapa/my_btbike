package com.example.myapplication;

import android.app.Service;
import android.bluetooth.BluetoothClass;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by 傻明也有春天 on 2016/4/24.
 */
public class BluetoothChat extends Service{
  int count = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
    private class MyRunnable implements Runnable{

        @Override
        public void run() {
            Intent intent = new Intent();
            intent.setAction("com.android.myService");
            count = count+1;
            intent.putExtra("Count",count);
            sendBroadcast(intent);



        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
