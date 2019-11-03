package me.juangoncalves.blloc.switchview;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.core.math.MathUtils;

import static android.view.MotionEvent.INVALID_POINTER_ID;

public class BllocSwitchView extends View {

    private static final long ANIMATION_DURATION = 330L;
    private static final long MAX_CLICK_DURATION = 200L;
    private static final int ACTUAL_WIDTH = 140;
    private static final int ACTUAL_HEIGHT = 70;
    private static final float MIN_INNER_SHAPE_WIDTH = 1f;
    private static final float PADDING = 21;
    private static final int MIN_OPACITY = 80;

    @ColorInt
    private int containerColor;
    private boolean checked;

    private RectF containerRect = new RectF();
    private RectF innerShapeRect = new RectF();
    private Paint innerShapePaint = new Paint();
    private Paint containerPaint = new Paint();

    // The ‘active pointer’ is the one currently moving our object.
    private AnimatorSet currentAnimation = null;
    private int activePointerId = INVALID_POINTER_ID;
    private float lastTouchX;
    private long clickStartTime = 0;

    public BllocSwitchView(Context context) {
        super(context);
        setSaveEnabled(true);
    }

    public BllocSwitchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.BllocSwitchView);
        checked = ta.getBoolean(R.styleable.BllocSwitchView_sv_checked, true);
        containerColor = ta.getColor(
                R.styleable.BllocSwitchView_sv_backgroundColor,
                getResources().getColor(R.color.switch_view_background_on)
        );
        ta.recycle();
        updateInnerShapePaint();
    }

    public BllocSwitchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        if (this.checked != checked) {
            toggle();
        }
    }

    public void toggle() {
        if (checked) {
            currentAnimation = shrinkAndMoveToEnd();
        } else {
            currentAnimation = expandAndMoveToStart();
        }
        currentAnimation.start();
        checked = !checked;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float roundedCornerRadius = containerRect.height() / 2;
        canvas.drawRoundRect(containerRect, roundedCornerRadius, roundedCornerRadius, containerPaint);
        canvas.drawArc(innerShapeRect, 0, 360, true, innerShapePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                int pointerIndex = ev.getActionIndex();
                // Track the time to determine if it's a simple click or a drag action
                clickStartTime = System.currentTimeMillis();
                // Remember where we started (for dragging)
                lastTouchX = ev.getX(pointerIndex);
                // Save the ID of this pointer (for dragging)
                activePointerId = ev.getPointerId(0);
                // As we are either toggling the button or starting a drag action, the current
                // animation should be cancelled
                if (currentAnimation != null) {
                    currentAnimation.cancel();
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                int pointerIndex = ev.findPointerIndex(activePointerId);
                float x = ev.getX(pointerIndex);
                float xDiff = x - lastTouchX;
                float nextLeftPos = MathUtils.clamp(
                        innerShapeRect.left + xDiff,
                        getMinLeftCoordinateForInnerShape(),
                        getMaxRightCoordinateForInnerShape()
                );
                innerShapeRect.left = nextLeftPos;
                innerShapeRect.right = nextLeftPos + calculateInnerShapeWidthForPosition(nextLeftPos);
                containerPaint.setColor(calculateContainerColorOpacityForPosition(nextLeftPos));
                invalidate();
                // Remember this touch position for the next move event
                lastTouchX = x;
                break;
            }

            case MotionEvent.ACTION_UP: {
                activePointerId = INVALID_POINTER_ID;
                long actionDuration = System.currentTimeMillis() - clickStartTime;
                if (actionDuration <= MAX_CLICK_DURATION) {
                    performClick();
                } else {
                    float lastX = ev.getX();
                    float containerCenter = containerRect.centerX();
                    if (lastX <= containerCenter) {
                        checked = true;
                        currentAnimation = expandAndMoveToStart();
                    } else {
                        checked = false;
                        currentAnimation = shrinkAndMoveToEnd();
                    }
                    currentAnimation.start();
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                activePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                int pointerIndex = ev.getActionIndex();
                int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == activePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    lastTouchX = ev.getX(newPointerIndex);
                    activePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }
        return true;
    }

    private float calculateInnerShapeWidthForPosition(float leftPoint) {
        // Modelled as a line (y = mx + b) with the vertical axis being the left coordinate of the
        // inner shape rectangle and the horizontal axis as the expected width of the inner shape
        // for that position, such that for Y = rightLimit, the shape's width equals MIN_INNER_SHAPE_WIDTH,
        // and for Y = leftLimit, the diameter of the full circle.
        float leftLimit = getMinLeftCoordinateForInnerShape();
        float rightLimit = getMaxRightCoordinateForInnerShape();
        float maxInnerWidth = getFullInnerCircleDiameter();
        float slope = (leftLimit - rightLimit) / (maxInnerWidth - MIN_INNER_SHAPE_WIDTH);
        float yAxisIntersection = -1 * MIN_INNER_SHAPE_WIDTH * slope + rightLimit;
        float result = (leftPoint - yAxisIntersection) / slope;
        return MathUtils.clamp(result, MIN_INNER_SHAPE_WIDTH, maxInnerWidth);
    }

    private int calculateContainerColorOpacityForPosition(float leftPoint) {
        // Modelled as a line (y = mx + b) with the vertical axis being the left coordinate of the
        // inner shape rectangle and the horizontal axis as the expected color opacity of the
        // container's background for that position, such that for Y = rightLimit, it returns the
        // minimum opacity, and for Y = leftLimit, full opacity.
        int fullOpacity = Color.alpha(containerColor);
        float leftLimit = getMinLeftCoordinateForInnerShape();
        float rightLimit = getMaxRightCoordinateForInnerShape();
        float slope = (leftLimit - rightLimit) / (fullOpacity - MIN_OPACITY);
        float yAxisIntersection = -1 * MIN_OPACITY * slope + rightLimit;
        float result = (leftPoint - yAxisIntersection) / slope;
        return ColorUtils.setAlphaComponent(containerColor, (int) result);
    }

    @Override
    public boolean performClick() {
        toggle();
        return super.performClick();
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
            case MeasureSpec.UNSPECIFIED:
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
            case MeasureSpec.UNSPECIFIED:
            default:
                height = ACTUAL_HEIGHT;
                break;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        containerRect.right = width;
        containerRect.left = width - ACTUAL_WIDTH;
        int verticalCenter = height / 2;
        containerRect.top = verticalCenter - ACTUAL_HEIGHT / 2;
        containerRect.bottom = verticalCenter + ACTUAL_HEIGHT / 2;
        innerShapeRect.top = containerRect.top + PADDING;
        innerShapeRect.bottom = containerRect.bottom - PADDING;
        // Decide depending on the switch status whether to draw the full circle (ON) or the straight line (OFF)
        if (isChecked()) {
            innerShapeRect.left = getMinLeftCoordinateForInnerShape();
            innerShapeRect.right = innerShapeRect.left + getFullInnerCircleDiameter();
        } else {
            innerShapeRect.right = getMaxRightCoordinateForInnerShape();
            innerShapeRect.left = innerShapeRect.right - MIN_INNER_SHAPE_WIDTH;
        }
        updateContainerPaint();
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState customState = new SavedState(superState);
        customState.isChecked = checked;
        return customState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        SavedState customState = (SavedState) state;
        setChecked(customState.isChecked);
    }

    private float getMaxRightCoordinateForInnerShape() {
        return containerRect.right - PADDING - getFullInnerCircleDiameter() / 2;
    }

    private float getFullInnerCircleDiameter() {
        // As we never modify the height of the inner shape rect, we can always use its height
        // as the diameter of the circle when it is completely expanded.
        return innerShapeRect.height();
    }

    private float getMinLeftCoordinateForInnerShape() {
        return containerRect.left + PADDING;
    }

    private void updateInnerShapePaint() {
        innerShapePaint.setStyle(Paint.Style.STROKE);
        innerShapePaint.setColor(Color.WHITE);
        innerShapePaint.setStrokeWidth(4);
    }

    private void updateContainerPaint() {
        containerPaint.setStyle(Paint.Style.FILL);
        containerPaint.setColor(calculateContainerColorOpacityForPosition(innerShapeRect.left));
    }

    private AnimatorSet expandAndMoveToStart() {
        ValueAnimator shapeAnimator = ValueAnimator.ofFloat(innerShapeRect.width(), getFullInnerCircleDiameter());
        shapeAnimator.setDuration(ANIMATION_DURATION);
        shapeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        shapeAnimator.addUpdateListener(new InnerShapeWidthUpdateListener());
        ValueAnimator positionAnimator = ValueAnimator.ofFloat(innerShapeRect.left, getMinLeftCoordinateForInnerShape());
        positionAnimator.setDuration(ANIMATION_DURATION);
        positionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        positionAnimator.addUpdateListener(new InnerShapePositionUpdateListener());
        ValueAnimator alphaAnimator = ValueAnimator.ofInt(Color.alpha(containerPaint.getColor()), Color.alpha(containerColor));
        alphaAnimator.setDuration(ANIMATION_DURATION);
        alphaAnimator.addUpdateListener(new BackgroundColorUpdateListener());
        AnimatorSet set = new AnimatorSet();
        set.playTogether(shapeAnimator, positionAnimator, alphaAnimator);
        return set;
    }

    private AnimatorSet shrinkAndMoveToEnd() {
        ValueAnimator shrinkValueAnimator = ValueAnimator.ofFloat(innerShapeRect.width(), MIN_INNER_SHAPE_WIDTH);
        shrinkValueAnimator.setDuration(ANIMATION_DURATION);
        shrinkValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        shrinkValueAnimator.addUpdateListener(new InnerShapeWidthUpdateListener());
        ValueAnimator positionAnimator = ValueAnimator.ofFloat(innerShapeRect.left, getMaxRightCoordinateForInnerShape());
        positionAnimator.setDuration(ANIMATION_DURATION);
        positionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        positionAnimator.addUpdateListener(new InnerShapePositionUpdateListener());
        ValueAnimator alphaAnimator = ValueAnimator.ofInt(Color.alpha(containerPaint.getColor()), MIN_OPACITY);
        alphaAnimator.setDuration(ANIMATION_DURATION);
        alphaAnimator.addUpdateListener(new BackgroundColorUpdateListener());
        AnimatorSet set = new AnimatorSet();
        set.playTogether(shrinkValueAnimator, positionAnimator, alphaAnimator);
        return set;
    }

    private class BackgroundColorUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            int updatedOpacity = (int) valueAnimator.getAnimatedValue();
            int currentColor = containerPaint.getColor();
            containerPaint.setColor(ColorUtils.setAlphaComponent(currentColor, updatedOpacity));
            invalidate();
        }
    }

    private class InnerShapeWidthUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            float updatedWidth = (float) valueAnimator.getAnimatedValue();
            innerShapeRect.right = innerShapeRect.left + updatedWidth;
            invalidate();
        }
    }

    private class InnerShapePositionUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            float updatedPosition = (float) valueAnimator.getAnimatedValue();
            float width = innerShapeRect.width();
            innerShapeRect.left = updatedPosition;
            innerShapeRect.right = updatedPosition + width;
            invalidate();
        }
    }

    private static class SavedState extends BaseSavedState {
        boolean isChecked;
        private static final int TRUE = 1;
        private static final int FALSE = 0;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            isChecked = in.readInt() == TRUE;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(isChekedAsInt());
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        private int isChekedAsInt() {
            if (isChecked)
                return TRUE;
            else
                return FALSE;
        }

    }

}
