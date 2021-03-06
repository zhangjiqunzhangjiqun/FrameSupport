package com.frame.base;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.frame.R;

import butterknife.ButterKnife;

/**
 * 通用dialog
 */
public abstract class BaseDialog extends Dialog implements LifecycleObserver {

    public View mRootView;
    private int animResId = 0;//动画样式
    private int gravity = 0;
    private boolean isCancelable = true;//点击外部是否可以取消弹框
    private View.OnClickListener mListener;
    private int[] mIds;
    public Context mContext;

    public BaseDialog( Context context) {
        super(context, R.style.ActionSheetDialogStyle);//默认为dialog样式
        this.mContext = context;
        initCommon(context);
    }

    public BaseDialog( Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;
        initCommon(context);
    }

    public BaseDialog( Context context, int themeResId, int gravity) {
        super(context, themeResId);
        this.gravity = gravity;
        this.mContext = context;
        initCommon(context);
    }

    public BaseDialog( Context context, int themeResId, int animResId, int gravity) {
        super(context, themeResId);
        this.gravity = gravity;
        this.animResId = animResId;
        this.mContext = context;
        initCommon(context);
    }

    public BaseDialog( Context context, int themeResId, int gravity, boolean isCancelable) {
        super(context, themeResId);
        this.gravity = gravity;
        this.isCancelable = isCancelable;
        this.mContext = context;
        initCommon(context);
    }

    /**
     * 在initCommon()方法前调用
     */
    public void setGravity(int gravity) {
        this.gravity = gravity;
    }

    protected void initCommon(Context context) {
        if (context instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) context;
            if (activity != null)
                activity.getLifecycle().addObserver(this);
        }
        setCancelable(isCancelable);
        setCanceledOnTouchOutside(isCancelable);
        if (isUseShadow()) {//是否周围圆角显示
            setContentView(R.layout.dialog_shadow_bg);
            CardView cardView = findViewById(R.id.v_root);
            LayoutInflater.from(mContext).inflate(getLayoutID(), cardView);
        } else
            setContentView(getLayoutID());
        ButterKnife.bind(this);
        //居中显示
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(attributes);
        window.setGravity(gravity == 0 ? Gravity.CENTER : gravity);
        //设置动画（默认dialog样式）
        window.setWindowAnimations(animResId == 0 ? R.style.ActionSheetDialogAnimation : animResId);
        initView(context);
    }

    protected boolean isUseShadow() {
        return false;
    }

    // Tips:调用改方法后 BindView将会失效
    protected void resetContentView(Context context, @LayoutRes int res) {
        mRootView = LayoutInflater.from(context).inflate(res, null);
        setContentView(mRootView, new LinearLayout.LayoutParams(getWidth(), LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void show() {
        if (mContext instanceof Activity) {
            Activity activity = (Activity) mContext;
            if (activity == null || activity.isFinishing())
                return;
        }
        super.show();
    }

    protected int getWidth() {
        return LinearLayout.LayoutParams.MATCH_PARENT;
    }

    protected void initView(Context context) {
    }

    protected abstract @LayoutRes
    int getLayoutID();

    public BaseDialog setOnClickListener(int[] ids, View.OnClickListener listener) {
        mIds = ids;
        mListener = listener;
        if (mListener != null && mIds != null && mIds.length != 0) {
            for (int id : mIds) {
                findViewById(id).setOnClickListener(mListener);
            }
        }
        return this;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        if (isShowing())
            dismiss();
    }
}
