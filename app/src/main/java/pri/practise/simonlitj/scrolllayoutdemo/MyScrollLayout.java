package pri.practise.simonlitj.scrolllayoutdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

public class MyScrollLayout extends ViewGroup {
    private static final int SNAP_VELOCITY=200;
    private Scroller mScroller;
    private int touchSlop;
    private static final String TAG=MyScrollLayout.class.getSimpleName();
    private static final int TOUCH_STATE_STOP=0x001;
    private static final int TOUCH_STATE_FLING=0x002;
    private int touchState=TOUCH_STATE_STOP;
    private float lastionMotionY=0;
//    private float lastionMotionY=0;
    private int curScreen;

    private int topBorder;
    private int bottomBorder;
    private VelocityTracker mVelocityTracker;

    public MyScrollLayout(Context context) {
        super(context);
        mScroller=new Scroller(context);
        touchSlop= ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public MyScrollLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller=new Scroller(context);
        touchSlop= ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public MyScrollLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller=new Scroller(context);
        touchSlop= ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public MyScrollLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mScroller=new Scroller(context);
        touchSlop= ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec,heightMeasureSpec);
        int width=measureWidth(widthMeasureSpec);
        int height=measureHeight(heightMeasureSpec);
        setMeasuredDimension(width,height);
    }

    private int measureHeight(int heightMeasureSpec) {
        int mode=MeasureSpec.getMode(heightMeasureSpec);
        int size=MeasureSpec.getSize(heightMeasureSpec);

        int height=0;
        if (mode==MeasureSpec.AT_MOST){
            throw new IllegalStateException("Must not be" +
                    " MeasureSpec.AT_MOST.");
        }else {
            height=size;
        }
        return height*this.getChildCount();
    }

    private int measureWidth(int widthMeasureSpec) {
        int mode=MeasureSpec.getMode(widthMeasureSpec);
        int size=MeasureSpec.getSize(widthMeasureSpec);

        int width=0;
        if(mode==MeasureSpec.AT_MOST){
            throw new IllegalStateException("Must not be" +
                    " MeasureSpec.AT_MOST.");
        }else {
            width=size;
        }
        return width;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int n=this.getChildCount();
        int w=(r-l);
        int h=(b-t)/n;

        for (int i = 0; i <n ; i++) {
            View child=getChildAt(i);
            int left=0;
            int right=w;
            int top=i*h;
            int bottom=(i+1)*h;

            child.layout(left,top,right,bottom);
        }
        topBorder = getChildAt(0).getTop();
        bottomBorder = getChildAt(getChildCount() - 1).getBottom();
    }

    public void moveToScreen(int whichScreen){
        curScreen=whichScreen;
//        if(curScreen>getChildCount()-1){
//            curScreen=getChildCount()-1;
//        }
//        if(curScreen<0){
//            curScreen=0;
//        }
        int scrollY=getScrollY();
        int splitHeight=getHeight()/getChildCount();    //每一屏的宽度
        int dy=curScreen*splitHeight-scrollY;        //要移动的距离
        mScroller.startScroll(0,scrollY,0,dy,Math.abs(dy));//开始移动
        invalidate();
    }

    public void moveToDestination(){
        int splitHeight=getHeight()/getChildCount();    //每一屏的宽度
        int toScreen=(getScrollY()+splitHeight/2)/splitHeight;//判断是回滚还是进入下一屏
        moveToScreen(toScreen);            //移动到目标分屏
    }

    public void moveToNext(){
        moveToScreen(curScreen+1);
    }

    public void moveToPrevious(){
        moveToScreen(curScreen-1);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action=ev.getAction();
        final int y=(int)ev.getY();
        if(action==MotionEvent.ACTION_MOVE&&
                touchState==TOUCH_STATE_STOP){
            return true;
        }
        switch (action){
            case MotionEvent.ACTION_DOWN:
                lastionMotionY=y;
                touchState=mScroller.isFinished()?TOUCH_STATE_STOP
                        :TOUCH_STATE_FLING;
                break;
            case MotionEvent.ACTION_MOVE:
                //滑动距离过小不算滑动
                final int dy=(int)Math.abs(y-lastionMotionY);
                if (dy>touchSlop){
                    touchState=TOUCH_STATE_FLING;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                touchState=TOUCH_STATE_STOP;
                break;
            default:
                break;
        }
        return touchState!=TOUCH_STATE_STOP;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker=mVelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        super.onTouchEvent(event);

        int action=event.getAction();
        final int y=(int)event.getY();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                //手指按下时，如果正在滚动，则立即停止
                if (mScroller != null && !mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                lastionMotionY=y;
                break;
            case MotionEvent.ACTION_MOVE:
                //随手指滑动
                int dy=(int)(lastionMotionY-y);
                if (getScrollY()+dy <= topBorder){
//                    scrollTo(0,topBorder);
                    return false;
                }else if ((getScrollY() + getHeight()+dy)>=bottomBorder){
//                    scrollTo(0,bottomBorder-getHeight());
                    return false;
                }else {
                    scrollBy(0,dy);
                    lastionMotionY=y;
                }

                break;
            case MotionEvent.ACTION_UP:
                final VelocityTracker mVelocityTracker=this.mVelocityTracker;
                mVelocityTracker.computeCurrentVelocity(1000);
                int velocityY=(int)mVelocityTracker.getYVelocity();
                //通过velocityY的正负值可以判断滑动方向
                if(velocityY>SNAP_VELOCITY&&curScreen>0){
                    moveToPrevious();
                }else if (velocityY<-SNAP_VELOCITY&&curScreen<(getChildCount()-1)){
                    moveToNext();
                }else {
                    moveToDestination();
                }
                if (mVelocityTracker != null) {
                    this.mVelocityTracker.clear();
                    this.mVelocityTracker.recycle();
                    this.mVelocityTracker=null;
                }
                touchState=TOUCH_STATE_STOP;
                break;
            case MotionEvent.ACTION_CANCEL:
                touchState=TOUCH_STATE_STOP;
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()){
            this.scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
            postInvalidate();
        }
    }
}
