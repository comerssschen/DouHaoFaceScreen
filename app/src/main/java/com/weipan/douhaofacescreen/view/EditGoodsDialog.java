package com.weipan.douhaofacescreen.view;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.weipan.douhaofacescreen.R;

/**
 * 作者：create by comersss on 2019/4/9 09:58
 * 邮箱：904359289@qq.com
 */
public class EditGoodsDialog extends Dialog {

    private final Activity context;

    private UpdateGoodsNumberListener numberListener;
    private ImageView ivClose;
    private TextView goodsName;
    private String content;
    private CustomCarGoodsCounterView goodCountView;

    private int mNum;

    /**
     * 更新商品数量监听器
     */
    public interface UpdateGoodsNumberListener {
        void updateGoodsNumber(int number);
    }

    public void setUpdateGoodsNumberListener(UpdateGoodsNumberListener litener) {
        this.numberListener = litener;
    }

    public EditGoodsDialog(Activity context) {
        super(context);
        this.context = context;
    }

    public void setcontent(String name) {
        this.content = name;
    }

    public void setNum(int num) {
        mNum = num;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.edit_goods_dialog);

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);
        Display display = context.getWindowManager().getDefaultDisplay();
        lp.width = (int) (display.getWidth() * 0.9); // 宽度
        dialogWindow.setAttributes(lp);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        ivClose = findViewById(R.id.iv_close);
        ivClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        goodsName = findViewById(R.id.tv_goodsname);
        goodsName.setText(content);

        goodCountView = findViewById(R.id.good_countview);
        goodCountView.setGoodsNumber(mNum);
        goodCountView.setedit(false);
        goodCountView.setUpdateGoodsNumberListener(new CustomCarGoodsCounterView.UpdateGoodsNumberListener() {
            @Override
            public void updateGoodsNumber(int number) {
                numberListener.updateGoodsNumber(number);
            }
        });
    }


}
