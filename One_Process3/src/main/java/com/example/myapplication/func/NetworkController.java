package com.example.myapplication.func;

import com.example.myapplication.func.model.NetworkModel;
import com.example.myapplication.utils.FileUtils;
import com.example.myapplication.utils.MyException;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLConnection;


/**
 * 网络连接控制
 * @author long
 */
public class NetworkController {
	public static final int handle_netstatuschange = 9701;
	
	public static final String MSG_CANNOTCONNECT = "无法连接服务器";
	
	private static NetworkController mNetworkController;
	
	// Interfaces
	private static OnNetworkStatusChangeListener mOnNetworkStatusChangeListener;
	
	
	/**
	 * 获取 NetworkController 实例
	 * @return NetworkController 实例
	 */
	public static NetworkController getInstance(){
		if(mNetworkController == null){
			mNetworkController = new NetworkController();
		}
		return mNetworkController;
	}
	
	/**
	 * 接口实现方法 
	 * @param mOnNetworkStatusChangeListener
	 */
	public void setOnNetworkStatusChangeListener(OnNetworkStatusChangeListener mOnNetworkStatusChangeListener){
		NetworkController.mOnNetworkStatusChangeListener = mOnNetworkStatusChangeListener;
	}
	
	/**
	 * POST 获取数据
	 * @param urlStr URL地址ַ
	 * @param params 参数
	 * @return 字符串数据
	 * @throws IOException 无法连接
	 */
	public static String doConnectionGetPOSTData(String urlStr, String params) throws IOException{
		String result = "";
		URLConnection conn = null;
		try{
			conn = NetworkModel.getURLConnection(urlStr);
			result = NetworkModel.getDataWithParams(conn, params);
		}catch(IOException e){
			if(mOnNetworkStatusChangeListener != null){
				mOnNetworkStatusChangeListener.onNetworkStatusChanged();
			}
			throw new IOException(""+e.getMessage());
		}
		return result;
	}
	
	/**
	 * GET 获取数据
	 * 
	 * @param urlStr URL地址ַ
	 * @param params 参数
	 * @return 字符串数据
	 * @throws IOException 无法连接
	 */
	public static String doConnectionGetGETData(String urlStr, String params) throws IOException{
		String result = "";
		URLConnection conn = null;
		try{
			conn = NetworkModel.getURLConnection(urlStr+"?"+params);
			result = NetworkModel.getDataWithoutParams(conn);
		}catch(IOException e){
//			mHandler.sendEmptyMessage(handle_netstatuschange);
			if(mOnNetworkStatusChangeListener != null){
				mOnNetworkStatusChangeListener.onNetworkStatusChanged();
			}
			throw e;
		}
		return result;
	}
	
	/**
	 * 下载文件
	 * @param urlStr	文件 URL
	 * @param filePath	文件保存路径
	 * @param fileName	文件名
	 * @return	文件句柄
	 * @throws IOException
	 */
	public static File downloadFile(String urlStr, String filePath, String fileName) throws IOException{
		FileUtils.newFile(filePath);
		File file = null;
		URLConnection conn = null;
		try{
			conn = NetworkModel.getURLConnection(urlStr);
			file = NetworkModel.download(filePath, conn);
		}catch(IOException e){
			if(mOnNetworkStatusChangeListener != null){
				mOnNetworkStatusChangeListener.onNetworkStatusChanged();
			}
			throw e;
		}
		return file;
	}
	
	/**
	 * 发送字节数据
	 * @throws IOException 
	 * @throws MyException 
	 */
	public static byte[] sendBuffer(String ipport, byte[] buffer) throws IOException, MyException {
		byte[] result = null;
		Socket s = null;
		try {
			s = NetworkModel.getSocket(ipport);
			NetworkModel.sendBuffer(s.getOutputStream(), buffer);
			result = NetworkModel.readBuffer(s.getInputStream());
		}catch(SocketTimeoutException e){
			throw new MyException("请求超时");
		}catch (IOException e) {
			throw new MyException("网络错误");
		}finally {
			if(s != null){
				NetworkModel.closeConnection(s);
			}
		}
		return result;
	}
	
	/**
	 * 发送字节数据
	 * @throws IOException 
	 * @throws MyException 
	 */
	public static void sendBufferWithoutRecieve(String ipport, byte[] buffer) throws IOException, MyException {
		Socket s = null;
		try {
			s = NetworkModel.getSocket(ipport);
			NetworkModel.sendBuffer(s.getOutputStream(), buffer);
		}catch(SocketTimeoutException e){
			throw new MyException("请求超时");
		}catch (IOException e) {
			throw new MyException("网络错误");
		}finally {
			if(s != null){
				NetworkModel.closeConnection(s);
			}
		}
	}
	
	/**
	 * 网络连接变化回调接口
	 * @author long
	 */
	public interface OnNetworkStatusChangeListener{
		public void onNetworkStatusChanged();
	}

}
