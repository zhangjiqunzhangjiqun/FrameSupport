package com.frame.base;


import android.annotation.SuppressLint;


import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.frame.bean.BaseBean;
import com.frame.bean.FileInfoBean;
import com.frame.config.AppConfig;
import com.frame.request.APIException;
import com.frame.request.HttpRequest;
import com.frame.util.GsonUtil;
import com.frame.util.ToastUtil;
import com.trello.rxlifecycle3.android.ActivityEvent;
import com.trello.rxlifecycle3.android.FragmentEvent;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;
import com.trello.rxlifecycle3.components.support.RxFragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;

/**
 * 网络层
 */
public class BaseModel {

    private final HttpRequest mHttpRequest;
    private Builder mBuilder;
    private String log_tag = "okhttp";
    private boolean mIsEmpty;

    private BaseModel(Builder builder) {
        mBuilder = builder;
        mHttpRequest = new HttpRequest();
    }

    public void post(String api) {
        post(api, BaseBean.class);
    }

    public void get(String api) {
        get(api, BaseBean.class);
    }

    public <B extends BaseBean> void post(String api, final Class<B> clazz) {
        Observer<ResponseBody> responseBodySubscriber = getResponseBodySubscriber(api, clazz);
        if (responseBodySubscriber == null) return;
        if (mBuilder.mParam == null || mBuilder.mParam.size() == 0) { //参数为空时，添加一个无用参数
            mBuilder.mParam = new HashMap<>();
            mBuilder.mParam.put("", "");
        }
        if (mBuilder.isSyncLifeCycle) {//是否需要同步生命周期
            if (mBuilder.mBaseRequestView instanceof RxAppCompatActivity) {
                mHttpRequest.post(api, mBuilder.mParam, ((RxAppCompatActivity) mBuilder.mBaseRequestView).bindUntilEvent(ActivityEvent.DESTROY)).subscribe(responseBodySubscriber);
            } else if (mBuilder.mBaseRequestView instanceof RxFragment) {
                mHttpRequest.post(api, mBuilder.mParam, ((RxFragment) mBuilder.mBaseRequestView).bindUntilEvent(FragmentEvent.DESTROY)).subscribe(responseBodySubscriber);
            } else {
                mHttpRequest.post(api, mBuilder.mParam, null).subscribe(responseBodySubscriber);
            }
        } else {
            mHttpRequest.post(api, mBuilder.mParam, null).subscribe(responseBodySubscriber);
        }
    }

    public <B extends BaseBean> void get(String api, final Class<B> clazz) {
        Observer<ResponseBody> responseBodySubscriber = getResponseBodySubscriber(api, clazz);
        if (responseBodySubscriber == null) return;
        if (mBuilder.isSyncLifeCycle) {//是否需要同步生命周期
            if (mBuilder.mBaseRequestView instanceof RxAppCompatActivity) {
                mHttpRequest.get(api, ((RxAppCompatActivity) mBuilder.mBaseRequestView).bindUntilEvent(ActivityEvent.DESTROY)).subscribe(responseBodySubscriber);
            } else if (mBuilder.mBaseRequestView instanceof RxFragment) {
                mHttpRequest.get(api, ((RxFragment) mBuilder.mBaseRequestView).bindUntilEvent(FragmentEvent.DESTROY)).subscribe(responseBodySubscriber);
            } else {
                mHttpRequest.get(api, null).subscribe(responseBodySubscriber);
            }
        } else {
            mHttpRequest.get(api, null).subscribe(responseBodySubscriber);//开启请求
        }
    }

