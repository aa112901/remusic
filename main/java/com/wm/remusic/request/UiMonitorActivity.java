package com.wm.remusic.request;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wm.remusic.R;


@SuppressWarnings("HardCodedStringLiteral")
public class UiMonitorActivity extends AppCompatActivity implements View.OnClickListener{

    private Button mBtnMonitor = null;
    private EditText mEtTime = null;
    private TextView mTvMonitor = null;

    public static void launch(Context context){
        Intent intent = new Intent(context, UiMonitorActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui_monitor);
        setTitle("UI卡顿检测");
        init();
    }

    private void init(){
        mBtnMonitor = (Button) findViewById(R.id.btn_monitor);
        mEtTime = (EditText) findViewById(R.id.et_slow_time);
        mBtnMonitor.setOnClickListener(this);
        mBtnMonitor.setText(UiMonitor.getInstance().getIsUiMonitoring()? "停止检测": "开始检测");
        mTvMonitor = (TextView) findViewById(R.id.tv_ui_monitor);
        String lastUiDelay = UiMonitor.getInstance().getLastUiDelayContent();
        mTvMonitor.setText("存储位置：" + Constant.SAVE_PATH + "ui.log" + "\n" + (TextUtils.isEmpty(lastUiDelay)? "": lastUiDelay));
    }

    @Override
    public void onClick(View v) {
        if(v == mBtnMonitor){
            if(UiMonitor.getInstance().getIsUiMonitoring()){
                UiMonitor.getInstance().stopUiMonitor();
                mBtnMonitor.setText("开始检测");
            }
            else{
                String time = mEtTime.getText().toString();
                if(TextUtils.isEmpty(time)){
                    Toast.makeText(UiMonitorActivity.this, "请输入检测时间", Toast.LENGTH_SHORT).show();
                    return;
                }
                try{
                    int delayTime = Integer.valueOf(time);
                    if(delayTime > UiMonitor.MAX_MONITOR_TEST_TIME){
                        Toast.makeText(UiMonitorActivity.this, "最大检测时间不超过1s", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mBtnMonitor.setText("停止检测");
                    UiMonitor.getInstance().startUiMonitor(delayTime);
                }
                catch (NumberFormatException e){
                    e.printStackTrace();
                    Toast.makeText(UiMonitorActivity.this, "请输入数字代表时间", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
