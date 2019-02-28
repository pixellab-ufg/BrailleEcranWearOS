package com.example.braillecranwear;

import android.content.Context;
import android.content.SharedPreferences;
import android.inputmethodservice.Keyboard;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.example.braillecranwear.BrailleÉcran.Dots;
import com.example.braillecranwear.BrailleÉcran.DotsXMLPullParser;

import java.util.ArrayList;
import java.util.List;

class BrailleKeyboard extends Keyboard {

    // Create an array of six initially invisible OutputDots and associate them with the XML
    public final Key ImageDots[] = new Key[6];
    public final boolean StateDots[] = new boolean[6];

    // An array that stores the 44 possible summations.
    private ArrayList<Integer> SummedValueDots = new ArrayList<Integer>();

    // List of 44 Dots Objects, that hold the information read from XML
    private List<Dots> DotsList;

    // Tone Generator, Vibrator and TextToSpeech for feedback
    private ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
    private Vibrator vibrator = null;
    private TextToSpeech dotTTS;

    // Access to database for loading settings
    public SharedPreferences settings;

    // Flags to deal with feedback
    boolean useVibrationPatterns = false;
    boolean useToneGenerator = true;
    boolean useDotNumberSpeaker = false;

    // Layout Order string
    private String layoutOrder = "123456";

    private Context context;
    private int nSymbols = 53; // Number of symbols listed in the XML


