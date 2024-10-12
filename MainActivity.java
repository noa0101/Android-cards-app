package com.example.cards_firsttry;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int LANGUAGE_REQUEST_CODE = 1;

    // Reference to the TextView
    private EditText editTextWord;
    private ListView vocabList;
    private ArrayList<String> wordsList;
    private ArrayAdapter<String> adapter;

    // Language text
    private TextView selectedLanguageView;
    private String selectedLanguage = "No language selected";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectedLanguageView = findViewById(R.id.languageSpecifier);  // Add this TextView to your XML layout
        vocabList = findViewById(R.id.vocabList);
        wordsList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, wordsList);
        vocabList.setAdapter(adapter);

        vocabList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                goto_vocab(parent, view, position, id); // Call the method to go to SecondActivity
            }
        });

        // Set initial language text
        selectedLanguageView.setText(selectedLanguage);
    }

    public void add(View view) {
        final EditText input = new EditText(this);

        new AlertDialog.Builder(this)
                .setTitle("Choose a Name")
                .setMessage("Please enter your name:")
                .setView(input)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = input.getText().toString().trim();
                        if (!name.isEmpty()) {
                            wordsList.add(name);
                            adapter.notifyDataSetChanged();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void goto_vocab(AdapterView<?> parent, View view, int position, long id) {
        String selectedWord = wordsList.get(position);
        Intent intent = new Intent(MainActivity.this, SecondActivity.class);
        intent.putExtra("word", selectedWord);
        startActivity(intent);
    }

    public void set_language(View view) {
        Intent intent = new Intent(MainActivity.this, LanguageSelectionActivity.class);
        startActivityForResult(intent, LANGUAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LANGUAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("selectedLanguage")) {
                selectedLanguage = data.getStringExtra("selectedLanguage");
                selectedLanguageView.setText(selectedLanguage);
            }
        }
    }
}
