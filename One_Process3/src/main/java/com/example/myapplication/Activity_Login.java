package com.example.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.func.NetworkController;
import com.example.myapplication.func.ThreadPool;
import com.example.myapplication.func.bean.User;
import com.example.myapplication.utils.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * Created by 傻明也有春天 on 2016/4/13.
 */
public class Activity_Login extends Activity {
	private static Activity_Login mContext;
	public static final String TAG = "Activity_Login";
	private static final int handle_success = 9701;
	private static final int handle_failed = 9702;
    SQLiteDatabase db;
    Button btn_reg;
    Button btn_login;
    EditText edit_account;
    EditText edit_password;
    TexttoSpeak speak;
	private ProgressDialog progressDialog;

	private static Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
				case handle_success:
					if(mContext.progressDialog!=null && mContext.progressDialog.isShowing()){
						mContext.progressDialog.cancel();
					}
					mContext.loginSuccess();
					break;
				case handle_failed:
					if(mContext.progressDialog!=null && mContext.progressDialog.isShowing()){
						mContext.progressDialog.cancel();
					}
					mContext.loginFailed();
					break;
			}
		}
	};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		mContext = this;
		if(User.getUser().isLogin()){
			jumpToMain();
		}
        setContentView(R.layout.activity_login);

		btn_reg = (Button) findViewById(R.id.activity_login_bn_reg);
		btn_login = (Button) findViewById(R.id.activity_login_bn_login);
		edit_account = (EditText) findViewById(R.id.activity_login_edit_account);
		edit_password = (EditText) findViewById(R.id.activity_login_edit_password);
		speak = new TexttoSpeak(this);

		edit_account.setText("Sharon");
		edit_password.setText("sharon");

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();

            }
        });
        btn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Activity_Login.this, Activity_Reg.class);
                startActivity(intent);
            }
        });

		initDB();
    }

    private void initDB() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File file = Environment.getExternalStorageDirectory();
            db = SQLiteDatabase.openOrCreateDatabase(file + File.separator + "login.db4", null);
            db.execSQL("create table if not exists User1(mobileNum varchar(20),password varchar(20), name varchar(20))");
        }
    }

    private void login() {
		String account = edit_account.getText().toString();
		String password = edit_password.getText().toString();
		if(true){
			loginServer(account, password);
		}else{
			loginCache(account, password);
		}
    }

	private void loginServer(final String account, final String password){
		progressDialog = new ProgressDialog(mContext);
		progressDialog.setMessage("正在登录");
		progressDialog.show();
		ThreadPool.getPool().create(Config.TASK_LOGIN, new Runnable() {
			@Override
			public void run() {
				String urlStr = Config.IFACE_LOGIN;
				String params = "user="+account+"&pass="+password;
				Message msg = new Message();
				try {
					String result = NetworkController.doConnectionGetGETData(urlStr, params);
					Log.d(TAG, "result:"+result);
					JSONObject jsonRet = new JSONObject(result);
					if(jsonRet.getInt("ret") == 1){
						JSONObject jsonUser = jsonRet.getJSONArray("data").getJSONObject(0);
						new User(jsonUser.getInt(Config.column_user_id),
								jsonUser.getString(Config.column_user_name),
								jsonUser.getString(Config.column_user_tel),
								jsonUser.getString(Config.column_user_face),
								jsonUser.getString(Config.column_user_sex));
						msg.what = handle_success;
					}else{
						msg.what = handle_failed;
						msg.obj = jsonRet.getString("err");
					}
				} catch (IOException e) {
					e.printStackTrace();
					msg.what = handle_failed;
					msg.obj = "网络无法连接";
				} catch (JSONException e) {
					e.printStackTrace();
					msg.what = handle_failed;
					msg.obj = "服务器故障";
				}
				mHandler.sendMessage(msg);
			}
		});
	}

	private void loginCache(String accountStr, String passwordStr){
		boolean right = false;
		Cursor cursor = db.query("User1", null, null, null, null, null, null);
		while (cursor.moveToNext()) {
			String mobile_id = cursor.getString(cursor.getColumnIndex("mobileNum")).trim();
			String password = cursor.getString(cursor.getColumnIndex("password")).trim();
			if (mobile_id.equals(accountStr) && password.equals(passwordStr)) {
				right = true;
				break;
			}
		}
		if (right) {
			mHandler.sendEmptyMessage(handle_success);

		} else {
			mHandler.sendEmptyMessage(handle_failed);

		}
	}

	private void jumpToMain(){
		Intent intent = new Intent(Activity_Login.this, Activity_Main.class);
		startActivity(intent);
		finish();
	}

	private void loginSuccess(){
//		speak.speak("2016小梦为您服务，请先连接蓝牙");
		speak.speak("欢迎回来"+User.getUser().getName());
		jumpToMain();
	}

	private void loginFailed(){
		Toast.makeText(Activity_Login.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
	}

}
