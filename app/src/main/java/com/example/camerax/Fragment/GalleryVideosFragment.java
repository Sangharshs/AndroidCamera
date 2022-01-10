package com.example.camerax.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.camerax.Adapter.VideoItemsAdapter;
import com.example.camerax.Constant;
import com.example.camerax.Method;
import com.example.camerax.R;
import com.example.camerax.StorageUtil;

import java.io.File;

public class GalleryVideosFragment extends Fragment {

    View v;

    RecyclerView recyclerView;
    VideoItemsAdapter adapter;
    private Uri fileUri;

    //
    private boolean permission;
    private File storage;
    private String[] storagePaths;
    public GalleryVideosFragment() {
        // Required empty public constructor
    }

    Button dismissFragBtn;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_gallery_videos, container, false);
        dismissFragBtn = v.findViewById(R.id.dismissBtn);
        recyclerView = v.findViewById(R.id.videosRecyclerview);

        LinearLayoutManager layoutManager =
                new GridLayoutManager(getActivity(), 2, GridLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setNestedScrollingEnabled(false);

        adapter = new VideoItemsAdapter(v.getContext());
        recyclerView.setAdapter(adapter);

        Log.e("ListSize",String.valueOf(Constant.allMediaList.size()));

        storagePaths = StorageUtil.getStorageDirectories(v.getContext());

        for (String path : storagePaths) {
            storage = new File(path);
            Method.load_Directory_Files(storage);
        }

        adapter.notifyDataSetChanged();

        dismissFragBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction().remove(GalleryVideosFragment.this).commit();
                //getActivity().onBackPressed();
            }
        });

        return v;
    }
}