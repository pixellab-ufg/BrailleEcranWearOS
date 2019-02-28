package com.example.braillecranwear;

import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.example.braillecranwear.GestureDetectors.Swipe4DirectionsDetector;


public class BrailleTouchIME extends BrailleIME {

    public String method = "touch";

    @Override
    public View onCreateInputView() {
        super.onCreateInputView();

        // Set up touch gesture
        setTouchListener();

        // Swipe Gesture Detection
        gestureDetector = new GestureDetector(this, new Swipe4DirectionsDetector() {

            @Override
            public void onTopSwipe() {
            }
            @Override
            public void onLeftSwipe() {
            }
            @Override
            public void onRightSwipe() {
            }
            @Override
            public void onBottomSwipe() {
            }
            @Override
            public boolean onDoubleTap(MotionEvent event) {
                for (int i = 0; i < keyboard.ImageDots.length; i++) {
                    if (keyboard.ImageDots[i].isInside((int) event.getX(), (int) event.getY())) {
                        keyboard.toggleDotVisibility(i);
                        return super.onDoubleTapEvent(event);
                    }
                }
                confirmCharacter();
                return super.onDoubleTap(event);
            }
        });

        return keyboardView;
    }

    void setTouchListener() {

        keyboardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                // Handles Long Press with custom timeout
                handleLongPressDetection(event);

                if (!hasJustLongPressed && !twoFingersSwipeListener.onTouchEvent(event) && !hasJustTwoFingerSwiped) {

                    boolean hasSimpleClicked = false;
                    if (event.getActionMasked() == MotionEvent.ACTION_UP) {

                        for (int i = 0; i < keyboard.ImageDots.length; i++) {
                            if (keyboard.ImageDots[i].isInside((int) event.getX(), (int) event.getY())) {
                                keyboard.toggleDotVisibility(i);
                                hasSimpleClicked = true;
                                break;
                            }
                        }
                    }

                    if (!hasSimpleClicked)
                        gestureDetector.onTouchEvent(event);

                } else {
                    hasJustTwoFingerSwiped = true;
                    new Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    hasJustTwoFingerSwiped = false;
                                }
                            }, 200);
                }
                return false;

            }
        });
    }
}
