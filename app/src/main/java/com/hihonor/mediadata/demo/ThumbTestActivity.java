/*
 * Copyright (c) Honor Terminal Co., Ltd. 2023-2023. All rights reserved.
 */

package com.hihonor.mediadata.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.hihonor.mcs.media.datacenter.DataCenterManager;
import com.hihonor.mcs.media.datacenter.DataCenterStore;
import com.hihonor.mcs.media.datacenter.thumbmanager.LocalPortraitCropCallback;
import com.hihonor.mcs.media.datacenter.thumbmanager.ThumbRequest;
import com.hihonor.mcs.media.datacenter.thumbmanager.ThumbResultCallback;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class ThumbTestActivity extends Activity {
    private final static String TAG = "ThumbTestActivity";

    private DataCenterManager mDataCenterManager;

    private Button mConfirmButton;

    private Button mPortraitThumbButton;

    private Button mPortraitThumbButtonCur;

    private EditText mFilePath;

    private EditText mThumbKeyText;

    private RadioButton mMicroType;

    private RadioButton mBigType;

    private RadioButton mInvalidType;

    private ImageView mThumbView;

    private ImageView mPortraitThumbView;

    private int mThumbType = 0;

    private Cursor finalCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thumb_test_cus);
        bindFirst();
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (finalCursor != null) {
            finalCursor.close();
        }
    }

    @SuppressLint("Range")
    private void initView() {
        mMicroType = findViewById(R.id.thumb_type_micro);
        mMicroType.setOnClickListener(v -> mThumbType = DataCenterStore.ThumbType.THUMB_TYPE_MICRO);
        mBigType = findViewById(R.id.thumb_type_big);
        mBigType.setOnClickListener(v -> mThumbType = DataCenterStore.ThumbType.THUMB_TYPE_BIG);

        mInvalidType = findViewById(R.id.invalid_type);
        mInvalidType.setOnClickListener(v -> mThumbType = 0);

        mThumbView = findViewById(R.id.thumbview);

        mPortraitThumbView = findViewById(R.id.portraitThumbView);

        mThumbKeyText = findViewById(R.id.thumb_key);

        mFilePath = findViewById(R.id.file_path);
        mConfirmButton = findViewById(R.id.get_thumb_button);
        mConfirmButton.setOnClickListener(v -> {
            String filePath = mFilePath.getText() == null ? "" : mFilePath.getText().toString();
            String rawKey = mThumbKeyText.getText() == null ? "" : mThumbKeyText.getText().toString();
            if (TextUtils.isEmpty(filePath) && TextUtils.isEmpty(rawKey)) {
                Toast.makeText(this, "请输入文件路径或者key值", Toast.LENGTH_SHORT).show();
                return;
            }
            String thumbKey = rawKey;
            if (TextUtils.isEmpty(thumbKey)) {
                thumbKey = getRawKey(filePath, thumbKey);
            }
            Log.i(TAG, "initView: thumb key = " + thumbKey);
            mDataCenterManager.requestThumbnail(
                    new ThumbRequest("local", thumbKey, mThumbType), new ThumbResultCallback() {
                        @Override
                        public void onThumbRequestSuccess(String s, String s1, int i, Bitmap bitmap) {
                            runOnUiThread(() -> mThumbView.setImageBitmap(bitmap));
                        }

                        @Override
                        public void onThumbRequestFailed(String s, String s1, int i, int i1) {
                            Log.i(TAG, "onThumbRequestFailed: ");
                            runOnUiThread(() -> Toast.makeText(ThumbTestActivity.this, "缩略图请求失败：错误：" + i1, Toast.LENGTH_SHORT)
                                    .show());
                        }
                    });
        });

        Uri uri = DataCenterStore.PortraitAlbums.getLocalPortraitAlbumListUri();
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        List<String> groupList = new ArrayList<>();
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    StringBuilder sb = new StringBuilder();
                    @SuppressLint("Range") String groupTag =
                            cursor.getString(cursor.getColumnIndex(DataCenterStore.PortraitAlbums.PortraitColumns.GROUP_TAG));
                    groupList.add(groupTag);
                    sb.append(groupTag);
                    sb.append(", coverId:").append(cursor.getInt(cursor.getColumnIndex(DataCenterStore.BaseAlbumColumns.MP_ID)));
                }
                cursor.close();
            } catch (Exception e) {
                Log.e(TAG, "" + e.getMessage());
            }
        }
        mPortraitThumbButton = findViewById(R.id.get_portrait_thumb_button);
        SecureRandom rand = new SecureRandom();
        mPortraitThumbButton.setOnClickListener(v -> {
            if (groupList.size() == 0) {
                Toast.makeText(ThumbTestActivity.this, "无人像数据", Toast.LENGTH_SHORT).show();
                return;
            }
            mDataCenterManager.requestLocalCroppedPortrait(
                    groupList.get(rand.nextInt(groupList.size())), new LocalPortraitCropCallback() {
                        @Override
                        public void onPortraitCropSuccess(String groupTag, Bitmap bitmap) {
                            Log.i(TAG, "requestId " + groupTag + " bitmap: " + bitmap.getByteCount());
                            runOnUiThread(() -> mPortraitThumbView.setImageBitmap(bitmap));
                        }

                        @Override
                        public void onPortraitCropFailed(String groupTag, int errorCode) {
                            Log.i(TAG, "onThumbRequestFailed: " + groupTag);
                            Toast.makeText(ThumbTestActivity.this, "缩略图请求失败：错误：" + errorCode, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
        });

        cursor = getContentResolver().query(uri, null, null, null, null);
        mPortraitThumbButtonCur = findViewById(R.id.get_portrait_thumb_button_cursor);
        finalCursor = cursor;
        mPortraitThumbButtonCur.setOnClickListener(v -> {
            try {
                if (finalCursor.isAfterLast()) {
                    finalCursor.moveToFirst();
                }
                finalCursor.moveToNext();
                mDataCenterManager.requestLocalCroppedPortrait(finalCursor, new LocalPortraitCropCallback() {
                    @Override
                    public void onPortraitCropSuccess(String groupTag, Bitmap bitmap) {
                        Log.i(TAG, "requestId " + groupTag + " bitmap: " + bitmap.getByteCount());
                        runOnUiThread(() -> mPortraitThumbView.setImageBitmap(bitmap));
                    }

                    @Override
                    public void onPortraitCropFailed(String groupTag, int errorCode) {
                        Log.i(TAG, "onThumbRequestFailed: " + groupTag);
                        Toast.makeText(ThumbTestActivity.this, "缩略图请求失败：错误：" + errorCode, Toast.LENGTH_SHORT)
                                .show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "" + e.getMessage());
            }
        });
    }

    @SuppressLint("Range")
    private String getRawKey(String filePath, String thumbKey) {
        String finalPath = "/storage/emulated/0/" + filePath;
        Log.i(TAG, "initView: filepath = " + finalPath);
        try (Cursor cursor = getContentResolver().query(DataCenterStore.Images.getLocalUri(),
                new String[]{DataCenterStore.Files.FileColumns.DATA, DataCenterStore.Files.FileColumns.THUMB_RAW_KEY},
                "_data = ?", new String[]{finalPath}, null)) {
            Log.i(TAG, "initView: cursor = " + cursor);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String data = cursor.getString(cursor.getColumnIndex(DataCenterStore.Files.FileColumns.DATA));
                    if (data != null && data.equals(finalPath)) {
                        thumbKey =
                                cursor.getString(cursor.getColumnIndex(DataCenterStore.Files.FileColumns.THUMB_RAW_KEY));
                    }
                }
            }
        }
        if (TextUtils.isEmpty(thumbKey)) {
            try (Cursor cursor = getContentResolver().query(DataCenterStore.Videos.getLocalUri(),
                    new String[]{
                            DataCenterStore.Files.FileColumns.DATA, DataCenterStore.Files.FileColumns.THUMB_RAW_KEY},
                    "_data = ?", new String[]{finalPath}, null)) {
                Log.i(TAG, "initView: cursor = " + cursor);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String data = cursor.getString(cursor.getColumnIndex(DataCenterStore.Files.FileColumns.DATA));
                        if (data != null && data.equals(finalPath)) {
                            thumbKey = cursor.getString(
                                    cursor.getColumnIndex(DataCenterStore.Files.FileColumns.THUMB_RAW_KEY));
                        }
                    }
                }
            }
        }
        return thumbKey;
    }

    private void bindFirst() {
        Toast.makeText(this, "bind", Toast.LENGTH_SHORT).show();
        Log.i("Client", "bind");
        if (mDataCenterManager != null) {
            mDataCenterManager.bindService();
            return;
        }
        mDataCenterManager = new DataCenterManager(this, new DataCenterManager.ClientCallback() {
            @Override
            public void onServiceConnected() {
                Log.i("Client", "onServiceConnected");
            }

            @Override
            public void onServiceDisconnected() {
                Log.i("Client", "onServiceDisconnected");
            }
        });
        mDataCenterManager.bindService();
    }
}