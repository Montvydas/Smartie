package com.monklu.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.monklu.bluetooth.com.monklu.bluetoothConnect.ConnectThread;
import com.monklu.bluetooth.com.monklu.bluetoothConnect.ManageConnectedThread;
import com.monklu.bluetooth.voiceMagic.VoiceMagic;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by montuxxx on 16/02/16.
 */
public class SendActivity extends Activity implements View.OnClickListener, TextToSpeech.OnInitListener {

    private static final int SPEECH_REQUEST_CODE = 0;

    public BluetoothDevice device;
    public ConnectThread connectThread;
    private TextToSpeech tts;

    private TextView receivedText;
    private VoiceMagic voiceMagic;

    private ManageConnectedThread mmManagegedConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        //getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setTitle("Smartie!");
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initialiseTextToSpeech();
        initialiseViews();
        initialiseBluetooth();

        voiceMagic = new VoiceMagic();

    }

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    // This callback is invoked when the Speech Recognizer returns.
// This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);

            String[] MagicResponse = voiceMagic.translateTask(spokenText);

            if (MagicResponse == null)
                speech("Sorry, can you repeat this?");
            else {
                speech(voiceMagic.getVoiceResponse(MagicResponse));
                sendResponse(voiceMagic.getMessageResponse(MagicResponse));
                receivedText.setText(voiceMagic.getMessageResponse(MagicResponse));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ok_google_image:
                displaySpeechRecognizer();
                break;
            default:
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                Log.e("TTS", "This Language is supported");
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
            if (tts != null) {
                tts.stop();
                tts.shutdown();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    private void speech(String speakThis) {
        tts.speak(speakThis, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void initialiseTextToSpeech() {
        tts = new TextToSpeech(getApplicationContext(), this);
        tts.speak("Fuck Android", TextToSpeech.QUEUE_FLUSH, null);
    }

    private void initialiseViews() {
        ImageButton googleImageButton = (ImageButton) findViewById(R.id.ok_google_image);
        googleImageButton.setOnClickListener(this);
        receivedText = (TextView) findViewById(R.id.received_text_view);
    }

    private void initialiseBluetooth() {
        Intent intent = getIntent();
        String deviceAddress = intent.getStringExtra(BluetoothActivity.DEVICE_ADDRESS);

        device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
        Log.e("Device Address is", deviceAddress);
    }

    @Override
    protected void onResume() {
        super.onResume();

        UUID deviceUUID = device.getUuids()[0].getUuid();
        Log.e("UUID", deviceUUID + "");
        connectThread = new ConnectThread(device, deviceUUID);
        connectThread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        connectThread.cancel();
        try {
            connectThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendResponse(String sendWord) {
        if (connectThread.getSocket() != null && connectThread.getSocket().isConnected()) {
            mmManagegedConnection = new ManageConnectedThread(connectThread.getSocket());
            mmManagegedConnection.write(sendWord);
        }
            //mmManagegedConnection.write(string.getBytes(Charset.forName("UTF-8")));
    }
}
