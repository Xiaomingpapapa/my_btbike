package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ClipDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.func.NetworkController;
import com.example.myapplication.func.ThreadPool;
import com.example.myapplication.func.bean.User;
import com.example.myapplication.utils.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class Activity_Main extends Activity implements View.OnClickListener {
    public static final String TAG = "Activity_Main";
    private final static int REQUEST_CONNECT_DEVICE = 1;    //宏定义查询设备句柄
    private static final int handle_progress = 9701;
    private static final int handle_picture = 9702;
    private static final int handle_recvdata = 9703;
    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号
    private InputStream is;    //输入流，用来接收蓝牙数据
    private String voice1 = "主人，当前室温已超过34.5度，您要适当多休息一下哦";
    private String voice2 = "主人，小梦检测到您当前运动量超出运动量范围，请减小运动强度";
    private Activity_Main mContext;
    BluetoothDevice _device = null;     // 蓝牙设备
    public static BluetoothSocket _socket = null;     // 蓝牙通信socket
    boolean bRun = true;
    boolean bThread = false;
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();    //获取本地蓝牙适配器，即蓝牙设备
    CircleProgress circleProgress;
    private int mCurrentProgress;
    private ProgressDialog updateProgress;
    private ImageView imgAudio;
    private Spinner levelSpinner;
    SeekBar seekbarDistance;
    ImageView imgHeart;
    ImageView imgWeight;
    ImageView imgTemperature;
    TextView txtDistance;
    TextView txtHeart;
    TextView txtWeight;
    TextView txtTemperature;
    TextView text13;
    TextView text_name1;
    TextView text_sex1;
    ListView list_button_view;
    List<Map<String, Object>> data;
    private Button bnState;
    private int[] image_button = new int[]{R.drawable.icon_n1, R.drawable.icon_n3, R.drawable.icon_n4, R.drawable.icon_n5};
    private String[] str_button = new String[]{"个人中心", "排行榜", "运动记录", "开启蓝牙"};
    TexttoSpeak speaker1;
    Timer timer;
    Timer timer2;
    Timer updateTimer;
    private boolean isCancel = false;
    public static int maxDistance = 200;
    private static int maxHeart = 140;
    private Map<String, Object> recvData = new HashMap<>();
    private static final String key_distance = "dis";
    private static final String key_tem = "temperature";
    private static final String key_heart = "heart";
    private static final String key_weight = "weight";
    private ClipDrawable clipDrawable;
    private int curDistance = 0;

    //消息处理队列
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case handle_recvdata:
                    String string1 = (String) msg.obj;
                    decodeData(string1);
                    break;
                case handle_progress:
                    if (updateProgress != null && updateProgress.isShowing()) {
                        updateProgress.cancel();
                    }
                    break;
                case handle_picture:
                    // TODO 在这里更新图片
                    updataPicture();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);
        init();
        initVariable();
        Log.v("msg", "打印日志");
        seekbarDistance.setMax(maxDistance);
        initSpinner();
        set_listData();
        set_list_onclick();
        refreshAudio();
        clipDrawable = new ClipDrawable(imgTemperature.getDrawable(), Gravity.BOTTOM, ClipDrawable.VERTICAL);
        if (_bluetooth == null) {
            Toast.makeText(this, "无法打开手机蓝牙，请确认手机是否有蓝牙功能！", Toast.LENGTH_LONG).show();
            // finish();
            return;
        }
        // 打开蓝牙
        new Thread() {
            public void run() {
                if (_bluetooth.isEnabled() == false) {
                    _bluetooth.enable();
                }
            }
        }.start();
        seekbarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCurrentProgress = progress / 5;
                circleProgress.setProgress(mCurrentProgress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        speaker1 = new TexttoSpeak(this);
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (!isCancel) {
                    float temp = (float) recvData.get(key_tem);
                    if (temp >= 34.5) {
                        speaker1.speak(voice1);
                    }
                }
            }
        };
        timer2 = new Timer();
        TimerTask timerTask2 = new TimerTask() {
            @Override
            public void run() {
                if (!isCancel) {
                    try {
                        int heart = (int) recvData.get(key_heart);
                        if (heart >= maxHeart) {
                            speaker1.speak(voice2);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        timer.schedule(timerTask, 3000, 6000);
        timer2.schedule(timerTask2, 3000, 6000);

        ThreadPool.getPool().create(Config.TASK_UPDATE_MAIN_PIC, new Runnable() {
            @Override
            public void run() {
                while (!isCancel) {
//                  System.out.println("状态："+getCurState());
                    if (getCurState() == STATE.START) {
                        handler.sendEmptyMessage(handle_picture);
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void refreshAudio() {
        if (TexttoSpeak.isAudioOn) {
            imgAudio.setImageResource(R.mipmap.volume_full1);
        } else {
            imgAudio.setImageResource(R.mipmap.volume_muted1);
        }
    }

    private void decodeData(String string1) {
        int index_t = string1.indexOf("t");
        int index_w = string1.indexOf("w");
        int index_p = string1.indexOf("p");
        int index_s = string1.indexOf("s");
        int index_z = string1.indexOf("z");

        try {
            if (index_t != -1 && index_w != -1) {
                if (recvData.containsKey(key_tem)) {
                    recvData.remove(key_tem);
                }
                String temStr = string1.substring(index_t + 1, index_w);
                if (temStr.length() > 0) {
                    try {
                        recvData.put(key_tem, Float.parseFloat(temStr));
                    } catch (Exception e) {
                    }
                } else {
                    recvData.put(key_tem, 0.0f);
                }
            }
            if (index_w != -1 && index_p != -1) {
                if (recvData.containsKey(key_weight)) {
                    recvData.remove(key_weight);
                }
                String weightStr = string1.substring(index_w + 1, index_p);
                if (weightStr.length() > 0) {
                    try {
                        recvData.put(key_weight, Float.parseFloat(weightStr));
                    } catch (Exception e) {
                    }
                } else {
                    recvData.put(key_weight, 0.0f);
                }
            }
            if (index_p != -1 && index_s != -1) {
                if (recvData.containsKey(key_heart)) {
                    recvData.remove(key_heart);
                }
                String heartStr = string1.substring(index_p + 1, index_s);
                if (heartStr.length() > 0) {
                    try {
                        recvData.put(key_heart, Integer.parseInt(heartStr));
                    } catch (Exception e) {
                    }
                } else {
                    recvData.put(key_heart, 0);
                }
            }
            if (index_s != -1 && index_z != -1) {
                if (recvData.containsKey(key_distance)) {
                    recvData.remove(key_distance);
                }
                String distanceStr = string1.substring(index_s + 1, index_z);
                if (distanceStr.length() > 0) {
                    try {
                        recvData.put(key_distance, Integer.parseInt(distanceStr));
                    } catch (Exception e) {
                    }
                } else {
                    recvData.put(key_distance, 0);
                }
            }
            updatePageData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 命令发送设置
     */
    private void initSpinner() {
        final String[] strs = {"低", "中", "高"};    // 命令显示
        final String[] cmd = {"a", "b", "c"};        // 命令值
        levelSpinner.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, strs));
        levelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (_socket != null && _socket.isConnected()) {
                    try {
                        OutputStream os = _socket.getOutputStream();
                        os.write(cmd[position].getBytes());
                        Toast.makeText(mContext, "发送命令:" + cmd[position], Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(mContext, "发送失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, "暂未连接平台", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    /**
     * 接收活动结果，响应startActivityForResult()
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:     //连接结果，由DeviceListActivity设置返回
                // 响应返回结果
                if (resultCode == Activity.RESULT_OK) {   //连接成功，由DeviceListActivity设置返回
                    // MAC地址，由DeviceListActivity设置返回
                    String address = data.getExtras()
                            .getString(DeviceList.EXTRA_DEVICE_ADDRESS);
                    // 得到蓝牙设备句柄
                    _device = _bluetooth.getRemoteDevice(address);

                    // 用服务号得到socket
                    try {
                        _socket = _device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
                    } catch (IOException e) {
                        Toast.makeText(this, "连接失败1！", Toast.LENGTH_SHORT).show();
                    }
                    //连接socket
                    ImageButton btn = (ImageButton) findViewById(R.id.activity_main_imgbn_back);
                    try {
                        _socket.connect();
                        Toast.makeText(this, "连接" + _device.getName() + "成功！", Toast.LENGTH_SHORT).show();
                        speaker1.speak("主人，蓝牙连接成功");
                        //btn.setText("断开");
                    } catch (IOException e) {
                        try {
                            Toast.makeText(this, "连接失败2！", Toast.LENGTH_SHORT).show();
                            _socket.close();
                            _socket = null;
                        } catch (IOException ee) {
                            Toast.makeText(this, "连接失败3！", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    //打开接收线程
                    try {
                        is = _socket.getInputStream();   //得到蓝牙数据输入流
                    } catch (IOException e) {
                        Toast.makeText(this, "接收数据失败！", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //得到蓝牙数据输入流
                    if (bThread == false) {
                        ReadThread.start();
                        bThread = true;
                    } else {
                        bRun = true;
                    }
                }
                break;
            default:
                break;
        }
    }

    Thread ReadThread = new Thread() {

        public void run() {
            int num = 0;
            byte[] buffer = new byte[1024];
            byte[] buffer_new = new byte[1024];
            int i = 0;
            int n = 0;
            int j = 0;
            bRun = true;

            StringBuilder sbRet = new StringBuilder();
            final int data_none = -1;
            int state = 0;

            while (true) {
                try {
                    while (is.available() == 0) {
                        while (bRun == false) {
                        }
                    }
                    while (true) {
                        num = is.read(buffer);         //读入数据
                        String ret = new String(buffer, 0, num);
//						Log.d(TAG, "读到:"+ret);
                        int index_a = ret.indexOf("a");
                        int index_z = ret.indexOf("z");
                        if (state == 0 && index_a != data_none) {
                            state = 1;
                            if (index_z != data_none) {
                                sbRet.append(ret.substring(index_a, index_z + 1));
                                state = 2;
                            } else {
                                sbRet.append(ret.substring(index_a));
                            }
                        } else if (state == 1 && index_z != data_none) {
                            if (index_a != data_none) {
                                sbRet = new StringBuilder();
                                sbRet.append(ret.substring(index_a, index_z + 1));
                            } else {
                                sbRet.append(ret.substring(0, index_z + 1));
                            }
                            state = 2;
                        } else if (state == 1) {
                            sbRet.append(ret);
                        }
                        if (state == 2) {
                            Message msg = new Message();
                            msg.what = handle_recvdata;
                            msg.obj = sbRet.toString();
                            handler.sendMessage(msg);
                            state = 0;
                            sbRet = new StringBuilder();
                        }
                    }
                } catch (IOException e) {
                }

            }
        }
    };

    //关闭程序掉用处理部分
    public void onDestroy() {
        super.onDestroy();
        if (_socket != null)  //关闭连接socket
            try {
                _socket.close();
            } catch (IOException e) {
            }
        if (_bluetooth != null) {
            _bluetooth.disable();  //关闭蓝牙服务
        }
        isCancel = true;
    }

    //连接按键响应函数
    public void onConnectButtonClicked() {
        if (_bluetooth.isEnabled() == false) {  //如果蓝牙服务不可用则提示
            Toast.makeText(this, " 打开蓝牙中...", Toast.LENGTH_LONG).show();
            _bluetooth.enable();
            return;
        }
        //如未连接设备则打开DeviceListActivity进行设备搜索
        if (_socket == null) {
            Intent serverIntent = new Intent(this, DeviceList.class); // 跳转程序设置
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);  // 设置返回宏定义
        } else {
            //关闭连接socket
            try {
                is.close();
                _socket.close();
                _socket = null;
                _bluetooth.disable();
                bRun = false;
                // btn.setText("连接");
            } catch (IOException e) {
            }
        }
        return;
    }

    private void init() {
        imgAudio = (ImageView) findViewById(R.id.activity_main_img_audio);
        levelSpinner = (Spinner) findViewById(R.id.activity_main_sp_level);
        circleProgress = (CircleProgress) findViewById(R.id.activity_main_circleprogress);
        seekbarDistance = (SeekBar) findViewById(R.id.activity_main_seekbar_distance);
        imgHeart = (ImageView) findViewById(R.id.activity_main_img_heart);
        imgWeight = (ImageView) findViewById(R.id.activity_main_img_weight);
        imgTemperature = (ImageView) findViewById(R.id.activity_main_img_temperature);
        txtDistance = (TextView) findViewById(R.id.activity_main_txt_distance);
        txtHeart = (TextView) findViewById(R.id.activity_main_txt_heart);
        txtWeight = (TextView) findViewById(R.id.activity_main_txt_weight);
        txtTemperature = (TextView) findViewById(R.id.activity_main_txt_temperature);
        text_name1 = (TextView) findViewById(R.id.text_name1);
        text_sex1 = (TextView) findViewById(R.id.text_sex1);
        list_button_view = (ListView) findViewById(R.id.list_button_view);
        bnState = (Button) findViewById(R.id.activity_main_bn_control);
        imgAudio.setOnClickListener(this);
        imgHeart.setOnClickListener(this);
        bnState.setOnClickListener(this);
        circleProgress.setOnClickListener(this);


    }


    private void initVariable() {
        mCurrentProgress = 0;
        recvData.put(key_tem, 0.0f);
        recvData.put(key_weight, 0.0f);
        recvData.put(key_heart, 0);
        recvData.put(key_distance, 0);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.activity_main_circleprogress:
                intent.setClass(mContext, Move1.class);
                startActivity(intent);
                break;
            case R.id.activity_main_img_audio:
                TexttoSpeak.isAudioOn = !TexttoSpeak.isAudioOn;
                refreshAudio();
                break;
            case R.id.activity_main_img_heart:
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("设置心率上限");
                final EditText editText = new EditText(mContext);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setText("" + maxHeart);
                builder.setView(editText);
                builder.setNegativeButton("取消", null);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        maxHeart = Integer.parseInt(editText.getText().toString());
                    }
                });
                builder.create().show();
                break;
            case R.id.activity_main_bn_control:
                switch (getCurState()) {
                    case STOP: {
                        setCurState(STATE.START);
                        bnState.setText("停止运动");
                        TimerTask timerTask = new TimerTask() {
                            @Override
                            public void run() {
                                int distance2 = (int) recvData.get(key_distance);
                                int heart_rate = (int) recvData.get(key_heart);
                                float weight = (float) recvData.get(key_weight);
                                float temperature = (float) recvData.get(key_tem);
                                uploadPageData(distance2, heart_rate, weight, temperature);
                            }

                        };
                        if(updateTimer==null){updateTimer = new Timer();}
                        updateTimer.schedule(timerTask, 0, 5000);
                        break;
                    }
                    case START: {
                        setCurState(STATE.STOP);
                        bnState.setText("开始运动");
                        try {

                            int distance2 = (int) recvData.get(key_distance);
                            int heart_rate = (int) recvData.get(key_heart);
                            float weight = (float) recvData.get(key_weight);
                            float temperature = (float) recvData.get(key_tem);
                            //updatePageData(distance2, heart_rate, weight, temperature);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        restorePicture();
                        break;
                    }

                }
                break;

        }
    }

    private enum STATE {
        STOP, START
    }

    private static STATE curState = STATE.STOP;

    private void setCurState(STATE curState) {
        this.curState = curState;
    }

    private STATE getCurState() {
        return curState;
    }


    /**
     * 更新界面
     */
    private void updatePageData() {
        int distance2 = (int) recvData.get(key_distance);
        int heart_rate = (int) recvData.get(key_heart);
        float weight = (float) recvData.get(key_weight) / 2;
        float temperature = (float) recvData.get(key_tem);
        if (curDistance != 0) {
            Intent intent = new Intent(Config.MY_ACTION);
            intent.putExtra(Config.key_broadcast_speed, distance2 - curDistance);
            sendBroadcast(intent);
        }
        curDistance = distance2;

        if (getCurState() == STATE.START) {
            txtDistance.setText("骑行圈数 " + distance2);    // 更新圈数
            // text13.setText("转速 " + Integer.parseInt(speed) + "转/分");
            txtHeart.setText(heart_rate + " 跳/分");    // 更新心跳速率
            txtWeight.setText(weight + " 千克");        // 更新当前体重
            txtTemperature.setText(temperature + " 度");        // 更新当前温度
            seekbarDistance.setProgress(distance2);            // 更新当前骑行距离
        }
    }

    static final int[] pictures = {R.drawable.n81, R.drawable.n82, R.drawable.n83, R.drawable.n84, R.drawable.n85, R.drawable.n86,
            R.drawable.n87, R.drawable.n88, R.drawable.n89, R.drawable.n810, R.drawable.n811, R.drawable.n812,
            R.drawable.n813, R.drawable.n814, R.drawable.n815, R.drawable.n816, R.drawable.n817, R.drawable.n818,
            R.drawable.n819, R.drawable.n820
    };
    int possition = 0;

    private void updataPicture() {
        imgHeart.setImageResource(pictures[possition % pictures.length]);
        String numStr = txtTemperature.getText().toString();
        int num = numStr.indexOf("度");
        float temperature = 0;
        if (num != -1) {
            temperature = Float.parseFloat(numStr.substring(0, num));
        }
        // TODO 更新温度
        final float tempA = 25f;
        final float tempB = 40f;
        clipDrawable.setLevel((int) ((temperature - tempA) / (tempB - tempA) * 10000));
        imgTemperature.setImageDrawable(clipDrawable);

        possition = possition > 65536 ? 0 : ++possition;
    }

    private void restorePicture() {
        imgHeart.setImageResource(R.drawable.n820);
        imgTemperature.setImageResource(R.drawable.m2);
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    /**
     * 上传数据
     */
    private void uploadPageData(final int distance2, final int heart_rate, final float weight, final float temperature) {
        //updateProgress = new ProgressDialog(this);
        //updateProgress.setMessage("正在上传您的运动结果");
        //updateProgress.setCancelable(false);
        //updateProgress.show();
        ThreadPool.getPool().create(Config.TASK_UPLOAD_RIDDING_RET, new Runnable() {
            @Override
            public void run() {
                String urlStr = Config.IFACE_UPLOAD_RIDRET;
                String params = "user_id="+User.getUser().getId()+"&round=" + distance2 + "&heart=" + heart_rate + "&weight=" + weight
                        + "&temperature=" + temperature + "&time=" + sdf.format(System.currentTimeMillis());
                Message msg = Message.obtain();
                try {
                    String result = NetworkController.doConnectionGetGETData(urlStr, params);
                    System.out.println("result:" + result);
                    JSONObject jsonRet = new JSONObject(result);
                    if (jsonRet.getInt("ret") == 1) {
                        Log.d(TAG, "上传成功");
                    } else {
                        Log.d(TAG, "上传失败");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    handler.sendEmptyMessage(handle_progress);
                }
            }
        });
    }

    private void set_listData() {
        data = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < image_button.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("image", image_button[i]);
            map.put("state", str_button[i]);
            data.add(map);

        }
        SimpleAdapter button_Adapter = new SimpleAdapter(Activity_Main.this, data, R.layout.list_button, new String[]{"image", "state"}, new int[]{R.id.img_btn, R.id.txt_btn});
        list_button_view.setAdapter(button_Adapter);
    }

    private void set_list_onclick() {
        list_button_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                switch (i) {
                    case 0: {
                        intent.setClass(Activity_Main.this, Activity_Person.class);
                        startActivity(intent);
                        break;
                    }
                    case 1: {
                        intent.setClass(Activity_Main.this, Activity_Ranking.class);
                        startActivity(intent);
                        break;

                    }
                    case 2: {
                        intent.setClass(Activity_Main.this, Activity_Record.class);
                        startActivity(intent);
                        break;
                    }
                    case 3: {
                        onConnectButtonClicked();
                    }
                }
            }
        });
    }

}


