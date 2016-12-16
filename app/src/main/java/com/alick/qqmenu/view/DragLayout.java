package com.alick.qqmenu.view;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.alick.qqmenu.R;
import com.nineoldandroids.view.ViewHelper;

/**
 * Created by Alick on 2015/10/11.
 */
public class DragLayout extends FrameLayout {
    private ViewDragHelper dragHelper;
    private static final String TAG="DragLayout";

    private FrameLayout ml_left;
    private FrameLayout ml_main;

    private int width;
    private int height;
    private int range;

    public DragLayout(Context context) {
        this(context, null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        dragHelper=ViewDragHelper.create(this,new DragCallBack());
    }

    private class DragCallBack extends ViewDragHelper.Callback{
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if(child==ml_main){
                left=fixLeft(left);
            }
            return left;
        }

        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return range;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            Log.i(TAG, "changedView=" + changedView.hashCode() + ",left=" + left);

            int newLeft=left;
            if(changedView==ml_left){
                newLeft=ml_main.getLeft()+dx;
            }

            newLeft=fixLeft(newLeft);

            if(changedView==ml_left){
                ml_left.layout(0,0,width,height);
                ml_main.layout(newLeft,0,newLeft+width,height);
            }

            invalidate();
        }
    }


    private int fixLeft(int left){
        if(left<0){
            left=0;
        }else if(left>range){
            left=range;
        }
        return left;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width=getWidth();
        height=getHeight();
        range=width*6/10;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ;
        return dragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dragHelper.processTouchEvent(event);
        return true;
    };

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ml_main = (FrameLayout) findViewById(R.id.ml_main);
        ml_left = (FrameLayout) findViewById(R.id.ml_left);

        Log.i(TAG,"ml_main="+ml_main.hashCode());
        Log.i(TAG,"ml_left="+ml_left.hashCode());
    }
}
