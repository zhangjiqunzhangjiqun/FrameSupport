package com.frame.base.fragment;


import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.frame.R;
import com.frame.base.BaseView;
import com.frame.bean.EventBean;
import com.frame.view.LoadingDialog;
import com.gyf.immersionbar.components.SimpleImmersionOwner;
import com.gyf.immersionbar.components.SimpleImmersionProxy;
import com.trello.rxlifecycle3.components.support.RxFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.ButterKnife;

/**
 * @Description: Fragment基类
 */
public abstract class BaseFragment extends RxFragment implements BaseView, SimpleImmersionOwner {
    protected Activity mActivity;
    protected LoadingDialog progressDialog;
    protected View rootView;

    public BaseFragment() {
        super();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
        mActivity = getActivity();
        rootView = inflater.inflate(getLayoutID(), container, false);
        ButterKnife.bind(this, rootView);//绑定framgent
        initCommon();
        if (isRegisterEventBus())
            EventBus.getDefault().register(this);
        return rootView;
    }

    protected void initCommon() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (immersionBarEnabled())
            mSimpleImmersionProxy.onActivityCreated(savedInstanceState);
        init(savedInstanceState);
        initData();
    }

    protected abstract void init(Bundle savedInstanceState);

    protected abstract void initData();

    protected abstract int getLayoutID();

    protected String getResString(int res) {
        return getResources().getString(res);
    }

    protected int getResInt(int res) {
        return getResources().getInteger(res);
    }

    /**
     * 是否需要注册EventBus
     */
    protected boolean isRegisterEventBus() {
        return false;
    }

    /**
     * 空布局时,Rv头部是否显示,结合{@link BaseFragment#UserAdapterEmpty()}使用
     */
    protected boolean isHeaderAndEmpty() {
        return false;
    }

    /**
     * 无数据时,是否使用Adapter设置空布局(不能和frame_root_view一起使用)
     */
    protected boolean UserAdapterEmpty() {
        return false;
    }

    /**
     * 是否需要开启沉浸式
     */
    @Override
    public boolean immersionBarEnabled() {
        return false;
    }

    @Override
    public void initImmersionBar() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventBusCome(EventBean event) {
        if (event != null)
            receiveEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onStickyEventBusCome(EventBean event) {
        if (event != null)
            receiveStickyEvent(event);
        EventBus.getDefault().removeStickyEvent(event);//手动移除,不然还是会接收到
    }

    /**
     * 接收到分发的普通事件
     */
    protected void receiveEvent(EventBean event) {
    }

    /**
     * 接受到分发的粘性事件
     */
    protected void receiveStickyEvent(EventBean event) {
    }

    /**
     * 显示加载对话框
     */
    @Override
    public void showLoadingDialog(String msg, boolean isCancel) {
        String message = TextUtils.isEmpty(msg) ? getResString(R.string.frame_load_ing) : msg;
        if (progressDialog == null)
            progressDialog = new LoadingDialog(mActivity);
        progressDialog.setCancle(isCancel);
        progressDialog.setMsg(message);
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    /**
     * 隐藏加载对话框
     */
    public void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    /**
     * 水平布局
     */
    protected void setLayoutManager(RecyclerView rv, int orientation) {
        rv.setLayoutManager(new LinearLayoutManager(mActivity, orientation == 1 ? RecyclerView.VERTICAL : RecyclerView.HORIZONTAL, false));
    }

    /**
     * 表格布局
     */
    protected void setLayoutManager(RecyclerView rv, int spanCount, int orientation) {
        rv.setLayoutManager(new GridLayoutManager(mActivity, spanCount, orientation == 1 ? RecyclerView.VERTICAL : RecyclerView.HORIZONTAL, false));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //页面销毁时隐藏dialog，否则重新打开页面时可能会报java.lang.IllegalArgumentException: View not attached to window manager
        dismissLoadingDialog();
        if (isRegisterEventBus())
            EventBus.getDefault().unregister(this);
        if (immersionBarEnabled())
            mSimpleImmersionProxy.onDestroy();
    }
    /************************************ImmersionBar沉浸式相关***********************************/
    /**
     * ImmersionBar代理类
     */
    private SimpleImmersionProxy mSimpleImmersionProxy = new SimpleImmersionProxy(this);

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (immersionBarEnabled())
            mSimpleImmersionProxy.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (immersionBarEnabled())
            mSimpleImmersionProxy.onHiddenChanged(hidden);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (immersionBarEnabled())
            mSimpleImmersionProxy.onConfigurationChanged(newConfig);
    }
}
