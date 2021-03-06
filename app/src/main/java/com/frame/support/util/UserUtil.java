package com.frame.support.util;


import android.text.TextUtils;

import com.blankj.utilcode.util.SPStaticUtils;

/**
 * @data on 2019/2/28 15:35
 * @describe 用户操作工具类
 */
public class UserUtil {
/*************************登录和退出相关操作开始*******************************/

    /**
     * 获取token
     */
    public static String getToken() {
        return SPStaticUtils.getString("token", "");
    }

    /**
     * 设置token
     */
    public static void setToken(String token) {
        SPStaticUtils.put("token", token);
    }

    /**
     * 判断用户是否登录
     * 目前是根据token来判断
     */
    public static boolean isLogin() {
        if (TextUtils.isEmpty(getToken()) || getToken().trim().equals(""))
            return false;
        else
            return true;
    }

    /**
     * 退出登录
     */
    public static void logout() {
        setToken("");
        //   setUserInfo(null);
    }

//    /**
//     * 得到用户信息
//     */
//    public static UserInfoBean getUserInfo() {
//        String infoJson = SPStaticUtils.getString("userInfo", "");
//        if (TextUtils.isEmpty(infoJson)) {
//            return null;
//        } else {
//            return GsonUtil.getBean(infoJson, UserInfoBean.class);
//        }
//    }
//
//    /**
//     * 储存用户信息
//     */
//    public static void setUserInfo(UserInfoBean userInfo) {
//        if (userInfo == null)
//            SPStaticUtils.put("userInfo", "");
//        else
//            SPStaticUtils.put("userInfo", new Gson().toJson(userInfo));
//    }
/*************************登录和退出相关操作结束*******************************/
}