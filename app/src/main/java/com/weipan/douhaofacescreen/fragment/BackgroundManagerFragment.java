package com.weipan.douhaofacescreen.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.blankj.utilcode.util.ObjectUtils;
import com.weipan.douhaofacescreen.R;
import com.weipan.douhaofacescreen.bean.GoodsCode;
import com.weipan.douhaofacescreen.bean.GvBeans;
import com.weipan.douhaofacescreen.common.BaseFragment;
import com.weipan.douhaofacescreen.util.ImageUtil;
import com.weipan.douhaofacescreen.view.CustomCarGoodsCounterView;
import com.weipan.douhaofacescreen.view.TakePictureManager;

public class BackgroundManagerFragment extends BaseFragment implements View.OnClickListener {
    private ImageView ivIcon;
    private EditText etName;
    private EditText etPrice;
    private EditText etUnit;
    private CustomCarGoodsCounterView etNum;
    private Button btnAdd;
    private Button btnDelete;
    String photoPath;
    GvBeans gvBeans;
    private EditText etCode;
    private TakePictureManager takePictureManager;

    @Override
    protected int setView() {
        return R.layout.fragment_background_setting;
    }

    @Override
    protected void init(View view) {
        ivIcon = view.findViewById(R.id.iv_icon);
        etName = view.findViewById(R.id.et_name);
        etPrice = view.findViewById(R.id.et_price);
        etUnit = view.findViewById(R.id.et_unit);
        etNum = view.findViewById(R.id.et_num);
        etCode = view.findViewById(R.id.et_code);
        btnAdd = view.findViewById(R.id.btn_add);
        btnDelete = view.findViewById(R.id.btn_delete);
        etName.setSaveEnabled(false);
        etPrice.setSaveEnabled(false);
        etUnit.setSaveEnabled(false);
        etNum.setSaveEnabled(false);
        etCode.setSaveEnabled(false);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        gvBeans = null;
        etPrice.setText("");
        etName.setText("");
        etUnit.setText("");
        etNum.setGoodsNumber(1);
        btnDelete.setVisibility(View.GONE);
        //设置输入框允许输入的类型（正则）
        etPrice.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        //设置输入字符
        etPrice.setFilters(new InputFilter[]{inputFilter});
        ivIcon.setOnClickListener(this);
        btnAdd.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
    }

    private InputFilter inputFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            // 删除等特殊字符，直接返回
            if (TextUtils.isEmpty(source)) {
                return null;
            }
            if (dend > 8) {
                return "";
            }
            String dValue = dest.toString();
            String[] splitArray = dValue.split("\\.");
            if (splitArray.length > 1) {
                String dotValue = splitArray[1];
                int dotIndex = dValue.indexOf(".");
                if (dend <= dotIndex) {
                    return null;
                } else {
                    // 2 表示输入框的小数位数
                    int diff = dotValue.length() + 1 - 2;
                    if (diff > 0) {
                        return source.subSequence(start, end - diff);
                    }
                }
            }
            return null;
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_delete:
                GoodsCode.getInstance().deleteGoods(gvBeans.getCode());
                break;
            case R.id.iv_icon:
                takePictureManager = new TakePictureManager(BackgroundManagerFragment.this, new TakePictureManager.takePictureCallBackListener() {
                    @Override
                    public void successful(Bitmap bitmap, String path, Uri uri) {
                        photoPath = path;
                        ivIcon.setImageBitmap(ImageUtil.getDiskBitmap(path));//显示在iv上
                    }
                });
                takePictureManager.takePic();
                break;
            case R.id.btn_add:
                add();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        takePictureManager.attachToActivityForResult(requestCode, resultCode, data);
    }

    private void add() {
        String url = photoPath;
        String name = etName.getText().toString().replace(" ", "").replace("\n", "");
        String price = etPrice.getText().toString().replace(" ", "").replace("\n", "");
        String unit = etUnit.getText().toString().replace(" ", "").replace("\n", "");
        String code = etCode.getText().toString().replace(" ", "").replace("\n", "");
        code = ObjectUtils.isEmpty(code) ? System.currentTimeMillis() + "" : code;

        if (GoodsCode.getInstance().getGood().containsKey(code)) {
            Toast.makeText(getContext(), "商品编号重复，请重新输入", Toast.LENGTH_LONG).show();
            return;
        }
        int num = etNum.getGoodsNumber();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(price)) {
            Toast.makeText(getContext(), "请填入必填", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(url)) {
            GoodsCode.getInstance().add(code, R.drawable.product_default, name, Float.parseFloat(price), num, unit, GoodsCode.MODE_5);
        } else {
            GoodsCode.getInstance().add(code, url, name, Float.parseFloat(price), num, unit, GoodsCode.MODE_5);
        }
        Toast.makeText(getContext(), "新增成功", Toast.LENGTH_LONG).show();
        etCode.setText("");
        etName.setText("");
        etPrice.setText("");
        etUnit.setText("");
        photoPath = "";
        ivIcon.setImageResource(R.drawable.add_image);
    }

}

