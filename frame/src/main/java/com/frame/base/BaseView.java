package com.frame.base;


/**
 * @date 2018/7/6
 * @dessribe 通用V层
 */
public interface BaseView {
    void showLoadingDialog(String msg,boolean isCancel);
    void dismissLoadingDialog();
}