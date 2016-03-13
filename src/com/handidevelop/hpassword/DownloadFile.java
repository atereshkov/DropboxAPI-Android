package com.handidevelop.hpassword;

import java.io.*;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

public class DownloadFile extends AsyncTask<Void, Void, Boolean> {

    private DropboxAPI<?> dropbox;
    private String path;
    private Context context;

    public DownloadFile(Context context, DropboxAPI<?> dropbox, String path)
    {
        this.context = context.getApplicationContext();
        this.dropbox = dropbox;
        this.path = path;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        final File tempDir = context.getCacheDir();

        FileOutputStream outputStream = null;
        try {
            File file = new File(tempDir+"/textfile.txt");

            outputStream = new FileOutputStream(file);
            DropboxAPI.DropboxFileInfo info = dropbox.getFile(path+"textfile.txt", null, outputStream, null);
            return true;
        } catch (Exception e) {
            System.out.println("Something went wrong: " + e);
            Toast.makeText(context, "Failed to download file Something went wrong", Toast.LENGTH_LONG)
                    .show();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {}
            }
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Toast.makeText(context, "File Downloaded Sucesfully!",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Failed to download file", Toast.LENGTH_LONG)
                    .show();
        }
    }

}
