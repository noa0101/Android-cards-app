package com.example.cards_firsttry;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        TextView textViewWord = findViewById(R.id.textView);
        String word = getIntent().getStringExtra("word");
        textViewWord.setText(word);
    }
}

