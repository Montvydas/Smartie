package com.monklu.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.monklu.bluetooth.com.monklu.bluetoothConnect.ConnectThread;
import com.monklu.bluetooth.com.monklu.bluetoothConnect.ManageConnectedThread;

import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by montuxxx on 16/02/16.
 */
public class SendActivity extends Activity implements View.OnClickListener, TextToSpeech.OnInitListener {
    private static final int SPEECH_REQUEST_CODE = 0;

    private String [] magicWords;
    private String [] magicResponse;
    private String speakThis;
    public BluetoothDevice device;
    public ConnectThread connectThread;
    private ManageConnectedThread mmManagegedConnection;

    private TextToSpeech tts;
    private  ArrayList<String> magicList;

    TextView receivedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        //getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setTitle("Smartie!");

        initialiseMagic();
        initialiseTextToSpeech();
        initialiseViews();
        initialiseBluetooth();

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
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            speakThis = translateTask(spokenText);
            if (speakThis != null)
                speech(speakThis);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ok_google_image:
                displaySpeechRecognizer();
                break;
//            case R.id.send_data_button:
//                String string = "Hello";
//                if (connectThread.getSocket() != null && connectThread.getSocket().isConnected()){
//                    ManageConnectedThread mmManagegedConnection = new ManageConnectedThread(connectThread.getSocket());
//                    //mmManagegedConnection.write(string.getBytes(Charset.forName("UTF-8")));
//                   // mmManagegedConnection.write("Hello");
//                }
//                //SEND DATA IN HERE
//                break;
//            case R.id.receive_data_button:
//                //RECEIVE DATA HERE
//                if (connectThread.getSocket() != null && connectThread.getSocket().isConnected()){
//                    ManageConnectedThread mmManagegedConnection = new ManageConnectedThread(connectThread.getSocket());
//                    mmManagegedConnection.run();
//                }
//                receivedText.setText("RECEIVED TEXT!!!!");
//                break;
            default:
        }
    }

    public String translateTask(String spokenText) {

        String[] extractedWords = spokenText.split(" ");
        //Toast.makeText(this, spokenText ,Toast.LENGTH_SHORT).show();
        Log.e("fullText", spokenText);

        boolean isFunction = true;

        //check if it's not empty
        if (spokenText == null || spokenText.isEmpty())
            isFunction = false;

        Log.e("after spokenText", "" + isFunction);

        //check for magical command
        if (isFunction) {
            for (String singleCommand : magicList) {
                magicWords = singleCommand.split(" ");
                isFunction = true;
                for (int i = 0; i < magicWords.length; i++) {
                    if (spokenText.contains(magicWords[i])) {
                        Log.e("contains", magicWords[i]);
                        magicResponse[i] = magicWords[i];
                    } else isFunction = false;
                }
                if (isFunction) break;
            }
        }

        Log.e("after magicWord", "" + isFunction);

        //Check for number
        if (isFunction && (!magicResponse[0].equalsIgnoreCase("get") || !magicResponse[0].equalsIgnoreCase("what")) ) {
            for (String str : extractedWords) {
                if (TextUtils.isDigitsOnly(str)) {
                    magicResponse[3] = str;
                    isFunction = true;
                    Log.e("number is", "" + str);
                    break;
                } else isFunction = false;
            }
        }

        Log.e("after Number", "" + isFunction);

       /* if (isFunction) {
            for (int i = 0; i < magicResponse.length; i++)
                Log.e("" + i + "", magicResponse[i]);
        }*/


        if (isFunction){
            String tmp = null;

            if (magicResponse[0].equalsIgnoreCase("set")){
                tmp = magicResponse[0] + "ting the " + magicResponse[1] + " in the " + magicResponse[2] + " to be " + magicResponse[3] + " degrees";
            } else if (magicResponse[0].equalsIgnoreCase("get") || magicResponse[0].equalsIgnoreCase("what") ){
                tmp = magicResponse[1] + " in the " + magicResponse[2] + " is " + "20" + " degrees";
            } else if (magicResponse[0].equalsIgnoreCase("make")){
                tmp = "making the" + magicResponse[1] + " on the " + magicResponse[2] + " to be " + magicResponse[3] + " degrees";
            }

            if (connectThread.getSocket() != null && connectThread.getSocket().isConnected()){
                ManageConnectedThread mmManagegedConnection = new ManageConnectedThread(connectThread.getSocket());
                //mmManagegedConnection.write(string.getBytes(Charset.forName("UTF-8")));
                if (magicResponse[0].equalsIgnoreCase("set") || magicResponse[0].equalsIgnoreCase("make")){
                    String sendStuff = "s" + magicResponse[3];
                    mmManagegedConnection.write(sendStuff);
                } else if (magicResponse[0].equalsIgnoreCase("get") || magicResponse[0].equalsIgnoreCase("what") ){
                    String sendStuff = "g";
                    mmManagegedConnection.write(sendStuff);
                }
            }
            return tmp;
        }
        else{
            return "Sorry, could you repeat that?";
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
                speech(speakThis);
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
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
        //tts.setPitch((float) pitch);
        //tts.setSpeechRate((float) speed);
        tts.speak(speakThis, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void initialiseMagic () {
        magicWords = new String[2];
        magicResponse = new String[4];

        magicList = new ArrayList<String>();
        magicList.add("set temperature kitchen");
        magicList.add("get temperature kitchen");
        magicList.add("what temperature kitchen");
        magicList.add("set light kitchen");
        magicList.add("get light kitchen");
        magicList.add("what light kitchen");
        magicList.add("set temperature bathroom");
        magicList.add("get temperature bathroom");
        magicList.add("what temperature bathroom");
        magicList.add("set light bathroom");
        magicList.add("get light bathroom");
        magicList.add("what light bathroom");
        magicList.add("make temperature outside");
    }
    public void initialiseTextToSpeech(){
        tts = new TextToSpeech(getApplicationContext(), this);
        tts.speak("Fuck Android", TextToSpeech.QUEUE_FLUSH, null);
    }
    public void initialiseViews (){

        ImageButton googleImageButton = (ImageButton) findViewById(R.id.ok_google_image);
        googleImageButton.setOnClickListener(this);
//        Button googleButton = (Button) findViewById(R.id.ok_google_button);
//        googleButton.setOnClickListener(this);
//        Button sendData = (Button) findViewById(R.id.send_data_button);
//        sendData.setOnClickListener(this);
//        Button receiveData = (Button) findViewById(R.id.receive_data_button);
//        receiveData.setOnClickListener(this);
        receivedText = (TextView) findViewById(R.id.received_text_view);
    }
    public void initialiseBluetooth (){

        Intent intent = getIntent();
        String deviceAddress = intent.getStringExtra(BluetoothActivity.DEVICE_ADDRESS);

        device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
        Log.e("Device Address is", deviceAddress);

        UUID deviceUUID = device.getUuids()[0].getUuid();
        Log.e("UUID", deviceUUID + "");

        connectThread = new ConnectThread(device, deviceUUID);
        connectThread.run();
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


}
