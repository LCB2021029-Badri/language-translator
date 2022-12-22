package com.example.languagetranslator;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class FakeActivity extends AppCompatActivity {

    private Spinner fromSpinner,toSpinner;
    private TextInputEditText sourceEdit;
    private ImageView micTv;
    private Button translateBtn;
    private TextView translatedText;

    //language options in the spinners
    String[] fromLanguages = {"From","English","Hindi","Telugu"};
    String[] toLanguages = {"To","English","Hindi","Telugu"};

    private static final int REQUEST_PERMISSION_CODE = 1;
    int languageCode,fromLanguageCode,toLanguageCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fromSpinner = findViewById(R.id.idFromSpinner);
        toSpinner = findViewById(R.id.idToSpinner);
        sourceEdit = findViewById(R.id.idEditSource);
        micTv = findViewById(R.id.idMic);
        translateBtn = findViewById(R.id.idTranslateBtn);
        translatedText = findViewById(R.id.idTranslatedText);

        //setting up the from spinner
        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fromLanguageCode = getLanguageCode(fromLanguages[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //setting "from" adapter
        ArrayAdapter fromAdapter = new ArrayAdapter(this,R.layout.spinner_item,fromLanguages);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

        //setting up the to spinner
        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                toLanguageCode = getLanguageCode(toLanguages[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //setting "to" adapter
        ArrayAdapter toAdapter = new ArrayAdapter(this,R.layout.spinner_item,fromLanguages);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);


        //seting the translate button
        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                translatedText.setText("");
                if(sourceEdit.getText().toString().isEmpty()){
                    Toast.makeText(FakeActivity.this, "please enter a text to translate", Toast.LENGTH_SHORT).show();
                }
                else if(fromLanguageCode == 0){
                    Toast.makeText(FakeActivity.this, "please select source language", Toast.LENGTH_SHORT).show();
                }
                else if(toLanguageCode == 0){
                    Toast.makeText(FakeActivity.this, "please select type of translated language", Toast.LENGTH_SHORT).show();
                }
                else{
                    translateText(fromLanguageCode,toLanguageCode,sourceEdit.getText().toString());
                }
            }
        });

        //using mic to get text
        micTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to convert into text");
                try{
                    startActivityForResult(i,REQUEST_PERMISSION_CODE);
                }catch(Exception e){
                    e.printStackTrace();
                    Toast.makeText(FakeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    //creating a new function for the spinner to work
    public int getLanguageCode(String language){
        int languageCode = 0;
        switch (language){
            case "English":languageCode = FirebaseTranslateLanguage.EN;
            break;

            case "Hindi":languageCode = FirebaseTranslateLanguage.HI;
            break;

            case "Telugu":languageCode = FirebaseTranslateLanguage.TE;
            break;

            default:languageCode = 0;
        }
        return languageCode;
    }

    //translating the text using firebase ML model
    private void translateText(int fromLanguageCode,int toLanguageCode,String source){
        translatedText.setText("Downloading Model...");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCode)
                .build();

        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //if the model is downloaded then, call the method to translate our text
                translatedText.setText("Translating...");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        translatedText.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(FakeActivity.this, "Failed to translate", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FakeActivity.this, "Falied to download ML model", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //onactivityresult to get the final results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //check if request code is equal to the permission code
        if(requestCode==REQUEST_PERMISSION_CODE){
            if(resultCode==RESULT_OK && data!=null){
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                sourceEdit.setText(result.get(0));
            }
        }
    }
}