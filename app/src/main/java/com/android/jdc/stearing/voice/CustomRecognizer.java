package com.android.jdc.stearing.voice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import org.apache.commons.codec.language.Soundex;

import java.util.ArrayList;

public class CustomRecognizer implements TextToSpeech.OnInitListener, RecognitionListener {
    private static final String TAG = "CustomRecognizer";
    public String Words;
    Soundex soundex = new Soundex();
    private SpeechRecognizer mSpeech;
    private Intent mSpeechIntent;
    private Context mContext;
    private VoiceRecognizerListener mListener;

    public CustomRecognizer(Activity _context) {
        this.mContext = _context;
        this.mListener = (VoiceRecognizerListener) _context;
        Words = "";

        mSpeech = SpeechRecognizer.createSpeechRecognizer(this.mContext);
        mSpeech.setRecognitionListener(this);
        mSpeechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        mSpeechIntent.putExtra(RecognizerIntent.ACTION_RECOGNIZE_SPEECH, RecognizerIntent.EXTRA_PREFER_OFFLINE);
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 60000);
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 50);
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 100);
    }

    void startover() {
        mSpeech.destroy();
        mSpeech = SpeechRecognizer.createSpeechRecognizer(this.mContext);
        mSpeech.setRecognitionListener(this);
        mSpeechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 60000);
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 50);
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 100);
        StartListening();
    }

    public void StartListening() {
        mSpeech.startListening(mSpeechIntent);
    }

    public void StopListening() {
        mSpeech.stopListening();
    }

    @Override
    public void onBeginningOfSpeech() {

        Log.i(TAG, "onBeginningOfSpeech");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {

        Log.i(TAG, "onBufferReceived");
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(TAG, "onEndOfSpeech");
    }


    @Override
    public void onResults(Bundle results) {
        Log.i(TAG, "onResults");
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches == null)
            Words = "Null";
        else {
            if (matches.size() != 0) {
                Words = matches.get(0);

                String[] words = Words.split(" ");
                processCommand(words[words.length - 1]);
            } else
                Words = "";

            //do anything you want for the result
        }

        Log.i(TAG, "Word: " + Words);

        startover();
    }

    @Override
    public void onRmsChanged(float rmsdB) {

        // Log.i(TAG, "onRmsChanged");
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {

        Log.i(TAG, "onReadyForSpeech");
    }


    @Override
    public void onError(int i) {

        Log.i(TAG, "onError");
        startover();
    }


    @Override
    public void onPartialResults(Bundle bundle) {
        Log.i(TAG, "onPartialResults");

        /*



         */


        ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches == null)
            Words = "Null";
        else {
            if (matches.size() != 0) {
                Words = matches.get(0);
                String[] words = Words.split(" ");
                processCommand(words[words.length - 1]);
            } else
                Words = "";
            //do anything you want for the result
        }

        Log.i(TAG, "Word Partia: " + Words);

    }

    private void processCommand(String comm) {
        try {
            int diff = soundex.difference(comm, "go");
            Log.i(TAG, "go diff: " + diff);
            if (diff >= 3) {
                mListener.onGoCommand();
                return;
            }
            diff = soundex.difference(comm, "left");
            if (diff >= 3) {
                mListener.onLeftCommand();
                return;
            }
            diff = soundex.difference(comm, "right");
            if (diff >= 3) {
                mListener.onRightCommand();
                return;
            }
            diff = soundex.difference(comm, "none");
            if (diff >= 3) {
                mListener.onNoneCommand();
                return;
            }

            diff = soundex.difference(comm, "full");
            if (diff >= 4) {
                mListener.onAllCommand();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onEvent(int i, Bundle bundle) {

        Log.i(TAG, "onEvent");
    }

    @Override
    public void onInit(int i) {

        Log.i(TAG, "onInit");
    }

    public interface VoiceRecognizerListener {
        void onAllCommand();

        void onNoneCommand();

        void onLeftCommand();

        void onRightCommand();

        void onGoCommand();
    }
}
