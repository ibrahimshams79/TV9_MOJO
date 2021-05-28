package com.tv9.tv9MoJo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.content.CursorLoader;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.tv9.tv9MoJo.networking.ApiConfig;
import com.tv9.tv9MoJo.networking.AppConfig;
import com.tv9.tv9MoJo.networking.ServerResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AudioActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int SELECT_AUDIO = 2;
    private String selectedPath;
    private Handler handler;
    private TextView tvStatus;
    EditText audiofileName, audiostory;
    ProgressDialog pDialog;
    Button audioselect, audiouploadfile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        audiofileName = findViewById(R.id.audiofileName);
        audiostory = findViewById(R.id.audiostory);
        findViewById(R.id.audioselectFile).setOnClickListener(this);
        findViewById(R.id.audiouploadFile).setOnClickListener(this);
        tvStatus = findViewById(R.id.tvStatus);
//        handler = new Handler(this);
        initDialog();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    public void openGallery() {
//        Intent pickAudioIntent = new Intent(Intent.ACTION_PICK,
//                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(pickAudioIntent, "Select Audio "), SELECT_AUDIO);
        startActivityForResult(intent, SELECT_AUDIO);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_AUDIO) {
                Uri selectedAudioUri = data.getData();
//                selectedAudioUri = handleAudioUri(selectedAudioUri);
                selectedPath = getPathFromURI(selectedAudioUri);
                tvStatus.setText("Selected Path :: " + selectedPath);
                Log.i(TAG, " Path :: " + selectedPath);
            }
        }
    }

    public static Uri handleAudioUri(Uri uri) {
        if (uri.getPath().contains("content")) {
            Pattern pattern = Pattern.compile("(content://media/.*\\d)");
            Matcher matcher = pattern.matcher(uri.getPath());
            if (matcher.find())
                return Uri.parse(matcher.group(1));
            else
                throw new IllegalArgumentException("Cannot handle this URI");
        }
        return uri;
    }

