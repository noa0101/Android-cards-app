package com.example.cards_firsttry;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int NATIVE_LANGUAGE_REQUEST_CODE = 1;
    private static final int VOCAB_LANGUAGE_REQUEST_CODE = 2;
    private static final int TEXT_LANGUAGE_REQUEST_CODE = 3;
    private static final int VOCAB_MODE = 0;
    private static final int TEXTS_MODE = 1;

    // Reference to the TextView
    private EditText editTextWord;
    private ListView listView;
    private ArrayList<String> wordsList;
    private ArrayAdapter<String> adapter;
    private String nativeLang = "English (en)";
    VocabDatabaseManager vocabManager;
    FileSystemManager filesManager;
    private int mode = VOCAB_MODE;
    // Language text
    private TextView selectedLanguageView;
    private TextView headline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectedLanguageView = findViewById(R.id.languageSpecifier);  // Add this TextView to your XML layout
        listView = findViewById(R.id.list);
        vocabManager = new VocabDatabaseManager(this);
        filesManager = new FileSystemManager(this);
        wordsList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, wordsList);
        listView.setAdapter(adapter);
        headline = findViewById(R.id.headline);
        goToTexts(null);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listPressed(position); // Call the method to go to SecondActivity
            }
        });
        // Set initial language text
        selectedLanguageView.setText("currecnt: " + nativeLang);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("eraseVocab")) {
            String vocabName = intent.getStringExtra("eraseVocab"); // Replace with your key
            eraseVocab(vocabName);
        }
        if(intent != null && intent.hasExtra("mode") && intent.getStringExtra("mode").equals("vocabs"))
            goToVocabs(null);
        if(intent != null && intent.hasExtra("eraseVocab"))
            eraseVocab(intent.getStringExtra("eraseVocab"));
    }

    private void eraseVocab(String vocabName) {
        vocabManager.deleteVocabulary(vocabName); // Call the delete method
        wordsList.remove(vocabName); // Update the displayed list
        adapter.notifyDataSetChanged(); // Notify the adapter about data changes
        Toast.makeText(this, "Deleted " + vocabName, Toast.LENGTH_SHORT).show();
    }

    public void addVocab() {
        final EditText input = new EditText(this);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Choose a Name")
                .setMessage("Please enter your vocabulary's name:")
                .setView(input)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString().trim();
                if (!name.isEmpty()) {
                    //check that name is not already in use
                    if(vocabManager.vocabExist(name)){
                        Toast.makeText(MainActivity.this, "Vocab with name " + name + " already exists", Toast.LENGTH_SHORT).show();
                    }
                    else
                        getVocabLng(name);
                }
                else{
                    Toast.makeText(MainActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        })
        .setNegativeButton("Cancel", null)
        .show();
    }

    private void getVocabLng(String vocabName) {
        new AlertDialog.Builder(this)
                .setMessage("Please select the language for this vocabulary.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this, LanguageSelectionActivity.class);
                        intent.putExtra("vocabName", vocabName);
                        startActivityForResult(intent, VOCAB_LANGUAGE_REQUEST_CODE);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void listPressed(int position){
        String selectedWord = wordsList.get(position);
        if(mode == VOCAB_MODE)
            gotoVocab(selectedWord);
        else
            gotoText(selectedWord);
    }

    public void gotoText(String name){
        Intent intent = new Intent(MainActivity.this, TextActivity.class);
        intent.putExtra("fileName", name);
        startActivity(intent);
    }

    public void gotoVocab(String name) {
        Intent intent = new Intent(MainActivity.this, VocabActivity.class);
        intent.putExtra("vocabName", name);
        intent.putExtra("language", vocabManager.getLanguageByVocabularyName(name));
        startActivity(intent);
    }

    public void addText(){
        final EditText input = new EditText(this);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Choose a Name")
                .setMessage("Please enter the new text's name:")
                .setView(input)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = input.getText().toString().trim();
                        if (!name.isEmpty()) {
                            if(filesManager.isFileExists(name)){
                                Toast.makeText(MainActivity.this, "File with name " + name + " already exists.", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                getFilePath(name);
                            }
                        }
                        else{
                            Toast.makeText(MainActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void getFilePath(String filename){
        final EditText input = new EditText(this);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Choose a File")
                .setMessage("Please enter your desired file's path:")
                .setView(input)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String path = input.getText().toString().trim();
                        if (!path.isEmpty()) {
                            if(!filesManager.doesFileExist(path)){
                                Toast.makeText(MainActivity.this, "File was not found on your device", Toast.LENGTH_LONG).show();
                            }
                            else{
                                getFileLng(filename, path);
                            }
                        }

                        else{
                            Toast.makeText(MainActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void getFileLng(String name, String path) {
        System.out.println("in getFileLng");
        System.out.println("name: " + name + "\npath: " + path);
        new AlertDialog.Builder(this)
                .setMessage("Please specify the language of the story.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this, LanguageSelectionActivity.class);
                        intent.putExtra("textName", name);
                        intent.putExtra("filePath", path);
                        startActivityForResult(intent, TEXT_LANGUAGE_REQUEST_CODE);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void add(View view){
        if(mode == VOCAB_MODE)
            addVocab();
        else
            addText();
    }


    public void set_language(View view) {
        Intent intent = new Intent(MainActivity.this, LanguageSelectionActivity.class);
        startActivityForResult(intent, NATIVE_LANGUAGE_REQUEST_CODE);
    }

    public void goToVocabs(View view){
        if(mode == VOCAB_MODE)
            return;
        mode = VOCAB_MODE;
        wordsList.clear();
        wordsList.addAll(vocabManager.getAllVocabularyNames());
        adapter.notifyDataSetChanged();
        headline.setText("My Vocabularies List:");
    }

    public void goToTexts(View view){
        if(mode == TEXTS_MODE)
            return;
        mode = TEXTS_MODE;
        wordsList.clear();
        wordsList.addAll(filesManager.getAllFiles());
        adapter.notifyDataSetChanged();
        headline.setText("My Downloaded Texts:");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String selectedLanguage;
        if (resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("selectedLanguage")) {
                selectedLanguage = data.getStringExtra("selectedLanguage");
                if(requestCode==NATIVE_LANGUAGE_REQUEST_CODE) {
                    nativeLang = selectedLanguage;
                    selectedLanguageView.setText("current: " + nativeLang);
                }
                else if(requestCode==VOCAB_LANGUAGE_REQUEST_CODE){
                    String name = data.getStringExtra("vocabName");
                    if (name == null) {
                        Toast.makeText(this, "Error: vocabulary name is missing", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        vocabManager.addVocabulary(name, selectedLanguage);
                        wordsList.add(name);
                        adapter.notifyDataSetChanged();
                    }
                }
                else if(requestCode==TEXT_LANGUAGE_REQUEST_CODE){
                    System.out.println("in activity");
                    String name = data.getStringExtra("textName");
                    String path = data.getStringExtra("filePath");

System.out.println("1");
                    filesManager.addFile(name, selectedLanguage, path);
                    System.out.println("2");

                    wordsList.add(name);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }
}