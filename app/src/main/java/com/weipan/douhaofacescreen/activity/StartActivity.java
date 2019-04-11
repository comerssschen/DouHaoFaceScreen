package com.weipan.douhaofacescreen.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.tencent.wxpayface.IWxPayfaceCallback;
import com.tencent.wxpayface.WxPayFace;
import com.weipan.douhaofacescreen.R;
import com.weipan.douhaofacescreen.bean.ArgGetAuthInfo;
import com.weipan.douhaofacescreen.common.BaseActivity;
import com.weipan.douhaofacescreen.common.Constant;
import com.weipan.douhaofacescreen.view.KeyBoardDialogView;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：create by comersss on 2019/4/3 09:01
 * 邮箱：904359289@qq.com
 */
public class StartActivity extends BaseActivity {
    @BindView(R.id.iv_setting)
    ImageView ivSetting;
    @BindView(R.id.tv_no_member)
    ImageView tvNoMember;
    @BindView(R.id.tv_member)
    ImageView tvMember;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);
        WxPayFace.getInstance().initWxpayface(this, new IWxPayfaceCallback() {
            public void response(Map paramMap) throws RemoteException {
                getAuthInfo();
            }
        });
    }

    public void getAuthInfo() {
        WxPayFace.getInstance().getWxpayfaceRawdata(new IWxPayfaceCallback() {
            public void response(Map paramMap) throws RemoteException {
                Log.i("test", "paramMap = " + paramMap);
                Map<String, Object> map = new HashMap<>();
                map.put("store_id", Constant.store_id);
                map.put("store_name", Constant.store_name);
                map.put("device_id", Constant.device_id);
                map.put("appid", Constant.appid);
                map.put("mch_id", Constant.mch_id);
                map.put("sub_appid", Constant.sub_appid);
                map.put("sub_mch_id", Constant.sub_mch_id);
                map.put("rawdata", paramMap.get("rawdata").toString());
                Log.i("test", "map = " + map);
                OkGo.<String>post(Constant.localhostUrl + "/api/Pay/FaceAuth")
                        .upJson(new Gson().toJson(map))
                        .execute(new StringCallback() {
                            @Override
                            public void onSuccess(Response<String> response) {
                                try {
                                    String body = response.body();
                                    Log.i("test", "getAuthInfo :" + body);
                                    ArgGetAuthInfo result = new Gson().fromJson(body, ArgGetAuthInfo.class);
                                    if (ObjectUtils.equals("200", result.getCode())) {
                                        Constant.authInfo = result.getData().getAuthinfo();
                                        //开启一个子线程，定时刷新
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Looper.prepare();
                                                try {
//                                                    int millis = Integer.parseInt(result.getData().getExpires_in());
//                                                    Log.i("test", millis + "");
                                                    Thread.sleep(24 * 60 * 60 * 1000);
                                                    getAuthInfo();
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                                Looper.loop();
                                            }
                                        }).start();
                                    } else {
                                        Toast.makeText(getApplicationContext(), " Msg = " + result.getMsg(), Toast.LENGTH_LONG).show();
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    ToastUtils.showShort("初始化失败,请退出重进");

                                }
                            }

                            @Override
                            public void onError(Response<String> response) {
                                super.onError(response);
                                ToastUtils.showShort("初始化失败,请退出重进");
                            }
                        });
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WxPayFace.getInstance().releaseWxpayface(StartActivity.this);
    }

    @OnClick({R.id.iv_setting, R.id.tv_no_member, R.id.tv_member})
    public void onViewClicked(View view) {
        ringtone.play();
        switch (view.getId()) {
            case R.id.iv_setting:
                Intent intent = new Intent(StartActivity.this, MoreActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_no_member:
                startActivity("");
                break;
            case R.id.tv_member:
                KeyBoardDialogView keyBoardDialogView = new KeyBoardDialogView(StartActivity.this);
                keyBoardDialogView.show();
                keyBoardDialogView.setOnConfirmLitener(new KeyBoardDialogView.onConfirmLitener() {
                    @Override
                    public void confirm(String msg) {
                        ToastUtils.showShort("会员登录成功！");
                        startActivity(msg);
                        keyBoardDialogView.dismiss();
                    }
                });
                break;
            default:
                break;
        }
    }

    private void startActivity(String memberNum) {
        Intent intent = new Intent(StartActivity.this, MainActivity.class);
        intent.putExtra("memberNum", memberNum);
        startActivity(intent);

    }
}
