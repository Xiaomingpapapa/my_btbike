package com.example.myapplication.utils;

import com.example.myapplication.R;

/**
 * Created by Long on 2016/7/21.
 */
public class Config {

	private static final String SERVER_HOST = "http://115.159.42.189/";
	private static final String SERVER_APP = "ThinkPHP/btbike.php/Home/";
	public static final String IFACE_LOGIN 			= SERVER_HOST + SERVER_APP + "User/login";
	public static final String IFACE_REG 			= SERVER_HOST + SERVER_APP + "User/register";
	public static final String IFACE_UPLOAD_RIDRET 	= SERVER_HOST + SERVER_APP + "Record/saveData";
	public static final String IFACE_GET_PERSONDATA = SERVER_HOST + SERVER_APP + "Ranking/getPersonData";
	public static final String IFACE_GET_RANKING 	= SERVER_HOST + SERVER_APP + "Ranking/getRankList";
	public static final String IFACE_GET_HISTORY 	= SERVER_HOST + SERVER_APP + "Record/get7DaysRecord";

	// 线程名，防止重复创建线程
	public static final String TASK_UPLOAD_RIDDING_RET = "upload_riddingret";
	public static final String TASK_DOWNLOAD_PERSON_DATA = "download_person_data";
	public static final String TASK_DOWNLOAD_RANK_DATA = "download_rank_data";
	public static final String TASK_DOWNLOAD_HISTORY_DATA = "download_history_data";
	public static final String TASK_LOGIN = "login_user";
	public static final String TASK_REG = "reg_user";
	public static final String TASK_UPDATE_MAIN_PIC = "updatepicture";

	// Broadcast 的 Action
	public static final String MY_ACTION = "com.example.myapplication.BroadCast";
	public static final String key_broadcast_speed = "speed";

	// 用户信息字段
	public static final String column_user_id = "id";
	public static final String column_user_name = "name";
	public static final String column_user_tel = "tel";
	public static final String column_user_face = "face";
	public static final String column_user_sex = "sex";

	public static final int[] userIconIds = {
			R.drawable.r5, R.drawable.r6, R.drawable.r7,
			R.drawable.r8, R.drawable.r9, R.drawable.r10
	};

}
