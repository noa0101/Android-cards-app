package com.example.cards_firsttry;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class TextActivity extends AppCompatActivity {
    private String textName;
    private String content;
    private String vocabName;
    private String language;
    private VocabDatabaseManager vocabManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("in text activity");
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.activity_text);
        Intent intent = getIntent();
        language = intent.getStringExtra("language");
        textName = intent.getStringExtra("fileName");
        ((TextView)findViewById(R.id.headline)).setText(textName);
        vocabName = "My Vocabulary from text \"" + textName + "\"";
        vocabManager = new VocabDatabaseManager(TextActivity.this);
        content = intent.getStringExtra("content");
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        // Split the text into words
        String[] words = content.split(" ");
        for (String word : words) {
            SpannableString spannableString = new SpannableString(word + " ");

            // Create a new ClickableSpan with a reference to the TextView
            spannableString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    Toast.makeText(TextActivity.this, "word pressed: " + word, Toast.LENGTH_LONG).show();
                    wordPressed(word);
                }

                @Override
                public void updateDrawState(android.text.TextPaint ds) {
                    // Remove underline and set color to default
                    ds.setUnderlineText(false);
                    ds.setColor(((TextView) findViewById(R.id.content)).getCurrentTextColor()); // Use TextView color
                }
            }, 0, word.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Append each spannable word to the builder
            spannableStringBuilder.append(spannableString);
        }


        TextView textView =  findViewById(R.id.content);
        // Set the final spannable string to the TextView
        textView.setText(spannableStringBuilder);
        textView.setMovementMethod(LinkMovementMethod.getInstance());  // Make links clickable
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Navigate back to the parent activity
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void wordPressed(String word){
        String translation = word; //change!
        if(translation == null){
            Toast.makeText(TextActivity.this, "Translation for the word " + word + " could not be found.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(word)
                .setMessage(translation)
                .setPositiveButton("Add to flashcards", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!vocabManager.vocabExist(vocabName))
                            vocabManager.addVocabulary(vocabName, language);
                        vocabManager.addWordToVocab(vocabName, word);
                        Toast.makeText(TextActivity.this, "word " + word + " added to vocabulary!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Don't add to flashcards", null)
                .show();
    }
}


