package com.infowarelab.conference.ui.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.infowarelab.conference.ui.activity.inconf.ConferenceActivity;
import com.infowarelab.conference.ui.activity.preconf.fragment.FragHistory;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.domain.ConferenceBean;
import com.infowarelabsdk.conference.transfer.Config;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author joe.xiao
 * @Date 2013-9-10下午1:46:13
 * @Email joe.xiao@infowarelab.com
 */
public class HConferenceListAdapter4Frag extends BaseAdapter {

    private Context mContext;
    private FragHistory fragHistory;

    //private static final Logger log = Logger.getLogger(ConferenceListAdapter4Frag.class);

    /**
     * 正在进行的会议列表
     */
//	private List<ConferenceBean> progressList;

    /**
     * 未开始会议列表
     */
//	private List<ConferenceBean> noStartConfList;

    /**
     * 全部的会议列表
     */
    private List<ConferenceBean> confList;

    private int selectItem;
    private String userName;

    private LayoutInflater mInflater;
    private String[] titles;
    private String pre;
    private String end;

    public HConferenceListAdapter4Frag(Context context, FragHistory fragHistory, List<ConferenceBean> confs, int defaultSelected) {
        this.mContext = context;
        this.fragHistory = fragHistory;
        this.selectItem = defaultSelected;
        titles = new String[]{mContext.getResources().getString(R.string.myconf),
                mContext.getResources().getString(R.string.publicconf)};
        mInflater = LayoutInflater.from(context);

        setAdapterData(confs);
    }

    /**
     * 设置Adapter的数据内容
     */
    public void setAdapterData(List<ConferenceBean> confs) {
        if (confList != null) {
            confList = null;
        }
        confList = confs;
        userName = FileUtil.readSharedPreferences(mContext, Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME);
//		progressList = confs.get(Config.PROGRESSCONFS);
//		noStartConfList = confs.get(Config.CONFS);
//		confList = new ArrayList<ConferenceBean>();
//		confList.addAll(noStartConfList);
//		confList.addAll(0, progressList);
//		//log.info("progressList=" + progressList.size());

    }

    /**
     * @param selectItem
     */
    public void setSelectItem(int selectItem) {
        this.selectItem = selectItem;
    }

    @Override
    public int getCount() {
        if (confList != null)
            return confList.size();
        return 0;
    }

