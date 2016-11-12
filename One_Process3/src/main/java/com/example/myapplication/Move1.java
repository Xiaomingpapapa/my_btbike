package com.example.myapplication;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.myapplication.utils.Config;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 傻明也有春天 on 2016/7/29.
 */
public class Move1 extends Activity {
    Button btn_start;
    ImageView img1;
    ImageView img2;
    ImageView img3;
    ImageView img_people2;
    SeekBar seekBar;
    ObjectAnimator objectAnimator;
    ObjectAnimator objectAnimator1;
    ObjectAnimator objectAnimator2;
    myBtnClick btnClick;
    AlertDialog.Builder builder;
    AlertDialog.Builder builder2;
    Timer timer;
    int distance = 0;
    final static String ACTION_NAME = null;
    int speed;
    myReceiver receiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.move1);
        init();
        setBtnClick();
        setAnimator();
        receiver = new myReceiver();
        register();




    }

    private void init() {
        btn_start = (Button) findViewById(R.id.btn_start);
        btnClick = new myBtnClick();
        img1 = (ImageView) findViewById(R.id.img1);
        img2 = (ImageView) findViewById(R.id.img2);
        img3 = (ImageView) findViewById(R.id.img3);
        img_people2 = (ImageView) findViewById(R.id.img_people2);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        builder = new AlertDialog.Builder(Move1.this);
        builder.setTitle("确认");
        builder.setMessage("游戏失败");
        builder.setPositiveButton("重新游戏", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                objectAnimator.start();
                objectAnimator1.start();
                objectAnimator2.start();
                seekBar.setProgress(0);
                timer = new Timer();
                setTimer(timer);

            }
        });
        builder.setNegativeButton("退出游戏", null);
        builder2 = new AlertDialog.Builder(Move1.this);
        builder2.setTitle("确认");
        builder2.setMessage("游戏成功");
        builder2.setPositiveButton("继续游戏", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                objectAnimator.start();
                objectAnimator1.start();
                objectAnimator2.start();
                seekBar.setProgress(0);
                timer = new Timer();
                setTimer(timer);
            }
        });
        builder2.setNegativeButton("取消", null);
    }

    private void setBtnClick() {
        btn_start.setOnClickListener(btnClick);
    }

    private class myBtnClick implements View.OnClickListener {

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_start: {
                    objectAnimator.start();
                    objectAnimator1.start();
                    objectAnimator2.start();
                    seekBar.setProgress(0);
                    timer = new Timer();
                    setTimer(timer);
                    break;
                }

            }
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x112: {
                    timer.cancel();
                    stopAnimator();
                    builder.show();
                    break;
                }
                case 0x113: {
                    timer.cancel();
                    stopAnimator();
                    builder2.show();
                    break;
                }
                case 0x114: {
                    int speed = (int) msg.obj;
                    if (speed != 0) {
                        distance = distance + speed; //此处到底除以几合适还得调试才知道
                        seekBar.setProgress(distance);
                    }
                    break;
                }


            }
        }
    };

    private void setAnimator() {
        objectAnimator = ObjectAnimator.ofFloat(img1, "y", 0f, 250f);
        objectAnimator1 = ObjectAnimator.ofFloat(img2, "y", 0f, 250f);
        objectAnimator2 = ObjectAnimator.ofFloat(img3, "y", 0f, 250f);
        objectAnimator1.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator2.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.setDuration(7000);
        objectAnimator1.setDuration(4000);
        objectAnimator2.setDuration(4000);


    }

    private void setTimer(Timer timer) {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if ((img1.getY() >= 245f && seekBar.getProgress() <= 22) || (img1.getY() >= 245f && Math.abs(seekBar.getProgress() - 22) <= 2) || (img2.getY() >= 245f && Math.abs(seekBar.getProgress() - 53) <= 2) || (img3.getY() >= 365f && Math.abs(seekBar.getProgress() - 84) <= 2)) {
                    Message message = Message.obtain();
                    message.what = 0x112;
                    handler.sendMessage(message);
                }
                if ((seekBar.getProgress() == 100)) {
                    Message message = Message.obtain();
                    message.what = 0x113;
                    handler.sendMessage(message);
                }

            }
        };
        timer.schedule(timerTask, 0, 100);

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void stopAnimator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            objectAnimator.pause();
        }
        objectAnimator1.pause();
        objectAnimator2.pause();

    }

    @Override
    protected void onResume() {
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onResume();
    }


    class myReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Config.MY_ACTION.equals(action)) {
                Log.v("msg2","接收");
                int speed = intent.getIntExtra(Config.key_broadcast_speed, 0);
                Log.v("msg2",""+speed);
                Message message = Message.obtain();
                message.what = 0x114;
                message.obj = speed;
                handler.sendMessage(message);
            }

        }
    }
    private void register(){
        Log.v("msg2","注册广播");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Config.MY_ACTION);
        registerReceiver(receiver, intentFilter);

    }
}
