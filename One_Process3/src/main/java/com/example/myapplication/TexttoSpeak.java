package com.example.myapplication;

/**
 * Created by 傻明也有春天 on 2016/7/17.
 */

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;

public class TexttoSpeak {
    private Context context;
    private TextToSpeech tts;
    public static boolean isAudioOn = true;

    public TexttoSpeak(final Context context) {
        this.context = context;
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.CHINA);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(context, "Language is not available.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void speak(String text) {
        if(isAudioOn){
            tts.speak(text, TextToSpeech.QUEUE_ADD, null);
        }
    }
    public void stop(){
        if (tts!=null)
            tts.shutdown();
    }
}
