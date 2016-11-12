package com.example.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.myapplication.func.NetworkController;
import com.example.myapplication.func.ThreadPool;
import com.example.myapplication.utils.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * Created by 傻明也有春天 on 2016/4/13.
 */
public class Activity_Reg extends Activity implements View.OnClickListener {
	public static final String TAG = "Activity_Reg";
	public static final int handle_success = 9701;
	public static final int handle_failed = 9702;
	private static Activity_Reg mContext;

    private SQLiteDatabase db;
	private ImageButton imagebnBack;
	private EditText editTel;
	private EditText editPwd;
	private EditText editNick;
	private ImageButton imgbnIcon;
	private RadioButton rbMale;
	private RadioButton rbFemale;
	private Button btnReg;
	private ProgressDialog progressDialog;

	private PicAdapter mPicAdapter;

	private static Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
				case handle_success:
					if(mContext.progressDialog!=null && mContext.progressDialog.isShowing()){
						mContext.progressDialog.cancel();
					}
					mContext.finish();
					break;
				case handle_failed:
					if(mContext.progressDialog!=null && mContext.progressDialog.isShowing()){
						mContext.progressDialog.cancel();
					}
					Toast.makeText(mContext, ""+msg.obj, Toast.LENGTH_SHORT).show();
					break;
			}
		}
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		mContext = this;
        setContentView(R.layout.activity_reg);

		imagebnBack = (ImageButton)findViewById(R.id.activity_reg_imgbn_back);
        editTel = (EditText) findViewById(R.id.activity_reg_edit_tel);
        editPwd = (EditText) findViewById(R.id.activity_reg_edit_password);
        editNick = (EditText) findViewById(R.id.activity_reg_edit_nick);
        btnReg = (Button) findViewById(R.id.activity_reg_bn_reg);
        imgbnIcon = (ImageButton) findViewById(R.id.activity_reg_imgbn_icon);
		rbMale = (RadioButton) findViewById(R.id.activity_reg_rb_male);
		rbFemale = (RadioButton) findViewById(R.id.activity_reg_rb_female);

		editTel.setText("13047918083");
		editPwd.setText("logintest");
		editNick.setText("测试名");
		imgbnIcon.setImageResource(Config.userIconIds[0]);

		imgbnIcon.setOnClickListener(this);
        imagebnBack.setOnClickListener(this);
		btnReg.setOnClickListener(this);
    }

	@Override
	public void onClick(View v) {
		if(v.equals(imagebnBack)){
			finish();
		}else if(v.equals(btnReg)){
			doRegister();
		}else if(v.equals(imgbnIcon)){
			selectIcon();
		}
	}

	private void selectIcon() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle("选择头像");
		GridView iconGrid = new GridView(mContext);
		iconGrid.setNumColumns(3);
		mPicAdapter = new PicAdapter();
		iconGrid.setAdapter(mPicAdapter);
		iconGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				checkIconPosition = position;
				mPicAdapter.notifyDataSetChanged();
			}
		});
		builder.setView(iconGrid);
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				imgbnIcon.setImageResource(Config.userIconIds[checkIconPosition]);
			}
		});
		builder.create().show();
	}

	private void doRegister(){
		String tel = editTel.getText().toString();
		String pwd = editPwd.getText().toString();
		String nick = editNick.getText().toString();
		int iconPos = checkIconPosition;
		String sex = null;
		if(rbFemale.isChecked()){
			sex = "女";
		}else {
			sex = "男";
		}
		if(checkInform(tel, pwd)){
			regServer(tel, pwd, nick, iconPos, sex);
		}
	}

	private boolean checkInform(String tel, String pwd) {
		boolean result = false;
		if(tel==null || tel.length()<11){
			Toast.makeText(mContext, "请填入正确的手机号", Toast.LENGTH_SHORT).show();
			return false;
		}else{
			result = true;
		}
		if(pwd==null || pwd.length()<6){
			Toast.makeText(mContext, "密码长度过短", Toast.LENGTH_SHORT).show();
			return false;
		}else{
			result = true;
		}
		return result;
	}

	private void regServer(final String tel, final String pwd, final String nick,
						   final int iconPos, final String sex) {
		progressDialog = new ProgressDialog(mContext);
		progressDialog.setMessage("请稍候");
		progressDialog.show();
		ThreadPool.getPool().create(Config.TASK_REG, new Runnable() {
			@Override
			public void run() {
				String urlStr = Config.IFACE_REG;
				String params = "nick="+nick+"&pwd="+pwd+"&tel="+tel+"&face"+iconPos+"&sex="+sex;
				Message msg = new Message();
				try {
					String result = NetworkController.doConnectionGetGETData(urlStr, params);
					Log.d(TAG, "result:"+result);
					JSONObject jsonRet = new JSONObject(result);
					if(jsonRet.getInt("ret") == 1){
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

    private void saveUser(String tel, String pwd, String nick) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File file = Environment.getExternalStorageDirectory();
            db = SQLiteDatabase.openOrCreateDatabase(file + File.separator + "login.db4", null);
        }
        db.execSQL("insert into User1 values('" + tel + "','" + pwd + "','" + nick + "')");
        Log.d("msg", "插入数据成功");
    }

	private int checkIconPosition = 0;
	class PicAdapter extends BaseAdapter{
		public PicAdapter(){
			checkIconPosition = 0;
		}
		@Override
		public int getCount() {
			return Config.userIconIds.length;
		}
		@Override
		public Object getItem(int position) {
			return null;
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imgIcon = new ImageView(mContext);
			imgIcon.setPadding(10, 10, 10, 10);
			imgIcon.setImageResource(Config.userIconIds[position]);
			if(position == checkIconPosition){
				imgIcon.setBackgroundResource(R.drawable.shape_round_gray);
			}else{
				imgIcon.setBackgroundResource(R.color.touming);
			}
			return imgIcon;
		}
	}

}