    public <B extends BaseBean> void upload(String api, final Class<B> clazz) {
        Observer<ResponseBody> responseBodySubscriber = getResponseBodySubscriber(api, clazz);
        if (responseBodySubscriber == null) return;
        if (mBuilder.isSyncLifeCycle) {//是否需要同步生命周期
            if (mBuilder.mBaseRequestView instanceof RxAppCompatActivity) {
                mHttpRequest.uploadFile(api, mBuilder.mParam, mBuilder.multiFileKey, mBuilder.mFileInfoBeans, ((RxAppCompatActivity) mBuilder.mBaseRequestView).bindUntilEvent(ActivityEvent.DESTROY)).subscribe(responseBodySubscriber);
            } else if (mBuilder.mBaseRequestView instanceof RxFragment) {
                mHttpRequest.uploadFile(api, mBuilder.mParam, mBuilder.multiFileKey, mBuilder.mFileInfoBeans, ((RxFragment) mBuilder.mBaseRequestView).bindUntilEvent(FragmentEvent.DESTROY)).subscribe(responseBodySubscriber);
            } else {
                mHttpRequest.uploadFile(api, mBuilder.mParam, mBuilder.multiFileKey, mBuilder.mFileInfoBeans, null).subscribe(responseBodySubscriber);
            }
        } else {
            mHttpRequest.uploadFile(api, mBuilder.mParam, mBuilder.multiFileKey, mBuilder.mFileInfoBeans, null).subscribe(responseBodySubscriber);
        }
    }

