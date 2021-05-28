package com.tv9.tv9MoJo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.dynamic.IFragmentWrapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;
    Button loginButton;
    EditText mobileNo, password;
    TextView signup_in_login;
    boolean CheckEditText=false;
    String Password_Str, MobileNo_Str;
//    ProgressDialog progressDialog;
//    HashMap<String,String> hashMap = new HashMap<>();
    SharedPreferences sharedPreferences;

    String HttpURL = "http://192.168.0.104/LoginRegister/login.php";
//    HttpParse httpParse = new HttpParse();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.loginButton);
        mobileNo = findViewById(R.id.loginTextMobile);
        password = findViewById(R.id.loginTextPassword);

        signup_in_login = findViewById(R.id.signup_in_login);
        sharedPreferences = getSharedPreferences("SHARED_PREF", MODE_PRIVATE);

        String phoneno = sharedPreferences.getString("PHONENO", MobileNo_Str);
        String pass = sharedPreferences.getString("PASSWORD", Password_Str);

        if (phoneno!=null && pass!=null){
            Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(), InitialActivity.class);
            startActivity(intent);
        }

        //Adding Click Listener on button.
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Checking whether EditText is Empty or Not
                CheckEditTextIsEmptyOrNot();

                if (CheckEditText) {

                    // If EditText is not empty and CheckEditText = True then this block will execute.

//                    UserLoginFunction(MobileNo_Str, Password_Str);
                    MobileNo_Str = mobileNo.getText().toString();
                    Password_Str = password.getText().toString();

                    new AsyncLogin().execute(MobileNo_Str, Password_Str);

                } else {

                    // If EditText is empty then this block will execute .
                    Toast.makeText(LoginActivity.this, "Please fill all form fields.", Toast.LENGTH_LONG).show();

                }


            }
        });

//        loginButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//
//            }
//        });

        signup_in_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }



    public void CheckEditTextIsEmptyOrNot() {
        MobileNo_Str = mobileNo.getText().toString();
        Password_Str = password.getText().toString();

        if (TextUtils.isEmpty(MobileNo_Str) || TextUtils.isEmpty(Password_Str)) {

            CheckEditText = false;

        } else {

            CheckEditText = true;
        }

    }

//    public void UserLoginFunction(final String mobileNo, final String password) {
//
//        class UserLoginFunctionClass extends AsyncTask<String, Void, String> {
//
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//
//                progressDialog = ProgressDialog.show(LoginActivity.this, "Signing in...", null, true, true);
//            }
//
//            @Override
//            protected void onPostExecute(String httpResponseMsg) {
//
//                super.onPostExecute(httpResponseMsg);
//
//                progressDialog.dismiss();
//
//                if (finalResult=="Login Success") {
//
//                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                    startActivity(intent);
//                    Toast.makeText(LoginActivity.this, httpResponseMsg, Toast.LENGTH_LONG).show();
//                }
//                else
//                    Toast.makeText(LoginActivity.this, httpResponseMsg, Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            protected String doInBackground(String... params) {
//
//                hashMap.put("rep_phoneno", params[0]);
//
//                hashMap.put("rep_password", params[1]);
//
//                finalResult = httpParse.postRequest(hashMap, HttpURL);
//
//                return finalResult;
//            }
//        }
//
//        UserLoginFunctionClass UserLoginFunction = new UserLoginFunctionClass();
//
//        UserLoginFunction.execute(mobileNo, password);
//    }

    private class AsyncLogin extends AsyncTask<String, String, String>
    {
        ProgressDialog loading = new ProgressDialog(LoginActivity.this);
        HttpURLConnection connection;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading.setMessage("\tLoading...");
            loading.setCancelable(false);
            loading.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                url = new URL(HttpURL);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "DoInBackground Exception1";
            }
            try {
                connection = (HttpURLConnection)url.openConnection();
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("rep_phoneno", params[0])
                        .appendQueryParameter("rep_password", params[1]);
                String query = builder.build().getEncodedQuery();

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                connection.connect();
            } catch (IOException e1) {
                e1.printStackTrace();
                return "DoInBackground exception2";
            }

            try {
                int response_code = connection.getResponseCode();

                if (response_code == HttpURLConnection.HTTP_OK) {
                    InputStream input = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    return(result.toString());
                }else{
                    return("unsuccessful");
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "DoInBackground exception3";
            } finally {
                connection.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            loading.dismiss();

            if(result.equalsIgnoreCase("true"))
            {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("PHONENO", MobileNo_Str);
                editor.putString("PASSWORD", Password_Str);
                editor.apply();

                Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), InitialActivity.class);
                startActivity(intent);
                finish();
            } else if (result.equalsIgnoreCase("false")){
                Toast.makeText(getApplicationContext(), "Invalid Email or Password.", Toast.LENGTH_LONG).show();
            } else{
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            }
        }
    }

}
