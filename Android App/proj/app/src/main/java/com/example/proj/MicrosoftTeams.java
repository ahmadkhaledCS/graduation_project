package com.example.proj;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MicrosoftTeams extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_microsoft_teams);

        SharedPreferences preferences=getSharedPreferences("UserProfile", Context.MODE_PRIVATE);

        Button logInToTeams=findViewById(R.id.logInToTeams);

        TextInputEditText emailEditText=findViewById(R.id.emailTextInputEditText);
        TextInputEditText passEditText=findViewById(R.id.passTextInputEditText);
        emailEditText.setText(preferences.getString("email","none"));
        passEditText.setText(preferences.getString("emailPassword","none"));


        logInToTeams.setOnClickListener(v -> {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.microsoft.teams");
            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            }else{
                final String appPackageName = "com.microsoft.teams"; // getPackageName() from Context or Activity object
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        });
    }
}