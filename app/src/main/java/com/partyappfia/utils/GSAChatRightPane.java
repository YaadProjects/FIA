package com.partyappfia.utils;

import android.content.Context;
import android.util.AttributeSet;

public class GSAChatRightPane extends GSAChatPane {

    public GSAChatRightPane(Context context) {
        super(context);
        setArrowDirection(ArrowDirection.eArrowDirection_Right);
    }

    public GSAChatRightPane(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GSAChatRightPane(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setArrowDirection(ArrowDirection.eArrowDirection_Right);
    }
}