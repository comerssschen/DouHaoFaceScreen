package com.weipan.douhaofacescreen.common;

import android.app.Application;

import com.sunmi.payment.PaymentService;
import com.weipan.douhaofacescreen.bean.DaoMaster;
import com.weipan.douhaofacescreen.bean.DaoSession;

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
        PaymentService.getInstance().init(this);

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

}
