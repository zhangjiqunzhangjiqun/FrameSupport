package com.frame.support.util;

import android.graphics.Bitmap;

import com.blankj.utilcode.util.ImageUtils;
import com.frame.FrameApplication;
import com.frame.config.BaseConfig;
import com.frame.support.R;
import com.frame.util.ToastUtil;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * 微信分享工具类
 */
public class WeChatShareUtils {

    /**
     * 分享微信文本
     * @param type    1：朋友圈  2：好友对话
     */
    public static void shareWeChatTxt(String content, int type) {
        WXTextObject textObject = new WXTextObject();
        textObject.text = content;
        WXMediaMessage mediaMessage = new WXMediaMessage();
        mediaMessage.mediaObject = textObject;
        mediaMessage.description = content;
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = "";
        req.message = mediaMessage;
        req.scene = type == 1 ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        String appId = BaseConfig.WEIXIN_APP_ID;
        IWXAPI iwxapi = WXAPIFactory.createWXAPI(FrameApplication.mContext, appId);
        if (!iwxapi.isWXAppInstalled()) {
            ToastUtil.showShortToast("未安装微信,请安装后再尝试分享");
            return;
        }
        iwxapi.registerApp(appId);
        iwxapi.sendReq(req);
    }

    /**
     * 发起app网页分享
     * @param type  1：朋友圈  2：好友对话
     */
    public static void shareWeChatUrl(String title, String desc, String url, int type ) {
        //初始化一个WXWebpageObject，填写url
        WXWebpageObject object = new WXWebpageObject();
        object.webpageUrl = url;
        //用 WXWebpageObject 对象初始化一个 WXMediaMessage 对象
        WXMediaMessage mediaMessage = new WXMediaMessage();
        mediaMessage.mediaObject = object;
        mediaMessage.title = title;//网页标题
        mediaMessage.description = desc;//网页描述
        Bitmap bitmap = ImageUtils.getBitmap(R.mipmap.ic_launcher);//图片
        mediaMessage.thumbData = ImageUtils.bitmap2Bytes(bitmap, Bitmap.CompressFormat.PNG);
        //构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = "";
        req.message = mediaMessage;
        req.scene = type == 1 ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        String appId = BaseConfig.WEIXIN_APP_ID;
        IWXAPI iwxapi = WXAPIFactory.createWXAPI(FrameApplication.mContext, appId);
        if (!iwxapi.isWXAppInstalled()) {
            ToastUtil.showShortToast("未安装微信,请安装后再尝试分享");
            return;
        }
        iwxapi.registerApp(appId);
        iwxapi.sendReq(req);
    }
}