//    @SuppressLint("NewApi")
//    public String getRealPathFromURI(Uri uri) {
//        String filePath = "";
//        String wholeID = DocumentsContract.getDocumentId(uri);
//
//        // Split at colon, use second item in the array
//        String id = wholeID.split(":")[1];
//
//        String[] column = {MediaStore.Images.Media.DATA};
//
//        // where id is equal to
//        String sel = MediaStore.Images.Media._ID + "=?";
//
//        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                column, sel, new String[]{id}, null);
//
//        int columnIndex = cursor.getColumnIndex(column[0]);
//
//        if (cursor.moveToFirst()) {
//            filePath = cursor.getString(columnIndex);
//        }
//        cursor.close();
//        return filePath;
//    }

    @SuppressLint("ObsoleteSdkInt")
    public String getPathFromURI(Uri uri){
        String realPath="";
// SDK < API11
        if (Build.VERSION.SDK_INT < 11) {
            String[] proj = { MediaStore.Video.Media.DATA };
            @SuppressLint("Recycle") Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
            int column_index = 0;
            String result="";
            if (cursor != null) {
                column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                realPath=cursor.getString(column_index);
            }
        }
        // SDK >= 11 && SDK < 19
        else if (Build.VERSION.SDK_INT < 19){
            String[] proj = { MediaStore.Video.Media.DATA };
            CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null, null, null);
            Cursor cursor = cursorLoader.loadInBackground();
            if(cursor != null){
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                cursor.moveToFirst();
                realPath = cursor.getString(column_index);
            }
        }
        // SDK > 19 (Android 4.4)
        else if (Build.VERSION.SDK_INT < 21){
            String wholeID = DocumentsContract.getDocumentId(uri);
            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];
            String[] column = { MediaStore.Video.Media.DATA };
            // where id is equal to
            String sel = MediaStore.Video.Media._ID + "=?";
            Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, column, sel, new String[]{ id }, null);
            int columnIndex = 0;
            if (cursor != null) {
                columnIndex = cursor.getColumnIndex(column[0]);
                if (cursor.moveToFirst()) {
                    realPath = cursor.getString(columnIndex);
                }
                cursor.close();
            }
        }
        else {
            String[] projection = {MediaStore.Video.Media.DATA};
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
                // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
                int column_index = cursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                cursor.moveToFirst();
                String cursorString = cursor.getString(column_index);
                cursor.close();
                return cursorString;
            }
        }
        return realPath;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.audioselectFile) {
            openGallery();
        }
        if (v.getId() == R.id.audiouploadFile) {
            if (null != selectedPath && !selectedPath.isEmpty()) {
                tvStatus.setText("Uploading..." + selectedPath);
//                FileUploadUtility.doFileUpload(selectedPath, handler);
                try {
                    uploadFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
    protected void initDialog() {

        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(true);
    }


    protected void showpDialog() {

        if (!pDialog.isShowing()) pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing()) pDialog.dismiss();
    }
    // Uploading Image/Video
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void uploadFile() throws IOException {
        {
            if (selectedPath == null || selectedPath.equals("")) {
                Toast.makeText(this, "Please select an Image ", Toast.LENGTH_LONG).show();
            }else if (audiofileName.getText()== null || audiofileName.getText().toString().equals(""))
            {
                Toast.makeText(this, "Please type the Image name ", Toast.LENGTH_LONG).show();
            }else if (audiostory.getText()== null || audiostory.getText().toString().equals(""))
            {
                Toast.makeText(this, "Please type the story ", Toast.LENGTH_LONG).show();
            }
            else {
                showpDialog();
                // Map is used to multipart the file using okhttp3.RequestBody
                Map<String, RequestBody> map = new HashMap<>();

                String imageName = audiofileName.getText().toString();
                String imageStory = audiostory.getText().toString();

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmSS").format(new Date());
                File file = new File(selectedPath);
                File audio = new File(file, imageName + "_" + timeStamp + ".mp3");


                //For Image Resizer Class.
//                Bitmap fullsizeBitmap = BitmapFactory.decodeFile(selectedPath);
//            Bitmap reducedBitmap = ImageResizer.reduceBitmapSize(fullsizeBitmap, 512000);
                /////////////////////////


//            if (imgSize>=500) {
//                file = compressToBitmap(imageName, file);
//                Toast.makeText(getApplicationContext(), "Image Compressed", Toast.LENGTH_SHORT).show();
//            }
//            else {
//              file = uncompressedBitmap(imageName, file);
//            }
//            File file = BitmaptoFile(reducedBitmap, imageName);
//            File mSaveBit = null; // Your image file
//            String filePath = file.getPath();
//            Bitmap bitmap = BitmapFactory.decodeFile(filePath);

//                file = compressToBitmap(imageName, file);


                // Parsing any Media type file
                RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), file);
                map.put("file\"; filename=\"" + audio.getName() + "\"", requestBody);
//            story.put("story", ApiConfig.createRequestBody(imageStory));
                RequestBody storyDescription = RequestBody.create(MultipartBody.FORM, imageStory);
                String stringdescription = imageStory;
//            story.put("story", imageStory);
                String url="http://192.168.0.104/tv9/";
                ApiConfig getResponse = AppConfig.getRetrofit(url).create(ApiConfig.class);
                Call<ServerResponse> call = getResponse.upload("token", map, stringdescription);
                call.enqueue(new Callback<ServerResponse>() {
                    @Override
                    public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                        if (response.isSuccessful()){
                            if (response.body() != null){
                                hidepDialog();
                                ServerResponse serverResponse = response.body();
                                Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                audiofileName.setText("");
                                audiostory.setText("");
//                                imageView.setImageResource(R.drawable.ic_launcher_background);

                                tvStatus.setVisibility(VideoView.INVISIBLE);
                            }
                        }else {
                            hidepDialog();
                            Toast.makeText(getApplicationContext(), "problem uploading image", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ServerResponse> call, Throwable t) {
                        hidepDialog();
                        Toast.makeText(getApplicationContext(), "problem uploading image " + t.getMessage(), Toast.LENGTH_LONG).show();
                        Log.v("Response gotten is", t.getMessage());
                    }
                });
            }
        }
    }

//    @Override
//    public boolean handleMessage(Message msg) {
//        Log.i("File Upload", "Response :: " + msg.obj);
//        String success = 1 == msg.arg1 ? "File Upload Success" : "File Upload Error";
//        Log.i(TAG, success);
//        tvStatus.setText(success);
//        return false;
//    }

}