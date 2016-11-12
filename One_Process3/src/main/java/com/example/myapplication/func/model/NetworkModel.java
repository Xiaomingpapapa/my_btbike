package com.example.myapplication.func.model;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;

/**
 * 网络连接模型类
 * @author long
 */
public class NetworkModel {
	public static final String TAG = "NetworkModel";
	private static NetworkModel mNetworkModel;
	public boolean isNetConn 		= false;	// 网络连接状态
	public boolean isServerConn 	= false;	// 服务器链接状态
	public boolean isDeviceOnline 	= false;	// 设备在线状态
	
	private static final int connTimeout = 10 * 1000;	// 10 秒超时
	private static final int readTimeout = 3 * 1000;	// 3 秒超时
	private static final int bufferCacheSize = 2048;	// 缓冲区大小
	
	
	/**
	 * 获取 NetworkModel 实例
	 * @return  NetworkModel 实例
	 */
	public static NetworkModel getInstance(){
		if(mNetworkModel == null){
			mNetworkModel = new NetworkModel();
		}
		return mNetworkModel;
	}
	
	/**
	 * 获取 URLConnection 实例
	 * @param urlStr URL地址ַ
	 * @return URLConnection 实例
	 * @throws IOException
	 */
	public static URLConnection getURLConnection(String urlStr) throws IOException{
//		urlStr = URLEncoder.encode(urlStr, "utf-8");
		Log.d(TAG, "urlStr => " + urlStr);
		URL url = new URL(urlStr);
		URLConnection conn = url.openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setConnectTimeout(connTimeout);
		conn.setReadTimeout(readTimeout);
		return conn;
	}
	
	/**
	 * 传入参数以获取回执数据
	 * @param conn URL地址
	 * @param params POST参数
	 * @return 回执数据
	 * @throws IOException 
	 */
	public static String getDataWithParams(URLConnection conn, String params) throws IOException{
		String result = "";
		String line = "";
		PrintWriter pw = new PrintWriter(conn.getOutputStream());
		pw.print(params);
		pw.flush();
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
		while((line = br.readLine()) != null){
			result += line;
		}
		if(pw != null){
			pw.close();
		}
		if(br != null){
			br.close();
		}
		return result;
	}
	
	/**
	 * 不传入参数获取回执数据
	 * @param conn URL地址
	 * @return 回执数据
	 * @throws IOException 
	 */
	public static String getDataWithoutParams(URLConnection conn) throws IOException{
		String result = "";
		String line = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
		while((line = br.readLine()) != null){
			result += line;
		}
		return result;
	}
	
	/**
	 * 获取指定位置 Socket 连接 
	 * @return
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public static Socket getSocket(String ipport) throws IOException{
		String ipaddr = ipport.substring(0, ipport.indexOf(":"));
		int port = Integer.parseInt(ipport.substring(ipport.indexOf(":")+1, ipport.length()));
		Log.d(TAG, "ipaddr:"+ipaddr+" port:"+port);
		
		Socket s = new Socket(ipaddr, port);
		s.setSoTimeout(3000);
		return s;
	}
	
	/**
	 * 发送字节流 
	 * @param os
	 * @param buffer
	 * @throws IOException
	 */
	public static void sendBuffer(OutputStream os, byte[] buffer) throws IOException{
		os.write(buffer);
		os.flush();
	}
	
	/**
	 * 接收字节
	 * @return
	 * @throws IOException
	 */
	public static byte[] readBuffer(InputStream is) throws IOException{
		byte[] tmp = new byte[bufferCacheSize];
		int length = is.read(tmp);
		
		byte[] result = null;
		if(length > 0){
			result = new byte[length];
			for(int i=0; i<length; i++){
				result[i] = tmp[i];
			}
		}
		
		return result;
	}
	
	/**
	 * 关闭 Socket 连接
	 * @param s
	 */
	public static void closeConnection(Socket s){
		try {
			if(!s.isClosed()){
				s.getOutputStream().close();
			}
		} catch (IOException e) {}
		try {
			if(!s.isClosed()){
				s.getInputStream().close();
			}
		} catch (IOException e) {}
		try {
			if(!s.isClosed()){
				s.close();
			}
		} catch (IOException e) {}
	}
	
	/**
	 * 通过 Url 下载文件
	 * @param filePath
	 * @param conn
	 * @return 
	 * @throws IOException 
	 */
	public static File download(String filePath, URLConnection conn) throws IOException{
		File file = new File(filePath);
		InputStream is = conn.getInputStream();

		byte[] bs = new byte[1024];
		OutputStream os = new FileOutputStream(file);
		for(int len; (len = is.read(bs)) != -1; ){
			os.write(bs, 0, len);
		}
		os.close();  
		is.close();
		return file;
	}

}
