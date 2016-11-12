package com.example.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.func.NetworkController;
import com.example.myapplication.func.ThreadPool;
import com.example.myapplication.func.bean.User;
import com.example.myapplication.utils.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

/**
 * 个人中心
 * Created by 傻明也有春天 on 2016/4/20.
 */
public class Activity_Person extends Activity {
	public static final String TAG = "Activity_Person";
    public static final int handle_update = 9701;
	public static final int handle_progress =9702;
	public static final int handle_failed = 9703;
	private static final String key_data_defeated = "defeated";
	private static final String key_data_yearold = "yearold";
	private static final String key_data_weight = "weight";
	private static final String key_data_reweight = "reweight";

	private static Activity_Person mContext;
	private ImageButton imgbnBack;
	private TextView txtDefeat;
	private TextView txtYearold;
	private TextView txtYearDesc;
	private TextView txtDate;
	private TextView txtWeightNow;
	private TextView txtReWeight;
	private ProgressDialog updateProgress;

    public final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case handle_update:
					updatePerson(msg.getData());
					break;
				case handle_progress:
					if(updateProgress!=null && updateProgress.isShowing()){
						updateProgress.cancel();
					}
					break;
				case handle_failed:
					Toast.makeText(mContext, ""+msg.obj, Toast.LENGTH_SHORT).show();
					break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		mContext = this;
        setContentView(R.layout.activity_person);

		imgbnBack = (ImageButton)findViewById(R.id.activity_person_imgbn_back);
		txtDefeat = (TextView) findViewById(R.id.activity_person_txt_defeat);
		txtYearold = (TextView) findViewById(R.id.activity_person_txt_yearold);
		txtYearDesc = (TextView) findViewById(R.id.activity_person_txt_yeardesc);
		txtDate = (TextView)findViewById(R.id.activity_person_txt_date);
		txtWeightNow = (TextView) findViewById(R.id.activity_person_txt_weight_now);
		txtReWeight = (TextView) findViewById(R.id.activity_person_txt_weight_msg);

		imgbnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

        int month = Calendar.getInstance().get(Calendar.MONTH) + (1-Calendar.JANUARY);
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        txtDate.setText(month+"月"+day+"日");

		getPersonData();
    }

	/** 更新用户数据
	 * @param data*/
	private void updatePerson(Bundle data){
		Log.d(TAG, "data:"+data);
		txtDefeat.setText(""+data.getFloat(key_data_defeated)+"%");
		txtYearold.setText(""+data.getInt(key_data_yearold)+"岁");
		txtYearDesc.setText("岁月是把杀猪刀");
		txtWeightNow.setText("当前"+data.getFloat(key_data_weight)+"千克");
		float reweight = data.getFloat(key_data_reweight);
		if(reweight>0){
			txtReWeight.setText("亲！这个月体重减少了"+reweight+"千克呢，请继续加油！");
		}else{
			txtReWeight.setText("亲！这个月体重增加了"+reweight+"千克哦，要多多锻炼啊！");
		}
	}

	/** 获取用户数据 */
	private void getPersonData(){
		updateProgress = new ProgressDialog(this);
		updateProgress.setMessage("请稍候");
		updateProgress.show();
		ThreadPool.getPool().create(Config.TASK_DOWNLOAD_PERSON_DATA, new Runnable() {
			@Override
			public void run() {
				String urlStr = Config.IFACE_GET_PERSONDATA;
				String params = "userId="+User.getUser().getId();
				Message msg = new Message();
				try {
					String result = NetworkController.doConnectionGetGETData(urlStr, params);
					System.out.println("result:"+result);

					JSONObject jsonRet = new JSONObject(result);
					if(jsonRet.getInt("ret") == 1){
						msg.what = handle_update;
						Bundle data = new Bundle();
						// TODO 解析数据
						JSONObject jsonData = jsonRet.getJSONObject("data");
						data.putFloat(key_data_defeated, Float.parseFloat(jsonData.getString(key_data_defeated)));
						data.putInt(key_data_yearold, jsonData.getInt(key_data_yearold));
						data.putFloat(key_data_weight, Float.parseFloat(jsonData.getString(key_data_weight)));
						data.putFloat(key_data_reweight, Float.parseFloat(jsonData.getString(key_data_reweight)));
						msg.setData(data);
					}else{
						msg.what = handle_failed;
						msg.obj = "获取数据失败";
					}
				} catch (IOException e) {
					e.printStackTrace();
					msg.what = handle_failed;
					msg.obj = "网络连接失败";
				} catch (JSONException e) {
					e.printStackTrace();
					msg.what = handle_failed;
					msg.obj = "获取数据失败";
				} finally {
					mHandler.sendEmptyMessage(handle_progress);
				}
				mHandler.sendMessage(msg);
			}
		});
	}

}

