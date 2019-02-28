package com.example.braillecranwear;

import android.content.Context;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.view.View;

class BrailleKeyboardView extends KeyboardView {


    public BrailleKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BrailleKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BrailleKeyboardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
