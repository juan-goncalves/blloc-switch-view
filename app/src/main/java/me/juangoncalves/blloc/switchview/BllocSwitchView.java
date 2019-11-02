package me.juangoncalves.blloc.switchview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

public class BllocSwitchView extends View {

    private enum State {
        ON, OFF
    }

    private static final long ANIMATION_DURATION = 250L;
    private static final int ACTUAL_WIDTH = 150;
    private static final int ACTUAL_HEIGHT = 70;
    private static final float PADDING = 21;

    private Paint innerShapePaint = getInnerShapePaint();
    private Paint containerPaint = getContainerPaint();

    private State state = State.OFF;
    private RectF containerRect = new RectF();
    private RectF innerShapeRect = new RectF();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float roundedCornerRadius = containerRect.height() / 2;
            canvas.drawRoundRect(containerRect, roundedCornerRadius, roundedCornerRadius, containerPaint);
            canvas.drawArc(innerShapeRect, 0, 360, true, innerShapePaint);
        }
    }

    public BllocSwitchView(Context context) {
        super(context);
    }

    public BllocSwitchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BllocSwitchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private ValueAnimator getValueAnimatorToShrinkCircle() {
        // TODO: User animator set to run at the same time the circle transformation animation and the background color change
        ValueAnimator shrinkValueAnimator = ValueAnimator.ofFloat(innerShapeRect.width(), 0f);
        shrinkValueAnimator.setDuration(ANIMATION_DURATION);
        shrinkValueAnimator.setInterpolator(new LinearInterpolator());
        shrinkValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float updatedWidth = (float) valueAnimator.getAnimatedValue();
                innerShapeRect.right = innerShapeRect.left + updatedWidth;
                invalidate();
            }
        });
        return shrinkValueAnimator;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            ValueAnimator animator;
            if (state == State.OFF) {
                animator = getValueAnimatorToShrinkCircle();
                state = State.ON;
            } else {
                animator = getValueAnimatorToExpandCircle();
                state = State.OFF;
            }
            animator.start();
        }
        return true;
    }

    private ValueAnimator getValueAnimatorToExpandCircle() {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, innerShapeRect.height());
        animator.setDuration(ANIMATION_DURATION);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float updatedWidth = (float) valueAnimator.getAnimatedValue();
                innerShapeRect.right = innerShapeRect.left + updatedWidth;
                invalidate();
            }
        });
        return animator;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        switch (widthMode) {
            case MeasureSpec.EXACTLY:
                width = widthSize;
                break;
            case MeasureSpec.AT_MOST:
                width = Math.min(ACTUAL_WIDTH, widthSize);
                break;
            default:
                width = ACTUAL_WIDTH;
                break;
        }

        int height;
        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                height = heightSize;
                break;
            case MeasureSpec.AT_MOST:
                height = Math.min(ACTUAL_HEIGHT, heightSize);
                break;
            default:
                height = ACTUAL_HEIGHT;
                break;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        containerRect.right = w;
        containerRect.left = w - ACTUAL_WIDTH;
        int verticalCenter = h / 2;
        containerRect.top = verticalCenter - ACTUAL_HEIGHT / 2;
        containerRect.bottom = verticalCenter + ACTUAL_HEIGHT / 2;
        innerShapeRect.top = containerRect.top + PADDING;
        innerShapeRect.bottom = containerRect.bottom - PADDING;
        innerShapeRect.left = containerRect.left + PADDING;
        innerShapeRect.right = innerShapeRect.left + innerShapeRect.height();
    }

    private Paint getInnerShapePaint() {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(4);
        return paint;
    }

    private Paint getContainerPaint() {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(getResources().getColor(R.color.switch_view_background));
        return paint;
    }

}
