package com.weipan.douhaofacescreen.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ObjectUtils;
import com.weipan.douhaofacescreen.R;

/**
 * 作者：create by comersss on 2019/4/2 14:54
 * 邮箱：904359289@qq.com
 */
public class KeyBoardDialogView extends Dialog implements View.OnClickListener {

    private final Context context;
    private EditText etPhoneNum;

    private onConfirmLitener onConfirmLitener;
    private Button btConfirm;

    public interface onConfirmLitener {
        void confirm(String msg);
    }

    public void setOnConfirmLitener(KeyBoardDialogView.onConfirmLitener onConfirmLitener) {
        this.onConfirmLitener = onConfirmLitener;

    }

    public KeyBoardDialogView(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.keyboard_dialog);

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        lp.width = (int) (display.getWidth() * 0.9); // 宽度
        dialogWindow.setAttributes(lp);

        etPhoneNum = findViewById(R.id.et_phone_num);
        etPhoneNum.setInputType(InputType.TYPE_NULL);
        LinearLayout llDialog = findViewById(R.id.ll_dialog);
        setItemClickListener(llDialog);

        btConfirm = findViewById(R.id.bt_confirm);

        etPhoneNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (ObjectUtils.isEmpty(s)) {
                    btConfirm.setEnabled(false);
                } else {
                    btConfirm.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    /**
     * 给每一个自定义数字键盘上的View 设置点击事件
     *
     * @param view
     */
    private void setItemClickListener(View view) {
        if (view.getId() == R.id.tv_title) {
            return;
        } else if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                //不断的给里面所有的View设置setOnClickListener
                View childView = ((ViewGroup) view).getChildAt(i);
                setItemClickListener(childView);
            }
        } else {
            view.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.iv_dialog_close) {
            dismiss();
        } else if (view.getId() == R.id.tv_clear) {
            etPhoneNum.setText("");
        } else if (view.getId() == R.id.tv_delete) {
            String password = etPhoneNum.getText().toString().trim();
            if (TextUtils.isEmpty(password)) {
                return;
            }
            password = password.substring(0, password.length() - 1);
            etPhoneNum.setText(password);
        } else if (view.getId() == R.id.bt_confirm) {
            if (onConfirmLitener != null) {
                onConfirmLitener.confirm(etPhoneNum.getText().toString());
            }
        } else if (view instanceof TextView) {
            String number = ((TextView) view).getText().toString().trim();
            addPassword(number);
        }
    }

    public void addPassword(String number) {
        if (TextUtils.isEmpty(number)) {
            return;
        }
        String password = etPhoneNum.getText().toString().trim();
        if (password.length() < 12) {
            //密码叠加
            password += number;
            etPhoneNum.setText(password);
        }
    }
}
