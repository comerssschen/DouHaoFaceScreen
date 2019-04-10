package com.weipan.douhaofacescreen.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sunmi.extprinterservice.ExtPrinterService;
import com.sunmi.payment.PaymentService;
import com.weipan.douhaofacescreen.adapter.CarAdapter;
import com.weipan.douhaofacescreen.bean.ArgScanQRCode;
import com.weipan.douhaofacescreen.bean.Config;
import com.weipan.douhaofacescreen.bean.GoodsCode;
import com.weipan.douhaofacescreen.bean.GvBeans;
import com.weipan.douhaofacescreen.bean.MenusBean;
import com.weipan.douhaofacescreen.bean.PayResultBean;
import com.weipan.douhaofacescreen.bean.Request;
import com.weipan.douhaofacescreen.bean.ResultScanQRCode;
import com.weipan.douhaofacescreen.common.BaseActivity;
import com.weipan.douhaofacescreen.fragment.PayModeSettingFragment;
import com.weipan.douhaofacescreen.listener.KPrinterPresenter;
import com.weipan.douhaofacescreen.listener.OnResponseListener;
import com.weipan.douhaofacescreen.service.ResultReceiver;
import com.weipan.douhaofacescreen.util.CountDownHelper;
import com.weipan.douhaofacescreen.util.OkGoUtils;
import com.weipan.douhaofacescreen.util.ResourcesUtils;
import com.weipan.douhaofacescreen.util.SharePreferenceUtil;
import com.weipan.douhaofacescreen.view.CloseConfirmDialog;
import com.weipan.douhaofacescreen.view.EditGoodsDialog;
import com.weipan.douhaofacescreen.view.LoadingDialog;
import com.weipan.douhaofacescreen.view.PayPopupWindow;
import com.weipan.douhaofacescreen.view.ScanQrCodeDialog;
import com.weipan.douhaofacescreen.R;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;

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
    private String realPayMoney;
    private PayPopupWindow mPhotoPopupWindow;
    public static KPrinterPresenter kPrinterPresenter;
    private ExtPrinterService extPrinterService = null;//k1 打印服务
    private ResultReceiver resultReceiver;
    private ScanQrCodeDialog scanQrCodeDialog;
    private Gson gson = new Gson();
    private CloseConfirmDialog closeConfirmDialog;
    private CountDownHelper helper;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
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
        mAdapter.setEmptyView(R.layout.emptyview, goodsRecyclerview);
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

//                            mAdapter.remove(position);
//                            mAdapter.addData(position, menus.get(position));

                        }
                        updateView();

                    }
                });

            }
        });

        Intent intent = new Intent();
        intent.setPackage("com.sunmi.extprinterservice");
        intent.setAction("com.sunmi.extprinterservice.PrinterService");
        bindService(intent, connService, Context.BIND_AUTO_CREATE);
        registerResultReceiver();

    }

    public void doSuceess(String payType) {
        kPrinterPresenter.print(menus, payType);
        Intent intent = new Intent(MainActivity.this, SucessActivity.class);
        intent.putExtra("menus", (Serializable) menus);
        intent.putExtra("count", totalCount);
        startActivity(intent);
        finish();

//        totalCount = 0;
//        menus.clear();
//        tvTotalMoney.setText("");
//        tvTotalCount.setText("");
//        btGoPay.setBackgroundColor(Color.parseColor("#999999"));
    }

    private void registerResultReceiver() {
        resultReceiver = new ResultReceiver(new ResultReceiver.ResultCallback() {
            @Override
            public void callback(String result) {
                loadingDialog.dismiss();
                Log.i("test", result);

                if (ObjectUtils.isEmpty(result)) {
                    ToastUtils.showShort("网络连接失败，请重试");
                    return;
                }
                PayResultBean response = gson.fromJson(result, PayResultBean.class);
                switch (response.getResultCode()) {
                    case "T00"://交易成功
                        doSuceess("支付宝扫脸支付");
                        break;
                    default:
                        // 交易失败
                        String resultMsg = response.getResultMsg();
                        Intent intent = new Intent(MainActivity.this, FailActivity.class);
                        intent.putExtra("msg", resultMsg);
                        startActivityForResult(intent, 20);
                        break;
                }
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ResultReceiver.RESPONSE_ACTION);
        registerReceiver(resultReceiver, intentFilter);
    }

    private ServiceConnection connService = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            extPrinterService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            extPrinterService = ExtPrinterService.Stub.asInterface(service);
            kPrinterPresenter = new KPrinterPresenter(MainActivity.this, extPrinterService);
        }
    };

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
//                mAdapter.remove(position);
//                mAdapter.addData(position, menus.get(position));
                mAdapter.notifyItemChanged(position);
//                mAdapter.setData(position, menus.get(position));
            } else {
                menus.add(bean);
                mAdapter.addData(position, bean);
            }
            updateView();
        }
    }


    public void updateView() {
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
                loadingDialog.show();
                Request request = new Request();
                // 应用类型
                request.appType = "51";
                // 应用包名
                request.appId = getPackageName();
                // 交易类型
                request.transType = "00";
                // 交易金额
                request.amount = Float.valueOf(Float.valueOf(realPayMoney) * 100).longValue();
                // Saas软件订单号
//                request.orderId = "123346546465";
                // 商品信息
//                request.orderInfo = "商品信息";
                // 支付码
//                request.payCode = "17682310719";
                Config config = new Config();
                // 交易过程中是否显示UI界面
                config.processDisplay = true;
//                // 是否展示交易结果页
                config.resultDisplay = false;
//                // 是否打印小票
                config.printTicket = false;
//                // 指定签购单上的退款订单号类型
//                config.printIdType = "指定签购单上的退款订单号类型";
//                // 备注
//                config.remarks = "备注";
                request.config = config;
                String jsonStr = gson.toJson(request);
                Log.i("test", jsonStr);
                PaymentService.getInstance().callPayment(jsonStr);

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

    @OnClick({R.id.tv_cancle, R.id.bt_go_pay})
    public void onViewClicked(View view) {
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
        if (extPrinterService != null) {
            unbindService(connService);
        }
        kPrinterPresenter = null;
        if (resultReceiver != null) {
            unregisterReceiver(resultReceiver);
        }

        menus = null;
        decimalFormat = new DecimalFormat("0.00");
        mAdapter = null;
        isRealDeal = false;
        totalCount = 0;
        totalMoney = null;
        realPayMoney = null;
        mPhotoPopupWindow = null;
        kPrinterPresenter = null;
        extPrinterService = null;//k1 打印服务
        scanQrCodeDialog = null;
        closeConfirmDialog = null;
    }


}
