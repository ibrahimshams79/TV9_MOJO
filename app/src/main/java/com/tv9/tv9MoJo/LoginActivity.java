package com.tv9.tv9MoJo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    Button loginButton;
    EditText mobileNo, password;
    TextView signup_in_login;
    boolean CheckEditText=false;
    String Password_Str, MobileNo_Str, finalResult;
    ProgressDialog progressDialog;
    HashMap<String,String> hashMap = new HashMap<>();

    String HttpURL = "http://192.168.3.81/LoginRegister/login.php";
    HttpParse httpParse = new HttpParse();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.loginButton);
        mobileNo = findViewById(R.id.loginTextMobile);
        password = findViewById(R.id.loginTextPassword);

        signup_in_login = findViewById(R.id.signup_in_login);

        //Adding Click Listener on button.
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Checking whether EditText is Empty or Not
                CheckEditTextIsEmptyOrNot();

                if (CheckEditText) {

                    // If EditText is not empty and CheckEditText = True then this block will execute.

                    UserLoginFunction(MobileNo_Str, Password_Str);

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
            Toast.makeText(LoginActivity.this, ""+ CheckEditText, Toast.LENGTH_LONG).show();

            CheckEditText = false;

        } else {
            Toast.makeText(LoginActivity.this, ""+ CheckEditText, Toast.LENGTH_LONG).show();

            CheckEditText = true;
        }

    }

    public void UserLoginFunction(final String mobileNo, final String password) {

        class UserLoginFunctionClass extends AsyncTask<String, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                progressDialog = ProgressDialog.show(LoginActivity.this, "Signing in...", null, true, true);
            }

            @Override
            protected void onPostExecute(String httpResponseMsg) {

                super.onPostExecute(httpResponseMsg);

                progressDialog.dismiss();

                if (finalResult=="Login Success") {

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    Toast.makeText(LoginActivity.this, httpResponseMsg, Toast.LENGTH_LONG).show();
                }
                else
                    Toast.makeText(LoginActivity.this, httpResponseMsg, Toast.LENGTH_LONG).show();
            }

            @Override
            protected String doInBackground(String... params) {

                hashMap.put("rep_phoneno", params[0]);

                hashMap.put("rep_password", params[1]);

                finalResult = httpParse.postRequest(hashMap, HttpURL);

                return finalResult;
            }
        }

        UserLoginFunctionClass UserLoginFunction = new UserLoginFunctionClass();

        UserLoginFunction.execute(mobileNo, password);
    }

}