    private <B extends BaseBean> Observer<ResponseBody> getResponseBodySubscriber(final String tag, final Class<B> clazz) {
        //检查网络
        if (!NetworkUtils.isWifiConnected() && !NetworkUtils.isConnected()) {
            if (mBuilder.mBaseRequestView instanceof BaseSwipeView) {
                ((BaseSwipeView) mBuilder.mBaseRequestView).resetRefreshView();
                if (mBuilder.mLoadMode == LoadMode.LOAD_MODE)
                    ((BaseSwipeView) mBuilder.mBaseRequestView).loadMoreFailView();
            }
            if (mBuilder.mLoadStyle == LoadStyle.DIALOG_VIEW || mBuilder.mLoadStyle == LoadStyle.VIEW)
                mBuilder.mBaseRequestView.showNetErrorView("");
            ToastUtil.showShortToast("请检查网络");
            return null;
        }
        Observer<ResponseBody> subscriber = new Observer<ResponseBody>() {

            @Override
            public void onError(Throwable e) {
                LogUtils.e(log_tag, "请求错误");
                refreshStatusView(e, mBuilder.requestTag == null ? tag : mBuilder.requestTag);
            }

            @Override
            public void onComplete() {
                switch (mBuilder.mLoadStyle) {
                    case NONE:
                        break;
                    case VIEW:
                        if (mIsEmpty)
                            mBuilder.mBaseRequestView.showEmptyView();
                        else
                            mBuilder.mBaseRequestView.refreshView();
                        break;
                    case DIALOG:
                        mBuilder.mBaseRequestView.dismissLoadingDialog();
                        break;
                    case DIALOG_VIEW:
                        if (mIsEmpty)
                            mBuilder.mBaseRequestView.showEmptyView();
                        else
                            mBuilder.mBaseRequestView.refreshView();
                        mBuilder.mBaseRequestView.dismissLoadingDialog();
                        break;
                }
                if (mBuilder.mBaseRequestView instanceof BaseSwipeView)
                    ((BaseSwipeView) mBuilder.mBaseRequestView).resetRefreshView();
            }

            @Override
            public void onSubscribe(Disposable d) {
                if (mBuilder.mLoadStyle == LoadStyle.NONE) {
                    return;
                } else if (mBuilder.mLoadStyle == LoadStyle.VIEW) {
                    mBuilder.mBaseRequestView.showLoadingView();
                } else {
                    mBuilder.mBaseRequestView.showLoadingDialog(mBuilder.mMsg, mBuilder.isDialogCancel);
                }
            }

            @SuppressLint("CheckResult")
            @Override
            public void onNext(ResponseBody requestBody) {
                //解析json
                try {
                    B bean = GsonUtil.getBean(requestBody.string(), clazz);
                    mIsEmpty = bean.isEmpty();
                    if (bean.code == APIException.SUCCESS) {
                        mBuilder.mBaseRequestView.requestSuccess(bean, mBuilder.mLoadMode, mBuilder.requestTag == null ? tag : mBuilder.requestTag, mBuilder.pageCount);
                    } else {
                        mBuilder.mBaseRequestView.requestFail(bean, mBuilder.requestTag == null ? tag : mBuilder.requestTag);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (AppConfig.DEBUG) {
                        LogUtils.e("数据解析异常", e.getMessage() + "");
                        throw new RuntimeException("数据解析异常");
                    } else {
                        mBuilder.mBaseRequestView.requestError(e, mBuilder.requestTag == null ? tag : mBuilder.requestTag);
                    }
                }
            }
        };
        return subscriber;
    }

    private void refreshStatusView(Throwable e, Object tag) {
        switch (mBuilder.mLoadStyle) {
            case NONE:
                break;
            case VIEW:
                mBuilder.mBaseRequestView.showNetErrorView("服务器错误: " + e.getMessage());
                break;
            case DIALOG:
                mBuilder.mBaseRequestView.dismissLoadingDialog();
                break;
            case DIALOG_VIEW:
                mBuilder.mBaseRequestView.dismissLoadingDialog();
                mBuilder.mBaseRequestView.showNetErrorView("服务器错误: " + e.getMessage());
                break;
        }
        mBuilder.mBaseRequestView.requestError(e, tag);
        if (mBuilder.mBaseRequestView instanceof BaseSwipeView) {
            ((BaseSwipeView) mBuilder.mBaseRequestView).resetRefreshView();
            if (mBuilder.mLoadMode == LoadMode.LOAD_MODE)
                ((BaseSwipeView) mBuilder.mBaseRequestView).loadMoreFailView();
        }
    }

    //构建模式--用于添加配置
    public static class Builder {
        //默认参数
        private LoadStyle mLoadStyle = LoadStyle.NONE;
        private LoadMode mLoadMode = LoadMode.FIRST;
        private String mMsg = null;
        private Object requestTag;
        private boolean isDialogCancel = true;//请求时dialog是否可以手动取消
        private boolean isSyncLifeCycle = true;//是否同步生命周期
        //动态参数
        private Map<String, Object> mParam;
        private BaseRequestView mBaseRequestView;
        //上传文件
        private List<FileInfoBean> mFileInfoBeans;
        private String multiFileKey;
        private int pageCount = AppConfig.ViewPage.PAGE_COUNT;//每页请求的数据量

        //绑定界面就用这构造
        public Builder( BaseRequestView baseRequestView) {
            mBaseRequestView = baseRequestView;
        }

        //设置加载风格(无  ||加载框  ||重新加载)
        public Builder setLoadStyle( LoadStyle loadStyle) {
            mLoadStyle = loadStyle;
            return this;
        }

        //设置上拉加载(第一次 ||加载更多)
        public Builder setLoadMode( LoadMode loadMode) {
            mLoadMode = loadMode;
            return this;
        }

        //添加参数
        public Builder putParam( String key, Object value) {
            if (mParam == null)
                mParam = new HashMap<>();
            mParam.put(key, value);
            return this;
        }

        public Builder putAllParam( Map<String, Object> map) {
            if (mParam == null) {
                mParam = new HashMap<>();
            }
            mParam.putAll(map);
            return this;
        }

        //添加文件
        public Builder setFileInfoBeans(List<FileInfoBean> fileInfoBeans) {
            mFileInfoBeans = fileInfoBeans;
            return this;
        }

        public BaseModel create() {
            return new BaseModel(this);
        }

        //多文件上传key（后台约定）
        public Builder setMultiFileKey(String multiFileKey) {
            this.multiFileKey = multiFileKey;
            return this;
        }

        //设置加载文字(默认：拼命加载中...)
        public Builder setMsgType(String msg) {
            mMsg = msg;
            return this;
        }

        //用于多个baseben情况区分网络请求
        public Builder setRequestTag(Object requestTag) {
            this.requestTag = requestTag;
            return this;
        }

        public Builder setDialogCancel(boolean dialogCancel) {
            isDialogCancel = dialogCancel;
            return this;
        }

        public Builder isSyncLifeCycle(boolean isSync) {
            isSyncLifeCycle = isSync;
            return this;
        }

        public Builder setPageCount(int count) {
            pageCount = count;
            return this;
        }
    }

    //加载样式
    public enum LoadStyle {
        NONE(1),//静默加载
        DIALOG(2),//加载框样式加载
        VIEW(3),//布局样式加载
        DIALOG_VIEW(4);//加载框样式加载又有布局样式加载

        private int mValue;

        LoadStyle(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }
    }

    //加载模式
    public enum LoadMode {
        FIRST(1),//首次加载
        LOAD_MODE(2);//加载更多
        private int mValue;

        LoadMode(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }
    }

}
