package com.tv9.tv9MoJo;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;

import com.tv9.tv9MoJo.networking.ApiConfig;
import com.tv9.tv9MoJo.networking.AppConfig;
import com.tv9.tv9MoJo.networking.ServerResponse;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.vincent.videocompressor.VideoCompress;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class VideoActivity extends AppCompatActivity {
    private static final String TAG = "";
    private static final int REQUEST_TAKE_GALLERY_VIDEO = 3;
    private Button pickVideo, uploadVideo;
    public static final int REQUEST_PICK_VIDEO = 3;
    public ProgressDialog pDialog;
    private VideoView mVideoView;
    private TextView mBufferingTextView;
    private Uri video;
    private String videoPath, compressedPath;
    private EditText videoStory, videoName;
    private String mImageFileLocation = "";

    // Current playback position (in milliseconds).
    private int mCurrentPosition = 0;

    // Tag for the instance state bundle.
    private static final String PLAYBACK_TIME = "play_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_layout);
        videoStory = (EditText) findViewById(R.id.videostory);
        videoName = (EditText) findViewById(R.id.videoName);
        pickVideo = (Button) findViewById(R.id.pickVideo);
        uploadVideo = (Button) findViewById(R.id.uploadVideo);

        pickVideo.setOnClickListener(view -> {
            Intent pickVideoIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);

//            Intent pickVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
//            pickVideoIntent.setType("video/*");
            startActivityForResult(pickVideoIntent, REQUEST_TAKE_GALLERY_VIDEO);
        });

        uploadVideo.setOnClickListener(view -> {
            if (video != null) {
                uploadFile();
            } else {
                Toast.makeText(VideoActivity.this, "Please select a video", Toast.LENGTH_SHORT).show();
            }
        });

        mVideoView = (VideoView) findViewById(R.id.videoview);
        mBufferingTextView = (TextView) findViewById(R.id.buffering_textview);

        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(PLAYBACK_TIME);
        }

        // Set up the media controller widget and attach it to the video view.
        MediaController controller = new MediaController(this);
        controller.setMediaPlayer(mVideoView);
        mVideoView.setMediaController(controller);

        initDialog();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mVideoView.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Media playback takes a lot of resources, so everything should be
        // stopped and released at this time.
        releasePlayer();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current playback position (in milliseconds) to the
        // instance state bundle.
        outState.putInt(PLAYBACK_TIME, mVideoView.getCurrentPosition());
    }

    private void initializePlayer(Uri uri) {
        // Show the "Buffering..." message while the video loads.
        mBufferingTextView.setVisibility(VideoView.VISIBLE);
        if (uri != null) {
            mVideoView.setVideoURI(uri);
        }
        // Listener for onPrepared() event (runs after the media is prepared).
        mVideoView.setOnPreparedListener(
                new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {

                        // Hide buffering message.
                        mBufferingTextView.setVisibility(VideoView.INVISIBLE);

                        // Restore saved position, if available.
                        if (mCurrentPosition > 0) {
                            mVideoView.seekTo(mCurrentPosition);
                        } else {
                            // Skipping to 1 shows the first frame of the video.
                            mVideoView.seekTo(1);
                        }

                        // Start playing!
                        mVideoView.start();
                    }
                });

        // Listener for onCompletion() event (runs after media has finished
        // playing).
        mVideoView.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        Toast.makeText(VideoActivity.this,
                                R.string.toast_message,
                                Toast.LENGTH_SHORT).show();

                        // Return the video position to the start.
                        mVideoView.seekTo(0);
                    }
                });
    }

    private void releasePlayer() {
        mVideoView.stopPlayback();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == REQUEST_PICK_VIDEO) {
                if (data != null) {
                    Toast.makeText(this, "Video content URI: " + data.getData(),
                            Toast.LENGTH_LONG).show();
                    video = data.getData();

                    videoPath = getPathFromURI(video);


//
                    initializePlayer(video);
                }
            }
        } else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, "Sorry, there was an error!", Toast.LENGTH_LONG).show();
        }
    }


    @SuppressLint("ObsoleteSdkInt")
    public String getPathFromURI(Uri uri) {
        String realPath = "";
// SDK < API11
        if (Build.VERSION.SDK_INT < 11) {
            String[] proj = {MediaStore.Video.Media.DATA};
            @SuppressLint("Recycle") Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
            int column_index = 0;
            String result = "";
            if (cursor != null) {
                column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                realPath = cursor.getString(column_index);
            }
        }
        // SDK >= 11 && SDK < 19
        else if (Build.VERSION.SDK_INT < 19) {
            String[] proj = {MediaStore.Video.Media.DATA};
            CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null, null, null);
            Cursor cursor = cursorLoader.loadInBackground();
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                cursor.moveToFirst();
                realPath = cursor.getString(column_index);
            }
        }
        // SDK > 19 (Android 4.4)
        else if (Build.VERSION.SDK_INT < 21) {
            String wholeID = DocumentsContract.getDocumentId(uri);
            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];
            String[] column = {MediaStore.Video.Media.DATA};
            // where id is equal to
            String sel = MediaStore.Video.Media._ID + "=?";
            Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, column, sel, new String[]{id}, null);
            int columnIndex = 0;
            if (cursor != null) {
                columnIndex = cursor.getColumnIndex(column[0]);
                if (cursor.moveToFirst()) {
                    realPath = cursor.getString(columnIndex);
                }
                cursor.close();
            }
        } else {
            String[] projection = {MediaStore.Video.Media.DATA};
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
                // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
                int column_index = cursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            }
        }
        return realPath;
    }

    private void uploadFile() {
        if (video == null || video.equals("")) {
            Toast.makeText(this, "please select a Video ", Toast.LENGTH_LONG).show();
            return;
        } else if (videoName.getText() == null || videoName.getText().toString().equals("")) {
            Toast.makeText(this, "Please type the Video name ", Toast.LENGTH_LONG).show();
        } else if (videoStory.getText() == null || videoStory.getText().toString().equals("")) {
            Toast.makeText(this, "Please type the story ", Toast.LENGTH_LONG).show();
        } else {

//            Compression Logic
//            compressedPath = compressVideo(videoPath);

            checkPermission();
            File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/TV9 MoJo Uploads");
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmSS").format(new Date());
            if (!storageDirectory.exists()) storageDirectory.mkdir();
            String videoNamestring = videoName.getText().toString();
            String videoDescription = videoStory.getText().toString();
            String outputPath = storageDirectory + "/" + videoNamestring + "_" + timeStamp + ".mp4";
            final ProgressDialog dialog = new ProgressDialog(VideoActivity.this);

            File file2 = new File(videoPath);
            long length = file2.length();
            length = length/1024;

            if (length > 500000) {
                VideoCompress.compressVideoMedium(videoPath, outputPath, new VideoCompress.CompressListener() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess() {
                        String state = Environment.getExternalStorageState();

                        if (Environment.MEDIA_MOUNTED.equals(state)) {
                            if (Build.VERSION.SDK_INT >= 23) {
                                if (checkPermission()) {
                                    showpDialog();


                                    // Map is used to multipart the file using okhttp3.RequestBody
                                    Map<String, RequestBody> map = new HashMap<>();

                                    File file;
                                    file = new File(outputPath);
//             Parsing any Media type file
                                    RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), file);
                                    map.put("file\"; filename=\"" + file.getName() + "\"", requestBody);
                                    String url="http://192.168.0.104/tv9/";
                                    ApiConfig getResponse = AppConfig.getRetrofit(url).create(ApiConfig.class);

                                    Call<ServerResponse> call = getResponse.upload("token", map, videoDescription);
                                    call.enqueue(new Callback<ServerResponse>() {
                                        @Override
                                        public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                                            if (response.isSuccessful()) {
                                                if (response.body() != null) {
                                                    hidepDialog();
                                                    ServerResponse serverResponse = response.body();
                                                    Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                                    videoName.setText("");
                                                    videoStory.setText("");
                                                }
                                            } else {
                                                hidepDialog();
                                                Toast.makeText(getApplicationContext(), "problem uploading Video", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<ServerResponse> call, Throwable t) {
                                            hidepDialog();
                                            Log.v("Response gotten is", t.getMessage());
                                            Toast.makeText(getApplicationContext(), "Network Error " + t.getMessage(), Toast.LENGTH_SHORT).show();

                                        }
                                    });
                                }
                            }
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onFail() {
                        dialog.dismiss();
                    }

                    @Override
                    public void onProgress(float percent) {
                        dialog.setMessage("Compressing Video " + Math.round(percent) + "%");
                        dialog.show();
                        dialog.setCancelable(false);
                    }
                });
            }
            else {
                String state = Environment.getExternalStorageState();

                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (checkPermission()) {
                            showpDialog();


                            // Map is used to multipart the file using okhttp3.RequestBody
                            Map<String, RequestBody> map = new HashMap<>();

                            File file;
                            file = new File(videoPath);
//             Parsing any Media type file
                            RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), file);
                            map.put("file\"; filename=\"" + file.getName() + "\"", requestBody);
                            String url="http://192.168.0.104/tv9/";
                            ApiConfig getResponse = AppConfig.getRetrofit(url).create(ApiConfig.class);

                            Call<ServerResponse> call = getResponse.upload("token", map, videoDescription);
                            call.enqueue(new Callback<ServerResponse>() {
                                @Override
                                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                                    if (response.isSuccessful()) {
                                        if (response.body() != null) {
                                            hidepDialog();
                                            ServerResponse serverResponse = response.body();
                                            Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                            videoName.setText("");
                                            videoStory.setText("");
                                        }
                                    } else {
                                        hidepDialog();
                                        Toast.makeText(getApplicationContext(), "problem uploading Video", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ServerResponse> call, Throwable t) {
                                    hidepDialog();
                                    Log.v("Response gotten is", t.getMessage());
                                    Toast.makeText(getApplicationContext(), "Network Error " + t.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });
                        }
                    }
                }
                dialog.dismiss();
            }

        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(VideoActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
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


    String compressVideo(String videoPath) {
//        String inputVideoPath = getPath(this, video);
        Log.d("doFileUpload ", videoPath);
        FFmpeg ffmpeg = FFmpeg.getInstance(this);
        try {
            //Load the binary
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onStart() {
                    Log.d("FFmpeg", "onStart");
                }

                @Override
                public void onFailure() {
                    Log.d("FFmpeg", "onFailure");
                }

                @Override
                public void onSuccess() {
                    Log.d("FFmpeg", "onSuccess");
                }

                @Override
                public void onFinish() {
                    Log.d("FFmpeg", "onFinish");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            // Handle if FFmpeg is not supported by device
        }

        // to execute "ffmpeg -version" command you just need to pass "-version"

        checkPermission();
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/TV9 MoJo Uploads");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmSS").format(new Date());
        if (!storageDirectory.exists()) storageDirectory.mkdir();
        String videoNamestring = videoName.getText().toString();
        String videoDescription = videoStory.getText().toString();
        String outputPath = storageDirectory + "/" + videoNamestring + "_" + timeStamp + ".mp4";

//        String outputPath = tempStorage (videoNamestring);
        String[] commandArray;
        commandArray = new String[]{"-y", "-i", videoPath, "-s", "720x480", "-r", "25",
                "-vcodec", "mpeg4", "-b:v", "300k", "-b:a", "48000", "-ac", "2", "-ar", "22050", outputPath};
        final ProgressDialog dialog = new ProgressDialog(VideoActivity.this);
        try {
            ffmpeg.execute(commandArray, new ExecuteBinaryResponseHandler() {
                @Override
                public void onStart() {
                    Log.e("FFmpeg", "onStart" + outputPath);
                    dialog.setMessage("Compressing... please wait");
                    dialog.show();
                    dialog.setCancelable(false);
                }

                @Override
                public void onProgress(String message) {

                    Log.e("FFmpeg onProgress? ", message);
                }

                @Override
                public void onFailure(String message) {
                    Log.e("FFmpeg onFailure? ", message);
                }

                @Override
                public void onSuccess(String message) {
                    Log.e("FFmpeg onSuccess? ", message);

                }

                @Override
                public void onFinish() {
                    Log.e("FFmpeg", "onFinish");
                    if (dialog.isShowing())
                        dialog.dismiss();
//                    initializePlayer(Uri.parse(outputPath));
                    String state = Environment.getExternalStorageState();

                    if (Environment.MEDIA_MOUNTED.equals(state)) {
                        if (Build.VERSION.SDK_INT >= 23) {
                            if (checkPermission()) {
                                showpDialog();


                                // Map is used to multipart the file using okhttp3.RequestBody
                                Map<String, RequestBody> map = new HashMap<>();

                                File file;
                                file = new File(compressedPath);
//             Parsing any Media type file
                                RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), file);
                                map.put("file\"; filename=\"" + file.getName() + "\"", requestBody);
                                String url="http://192.168.0.104/tv9/";
                                ApiConfig getResponse = AppConfig.getRetrofit(url).create(ApiConfig.class);

                                Call<ServerResponse> call = getResponse.upload("token", map, videoDescription);
                                call.enqueue(new Callback<ServerResponse>() {
                                    @Override
                                    public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                                        if (response.isSuccessful()) {
                                            if (response.body() != null) {
                                                hidepDialog();
                                                ServerResponse serverResponse = response.body();
                                                Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();

                                            }
                                        } else {
                                            hidepDialog();
                                            Toast.makeText(getApplicationContext(), "problem uploading Video", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ServerResponse> call, Throwable t) {
                                        hidepDialog();
                                        Log.v("Response gotten is", t.getMessage());
                                        Toast.makeText(getApplicationContext(), "Network Error " + t.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });
                            }
                        }
                    }

                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }

        return outputPath;
    }

    String tempStorage(String fileName) {
        Logger.getAnonymousLogger().info("Generating the image - method started");

        // Here we create a "non-collision file name", alternatively said, "an unique filename" using the "timeStamp" functionality
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmSS").format(new Date());
        String videoName = fileName + "_" + timeStamp;
        // Here we specify the environment location and the exact path where we want to save the so-created file
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/TV9 MoJo Uploads");
        Logger.getAnonymousLogger().info("Storage directory set");

        // Then we create the storage directory if does not exists
        if (!storageDirectory.exists()) storageDirectory.mkdir();

        // Here we create the file using a prefix, a suffix and a directory
        File video = new File(storageDirectory, videoName + ".mp4");

        // Here the location is saved into the string mImageFileLocation
        Logger.getAnonymousLogger().info("File name and path set");

        mImageFileLocation = video.getAbsolutePath();
        // fileUri = Uri.parse(mImageFileLocation);
        // The file is returned to the previous intent across the camera application
        return mImageFileLocation;
    }
}
