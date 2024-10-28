package com.example.cards_firsttry;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class VocabActivity extends AppCompatActivity {
    private String vocabName;
    private String language;
    private Translator trans;
    private VocabDatabaseManager vocabManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocab);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView textViewWord = findViewById(R.id.headline);
        Intent intent = getIntent();
        vocabName = intent.getStringExtra("vocabName");
        language = intent.getStringExtra("language");
        textViewWord.setText(vocabName);
        trans = new Translator();
        vocabManager = new VocabDatabaseManager(VocabActivity.this);
    }

    public void eraseVocab(View view){
        new AlertDialog.Builder(this)
                .setMessage("Are you certain you wish to erase this vocabulary?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(VocabActivity.this, MainActivity.class);
                        intent.putExtra("eraseVocab", vocabName);
                        intent.putExtra("mode", "vocabs");
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void addWord(View view){
        final EditText input = new EditText(this);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(vocabName)
                .setMessage("Please enter the new word you wish to add:")
                .setView(input)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String word = input.getText().toString().trim();
                        if (!word.isEmpty()) {
                            trans.isWordInLanguage(word, language, new Translator.OnLanguageCheckListener() {
                                @Override
                                public void onResult(boolean isInLanguage) {
                                    if (isInLanguage) {
                                        vocabManager.addWordToVocab(vocabName, word);
                                        Toast.makeText(VocabActivity.this, "Word \"" + word + "\" added to vocabulary.", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(VocabActivity.this, "Word \"" + word + "\" is not found in language " + language, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("mode", "vocabs");
        setResult(RESULT_OK, intent); // Send the result back to the parent activity

        // Call the default behavior
        super.onBackPressed(); // This will finish the current activity and go back to the previous one
    }

}

