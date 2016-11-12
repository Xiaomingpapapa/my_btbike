package com.example.myapplication.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * 图片加载器
 * @author long
 */
public class ImageLoader {
	public static final String TAG = "ImageLoader";
	// 图片缓存技术的核心类，用于缓存所有下载好的图片，在内存达到设定值时移除最少使用的图片
	private static LruCache<String, Bitmap> mMemoryCache;

	// ImageLoader 实例
	private static ImageLoader mImageLoader;

	private ImageLoader() {
		// 获取应用程序最大可用内存
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		// 设置图片缓存大小微程序最大可用内存的 1/8
		int cacheSize = maxMemory / 8;
		Log.d(TAG, "内存限额: "+cacheSize);
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getRowBytes() * bitmap.getHeight();
			}
		};
	}

	/**
	 * 获取 ImageLoader 实例
	 */
	public static ImageLoader getInstance() {
		if (mImageLoader == null) {
			mImageLoader = new ImageLoader();
		}
		return mImageLoader;
	}

	/**
	 * 将一张图片储存到 LruCache 中
	 * @param key 		LruCache 的键
	 * @param bitmap	LruCache 的值（bitmap）
	 */
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemoryCache(key)==null && bitmap!=null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	/**
	 * 从LruCache 中获取一张图片
	 * @param key 	LruCache 的键
	 * @return 图片
	 */
	public Bitmap getBitmapFromMemoryCache(String key) {
		return mMemoryCache.get(key);
	}

	/**
	 * 计算 缩放度 inSampleSize，压缩大小为 1/inSampleSize
	 * @param options
	 * @param reqWidth
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth) {
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (width >= reqWidth) {
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = widthRatio;
		}
		return inSampleSize;
	}

	/**
	 * 压缩图像
	 * @param options
	 * @param width
	 * @param height
	 * @return
	 */
	public static BitmapFactory.Options handleImage(BitmapFactory.Options options, int width, int height) {
		options.inJustDecodeBounds = false;
		if (width > height) {
			options.inSampleSize = calculateInSampleSize(options, width);
		} else {
			options.inSampleSize = calculateInSampleSize(options, height);
		}
		return options;
	}
	
	public static Bitmap screenShot(Activity activity){
		 //1.构建Bitmap  
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        int w = dm.widthPixels;  
        int h = dm.heightPixels;  

        Bitmap bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);      

        //2.获取屏幕  
        Rect outRect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);
        Log.d(TAG, "left:"+outRect.left+" top:"+outRect.top+" right:"+outRect.right+" bottom:"+outRect.bottom);
        
        View decorview = activity.getWindow().getDecorView();   
        decorview.setDrawingCacheEnabled(true);   
        Log.d(TAG, "decorview.getDrawingCache():"+decorview.getDrawingCache());
		bmp = Bitmap.createBitmap(decorview.getDrawingCache(), 
				outRect.left,outRect.top, outRect.right, outRect.bottom);
        return bmp;
	}

	/**
	 * 从文件中加载图片
	 * @param imagePath 图片路径
	 * @return Bitmap 实例
	 */
	public static Bitmap getImageFromFile(String key, String imagePath, int width, int height) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		if (width != 0 || height != 0) {
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(imagePath, options);
			handleImage(options, width, height);
		}
		getInstance().addBitmapToMemoryCache(key, BitmapFactory.decodeFile(imagePath, options));
		return getInstance().getBitmapFromMemoryCache(key);
	}

	/**
	 * 从 Uri 中加载图片
	 * @param mContext
	 * @param imageUri 	图片 uri
	 * @return Bitmap 	实例
	 * @throws FileNotFoundException
	 */
	public static Bitmap getImageFromUri(String key, Context mContext, Uri imageUri, int width, int height)
			throws FileNotFoundException {
		ContentResolver cr = mContext.getContentResolver();
		BitmapFactory.Options options = new BitmapFactory.Options();
		if (width != 0 || height != 0) {
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(cr.openInputStream(imageUri), null, options);
			handleImage(options, width, height);
		}
		getInstance().addBitmapToMemoryCache(key, BitmapFactory.decodeStream(cr.openInputStream(imageUri), null, options));
		return getInstance().getBitmapFromMemoryCache(key);
	}

	/**
	 * 从 InputStream 中加载图片
	 * @param key
	 * @param in 		输入流
	 * @param width 	ImageView 的宽高
	 * @param height 	ImageView 的宽高
	 * @return
	 */
	public static Bitmap getImageFromStream(String key, InputStream in, int width, int height) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		if (width != 0 || height != 0) {
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(in, null, options);
			handleImage(options, width, height);
		}
		getInstance().addBitmapToMemoryCache(key, BitmapFactory.decodeStream(in, null, options));
		return getInstance().getBitmapFromMemoryCache(key);
	}
	
	/**
	 * 从 Resource 中加载图片
	 * @param key
	 * @param res 		资源包
	 * @param resId		资源ID
	 * @param width 	ImageView 的宽高
	 * @param height 	ImageView 的宽高
	 * @return
	 */
	public static Bitmap getImageFromResource(String key, Resources res, int resId, int width, int height) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		if (width != 0 || height != 0) {
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeResource(res, resId, options);
			handleImage(options, width, height);
		}
		getInstance().addBitmapToMemoryCache(key, BitmapFactory.decodeResource(res, resId, options));
		return getInstance().getBitmapFromMemoryCache(key);
	}
	
	/**
	 * 当前内存使用量
	 * @return
	 */
	public int memeryUsed(){
		return mMemoryCache.size();
	}

}
