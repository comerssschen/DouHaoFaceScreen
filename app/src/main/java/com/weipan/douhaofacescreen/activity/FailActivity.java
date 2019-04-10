package com.weipan.douhaofacescreen.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.weipan.douhaofacescreen.R;
import com.weipan.douhaofacescreen.common.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：create by comersss on 2019/4/8 16:01
 * 邮箱：904359289@qq.com
 */
public class FailActivity extends BaseActivity {
    @BindView(R.id.tv_msg)
    TextView tvMsg;
    @BindView(R.id.bt_try_again)
    Button btTryAgain;
    @BindView(R.id.bt_cancel)
    Button btCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fail);
        ButterKnife.bind(this);
        String msg = getIntent().getStringExtra("msg");
        tvMsg.setText(msg);
    }

    @OnClick({R.id.bt_try_again, R.id.bt_cancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bt_try_again:
                finish();
                break;
            case R.id.bt_cancel:
                Intent intent = new Intent();
                setResult(30, intent);
                finish();
                break;
        }
    }
}
