package com.example.cards_firsttry;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class LanguageSelectionActivity extends AppCompatActivity {

    private ListView languageListView;
    private List<String> languageList = new ArrayList<>();

    // Retrofit API Interface for LibreTranslate
    public interface LibreTranslateApiService {
        @GET("/languages")
        Call<List<Language>> getSupportedLanguages();
    }

    // Data class to hold language information
    public static class Language {
        private String code;
        private String name;

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selection);

        languageListView = findViewById(R.id.languageListView);

        // Fetch and display the list of languages
        fetchLanguages();

        // Handle clicks on the language list items
        languageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedLanguage = languageList.get(position);

                // Pass the selected language back to the MainActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selectedLanguage", selectedLanguage);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    private void fetchLanguages() {
        // Create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://libretranslate.com/")
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .build();

        LibreTranslateApiService apiService = retrofit.create(LibreTranslateApiService.class);

        // Make the network call
        Call<List<Language>> call = apiService.getSupportedLanguages();
        call.enqueue(new Callback<List<Language>>() {
            @Override
            public void onResponse(Call<List<Language>> call, retrofit2.Response<List<Language>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Language> languages = response.body();
                    for (Language lang : languages) {
                        languageList.add(lang.getName() + " (" + lang.getCode() + ")");
                    }

                    // Set the adapter for the ListView
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(LanguageSelectionActivity.this,
                            android.R.layout.simple_list_item_1, languageList);
                    languageListView.setAdapter(adapter);
                } else {
                    Log.d("API Response", response.errorBody().toString());
                    Toast.makeText(LanguageSelectionActivity.this, "Failed to fetch languages", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Language>> call, Throwable t) {
                Toast.makeText(LanguageSelectionActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
