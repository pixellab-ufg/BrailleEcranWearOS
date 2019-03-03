package com.example.braillecranwear;

import android.inputmethodservice.Keyboard;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.example.braillecranwear.GestureDetectors.Swipe4DirectionsDetector;

import java.util.ArrayList;

public class BrailleConnectIME extends BrailleIME {

    //Flags
    private boolean checkOutput = false;
    private boolean isComposingLetter = false;

    @Override
    public View onCreateInputView() {
        super.onCreateInputView();

        method = "connect";

        // Set up touch gesture
        setTouchListener();

        // Double Tap listener, used for inserting space
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

                        addToLog("Double tap inside button", String.valueOf(i), true);
                        return super.onDoubleTapEvent(event);
                    }
                }


                addToLog("Double tap inside middle region", " - ", true);
                keyboard.toggleAllDotsOff();
                confirmCharacter();
                checkOutput = false;
                isComposingLetter = false;

                return super.onDoubleTap(event);
            }
        });

        return keyboardView;
    }


    void setTouchListener() {
        keyboardView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                // Handles Long Press with custom timeout
                handleLongPressDetection(event);

//                Log.d("POINTER COUNT", String.valueOf(event.getPointerCount()));

                hasJustTwoFingerSwiped = twoFingersSwipeListener.onTouchEvent(event);

                if (event.getPointerCount() <= 1 &&
                    event.getAction() != MotionEvent.ACTION_POINTER_DOWN &&
                    event.getAction() != MotionEvent.ACTION_POINTER_UP &&
                    !hasJustLongPressed &&
                    !hasJustTwoFingerSwiped) {

                    gestureDetector.onTouchEvent(event);

                    float x = event.getX();
                    float y = event.getY();
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            vibrator.vibrate(40);
                            isComposingLetter = true;
                            touch_start(x, y);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            isComposingLetter = true;

                            int vibrationTime = getDistanceRelativeVibration(v, event.getX(), event.getY());
                            if (vibrationTime > 0)
                                vibrator.vibrate(vibrationTime);

                            keyboardView.getWidth();
                            touch_move(x, y);
                            break;
                        case MotionEvent.ACTION_UP:
                            if (isComposingLetter)
                                touch_up(false);
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            if (isComposingLetter)
                                touch_up(false);
                            break;
                    }
                    return true;
                } else {
                    if (hasJustTwoFingerSwiped) {
                        isComposingLetter = false;
                        keyboard.toggleAllDotsOff();
                        keyboardView.invalidateAllKeys();
                        checkOutput = false;

                        new Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        isComposingLetter = false;
                                        keyboard.toggleAllDotsOff();
                                        keyboardView.invalidateAllKeys();
                                        checkOutput = false;
                                        hasJustTwoFingerSwiped = false;

                                        addToLog("Disabled dots due to two finger swipe", String.valueOf(0), keyboard.StateDots[0]);
                                    }
                                }, 200);
                    }
                }
                return false;
            }
        });
    }


    private void touch_start(float xf, float yf) {

        addToLog("Touch start", String.valueOf(0), keyboard.StateDots[0]);
        touch_move(xf, yf);
    }
    private void touch_move(float xf, float yf) {

        checkOutput = false;
        int x = (int) xf;
        int y = (int) yf;

        if (keyboard.ImageDots[0].isInside(x, y)) {
            //Log.d("BUTTON 0", "ENTER REGION!");
            if (!keyboard.StateDots[0]) {
                keyboard.toggleDotVisibility(0);
                addToLog("Passed over Button", String.valueOf(0), keyboard.StateDots[0]);
            }
            isComposingLetter = true;
        } else if (keyboard.ImageDots[1].isInside(x, y)) {
            //Log.d("BUTTON 1", "ENTER REGION!");
            if (!keyboard.StateDots[1]) {
                keyboard.toggleDotVisibility(1);
                addToLog("Passed over Button", String.valueOf(1), keyboard.StateDots[1]);
            }
            isComposingLetter = true;
        } else if (keyboard.ImageDots[2].isInside(x, y)) {
            //Log.d("BUTTON 2", "ENTER REGION!");
            if (!keyboard.StateDots[2]) {
                keyboard.toggleDotVisibility(2);
                addToLog("Passed over Button", String.valueOf(2), keyboard.StateDots[2]);
            }
            isComposingLetter = true;
        } else if (keyboard.ImageDots[3].isInside(x, y)) {
            //Log.d("BUTTON 3", "ENTER REGION!");
            if (!keyboard.StateDots[3]) {
                keyboard.toggleDotVisibility(3);
                addToLog("Passed over Button", String.valueOf(3), keyboard.StateDots[3]);
            }
            isComposingLetter = true;
        } else if (keyboard.ImageDots[4].isInside(x, y)) {
            //Log.d("BUTTON 4", "ENTER REGION!");
            if (!keyboard.StateDots[4]) {
                keyboard.toggleDotVisibility(4);
                addToLog("Passed over Button", String.valueOf(4), keyboard.StateDots[4]);
            }
            isComposingLetter = true;
        } else if (keyboard.ImageDots[5].isInside(x, y)) {
            //Log.d("BUTTON 5", "ENTER REGION!");
            if (!keyboard.StateDots[5]) {
                keyboard.toggleDotVisibility(5);
                addToLog("Passed over Button", String.valueOf(5), keyboard.StateDots[5]);
            }
            isComposingLetter = true;
        } else {
            isComposingLetter = false;
        }

        keyboardView.invalidateAllKeys();
    }

    private void touch_up(boolean skipTimeout) {
        checkOutput = true;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (checkOutput) {
                    addToLog("Touch up", " - ", true);
                    confirmCharacter();
                    checkOutput = false;
                    isComposingLetter = false;
                }

            }
        }, skipTimeout ? 0 : 1200);

    }

    public int getDistanceRelativeVibration(View view, float px,float py) {

        int radius = view.getWidth()/2;
        int distX = (int) Math.abs(px - radius);
        int distY = (int) Math.abs(py - radius);

        // For a 200px radius, max vibration is 40, min 0
        int dist = (int) Math.sqrt(Math.pow(distX, 2) + Math.pow(distY, 2));

        // return dist/5;
        // Exponential variation
        if ((dist/(5 + Math.pow(1.03, 2))) > 0)
            return (int) (dist/(5 + Math.pow(1.03, 2)));
        else
            return (int) (-1*(dist/(5 + Math.pow(1.03, 2))));
    }
}
