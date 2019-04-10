package com.weipan.douhaofacescreen.event;


import com.weipan.douhaofacescreen.bean.MenusBean;

import java.util.ArrayList;

/**
 * 作者：create by comersss on 2019/3/19 16:14
 * 邮箱：904359289@qq.com
 */
public class SucessEvent {
    private ArrayList<MenusBean> mMenues;

    public SucessEvent(ArrayList<MenusBean> menus) {
        mMenues = menus;
    }

    public ArrayList<MenusBean> getMsg() {
        return mMenues;
    }

}