    public BrailleKeyboard(Context context, int xmlLayoutResId) {
        super(context, xmlLayoutResId);

        this.context = context;

        // Loads preferences
        settings = PreferenceManager
                .getDefaultSharedPreferences(context);
        this.loadSettings();

        // Associate view elements
        this.createDotButtons();

        // Sets tts and vibration service
        vibrator = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
        dotTTS = new TextToSpeech(this.context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                Log.d("TTS", "TextToSpeech Service Initialized");
                //tts.setLanguage(Locale.ENGLISH);
            }
        });

        // Uses the XMLPullParser to fill the Dots list with Dots Objects
        DotsList = DotsXMLPullParser.getStaticDotsFromFile(context, layoutOrder);

        // Fill the array with the 45 possible summations
        for (int i = 0; i < nSymbols; i++) {
            SummedValueDots.add(DotsList.get(i).getSumValue());
        }

    }


    // Appear or disappear with the Dots
    public boolean toggleDotVisibility(int i){
        if (!StateDots[i]) {
            this.setDotVisibility(i,true);
            return true;
        } else {
            this.setDotVisibility(i,false);
            return false;
        }
    }

    // Change dot visibility, regardless of it's current state.
    public void setDotVisibility(int i, boolean value) {

        if (!value) {
            StateDots[i] = false;
            ImageDots[i].icon.mutate().setAlpha(40);

            switch (i) {
                case 0:
                    if (useToneGenerator) toneGenerator.startTone(ToneGenerator.TONE_CDMA_SIGNAL_OFF, 100);
                    if (useVibrationPatterns)
                        this.vibrator.vibrate(new long[]{0,50}, -1);
                    else
                        this.vibrator.vibrate(100);
                    if (useDotNumberSpeaker) this.dotTTS.speak("1", TextToSpeech.QUEUE_FLUSH, null, "Dot 1");
                    break;
                case 1:
                    if (useToneGenerator) toneGenerator.startTone(ToneGenerator.TONE_CDMA_SIGNAL_OFF, 100);
                    if (useVibrationPatterns)
                        this.vibrator.vibrate(new long[]{0,50,30,50},-1);
                    else
                        this.vibrator.vibrate(100);
                    if (useDotNumberSpeaker) this.dotTTS.speak("2", TextToSpeech.QUEUE_FLUSH, null, "Dot 2");
                    break;
                case 2:
                    if (useToneGenerator) toneGenerator.startTone(ToneGenerator.TONE_CDMA_SIGNAL_OFF, 100);
                    if (useVibrationPatterns)
                        this.vibrator.vibrate(new long[]{0,50,30,50,30,50},-1);
                    else
                        this.vibrator.vibrate(100);
                    if (useDotNumberSpeaker) this.dotTTS.speak("3", TextToSpeech.QUEUE_FLUSH, null, "Dot 3");
                    break;
                case 3:
                    if (useToneGenerator) toneGenerator.startTone(ToneGenerator.TONE_CDMA_SIGNAL_OFF, 100);
                    if (useVibrationPatterns)
                        this.vibrator.vibrate(new long[]{0,50,30,50,30,50,30,50},-1);
                    else
                        this.vibrator.vibrate(100);
                    if (useDotNumberSpeaker) this.dotTTS.speak("4", TextToSpeech.QUEUE_FLUSH, null, "Dot 4");
                    break;
                case 4:
                    if (useToneGenerator) toneGenerator.startTone(ToneGenerator.TONE_CDMA_SIGNAL_OFF, 100);
                    if (useVibrationPatterns)
                        this.vibrator.vibrate(new long[]{0,50,30,50,30,50,30,50,30,50},-1);
                    else
                        this.vibrator.vibrate(100);
                    if (useDotNumberSpeaker) this.dotTTS.speak("5", TextToSpeech.QUEUE_FLUSH, null, "Dot 5");
                    break;
                case 5:
                    if (useToneGenerator) toneGenerator.startTone(ToneGenerator.TONE_CDMA_SIGNAL_OFF, 100);
                    if (useVibrationPatterns)
                        this.vibrator.vibrate(new long[]{0,50,25,50,25,50,25,50,25,50},-1);
                    else
                        this.vibrator.vibrate(100);
                    if (useDotNumberSpeaker) this.dotTTS.speak("6", TextToSpeech.QUEUE_FLUSH, null, "Dot 6");
                    break;
            }

        } else {
            StateDots[i] = true;
            ImageDots[i].icon.mutate().setAlpha(255);

            switch (i) {
                case 0:
                    if (useToneGenerator) toneGenerator.startTone(ToneGenerator.TONE_DTMF_1, 100);
                    if (useVibrationPatterns) this.vibrator.vibrate(new long[]{0,50}, -1);
                    else
                        this.vibrator.vibrate(200);
                    if (useDotNumberSpeaker) this.dotTTS.speak("1", TextToSpeech.QUEUE_ADD, null, "Dot 1");
                    break;
                case 1:
                    if (useToneGenerator) toneGenerator.startTone(ToneGenerator.TONE_DTMF_2, 100);
                    if (useVibrationPatterns)
                        this.vibrator.vibrate(new long[]{0,50,30,50},-1);
                    else
                        this.vibrator.vibrate(200);
                    if (useDotNumberSpeaker) this.dotTTS.speak("2", TextToSpeech.QUEUE_ADD, null, "Dot 2");
                    break;
                case 2:
                    if (useToneGenerator) toneGenerator.startTone(ToneGenerator.TONE_DTMF_3, 100);
                    if (useVibrationPatterns)
                        this.vibrator.vibrate(new long[]{0,50,30,50,30,50},-1);
                    else
                        this.vibrator.vibrate(200);
                    if (useDotNumberSpeaker) this.dotTTS.speak("3", TextToSpeech.QUEUE_ADD, null, "Dot 3");
                    break;
                case 3:
                    if (useToneGenerator) toneGenerator.startTone(ToneGenerator.TONE_DTMF_4, 100);
                    if (useVibrationPatterns)
                        this.vibrator.vibrate(new long[]{0,50,30,50,30,50,30,50},-1);
                    else
                        this.vibrator.vibrate(200);
                    if (useDotNumberSpeaker) this.dotTTS.speak("4", TextToSpeech.QUEUE_ADD, null, "Dot 4");
                    break;
                case 4:
                    if (useToneGenerator) toneGenerator.startTone(ToneGenerator.TONE_DTMF_5, 100);
                    if (useVibrationPatterns)
                        this.vibrator.vibrate(new long[]{0,50,30,50,30,50,30,50,30,50},-1);
                    else
                        this.vibrator.vibrate(200);
                    if (useDotNumberSpeaker) this.dotTTS.speak("5", TextToSpeech.QUEUE_ADD, null, "Dot 5");
                    break;
                case 5:
                    if (useToneGenerator) toneGenerator.startTone(ToneGenerator.TONE_DTMF_6, 100);
                    if (useVibrationPatterns)
                        this.vibrator.vibrate(new long[]{0,50,25,50,25,50,25,50,25,50},-1);
                    else
                        this.vibrator.vibrate(200);
                    if (useDotNumberSpeaker) this.dotTTS.speak("6", TextToSpeech.QUEUE_ADD, null, "Dot 6");
                    break;
            }
        }
    }

    // Disappear all the Dots
    public void toggleAllDotsOff() {

        for (int i = 0; i < 6; i++) {
            ImageDots[i].icon.mutate().setAlpha(40);
            StateDots[i] = false;
        }

    }
    public void toggleOnlyDotButtonsOff() {
        for (int i = 0; i < 6; i++)
            StateDots[i] = false;
    }
    public void toggleOnlyDotImagesOff() {
        for (int i = 0; i < 6; i++)
            ImageDots[i].icon.mutate().setAlpha(40);
    }

    // Called in MainActivity every time a Dot is added or removed.
    public String checkCurrentCharacter(boolean CapsOn, boolean tmpCapsOn, boolean NumOn, boolean tmpNumOn) {

        String latimOutput = "";
        int currentSum = 0, indexLatimOutput = 0;

        // Reads the current visibility of the dots and associates it with a summed value
        for (int i = 0; i < 6; i++) {
            if (StateDots[i])
                currentSum += Math.pow(2, i);
        }

        // Looks for which index of the SummedValuesDots vector equals the sum of the current Dots
        for (; indexLatimOutput < nSymbols; indexLatimOutput++) {
            if (SummedValueDots.get(indexLatimOutput) == currentSum) {
                break;
            }
        }

        // Gets from the Dots object the relation between the found summation and a Symbol
        // the indexLatimOutput is checked to see if it's a valid character.
        if (indexLatimOutput < nSymbols) {
            latimOutput = DotsList.get(indexLatimOutput).getDotSymbol();

            // Removing Settings from the list, making it an Invalid Character
            if (latimOutput.equals("Cf") && CapsOn == false && tmpCapsOn == false)
                latimOutput = "In";

            // Removing Help from the list, making it an Invalid Character
            if (latimOutput.equals("?!") && NumOn == false && tmpNumOn == false)
                latimOutput = "In";

            // Apply the flags changes:
            if (CapsOn || tmpCapsOn) {
                if (latimOutput != " ") // workaround, uoUpperCase() seems to remove white space
                    latimOutput = latimOutput.toUpperCase();
            }
            Log.d("LATIM_OUTPUT", "Current: " + latimOutput);
            if (NumOn || tmpNumOn) {

                switch (latimOutput)
                {
                    case "a":
                        latimOutput = "1";
                        break;
                    case "b":
                        latimOutput = "2";
                        break;
                    case "c":
                        latimOutput = "3";
                        break;
                    case "d":
                        latimOutput = "4";
                        break;
                    case "e":
                        latimOutput = "5";
                        break;
                    case "f":
                        latimOutput = "6";
                        break;
                    case "g":
                        latimOutput = "7";
                        break;
                    case "h":
                        latimOutput = "8";
                        break;
                    case "i":
                        latimOutput = "9";
                        break;
                    case " ":
                        latimOutput = " ";
                        break;
                    case "j":
                        latimOutput = "0";
                        break;
                    case "A":
                        latimOutput = "1";
                        break;
                    case "B":
                        latimOutput = "2";
                        break;
                    case "C":
                        latimOutput = "3";
                        break;
                    case "D":
                        latimOutput = "4";
                        break;
                    case "E":
                        latimOutput = "5";
                        break;
                    case "F":
                        latimOutput = "6";
                        break;
                    case "G":
                        latimOutput = "7";
                        break;
                    case "H":
                        latimOutput = "8";
                        break;
                    case "I":
                        latimOutput = "9";
                        break;
                    case "J":
                        latimOutput = "0";
                        break;
                    case ".":
                        latimOutput = ".";
                        break;
                    case "-":
                        latimOutput = "-";
                        break;
                    case "?":
                        latimOutput = "?";
                        break;
                    case "!":
                        latimOutput = "!";
                        break;
                    case "%":
                        latimOutput = "%";
                        break;
                    case ",":
                        latimOutput = ",";
                        break;
                    case ":":
                        latimOutput = ":";
                        break;
                    case "$":
                        latimOutput = "$";
                        break;
                    case "\"":
                        latimOutput = "\"";
                        break;
                    case "*":
                        latimOutput = "*";
                        break;
                    case "Cf":
                        latimOutput = "Cf";
                        break;
                    case "CF":
                        latimOutput = "Cf";
                        break;
                    case "?!":
                        latimOutput = "?!";
                        break;
                    default:
                        latimOutput = "In";
                        break;
                }
            }
        } else {
            latimOutput = "In";
        }

        toggleOnlyDotButtonsOff();

        Log.d("BRAILLE_DOTS", "Output:" + latimOutput + ".");
        return latimOutput;
    }

    // Load settings from database
    public void loadSettings() {
        useDotNumberSpeaker = settings.getBoolean("dot_number_speaker", useDotNumberSpeaker);
        useVibrationPatterns = settings.getBoolean("vibration_patterns", useVibrationPatterns);
        useToneGenerator = settings.getBoolean("tone_generator", useToneGenerator);
        layoutOrder = settings.getString("dots_layout", layoutOrder);

        Log.d("LAYOUT ORDER SET TO: ", layoutOrder);
    }

    public void setLayoutOrder(String layoutOrder) {
        if (layoutOrder.equals("123456") ||
                layoutOrder.equals("456123") ||
                layoutOrder.equals("123654") ||
                layoutOrder.equals("654123") ||
                layoutOrder.equals("321654") ||
                layoutOrder.equals("456321") ||
                layoutOrder.equals("654321") ||
                layoutOrder.equals("321456")
        ) {
            final SharedPreferences.Editor editor = settings.edit();
            editor.putString("layoutOrder", layoutOrder);
            editor.apply();
        }
    }

    public String getLayoutOrder() {
        return layoutOrder;
    }

    // To be used on activities onDestroy()
    public void freeTTSService() {
        dotTTS.stop();
        dotTTS.shutdown();
    }

    // Creates Dot Buttons
    private void createDotButtons() {
        List<Key> keys = getKeys();

        for(int i = 0; i < keys.size(); i++) {
            if (keys.get(i).codes[0] == 1) {
                ImageDots[0] = keys.get(i);
            } else if (keys.get(i).codes[0] == 2) {
                ImageDots[1] = keys.get(i);
            } else if (keys.get(i).codes[0] == 3) {
                ImageDots[2] = keys.get(i);
            } else if (keys.get(i).codes[0] == 4) {
                ImageDots[3] = keys.get(i);
            } else if (keys.get(i).codes[0] == 5) {
                ImageDots[4] = keys.get(i);
            } else if (keys.get(i).codes[0] == 6) {
                ImageDots[5] = keys.get(i);
            }
        }
        toggleAllDotsOff();
    }

}
