package com.infowarelab.conference.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.UserCommonImpl;
import com.infowarelabsdk.conference.common.impl.VideoCommonImpl;
import com.infowarelabsdk.conference.domain.UserBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

//管理视频列表
public class VideoShareAdapterByUserId extends BaseAdapter {
    private Context context;
    private ArrayList<Map<String, Object>> devices;
    private Map<Integer, Integer> syncs;
    //	private List<UserBean> videoUsers = new ArrayList<UserBean>();
    private Set<Integer> localPreviewChannelid = new HashSet<Integer>();
    private boolean isHost;

    public VideoShareAdapterByUserId(Context context, Set<Integer> locals, boolean isHost) {
        this.context = context;
        this.localPreviewChannelid = locals;
        this.isHost = isHost;
//		initUsers();
        getDevices();
    }

    public void initUsers() {
        devices = new ArrayList<Map<String, Object>>();
        syncs = ((VideoCommonImpl) CommonFactory.getInstance().getVideoCommon()).getSyncMap();
        Map<Integer, Integer> devicesMap = ((VideoCommonImpl) CommonFactory.getInstance().getVideoCommon()).getDeviceMap();
        for (Map.Entry<Integer, Integer> e : devicesMap.entrySet()) {
            if (isHost || !syncs.containsKey(e.getValue())) {
                Map<String, Object> device = new HashMap<String, Object>();
                device.put("cid", e.getKey());
                device.put("uid", e.getValue());
                device.put("name", ((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).getUser(e.getValue()).getUsername());
                devices.add(device);
            }
        }


    }

    //管理视频设备列表
    private void getDevices() {
        devices = new ArrayList<Map<String, Object>>();
        syncs = ((VideoCommonImpl) CommonFactory.getInstance().getVideoCommon()).getSyncMap();

        LinkedList<UserBean> list = (LinkedList<UserBean>) ((UserCommonImpl) CommonFactory.getInstance().getUserCommon())
                .getUserArrayList().clone();
        Map<Integer, Integer> videoDevideMap = ((VideoCommonImpl) CommonFactory.getInstance().getVideoCommon()).getDeviceMap();

        //遍历用户列表
        for (UserBean userBean : list) {
            int count = 1;
            Map<String, Object> map = null;
            while (videoDevideMap.containsValue(userBean.getUid())) {
                if (map == null) {
                    map = new HashMap<String, Object>();
                    map.put("uid", userBean.getUid());
                    map.put("name", userBean.getUsername());
                }

                //获取当前用户下面的所有设备列表
                for (Iterator<Map.Entry<Integer, Integer>> it = videoDevideMap.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<Integer, Integer> item = it.next();
                    int key = item.getKey();
                    int value = item.getValue();
                    if (value == userBean.getUid()) {
                        map.put("cid" + count, key);
                        count++;
                        it.remove();
                        break;
                    }

                }

            }
            if (map != null) devices.add(map);
        }

    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Map<String, Object> getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //刷新每个用户设备信息
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.conference_video_share_item, null);
        }
        Map<String, Object> device = devices.get(position);

        LinearLayout ll1 = (LinearLayout) convertView.findViewById(R.id.llfirstfloor);
        LinearLayout ll2 = (LinearLayout) convertView.findViewById(R.id.llExpand);
        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        ImageView ivView = (ImageView) convertView.findViewById(R.id.ivView);
        ImageView ivSync = (ImageView) convertView.findViewById(R.id.ivSync);

        tvName.setText((String) device.get("name"));
        int uid = (int) device.get("uid");
        Object object = device.get("cid2");

