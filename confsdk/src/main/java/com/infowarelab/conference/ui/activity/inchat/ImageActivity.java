
package com.infowarelab.conference.ui.activity.inchat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.infowarelab.hongshantongphone.R;
import com.infowarelab.conference.localDataCommon.LocalCommonFactory;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.DocCommonImpl;
import com.infowarelabsdk.conference.domain.DocBean;
import com.infowarelabsdk.conference.domain.PageBean;

public class ImageActivity extends Activity{
	private DocCommonImpl docCommon;
	
	private static int flag;
	public static final int PHOTO_REQUEST_GALLERY = 1;
	public static final int PHOTO_REQUEST_TAKEPHOTO = 2;
	public static final int MAX_WIDTH = 200;  
    public static final int MAX_HEIGHT = 200;
	private File tempFile;
	private String name;
	private String character;
		
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		Bundle bundle = new Bundle();
		bundle = this.getIntent().getExtras();
		flag = Integer.parseInt(bundle.getString("flag"));
		if(null==LocalCommonFactory.getInstance()||null==LocalCommonFactory.getInstance().getContactDataCommon()){
			finish();
		}

		Log.d("InfowareLab.Debug", "ImageActivity.onCreate");

		LocalCommonFactory.getInstance().getContactDataCommon().setHandler(handler);
		docCommon = (DocCommonImpl) CommonFactory.getInstance().getDocCommon();
		
//		tempFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), getPhotoFileName());
//		tempFile = new File(ConferenceApplication.getConferenceApp().getFilePath(), getPhotoFileName());

		tempFile = new File(getExternalCacheDir(),getPhotoFileName());

		Log.i("imageactivity", "imageactivity ="+Environment.getExternalStorageDirectory().getAbsolutePath());
		switch(flag) {
		case PHOTO_REQUEST_GALLERY:
    		Intent localIntent = new Intent();  
    		localIntent.setType("image/*");  
    		localIntent.setAction("android.intent.action.GET_CONTENT");  
    		startActivityForResult(localIntent, PHOTO_REQUEST_GALLERY); 
			break;
		case PHOTO_REQUEST_TAKEPHOTO:
			//考虑中途关闭了本地视频的可能
			if(bundle.getBoolean("isCameraOpen")){
				setContentView(R.layout.conference_share_addfile);
			}else{
				getSystemCamera();
			}
			
			break;
		}
	}
	
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == 1001){
				return;			 
			}else if(msg.what == 1002){
				getSystemCamera();
			}
		}
	};


	private void getSystemCamera(){

		//启动相机程序
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");

		//Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
//		startActivityForResult(intent, PHOTO_REQUEST_TAKEPHOTO);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
		}
		intent.putExtra(MediaStore.EXTRA_OUTPUT,getUriForFile(ImageActivity.this,tempFile));
		startActivityForResult(intent, PHOTO_REQUEST_TAKEPHOTO);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		Log.d("InfowareLab.Debug", "ImageActivity.onActivityResult");
		switch (requestCode) {
		case PHOTO_REQUEST_GALLERY:
			if(data == null){
				finish();
			}else{
				Uri imageFileUri = data.getData();
	            
	            try {  
	                BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();  
	                bmpFactoryOptions.inJustDecodeBounds = true;

					BitmapFactory.decodeStream(getContentResolver().openInputStream(imageFileUri), null, bmpFactoryOptions);
					Bitmap bmp;

	                int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth / (float) 1080);  
	                if (widthRatio > 1) {  
	                    bmpFactoryOptions.inSampleSize = widthRatio;  
	                }  
	                  
	                bmpFactoryOptions.inJustDecodeBounds = false;  
	                bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageFileUri),null, bmpFactoryOptions);

					if (null == bmp) {
						Toast.makeText(this, "错误：无法打开选定的图片文件。", Toast.LENGTH_LONG).show();
						finish();
						break;
					}

	                sharePhoto(bmp);
	                finish();
	                  
	            } catch (Exception e) {  
	                // TODO: handle exception  
	            }
			}			
			break;
		
    	case PHOTO_REQUEST_TAKEPHOTO:

    		sendPhotoFromCamera();
    		break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * 从摄像头拍照获得图片
	 */
	private void sendPhotoFromCamera(){
		try {  
            BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();  
            bmpFactoryOptions.inJustDecodeBounds = true;  
            Bitmap bmp ;
              
            int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth / (float) 1080);  
            if (widthRatio > 1){  
                bmpFactoryOptions.inSampleSize = widthRatio;  
            }  

            bmpFactoryOptions.inJustDecodeBounds = false;

			bmp = BitmapFactory.decodeFile(tempFile.getPath(), bmpFactoryOptions);
			int degree = readPictureDegree(tempFile.getPath());
			if(degree!=0){
				Matrix m = new Matrix();
				int width = bmp.getWidth();
				int height = bmp.getHeight();
				m.setRotate(degree); // 旋转angle度
				bmp = Bitmap.createBitmap(bmp, 0, 0, width, height,m, true);
			}

            if(bmp!= null && bmp.getWidth() != 0 && bmp.getHeight() != 0){
            	sharePhoto(bmp);
            }
            
            finish();
        } catch (Exception e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }
	}

	public static int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation =
					exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
							ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					degree = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					degree = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					degree = 270;
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/**
	 * 设置图片存储的路径
	 * @return
	 */
 	private String getPhotoFileName() {
 		Date date = new Date(System.currentTimeMillis());
 		SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
 		character = "jpg";
 		name = dateFormat.format(date) + ".jpg";
 		return name;
 	}
 	
 	/**
 	 * 分享本地图片
 	 * @param photo
 	 */
 	private void sharePhoto(Bitmap photo){
 		DocBean docbean = new DocBean();
		docbean.setTitle(name);
		docbean.setPageCount(1);
		int docID = docCommon.shareDoc(docbean);
		docbean.setDocID(docID);
		docbean.setLocal(true);
		docCommon.onShareDoc(docbean);
		
		
		PageBean page = new PageBean();
		page.setDocID(docID);
		page.setWidth(photo.getWidth());
		page.setHeight(photo.getHeight());
		page.setPageID(1);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		CompressFormat format = null;
		if(character.equals("jpg") || character.equals("JPEG")){
			format = CompressFormat.JPEG;
		}else if(character.equals("png")){
			format = CompressFormat.PNG;
		}
		photo.compress(format, 100, baos);
		byte[] data = baos.toByteArray();
		page.setRawDate(data);
		page.setLength(data.length);
		docCommon.newPage(page);
		docCommon.onPageData(page);
 	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
	}
	/**
	 * 将文件转换成uri(支持7.0)
	 * @param mContext
	 * @param file
	 * @return
	 */
	private Uri getUriForFile(Context mContext, File file) {
		Uri fileUri = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			fileUri = FileProvider.getUriForFile(mContext, "com.infowarelab.fileprovider", file);
		} else {
			fileUri = Uri.fromFile(file);
		}
		return fileUri;
	}

	@Override
	public void onDestroy() {
//		if (docCommon != null) {
//			docCommon.setHandler(null);
//		}
		LocalCommonFactory.getInstance().getContactDataCommon().setHandler(null);
		Log.d("InfowareLab.Debug", "ImageActivity.onDestroy");

		super.onDestroy();
	}
    
}
