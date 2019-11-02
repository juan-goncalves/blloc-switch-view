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

    private static final long ANIMATION_DURATION = 250L; // Milliseconds
    private float innerCircleRadius = 100.0f;
    private State state = State.OFF;
    private Paint paint = getDefaultPaint();
    private RectF rectangle = new RectF(50, 50, 50 + innerCircleRadius * 2, 50 + innerCircleRadius * 2);

    public BllocSwitchView(Context context) {
        super(context);
    }

    public BllocSwitchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BllocSwitchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawArc(rectangle, 0, 360, true, paint);
        }
    }

    private Paint getDefaultPaint() {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(10);
        return paint;
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

    private ValueAnimator getValueAnimatorToShrinkCircle() {
        // TODO: User animator set to run at the same time the circle transformation animation and the background color change
        ValueAnimator animator = ValueAnimator.ofFloat(rectangle.width(), 0f);
        animator.setDuration(ANIMATION_DURATION);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float updatedWidth = (float) valueAnimator.getAnimatedValue();
                rectangle.right = rectangle.left + updatedWidth;
                invalidate();
            }
        });
        return animator;
    }

    private ValueAnimator getValueAnimatorToExpandCircle() {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, innerCircleRadius * 2);
        animator.setDuration(ANIMATION_DURATION);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float updatedWidth = (float) valueAnimator.getAnimatedValue();
                rectangle.right = rectangle.left + updatedWidth;
                invalidate();
            }
        });
        return animator;
    }

    private enum State {
        ON, OFF
    }

}
