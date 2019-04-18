package com.weipan.douhaofacescreen.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.blankj.utilcode.util.Utils;
import com.weipan.douhaofacescreen.R;


/**
 * 作者：create by comersss on 2018/12/10 16:51
 * 邮箱：904359289@qq.com
 */
public class PayPopupWindow extends PopupWindow {
    private View mView; // PopupWindow 菜单布局
    private Context mContext; // 上下文参数
    private PopLitener mLlistener;
    private String mParm1, mParm2;
    private Ringtone ringtone;

    public interface PopLitener {
        void onClosed();

        void onPart1();

        void onPart2();

        void onPart3();
    }

    public PayPopupWindow(Activity context, String parm1, String parm2) {
        super(context);
        this.mContext = context;
        this.mParm1 = parm1;
        this.mParm2 = parm2;
        Init();
    }

    /**
     * 设置布局以及点击事件
     */
    private void Init() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        ringtone = RingtoneManager.getRingtone(Utils.getApp(), notification);
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        mView = inflater.inflate(R.layout.pop_pay_dialog, null);
        ImageView tvFacePay = mView.findViewById(R.id.tv_face_pay);
        ImageView tvBt2 = mView.findViewById(R.id.tv_bt2);
        TextView tvParm1 = mView.findViewById(R.id.tv_parm1);
        TextView tvParm2 = mView.findViewById(R.id.tv_parm2);
        tvParm1.setText(mParm1);
        tvParm2.setText(mParm2);
        ImageView ivClose = mView.findViewById(R.id.iv_close);

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ringtone.play();
                if (mLlistener != null) {
                    mLlistener.onClosed();
                }

            }
        });
        tvFacePay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ringtone.play();
                if (mLlistener != null) {
                    mLlistener.onPart1();
                }
            }
        });
        tvBt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ringtone.play();
                if (mLlistener != null) {
                    mLlistener.onPart2();
                }
            }
        });

        // 导入布局
        this.setContentView(mView);
        // 设置动画效果
        this.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        this.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        // 设置可触
        this.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x0000000);
        this.setBackgroundDrawable(dw);
        // 单击弹出窗以外处 关闭弹出窗
        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int height = mView.findViewById(R.id.ll_pop).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });
    }

    public void setPopListener(PopLitener listener) {
        this.mLlistener = listener;
    }

}
