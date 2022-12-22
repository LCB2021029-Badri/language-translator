package com.example.languagetranslator

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.languagetranslator.databinding.ActivityMainBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
//    private var fromSpinner: Spinner? = null
//    private  var toSpinner:Spinner? = null
//    private var sourceEdit: TextInputEditText? = null
//    private var micTv: ImageView? = null
//    private var translateBtn: Button? = null
//    private var translatedText: TextView? = null

    //language options in the spinners
    var fromLanguages = arrayOf("From", "English", "Hindi", "Telugu")
    var toLanguages = arrayOf("To", "English", "Hindi", "Telugu")

    private val REQUEST_PERMISSION_CODE = 1
    var languageCode = 0
    var fromLanguageCode:kotlin.Int = 0
    var toLanguageCode:kotlin.Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        fromSpinner = findViewById(R.id.idFromSpinner)
//        toSpinner = findViewById(R.id.idToSpinner)
//        sourceEdit = findViewById(R.id.idEditSource)
//        micTv = findViewById(R.id.idMic)
//        translateBtn = findViewById(R.id.idTranslateBtn)
//        translatedText = findViewById(R.id.idTranslatedText)

        //setting up the from spinner
        binding.idFromSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                fromLanguageCode = getLanguageCode(fromLanguages[i])
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        })
        //setting "from" adapter
        //setting "from" adapter
        val fromAdapter: ArrayAdapter<*> =
            ArrayAdapter<Any?>(this, R.layout.spinner_item, fromLanguages)
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.idFromSpinner.setAdapter(fromAdapter)

        //setting up the to spinner

        //setting up the to spinner
        binding.idToSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                toLanguageCode = getLanguageCode(toLanguages[i])
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        })

        //setting "to" adapter

        //setting "to" adapter
        val toAdapter: ArrayAdapter<*> =
            ArrayAdapter<Any?>(this, R.layout.spinner_item, fromLanguages)
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.idToSpinner.setAdapter(toAdapter)


        //seting the translate button


        //seting the translate button
        binding.idTranslateBtn.setOnClickListener(View.OnClickListener {
            binding.idTranslatedText.setText("")
            if (binding.idEditSource.getText().toString().isEmpty()) {
                Toast.makeText(
                    this,
                    "please enter a text to translate",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (fromLanguageCode === 0) {
                Toast.makeText(
                    this,
                    "please select source language",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (toLanguageCode === 0) {
                Toast.makeText(
                    this,
                    "please select type of translated language",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                translateText(fromLanguageCode, toLanguageCode, binding.idEditSource.getText().toString())
            }
        })

        //using mic to get text

        //using mic to get text
        binding.idMic.setOnClickListener(View.OnClickListener {
            val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            i.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to convert into text")
            try {
                startActivityForResult(i, REQUEST_PERMISSION_CODE)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        })

    }


    //creating a new function for the spinner to work
    fun getLanguageCode(language: String?): Int {
        var languageCode = 0
        languageCode = when (language) {
            "English" -> FirebaseTranslateLanguage.EN
            "Hindi" -> FirebaseTranslateLanguage.HI
            "Telugu" -> FirebaseTranslateLanguage.TE
            else -> 0
        }
        return languageCode
    }

    //translating the text using firebase ML model
    private fun translateText(fromLanguageCode: Int, toLanguageCode: Int, source: String) {
        binding.idTranslatedText!!.text = "Downloading Model..."
        val options = FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(fromLanguageCode)
            .setTargetLanguage(toLanguageCode)
            .build()
        val translator = FirebaseNaturalLanguage.getInstance().getTranslator(options)
        val conditions = FirebaseModelDownloadConditions.Builder().build()
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener { //if the model is downloaded then, call the method to translate our text
                binding.idTranslatedText!!.text = "Translating..."
                translator.translate(source).addOnSuccessListener { s -> binding.idTranslatedText!!.text = s }
                    .addOnFailureListener {
                        Toast.makeText(
                            this,
                            "Failed to translate",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }.addOnFailureListener {
                Toast.makeText(
                    this,
                    "Falied to download ML model",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    //onactivityresult to get the final results
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //check if request code is equal to the permission code
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                binding.idEditSource!!.setText(result!![0])
            }
        }
    }


}