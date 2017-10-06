package cn.shawn.switcher;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;



/**
 * Created by Shawn on 2017/10/3.
 */

public class Switcher extends View {

    public static final String TAG = "switcher";

    public static final int DEFAULT_CIRCLE_RADIUS = 50;

    public static final int DEFAULT_RECT_WIDTH = 80;

    public static final int DEFAULT_CIRCLE_MARGIN = 10;

    private int mTransitionMargin = DEFAULT_CIRCLE_MARGIN;

    private float mRectWidth = DEFAULT_RECT_WIDTH;

    private float mCircleRadius = DEFAULT_CIRCLE_RADIUS;

    private static final String DEFAULT_TRANSITION_COLOR = "#62b900";

    private int mLastX, mLastY;

    private boolean movedOperate;

    private Paint mBgPaint;

    private Paint mTransitionPaint;

    private Paint mCirclePaint;

    private int mTransitionColor;

    private float mMovedX ;

    private RectF mBgRect;

    public Switcher(Context context) {
        this(context, null);
    }

    public Switcher(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Switcher(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // close hardware accelerate
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.Switcher);
        mTransitionColor = array.getColor(R.styleable.Switcher_transitionColor, Color.parseColor(DEFAULT_TRANSITION_COLOR));
        mTransitionMargin = array.getDimensionPixelSize(R.styleable.Switcher_indicatorMargin,DEFAULT_CIRCLE_MARGIN);
        array.recycle();
        initPaint();
    }

    private void initPaint() {
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(Color.WHITE);
        // for background
        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setColor(Color.LTGRAY);
        mBgPaint.setAlpha(0xaf);
        // for transition
        mTransitionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTransitionPaint.setColor(mTransitionColor);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = (int) event.getRawX();
                mLastY = (int) event.getRawY();
                if (isChecked() && event.getX() < mCircleRadius) {
                    movedOperate = false;
                } else if (!isChecked() && event.getX() > mCircleRadius + mRectWidth) {
                    movedOperate = false;
                } else movedOperate = true;
                return true;
            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) (mLastX - event.getRawX());
                int deltaY = (int) (mLastY - event.getRawY());
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    movedOperate = true;
                    modifyBoundary(deltaX);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!movedOperate) toggle();
                else modifyPosition();
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    public boolean isChecked() {
        return mMovedX == mRectWidth;
    }

    public void check() {
        ValueAnimator animator = ValueAnimator.ofFloat(0f,  mRectWidth);
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mMovedX = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        animator.start();
    }

    public void close() {
        ValueAnimator animator = ValueAnimator.ofFloat(mRectWidth, 0f);
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mMovedX =(float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        animator.start();
    }

    public void toggle() {
        if (isChecked()) close();
        else check();
    }

    private void modifyPosition() {
        ValueAnimator animator;
        if (mMovedX < mRectWidth / 2) {
            animator = ValueAnimator.ofFloat(mMovedX, 0);
        } else {
            animator = ValueAnimator.ofFloat(mMovedX, mRectWidth);
        }
        animator.setDuration(100);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mMovedX = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();
    }

    private void modifyBoundary(int deltaX) {
        mMovedX -= deltaX * 0.125f;
        if (mMovedX < 0) mMovedX = 0;
        else if (mMovedX > mRectWidth) mMovedX = (int) mRectWidth;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float width = getMeasureSize(widthMeasureSpec, true);
        float height = getMeasureSize(heightMeasureSpec, false);
        mCircleRadius = (height / 2);
        // 精度
        mRectWidth =  (width /  9 * 4 -20);
        setMeasuredDimension((int)width, (int)height);
        mBgRect = new RectF(0f, 0f,  2 * mCircleRadius + mRectWidth,  2 * mCircleRadius);
    }

    private int getMeasureSize(int spec, boolean width) {
        int desireSize;
        int size = MeasureSpec.getSize(spec);
        int mode = MeasureSpec.getMode(spec);
        if (mode == MeasureSpec.EXACTLY) {
            desireSize = size;
        } else {
            desireSize = width ? DEFAULT_RECT_WIDTH + 2 * DEFAULT_CIRCLE_RADIUS : 2 * DEFAULT_CIRCLE_RADIUS;
        }
        return desireSize;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //draw bg
        canvas.drawRoundRect(mBgRect, mCircleRadius, mCircleRadius, mBgPaint);
        mTransitionPaint.setAlpha((int) ((mMovedX /  mRectWidth) * 0xff));
        canvas.drawRoundRect(mBgRect, mCircleRadius, mCircleRadius, mTransitionPaint);
        //draw indicator
        mCirclePaint.setShadowLayer(8f,0,0, Color.WHITE);
        canvas.drawCircle(mCircleRadius + mMovedX, mCircleRadius,
                mCircleRadius - mTransitionMargin, mCirclePaint);
    }

}
