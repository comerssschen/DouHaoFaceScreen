package com.weipan.douhaofacescreen.adapter;

import com.blankj.utilcode.util.ObjectUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.weipan.douhaofacescreen.R;
import com.weipan.douhaofacescreen.bean.MenusBean;
import com.weipan.douhaofacescreen.util.ImageUtil;

/**
 * 作者：create by comersss on 2019/4/2 16:35
 * 邮箱：904359289@qq.com
 */
public class CarAdapter extends BaseQuickAdapter<MenusBean, BaseViewHolder> {
    public CarAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(BaseViewHolder helper, MenusBean info) {
        helper.setText(R.id.tv_name_item, info.getName());
        helper.setText(R.id.tv_unit_item, ObjectUtils.isEmpty(info.getUnit()) ? info.getUnitPrice() : info.getUnitPrice() + "/" + info.getUnit());
        helper.setText(R.id.tv_num_item, "* " + info.getCount());
        helper.setText(R.id.tv_money_item, info.getMoney());

        if (ObjectUtils.isEmpty(info.getImgUrl())) {
            helper.setImageResource(R.id.iv_icon_item, info.getImgId());
        } else {
            helper.setImageBitmap(R.id.iv_icon_item, ImageUtil.getDiskBitmap(info.getImgUrl()));
        }
        helper.addOnClickListener(R.id.tv_edit_item);

    }
}
