package com.example.braillecranwear;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

public class BrailleCandidatesView extends ScrollView {

    BrailleIME brailleIMEService;

    private boolean enableScrolling = false;

    public BrailleCandidatesView(Context context) {
        super(context);

        Log.d("CANDIDATES","EU EXISTO1");

//        setBackgroundColor(Color.BLUE);
//        setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
//        setMinimumHeight(200);
//        setMinimumWidth(200);
    }

    public boolean isEnableScrolling() {
        return enableScrolling;
    }

    public void setEnableScrolling(boolean enableScrolling) {
        this.enableScrolling = enableScrolling;
    }

    public void setService(BrailleIME listener) {
        brailleIMEService = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isEnableScrolling()) {
            return super.onInterceptTouchEvent(ev);
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isEnableScrolling()) {
            return super.onTouchEvent(ev);
        } else {
            return false;
        }
    }
}
