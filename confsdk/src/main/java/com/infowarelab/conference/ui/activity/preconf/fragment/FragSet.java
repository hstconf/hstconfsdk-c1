package com.infowarelab.conference.ui.activity.preconf.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.conference.ui.activity.preconf.ActSetAbout;
import com.infowarelab.conference.ui.activity.preconf.ActSetResolution;
import com.infowarelab.conference.ui.activity.preconf.ActSetSite;
import com.infowarelab.conference.ui.activity.preconf.BaseFragment;
import com.infowarelab.conference.ui.activity.preconf.LoginActivity;
import com.infowarelab.conference.ui.view.ClearCacheDialog;
import com.infowarelab.hongshantongphone.R;

import java.io.File;
import java.text.DecimalFormat;

public class FragSet extends BaseFragment implements OnClickListener {
    private View setView;
    private TextView tvCache;
    private LinearLayout ll1, ll2, ll3, ll4, ll5;

    private static final int GETCACHE = 1;
    private static final int CLEARCACHE = 2;

    public static final int REQUESTCODE_RESOLUTION = 96;
    public static final int REQUESTCODE_SITE = 97;
    public static final int REQUESTCODE_LOGIN = 99;
    public static final int REQUESTCODE_LOGIN_CREATE = 98;

    private ClearCacheDialog cacheDialog;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        setView = inflater.inflate(R.layout.a6_preconf_set, container, false);
        initView();
        return setView;
    }

    private void initView() {
        ll1 = (LinearLayout) setView.findViewById(R.id.act_preconf_set_ll_1);
        ll2 = (LinearLayout) setView.findViewById(R.id.act_preconf_set_ll_2);
        ll3 = (LinearLayout) setView.findViewById(R.id.act_preconf_set_ll_3);
        ll4 = (LinearLayout) setView.findViewById(R.id.act_preconf_set_ll_4);
        ll5 = (LinearLayout) setView.findViewById(R.id.act_preconf_set_ll_5);
        tvCache = (TextView) setView.findViewById(R.id.act_preconf_set_tv_cache);

        ll1.setOnClickListener(this);
        ll2.setOnClickListener(this);
        ll3.setOnClickListener(this);
        ll4.setOnClickListener(this);
        ll5.setOnClickListener(this);

        new Thread(new MyThread1()).start();
    }

    private Handler setHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GETCACHE:
                    tvCache.setText(String.valueOf(msg.obj));
                    break;
                case CLEARCACHE:
                    Toast.makeText(getActivity(), getResources().getString(R.string.preconf_set_cache_2), Toast.LENGTH_SHORT).show();
                    tvCache.setText(String.valueOf(msg.obj));
                    break;
                default:
                    break;
            }
        }

        ;
    };

    /**
     * 显示提示框
     */
