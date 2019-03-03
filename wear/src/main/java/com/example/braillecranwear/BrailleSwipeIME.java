package com.example.braillecranwear;

import android.inputmethodservice.Keyboard;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.example.braillecranwear.GestureDetectors.Swipe8DirectionsDetector;

import java.util.ArrayList;

public class BrailleSwipeIME extends BrailleIME {


    //Flags
    private boolean hasSwiped = false;
    private boolean hasJustLongPressed = false;

    @Override
    public View onCreateInputView() {
        super.onCreateInputView();

        method = "swipe";

        // Swipe Gesture Detection
        gestureDetector = new GestureDetector(this, new Swipe8DirectionsDetector() {
            @Override
            public void onTopLeftSwipe() {
                keyboard.toggleDotVisibility(0);
                addToLog("Swipe towards Button", String.valueOf(0), keyboard.StateDots[0]);
            }

            @Override
            public void onTopRightSwipe() {
                keyboard.toggleDotVisibility(3);
                addToLog("Swipe towards Button", String.valueOf(3), keyboard.StateDots[3]);
            }

            @Override
            public void onMiddleLeftSwipe() {
                keyboard.toggleDotVisibility(1);
                addToLog("Swipe towards Button", String.valueOf(1), keyboard.StateDots[1]);
            }

            @Override
            public void onMiddleRightSwipe() {
                keyboard.toggleDotVisibility(4);
                addToLog("Swipe towards Button", String.valueOf(4), keyboard.StateDots[4]);
            }

            @Override
            public void onBottomLeftSwipe() {
                keyboard.toggleDotVisibility(2);
                addToLog("Swipe towards Button", String.valueOf(2), keyboard.StateDots[2]);
            }

            @Override
            public void onBottomRightSwipe() {
                keyboard.toggleDotVisibility(5);
                addToLog("Swipe towards Button", String.valueOf(5), keyboard.StateDots[5]);
            }

            @Override
            public boolean onDoubleTap(MotionEvent event) {
                if (!hasSwiped){
                    addToLog("Double tap", " - ", true);
                    confirmCharacter();
                }

                return super.onDoubleTap(event);
            }
        });

        setTouchListener();

        for (int i = 0; i < keyboard.ImageDots.length; i++) {
            // TODO: Prevent key buttons to be clickable here
        }

        return keyboardView;
    }

    void setTouchListener() {
        keyboardView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                // Handles Long Press with custom timeout
                handleLongPressDetection(event);

                hasSwiped = event.getActionMasked() == MotionEvent.ACTION_MOVE;
                if (!hasJustLongPressed && !twoFingersSwipeListener.onTouchEvent(event) && !hasJustTwoFingerSwiped) {
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
