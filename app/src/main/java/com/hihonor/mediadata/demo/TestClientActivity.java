
package com.hihonor.mediadata.demo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hihonor.mcs.media.datacenter.DataCenterStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class TestClientActivity extends AppCompatActivity {
    private static final String TAG = TestClientActivity.class.getSimpleName();

    private static final String STR_NEW_LINE = "\n";
    private static final String GROUP_TAG_PREFIX = "ser_";
    private static final int MSG_QUERY_FAVOURITE = 2;
    private static final int MSG_QUERY_IMG = 3;
    private static final int MSG_QUERY_VID = 4;
    private static final int MSG_QUERY_PORTRAIT = 5;
    private static final int MSG_QUERY_PORTRAIT_GROUP = 6;
    private static final int MSG_QUERY_GEO = 7;
    private static final int MSG_QUERY_CATEGORY = 8;

    private static final int REQUEST_CODE_IMAGE = 1;
    private static final int REQUEST_CODE_VIDEO = 2;
    private static final int REQUEST_CODE_MANAGER = 3;

    private TestClientViewModel mDataCenterModel;
    private RecyclerView mActionListView;

    private ActionListAdapter mActionAdapter;

    private RecyclerView mEchoListView;

    private EchoListAdapter mEchoAdapter;

    private List<String> mEchoList;

    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("HH:mm:ss:SSS", Locale.getDefault());

    private EditText mFileEditText;
    private ImageButton mClearBtn;

    private HandlerThread mHandlerThread;
    private Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_client);
        initDeviceList();
        initActionList();
        initEchoList();
        initHandlerThread();
    }

    private void initDeviceList() {
        mDataCenterModel = new ViewModelProvider(this).get(TestClientViewModel.class);
    }

    private void initActionList() {
        mActionListView = findViewById(R.id.action_list);
        mActionAdapter = new ActionListAdapter();
        mActionListView.setAdapter(mActionAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        RecyclerView.ItemDecoration itemDecoration = new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.onDraw(c, parent, state);
            }

            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent,
                                       @NonNull RecyclerView.State state) {
                outRect.left = 10;
                outRect.right = 10;
                outRect.top = 10;
                outRect.bottom = 10;
            }
        };
        mActionListView.addItemDecoration(itemDecoration);
        mActionListView.setLayoutManager(gridLayoutManager);
        mFileEditText = findViewById(R.id.file_input_text);
        mFileEditText.setMaxLines(5);
        mClearBtn = findViewById(R.id.clear_img_view);
        mClearBtn.setOnClickListener(v -> mFileEditText.setText(""));
    }

    private void initEchoList() {
        mEchoListView = findViewById(R.id.echo_list);
        mEchoListView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mEchoAdapter = new EchoListAdapter();
        mEchoList = new ArrayList<>();
        mEchoListView.setAdapter(mEchoAdapter);
        mEchoListView.setLayoutManager(new LinearLayoutManager(this));
        mDataCenterModel.getEchoText().observe(this, this::printLog);
    }

    private void initHandlerThread() {
        mHandlerThread = new HandlerThread("TestThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_QUERY_FAVOURITE:
                        queryFavourite();
                        break;
                    case MSG_QUERY_IMG:
                        queryImages();
                        break;
                    case MSG_QUERY_VID:
                        queryVideos();
                        break;
                    case MSG_QUERY_PORTRAIT:
                        queryPortrait();
                        break;
                    case MSG_QUERY_PORTRAIT_GROUP:
                        queryPortraitGroup();
                        break;
                    case MSG_QUERY_GEO:
                        queryGeo();
                        break;
                    case MSG_QUERY_CATEGORY:
                        queryCategory();
                        break;
                    default:
                        break;
                }
            }
        };
        mHandler.postDelayed(this::requestPermission, 1000);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void requestPermission() {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO},
                REQUEST_CODE_IMAGE);
        }
        permission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, new String[]{Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.READ_MEDIA_VIDEO},
                REQUEST_CODE_VIDEO);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_IMAGE:
            case REQUEST_CODE_VIDEO:
                if (grantResults != null && grantResults.length > 0) {
                    boolean result = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    Log.d(TAG, "onRequestPermissionsResult result:" + result + ", requestCode:" + requestCode);
                    Log.d(TAG, "grantResults:" + Arrays.toString(grantResults));
                    if (!result) {
                        Toast.makeText(this, "未同意权限，功能可能有异常", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_MANAGER:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!Environment.isExternalStorageManager()) {
                        Toast.makeText(this, "未同意权限，功能可能有异常", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            default:
                break;
        }
    }

    public static String getMaskedNodeId(String nodeId) {
        String result = nodeId;
        int length = nodeId.length();
        if (length >= 8) {
            result = nodeId.substring(0, 4) + "***" + nodeId.substring(length - 4, length);
        }
        return result;
    }

    static class ActionViewHolder extends RecyclerView.ViewHolder {
        TextView buttonText;

        public ActionViewHolder(@NonNull View itemView) {
            super(itemView);
            buttonText = itemView.findViewById(R.id.button_text);
        }
    }

    class ActionListAdapter extends RecyclerView.Adapter<ActionViewHolder> {
        @NonNull
        @Override
        public ActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_action_list, parent, false);
            ActionViewHolder viewHolder = new ActionViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ActionViewHolder holder, @SuppressLint("RecyclerView") int position) {
            holder.buttonText.setText(mDataCenterModel.getActionList()[position]);
            holder.itemView.setOnClickListener(view -> processClick(view, position));
        }

        @Override
        public int getItemCount() {
            return mDataCenterModel.getActionList().length;
        }
    }

    private void processClick(View view, int position) {
        // Pay attention to the order of @TestClientViewModel.sActionList
        // As I was too lazy to declare an Id for each action
        switch (position) {
            case 0:
                clearEcho();
                break;
            case 1:
                bind();
                break;
            case 2:
                unbind();
                break;
            case 3:
                goToThumbTestActivity();
                break;
            case 4:
                mHandler.sendEmptyMessage(MSG_QUERY_FAVOURITE);
                break;
            case 5:
                mHandler.sendEmptyMessage(MSG_QUERY_IMG);
                break;
            case 6:
                mHandler.sendEmptyMessage(MSG_QUERY_VID);
                break;
            case 7:
                mHandler.sendEmptyMessage(MSG_QUERY_PORTRAIT);
                break;
            case 8:
                mHandler.sendEmptyMessage(MSG_QUERY_PORTRAIT_GROUP);
                break;
            case 9:
                mHandler.sendEmptyMessage(MSG_QUERY_GEO);
                break;
            case 10:
                mHandler.sendEmptyMessage(MSG_QUERY_CATEGORY);
                break;
            case 11:
                search();
                break;
            case 12:
                openLivePhoto();
                break;
            default:
                break;
        }
    }

    private void bind() {
        printLog("Bind Service");
        mDataCenterModel.bind(this.getApplicationContext());
    }

    private void unbind() {
        printLog("Unbind Service");
        mDataCenterModel.unbind();
    }

    private void goToThumbTestActivity() {
        printLog("Go to ThumbTestActivity");
        Intent intent = new Intent(this, ThumbTestActivity.class);
        intent.putExtra("deviceId", "local");
        startActivity(intent);
    }

    private void queryFavourite() {
        String nodeId = "local";
        printLog("queryFavourite for: " + getMaskedNodeId(nodeId));
        try {
            mDataCenterModel.queryFavourite(this, nodeId).ifPresent(cursor -> {
                printData("favourite count:" + cursor.getCount());
                while (cursor.moveToNext()) {
                    StringBuilder sb = new StringBuilder();
                    @SuppressLint("Range") long id =
                            cursor.getLong(cursor.getColumnIndex(DataCenterStore.Files.FileColumns.ID));
                    @SuppressLint("Range") String data =
                            cursor.getString(cursor.getColumnIndex(DataCenterStore.Files.FileColumns.DATA));
                    sb.append(id);
                    sb.append(" | ");
                    sb.append(data);
                    printData(sb.toString());
                }
                cursor.close();
            });
        } catch (Exception e) {
            printLog("error:" + e.getMessage());
        }
    }

    private void queryImages() {
        String nodeId = "local";
        printLog("queryAllImages for: " + getMaskedNodeId(nodeId));
        AtomicLong tempId = new AtomicLong(-1);
        try {
            mDataCenterModel.queryImages(this, nodeId).ifPresent(cursor -> {
                while (cursor.moveToNext()) {
                    StringBuilder sb = new StringBuilder();
                    @SuppressLint("Range") long id =
                            cursor.getLong(cursor.getColumnIndex(DataCenterStore.Files.FileColumns.ID));
                    tempId.set(id);
                    @SuppressLint("Range") String data =
                            cursor.getString(cursor.getColumnIndex(DataCenterStore.Files.FileColumns.DATA));
                    sb.append(id);
                    sb.append(" | ");
                    sb.append(data);
                    printData(sb.toString());
                }
                cursor.close();
            });
        } catch (Exception e) {
            printLog(e.getMessage());
        }
        if (tempId.get() == -1) {
            return;
        }
        printLog("queryOneImage for: " + getMaskedNodeId(nodeId) + " id=" + tempId.get());
        try {
            mDataCenterModel.queryImages(this, nodeId, tempId.get()).ifPresent(cursor -> {
                while (cursor.moveToNext()) {
                    StringBuilder sb = new StringBuilder();
                    @SuppressLint("Range") long id =
                            cursor.getLong(cursor.getColumnIndex(DataCenterStore.Files.FileColumns.ID));
                    @SuppressLint("Range") String data =
                            cursor.getString(cursor.getColumnIndex(DataCenterStore.Files.FileColumns.DATA));
                    sb.append(id);
                    sb.append(" | ");
                    sb.append(data);
                    printData(sb.toString());
                }
                cursor.close();
            });
        } catch (Exception e) {
            printLog(e.getMessage());
        }
    }

    private void queryVideos() {
        String nodeId = "local";
        printLog("queryAllVideos for: " + getMaskedNodeId(nodeId));
        AtomicLong tempId = new AtomicLong(-1);
        try {
            mDataCenterModel.queryVideos(this, nodeId).ifPresent(cursor -> {
                while (cursor.moveToNext()) {
                    StringBuilder sb = new StringBuilder();
                    @SuppressLint("Range") long id =
                            cursor.getLong(cursor.getColumnIndex(DataCenterStore.Files.FileColumns.ID));
                    tempId.set(id);
                    @SuppressLint("Range") String data =
                            cursor.getString(cursor.getColumnIndex(DataCenterStore.Files.FileColumns.DATA));
                    sb.append(id);
                    sb.append(" | ");
                    sb.append(data);
                    printData(sb.toString());
                }
                cursor.close();
            });
        } catch (Exception e) {
            printLog(e.getMessage());
        }
        if (tempId.get() == -1) {
            return;
        }
        printLog("queryOneVideo for: " + getMaskedNodeId(nodeId) + " id=" + tempId.get());
        try {
            mDataCenterModel.queryVideos(this, nodeId, tempId.get()).ifPresent(cursor -> {
                while (cursor.moveToNext()) {
                    StringBuilder sb = new StringBuilder();
                    @SuppressLint("Range") long id =
                            cursor.getLong(cursor.getColumnIndex(DataCenterStore.Files.FileColumns.ID));
                    @SuppressLint("Range") String data =
                            cursor.getString(cursor.getColumnIndex(DataCenterStore.Files.FileColumns.DATA));
                    sb.append(id);
                    sb.append(" | ");
                    sb.append(data);
                    printData(sb.toString());
                }
                cursor.close();
            });
        } catch (Exception e) {
            printLog(e.getMessage());
        }
    }

    @SuppressLint("Range")
    private void queryPortrait() {
        String nodeId = "local";
        printLog("queryPortraitList for: " + getMaskedNodeId(nodeId));
        AtomicReference<String> tempId = new AtomicReference<>("");
        try {
            mDataCenterModel.queryPortrait(this, nodeId).ifPresent(cursor -> {
                printData("query portrait num : " + cursor.getCount());
                while (cursor.moveToNext()) {
                    StringBuilder sb = new StringBuilder();
                    String groupTag =
                            cursor.getString(cursor.getColumnIndex(DataCenterStore.PortraitAlbums.PortraitColumns.GROUP_TAG));
                    tempId.set(groupTag);
                    sb.append(groupTag);
                    printData(sb.toString());
                    printData(" mp_id:" + cursor.getInt(cursor.getColumnIndex(DataCenterStore.BaseAlbumColumns.MP_ID)));
                }
                cursor.close();
            });
        } catch (Exception e) {
            printLog(e.getMessage());
        }
        if (TextUtils.isEmpty(tempId.get())) {
            return;
        }
        printLog("queryOnePortrait for: " + getMaskedNodeId(nodeId) + " id=" + tempId.get());
        try {
            mDataCenterModel.queryPortrait(this, nodeId, tempId.get()).ifPresent(cursor -> {
                printData("count:" + cursor.getCount());
                while (cursor.moveToNext()) {
                    StringBuilder sb = new StringBuilder();
                    long id = cursor.getLong(cursor.getColumnIndex(DataCenterStore.Files.FileColumns.ID));
                    String data = cursor.getString(cursor.getColumnIndex(DataCenterStore.Files.FileColumns.DATA));
                    sb.append(id);
                    sb.append(" | ");
                    sb.append(data);
                    printData(sb.toString());
                }
                cursor.close();
            });
        } catch (Exception e) {
            printLog(e.getMessage());
        }
    }

    @SuppressLint("Range")
    private void queryGeo() {
        String nodeId = "local";
        printLog("queryGeoList for: " + getMaskedNodeId(nodeId));
        AtomicReference<String> tempId = new AtomicReference<>("");
        try {
            mDataCenterModel.queryGeo(this, nodeId).ifPresent(cursor -> {
                printData("query geo album num : " + cursor.getCount());
                while (cursor.moveToNext()) {
                    StringBuilder sb = new StringBuilder();
                    String locality =
                            cursor.getString(cursor.getColumnIndex(DataCenterStore.GeoAlbum.GeoColumns.GEO_NAME));
                    tempId.set(locality);
                    sb.append(locality);
                    sb.append(", coverId:").append(cursor.getInt(cursor.getColumnIndex(DataCenterStore.BaseAlbumColumns.MP_ID)));
                    sb.append(", count:").append(cursor.getInt(cursor.getColumnIndex(DataCenterStore.BaseAlbumColumns.SHOW_COUNT)));
                    printData(sb.toString());
                }
                cursor.close();
            });
        } catch (Exception e) {
            printLog(e.getMessage());
        }
        if (TextUtils.isEmpty(tempId.get())) {
            return;
        }
        printLog("queryOneGeo for: " + getMaskedNodeId(nodeId) + " id=" + tempId.get());
        try {
            mDataCenterModel.queryGeo(this, nodeId, tempId.get()).ifPresent(cursor -> {
                while (cursor.moveToNext()) {
                    StringBuilder sb = new StringBuilder();
                    long id = cursor.getLong(cursor.getColumnIndex(DataCenterStore.Files.FileColumns.ID));
                    String data = cursor.getString(cursor.getColumnIndex(DataCenterStore.Files.FileColumns.DATA));
                    sb.append(id);
                    sb.append(" | ");
                    sb.append(data);
                    printData(sb.toString());
                }
                cursor.close();
            });
        } catch (Exception e) {
            printLog(e.getMessage());
        }
    }

    @SuppressLint("Range")
    private void queryCategory() {
        String nodeId = "local";
        printLog("queryCategoryList for: " + getMaskedNodeId(nodeId));
        AtomicInteger tempId = new AtomicInteger(-1);
        try {
            mDataCenterModel.queryCategory(this, nodeId).ifPresent(cursor -> {
                printData("query category album num : " + cursor.getCount());
                while (cursor.moveToNext()) {
                    StringBuilder sb = new StringBuilder();
                    int id =
                            cursor.getInt(cursor.getColumnIndex(DataCenterStore.CategoryAlbums.CategoryColumns.CATEGORY_ID));
                    tempId.set(id);
                    sb.append(id);
                    sb.append(", mp_id:").append(cursor.getInt(cursor.getColumnIndex(DataCenterStore.BaseAlbumColumns.MP_ID)));
                    sb.append(", count:").append(cursor.getInt(cursor.getColumnIndex(DataCenterStore.BaseAlbumColumns.SHOW_COUNT)));
                    sb.append(", data:").append(cursor.getString(cursor.getColumnIndex(DataCenterStore.Files.FileColumns.DATA)));
                    printData(sb.toString());
                }
                cursor.close();
            });
        } catch (Exception e) {
            printLog(e.getMessage());
        }
        if (tempId.get() == -1) {
            return;
        }
        printLog("queryOneCategory for: " + getMaskedNodeId(nodeId) + " id=" + tempId.get());
        try {
            mDataCenterModel.queryCategory(this, nodeId, tempId.get()).ifPresent(cursor -> {
                printLog("count:" + cursor.getCount());
                while (cursor.moveToNext()) {
                    StringBuilder sb = new StringBuilder();
                    long id = cursor.getLong(cursor.getColumnIndex(DataCenterStore.Files.FileColumns.ID));
                    String data = cursor.getString(cursor.getColumnIndex(DataCenterStore.Files.FileColumns.DATA));
                    sb.append(id);
                    sb.append(" | ");
                    sb.append(data);
                    printData(sb.toString());
                }
                cursor.close();
            });
        } catch (Exception e) {
            printLog(e.getMessage());
        }
    }

    @SuppressLint("Range")
    private void queryPortraitGroup() {
        String str = mFileEditText.getText().toString();
        if (TextUtils.isEmpty(str)) {
            Toast.makeText(this, "请输入人脸group tag", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] groups = str.split(STR_NEW_LINE);
        List<String> list = new ArrayList<>();
        for (String tag : groups) {
            if (tag.startsWith(GROUP_TAG_PREFIX)) {
                list.add(tag);
            }
        }
        try {
            printLog("query portrait group start");
            mDataCenterModel.queryPortraitGroup(this, DataCenterStore.LOCAL_DEVICE, list).ifPresent(cursor -> {
                printData("query portrait group : " + cursor.getCount());
                while (cursor.moveToNext()) {
                    StringBuilder sb = new StringBuilder();
                    long id = cursor.getLong(cursor.getColumnIndex(DataCenterStore.Files.FileColumns.ID));
                    String data = cursor.getString(cursor.getColumnIndex(DataCenterStore.Files.FileColumns.DATA));
                    sb.append(id);
                    sb.append(" | ");
                    sb.append(data);
                    printData(sb.toString());
                }
                cursor.close();
            });
        } catch (Exception e) {
            printLog(e.toString());
        }
        printLog("query portrait group end");
    }

    private void search() {
        printLog("Go to TestSearchActivity");
        Intent intent = new Intent(this, TestSearchActivity.class);
        startActivity(intent);
    }

    private void openLivePhoto() {
        Intent intent = new Intent(this, LivePhotoActivity.class);
        startActivity(intent);
    }

    static class EchoViewHolder extends RecyclerView.ViewHolder {
        TextView echoText;

        public EchoViewHolder(@NonNull View itemView) {
            super(itemView);
            echoText = itemView.findViewById(R.id.echo_text);
        }
    }

    class EchoListAdapter extends RecyclerView.Adapter<EchoViewHolder> {
        @NonNull
        @Override
        public EchoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_echo_list, parent, false);
            EchoViewHolder viewHolder = new EchoViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull EchoViewHolder holder, int position) {
            holder.echoText.setText(mEchoList.get(position));
            holder.itemView.setOnLongClickListener(v -> {
                String str = holder.echoText.getText().toString();
                String editStr = mFileEditText.getText().toString();
                if (TextUtils.isEmpty(editStr)) {
                    mFileEditText.setText(str);
                } else {
                    mFileEditText.setText(editStr + STR_NEW_LINE + str);
                    mFileEditText.setSelection(mFileEditText.getText().length());
                }
                return false;
            });
        }

        @Override
        public int getItemCount() {
            return mEchoList.size();
        }
    }

    private void clearEcho() {
        mEchoList.clear();
        mEchoAdapter.notifyDataSetChanged();
    }

    private void addEcho(String echoText) {
        Log.i(TAG, "echo=" + echoText);
        runOnUiThread(() -> {
            int size = mEchoList.size();
            mEchoList.add(echoText);
            mEchoAdapter.notifyItemInserted(size);
        });
    }

    private void printLog(String log) {
        addEcho(mSimpleDateFormat.format(new Date()) + ": " + log);
        Log.i(TAG, log);
    }

    private void printData(String data) {
        addEcho(data);
        Log.i(TAG, data);
    }
}