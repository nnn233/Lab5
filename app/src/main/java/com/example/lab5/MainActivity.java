package com.example.lab5;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    EditText idText;
    Button downloadButton, openButton, deleteButton;
    DownloadManager manager;
    File file;

    long reference;
    boolean isPermissionGiven;

    static int STORAGE_PERMISSION_CODE = 100;

    String path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        idText = findViewById(R.id.text_id);
        downloadButton = findViewById(R.id.download);
        downloadButton.setOnClickListener(l -> download());

        openButton = findViewById(R.id.open);
        openButton.setOnClickListener(l -> openFile());

        deleteButton = findViewById(R.id.delete);
        deleteButton.setOnClickListener(l -> deleteFile());
    }

    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            file = new File(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DOWNLOADS + "/" + path);
            if (file.exists()) {
                openButton.setEnabled(true);
                deleteButton.setEnabled(true);
            } else
                Toast.makeText(ctxt, "Журнала с таким номером не существует", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onComplete);
    }

    private void checkPermission(int requestCode) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
        else
            isPermissionGiven = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isPermissionGiven = true;
        }
    }

    public void openFile() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///" + path));
            startActivity(intent);
        } catch (Exception e) {
            startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
        }
    }

    public void deleteFile() {
        if (file.delete()) {
            Toast.makeText(this, "Файл был успешно удален", Toast.LENGTH_SHORT).show();
            openButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
    }

    private boolean checkConnectivity() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr != null) {
            NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    return true;
                } else return activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            }
        }
        return false;
    }

    private void download() {
        if (!idText.getText().toString().equals("")) {
            if (checkConnectivity()) {
                int id = Integer.parseInt(idText.getText().toString().trim());
                downloadUrl("https://ntv.ifmo.ru/file/journal/" + id + ".pdf");
            } else
                Toast.makeText(this, "Проверьте подключение к интернету", Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this, "Введите idText", Toast.LENGTH_SHORT).show();
    }

    private void downloadUrl(String url) {
        checkPermission(STORAGE_PERMISSION_CODE);
        if (isPermissionGiven) {
            manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(url);
            path = "journals/" + url.replace("https://ntv.ifmo.ru/file/journal/", "");
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, path);
            reference = manager.enqueue(request);
        }
    }
}