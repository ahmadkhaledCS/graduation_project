package com.example.proj;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    // RequestQueue for the api request
    RequestQueue requestQueue;
    Button logInButton;
    TextInputEditText idEditText;
    TextInputEditText passwordEditText;

    // SharedPreferences to save user profile
    SharedPreferences preferences;

    ConstraintLayout mainLayout;
    LinearLayout animationLayout;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainLayout=findViewById(R.id.mainLayout);

        // initializing the requestQueue
        requestQueue=Volley.newRequestQueue(this);

        logInButton=findViewById(R.id.loginButton);
        idEditText=findViewById(R.id.idEditText);
        passwordEditText=findViewById(R.id.passEditText);
        imageView=findViewById(R.id.loginImageView);

        // initializing the sharedPreferences to save profile
        preferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);


        animationLayout=findViewById(R.id.animationLayout);

        // login button logic
        logInButton.setOnClickListener(v -> {
            // view animation and run it
            openDialog();
            // connect to api to log in
            jsonParse(Objects.requireNonNull(idEditText.getText()).toString(),
                    Objects.requireNonNull(passwordEditText.getText()).toString());
        });

    }

    private void openDialog() {
        View view;
        // clear enter view
        for(int i=0;i<mainLayout.getChildCount();i++){
            view=mainLayout.getChildAt(i);
            view.setVisibility(View.GONE);
        }
        // view the animation layout that contains animation and textView
        animationLayout.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // check if user profile is available then user is lodged in
        if(preferences.getString("id",null)!=null){
            startActivity(new Intent(this,Dashboard.class));
            finish();
        }
    }

    private void jsonParse(String id, String password) {
        // api call path
        //String url=String.format("http://192.168.137.1:8000/profile/%s/%s",id,password);//phone
        String url=String.format("http://10.0.2.2:8000/profile/%s/%s",id,password);//emulator
        // constructing the get request
        JsonObjectRequest request=new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject jsonObject;
                        // if the response contains profile log in was successful
                        if(response.has("profile")){
                            jsonObject=response.getJSONObject("profile");
                            // adding the user password to object
                            jsonObject.put("regPassword",password);
                            saveData(jsonObject);
                            startActivity(new Intent(this,AboutApp.class));
                            startService(new Intent(this,SaveDataToDatabase.class));
                            finish();
                        }else{
                            Toast.makeText(this, "معلومات الحساب خاطئة", Toast.LENGTH_SHORT).show();
                            View view;
                            // restore enter view
                            for(int i=0;i<mainLayout.getChildCount();i++){
                                view=mainLayout.getChildAt(i);
                                view.setVisibility(View.VISIBLE);
                            }
                            animationLayout.setVisibility(View.INVISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, Throwable::printStackTrace);
        // configure request timeout
        request.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) {

            }
        });
        requestQueue.add(request);
    }

    public void saveData(JSONObject jsonObject){
        @SuppressLint("CommitPrefEdits")
        SharedPreferences.Editor editor=preferences.edit();
        Iterator<String> iterator = jsonObject.keys();
        // looping on response json file and saving data to sharedPreferences
        while (iterator.hasNext()) {
            String key = iterator.next();
            try {
                String value = jsonObject.get(key).toString();
                editor.putString(key,value);
            } catch (JSONException e) {
                // Something went wrong!
            }
        }
        editor.apply();
    }

}