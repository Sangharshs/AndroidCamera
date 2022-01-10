package com.example.camerax;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.io.File;

public class Mp3FilesActivity extends AppCompatActivity {


    File storage;
    String[] allpath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mp3_files);

        allpath = StorageUtil.getStorageDirectories(this);


        for(String path:allpath){
            storage = new File(path);
            Method.load_Directory_Files(storage);
        }


    }
}