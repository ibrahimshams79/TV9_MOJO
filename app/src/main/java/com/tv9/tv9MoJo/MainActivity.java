package com.tv9.tv9MoJo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.tv9.tv9MoJo.networking.ApiConfig;
import com.tv9.tv9MoJo.networking.AppConfig;
import com.tv9.tv9MoJo.networking.ServerResponse;

import java.util.HashMap;
import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageButton image, video, audio, pdf;
    private Button submitStory;
    private EditText story, descWithText;
    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image =  findViewById(R.id.image);
        video =  findViewById(R.id.video);
        audio =  findViewById(R.id.audio);
        pdf =  findViewById(R.id.pdf);

        story = findViewById(R.id.storyWithText);
        descWithText = findViewById(R.id.descriptionWithText);
        submitStory = findViewById(R.id.submitStory);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initDialog();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            image.setEnabled(false);
            video.setEnabled(false);
            audio.setEnabled(false);
            pdf.setEnabled(false);
            submitStory.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        } else {
            image.setEnabled(true);
            video.setEnabled(true);
            audio.setEnabled(true);
            pdf.setEnabled(true);
            submitStory.setEnabled(true);
        }
        image.setOnClickListener(this);
        video.setOnClickListener(this);
        audio.setOnClickListener(this);
        pdf.setOnClickListener(this);
        submitStory.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                image.setEnabled(true);
                video.setEnabled(true);
                audio.setEnabled(true);
                pdf.setEnabled(true);
                submitStory.setEnabled(true);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image:
                Intent intent = new Intent(this, ImageActivity.class);
                startActivity(intent);
                break;
            case R.id.video:
                Intent intent2 = new Intent(this, VideoActivity.class);
                startActivity(intent2);
                break;
            case R.id.audio:
                Intent intent3 = new Intent(this, AudioActivity.class);
                startActivity(intent3);
                break;
            case R.id.pdf:
                Intent intent4 = new Intent(this, PdfActivity.class);
                startActivity(intent4);
                break;
            case R.id.submitStory:
                submitStory();
               break;
        }

    }

    protected void initDialog() {

        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(false);
    }

    protected void showpDialog() {

        if (!pDialog.isShowing()) pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing()) pDialog.dismiss();
    }

    private void submitStory() {
        String storyWithText = story.getText().toString();
        String description = descWithText.getText().toString();

        if (storyWithText.isEmpty() || description.isEmpty())
        {
            Toast.makeText(this, "Required fields can't be empty", Toast.LENGTH_SHORT).show();
        }
        else {
            ApiConfig getResponse = AppConfig.getRetrofit().create(ApiConfig.class);
            Call<ServerResponse> call = getResponse.uploadTextStory("token", storyWithText, description);
            call.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    if (response.isSuccessful()){
                        if (response.body() != null){
                            hidepDialog();
                            ServerResponse serverResponse = response.body();
                            Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            story.setText("");
                            descWithText.setText("");
                        }
                    }else {
                        hidepDialog();
                        Toast.makeText(getApplicationContext(), "problem submitting the story", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    hidepDialog();
                    Toast.makeText(getApplicationContext(), "problem uploading the story" + t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.v("Response gotten is", t.getMessage());
                }
            });
        }
    }
}
