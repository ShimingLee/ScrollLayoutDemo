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
    private static final int SNAP_VELOCITY=300;
    private Scroller mScroller;
    private int touchSlop;
    private static final String TAG=MyScrollLayout.class.getSimpleName();
    private static final int TOUCH_STATE_STOP=0x001;
    private static final int TOUCH_STATE_FLING=0x002;
    private int touchState=TOUCH_STATE_STOP;
//    private float lastionMotionX=0;
    private float lastionMotionY=0;
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
//        MeasureSpec——测量规格
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
//        统计layout个数，并排列进入layout

        int n=this.getChildCount();
        int w=r-l;
        int h=(b-t)/n;

        for (int i = 0; i <n ; i++) {
            View child=getChildAt(i);
            int left=0;
            int right=w;
            int top=i*h;
            int bottom=(i+1)*h;

            child.layout(left,top,right,bottom);
        }
        // 初始化左右边界值
        topBorder = getChildAt(0).getTop();
        bottomBorder = getChildAt(getChildCount() - 1).getBottom();
    }

    public void moveToScreen(int whichScreen){
        Log.i(TAG, "moveToScreen: curScreen: "+curScreen);
        curScreen=whichScreen;
        if(curScreen>getChildCount()-1){
            curScreen=getChildCount()-1;
        }else if(curScreen<0) {
            curScreen = 0;
        }
        int scrollY=getScrollY();
        Log.i(TAG, "moveToScreen: getHeight: "+getHeight());
        Log.i(TAG, "moveToScreen: scrollY: "+scrollY);
        int splitHeight=getHeight()/getChildCount();
        int dy=curScreen*splitHeight-scrollY;
        Log.i(TAG, "moveToScreen: dy: "+dy);
//        if (scrollY<0||scrollY>getHeight()){
////            mScroller.startScroll(0,0,0,0,Math.abs(dy));
//        }else{
            mScroller.startScroll(0,scrollY,0,dy,Math.abs(dy));
            invalidate();
//        }
//        if (dy<0){
//            invalidate();
//        }else {

//        }

//        int scrollX=getScrollX();
//        int splitWidth=getWidth()/getChildCount();    //每一屏的宽度
//        int dx=curScreen*splitWidth-scrollX;        //要移动的距离
//        mScroller.startScroll(scrollX,0,dx,0,Math.abs(dx));//开始移动

    }

    public void moveToDestination(){
        int splitHeight=getHeight()/getChildCount();
        Log.i(TAG, "moveToDestination: splitHeight: "+splitHeight);
        Log.i(TAG, "moveToDestination: getScrollY: "+getScrollY());
        int toScreen=(getScrollY()+splitHeight/2)/splitHeight;
        Log.i(TAG, "moveToDestination: toScreen: "+toScreen);
//        int splitWidth=getWidth()/getChildCount();    //每一屏的宽度
//        int toScreen=(getScrollX()+splitWidth/2)/splitWidth;//判断是回滚还是进入下一屏
        moveToScreen(toScreen);            //移动到目标分屏
    }

    public void moveToNext(){
        Log.i(TAG, "moveToNext: curScreen: "+curScreen);
        moveToScreen(curScreen+1);
    }

    public void moveToPrevious(){
        Log.i(TAG, "moveToPrevious: curScreen: "+curScreen);
        moveToScreen(curScreen-1);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action=ev.getAction();
        final int y=(int)ev.getY();
//        final int x=(int)ev.getX();
        if(action==MotionEvent.ACTION_MOVE&&
                touchState==TOUCH_STATE_STOP){
            return true;
        }
        switch (action){
            case MotionEvent.ACTION_DOWN:
                lastionMotionY=y;
//                lastionMotionX=x;
                touchState=mScroller.isFinished()?TOUCH_STATE_STOP
                        :TOUCH_STATE_FLING;
                break;
            case MotionEvent.ACTION_MOVE:
                //滑动距离过小不算滑动
                final int dy=(int)Math.abs(y-lastionMotionY);
//                final int dx=(int)Math.abs(x-lastionMotionX);
//                if (dx>touchSlop){
//                    touchState=TOUCH_STATE_FLING;
//                }
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
//        获得位置追踪器
        if (mVelocityTracker == null) {
            mVelocityTracker=VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        super.onTouchEvent(event);

        int action=event.getAction();
        final int y=(int)event.getY();
//        final int x=(int)event.getX();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                //手指按下时，如果正在滚动，则立即停止
                if (mScroller != null && !mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
//                上次触摸的位置
                lastionMotionY=y;
//                lastionMotionX=x;
                break;
            case MotionEvent.ACTION_MOVE:
                //随手指滑动
                int dy=(int)(lastionMotionY-y);
                Log.i(TAG, "onTouchEvent: dy: "+dy);
//                Log.i(TAG, "onTouchEvent: getBottom: "+getBottom());
//                if(getScrollY()>getHeight())
                if (getScrollY()+dy <= topBorder){
//                    scrollTo(0,topBorder);
                    return true;
                }else if ((getScrollY() + getHeight()+dy)>=bottomBorder){
//                    scrollTo(0,bottomBorder-getHeight());
                    return true;
                }else {
                    scrollBy(0,dy);
                }
//                覆盖上次触摸的位置
                lastionMotionY=y;
//                int dx=(int)(lastionMotionX-x);
//                scrollBy(dx,0);
//                lastionMotionX=x;
                break;
            case MotionEvent.ACTION_UP:
//                当手指抬起时
                final VelocityTracker velocityTracker=this.mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000);
                int velocityY=(int)velocityTracker.getYVelocity();
                Log.i(TAG, "onTouchEvent: velocityY: "+velocityY);
//                int velocityX=(int)velocityTracker.getXVelocity();
                //通过velocityY的正负值可以判断滑动方向
                Log.i(TAG, "onTouchEvent: curScreen: "+curScreen);
                if(velocityY>SNAP_VELOCITY&&curScreen>0){
                    moveToPrevious();
                }else if (velocityY<-SNAP_VELOCITY&&curScreen<(getChildCount()-1)) {
                    moveToNext();
                }else {
                    moveToDestination();
                }
                if (velocityTracker != null) {
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
