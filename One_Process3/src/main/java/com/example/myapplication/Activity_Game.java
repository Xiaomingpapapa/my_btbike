package com.example.myapplication;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.example.myapplication.utils.Config;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Long on 2016/8/1.
 */
public class Activity_Game extends Activity implements View.OnClickListener {
	public static final String TAG = "Activity_Game";
	public static final int handle_failed = 0x112;
	public static final int handle_success = 0x113;
	public static final int handle_speed = 0x114;

	private Activity_Game mContext;

	private Dialog dialog1;
	private Dialog dialog2;
	private Button btn_start;
	private ImageView img1;
	private ImageView img2;
	private ImageView img_people2;
	private SeekBar seekBar;
	private ObjectAnimator objectAnimator;
	private ObjectAnimator objectAnimator1;
	private Timer timer;
	int distance = 0;

	private boolean isCancel = false;

	private BroadcastReceiver dataReceiver;

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case handle_failed:
					stopAnimator();
					timer.cancel();
					showFailed();
					break;
				case handle_success:
					stopAnimator();
					timer.cancel();
					showSuccess();
					break;
				case handle_speed:
					int speed = (int)msg.obj;
					distance += speed;
					seekBar.setProgress(distance);
					break;
			}
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_game1);

		btn_start = (Button) findViewById(R.id.btn_start);
		img1 = (ImageView) findViewById(R.id.img1);
		img2 = (ImageView) findViewById(R.id.img2);
		img_people2 = (ImageView) findViewById(R.id.img_people2);
		seekBar = (SeekBar) findViewById(R.id.seekBar);

		seekBar.setMax(Activity_Main.maxDistance);
		btn_start.setOnClickListener(this);
		initAnimator();

		dataReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(Config.MY_ACTION)){
					int speed = intent.getIntExtra(Config.key_broadcast_speed, 0);
					Log.d(TAG, "游戏收到 distance:"+speed);
					getSpeed(speed);
				}
			}
		};
		IntentFilter intentFilter = new IntentFilter(Config.MY_ACTION);
		registerReceiver(dataReceiver, intentFilter);
	}

	private void showSuccess(){
		if(!isCancel && (dialog2==null || !dialog2.isShowing())){
			AlertDialog.Builder builder2 = new AlertDialog.Builder(mContext);
			builder2.setTitle("确认");
			builder2.setMessage("游戏成功");
			builder2.setPositiveButton("进入下一关", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startNew();
				}
			});
			dialog2 = builder2.create();
			dialog2.show();
		}
	}

	private void showFailed(){
		if(!isCancel && (dialog1==null || !dialog1.isShowing())){
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle("确认");
			builder.setMessage("游戏失败");
			builder.setPositiveButton("重新游戏", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startNew();
				}
			});
			builder.setCancelable(false);
			dialog1 = builder.create();
			dialog1.show();
		}
	}

	private void startNew(){
		distance = 0;
		objectAnimator.start();
		objectAnimator1.start();
		seekBar.setProgress(0);
		timer = new Timer();
		initTimer(timer);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_start: {
				objectAnimator.start();
				objectAnimator1.start();
				seekBar.setProgress(0);
				timer = new Timer();
				initTimer(timer);
				break;
			}
		}
	}

	private void initAnimator() {
		objectAnimator = ObjectAnimator.ofFloat(img1, "y", 0f, 1010f);
		objectAnimator1 = ObjectAnimator.ofFloat(img2, "y", 0f, 1010f);
		objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
		objectAnimator.setDuration(3000);
		objectAnimator1.setDuration(6000);
	}

	private void initTimer(Timer timer) {
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				if(isCancel){
					return ;
				}
				if ((img1.getY() >= 990f && Math.abs(seekBar.getProgress() - 30) <= 4)
						|| (img2.getY() >= 990 && seekBar.getProgress() <= 97)) {
					Message message = Message.obtain();
					message.what = handle_failed;
					handler.sendMessage(message);
				}
				if ((seekBar.getProgress() >= 93) && img2.getY() <= 990) {
					Message message = Message.obtain();
					message.what = handle_success;
					handler.sendMessage(message);
				}

			}
		};
		timer.schedule(timerTask, 0, 50);
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	private void stopAnimator() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			objectAnimator.pause();
		}
		objectAnimator1.pause();
	}

	private void getSpeed(int speed){
		// 在此不断换取数据，每获取一次数据就将数据通过Message发送给Handler处理
//		speed = 4;
		Message message = Message.obtain();
		message.what = handle_speed;
		message.obj = speed;
		handler.sendMessage(message);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		isCancel = true;
		unregisterReceiver(dataReceiver);
	}
}