//	private void showAlertDialog(){
//		alertDialog = new AlertDialog.Builder(getActivity());  
//	    alertDialog.setCustomTitle(LayoutInflater.from(getActivity()).inflate(R.layout.notify_dialog, null));
//        alertDialog.setPositiveButton(getResources().getString(R.string.confirm),  
//                new DialogInterface.OnClickListener() {  
//                    @Override  
//                    public void onClick(DialogInterface dialog, int which) {
//                    	sendIntent();
//                    }  
//                });
//	    alertDialog.setNegativeButton(getResources().getString(R.string.cancel),
//	    		new DialogInterface.OnClickListener() {  
//                    @Override  
//                    public void onClick(DialogInterface dialog, int which) {  
//                    	FileUtil.saveSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES,
//								Constants.SITE_NAME, "");
//						FileUtil.saveSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES,
//								Constants.SITE_ID, "");
//						FileUtil.saveSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES,
//								Constants.SITE, "");
//						FileUtil.saveSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES,
//								Constants.HAS_LIVE_SERVER, "");
//                    	dialog.dismiss();
//                    }  
//                });  
//        alertDialog.show();
//	}
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.act_preconf_set_ll_1) {
            jump2Login(REQUESTCODE_LOGIN);
        } else if (id == R.id.act_preconf_set_ll_2) {
            jump2Site(REQUESTCODE_SITE);
        } else if (id == R.id.act_preconf_set_ll_3) {
            jump2Video(REQUESTCODE_RESOLUTION);
        } else if (id == R.id.act_preconf_set_ll_4) {
            showCacheDialog();
        } else if (id == R.id.act_preconf_set_ll_5) {
            jump2About(0);
        }
    }

    private void jump2Login(int req) {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.putExtra("state", 1);
        intent.putExtra("turnIndex", 1);
        getActivity().startActivityForResult(intent, req);
    }

    private void jump2Site(int req) {
        Intent intent = new Intent(getActivity(), ActSetSite.class);
        getActivity().startActivityForResult(intent, req);
    }

    private void jump2Video(int req) {
        Intent intent = new Intent(getActivity(), ActSetResolution.class);
        getActivity().startActivityForResult(intent, req);
    }

    private void jump2About(int req) {
        Intent intent = new Intent(getActivity(), ActSetAbout.class);
        getActivity().startActivityForResult(intent, req);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        // TODO Auto-generated method stub
        super.onHiddenChanged(hidden);
    }

    public void showCacheDialog() {
        if (cacheDialog == null) {
            cacheDialog = new ClearCacheDialog(getActivity(), ConferenceApplication.SCREEN_WIDTH > ConferenceApplication.SCREEN_HEIGHT ? ConferenceApplication.SCREEN_HEIGHT * 4 / 5 : ConferenceApplication.SCREEN_WIDTH * 4 / 5);
            cacheDialog.setClickListener(new ClearCacheDialog.OnResultListener() {

                @Override
                public void doYes() {
                    // TODO Auto-generated method stub
                    new Thread(new MyThread2()).start();
                }

                @Override
                public void doNo() {
                    // TODO Auto-generated method stub

                }
            });
        }
        if (cacheDialog != null && !cacheDialog.isShowing()) {
            cacheDialog.show();
        }
    }

    public static long getFolderSize(File file) {

        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);

                } else {
                    size = size + fileList[i].length();

                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block  
            e.printStackTrace();
        }
        //return size/1048576;
        return size;
    }

    public String FormetFileSize(long fileS) {// 转换文件大小
        DecimalFormat df = new DecimalFormat("#0.00");
        String fileSizeString = "";
//	  if (fileS < 1024)
//	  {
//	   fileSizeString = fileS + "B";
//	  }
//	  else if (fileS < 1048576)
//	   {
//	    fileSizeString = df.format((double) fileS / 1024) + "K";
//	   }
//	  else 
        if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

    public String getFilePath() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory() + File.separator + "infowarelab";
        } else {
            return getActivity().getCacheDir() + File.separator + "infowarelab";
        }
    }

    public void deleteFolderFile(String filePath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                if (file.isDirectory()) {// 处理目录     
                    File files[] = file.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        deleteFolderFile(files[i].getAbsolutePath(), true);
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory()) {// 如果是文件，删除     
                        file.delete();
                    } else {// 目录     
                        if (file.listFiles().length == 0) {// 目录下没有文件或者目录，删除
                            file.delete();
                        }
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block  
                e.printStackTrace();
            }
        }
    }

    public class MyThread1 implements Runnable {      // thread
        @Override
        public void run() {
            try {
                long size = getFolderSize(new File(getFilePath()));
                Message message = new Message();
                message.what = GETCACHE;
                message.obj = FormetFileSize(size);
                setHandler.sendMessage(message);
            } catch (Exception e) {
            }
        }
    }

    public class MyThread2 implements Runnable {      // thread
        @Override
        public void run() {
            try {
                deleteFolderFile(getFilePath(), false);
                long size = getFolderSize(new File(getFilePath()));
                Message message = new Message();
                message.what = CLEARCACHE;
                message.obj = FormetFileSize(size);
                setHandler.sendMessage(message);
            } catch (Exception e) {
            }
        }
    }
}
