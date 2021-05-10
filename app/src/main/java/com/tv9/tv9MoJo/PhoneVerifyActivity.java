package com.tv9.tv9MoJo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneVerifyActivity extends AppCompatActivity {

    EditText Mobile, OTP;
    Button verify_btn;
    Button requestOTP;
    ProgressBar progressBar;
    String verificationCodeBySystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_verify_activity);

        verify_btn = findViewById(R.id.cirLoginButton);
        Mobile = (EditText)findViewById(R.id.textInputMobile);
        OTP = (EditText)findViewById(R.id.textInputPassword);
        progressBar = findViewById(R.id.progress_bar);
        requestOTP = findViewById(R.id.requestOTP);
        progressBar.setVisibility(View.GONE);
        verify_btn.setEnabled(false);


        requestOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNo = Mobile.getText().toString();
                if (Mobile.length()==10) {
                    sendVerificationCodeToUser(phoneNo);
                    progressBar.setVisibility(View.VISIBLE);
                }
                else Toast.makeText(PhoneVerifyActivity.this, "Enter valid Mobile number", Toast.LENGTH_SHORT).show();
            }
        });

        verify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (OTP.getText().toString().isEmpty()||OTP.getText().toString().length()<6)
                {
                    Toast.makeText(PhoneVerifyActivity.this, "Invalid OTP entered", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    verifyCode(OTP.getText().toString());
                }
            }
        });

    }



    private void sendVerificationCodeToUser(String phoneNo) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
               "+91" + phoneNo,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,   // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                //        If the mobile number not in the device
                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(s, forceResendingToken);
                    verificationCodeBySystem = s;
                    verify_btn.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PhoneVerifyActivity.this, "OTP sent", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    String code = phoneAuthCredential.getSmsCode();
                    if (code!=null){
                        verify_btn.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(PhoneVerifyActivity.this, "OTP sent and verified", Toast.LENGTH_SHORT).show();
                        signInTheUserByCredential(phoneAuthCredential);
                    }
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Log.d("code not sent",   e.getMessage());
                    Toast.makeText(PhoneVerifyActivity.this, "code not sent" +e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            };

    private void verifyCode(String codeByUSer) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCodeBySystem, codeByUSer);
        signInTheUserByCredential(credential);
    }

    private void signInTheUserByCredential(PhoneAuthCredential credential) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(PhoneVerifyActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful())
                        {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Toast.makeText(PhoneVerifyActivity.this, "phone not verified" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

}
