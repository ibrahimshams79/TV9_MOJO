package com.tv9.tv9MoJo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.vishnusivadas.advanced_httpurlconnection.PutData;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    Button register, log_in;
    EditText Full_Name, User_Name, Password, Mobile;
    String FullName, UserName, MobileHolder, PasswordHolder;
    //    String finalResult;
    String HttpURL = "http://192.168.3.81/LoginRegister/signup.php";
    ProgressBar progressBar;
//    Boolean CheckEditText;
//    ProgressDialog progressDialog;
//    HashMap<String, String> hashMap = new HashMap<>();
//    HttpParse httpParse = new HttpParse();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Assign Id'S
        Full_Name = (EditText) findViewById(R.id.editTextF_Name);
        User_Name = (EditText) findViewById(R.id.editUser_Name);
        Mobile = (EditText) findViewById(R.id.editTextMobile);

        Password = (EditText) findViewById(R.id.editTextPassword);

        register = (Button) findViewById(R.id.registerButton);
        log_in = (Button) findViewById(R.id.verifyMobileButton);
        progressBar = findViewById(R.id.regprogress);

        //Adding Click Listener on button.
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullName = Full_Name.getText().toString();
                UserName = User_Name.getText().toString();
                PasswordHolder = Password.getText().toString();
                MobileHolder = Mobile.getText().toString();

                // Checking whether EditText is Empty or Not
//                CheckEditTextIsEmptyOrNot();

                if (TextUtils.isEmpty(FullName) || TextUtils.isEmpty(UserName) || TextUtils.isEmpty(MobileHolder) || TextUtils.isEmpty(PasswordHolder)) {

                    // If EditText is not empty and CheckEditText = True then this block will execute.

//                    UserRegisterFunction(FullName, UserName, MobileHolder, PasswordHolder);

                    // If EditText is empty then this block will execute .
                    Toast.makeText(RegisterActivity.this, "Please fill all form fields.", Toast.LENGTH_LONG).show();

                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //Starting Write and Read data with URL
                            //Creating array for parameters
                            String[] field = new String[4];
                            field[0] = "fullname";
                            field[1] = "username";
                            field[2] = "mobileno";
                            field[3] = "password";
                            //Creating array for data
                            String[] data = new String[4];
                            data[0] = "FullName";
                            data[1] = "UserName";
                            data[2] = "MobileHolder";
                            data[3] = "PasswordHolder";
                            PutData putData = new PutData(HttpURL, "POST", field, data);
                            if (putData.startPut()) {
                                if (putData.onComplete()) {
                                    progressBar.setVisibility(View.GONE);
                                    String result = putData.getResult();
                                    //End ProgressBar (Set visibility to GONE)
                                    Log.i("PutData", result);
                                    if (result.equals("Sign Up Success")) {
                                        Toast.makeText(RegisterActivity.this, result, Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();

                                    } else {
                                        Toast.makeText(RegisterActivity.this, result, Toast.LENGTH_LONG).show();

                                    }
                                }
                            }
                            //End Write and Read data with URL
                        }
                    });
                }
            }
        });

        log_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);

            }
        });

    }

//    public void CheckEditTextIsEmptyOrNot(){
//
//        FullName = Full_Name.getText().toString();
//        UserName = User_Name.getText().toString();
//        PasswordHolder = Password.getText().toString();
//        MobileHolder = Mobile.getText().toString();
//
//        if(TextUtils.isEmpty(FullName) || TextUtils.isEmpty(UserName) || TextUtils.isEmpty(MobileHolder) || TextUtils.isEmpty(PasswordHolder))
//        {
//
//            CheckEditText = false;
//
//        }
//        else {
//
//            CheckEditText = true ;
//        }
//
//    }

//    public void UserRegisterFunction(final String FullName, final String UserName, final String phone, final String password){
//
//        class UserRegisterFunctionClass extends AsyncTask<String,Void,String> {
//
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//
//                progressDialog = ProgressDialog.show(RegisterActivity.this,"Signing you up...",null,true,true);
//            }
//
//            @Override
//            protected void onPostExecute(String httpResponseMsg) {
//
//                super.onPostExecute(httpResponseMsg);
//
//                progressDialog.dismiss();
//
//                Toast.makeText(RegisterActivity.this,httpResponseMsg.toString(), Toast.LENGTH_LONG).show();
//
//            }
//
//            @Override
//            protected String doInBackground(String... params) {
//
//                hashMap.put("rep_name",params[0]);
//
//                hashMap.put("user_name",params[1]);
//
//                hashMap.put("rep_phoneno",params[2]);
//
//                hashMap.put("rep_password",params[3]);
//
//                finalResult = httpParse.postRequest(hashMap, HttpURL);
//
//                return finalResult;
//            }
//        }
//
//        UserRegisterFunctionClass userRegisterFunctionClass = new UserRegisterFunctionClass();
//
//        userRegisterFunctionClass.execute(FullName, UserName, phone, password);
//    }

}