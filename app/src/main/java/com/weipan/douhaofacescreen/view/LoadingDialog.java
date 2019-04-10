package com.weipan.douhaofacescreen.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.weipan.douhaofacescreen.R;


/**
 * 加载中对款框
 * Created by Administrator on 2016/11/14.
 */
public class LoadingDialog extends Dialog {

    public Context mContext;
    private LayoutInflater inflater;
    private WindowManager.LayoutParams lp;
    private TextView loadtext;
    public boolean isClose;

    public LoadingDialog(final Context context, String content) {
        super(context, R.style.MyLoadDialog);

        getWindow().setBackgroundDrawableResource(R.color.transparent);

        this.mContext = context;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.widget_dialog, null);
        loadtext = layout.findViewById(R.id.loading_text);
        loadtext.setText(content);
        setContentView(layout);
        // 设置window属性
        lp = getWindow().getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.dimAmount = 0; // 去背景遮盖
        // lp.alpha = 1.0f;//透明效果
        getWindow().setAttributes(lp);
    }

//    /**
//     * 设置显示文字
//     * 默认不显示文字
//     * @param content 文字内容
//     */
//    public void setLoadText(String content) {
////        loadtext.setVisibility(View.VISIBLE);
//        loadtext.setText(content);
//    }

    /**
     * 限制反回键盘关闭进度条
     *
     * @param close
     */
    public void setColose(boolean close) {
        this.isClose = close;
        if (!isClose) {
            setCancelable(isClose);
            setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog,
                                     int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_SEARCH) {

                        dismiss();
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }
    }

    @Override
    public void dismiss() {
        try {
            super.dismiss();
        } catch (Exception e) {

        }

    }
}
