package com.example.myapplication.func.model;

public class MyThread extends Thread{
	public static final int handle_start = 9701;
	public static final int handle_finish = 9702;
	
	private Runnable task;
	
	public boolean isRunning = false;
	public boolean isFinished = false;
	
	public String taskKey;
	
	
	// interface
	private PostTaskFinishedListener mPostTaskFinishedListener;
	
	/**
	 * 创建一个任务
	 * @param runnable
	 */
	public MyThread(String key, Runnable runnable){
		setTast(key, runnable);
	}
	
	/**
	 * 设置
	 * @param runnable	任务
	 */
	public void setTast(String key, Runnable runnable){
		taskKey = key;
		task = runnable;
	}
	
	/**
	 * 执行任务流程
	 */
	@Override
	public void run() {
		super.run();
		task.run();	// 执行用户任务
		
		mPostTaskFinishedListener.onTaskFinished(taskKey);	// 用户任务结束回调
	}
	
	// 任务结束回调
	public void setPostTaskFinishedListener(PostTaskFinishedListener mPostTaskFinishedListener){
		this.mPostTaskFinishedListener = mPostTaskFinishedListener;
	}
	public interface PostTaskFinishedListener{
		public void onTaskFinished(String key);
	}

}
