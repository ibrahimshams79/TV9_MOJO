package com.tv9.tv9MoJo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import it.sauronsoftware.ftp4j.FTPClient;

public class InitialActivity extends AppCompatActivity {
    public static FTPClient client;
    public static String currentUser;
    public static String currentHost;
    public static String currentPassword;
    public static int currentPort = 21;

    public static void login() throws Exception {
        client = new FTPClient();
        client.connect(currentHost, currentPort);
        client.login(currentUser, currentPassword);
    }

    private Toolbar toolbar;

    private ListView homeListView;
    private List<HashMap<String, Object>> homeSimpleAdaptList;
    ;
    private SimpleAdapter homeSimpleAdapter;

    private com.baoyz.swipemenulistview.SwipeMenuListView ftpListView;
    private List<HashMap<String, Object>> ftpSimpleAdaptList;
    ;
    private SimpleAdapter ftpSimpleAdapter;

    private Button newFTPButton;

    private com.wang.avi.AVLoadingIndicatorView loadingView;
    private TextView loadText;

    private ArrayList<String> hostNamesCopy;
    private ArrayList<String> userNamesCopy;
    private ArrayList<String> userPasswordsCopy;
    private ArrayList<String> canLoginCopy;
    private ArrayList<String> loginDirectly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        initViews();
        setupData();
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadingView.hide();
        loadText.setVisibility(View.INVISIBLE);