        //单设备用户
        if (object == null || ((Integer) object).intValue() < 1) {
            if (((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).getOwnID() == uid) {
                ivView.setVisibility(View.GONE);
            } else {
                ivView.setVisibility(View.VISIBLE);
            }
            if (((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost()) {
                ivSync.setVisibility(View.VISIBLE);
            } else {
                ivSync.setVisibility(View.GONE);
            }
            ll2.removeAllViews();
            ll2.setVisibility(View.GONE);
            int cid = (int) device.get("cid1");
            if (syncs.containsKey(cid)) {
                ivSync.setImageResource(R.drawable.icon_sharevideo_on);
            } else {
                ivSync.setImageResource(R.drawable.icon_sharevideo_normal);
            }
            if (localPreviewChannelid.contains(cid)) {
                ivView.setImageResource(R.drawable.icon_viewvideo_on);
            } else {
                ivView.setImageResource(R.drawable.icon_viewvideo_normal);
            }
            ivView.setTag(cid);
            ivSync.setTag(cid);
            ivView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkView((Integer) v.getTag());
                }
            });
            ivSync.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkSync((Integer) v.getTag());
                }
            });
//            ll1.setTag(cid);
//            ll1.setOnClickListener(new OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    check((Integer) v.getTag());
//                }
//            });
        }
        //多设备用户
        else {
            ivView.setVisibility(View.GONE);
            ivSync.setVisibility(View.GONE);
            ll2.removeAllViews();
            ll2.setVisibility(View.VISIBLE);
            for (int count = 1; count < 10; count++) {
                Object obj = device.get("cid" + count);
                int cidexpand = null == obj ? 0 : (Integer) obj;
                if (cidexpand == 0) break;
                View view = LayoutInflater.from(context).inflate(R.layout.conference_video_share_item_expand, null);
                LinearLayout ll = (LinearLayout) view.findViewById(R.id.ll1);
                TextView tv = (TextView) view.findViewById(R.id.tvName);
                ImageView iv1 = (ImageView) view.findViewById(R.id.ivView);
                ImageView iv2 = (ImageView) view.findViewById(R.id.ivSync);

                String name = "unknown";
                VideoCommonImpl videoCommon = ((VideoCommonImpl) CommonFactory.getInstance().getVideoCommon());

                if (videoCommon != null){
                    name = videoCommon.getDeviceName(cidexpand);
                }

                //设置当前用户设备的名称
                tv.setText(name); //(String) device.get("name") + "_" + count);

                if (syncs.containsKey(cidexpand)) {
                    iv2.setImageResource(R.drawable.icon_sharevideo_on);
                } else {
                    iv2.setImageResource(R.drawable.icon_sharevideo_normal);
                }

                if (localPreviewChannelid.contains(cidexpand)) {
                    iv1.setImageResource(R.drawable.icon_viewvideo_on);
                } else {
                    iv1.setImageResource(R.drawable.icon_viewvideo_normal);
                }

                iv1.setTag(cidexpand);
                iv2.setTag(cidexpand);
                iv1.setVisibility(View.VISIBLE);

                if (((UserCommonImpl) CommonFactory.getInstance().getUserCommon()).isHost()) {
                    iv2.setVisibility(View.VISIBLE);
                } else {
                    iv2.setVisibility(View.GONE);
                }

                iv1.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkView((Integer) v.getTag());
                    }
                });
                iv2.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkSync((Integer) v.getTag());
                    }
                });
//                ll.setTag(cidexpand);
//                ll.setOnClickListener(new OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        check((Integer) v.getTag());
//                    }
//                });
                ll2.addView(view);
            }
        }


        return convertView;
    }

    private void setData(final int position, View convertView) {


    }

    private ItemCheck itemCheck;

    public interface ItemCheck {
        public void checkView(int cid);

        public void checkSync(int cid);
    }

    public void setItemCheck(ItemCheck itemCheck) {
        this.itemCheck = itemCheck;
    }

    //    private void check(int uid) {
//        if (this.itemCheck != null) {
//            this.itemCheck.check(uid);
//        }
//    }
    private void checkView(int cid) {
        if (this.itemCheck != null) {
            this.itemCheck.checkView(cid);
        }
    }

    private void checkSync(int cid) {
        if (this.itemCheck != null) {
            this.itemCheck.checkSync(cid);
        }
    }
}
