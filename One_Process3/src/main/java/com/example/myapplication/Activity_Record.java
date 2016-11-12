package com.example.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.myapplication.func.NetworkController;
import com.example.myapplication.func.ThreadPool;
import com.example.myapplication.func.bean.User;
import com.example.myapplication.utils.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.view.ColumnChartView;

/**
 * 运动记录
 * Created by 傻明也有春天 on 2016/4/16.
 */
public class Activity_Record extends Activity {

    public static final int handle_update = 9701;
    public static final int handle_progress = 9702;
	public static final int handle_failed = 9703;
	public static final String TAG = "Activity_Record";
	private final String key_data_column = "arr_column";
	private final String key_data_list = "arr_list";
	private final String key_data_time = "arr_time";
	private final String key_data_column_round = "round";
	private final String key_data_column_time = "time";
	private final String key_data_column_heart = "heart";

	private final String unit_round = "圈";
	private final String unit_heart = "跳/分";

	private ListView list;
    private Canvas canvas = new Canvas();
    private ImageButton imgbnBack;
    private List<Map<String,Object>> dataList;
    private SimpleAdapter simpleAdapter;
    private ColumnChartView columnChartView;
	private ColumnChartData columnChartData;

    private ProgressDialog networkProgress;

	private static int maxValue = 50000;

    int imageId[] = {R.drawable.r,R.drawable.s,R.drawable.t};
    String txt1[] = {"平均骑行","平均心率","健康状态"};
	private final int[] columnColors = {
			Color.parseColor("#cccc00"), Color.parseColor("#99cccc"), Color.parseColor("#ff9999"),
			Color.parseColor("#99cc00"), Color.parseColor("#0099cc"), Color.parseColor("#cccc33"),
			Color.parseColor("#ff9900")
	};

    public final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case handle_update:
					float[] roundArr = msg.getData().getFloatArray(key_data_column);
					int[] heartArr = msg.getData().getIntArray(key_data_list);
					String[] timeArr = msg.getData().getStringArray(key_data_time);
					updateListData(roundArr, heartArr);
					updateColumnData(roundArr, timeArr);
					updatePage();
                    break;
                case handle_progress:
                    if(networkProgress!=null && networkProgress.isShowing()){
                        networkProgress.cancel();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pastrecord);

		columnChartView = (ColumnChartView) findViewById(R.id.activity_pastrecord_chart_columnchart);
		imgbnBack = (ImageButton)findViewById(R.id.activity_record_imgbn_back);
        list = (ListView)findViewById(R.id.activity_record_lv_list);
		dataList = new ArrayList<>();

		imgbnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

        simpleAdapter = new SimpleAdapter(Activity_Record.this, dataList,
				R.layout.adapter_item_historylist,
                new String[]{"image","name","data"},
                new int[]{R.id.adapter_item_historylist_img_icon,
						R.id.adapter_item_historylist_txt_name,
						R.id.adapter_item_historylist_txt_data});
        list.setAdapter(simpleAdapter);

