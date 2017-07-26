package com.alick.qqmenu.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alick.qqmenu.R;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.FloatEvaluator;
import com.nineoldandroids.animation.TypeEvaluator;
import com.nineoldandroids.view.ViewHelper;

/**
 * Created by Alick on 2015/10/11.
 */
public class DragLayout extends FrameLayout {

    protected static final String TAG = DragLayout.class.getSimpleName();

    private ViewDragHelper mDragHelper;


    private ViewGroup mLeftContent;
    private ViewGroup mMainContent;

    /**最外层布局高度*/
    private int mHeight;
    /**最外层布局宽度*/
    private int mWidth;
    /**拖拽范围*/
    private int mRange;

    private TypeEvaluator<Number> floatEvaluator=new FloatEvaluator();
    private TypeEvaluator<Number> argbEvaluator=new ArgbEvaluator();


    public DragLayout(Context context) {
        this(context,null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
        // TODO Auto-generated constructor stub
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //a.初始化操作
        mDragHelper = ViewDragHelper.create(this,mCallback);
    }

    ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {

        //根据返回结果决定当前child是否可以拖拽
        //child是当前被拖拽的view
        //pointerId是区分多点触摸的id
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
//			Log.i(TAG, "child="+child+"--->tryCaptureView()");
            return true;
        }


        //当view被捕获时被调用
        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
//			Log.i(TAG,"capturedChild="+capturedChild+"--->onViewCaptured()");
            super.onViewCaptured(capturedChild, activePointerId);
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            //返回拖拽的范围,不对拖拽进行真正的限制,仅仅决定了动画执行速度
            return mRange;
        }

        //2.根据建议值修正将要移动到的(横向)位置
        //此时没有发生真正的移动
        //child:当前拖拽的view
        //left新的位置建议值,dx位置变化量
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {

            if(child==mMainContent){
                left = fixLeft(left);
            }

            return left;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return super.clampViewPositionVertical(child, top, dy);
        };

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);

            //为了兼容低版本做到兼容:每次更改值之后进行重绘
            invalidate();
        }

        //位置已经发生改变后的回调函数
        @Override
        public void onViewPositionChanged(View changedView, int left, int top,int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
//			Log.i(TAG, "旧的left="+changedView.getLeft()+",left="+left+",top="+top+",dx="+dx+",dy="+dy+"--->onViewPositionChanged()");

            int newLeft=left;
            //思路:当拖拽的view为左面板时,强制移动主面板,且左面板不动
            if(changedView==mLeftContent){
                //禁止左面板移动
                mLeftContent.layout(0,0,mWidth,mHeight);
                //计算出主面板的偏移量
                newLeft=fixLeft(mMainContent.getLeft()+dx);
                //当触摸左面板时,使主面板移动
                mMainContent.layout(newLeft,0,newLeft+dx+mWidth,mHeight);
            }
            dispatchDragEvent(newLeft);

        }

        /**
         * 更新状态,执行动画
         * @param newLeft
         */
        private void dispatchDragEvent(int newLeft) {
            float percent=newLeft*1.0f/mRange;;
            Log.i(TAG, "percent="+percent);

			/*==========执行左面板的属性动画==========*/

            //大小缩放
            ViewHelper.setScaleX(mLeftContent,floatEvaluator.evaluate(percent, 0.5f,1.0f).floatValue());
            ViewHelper.setScaleY(mLeftContent,floatEvaluator.evaluate(percent, 0.5f,1.0f).floatValue());
            //左右移动
            ViewHelper.setTranslationX(mLeftContent, floatEvaluator.evaluate(percent,-mWidth/2,0).floatValue());
            //透明度渐变
            ViewHelper.setAlpha(mLeftContent, floatEvaluator.evaluate(percent, 0.1f, 1.0f).floatValue());

			/*==========执行主面板的属性动画==========*/
            ViewHelper.setScaleX(mMainContent, floatEvaluator.evaluate(percent,1.0f,0.8f).floatValue());
            ViewHelper.setScaleY(mMainContent, floatEvaluator.evaluate(percent,1.0f,0.8f).floatValue());


			/*==========执行背景动画==========*/
            getBackground().setColorFilter(argbEvaluator.evaluate(percent, Color.BLACK,Color.TRANSPARENT).intValue(), PorterDuff.Mode.SRC_OVER);


        }



        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);

            Log.i(TAG, "xvel="+xvel+"--->onViewReleased()");

            if(xvel==0 && mMainContent.getLeft()>mRange/2){
                open(true);
            }else if(xvel>0){
                open(true);
            }else{
                close(true);
            }
        }


    };




    //b.传递触摸事件
    @Override
    public boolean onInterceptTouchEvent(android.view.MotionEvent ev) {

        return mDragHelper.shouldInterceptTouchEvent(ev);

    };


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if(getChildCount()<2){
            throw new IllegalStateException("布局至少有两个子布局");
        }

        if(!(getChildAt(0) instanceof ViewGroup && getChildAt(1) instanceof ViewGroup)){
            throw new IllegalArgumentException("子view必须是ViewGroup的子类");
        }

        mLeftContent=(ViewGroup) getChildAt(0);
        mMainContent=(ViewGroup) getChildAt(1);

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mHeight = getMeasuredHeight();
        mWidth = getMeasuredWidth();

        mRange=(int) (mWidth*0.6);
    };

    @Override
    public void computeScroll() {
        super.computeScroll();
        //如果是在computeScroll()方法中,则参数传true
        if(mDragHelper.continueSettling(true)){
            //返回true时,继续执行动画,this代表当前ViewGroup
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * 修正左边距
     * @param left
     * @return
     */
    private int fixLeft(int left) {
        if(left<0){
            left=0;
        }
        if(left>mRange){
            left=mRange;
        }
        return left;
    };

    private void close() {
        close(false);
    }

    private void close(boolean isSmooth) {
        if(isSmooth){
            mDragHelper.smoothSlideViewTo(mMainContent,0,0);
            Log.d(TAG,"--->smoothSlideViewTo()--->close()");
        }else{
            mMainContent.layout(0,0,mWidth,mHeight);
            Log.d(TAG,"--->layout()--->close()");
        }
    }

    private void open() {
        open(false);
    }

    private void open(boolean isSmooth) {
        if(isSmooth){
            if(mDragHelper.smoothSlideViewTo(mMainContent,mRange,0)){
//				ViewCompat.postInvalidateOnAnimation(this);
                Log.d(TAG,"--->smoothSlideViewTo()--->open()");
            }
        }else{
            mMainContent.layout(mRange,0,mRange+mWidth,mHeight);
            Log.d(TAG,"--->layout()--->open()");
        }
    }

}
