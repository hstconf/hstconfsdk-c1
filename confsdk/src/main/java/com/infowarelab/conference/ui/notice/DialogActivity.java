package com.infowarelab.conference.ui.notice;
////import org.apache.log4j.Logger;

import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.infowarelab.conference.BaseActivity;
import com.infowarelab.hongshantongphone.R;
import com.infowarelab.conference.ui.action.JoinConfByUrlAction;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;

/**
 * @author Jack.Yan@infowarelab.com
 * @description Show dialog activity
 * @date 2012-11-9
 */
public class DialogActivity extends BaseActivity implements OnClickListener {

    //private final Logger log = Logger.getLogger(getClass());

    private Button enter, delay, cancel;
    private TextView dialog_message;

    private NoticeManager noticeManager;
    private int requestId;

    private ReceiverMessage receiverMessage;
    private JoinConfByUrlAction joinConfByUrlAction;

    private Vibrator vibrator;
    private long milliseconds = 2 * 1000;
    private long delayTime = 5 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice_dialog);
        //log.info("onCreate...");
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        enter.setOnClickListener(this);
        delay.setOnClickListener(this);
        cancel.setOnClickListener(this);
        noticeManager = NoticeManager.getInstance(this);
        try {
            requestId = Integer.parseInt(receiverMessage.getConfKey());
        } catch (Exception e) {
            requestId = 0;
        }
        //log.info("play vibrator");
        if (vibrator == null) {
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        }
        vibrator.vibrate(milliseconds);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //log.info("close vibrator");
        vibrator.cancel();
    }

    private void init() {
        enter = (Button) findViewById(R.id.dialogEnter);
        delay = (Button) findViewById(R.id.dialogDelay);
        cancel = (Button) findViewById(R.id.dialogCancel);
        dialog_message = (TextView) findViewById(R.id.dialogMessage);
        joinConfByUrlAction = new JoinConfByUrlAction(this);
        receiverMessage = (ReceiverMessage) getIntent().getSerializableExtra(Constants.TAG_NOTIFICATION);
        try {
            delayTime = Long.parseLong(getString(R.string.delayTime));
        } catch (Exception e) {
            //log.error("delayTime parseLong error, use '300000' as default value");
            delayTime = 5 * 60 * 1000;
        }
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        dialog_message.setText(receiverMessage.getConfMessage());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.dialogEnter) {
            //log.info("joining the conference...");
            noticeManager.cancelPush(noticeManager.getPendingIntent(this.getIntent(), requestId));
            joinConf();
        } else if (id == R.id.dialogDelay) {
            //log.info("dialog will re-open after [" + delayTime + "] milliseconds");
            noticeManager.pushMessage(noticeManager.getPendingIntent(this.getIntent(), requestId), System.currentTimeMillis() + delayTime);
            Toast.makeText(this, getString(R.string.confirm_dialog_delay), Toast.LENGTH_LONG).show();
        } else if (id == R.id.dialogCancel) {
            //log.info("dialog has been canceled");
            noticeManager.cancelPush(noticeManager.getPendingIntent(this.getIntent(), requestId));
            Toast.makeText(this, getString(R.string.confirm_dialog_cancel), Toast.LENGTH_LONG).show();
        } else {
            return;
        }
        finish();
    }

    private void joinConf() {
        String userName = FileUtil.readSharedPreferences(DialogActivity.this, Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME);
        if (userName == null || "".equals(userName)) {
            userName = "guest";
        }
        //log.info("Confkey:"+receiverMessage.getConfKey()+" UserName:"+userName+" Password:"+receiverMessage.getConfPassword());
        joinConfByUrlAction.setLoginBean(receiverMessage.getConfKey(),
                userName,
                receiverMessage.getConfPassword());
    }
}
