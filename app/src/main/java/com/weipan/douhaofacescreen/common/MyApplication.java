package com.weipan.douhaofacescreen.common;

import android.app.Application;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.tencent.wxpayface.IWxPayfaceCallback;
import com.tencent.wxpayface.WxPayFace;
import com.weipan.douhaofacescreen.bean.ArgGetAuthInfo;
import com.weipan.douhaofacescreen.bean.DaoMaster;
import com.weipan.douhaofacescreen.bean.DaoSession;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by highsixty on 2017/11/20.
 * mail  gaolulin@sunmi.com
 */

public class MyApplication extends Application {

    public static MyApplication app = null;
    private DaoSession mDaoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        this.initDB();
        WxPayFace.getInstance().initWxpayface(this, new IWxPayfaceCallback() {
            public void response(Map paramMap) throws RemoteException {
                getAuthInfo();
            }
        });
    }

    public static MyApplication getInstance() {
        return app;
    }


    private void initDB() {
        this.mDaoSession = (new DaoMaster((new DaoMaster.DevOpenHelper(this, "aserbao.db")).getWritableDb())).newSession();
    }

    public DaoSession getDaoSession() {
        return this.mDaoSession;
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
                OkGo.<String>post(Constant.localhostUrl + "/api/Pay/FaceAuth")
                        .upJson(new Gson().toJson(map))
                        .execute(new StringCallback() {
                            @Override
                            public void onSuccess(Response<String> response) {
                                try {
                                    String body = response.body();
                                    ArgGetAuthInfo result = new Gson().fromJson(body, ArgGetAuthInfo.class);
                                    if (ObjectUtils.equals("200", result.getCode())) {
                                        Constant.authInfo = result.getData().getAuthinfo();
//                                        //开启一个子线程，定时刷新
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

}