        if (InitialActivity.client != null) {
            try {
                InitialActivity.client.disconnect(false);
                InitialActivity.client = null;

                showToast("sign out");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //    This method implements the "My ftp" data display for ListView
    private void setupData() {

        //The following is the data from hardpreference to get the host number, user name, user password, and sign-in
        hostNamesCopy = new ArrayList<>(Arrays.asList(getSharedPreference("host_name")));
        userNamesCopy = new ArrayList<>(Arrays.asList(getSharedPreference("user_name")));
        userPasswordsCopy = new ArrayList<>(Arrays.asList(getSharedPreference("user_password")));
        canLoginCopy = new ArrayList<>(Arrays.asList(getSharedPreference("can_login")));
        loginDirectly = new ArrayList<>(Arrays.asList(getSharedPreference("login_directly")));

        if (hostNamesCopy.size() == 1 && hostNamesCopy.get(0).equals("")) {
            return;
        }

        //Traversing the array, assigning the data to ftpSimpleAdaptList, tells listview to update the data through the bound adapter
        for (int i = 0; i < hostNamesCopy.size(); i++) {
            HashMap<String, Object> hashMap
                    = new HashMap<>();
            hashMap.put("icon", R.drawable.ftp2);
            hashMap.put("name", userNamesCopy.get(i) + "@" + hostNamesCopy.get(i));
            //Note that String comparisons are address comparisons
            if (canLoginCopy.get(i).contentEquals("false")) {
                hashMap.put("name", userNamesCopy.get(i) + "@" + hostNamesCopy.get(i) + "(failure)");
            }
            ftpSimpleAdaptList.add(hashMap);
        }

        //Notify Listview to update the data
        ftpSimpleAdapter.notifyDataSetChanged();
    }

    private void initViews() {
        toolbar = findViewById(R.id.init_toolbar);
        toolbar.setTitle("FTP");
        setSupportActionBar(toolbar);

//        homeListView = findViewById(R.id.home_List_view);
        homeSimpleAdaptList = new ArrayList<>();
        String[] from1 = {"icon", "name"};
        int[] to1 = {R.id.cell_image, R.id.cell_name};
        HashMap<String, Object> hashMap
                = new HashMap<>();
        hashMap.put("icon", R.drawable.home);
        hashMap.put("name", "Local Home");
        homeSimpleAdaptList.add(hashMap);
        homeSimpleAdapter = new SimpleAdapter(getApplicationContext(), homeSimpleAdaptList, R.layout.cell, from1, to1);
//        homeListView.setAdapter(homeSimpleAdapter);

        ftpListView = findViewById(R.id.ftp_list_view);
        ftpSimpleAdaptList = new ArrayList<>();
        String[] from2 = {"icon", "name"};
        int[] to2 = {R.id.cell_image, R.id.cell_name};
        ftpSimpleAdapter = new SimpleAdapter(getApplicationContext(), ftpSimpleAdaptList, R.layout.cell, from2, to2);
        ftpListView.setAdapter(ftpSimpleAdapter);

        newFTPButton = findViewById(R.id.new_ftp);

        loadingView = findViewById(R.id.avi1);

        loadText = findViewById(R.id.loadText);
    }

    private void setListeners() {
        //Write a click-to-response event for My Local Files: The page jumps into LocalHomeActivity
//        homeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(InitialActivity.this, LocalHomeActivity.class);
//                startActivity(intent);
//            }
//        });

        //Write a click listener for "ftplistView": Click on the appropriate view to log the appropriate user into the ftp
        ftpListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                loadingView.smoothToShow();
                loadText.setVisibility(View.VISIBLE);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            //Get the user's password account number
                            currentUser = userNamesCopy.get(position);
                            currentHost = hostNamesCopy.get(position);
                            currentPassword = userPasswordsCopy.get(position);

                            //The login function
                            login();

                            if (InitialActivity.client.isConnected()) {
                                if (canLoginCopy.get(position).contentEquals("false")) {
                                    canLoginCopy.set(position, "true");
                                    setSharedPreferenceValues("can_login", canLoginCopy);
                                    setSharedPreferenceValues("login_directly", loginDirectly);

                                    HashMap<String, Object> hashMap
                                            = new HashMap<>();
                                    hashMap.put("icon", R.drawable.ftp2);
                                    hashMap.put("name", userNamesCopy.get(position) + "@" + hostNamesCopy.get(position));
                                    ftpSimpleAdaptList.remove(position);
                                    ftpSimpleAdaptList.add(position, hashMap);

                                    notifyDataChanged();
                                }

                                Intent intent = new Intent(InitialActivity.this, MainActivity.class);
                                intent.putExtra("url", "http://" + hostNamesCopy.get(position) + "/tv9/");
                                startActivity(intent);
                                showToast("Connected to " + hostNamesCopy.get(position));
                            } else {
                                showToast("\n" + "Login failed");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();

                            if (canLoginCopy.get(position).contentEquals("true")) {
                                canLoginCopy.set(position, "false");
                                setSharedPreferenceValues("can_login", canLoginCopy);

                                HashMap<String, Object> hashMap
                                        = new HashMap<>();
                                hashMap.put("icon", R.drawable.ftp2);
                                hashMap.put("name", userNamesCopy.get(position) + "@" + hostNamesCopy.get(position) + "(failure)");
                                ftpSimpleAdaptList.remove(position);
                                ftpSimpleAdaptList.add(position, hashMap);

                                notifyDataChanged();
                            }

                            showToast("Login failed");
                            hideLoadView();
                        }
                    }
                }).start();
            }
        });


        //Right-slip delete button implementation
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {

                //Set the red background
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                deleteItem.setWidth(200);

                //Set the delete icon
                deleteItem.setIcon(R.drawable.delete);

                //Add a delete button
                menu.addMenuItem(deleteItem);
            }
        };

        //Bind the appropriate delete button to ftpListView
        ftpListView.setMenuCreator(creator);

        //Implements the contents of the delete button
        ftpListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        hostNamesCopy.remove(position);
                        userNamesCopy.remove(position);
                        userPasswordsCopy.remove(position);
                        canLoginCopy.remove(position);
                        setSharedPreferenceValues("can_login", canLoginCopy);
                        setSharedPreferenceValues("host_name", hostNamesCopy);
                        setSharedPreferenceValues("user_name", userNamesCopy);
                        setSharedPreferenceValues("user_password", userPasswordsCopy);

                        ftpSimpleAdaptList.remove(position);

                        //A view was deleted, notifying the update
                        ftpSimpleAdapter.notifyDataSetChanged();

                    default:
                        break;
                }

                // false : close the menu; true : not close the menu
                return true;
            }
        });
        ftpListView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);

        newFTPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View cusView = LayoutInflater.from(InitialActivity.this).inflate(R.layout.login, null);
                final AlertDialog.Builder cusDia = new AlertDialog.Builder(InitialActivity.this);
                cusDia.setTitle("Add FTP address\n");
                cusDia.setView(cusView);

                cusDia.setPositiveButton("save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        EditText hostName = cusView.findViewById(R.id.host_name);
                        EditText userName = cusView.findViewById(R.id.user_name);
                        EditText userPassword = cusView.findViewById(R.id.user_password);

                        final String hostNameString = hostName.getText().toString().trim();
                        final String userNameString = userName.getText().toString().trim();
                        final String userPasswordString = userPassword.getText().toString().trim();

                        if (TextUtils.isEmpty(hostNameString) || TextUtils.isEmpty(userNameString) || TextUtils.isEmpty(userPasswordString)) {

                            new AlertDialog.Builder(InitialActivity.this)
                                    .setTitle("Login")
                                    .setMessage("Please fill all fields")

                                    // Specifying a listener allows you to take an action before dismissing the dialog.
                                    // The dialog is automatically dismissed when a dialog button is clicked.
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })

                                    // A null listener allows the button to dismiss the dialog and take no further action.
                                    .setNegativeButton(android.R.string.cancel, null)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();

                        } else {
                            loadingView.smoothToShow();
                            loadText.setVisibility(View.VISIBLE);
                            hostNamesCopy.add(hostNameString);
                            userNamesCopy.add(userNameString);
                            userPasswordsCopy.add(userPasswordString);
                            canLoginCopy.add("false");

                            final HashMap<String, Object> hashMap
                                    = new HashMap<>();
                            hashMap.put("icon", R.drawable.ftp2);
                            hashMap.put("name", userNameString + "@" + hostNameString);
                            ftpSimpleAdaptList.add(hashMap);
                            ftpSimpleAdapter.notifyDataSetChanged();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        currentUser = userNameString;
                                        currentHost = hostNameString;
                                        currentPassword = userPasswordString;
                                        login();

                                        if (InitialActivity.client.isConnected()) {
                                            canLoginCopy.remove(canLoginCopy.size() - 1);
                                            canLoginCopy.add("true");
                                            setSharedPreference("can_login", "true");
                                            setSharedPreference("host_name", hostNameString);
                                            setSharedPreference("user_name", userNameString);
                                            setSharedPreference("user_password", userPasswordString);

                                            showToast("login successful");
                                            hideLoadView();

                                            Intent intent = new Intent(InitialActivity.this, MainActivity.class);
                                            intent.putExtra("url", "http://" + hostNamesCopy + "/tv9/");
                                            startActivity(intent);
                                        } else {
                                            showToast("\n" + "Login failed");
//                                        showToast("Connected to " +hostNameString);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();

                                        setSharedPreference("can_login", "false");
                                        setSharedPreference("host_name", hostNameString);
                                        setSharedPreference("user_name", userNameString);
                                        setSharedPreference("user_password", userPasswordString);

                                        hashMap.put("name", userNameString + "@" + hostNameString + "(failure)");
                                        ftpSimpleAdaptList.remove(ftpSimpleAdaptList.size() - 1);
                                        ftpSimpleAdaptList.add(hashMap);


                                        notifyDataChanged();
                                        showToast("Login failed");
                                        hideLoadView();
                                    }
                                }
                            }).start();
                        }
                    }
                });

                cusDia.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                cusDia.create().show();
            }
        });
    }


    public String[] getSharedPreference(String key) {
        String regularEx = "#";
        String[] str;
        SharedPreferences sp = getSharedPreferences("FTPHost", Context.MODE_PRIVATE);
        String values;
        values = sp.getString(key, "");
        str = values.split(regularEx);

        return str;
    }

    public void setSharedPreferenceValues(String key, ArrayList<String> values) {
        String regularEx = "#";
        String str = "";
        SharedPreferences sp = getSharedPreferences("FTPHost", Context.MODE_PRIVATE);
        if (values != null && values.size() > 0) {
            for (String value : values) {
                str += value;
                str += regularEx;
            }
            SharedPreferences.Editor et = sp.edit();
            et.putString(key, str);
            et.apply();
        }
    }

    public void setSharedPreference(String key, String value) {
        String regularEx = "#";
        SharedPreferences sp = getSharedPreferences("FTPHost", Context.MODE_PRIVATE);
        String values;
        values = sp.getString(key, "");
        SharedPreferences.Editor et = sp.edit();
        et.putString(key, values + value + regularEx);
        et.apply();
    }


    //Interactive message processing
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Helper.SHOW_TOAST:
                    Toast.makeText(InitialActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case Helper.NOTIFY_DATA_CHANGED:
                    ftpSimpleAdapter.notifyDataSetChanged();
                    break;
                case Helper.SHOW_LOAD_VIEW:
                    loadingView.smoothToShow();
                    break;
                case Helper.HIDE_LOAD_VIEW:
                    loadingView.smoothToHide();
                    loadText.setVisibility(View.INVISIBLE);
                    break;
                default:
                    break;
            }
        }
    };

    //Short notifications
    private void showToast(String message) {
        Message msg = new Message();
        msg.what = Helper.SHOW_TOAST;
        msg.obj = message;
        mHandler.sendMessage(msg);
    }

    //Hide the load icon
    private void hideLoadView() {
        Message msg = new Message();
        msg.what = Helper.HIDE_LOAD_VIEW;
        mHandler.sendMessage(msg);
    }

    //Notify listview updates
    private void notifyDataChanged() {
        Message msg = new Message();
        msg.what = Helper.NOTIFY_DATA_CHANGED;
        mHandler.sendMessage(msg);
    }
}
