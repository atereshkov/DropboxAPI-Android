package com.handidevelop.hpassword;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;

import java.util.ArrayList;


public class MyActivity extends Activity implements OnClickListener {

    private LinearLayout container;
    private DropboxAPI<AndroidAuthSession> dropboxApi;
    private boolean isUserLoggedIn;
    private Button loginBtn;
    private Button uploadFileBtn;
    private Button listFilesBtn;
    private Button downloadFileBtn;

    private final static String DROPBOX_FILE_DIR = "/HPassword/";
    private final static String DROPBOX_NAME = "dropbox_prefs";
    private final static String ACCESS_KEY = "9w8m73yj04vho5l";
    private final static String ACCESS_SECRET = "xtlw9ot6sgse4kg";
    private final static AccessType ACCESS_TYPE = AccessType.DROPBOX;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        loginBtn = (Button) findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(this);
        uploadFileBtn = (Button) findViewById(R.id.uploadFileBtn);
        uploadFileBtn.setOnClickListener(this);
        listFilesBtn = (Button) findViewById(R.id.listFilesBtn);
        listFilesBtn.setOnClickListener(this);
        container = (LinearLayout) findViewById(R.id.container_files);
        downloadFileBtn = (Button) findViewById(R.id.downloadFileBtn);
        downloadFileBtn.setOnClickListener(this);

        loggedIn(false);

        AppKeyPair appKeyPair = new AppKeyPair(ACCESS_KEY, ACCESS_SECRET);

        AndroidAuthSession session;
        SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
        String key = prefs.getString(ACCESS_KEY, null);
        String secret = prefs.getString(ACCESS_SECRET, null);

        if (key != null && secret != null) {
            AccessTokenPair token = new AccessTokenPair(key, secret);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, token);
        } else {
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        }
        dropboxApi = new DropboxAPI<AndroidAuthSession>(session);

    }

    @Override
    protected void onResume() {
        super.onResume();

        AndroidAuthSession session = dropboxApi.getSession();
        if (dropboxApi.getSession().authenticationSuccessful()) {
            try {
                dropboxApi.getSession().finishAuthentication();
                TokenPair tokens = dropboxApi.getSession().getAccessTokenPair();
                SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(ACCESS_KEY, tokens.key);
                editor.putString(ACCESS_SECRET, tokens.secret);
                editor.commit();

                loggedIn(true);
            } catch (IllegalStateException e) {
                Toast.makeText(this, "Error during Dropbox authentication",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private final Handler handler = new Handler() {
        public void handleMessage(Message message) {
            ArrayList<String> result = message.getData().getStringArrayList("data");

            for (String fileName : result)
            {
                TextView textView = new TextView(MyActivity.this);
                textView.setText(fileName);
                container.addView(textView);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginBtn:
                if (isUserLoggedIn)
                {
                    dropboxApi.getSession().unlink();
                    loggedIn(false);
                } else
                {
                    dropboxApi.getSession().startAuthentication(MyActivity.this);
                    //dropboxApi.getSession().startOAuth2Authentication(MyActivity.this);
                }
                break;
            case R.id.uploadFileBtn:
                UploadFile uploadFile = new UploadFile(this, dropboxApi, DROPBOX_FILE_DIR);
                uploadFile.execute();
                break;
            case R.id.listFilesBtn:
                ListFiles listFiles = new ListFiles(dropboxApi, DROPBOX_FILE_DIR, handler);
                listFiles.execute();
                break;
            case R.id.downloadFileBtn:
                DownloadFile downloadFile = new DownloadFile(this, dropboxApi, DROPBOX_FILE_DIR);
                downloadFile.execute();
            default:
                break;
        }
    }

    public void loggedIn(boolean userLoggedIn) {
        isUserLoggedIn = userLoggedIn;
        uploadFileBtn.setEnabled(userLoggedIn);
        uploadFileBtn.setBackgroundColor(userLoggedIn ? Color.BLUE : Color.GRAY);
        listFilesBtn.setEnabled(userLoggedIn);
        listFilesBtn.setBackgroundColor(userLoggedIn ? Color.BLUE : Color.GRAY);
        downloadFileBtn.setEnabled(userLoggedIn);
        downloadFileBtn.setBackgroundColor(userLoggedIn ? Color.BLUE : Color.GRAY);
    }


}
