package com.example.myapplication.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileUtils {
	public static final String TAG = "FileUtils";
	private static FileUtils fileUtils;

	private FileUtils() {
		System.out.println("构造FileUtils");
	}
	
	public static boolean isExist(String filePath){
		return new File(filePath).exists();
	}
	
	/**
	 * 创建文件，如已存在则删除后创建
	 * @param filePath
	 * @return 返回
	 */
	public static File newFile(String filePath) {
		File file = new File(filePath);
		File parentPath = new File(filePath.substring(0, filePath.lastIndexOf("/")));
		if(!parentPath.exists()){
			boolean result = parentPath.mkdirs();
			System.out.println("创建目录: "+result+" "+parentPath);
		}
		if(file.exists()){
			deleteFileSafely(file);
		}
		return file;
	}
	
	/**
	 * 取得一个文件，并创建目录
	 * @param filePath
	 * @return
	 */
	public static File getFile(String filePath) {
		File file = new File(filePath);
		File path = new File(filePath.substring(0, filePath.lastIndexOf("/")));
		if(!file.exists()){
			if(!path.exists()){
				path.mkdirs();
				Log.d(TAG, "创建目录: "+path);
			}
		}
		return file;
	}
	
	/**
	 * 安全删除文件
	 */
	public static boolean deleteFileSafely(File file) {
        if (file != null) {
            String tmpPath = file.getParent() + File.separator + System.currentTimeMillis();
            File tmp = new File(tmpPath);
            file.renameTo(tmp);
            return tmp.delete();
        }
        return false;
    }
	
	/**
	 * 写入文件
	 * @param file
	 * @param obj
	 * @return
	 * @throws IOException
	 */
	public static boolean writeToFile(String file, Object obj) throws IOException{
		File parent = new File(file.substring(0, file.lastIndexOf("/")));
		if(!parent.exists()){
			parent.mkdirs();
		}
		FileOutputStream fos = new FileOutputStream(file);
		byte[] buffer = obj.toString().getBytes();
		fos.write(buffer);
		fos.flush();
		fos.close();
		return true;
	}
	
	/**
	 * 从文件读取
	 * @param filePath
	 * @return
	 * @throws IOException 
	 */
	public static String readFromFile(String filePath) throws IOException {
		File file = new File(filePath);
		FileInputStream fis = new FileInputStream(file);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		StringBuilder sb = new StringBuilder();
		String line = "";
		while((line=br.readLine()) != null){
			sb.append(line);
		}
		br.close();
		return sb.toString();
	}
	
}