    @Override
    public Object getItem(int position) {

        if (confList != null)
            return confList.get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder = null;
        if (convertView == null) {

            if (fragHistory.isLandscape())
                convertView = mInflater.inflate(R.layout.a6_preconf_join_page_list_item, null);
            else
                convertView = mInflater.inflate(R.layout.a6_preconf_join_page_list_item_portrait, null);

            mViewHolder = new ViewHolder();
            mViewHolder.llType = (LinearLayout) convertView.findViewById(R.id.llmeetingType);
            mViewHolder.meetingType = (TextView) convertView.findViewById(R.id.meetingType);
            mViewHolder.meetingTitle = (TextView) convertView.findViewById(R.id.meetingTitle);
			mViewHolder.meetingNumber = (TextView) convertView.findViewById(R.id.meetingNumber);
            mViewHolder.meetingHostName = (TextView) convertView.findViewById(R.id.meetingHostName);
            mViewHolder.meetingStartTime = (TextView) convertView.findViewById(R.id.meetingStartTime);
            mViewHolder.meetingState = (ImageView) convertView.findViewById(R.id.meetingState);
            mViewHolder.btnStartMeeting = convertView.findViewById(R.id.btn_start_conf);

            mViewHolder.ivCopy = convertView.findViewById(R.id.iv_copy);
            mViewHolder.ivPortrait = convertView.findViewById(R.id.iv_portrait);

//			mViewHolder.startPublcMeeting = (ImageButton)convertView.findViewById(R.id.startMeeting_public);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        //setItemClick(convertView, (position + 1));
		setItemClick(mViewHolder.btnStartMeeting, (position + 1));
        contaionConfs(mViewHolder, position);
        Date starDate = confList.get(position).getStartDate();
        mViewHolder.meetingTitle.setText(confList.get(position).getName());
        mViewHolder.meetingTitle.getPaint().setFakeBoldText(true);

//		pre = confList.get(position).getId().substring(0, 4);
//		end = confList.get(position).getId().substring(4);
//		mViewHolder.meetingNumber.setText(mContext.getResources().getString(R.string.more_id) + pre + " " + end);
        if (confList.get(position).getHostDisplayName() == null ||
                confList.get(position).getHostDisplayName().trim().equals("")) {
            mViewHolder.meetingHostName.setText(confList.get(position).getHostName()
//					+ mContext.getResources().getString(R.string.host)
            );
        } else {
            mViewHolder.meetingHostName.setText(confList.get(position).getHostDisplayName()
//					+ mContext.getResources().getString(R.string.host)
            );
        }

        mViewHolder.meetingStartTime.setText(dateToString(confList.get(position).getStartDate()));
        mViewHolder.meetingNumber.setText(confList.get(position).getId());

        String hostName = mViewHolder.meetingHostName.getText().toString();
        if (TextUtils.isEmpty(hostName)){
            mViewHolder.meetingHostName.setVisibility(View.GONE);
            mViewHolder.ivPortrait.setVisibility(View.GONE);
        }
        else {
            mViewHolder.ivPortrait.setVisibility(View.VISIBLE);
            mViewHolder.meetingHostName.setVisibility(View.VISIBLE);
        }

        setConfIdClick(mViewHolder.meetingNumber, position);
        setConfIdClick(mViewHolder.ivCopy, mViewHolder.meetingNumber.getText().toString(), position);
        setConfIdClick(mViewHolder.ivPortrait, mViewHolder.meetingNumber.getText().toString(), position);
        setConfIdClick(mViewHolder.meetingHostName, mViewHolder.meetingNumber.getText().toString(), position);

        setConfIdClick(convertView, mViewHolder.meetingNumber.getText().toString(), position);

        return convertView;
    }

    private void setConfIdClick(View tvControl, String text, final int position) {
        tvControl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                copyStr(text);
                Toast.makeText(mContext, text + "拷贝到剪贴板。", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 复制内容到剪切板
     *
     * @param copyStr
     * @return
     */
    private boolean copyStr(String copyStr) {
        try {
            //获取剪贴板管理器
            ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            // 创建普通字符型ClipData
            ClipData mClipData = ClipData.newPlainText("Label", copyStr);
            // 将ClipData内容放到系统剪贴板里。
            cm.setPrimaryClip(mClipData);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void setItemClick(View convertView, final int position) {
        convertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                fragHistory.setJoin(position);

            }
        });
    }

    private void setConfIdClick(TextView tvConfId, final int position) {
        tvConfId.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //fragHistory.setJoin(position);
                String confId = tvConfId.getText().toString();
                copyStr(confId);
                Toast.makeText(mContext, confId + "拷贝到剪贴板。", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class ViewHolder {
        public TextView meetingNumber;
        private LinearLayout llType;
        private TextView meetingType, meetingTitle, meetingHostName, meetingStartTime;
        private ImageView meetingState;
        private Button btnStartMeeting;
        private ImageView ivCopy;
        private ImageView ivPortrait;
        //private ImageButton startPublcMeeting;
    }

    public void setTitleText(View mHeader, int firstVisiblePosition) {
        String title = "";
        if (fragHistory.getConferencePagerListView().getMyConfsCount() == 0) {
            title = titles[1];
        } else {
            if (firstVisiblePosition > fragHistory.getConferencePagerListView().getMyConfsCount()) {
                title = titles[1];
            } else {
                title = titles[0];
            }
        }

        TextView sectionHeader = (TextView) mHeader;
        sectionHeader.setText(title);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public int getTitleState(int position) {
        if (position <= 0 || getCount() == 0) {
            return 0;
        }

        if (ConferenceActivity.isLogin && fragHistory.getConferencePagerListView().getMyConfsCount() != 0
                && position == fragHistory.getConferencePagerListView().getMyConfsCount()) {
            return 2;
        }
        return 1;
    }

//	private void setClick(View view, int position){
    //view.setOnClickListener(this)
//	}

    /**
     * 进行中会议存在于所有会议信息
     *
     * @param viewHolder
     * @param position
     */
    private void contaionConfs(ViewHolder viewHolder, int position) {
//		if(position<progressList.size()){

//		}
        setListTitle(viewHolder, position);
//		setStartMeetingButton(viewHolder, position);
        setConfStatus(viewHolder, position);

    }

//	private void setStartMeetingButton(ViewHolder viewHolder, int position) {
//		viewHolder.startMeeting.setVisibility(View.GONE);
//		viewHolder.startPublcMeeting.setVisibility(View.GONE);
//		if(ConferenceActivity.isLogin){
//			if(confList.get(position).getHostName().equals(userName)){
//				if(position < fragHistory.getmConferencePagerListView().getMyConfsCount() && confList.get(position).getStatus().equals("0")){
//					viewHolder.startMeeting.setVisibility(View.VISIBLE);
//					confList.get(position).setNeedStartConf(1);
//				}else{
//					viewHolder.startPublcMeeting.setVisibility(View.VISIBLE);
//				}
//			}
//		}else{
//			if(position < fragHistory.getmConferencePagerListView().getMyConfsCount()){
//				viewHolder.startPublcMeeting.setVisibility(View.VISIBLE);
//			}
//		}
//	}

    private void setConfStatus(ViewHolder viewHolder, int position) {
        ConferenceBean conferenceBean = confList.get(position);
        int id = 0;
        //log.info("status = "+conferenceBean.getStatus()+" type = "+conferenceBean.getType());
        if (conferenceBean.getStatus() != null && (conferenceBean.getStatus().equals("3") || conferenceBean.getStatus().equals("5") || conferenceBean.getStatus().equals("6"))) {
            if (conferenceBean.getType().equals(Config.MEETING)) {
//				id = R.drawable.meetinglist_status_start;
                id = R.drawable.conf_on;
            } else {
//				id = R.drawable.meetinglistlive_status_start;
                id = R.drawable.conf_on;
            }

            viewHolder.btnStartMeeting.setBackgroundResource(R.drawable.btn_sc_join);
            viewHolder.btnStartMeeting.setTextColor(mContext.getResources().getColor(R.color.white));
            viewHolder.btnStartMeeting.setText(mContext.getResources().getString(R.string.playbuttonText2));

        } else {
            if (conferenceBean.getType().equals(Config.MEETING)) {
//				id = R.drawable.meetinglist_status_nostarted;
                id = R.drawable.conf_off;
            } else {
//				id = R.drawable.meetinglistlive_status_nostarted;
                id = R.drawable.conf_off;
            }

            viewHolder.btnStartMeeting.setTextColor(mContext.getResources().getColor(R.color.app_main_hue));
            viewHolder.btnStartMeeting.setText(mContext.getResources().getString(R.string.transcodebuttonText2));
        }

        viewHolder.meetingState.setImageResource(id);
    }

    private void setListTitle(ViewHolder viewHolder, int position) {

        viewHolder.llType.setVisibility(View.GONE);

//        if (ConferenceActivity.isLogin) {
//            if (position == fragHistory.getmConferencePagerListView().getMyConfsCount()) {
////				viewHolder.meetingType.setVisibility(View.VISIBLE);
//                viewHolder.llType.setVisibility(View.VISIBLE);
//                viewHolder.meetingType.setText(titles[1]);
//            } else if (position == 0) {
////				viewHolder.meetingType.setVisibility(View.VISIBLE);
//                viewHolder.llType.setVisibility(View.VISIBLE);
//                viewHolder.meetingType.setText(titles[0]);
//            } else {
////				viewHolder.meetingType.setVisibility(View.GONE);
//                viewHolder.llType.setVisibility(View.GONE);
//            }
//        } else {
//            if (position == 0) {
////				viewHolder.meetingType.setVisibility(View.VISIBLE);
//                viewHolder.llType.setVisibility(View.VISIBLE);
//                viewHolder.meetingType.setText(titles[1]);
//                //log.info("position:"+position);
//            } else {
////				viewHolder.meetingType.setVisibility(View.GONE);
//                viewHolder.llType.setVisibility(View.GONE);
//            }
//        }
    }

    private String dateToString(Date startDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        try {
            return format.format(startDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
