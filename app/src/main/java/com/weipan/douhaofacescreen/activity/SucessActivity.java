package com.weipan.douhaofacescreen.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.weipan.douhaofacescreen.R;
import com.weipan.douhaofacescreen.adapter.SusceeAdapter;
import com.weipan.douhaofacescreen.bean.MenusBean;
import com.weipan.douhaofacescreen.common.BaseActivity;
import com.weipan.douhaofacescreen.fragment.PayModeSettingFragment;
import com.weipan.douhaofacescreen.util.CountDownHelper;
import com.weipan.douhaofacescreen.util.SharePreferenceUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：create by comersss on 2019/3/18 15:48
 * 邮箱：904359289@qq.com
 */
public class SucessActivity extends BaseActivity {
    @BindView(R.id.tv_total_count)
    TextView tvTotalCount;
    @BindView(R.id.tv_total_money)
    TextView tvTotalMoney;
    @BindView(R.id.tv_bt_parm1)
    TextView tvBtParm1;
    @BindView(R.id.tv_bt_parm2)
    TextView tvBtParm2;
    @BindView(R.id.tv_time_tiger)
    TextView tvTimeTiger;
    @BindView(R.id.lv_menus)
    ListView lvMenus;
    private SusceeAdapter menusAdapter;
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private String PayMoney;
    private int totalCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sucess);
        ButterKnife.bind(this);
        totalCount = getIntent().getIntExtra("count", 0);
        ArrayList<MenusBean> menus = (ArrayList<MenusBean>) getIntent().getSerializableExtra("menus");
        Log.i("test", "menus = " + menus.toString());
        menusAdapter = new SusceeAdapter(this, menus);
        lvMenus.setAdapter(menusAdapter);
        float price = 0.00f;
        for (MenusBean bean1 : menus) {
            price = price + Float.parseFloat(bean1.getMoney().substring(1));
        }
        tvTotalMoney.setText("合计 ：￥" + decimalFormat.format(price));
        menusAdapter.update(menus);
        tvTotalCount.setText("共" + totalCount + "件商品");
        boolean isRealDeal = (boolean) SharePreferenceUtil.getParam(SucessActivity.this, PayModeSettingFragment.IS_REAL_DEAL, PayModeSettingFragment.default_isRealDeal);
        if (isRealDeal) {
            PayMoney = price + "";
        } else {
            PayMoney = "0.01";
        }
        tvBtParm1.setText("实付 ￥" + PayMoney);
//        EventBus.getDefault().post(new SucessEvent(menus));
        CountDownHelper helper = new CountDownHelper(tvTimeTiger, 10, 1);
        helper.setOnFinishListener(new CountDownHelper.OnFinishListener() {
            @Override
            public void fin() {
                finish();
            }
        });
        helper.start();
    }


    @OnClick({R.id.tv_time_tiger})
    public void onViewClicked(View view) {
        ringtone.play();
        switch (view.getId()) {
            case R.id.tv_time_tiger:
                finish();
                break;
        }
    }

}
