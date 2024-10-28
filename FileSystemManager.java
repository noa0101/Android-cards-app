package com.example.cards_firsttry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileSystemManager {
    private Context context;
    private FileDatabaseHelper dbHelper;

    // Constructor to initialize context and SQLite helper
    public FileSystemManager(Context context) {
        this.context = context;
        this.dbHelper = new FileDatabaseHelper(context);
    }

    // Method to check if a file exists at a given path
    public boolean doesFileExist(String filePath) {
        File file = new File(filePath);
        return file.exists(); // Returns true if file exists, false otherwise
    }

    /*
    // Method to add a new file with the given name, language, and file path
    public void addFile(String fileName, String language, String filePath) {
        System.out.println("in add file");
        System.out.println("name: " + fileName + "\nlanguage: " + language + "\nfile path: " + filePath);
        File file = new File(filePath);
        System.out.println("a");
        if (!file.exists()) {
            System.out.println("Error: File not found: " + filePath);
            return; // Exit the method if the file does not exist
        }
        System.out.println("b");


        try (BufferedReader br = new BufferedReader(new FileReader(file));
             FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)) {
            System.out.println("c");

            StringBuilder content = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
            System.out.println("d");


            if (content.length() > 0) { // Check if content is not empty
                fos.write(content.toString().getBytes(StandardCharsets.UTF_8));
                dbHelper.insertFileNameAndLanguage(fileName, language);
            } else {
                System.out.println("Warning: The file is empty: " + filePath);
            }

        } catch (FileNotFoundException e) {
            System.out.println("Error: File not found during reading: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error: IOException occurred: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: An unexpected error occurred: " + e.getMessage());
        }
    }*/

    public void addFile(String fileName, String language, String content) {
        System.out.println("b");

        try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)) { // Corrected here
            System.out.println("c");

            if (content.length() > 0) { // Check if content is not empty
                fos.write(content.getBytes(StandardCharsets.UTF_8)); // No need to call toString() on content
                dbHelper.insertFileNameAndLanguage(fileName, language);
            } else {
                System.out.println("Warning: The file is empty.");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error: File not found during writing: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error: IOException occurred: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: An unexpected error occurred: " + e.getMessage());
        }
    }


    // Method to retrieve the content of a file by name
    public String getFile(String fileName) {
        if (isFileExists(fileName)) {
            try {
                FileInputStream fis = context.openFileInput(fileName);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader bufferedReader = new BufferedReader(isr);
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                fis.close();
                return content.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null; // File doesn't exist
    }

    // Method to retrieve the language of a file by name
    public String getFileLanguage(String fileName) {
        return dbHelper.getFileLanguage(fileName);
    }

    // Method to delete a file by name
    public void deleteFile(String fileName) {
        if (isFileExists(fileName)) {
            // Delete the file from internal storage
            context.deleteFile(fileName);

            // Remove the file name from the SQLite database
            dbHelper.deleteFileName(fileName);
        }
    }

    // Method to get the list of all files
    public ArrayList<String> getAllFiles() {
        return dbHelper.getAllFileNames();
    }

    // Check if the file exists in the SQLite database
    public boolean isFileExists(String fileName) {
        return dbHelper.isFileExists(fileName);
    }

    // SQLite helper class to manage the file name and language database
    private static class FileDatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "fileManager.db";
        private static final int DATABASE_VERSION = 2;
        private static final String TABLE_NAME = "files";
        private static final String COLUMN_NAME = "fileName";
        private static final String COLUMN_LANGUAGE = "language";

        public FileDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Create table to store file names and their associated languages
            String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME + " TEXT PRIMARY KEY, " +
                    COLUMN_LANGUAGE + " TEXT)";
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 2) {
                // Add new column for language if upgrading from version 1 to 2
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_LANGUAGE + " TEXT");
            }
        }

        // Insert a new file name and its associated language into the database
        public void insertFileNameAndLanguage(String fileName, String language) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, fileName);
            values.put(COLUMN_LANGUAGE, language);
            db.insert(TABLE_NAME, null, values);
        }

        // Get the language associated with a file
        public String getFileLanguage(String fileName) {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_LANGUAGE}, COLUMN_NAME + " = ?",
                    new String[]{fileName}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String language = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LANGUAGE));
                cursor.close();
                return language;
            }
            return null;
        }

        // Check if a file name exists in the database
        public boolean isFileExists(String fileName) {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_NAME, null, COLUMN_NAME + " = ?",
                    new String[]{fileName}, null, null, null);
            boolean exists = cursor.getCount() > 0;
            cursor.close();
            return exists;
        }

        // Delete a file name from the database
        public void deleteFileName(String fileName) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_NAME, COLUMN_NAME + " = ?", new String[]{fileName});
        }

        // Get all file names from the database
        public ArrayList<String> getAllFileNames() {
            ArrayList<String> fileNames = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                fileNames.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
            }
            cursor.close();
            return fileNames;
        }
    }
}
