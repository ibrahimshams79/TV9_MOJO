package com.tv9.tv9MoJo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.tv9.tv9MoJo.networking.ApiConfig;
import com.tv9.tv9MoJo.networking.AppConfig;
import com.tv9.tv9MoJo.networking.ServerResponse;

import java.util.HashMap;
import java.util.Map;

import io.paperdb.Paper;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener{
    private ImageButton image, video, audio, pdf;
    private Button submitStory;
    private EditText story, descWithText;
    ProgressDialog pDialog;
    private long backPressedTime;
    private Toast backToast;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawer;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image = findViewById(R.id.image);
        video = findViewById(R.id.video);
        audio = findViewById(R.id.audio);
        pdf = findViewById(R.id.pdf);

        story = findViewById(R.id.storyWithText);
        descWithText = findViewById(R.id.descriptionWithText);
        submitStory = findViewById(R.id.submitStory);

        sharedPreferences = getSharedPreferences("SHARED_PREF", MODE_PRIVATE);

        initDialog();


        Toolbar toolbar = findViewById(R.id.reporter_drawer_toolbar);
        toolbar.setTitle("Home");
        this.setSupportActionBar(toolbar);

        drawer = findViewById(R.id.reporter_drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.reporter_nav_view_home);

//        navigationView.setNavigationItemSelectedListener(item -> {
//            int id = item.getItemId();
//
//            if (id == R.id.nav_ftp) {
//                Toast.makeText(getApplicationContext(), "FTP Selected", Toast.LENGTH_SHORT).show();
//                drawer.closeDrawer(GravityCompat.START);
//            } else if (id == R.id.nav_categories) {
//                drawer.closeDrawer(GravityCompat.START);
//                Toast.makeText(getApplicationContext(), "category Selected", Toast.LENGTH_SHORT).show();
//            } else if (id == R.id.nav_settings) {
//                drawer.closeDrawer(GravityCompat.START);
//                Toast.makeText(getApplicationContext(), "settings Selected", Toast.LENGTH_SHORT).show();
//            } else if (id == R.id.nav_logout) {
//                drawer.closeDrawer(GravityCompat.START);
//                Toast.makeText(getApplicationContext(), "Logout Selected", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(intent);
//                finish();
//            }
//            return true;
//        });

        navigationView.setNavigationItemSelectedListener(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            image.setEnabled(false);
            video.setEnabled(false);
            audio.setEnabled(false);
            pdf.setEnabled(false);
            submitStory.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

        if (storyWithText.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Required fields can't be empty", Toast.LENGTH_SHORT).show();
        } else {
            String url = "http://192.168.0.104/tv9/";
            ApiConfig getResponse = AppConfig.getRetrofit(url).create(ApiConfig.class);
            Call<ServerResponse> call = getResponse.uploadTextStory("token", storyWithText, description);
            call.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            hidepDialog();
                            ServerResponse serverResponse = response.body();
                            Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            story.setText("");
                            descWithText.setText("");
                        }
                    } else {
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

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                backToast.cancel();
                super.onBackPressed();
                return;
            } else {
                backToast = Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT);
                backToast.show();
            }
            backPressedTime = System.currentTimeMillis();
        }
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.reporter_drawer_menu, menu);
//        return true;
//    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_ftp) {
            Toast.makeText(getApplicationContext(), "FTP Selected", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_categories) {
            Toast.makeText(getApplicationContext(), "category Selected", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
            Toast.makeText(getApplicationContext(), "settings Selected", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_logout) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            Toast.makeText(getApplicationContext(), "Logout Selected", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

//        DrawerLayout drawer = findViewById(R.id.reporter_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }
}
