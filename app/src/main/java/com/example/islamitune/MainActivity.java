package com.example.islamitune;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_AUDIO_REQUEST_CODE = 1;

    private ListView listView;
    private Button addButton;
    private ArrayList<String> musicFileNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        addButton = findViewById(R.id.addButton);

        musicFileNames = new ArrayList<>();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker();
            }
        });

        Dexter.withContext(this).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        Toast.makeText(MainActivity.this, "Runtime Permission Given", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                })
                .check();
    }

    private void openFilePicker() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        }

        intent.setType("audio/*");
        startActivityForResult(intent, PICK_AUDIO_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_AUDIO_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                // Check if multiple files are selected
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    handleSelectedFileUris(clipData);
                } else {
                    // Single file selected
                    Uri uri = data.getData();
                    ArrayList<Uri> uris = new ArrayList<>();
                    uris.add(uri);
                    handleSelectedFileUrisList(uris);
                }
            }
        }
    }

    private void handleSelectedFileUris(ClipData clipData) {
        ArrayList<Uri> uris = new ArrayList<>();
        for (int i = 0; i < clipData.getItemCount(); i++) {
            Uri uri = clipData.getItemAt(i).getUri();
            uris.add(uri);
        }

        handleSelectedFileUrisList(uris);
    }

    private void handleSelectedFileUrisList(ArrayList<Uri> uris) {
        for (Uri uri : uris) {
            handleSelectedFileUri(uri);
        }

        updateListView();
    }

    private void handleSelectedFileUri(Uri uri) {
        if (uri != null) {
            // Handle the selected file URI
            Log.d("Selected File URI", uri.toString());

            // Add the file name to the list without the ".mp3" extension
            String fileName = getFileNameWithoutExtension(uri);
            musicFileNames.add(fileName);
        }
    }

    private String getFileNameWithoutExtension(Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    fileName = cursor.getString(displayNameIndex);
                }
            }
        } else if (uri.getScheme().equals("file")) {
            fileName = new File(uri.getPath()).getName();
        }

        // Remove ".mp3" extension if present
        if (fileName != null && fileName.toLowerCase().endsWith(".mp3")) {
            fileName = fileName.substring(0, fileName.length() - 4);
        }

        return fileName;
    }

    private void updateListView() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, musicFileNames);
        listView.setAdapter(adapter);
    }
}

