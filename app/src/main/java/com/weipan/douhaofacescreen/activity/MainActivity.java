package com.weipan.douhaofacescreen.activity;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.felhr.usbserial.UsbSerialInterface;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.superscan.lib.device.usb.AsyncUsbScanner;
import com.superscan.lib.device.usb.UsbToolKit;
import com.tencent.wxpayface.IWxPayfaceCallback;
import com.tencent.wxpayface.WxPayFace;
import com.tx.printlib.Const;
import com.tx.printlib.UsbPrinter;
import com.weipan.douhaofacescreen.R;
import com.weipan.douhaofacescreen.adapter.CarAdapter;
import com.weipan.douhaofacescreen.bean.ArgScanQRCode;
import com.weipan.douhaofacescreen.bean.GoodsCode;
import com.weipan.douhaofacescreen.bean.GvBeans;
import com.weipan.douhaofacescreen.bean.MenusBean;
import com.weipan.douhaofacescreen.bean.ResultFacePay;
import com.weipan.douhaofacescreen.bean.ResultScanQRCode;
import com.weipan.douhaofacescreen.common.BaseActivity;
import com.weipan.douhaofacescreen.common.Constant;
import com.weipan.douhaofacescreen.fragment.PayModeSettingFragment;
import com.weipan.douhaofacescreen.listener.OnResponseListener;
import com.weipan.douhaofacescreen.util.CountDownHelper;
import com.weipan.douhaofacescreen.util.OkGoUtils;
import com.weipan.douhaofacescreen.util.ResourcesUtils;
import com.weipan.douhaofacescreen.util.SharePreferenceUtil;
import com.weipan.douhaofacescreen.view.CloseConfirmDialog;
import com.weipan.douhaofacescreen.view.EditGoodsDialog;
import com.weipan.douhaofacescreen.view.LoadingDialog;
import com.weipan.douhaofacescreen.view.PayPopupWindow;
import com.weipan.douhaofacescreen.view.ScanQrCodeDialog;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends BaseActivity {

    @BindView(R.id.goods_recyclerview)
    RecyclerView goodsRecyclerview;
    @BindView(R.id.tv_cancle)
    TextView tvCancle;
    @BindView(R.id.tv_member_id)
    TextView tvMemberId;
    @BindView(R.id.tv_total_money)
    TextView tvTotalMoney;
    @BindView(R.id.tv_total_count)
    TextView tvTotalCount;
    @BindView(R.id.bt_go_pay)
    Button btGoPay;
    private ArrayList<MenusBean> menus = new ArrayList<>();
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private CarAdapter mAdapter;
    private boolean isRealDeal;
    private int totalCount = 0;
    private String totalMoney;
    private String realPayMoney = "0.01";
    private PayPopupWindow mPhotoPopupWindow;
    private ScanQrCodeDialog scanQrCodeDialog;
    private Gson gson = new Gson();
    private CloseConfirmDialog closeConfirmDialog;
    private CountDownHelper helper;
    private LoadingDialog loadingDialog;

    private AsyncUsbScanner asyncUsbScanner;
    private UsbPrinter mUsbPrinter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
        initScan();
    }

    private void initScan() {
        UsbToolKit usbToolKit = new UsbToolKit(MainActivity.this);
        List<UsbDevice> usbDevices = usbToolKit.listDevices();
        assert usbDevices.size() > 0;
        asyncUsbScanner = new AsyncUsbScanner(usbDevices.get(0), usbToolKit.getUsbManager(), new UsbSerialInterface.UsbReadCallback() {
            @Override
            public void onReceivedData(byte[] bytes) {
                String msg = new String(bytes).trim();
                Log.i("test", msg);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!ObjectUtils.isEmpty(scanQrCodeDialog) && scanQrCodeDialog.isShowing()) {
                            scanQRCode(msg);
                        } else {
                            scanResult(msg);
                        }
                    }
                });
            }
        });
        asyncUsbScanner.open();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 30 && requestCode == 20) {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        helper = new CountDownHelper(tvCancle, 90, 1);
        helper.setOnFinishListener(new CountDownHelper.OnFinishListener() {
            @Override
            public void fin() {
                if (!(ActivityUtils.getTopActivity() instanceof MainActivity)) {
                    return;
                }
                if (ObjectUtils.isEmpty(closeConfirmDialog)) {
                    closeConfirmDialog = new CloseConfirmDialog(MainActivity.this);
                    closeConfirmDialog.setOnCloseOrderLitener(new CloseConfirmDialog.OnCloseOrderLitener() {
                        @Override
                        public void close() {
                            finish();
                        }
                    });
                }
                if (!closeConfirmDialog.isShowing()) {
                    closeConfirmDialog.show();
                }

            }
        });
        helper.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        helper.stop();
        helper = null;
    }

    private void init() {
        loadingDialog = new LoadingDialog(MainActivity.this, "支付中...");
        String memberNum = getIntent().getStringExtra("memberNum");
        if (!ObjectUtils.isEmpty(memberNum)) {
            tvMemberId.setText("会员ID： " + memberNum);
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        goodsRecyclerview.setLayoutManager(linearLayoutManager);
        mAdapter = new CarAdapter(R.layout.car_item);
        goodsRecyclerview.setAdapter(mAdapter);

        View emptyView = getLayoutInflater().inflate(R.layout.emptyview, (ViewGroup) goodsRecyclerview.getParent(), false);
        ImageView scanGif = emptyView.findViewById(R.id.iv_scan_gif);
        Glide.with(MainActivity.this).load(R.drawable.scan_gif).into(scanGif);

        mAdapter.setEmptyView(emptyView);
        mAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_LEFT);
        mAdapter.setDuration(500);
        mAdapter.isFirstOnly(true);

        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                MenusBean menusBean = menus.get(position);
                EditGoodsDialog editGoodsDialog = new EditGoodsDialog(MainActivity.this);
                editGoodsDialog.setcontent(menusBean.getName() + "*" + menusBean.getCount() + menusBean.getUnit());
                editGoodsDialog.setNum(menus.get(position).getCount());
                editGoodsDialog.show();
                editGoodsDialog.setUpdateGoodsNumberListener(new EditGoodsDialog.UpdateGoodsNumberListener() {
                    @Override
                    public void updateGoodsNumber(int number) {
                        if (number == 0) {
                            mAdapter.remove(position);
                            menus.remove(position);
                            editGoodsDialog.dismiss();
                        } else {
                            menus.get(position).setCount(number);
                            menus.get(position).setMoney(ResourcesUtils.getString(R.string.units_money) + decimalFormat.format(number * Float.parseFloat(menus.get(position).getUnitPrice().substring(1))));
                            mAdapter.notifyItemChanged(position);
                        }
                        updateView();
                    }
                });

            }
        });

        mUsbPrinter = new UsbPrinter(MainActivity.this);
    }

    public void doSuceess(String payType) {
        DouHaoPrint(menus, payType);
        Intent intent = new Intent(MainActivity.this, SucessActivity.class);
        intent.putExtra("menus", (Serializable) menus);
        intent.putExtra("count", totalCount);
        startActivity(intent);
        finish();
    }

    private UsbDevice getCorrectDevice() {
        final UsbManager usbMgr = (UsbManager) getSystemService(Context.USB_SERVICE);
        final Map<String, UsbDevice> devMap = usbMgr.getDeviceList();
        for (String name : devMap.keySet()) {
            Log.v("test", "check device: " + name);
            if (UsbPrinter.checkPrinter(devMap.get(name)))
                return devMap.get(name);
        }
        return null;
    }

    public void DouHaoPrint(ArrayList<MenusBean> menus, String payType) {
        final UsbDevice dev = getCorrectDevice();
        if (dev != null && mUsbPrinter.open(dev)) {
            mUsbPrinter.init();
            mUsbPrinter.doFunction(Const.TX_ALIGN, Const.TX_ALIGN_CENTER, 0);
            mUsbPrinter.outputStringLn("杭州微盘每日付收款明细");
            mUsbPrinter.outputStringLn("\n********************************");
            mUsbPrinter.doFunction(Const.TX_ALIGN, Const.TX_ALIGN_LEFT, 0);
            mUsbPrinter.outputStringLn("店 员 号：032  POS号：32");
            mUsbPrinter.outputStringLn("下单时间：" + TimeUtils.getNowString());
            mUsbPrinter.outputStringLn("支付方式：" + payType);
            mUsbPrinter.outputStringLn("********************************");
            printString("商品名称/数量/单位", "合计");
            mUsbPrinter.outputStringLn("--------------------------------");
            float price = 0.00f;
            for (MenusBean menu : menus) {
                printString(menu.getName() + " * " + menu.getCount() + " " + menu.getUnit(), menu.getMoney().replace("¥", "￥"));
                price = price + Float.parseFloat(menu.getMoney().substring(1));
            }
            mUsbPrinter.outputStringLn("--------------------------------");
            printString("自助收银", "共" + totalCount + "件");
            printString("商品合计", "￥" + price);
            printString("优惠金额", "-￥0.00");
            printString("优惠券金额", "-￥0.00");
            mUsbPrinter.outputStringLn(" ");
            printString("应付金额", "￥" + price);
            printString("实际支付金额", "￥" + realPayMoney);
            mUsbPrinter.outputStringLn("\n依法预付费卡、第三方卡、支付优惠的支付方式其消费金额不再重复开立发票");
            mUsbPrinter.outputStringLn("\n--------------------------------");
            mUsbPrinter.doFunction(Const.TX_FEED, 10, 0);

            mUsbPrinter.doFunction(Const.TX_UNIT_TYPE, Const.TX_UNIT_PIXEL, 0);
            mUsbPrinter.doFunction(Const.TX_FEED, 140, 0);
            mUsbPrinter.doFunction(Const.TX_CUT, Const.TX_CUT_FULL, 0);
            mUsbPrinter.close();
        }

    }

    private void printString(String left, String right) {
        int lLength = 0;
        int rLength = 0;
        try {
            lLength = left.getBytes("GBK").length;
            rLength = right.getBytes("GBK").length;
            mUsbPrinter.doFunction(Const.TX_ALIGN, Const.TX_ALIGN_LEFT, 0);
            String nullString = "                                ";
            if (left.length() > 32) {
                mUsbPrinter.outputStringLn(left + nullString.substring(0, 64 - lLength - rLength) + right);
            } else {
                mUsbPrinter.outputStringLn(left + nullString.substring(0, 32 - lLength - rLength) + right);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private StringBuilder sb = new StringBuilder();
    private Handler myHandler = new Handler();

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        switch (action) {
            case KeyEvent.ACTION_DOWN:
                int unicodeChar = event.getUnicodeChar();
                if (unicodeChar != 0) {
                    sb.append((char) unicodeChar);
                }
                if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
                    return super.dispatchKeyEvent(event);
                }
                if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    return super.dispatchKeyEvent(event);
                }
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    return super.dispatchKeyEvent(event);
                }
                if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
                    return super.dispatchKeyEvent(event);
                }
                if (event.getKeyCode() == KeyEvent.KEYCODE_HOME) {
                    return super.dispatchKeyEvent(event);
                }
                if (event.getKeyCode() == KeyEvent.KEYCODE_POWER) {
                    return super.dispatchKeyEvent(event);
                }
                final int len = sb.length();
                myHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (len != sb.length()) return;
                        if (sb.length() > 0) {
                            String result = sb.toString().replace(" ", "").replace("\n", "");
//                            if (!ObjectUtils.isEmpty(scanQrCodeDialog) && scanQrCodeDialog.isShowing()) {
//                                scanQRCode(result);
//                            } else {
                            scanResult(result);
//                            }
                            sb.setLength(0);
                        }
                    }
                }, 200);
                return true;
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    private void scanResult(String code) {
        code = code.replace(" ", "").replace("\n", "");
        Log.i("test", "code = " + code);
        if (GoodsCode.getInstance().getGood().containsKey(code)) {
            GvBeans mOther = GoodsCode.getInstance().getGood().get(code);
            MenusBean bean = new MenusBean();
            bean.setId("" + (menus.size() + 1));
            bean.setMoney(mOther.getPrice());
            bean.setImgId(mOther.getImgId());
            bean.setImgUrl(mOther.getImgUrl());
            bean.setName(mOther.getName());
            bean.setCode(mOther.getCode());
            bean.setUnit(mOther.getUnit());
            bean.setUnitPrice(mOther.getPrice());
            bean.setCount(1);

            boolean isExist = false;
            int position = menus.size();
            for (int i = 0; i < menus.size(); i++) {
                if (ObjectUtils.equals(menus.get(i).getCode(), bean.getCode())) {
                    isExist = true;
                    position = i;
                    menus.get(i).setCount(menus.get(i).getCount() + 1);
                    menus.get(i).setMoney(ResourcesUtils.getString(R.string.units_money) + decimalFormat.format((Float.parseFloat(menus.get(i).getMoney().substring(1)) + Float.parseFloat(bean.getMoney().substring(1)))));
                }
            }
            if (isExist) {
                mAdapter.notifyItemChanged(position);
            } else {
                menus.add(bean);
                mAdapter.addData(position, bean);
            }
            updateView();
        }
    }

    public void updateView() {
        ringtone.play();
        float price = 0.00f;
        int count = 0;
        if (ObjectUtils.isEmpty(menus) && menus.size() == 0) {
            btGoPay.setEnabled(false);
        } else {
            for (MenusBean bean1 : menus) {
                price = price + Float.parseFloat(bean1.getMoney().substring(1));
                count = count + bean1.getCount();
            }
            btGoPay.setEnabled(true);
        }
        totalCount = count;
        totalMoney = decimalFormat.format(price);
        tvTotalMoney.setText("合计 " + totalMoney + "元");
        tvTotalCount.setText("(共" + totalCount + "件商品)");
    }

    private void showPayPopWindow() {
        mPhotoPopupWindow = new PayPopupWindow(MainActivity.this, "共" + totalCount + "件商品", "￥" + realPayMoney);
        mPhotoPopupWindow.setPopListener(new PayPopupWindow.PopLitener() {
            @Override
            public void onClosed() {
                mPhotoPopupWindow.dismiss();
            }

            @Override
            public void onPart1() {
                wxPay();
                mPhotoPopupWindow.dismiss();
            }

            @Override
            public void onPart2() {
                if (ObjectUtils.isEmpty(scanQrCodeDialog)) {
                    scanQrCodeDialog = new ScanQrCodeDialog(MainActivity.this);
                    scanQrCodeDialog.setOnScanResultLitener(new ScanQrCodeDialog.OnScanResultLitener() {
                        @Override
                        public void confirm(String result) {
                            scanQRCode(result);
                        }
                    });
                }
                scanQrCodeDialog.show();
            }

            @Override
            public void onPart3() {
                if (ObjectUtils.isEmpty(scanQrCodeDialog)) {
                    scanQrCodeDialog = new ScanQrCodeDialog(MainActivity.this);
                    scanQrCodeDialog.setOnScanResultLitener(new ScanQrCodeDialog.OnScanResultLitener() {
                        @Override
                        public void confirm(String result) {
                            scanQRCode(result);
                        }
                    });
                }
                scanQrCodeDialog.show();

            }
        });
        mPhotoPopupWindow.showAtLocation(getWindow().getDecorView(),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private void wxPay() {
        HashMap localHashMap = new HashMap();
        localHashMap.put("face_authtype", "FACEPAY");
        localHashMap.put("appid", Constant.appid);
        localHashMap.put("mch_id", Constant.mch_id);
        localHashMap.put("store_id", Constant.store_id);
        String outTratNum = System.currentTimeMillis() + "";
        localHashMap.put("out_trade_no", outTratNum);
        localHashMap.put("total_fee", "1");
        localHashMap.put("ask_face_permit", "0");
        localHashMap.put("sub_appid", Constant.sub_appid);
        localHashMap.put("sub_mch_id", Constant.sub_mch_id);
        if (TextUtils.isEmpty(Constant.authInfo)) {
            Toast.makeText(MainActivity.this, "初始化失败，请退出重进", Toast.LENGTH_SHORT).show();
            return;
        }
        localHashMap.put("authinfo", Constant.authInfo);
        localHashMap.put("payresult", "SUCCESS");

        Log.i("test", "localHashMap =  " + localHashMap.toString());
        WxPayFace.getInstance().getWxpayfaceCode(localHashMap, new IWxPayfaceCallback() {
            public void response(final Map paramMap) throws RemoteException {
                Log.i("test", "response | getWxpayfaceCode " + paramMap);
                String code = paramMap.get("return_code").toString();
                if (TextUtils.equals(code, "SUCCESS")) {
                    String url = Constant.localhostUrl + "/api/Pay/FacePay?appid=" + Constant.appid + "&mch_id=" + Constant.mch_id + "&sub_appid=" + Constant.sub_appid + "&sub_mch_id=" + Constant.sub_mch_id + "&out_trade_no=" + outTratNum + "&total_fee=" + realPayMoney + "&openid=" + paramMap.get("openid").toString() + "&face_code=" + paramMap.get("face_code").toString();
                    OkGo.<String>get(url)
                            .execute(new StringCallback() {
                                @Override
                                public void onSuccess(Response<String> response) {
                                    Log.i("test", "response.body() = " + response.body());
                                    try {
                                        ResultFacePay resultFacePay = gson.fromJson(response.body(), ResultFacePay.class);
                                        if (ObjectUtils.equals(resultFacePay.getCode(), "200")) {
                                            WxPayFace.getInstance().updateWxpayfacePayResult(localHashMap, new IWxPayfaceCallback() {
                                                public void response(Map paramMap) throws RemoteException {
                                                    doSuceess("微信扫脸支付");
                                                }
                                            });
                                        } else {
                                            ToastUtils.showShort(resultFacePay.getMsg());
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        ToastUtils.showShort("网络异常");
                                    }
                                }

                                @Override
                                public void onError(Response<String> response) {
                                    super.onError(response);
                                    ToastUtils.showShort("网络异常：" + response.body());
                                }
                            });

                } else if (TextUtils.equals(code, "USER_CANCEL")) {
                    ToastUtils.showShort("用户取消");
                } else if (TextUtils.equals(code, "SCAN_PAYMENT")) {
                    ToastUtils.showShort("扫码支付");
                } else {
                    ToastUtils.showShort(paramMap.get("return_msg").toString());
                }

            }
        });

    }

    @OnClick({R.id.tv_cancle, R.id.bt_go_pay})
    public void onViewClicked(View view) {
        ringtone.play();
        switch (view.getId()) {
            case R.id.tv_cancle:
                if (ObjectUtils.isEmpty(closeConfirmDialog)) {
                    closeConfirmDialog = new CloseConfirmDialog(MainActivity.this);
                    closeConfirmDialog.setOnCloseOrderLitener(new CloseConfirmDialog.OnCloseOrderLitener() {
                        @Override
                        public void close() {
                            finish();
                        }
                    });
                }
                closeConfirmDialog.show();
                break;
            case R.id.bt_go_pay:
                isRealDeal = (boolean) SharePreferenceUtil.getParam(MainActivity.this, PayModeSettingFragment.IS_REAL_DEAL, PayModeSettingFragment.default_isRealDeal);
                if (isRealDeal) {
                    realPayMoney = totalMoney;
                } else {
                    realPayMoney = "0.01";
                }
                showPayPopWindow();
                break;
        }
    }


    private void scanQRCode(String result) {
        loadingDialog.show();
        ArgScanQRCode arg = new ArgScanQRCode();
        arg.setAuth_code(result);
        arg.setCash_id("100112053");
        arg.setClient(1);
        arg.setRemark("刷脸支付");
        arg.setTotal_fee(realPayMoney);
        OkGoUtils.getInstance().postNoGateWay(MainActivity.this, gson.toJson(arg), "/api/pay/barcodepay", new OnResponseListener() {
            @Override
            public void onResponse(String serverRetData) {
                loadingDialog.dismiss();
                try {
                    ResultScanQRCode result = gson.fromJson(serverRetData, ResultScanQRCode.class);
                    doSuceess(ObjectUtils.equals(result.getPay_type(), "1") ? "微信扫码支付" : ObjectUtils.equals(result.getPay_type(), "2") ? "支付宝扫码支付" : "扫码支付");
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    ToastUtils.showShort("解析Json字符串失败");
                }
            }

            @Override
            public void onFail(String errMsg) {
                loadingDialog.dismiss();
                ToastUtils.showShort(errMsg);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        menus = null;
        decimalFormat = new DecimalFormat("0.00");
        mAdapter = null;
        isRealDeal = false;
        totalCount = 0;
        totalMoney = null;
        realPayMoney = null;
        mPhotoPopupWindow = null;
        scanQrCodeDialog = null;
        closeConfirmDialog = null;
        asyncUsbScanner.close();
        mUsbPrinter.close();
        mUsbPrinter = null;

    }


}
