package com.example.myapplication.func;

import android.util.Log;

import com.example.myapplication.func.model.MyThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThreadPool {
	public static final String TAG = "ThreadPool";
	private static ThreadPool mThreadPool;
	
	// 任务数量限制
	private static int MAX_RUN = 10;
	private static int MAX_WAIT = 100;
	
	/**
	 * 执行中任务
	 */
	private Map<String, MyThread> runningPool;
	
	/**
	 * 等待中任务
	 */
	private List<Map<String, Object>> waitingQueue;
	
	private ThreadPool() {
	}
	
	/**
	 * 获取唯一线程池
	 */
	public static ThreadPool getPool(){
		if(mThreadPool == null){
			mThreadPool = new ThreadPool();
			mThreadPool.runningPool = new HashMap<String, MyThread>();
			mThreadPool.waitingQueue = new ArrayList<Map<String, Object>>();
		}
		return mThreadPool;
	}
	
	/**
	 * 创建匿名任务，只可创建一个
	 * @return 是否创建成功
	 */
	public boolean create(Runnable run){
		return create("Anonymous", run);
	}
	
	/**
	 * 创建指定任务
	 * @return 是否创建成功
	 */
	public boolean create(String key, Runnable run){
		if(runningPool.containsKey(key)){
			Log.d(TAG, "任务 "+key+" 已经在执行");
			return false;
		}else if(runningPool.size()>MAX_RUN && waitingQueue.size()<=MAX_WAIT){
			Log.d(TAG, "ִ任务 "+key+" 加入等待队列，前方还有"+waitingQueue.size()+" 个任务");
			Map<String, Object> task = new HashMap<String, Object>();
			task.put("key", key);
			task.put("task", run);
			waitingQueue.add(task);
		}else if(waitingQueue.size()>MAX_WAIT){
			Log.e(TAG, "超出限值 "+key+" 不被执行");
			return false;
		}else{
			runningPool.put(key, new MyThread(key, run));
			startTask(key);
		}
		return true;
	}
	
	/**
	 * 启动任务
	 * @param key
	 */
	private void startTask(String key){
		Log.d(TAG, "启动任务: "+key);
		runningPool.get(key).setPostTaskFinishedListener(new MyThread.PostTaskFinishedListener() {
			public void onTaskFinished(String key) {
				onTaskFinish(key);
			}
		});
		runningPool.get(key).start();
	}
	
	/**
	 * 关闭任务
	 */
	public void cancelTask(String key){
		Log.d(TAG, "关闭任务: "+key);
	}
	
	/**
	 * 任务结束
	 */
	private void onTaskFinish(String key){
		Log.d(TAG, "任务: "+key+" 执行结束");
		runningPool.remove(key);
		
		// 从等待队列中拉取一个任务来执行
		for(int i=0; i<MAX_RUN-runningPool.size() && waitingQueue.size()>0; i++){
			Map<String, Object> map = waitingQueue.get(0);
			String taskKey = ""+map.get("key");
			Log.d(TAG, "拉取任务: "+taskKey);
			runningPool.put(taskKey, new MyThread(taskKey, (Runnable)map.get("task")));
			waitingQueue.remove(0);
			startTask(taskKey);
		}
	}
	
}
