package com.example.proj;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.button.MaterialButton;

public class AboutApp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);

        MaterialButton materialButton=findViewById(R.id.materialButton);
        materialButton.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(),Dashboard.class));
            finish();
        });
    }
}