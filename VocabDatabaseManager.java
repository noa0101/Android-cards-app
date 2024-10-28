package com.example.cards_firsttry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class VocabDatabaseManager extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "vocabulary.db";
    private static final int DATABASE_VERSION = 1;

    // Table and column names
    private static final String TABLE_VOCABULARIES = "vocabularies";
    private static final String COLUMN_VOCAB_NAME = "vocab_name";
    private static final String COLUMN_VOCAB_LANGUAGE = "vocab_language";
    private static final String COLUMN_VOCAB_WORDS = "vocab_words";

    public VocabDatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create vocabularies table
        String createTable = "CREATE TABLE " + TABLE_VOCABULARIES + " (" +
                COLUMN_VOCAB_NAME + " TEXT PRIMARY KEY, " +
                COLUMN_VOCAB_LANGUAGE + " TEXT, " +
                COLUMN_VOCAB_WORDS + " TEXT" + ")";
        db.execSQL(createTable);

        // Create an index on the vocabulary name for faster lookups
        String createIndex = "CREATE INDEX idx_vocab_name ON " + TABLE_VOCABULARIES + " (" + COLUMN_VOCAB_NAME + ")";
        db.execSQL(createIndex);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the old table and create a new one
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VOCABULARIES);
        onCreate(db);
    }

    // Method to add a vocabulary
    public void addVocabulary(String vocabName, String vocabLanguage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_VOCAB_NAME, vocabName);
        values.put(COLUMN_VOCAB_LANGUAGE, vocabLanguage);
        values.put(COLUMN_VOCAB_WORDS, "");
        db.insert(TABLE_VOCABULARIES, null, values);
        db.close();
    }

    // Method to add a word to a vocabulary
    public void addWordToVocab(String vocabName, String word) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_VOCAB_WORDS + " FROM " + TABLE_VOCABULARIES +
                " WHERE " + COLUMN_VOCAB_NAME + " = ?", new String[]{vocabName});
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(COLUMN_VOCAB_WORDS);

            if (columnIndex != -1) { // Ensure the column exists
                String currentWords = cursor.getString(columnIndex); // Get the words string from the column
                if (currentWords != null) {
                    currentWords += "," + word; // Add the new word
                } else {
                    currentWords = word; // Handle the case where there are no words yet
                }

                ContentValues values = new ContentValues();
                values.put(COLUMN_VOCAB_WORDS, currentWords);
                db.update(TABLE_VOCABULARIES, values, COLUMN_VOCAB_NAME + " = ?", new String[]{vocabName});
            }
            cursor.close();
        }
    }

    // Method to get all words from a vocabulary
    public ArrayList<String> getWordsByVocabularyName(String vocabName) {
        ArrayList<String> words = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + COLUMN_VOCAB_WORDS + " FROM " + TABLE_VOCABULARIES +
                " WHERE " + COLUMN_VOCAB_NAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{vocabName});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(COLUMN_VOCAB_WORDS);
                if (columnIndex != -1) {
                    String wordsString = cursor.getString(columnIndex);
                    if (wordsString != null) {
                        String[] wordsArray = wordsString.split(","); // Assuming words are comma-separated
                        Collections.addAll(words, wordsArray);
                    }
                }
            }
            cursor.close();
        }
        db.close();
        return words;
    }

    public boolean vocabExist(String vocabName){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT 1 FROM " + TABLE_VOCABULARIES +
                " WHERE " + COLUMN_VOCAB_NAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{vocabName});
        boolean exists = false;
        if (cursor != null) {
            exists = cursor.moveToFirst();  // Check if there's at least one result
            cursor.close();
        }
        return exists;
    }

    public ArrayList<String> getAllVocabularyNames() {
        ArrayList<String> vocabNames = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + COLUMN_VOCAB_NAME + ", " + COLUMN_VOCAB_LANGUAGE + " FROM " + TABLE_VOCABULARIES;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int nameIndex = cursor.getColumnIndex(COLUMN_VOCAB_NAME);
                int languageIndex = cursor.getColumnIndex(COLUMN_VOCAB_LANGUAGE);

                if (nameIndex != -1 && languageIndex != -1) {
                    String vocabName = cursor.getString(nameIndex);
                    String vocabLanguage = cursor.getString(languageIndex);
                    String formattedString = vocabName;// + " (" + vocabLanguage + ")";
                    vocabNames.add(formattedString);
                }
            }
            cursor.close();
        }
        db.close();
        return vocabNames;
    }

    public void deleteVocabulary(String vocabName) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_VOCABULARIES, COLUMN_VOCAB_NAME + " = ?", new String[]{vocabName});
        db.close(); // Close the database connection
    }

    // Method to get the language by vocabulary name
    public String getLanguageByVocabularyName(String vocabName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String language = null;

        Cursor cursor = db.query(TABLE_VOCABULARIES,
                new String[]{COLUMN_VOCAB_LANGUAGE},
                COLUMN_VOCAB_NAME + " = ?",
                new String[]{vocabName},
                null, null, null);

        // Check if a record was found
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int languageIndex = cursor.getColumnIndex(COLUMN_VOCAB_LANGUAGE);
                    if (languageIndex != -1) {
                        language = cursor.getString(languageIndex); // Get the language
                    }
                }
            } finally {
                cursor.close(); // Ensure the cursor is closed in the finally block
            }
        }
        db.close(); // Close the database connection
        return language; // Return the language or null if not found
    }

}
