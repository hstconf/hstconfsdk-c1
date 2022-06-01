package com.infowarelab.conference.ui.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.infowarelab.hongshantongphone.R;
import com.infowarelab.conference.ui.tools.CutString;
import com.infowarelabsdk.conference.domain.MessageBean;

public class ChatContentAdapter extends BaseAdapter {

    private static int IMVT_COM_MSG = 0;
    private static int IMVT_TO_MSG = 1;
    private List<MessageBean> coll;
    private LayoutInflater mInflater;
    private int preIndex = 0;

    public ChatContentAdapter(Context context, List<MessageBean> coll) {
        this.coll = coll;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return coll.size();
    }

    @Override
    public MessageBean getItem(int position) {
        return coll.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getItemViewType(int position) {
        // TODO Auto-generated method stub
        MessageBean entity = getItem(position);

        if (entity.isComeMeg()) {
            return IMVT_COM_MSG;
        } else {
            return IMVT_TO_MSG;
        }

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //System.out.println("getView "+position);
        MessageBean entity = getItem(position);
        boolean isComMsg = entity.isComeMeg();

        ViewHolder viewHolder = null;
        if (convertView == null || ((ViewHolder) convertView.getTag()).isComMsg != isComMsg) {
            if (isComMsg) {
                convertView = mInflater.inflate(R.layout.conference_attenders_chat_comeitem, null);
            } else {
                convertView = mInflater.inflate(R.layout.conference_attenders_chat_senditem, null);
            }

            viewHolder = new ViewHolder();
            viewHolder.time = (TextView) convertView.findViewById(R.id.chat_time);
            viewHolder.username = (TextView) convertView.findViewById(R.id.chat_name);
            viewHolder.content = (TextView) convertView.findViewById(R.id.chat_content);
            viewHolder.linear = (LinearLayout) convertView.findViewById(R.id.chat_layout);
            viewHolder.isComMsg = isComMsg;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (entity.isShowTime()) {
            viewHolder.time.setVisibility(View.VISIBLE);
        } else {
            viewHolder.time.setVisibility(View.GONE);
        }
	    	    
	    /*if(position > 0){
	    	MessageBean preEntity = coll.get(preIndex);	    	
	    	long pre = StringUtil.strToDate(preEntity.getDate(), "HH:mm").getTime();
	    	long current = StringUtil.strToDate(entity.getDate(), "HH:mm").getTime();
	    	System.out.println(preIndex+"/"+pre+"/"+current+"/"+(current - pre)/60/1000);
	    	//判断与前一条聊天内容相比，时间是否间隔在3分钟以上（毫秒级）
	    	if(Math.abs((current - pre)/60/1000) <= 3){
	    		viewHolder.time.setVisibility(View.GONE);
	    	}else{
	    		preIndex = position;
	    	}
	    	
	    }*/

        //从小时开始截取
        viewHolder.time.setText(entity.getDate().substring(0, 5));
        String name = entity.getUsername();
        name = CutString.cutString(name, 8);
        viewHolder.username.setText(name);
        viewHolder.content.setText(entity.getMessage());

        return convertView;
    }


    static class ViewHolder {
        public TextView time;
        public TextView username;
        public TextView content;
        public LinearLayout linear;
        public boolean isComMsg = true;
    }


}
