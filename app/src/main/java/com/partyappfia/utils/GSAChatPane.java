package com.partyappfia.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.partyappfia.R;

public class GSAChatPane extends LinearLayout {

    public enum ArrowDirection {
        eArrowDirection_Left,
        eArrowDirection_Right
    };
    private ArrowDirection meArrowDirection = ArrowDirection.eArrowDirection_Left;

    private final int ARROW_WIDTH = (int)(5 * getResources().getDisplayMetrics().density);
    private final int ARROW_HEIGHT = (int)(10 * getResources().getDisplayMetrics().density);
    private final int ARROW_TOP = (int)(20 * getResources().getDisplayMetrics().density);
    private float mCornerRadius = (int)(5 * getResources().getDisplayMetrics().density);

    public GSAChatPane(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public GSAChatPane(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GSAChatPane(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);
    }

    public void setArrowDirection(ArrowDirection eArrowDirection) {
        meArrowDirection = eArrowDirection;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        RectF rc = new RectF(0, 0, getWidth() - 1, getHeight() - 1);
        RectF rcPane = new RectF(rc);
        Path path = new Path();

        int nArrowTop = ARROW_TOP;
        if (getHeight() < (int)(48 * getResources().getDisplayMetrics().density))
            nArrowTop = (int)(getHeight() - 10 * getResources().getDisplayMetrics().density) / 2;
        nArrowTop = Math.min((int)rcPane.bottom - ARROW_HEIGHT - (int)mCornerRadius, nArrowTop);

        if (meArrowDirection == ArrowDirection.eArrowDirection_Left) {
            rcPane.left += ARROW_WIDTH;
            path.moveTo(rcPane.left, nArrowTop);
            path.lineTo(rc.left, nArrowTop + ARROW_HEIGHT / 2);
            path.lineTo(rcPane.left, nArrowTop + ARROW_HEIGHT);
        } else {
            rcPane.right -= ARROW_WIDTH;
            path.moveTo(rcPane.right, nArrowTop);
            path.lineTo(rc.right, nArrowTop + ARROW_HEIGHT / 2);
            path.lineTo(rcPane.right, nArrowTop + ARROW_HEIGHT);
        }
        path.close();

        Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Style.FILL);
            paint.setColor(getResources().getColor(R.color.colorOrange));
        canvas.drawRoundRect(rcPane, mCornerRadius, mCornerRadius, paint);
        canvas.drawPath(path, paint);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        // this gets called, but with a canvas sized after the padding.
    }
}