package com.example.braillecranwear;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.drm.DrmStore;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.textservice.SpellCheckerSession;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.braillecranwear.BrailleÉcran.ActivityAccessibleList;
import com.example.braillecranwear.BrailleÉcran.CharacterToSpeech;
import com.example.braillecranwear.GestureDetectors.TwoFingersSwipeDetector;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class BrailleIME extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener,
                    MessageApi.MessageListener,
                    MessageClient.OnMessageReceivedListener  {

    public BrailleKeyboard keyboard;
    public BrailleKeyboardView keyboardView;
    public BrailleCandidatesView candidateView;

    // Gesture detection
    protected TwoFingersSwipeDetector twoFingersSwipeListener;
    protected GestureDetector gestureDetector;
    protected int longPressTimeout = 800;
    protected int longLongPressTimeout = 2000;
    protected boolean isLongPressing = false;
    protected boolean hasJustLongPressed = false;
    protected boolean isLongLongPressing = false;
    protected boolean hasJustLongLongPressed = false;
    protected Runnable longPressRunnable;
    protected Handler longPressHandler;
    protected Runnable longLongPressRunnable;
    protected Handler longLongPressHandler;

    protected String message;
    private int cursorPosition = 0;

    // Feedback Tools
    protected ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
    protected Vibrator vibrator = null;
    protected CharacterToSpeech tts;

    //Settings flags
    protected boolean isScreenRotated = false;
    protected boolean isUsingWordReading = false;
    protected boolean isSpellChecking = true;
    protected boolean speakWordAtSpace = true;
    protected boolean spaceAfterPunctuation = false;

    // Control flags
    protected boolean isTTSInitialized = false;
    protected boolean hasJustTwoFingerSwiped = false;
    protected boolean recordLogs = true;

    //Spell Checker
    protected SpellCheckerSession spellCheckerSession;
    protected ArrayList<String> suggestions;

    // Related to Sending the Message
    private List<Node> myNodes = new ArrayList<>();
    private static GoogleApiClient mGoogleApiClient;
    private static final String SPELLCHECKER_WEAR_PATH = "/message-to-spellchecker";
    private static final long CONNECTION_TIME_OUT_MS = 2500;

    // Related to log for tests
    private Timer time;
    private TimerTask checkingTimerTask = null;
    private File logFile;
    private Calendar cal;
    public String method = "";

    // Constructor
    public BrailleIME() {
        message = "";
    }

    @Override
    public View onCreateInputView() {
        super.onCreateInputView();

        // Loads Keyboard
        keyboardView = (BrailleKeyboardView) getLayoutInflater().inflate(R.layout.input, null);
        keyboardView.setOnKeyboardActionListener(this);

        keyboard = new BrailleKeyboard(this, R.xml.braillecell);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setPreviewEnabled(false);

        setCandidatesViewShown(false);

        // Initializes Google API
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApiIfAvailable(Wearable.API)
                .build();
        getNodes();

        //Prepares Long Press variables
        longPressRunnable = new Runnable() {
            @Override
            public void run() {
                if (isLongPressing) {
//                    onLongPress();

                    isLongPressing = false;
                    hasJustLongPressed = true;
                }
            }
        };
        longPressHandler = new Handler();

        longLongPressRunnable = new Runnable() {
            @Override
            public void run() {
                if (isLongLongPressing) {
//                    onLongLongPress();

                    isLongLongPressing = false;
                    hasJustLongLongPressed = true;
                }
            }
        };
        longLongPressHandler = new Handler();

        // Sets two finger swipe for deleting and accessing navigation mode
        twoFingersSwipeListener = new TwoFingersSwipeDetector() {
            @Override
            protected void onTwoFingersSwipeLeft() {
                if (message.length() > 0)
                    removeCharacter();
            }

            @Override
            protected void onTwoFingersSwipeRight() {
                enterNavigationMode();
            }
        };

        return keyboardView;
    }

    @Override
    public View onCreateCandidatesView() {
        super.onCreateCandidatesView();

        candidateView = new BrailleCandidatesView(this);
        candidateView.setService(this);

        return candidateView;
    }

    @Override
    public void onStartCandidatesView(EditorInfo info, boolean restarting) {
        super.onStartCandidatesView(info, restarting);

        Log.d("CANDIDATES", info.toString());
    }

    @Override
    public void onPress(int primaryCode) {
        Log.d("ON PRESS", String.valueOf(primaryCode));
    }

    @Override
    public void onRelease(int primaryCode) {
        Log.d("ON RELEASE", String.valueOf(primaryCode));
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        Log.d("ON KEY", String.valueOf(primaryCode));
    }

    @Override
    public void onText(CharSequence text) {
        Log.d("ON TEXT", text.toString());
    }

    @Override
    public void swipeLeft() {
        Log.d("ON SWIPE", "LEFT");
    }

    @Override
    public void swipeRight() {
        Log.d("ON SWIPE", "RIGHT");
    }

    @Override
    public void swipeDown() {
        Log.d("ON SWIPE", "DOWN");
    }

    @Override
    public void swipeUp() { Log.d("ON SWIPE", "UP"); }

    // CONFIRMS THE BRAILLE CELL COMPOSITION
    public void confirmCharacter() {
        final String latinChar = keyboard.checkCurrentCharacter(false, false, false, false);

        Log.d("CHAR OUTPUT: ", latinChar);

        keyboard.getKeys().get(4).label = latinChar;
        keyboardView.invalidateKey(4);

        if (!latinChar.equals("Ma") &&
                !latinChar.equals("MA") &&
                !latinChar.equals("Nu") &&
                !latinChar.equals("NU") &&
                !latinChar.equals("In") &&
                !latinChar.equals("IN") &&
                !latinChar.equals("Cf") &&
                !latinChar.equals("CF") &&
                !latinChar.equals("?!")
        ) {
            if (message.length() > 1 && cursorPosition < message.length() - 1) {
                message = (message.substring(0, cursorPosition + 1).concat(latinChar)).concat(message.substring(cursorPosition + 1));
            } else {
                message = message.concat(latinChar);
            }
            Log.d("MESSAGE OUTPUT: ", "message:" + message);
            cursorPosition++;
        }

        if (isTTSInitialized) {

            // Breaks string into words to speak only last one
            String[] words = message.substring(0, cursorPosition).split(" ");
            Log.d("FULL MESSAGE OUTPUT: ", message);
//            Log.d("LAST MESSAGE OUTPUT: ", words[words.length - 1]);

            if (words.length > 0 && latinChar.equals(" ")&& isSpellChecking)
                fetchSuggestionsFromMobile(words[words.length - 1]);

            if (words.length > 0 && (isUsingWordReading || (speakWordAtSpace && latinChar.equals(" ")))) {
                tts.speak(words[words.length - 1], TextToSpeech.QUEUE_ADD, null, "Output");
            } else {
                speakComposedWord(latinChar);

                // Automatically adds space after punctuation.
                if (spaceAfterPunctuation && (latinChar.equals(",") || latinChar.equals(".") || latinChar.equals(":")|| latinChar.equals("!") || latinChar.equals("?"))){
                    keyboard.toggleAllDotsOff();
                    keyboardView.invalidateAllKeys();
                    confirmCharacter();
                }
            }
        }

        // Clears result letter on screen
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                keyboard.toggleOnlyDotImagesOff();
                keyboard.getKeys().get(4).label = "";
                keyboardView.invalidateAllKeys();
            }
        }, 1200);
    }

    // WORD SPEAKING
    private void speakComposedWord(String currentChar) {
        tts.speak(currentChar, TextToSpeech.QUEUE_FLUSH, null, "Audio Character Output");
    }

    // HANDLES LONG CLICK DETECTION LOGIC
    protected void handleLongPressDetection(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP ||
                event.getAction() == MotionEvent.ACTION_MOVE ||
                event.getAction() == MotionEvent.ACTION_POINTER_DOWN ||
                event.getAction() == MotionEvent.ACTION_POINTER_UP) {
            isLongPressing = false;
            isLongLongPressing = false;

            if (hasJustLongPressed && !hasJustLongLongPressed)
                onLongPress();

            if (hasJustLongLongPressed)
                onLongLongPress();

            longPressHandler.removeCallbacks(longPressRunnable);
            longLongPressHandler.removeCallbacks(longLongPressRunnable);

        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            isLongPressing = true;
            hasJustLongPressed = false;
            longPressHandler.postDelayed(longPressRunnable, longPressTimeout);

            isLongLongPressing = true;
            hasJustLongLongPressed = false;
            longLongPressHandler.postDelayed(longLongPressRunnable, longLongPressTimeout);

        }
    }

    // LONG PRESS LOGIC
    public void onLongPress() {
//        enterNavigationMode();
        Log.d("FULL MESSAGE OUTPUT: ", message);

        if (message.length() > 0) {
            tts.speak(getString(R.string.SendingFullSentence) + message, TextToSpeech.QUEUE_FLUSH, null, "Output info");
        } else {
            tts.speak(getString(R.string.EmptyMessage), TextToSpeech.QUEUE_FLUSH, null, "Output empty message info.");
        }

        vibrator.vibrate(300);

        ArrayList<Keyboard.Key> activeButtons = new ArrayList();
        for (int i = 0; i < keyboard.StateDots.length; i++) {
            if (keyboard.StateDots[i])
                activeButtons.add(keyboard.ImageDots[i]);
        }
        String activeButtonsMessage = new String();
        if (activeButtons.size() > 0) {
            for (int i = 0; i < activeButtons.size(); i++) {
                activeButtonsMessage = activeButtonsMessage.concat(String.valueOf(activeButtons.get(i).codes[0]));
                if (i <= activeButtons.size() - 3)
                    activeButtonsMessage += ", ";
                else if (i > activeButtons.size() - 3 && i <= activeButtons.size() - 2)
                    activeButtonsMessage += " e ";
                else
                    activeButtonsMessage += ".";
            }
            Log.d("Extra message", activeButtonsMessage);
            tts.speak(getString(R.string.ActivatedDots) + activeButtonsMessage, TextToSpeech.QUEUE_ADD, null, "Extra output info");
        } else {
            tts.speak(getString(R.string.NoActiveDots), TextToSpeech.QUEUE_ADD, null, "Output no active dots info.");
        }

    }

    public void onLongLongPress() {
        Log.d("LONG", "LOOONG LOOONG PRESS");
        vibrator.vibrate(600);
        isLongPressing = false;
        hasJustLongPressed = false;
        longPressHandler.removeCallbacks(longPressRunnable);

        getCurrentInputConnection().commitText(message, message.length() - 1);
        getCurrentInputConnection().finishComposingText();
        getCurrentInputConnection().performEditorAction(EditorInfo.IME_ACTION_DONE);

        tts.speak(getString(R.string.MessageSent), TextToSpeech.QUEUE_ADD, null, "Message sent.");

    }

    // WRIST TWIST GESTURES, CALL SUGGESTION LIST
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_NAVIGATE_NEXT:
                if (isSpellChecking) {
                    Log.d("WRIST FLICK", "SPELL CHECKING...");
                    launchSuggestions();
                } else {
                    Log.d("WRIST FLICK", "SPELL CHECK DISABLED ON WATCH");
                }
                return true;
            case KeyEvent.KEYCODE_NAVIGATE_PREVIOUS:
                Log.d("WRIST FLICK", "PREV");
                if (isSpellChecking) {
                    Log.d("WRIST FLICK", "SPELL CHECKING...");
                    launchSuggestions();
                } else {
                    Log.d("WRIST FLICK", "SPELL CHECK DISABLED ON WATCH");
                }
                return true;
        }
        // If you did not handle it, let it be handled by the next possible element as deemed by the Activity.
        return super.onKeyDown(keyCode, event);
    }

    // ENTERING NAVIGATION MODE LIST
    protected void enterNavigationMode() {
        if (message != null && message.length() > 0) {
            Intent intent = new Intent(this, ActivityAccessibleList.class);
            String[] chars = new StringBuilder(message).reverse().toString().split("");
            String[] charsRelevant = new String[chars.length - 1];
            for (int i = 1; i < chars.length; i++)
                charsRelevant[i-1] = chars[i];

            intent.putExtra("items", charsRelevant);
            intent.putExtra("introSpeakingSentence", getString(R.string.navigation_mode_intro));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                this.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.d("ACTIVITY", e.getMessage());
            }
        }
    }

    // ENTERING SUGGESTIONS LIST
    private void launchSuggestions() {
        if (suggestions != null && suggestions.size() > 0) {
            Intent intent = new Intent(this, ActivityAccessibleList.class);
            intent.putExtra("items", suggestions.toArray(new String[suggestions.size()]));
            intent.putExtra("introSpeakingSentence", getString(R.string.suggestions_intro));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                this.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.d("ACTIVITY", e.getMessage());
            }
        }
    }

    // RETURNING FROM LIST VIEW, EITHER SUGGESTIONS OR NAVIGATION MODE
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
//            Log.d("SUGGESTIONS", "Selecionado: " + data.getStringExtra("selectedItem"));
//            applySuggestion(data.getStringExtra("selectedItem"));
//        } else if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
//            Log.d("SELECTED WORD", "Selecionado: " + data.getStringExtra("selectedItem"));
////            removeCharacter(data.getStringExtra("selectedItem"), data.getIntExtra("selectedIndex", -1));
//            cursorPosition = Math.abs(data.getIntExtra("selectedIndex", 0) - (message.length() - 1));
//            Log.d("NEW CURSOR POSITION", cursorPosition + "");
//        } else if (resultCode == RESULT_CANCELED ){
//            Log.d("SUGGESTIONS", "Cancelado");
//            if (isTTSInitialized) {
//                tts.speak(getString(R.string.canceled), TextToSpeech.QUEUE_FLUSH, null, "suggestion_canceled");
//            }
//        }
    }

    // CHARACTER DELETING
    protected void removeCharacter() {
        if (cursorPosition <= message.length() && cursorPosition > 0) {
            tts.speak(tts.getAdaptedText(String.valueOf(message.charAt(cursorPosition -  1))) + " " + getString(R.string.deleted), TextToSpeech.QUEUE_FLUSH, null, "character_deleted");

            StringBuilder stringBuilder = new StringBuilder(message);
            stringBuilder.deleteCharAt(cursorPosition -1);
            message = stringBuilder.toString();
            cursorPosition--;
        }
    }

    // SPELL CHECKER SUGGESTION ACCEPTATION
    private void applySuggestion(String selectedSuggestion) {

        // Clears current suggestions list
        suggestions.clear();

        //Finds if there is any punctuation, as those are  ignored by spell checker
        String punctuation = message.substring(message.length() - 2);
        if (!(punctuation.equals(", ")) && !(punctuation.equals(". ")) && !(punctuation.equals("! ")) && !(punctuation.equals(": ")) && !(punctuation.equals("? ")) && !(punctuation.equals("; "))) {
            Log.d("PUNCTUATION", message.substring(message.length() - 2));
            punctuation = "";
        }

        // Rebuild list with fixed word
        String[] words = message.split(" ");
        words[words.length - 1] = selectedSuggestion;
        message = "";
        for (int i = 0; i < words.length; i++) {
            message = message + words[i];
            if (i < words.length - 1) {
                message = message + " ";
            }
        }
        message = message + punctuation;
        if (punctuation.equals("")) {
            message = message + " ";
        }
        Log.d("CORRECTED MESSAGE", message);

        // Speaks out selected suggestion
        if (isTTSInitialized) {
            tts.speak(getString(R.string.fixedMessage) + selectedSuggestion, TextToSpeech.QUEUE_FLUSH, null, "suggestion_option");
        }
    }

    // SPELL CHECKING -----------------------------------------
    private void fetchSuggestionsFromMobile(String input){

        if (myNodes != null && mGoogleApiClient != null) {
            byte[] message;
            try {
                message = input.getBytes("UTF-8");
                final byte[] finalMessage = message;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for(Node n:myNodes) {
                            Log.d("GOOGLE API","Sending message to node: " + n.getDisplayName());
                            Wearable.MessageApi.sendMessage(mGoogleApiClient,n.getId(),SPELLCHECKER_WEAR_PATH, finalMessage);
                        }
                    }
                }).run();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    /* Handling received suggestions. */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        String messageEventString = new String(messageEvent.getData());
        messageEventString = messageEventString.substring(1, messageEventString.length() - 1);
        String[] suggestedStrings = messageEventString.split(", ");
        suggestions = new ArrayList<String>();

        for (String suggestion: suggestedStrings) {
            suggestions.add(suggestion);
            Log.d("ON SUGGESTIONS RECEIVED", suggestion);
        }
        Log.d("ON SUGGESTIONS RECEIVED", messageEventString);
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD);

    }

    private List<Node> getNodes(){
        new Thread(new Runnable() {

            @Override
            public void run() {
                Log.d("GOOGLE API","Getting Google API nodes...");

                mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);

                NodeApi.GetConnectedNodesResult result = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                List<Node> nodes = result.getNodes();

                for(Node n:nodes){
                    Log.d("GOOGLE API","Adding Google API Node: "+n.getDisplayName());
                    myNodes.add(n);
                }

                Log.d("GOOGLE API","Getting nodes DONE!");
            }
        }).start();

        return null;
    }

    // LOG TO RECORD ACTIONS FOR TESTS -----------------------------
    // Sets up Log file
    private void createLogFile() {
        if (recordLogs) {

            cal = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("EEEE_MMMM_d_hh:mm:ss");

            logFile = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath() + "/BÉLOG_" + format.format(cal.getTime()) + ".txt");

            if (!logFile.exists()) {

                try {
                    logFile.createNewFile();

                    //BufferedWriter for performance, true to set append to file flag
                    BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                    buf.append( "description;appState;wordId;letterId;button;buttonQualifier;buttonState;message;dateDay;dateHour");
                    buf.newLine();
                    buf.flush();
                    buf.close();

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            addToLog("Instanciado este arquivo de Log", " - ", " - ", true, " - ");
        }
    }

    // Adds strings to the Log File
    public void addToLog(String description, String button, String buttonQualifier, boolean buttonState, String message)
    {

        if (recordLogs && logFile != null) {

            cal = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd;hh:mm:ss,SSS");

            try {
                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                cal.getTime();
                buf.append( description + ";" +
                        method + ";" +
                        getCurWordId() + ";" +
                        getCurLetterId() + ";" +
                        button + ";" +
                        buttonQualifier + ";" +
                        buttonState + ";'" +
                        message + "';" + format.format(cal.getTime()));
                buf.newLine();
                buf.flush();
                buf.close();

                Log.d("STORAGE", "INICIADO MESMO");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private int getCurWordId() {

        int wordCount = 0;
        String s = message;

        boolean word = false;
        int endOfLine = s.length() - 1;

        for (int i = 0; i < s.length(); i++) {
            // if the char is a letter, word = true.
            if (Character.isLetter(s.charAt(i)) && i != endOfLine) {
                word = true;
                // if char isn't a letter and there have been letters before,
                // counter goes up.
            } else if (!Character.isLetter(s.charAt(i)) && word) {
                wordCount++;
                word = false;
                // last word of String; if it doesn't end with a non letter, it
                // wouldn't count without this.
            } else if (Character.isLetter(s.charAt(i)) && i == endOfLine) {
                wordCount++;
            }
        }
        return wordCount;
    }

    private int getCurLetterId() {
        return message.length();
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);


        // Sets TextToSpeech for feedback
        tts = new CharacterToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    Log.d("TTS", "TextToSpeech Service Initialized");
                    isTTSInitialized = true;
                    tts.setLanguage(Locale.getDefault());
                }
            }
        });
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (isScreenRotated) {
            keyboardView.setRotation(-90f);
            keyboardView.setTranslationY(-10f);
            keyboardView.setTranslationX(10f);
        } else {
            keyboardView.setRotation(0f);
            keyboardView.setTranslationY(0f);
            keyboardView.setTranslationX(0f);
        }
        Log.d("STARTING VIEW", "HELLO THERE");
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);

        // Load Preferences
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        isScreenRotated = preferences.getBoolean("rotate_screen", isScreenRotated);
        isUsingWordReading = preferences.getBoolean("word_reading", isUsingWordReading);
//        isSpellChecking = preferences.getBoolean("rotate_screen", isScreenRotated);
        speakWordAtSpace = preferences.getBoolean("speak_word_at_space", speakWordAtSpace);
        spaceAfterPunctuation = preferences.getBoolean("space_at_punctuation", spaceAfterPunctuation);

        // Resets variables
        cursorPosition = 0;
        message = "";

        // Create Log File
        createLogFile();

        Log.d("STARTING", "HELLO THERE");
    }


    @Override
    public void onFinishInputView(boolean finishingInput) {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onFinishInputView(finishingInput);

        Log.d("FINISHING", "BYE BYE");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
