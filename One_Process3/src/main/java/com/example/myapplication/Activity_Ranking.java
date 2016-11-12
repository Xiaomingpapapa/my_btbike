package com.example.myapplication;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.func.NetworkController;
import com.example.myapplication.func.ThreadPool;
import com.example.myapplication.func.bean.User;
import com.example.myapplication.utils.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 排行
 * Created by 傻明也有春天 on 2016/5/12.
 */
public class Activity_Ranking extends Activity implements AdapterView.OnItemClickListener {
    public static final int handle_update = 9701;
    public static final int handle_progress =9702;
	public static final int handle_failed = 9703;
	private static final String key_data_roundnew = "round";
	private static final String key_data_roundtotal = "total_round";
	private static final String key_data_name = "name";
	private static final String key_data_face = "face";
	private static final String key_data_lastride = "time";

    private static Activity_Ranking mContext;

    private ImageButton imgbnBack;
    private ListView lvRank;
	private TextView txtDate;
	private ProgressDialog networkProgress;

    private List<Map<String, Object>> rankData;
    private RankAdapter rankAdapter;

    public static final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case handle_update:
                    mContext.updateList();
                    break;
                case handle_progress:
                    if( mContext.networkProgress!=null &&  mContext.networkProgress.isShowing()){
						mContext.networkProgress.cancel();
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
        setContentView(R.layout.activity_ranking);
        imgbnBack = (ImageButton)findViewById(R.id.activity_ranking_imgbn_back);
		txtDate = (TextView) findViewById(R.id.activity_number_txt_date);
        lvRank = (ListView) findViewById(R.id.activity_number_lv_rank);

		lvRank.setOnItemClickListener(this);
        imgbnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
				finish();
            }
        });
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		txtDate.setText(sdf.format(System.currentTimeMillis()));
        rankAdapter = new RankAdapter();
        lvRank.setAdapter(rankAdapter);

		getRankData();
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if(parent.equals(lvRank)){
			UserScore mUserScore = new UserScore(mContext, position);
			mUserScore.show();
		}
	}

	/** 执行界面更新 */
	private void updateList(){
		rankAdapter.notifyDataSetChanged();
	}

	/** 向服务器获取排行数据 */
	private void getRankData(){
		networkProgress = new ProgressDialog(mContext);
		networkProgress.setMessage("请稍候");
		networkProgress.setCancelable(false);
		networkProgress.show();
		ThreadPool.getPool().create(Config.TASK_DOWNLOAD_RANK_DATA, new Runnable() {
			@Override
			public void run() {
				String urlStr = Config.IFACE_GET_RANKING;
				String params = "userId="+ User.getUser().getId();
				Message msg = new Message();
				try {
					String result = NetworkController.doConnectionGetGETData(urlStr, params);
					System.out.println("result:"+result);
					JSONObject jsonRet = new JSONObject(result);
					if(jsonRet.getInt("ret") == 1){
						msg.what = handle_update;
						// TODO 解析数据
						JSONArray jsonData = jsonRet.getJSONArray("data");
						for(int i=0; i<jsonData.length(); i++){
							Map<String, Object> map = new HashMap<String, Object>();
							JSONObject jsonObj = jsonData.getJSONObject(i);
							map.put(key_data_roundtotal, jsonObj.getInt(key_data_roundnew));	//key_data_roundtotal
							map.put(key_data_roundnew, jsonObj.getInt(key_data_roundnew));
							map.put(key_data_name, jsonObj.getString(key_data_name));
							map.put(key_data_face, jsonObj.getInt(key_data_face));
							map.put(key_data_lastride, jsonObj.getString(key_data_lastride));

							rankData.add(map);
						}
					}else{
						msg.what = handle_failed;
						msg.obj = jsonRet.getString("err");
					}
				} catch (IOException e) {
					e.printStackTrace();
					msg.what = handle_failed;
					msg.obj = "网络连接失败";
				} catch (JSONException e) {
					e.printStackTrace();
					msg.what = handle_failed;
					msg.obj = "获取排行榜失败";
				} finally {
					mHandler.sendEmptyMessage(handle_progress);
				}
				mHandler.sendMessage(msg);
			}
		});
	}

	class UserScore extends Dialog {
		private TextView txtTitle;
		private TextView txtRoundnew;
		private TextView txtRoundtotal;
		private Button bnCancel;
		private int location;

		public UserScore(Context context, int position) {
			super(context);
			this.location = position;
		}
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.dialog_userscore);
			txtTitle = (TextView) findViewById(R.id.dialog_userscore_txt_title);
			txtRoundnew = (TextView) findViewById(R.id.dialog_userscore_txt_roundnew);
			txtRoundtotal = (TextView) findViewById(R.id.dialog_userscore_txt_roundtotal);
			bnCancel = (Button) findViewById(R.id.dialog_userscore_bn_cancel);

			txtTitle.setText(""+rankData.get(location).get(key_data_name)+"的运动记录");
			txtRoundnew.setText(""+rankData.get(location).get(key_data_roundnew));
			txtRoundtotal.setText(""+rankData.get(location).get(key_data_roundnew));	//key_data_roundtotal

			bnCancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					cancel();
				}
			});
		}
	}

	class RankAdapter extends BaseAdapter{
        public RankAdapter(){
            rankData = new ArrayList<>();
        }
        @Override
        public int getCount() {
            return rankData.size();
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
            ViewHolder holder = null;
            if(convertView == null){
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_item_rank, null);
                holder.imgIcon = (ImageView) convertView.findViewById(R.id.adapter_item_rank_imgbn_icon);
                holder.txtName = (TextView) convertView.findViewById(R.id.adapter_item_rank_txt_name);
                holder.txtMark = (TextView) convertView.findViewById(R.id.adapter_item_rank_txt_mark);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
			int iconPosition = (int) rankData.get(position).get(key_data_face);
			if(iconPosition<Config.userIconIds.length && iconPosition>=0){
				holder.imgIcon.setImageResource(Config.userIconIds[iconPosition]);
			}
            holder.txtName.setText(""+rankData.get(position).get(key_data_name));
            holder.txtMark.setText("骑行"+rankData.get(position).get(key_data_roundnew)+"圈");
            return convertView;
        }
    }
    class ViewHolder{
		ImageView imgIcon;
        TextView txtName;
        TextView txtMark;
    }
}
