package com.partyappfia.utils;

import android.content.Context;
import android.util.AttributeSet;

public class GSAChatLeftPane extends GSAChatPane {

    public GSAChatLeftPane(Context context) {
        super(context);
        setArrowDirection(GSAChatPane.ArrowDirection.eArrowDirection_Left);
    }

    public GSAChatLeftPane(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GSAChatLeftPane(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setArrowDirection(GSAChatPane.ArrowDirection.eArrowDirection_Left);
    }
}