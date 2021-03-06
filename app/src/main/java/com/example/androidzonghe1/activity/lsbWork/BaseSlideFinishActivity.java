package com.example.androidzonghe1.activity.lsbWork;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidzonghe1.R;
import com.example.androidzonghe1.others.lsbWork.SlideFinishRelativeLayout;


/**
 * 需要有SlideFinish效果的Activity继承自这个自Activity即可
 */
public abstract class BaseSlideFinishActivity extends AppCompatActivity implements SlideFinishRelativeLayout.IOnSlideToFinish, SlideFinishRelativeLayout.IOnSlideFinishChangeListener {
    private SlideFinishRelativeLayout mSlideFinishRelativeLayout;
    private boolean mIsOpenSlideFinish;
    private SlideFinishRelativeLayout.SlideMode mSlideMode;
    private ViewGroup mSlideView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        mIsOpenSlideFinish = true;
        mSlideMode = SlideFinishRelativeLayout.SlideMode.ALL;
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        if (mIsOpenSlideFinish) {
            View viewRoot = LayoutInflater.from(this).inflate(R.layout.activity_base, null);
            mSlideFinishRelativeLayout =
                    (SlideFinishRelativeLayout) viewRoot.findViewById(R.id.layout_root);
            mSlideFinishRelativeLayout.setOnSlideToFinishListener(this);
            mSlideFinishRelativeLayout.setSlideMode(mSlideMode);
            mSlideFinishRelativeLayout.setSlideEnable(mIsOpenSlideFinish);
            mSlideFinishRelativeLayout.setOnSlideFinishChangeListener(this);
            mSlideFinishRelativeLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSlideView = mSlideFinishRelativeLayout.getSlideView();
                    mSlideView.setPivotX(mSlideView.getWidth() / 2);
                    mSlideView.setPivotY(mSlideView.getHeight());
                }
            });
            ViewStub viewStub = (ViewStub) viewRoot.findViewById(R.id.layout_content);
            viewStub.setLayoutResource(layoutResID);
            viewStub.inflate();
            super.setContentView(viewRoot);
        } else {
            super.setContentView(layoutResID);
        }
    }

    /**
     * 设置滑动模式
     */

    protected void setSlideMode(SlideFinishRelativeLayout.SlideMode slideMode) {
        if (slideMode != null) {
            mSlideMode = slideMode;
            if (mSlideFinishRelativeLayout != null) {
                mSlideFinishRelativeLayout.setSlideMode(slideMode);
            }
        }
    }

    /**
     * 是否使能滑动finish
     */
    protected void enableSlideFinish(boolean isOpenSlideFinish) {
        mIsOpenSlideFinish = isOpenSlideFinish;
        if (mSlideFinishRelativeLayout != null) {
            mSlideFinishRelativeLayout.setSlideEnable(isOpenSlideFinish);
        }
    }

    protected ViewGroup getSlideView() {
        return mSlideView;
    }

    protected void setOnSlideFinishChangeListener(SlideFinishRelativeLayout.IOnSlideFinishChangeListener onSlideFinishChangeListener) {
        mSlideFinishRelativeLayout.setOnSlideFinishChangeListener(onSlideFinishChangeListener);
    }

    @Override
    public void onFinish() {
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mSlideFinishRelativeLayout != null && mIsOpenSlideFinish) {
            mSlideFinishRelativeLayout.scrollToFinishImmediately();
        }
    }

    @Override
    public void onSlideFinishChange(View slideView, float slidePercent) {
        slideView.setRotation(30 * slidePercent);
    }
}
