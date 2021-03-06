package com.frame.support;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.arialyy.aria.core.Aria;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.frame.FrameApplication;
import com.frame.config.AppConfig;
import com.frame.config.BaseConfig;
import com.frame.support.util.ChannelUtils;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.smtt.sdk.QbSdk;

import java.io.File;


public class AppContext extends FrameApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);//初始化工具类
        LogUtils.getConfig().setLogSwitch(AppConfig.DEBUG);//设置log开关
        //initBugly();
        Aria.init(this);
        initX5();
        initFolder();
    }

    /**
     * 不改变Oncreate()方法中的业务逻辑
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        // 安装tinker插件(热更新相关)
        // Beta.installTinker();
    }

    /**
     * 初始化X5
     * 搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
     */
    private void initX5() {
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
            @Override
            public void onViewInitFinished(boolean arg0) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                LogUtils.e("X5", " onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(), cb);
    }

    /**
     * 创建APP文件
     */
    @SuppressLint("MissingPermission")
    private void initFolder() {
        File dir = new File(BaseConfig.APP_FOLDER);  // 创建app目录
        if (!dir.exists()) {
            dir.mkdir();
        }
        File fileDir = new File(BaseConfig.FILE_FOLDER);  //创建file目录
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
    }

    /**
     * 初始化bugly
     */
    private void initBugly() {
        Context context = getApplicationContext();
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);// 设置是否为上报进程
        strategy.setAppChannel(ChannelUtils.getChannel());  //设置渠道
        // CrashReport.setUserId("");//设置用户ID
        //  Bugly.init(context, "8706956f68", AppConfig.DEBUG, strategy);//使用热更新或者升级功能时使用这个(热更新相关)
        CrashReport.initCrashReport(context, "8706956f68", AppConfig.DEBUG, strategy); // 仅使用异常捕获功能时使用这个
    }

}