		initChartData();
		initChartView();
		getHistoryData();
    }

	private void initChartData(){
		columnChartData = new ColumnChartData();
		List<Column> columns = new ArrayList<>();
		for(int i=0; i<7; i++){
			Column column = new Column();
			List<SubcolumnValue> values = new ArrayList<>();
			SubcolumnValue value = new SubcolumnValue();
			value.setValue(0);
			value.setColor(columnColors[i]);
			values.add(value);
			column.setValues(values);
			columns.add(column);

		}
		List<AxisValue> axisValues = new ArrayList<>();
		axisValues.add(new AxisValue(0).setLabel(""));
		axisValues.add(new AxisValue(1).setLabel(""));
		axisValues.add(new AxisValue(2).setLabel(""));
		axisValues.add(new AxisValue(3).setLabel(""));
		axisValues.add(new AxisValue(4).setLabel(""));
		axisValues.add(new AxisValue(5).setLabel(""));
		axisValues.add(new AxisValue(6).setLabel(""));
		columnChartData.setAxisYLeft(new Axis().setName("单位："+unit_round).setMaxLabelChars(5));
		columnChartData.setAxisXBottom(new Axis().setValues(axisValues));
		columnChartData.setColumns(columns);
	}

	private void initChartView(){
		columnChartView.setColumnChartData(columnChartData);
	}

    private void updateColumnData(float[] datas, String[] times){
        List<Column> columns = columnChartData.getColumns();
		Axis axisXBottom = columnChartData.getAxisXBottom();
		List<AxisValue> axisValues = axisXBottom.getValues();
        for(int i=0; i<7; i++){
            Column column = columns.get(i);
            SubcolumnValue value = column.getValues().get(0);
            value.setValue(datas[i]);
			String time = "无";
			if(times[i].length() > 6){
				time = times[i].substring(5);
			}
			axisValues.get(i).setLabel(time);
        }

        columnChartView.startDataAnimation(1000);
    }

	private void updateListData(float[] round, int[] heart){
		int roundCount = 0;
		int heartCount = 0;
		int notNullDay = 0;
		for(int i=0; i<round.length; i++){
			if(round[i]>0){
				notNullDay++;
			}
			roundCount += round[i];
		}
		for(int i=0; i<heart.length; i++){
			heartCount += heart[i];
		}
		if(notNullDay==0){
			notNullDay = 1;
		}
		String[] txt2 = new String[]{""+(roundCount/notNullDay)+unit_round,
				""+(heartCount/notNullDay)+unit_heart, "良好"};
		for (int i=0; i<3; i++){
			Map<String, Object> map = new HashMap<>();
			map.put("image", imageId[i]);
			map.put("name", txt1[i]);
			map.put("data", txt2[i]);
			dataList.add(map);
		}
	}

    /** 更新页面数据 */
    private void updatePage(){
        simpleAdapter.notifyDataSetChanged();
    }

    /** 获取历史数据 */
    private void getHistoryData(){
        networkProgress = new ProgressDialog(this);
        networkProgress.setMessage("请稍候");
        networkProgress.show();
		Log.v("msg","hahahah");
        ThreadPool.getPool().create(Config.TASK_DOWNLOAD_HISTORY_DATA, new Runnable() {
            @Override
            public void run() {
                String urlStr = Config.IFACE_GET_HISTORY;
                String params = "userId="+ User.getUser().getId();
				Message msg = Message.obtain();
                try {
                    String result = NetworkController.doConnectionGetGETData(urlStr, params);
                    System.out.println("result:"+result);
					JSONObject jsonRet = new JSONObject(result);
					if(jsonRet.getInt("ret") == 1){
						msg.what = handle_update;
						Bundle data = new Bundle();
						// TODO 解析数据
						JSONArray jsonData = jsonRet.getJSONArray("data");
						Log.v("msg",jsonData+"");
						float[] roundArr = new float[7];
						int[] heartArr = new int[7];
						String[] timeArr = new String[7];
						for(int i=0; i<7; i++){
							if(i >= jsonData.length()){
								roundArr[i] = 0;
								heartArr[i] = 0;
								timeArr[i] = "无";
								continue;
							}
							timeArr[i] = jsonData.getJSONObject(i).getString(key_data_column_time);
							JSONObject jsonObject = jsonData.getJSONObject(i);
							String flag = jsonObject.getString(key_data_column_round);
							if(flag==null||flag.equals("null")){
								roundArr[i] = 0;
							}
							else{
								roundArr[i] = jsonObject.getInt(key_data_column_round);
							}
							flag = jsonObject.getString(key_data_column_heart);
							if (flag == null||flag.equals("null")) {

								heartArr[i] = 0;
							}
							else{
								heartArr[i] = jsonObject.getInt(key_data_column_heart);
							}


						}
						data.putFloatArray(key_data_column, roundArr);
						data.putIntArray(key_data_list, heartArr);
						data.putStringArray(key_data_time, timeArr);
						msg.setData(data);
						Log.v("msg",data+"");
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
					msg.obj = "获取历史数据失败";
				} finally {
					mHandler.sendEmptyMessage(handle_progress);
                }
				mHandler.sendMessage(msg);
            }
        });
    }

}
