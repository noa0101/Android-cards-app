package com.example.cards_firsttry;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Translator {
    private static final String BASE_URL = "https://libretranslate.de";
    private static final String DETECT_ENDPOINT = "/detect";
    private static final String TRANSLATE_ENDPOINT = "/translate";
    private static final OkHttpClient client = new OkHttpClient();

    // Interface for the callback to handle result asynchronously
    public interface OnLanguageCheckListener {
        void onResult(boolean isInLanguage);
    }

    public void isWordInLanguage(String word, String languageName, OnLanguageCheckListener listener) {
        String expectedLanguageCode = extractCode(languageName);
        final int maxRetries = 3; // Number of retry attempts
        final int retryDelay = 2000; // 2 seconds delay between retries

        new Thread(() -> {
            try {
                // Prepare the URL and request
                String url = BASE_URL + "/detect";
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Prepare the request body with the word
                JSONObject requestBody = new JSONObject();
                requestBody.put("q", word);

                // Send the request
                try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
                    writer.write(requestBody.toString());
                    writer.flush();
                }

                // Get the response
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Parse the response to extract the detected language
                    JSONArray detectedLanguages = new JSONArray(response.toString());
                    String detectedLanguageCode = detectedLanguages.getJSONObject(0).getString("language");

                    // Compare detected language with expected language
                    listener.onResult(expectedLanguageCode.equals(detectedLanguageCode));
                } else {
                    Log.e("Translation", "Failed to get a valid response. Response Code: " + responseCode);
                }
            } catch (Exception e) {
                Log.e("Translation", "Error detecting language", e);
            }

            // Wait before retrying
            try {
                Thread.sleep(retryDelay);
            } catch (InterruptedException e) {
                Log.e("Translation", "Retry interrupted", e);
            }

            // After retries, notify failure
            listener.onResult(false);
        }).start();
    }

    // AsyncTask for translating a word between two languages
    public void translateWord(final String word, final String fromLanguage, final String toLanguage, final OnTranslationCompleteListener listener) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String sourceLang = extractCode(fromLanguage);
                String targetLang = extractCode(toLanguage);
                try {
                    String url = BASE_URL + TRANSLATE_ENDPOINT + "?q=" + word + "&source=" + sourceLang + "&target=" + targetLang;
                    Request request = new Request.Builder().url(url).build();
                    Response response = client.newCall(request).execute();

                    if (!response.isSuccessful()) {
                        Log.e("Translation", "Error: Unexpected response code " + response);
                        return null;
                    }

                    String responseBody = response.body().string();

                    // Parse the translation result
                    JSONObject json = new JSONObject(responseBody);
                    return json.getString("translatedText");
                } catch (IOException e) {
                    Log.e("Translation", "IOException occurred: " + e.getMessage());
                } catch (Exception e) {
                    Log.e("Translation", "Error occurred: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(String translatedText) {
                listener.onTranslationComplete(translatedText);
            }
        }.execute();
    }

    // Interface to handle translation result asynchronously
    public interface OnTranslationCompleteListener {
        void onTranslationComplete(String translatedText);
    }

    public static String extractCode(String input) {
        int startIndex = input.indexOf('(') + 1;
        int endIndex = input.indexOf(')');
        return input.substring(startIndex, endIndex).trim();
    }
}
