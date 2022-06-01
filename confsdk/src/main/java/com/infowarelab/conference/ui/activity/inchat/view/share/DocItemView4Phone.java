package com.infowarelab.conference.ui.activity.inchat.view.share;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.infowarelab.conference.ui.activity.inchat.view.ConfDsView;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.DocCommonImpl;
import com.infowarelabsdk.conference.common.impl.UserCommonImpl;
import com.infowarelabsdk.conference.confctrl.UserCommon;
import com.infowarelabsdk.conference.domain.DocBean;

public class DocItemView4Phone implements OnLongClickListener, OnClickListener{
	private ImageView file;
	private TextView fileName;
	private ImageView delete;
	private View view;
	private LinearLayout lin;
	private FrameLayout frame;
	
	private ConfDsView pView;
	private DocBean doc;
	private DocCommonImpl docCommon;
	private UserCommonImpl userCommon;

	private Context activity;

	public DocItemView4Phone(Context activity, ConfDsView pView, DocBean doc) {
		this.activity = activity;
		this.pView = pView;
		this.doc = doc;
	}

	public View getNewView() {
		initView();
		return view;
	}
	
	private void initView(){
		view = LayoutInflater.from(activity).inflate(R.layout.conference_share_file, null);
		file = (ImageView) view.findViewById(R.id.share_watchfile);
		fileName = (TextView) view.findViewById(R.id.share_filename);	
		delete = (ImageView) view.findViewById(R.id.share_deletefile);
		file.setImageResource(R.drawable.icon_file_normal);
		docCommon = (DocCommonImpl) CommonFactory.getInstance().getDocCommon();
		userCommon = (UserCommonImpl) CommonFactory.getInstance().getUserCommon();
		
		String name = doc.getTitle().toString();
		name = cutName(name, 8);
		fileName.setText(name);
		file.setTag(doc.getDocID());
		file.setOnClickListener(this);
		file.setOnLongClickListener(this);
		delete.setOnClickListener(this);
	}
	
	public void setClicked(){
		file.setImageResource(R.drawable.icon_file_on);
	}

	@Override
	public boolean onLongClick(View v) {
		if(userCommon.getSelf().getRole() == UserCommon.ROLE_HOST || doc.isLocal()){			
			delete.setVisibility(View.VISIBLE);
		}else{
			Toast.makeText(activity, activity.getString(R.string.noCloseDoc),
					Toast.LENGTH_SHORT).show();
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.share_watchfile) {
			lin = (LinearLayout) file.getParent().getParent().getParent().getParent();
			if (lin.getTag() != null) {
//				activity.removePointing();
				((ImageView) lin.getTag()).setImageResource(R.drawable.icon_file_normal);

				if (!((ImageView) lin.getTag()).getTag().toString().equals
						(String.valueOf(doc.getDocID()))) {
					frame = (FrameLayout) ((ImageView) lin.getTag()).getParent();
					frame.getChildAt(1).setVisibility(View.GONE);
				}
				docCommon.switchDoc(doc.getDocID());

			}
			lin.setTag(file);
			//点击两次同一个view时，确保其状态不变
			file.setImageResource(R.drawable.icon_file_on);
		} else if (id == R.id.share_deletefile) {
			docCommon.closeDoc(doc.getDocID());
			docCommon.onCloseDoc(doc.getDocID());

			//delete.setVisibility(View.GONE);
		}
		
	}

	public ImageView getFile() {
		return file;
	}

	public void setFile(ImageView file) {
		this.file = file;
	}
	
	private String cutName(String s,int l){
		try {
			s = idgui(s, l);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if(s.length() > 6){
				s = s.substring(0, 6)+"...";
			}
		}
		return s;
	}
	
	private String idgui(String s,int num)throws Exception{
        int changdu = s.getBytes("GBK").length;
        if(changdu > num){
            s = s.substring(0, s.length() - 1);
            s = idgui2(s,num)+"…";
        }
        return s;
    }
	private String idgui2(String s,int num)throws Exception{
		int changdu = s.getBytes("GBK").length;
		if(changdu > num){
			s = s.substring(0, s.length() - 1);
			s = idgui2(s,num);
		}
		return s;
	}

}
