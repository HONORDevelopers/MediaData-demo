package com.hihonor.mediadata.demo;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hihonor.mcs.media.datacenter.DataCenterStore;
import com.hihonor.mcs.media.datacenter.livephoto.LivePhoto;
import com.hihonor.mcs.media.datacenter.livephoto.LivePhotoUtils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

public class LivePhotoActivity extends Activity {
    private static final String TAG = "LivePhotoActivity";
    private static final int READ_IMAGE_REQUEST_CODE = 1;
    private static final int READ_VIDEO_REQUEST_CODE = 2;
    private static final int MAX_FRAME_INDEX = 90;
    private ImageView livePhotoJpegView;
    private MediaPlayer mediaPlayer;
    private SurfaceView surfaceView;
    private RecyclerView mEchoListView;
    private EchoListAdapter mEchoAdapter;
    private List<String> mEchoList;
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("HH:mm:ss:SSS", Locale.getDefault());
    private SimpleDateFormat mFileDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault());
    private EditText mEditText;
    private Button mBtnClear;
    private Button mBtnQuery;
    private Button mBtnCheck;
    private Button mBtnDecode;
    private Button mBtnExtractVideo;
    private Button mBtnExtractImage;
    private Button mBtnExtractVideoStream;
    private Button mBtnExtractImageStream;
    private Button mBtnPlay;
    private Button mBtnChoosePic;
    private Button mBtnChooseVid;
    private Button mBtnEncodeCustom;
    private TextView mTvIndex;
    private SeekBar mSeekBar;
    private EditText mEditTextStart;
    private EditText mEditTextDuration;

    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private static final int MSG_QUERY_LIVE_PHOTOS = 1;
    private static final int MSG_CHECK_IS_LIVE_PHOTOS = 2;
    private static final int MSG_DECODE_LIVE_PHOTOS = 3;
    private static final int MSG_EXTRACT_VIDEO = 4;
    private static final int MSG_EXTRACT_IMAGE = 5;
    private static final int MSG_EXTRACT_VIDEO_STREAM = 6;
    private static final int MSG_EXTRACT_IMAGE_STREAM = 7;
    private static final int MSG_ENCODE_CUSTOM = 8;
    private Uri pictureUri;
    private Uri videoUri;
    private int frameIndex = 0;
    private static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_photo);
        initView();
        initMedia();
        initEchoList();
        initHandlerThread();
        context = this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandlerThread.quit();
        context = null;
    }

    private void initView() {
        mEditText = findViewById(R.id.input_text);
        mEditText.setMaxLines(3);

        mBtnClear = findViewById(R.id.btn_clear);
        mBtnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearEcho();
            }
        });
        mBtnQuery = findViewById(R.id.btn_query);
        mBtnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessage(MSG_QUERY_LIVE_PHOTOS);
            }
        });
        mBtnCheck = findViewById(R.id.btn_check);
        mBtnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessage(MSG_CHECK_IS_LIVE_PHOTOS);
            }
        });

        mBtnDecode = findViewById(R.id.btn_decode);
        mBtnDecode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessage(MSG_DECODE_LIVE_PHOTOS);
            }
        });

        mBtnExtractVideo = findViewById(R.id.btn_extractVideo);
        mBtnExtractVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessage(MSG_EXTRACT_VIDEO);
            }
        });
        mBtnExtractImage = findViewById(R.id.btn_extractImage);
        mBtnExtractImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessage(MSG_EXTRACT_IMAGE);
            }
        });

        mBtnExtractVideoStream = findViewById(R.id.btn_video_stream);
        mBtnExtractVideoStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessage(MSG_EXTRACT_VIDEO_STREAM);
            }
        });

        mBtnExtractImageStream = findViewById(R.id.btn_image_stream);
        mBtnExtractImageStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessage(MSG_EXTRACT_IMAGE_STREAM);
            }
        });
        mBtnPlay = findViewById(R.id.btn_codec_play);
        mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String livePhotoPath = mEditText.getText().toString();
                if (TextUtils.isEmpty(livePhotoPath)) {
                    Toast.makeText(LivePhotoActivity.this, "请输入文件路径", Toast.LENGTH_LONG).show();
                    return;
                }
                testMediaCodecPlay(livePhotoPath);
            }
        });

        livePhotoJpegView = findViewById(R.id.live_photo_jpeg_view);
        surfaceView = findViewById(R.id.live_photo_surface);

        mBtnChoosePic = findViewById(R.id.btn_choose_picture);
        mBtnChoosePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/jpeg");
                startActivityForResult(intent, READ_IMAGE_REQUEST_CODE);
            }
        });
        mBtnChooseVid = findViewById(R.id.btn_choose_video);
        mBtnChooseVid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("video/*");
                startActivityForResult(intent, READ_VIDEO_REQUEST_CODE);
            }
        });
        mBtnEncodeCustom = findViewById(R.id.btn_encode_custom);
        mBtnEncodeCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.sendEmptyMessage(MSG_ENCODE_CUSTOM);
            }
        });
        mTvIndex = findViewById(R.id.tv_index);
        mSeekBar = findViewById(R.id.seekbar_index);
        mSeekBar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                frameIndex = MAX_FRAME_INDEX * progress / seekBar.getMax();
                mTvIndex.setText("" + frameIndex);
            }

            @Override
            public void onStartTrackingTouch(android.widget.SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(android.widget.SeekBar seekBar) {
            }
        });
        mEditTextStart = findViewById(R.id.edit_start);
        mEditTextDuration = findViewById(R.id.edit_duration);
    }

    private void initMedia() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
        });
    }

    private void initEchoList() {
        mEchoListView = findViewById(R.id.echo_list);
        mEchoListView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mEchoAdapter = new EchoListAdapter();
        mEchoList = new ArrayList<String>();
        mEchoListView.setAdapter(mEchoAdapter);
        mEchoListView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == READ_IMAGE_REQUEST_CODE) {
                pictureUri = data.getData();
                printLog("pic uri:" + pictureUri.getPath());
            } else if (requestCode == READ_VIDEO_REQUEST_CODE) {
                videoUri = data.getData();
                printLog("vid uri:" + videoUri.getPath());
            }
        }
    }

    private void initHandlerThread() {
        mHandlerThread = new HandlerThread("TestThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_QUERY_LIVE_PHOTOS:
                        queryLivePhotos();
                        break;
                    case MSG_CHECK_IS_LIVE_PHOTOS: {
                        String livePhotoPath = mEditText.getText().toString();
                        if (TextUtils.isEmpty(livePhotoPath)) {
                            Toast.makeText(LivePhotoActivity.this, "请输入文件路径", Toast.LENGTH_LONG).show();
                            return;
                        }
                        testIsLivePhoto(livePhotoPath);
                        testIsLivePhotoByUri(livePhotoPath);
                        break;
                    }
                    case MSG_DECODE_LIVE_PHOTOS: {
                        String livePhotoPath = mEditText.getText().toString();
                        if (TextUtils.isEmpty(livePhotoPath)) {
                            Toast.makeText(LivePhotoActivity.this, "请输入文件路径", Toast.LENGTH_LONG).show();
                            return;
                        }
                        testDecodeByPath(livePhotoPath);
                        testDecodeByUri(livePhotoPath);
                        testPlay(livePhotoPath);
                        break;
                    }
                    case MSG_EXTRACT_VIDEO: {
                        String livePhotoPath = mEditText.getText().toString();
                        if (TextUtils.isEmpty(livePhotoPath)) {
                            Toast.makeText(LivePhotoActivity.this, "请输入文件路径", Toast.LENGTH_LONG).show();
                            return;
                        }
                        testExtractVideoByPath(livePhotoPath);
                        testExtractVideoByUri(livePhotoPath);
                        break;
                    }
                    case MSG_EXTRACT_IMAGE: {
                        String livePhotoPath = mEditText.getText().toString();
                        if (TextUtils.isEmpty(livePhotoPath)) {
                            Toast.makeText(LivePhotoActivity.this, "请输入文件路径", Toast.LENGTH_LONG).show();
                            return;
                        }
                        testExtractImageByPath(livePhotoPath);
                        testExtractImageByUri(livePhotoPath);
                        break;
                    }
                    case MSG_EXTRACT_VIDEO_STREAM: {
                        String livePhotoPath = mEditText.getText().toString();
                        if (TextUtils.isEmpty(livePhotoPath)) {
                            Toast.makeText(LivePhotoActivity.this, "请输入文件路径", Toast.LENGTH_LONG).show();
                            return;
                        }
                        testExtractVideoStreamByPath(livePhotoPath);
                        testExtractVideoStreamByUri(livePhotoPath);
                        break;
                    }
                    case MSG_EXTRACT_IMAGE_STREAM: {
                        String livePhotoPath = mEditText.getText().toString();
                        if (TextUtils.isEmpty(livePhotoPath)) {
                            Toast.makeText(LivePhotoActivity.this, "请输入文件路径", Toast.LENGTH_LONG).show();
                            return;
                        }
                        testExtractImageStreamByPath(livePhotoPath);
                        testExtractImageStreamByUri(livePhotoPath);
                        break;
                    }
                    case MSG_ENCODE_CUSTOM: {
                        if (pictureUri == null || videoUri == null) {
                            Toast.makeText(LivePhotoActivity.this, "请先选择图片、视频", Toast.LENGTH_LONG).show();
                            return;
                        }
                        testEncodeCustom();
                        break;
                    }
                    default:
                        break;
                }
            }
        };
    }


    @SuppressLint("Range")
    private void queryLivePhotos() {
        printLog("queryLivePhotos ");
        AtomicLong tempId = new AtomicLong(-1);
        Uri uri = DataCenterStore.LivePhotos.getLocalUri();
        long start = System.currentTimeMillis();
        Bundle queryArgs = new Bundle();
        queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, "_id DESC");
        Cursor cursor = getContentResolver().query(uri, null, queryArgs, null);

        if (cursor != null) {
            printLog("LivePhotos number:" + cursor.getCount() + ",time:" + (System.currentTimeMillis() - start));
            while (cursor.moveToNext()) {
                StringBuilder sb = new StringBuilder();
                long id = cursor.getLong(cursor.getColumnIndex(DataCenterStore.Files.FileColumns.ID));
                tempId.set(id);
                String data = cursor.getString(cursor.getColumnIndex(DataCenterStore.Files.FileColumns.DATA));
                sb.append(data);
                printData(sb.toString());
            }
            cursor.close();
        }
        printLog("queryLivePhotos end");
        if (tempId.get() == -1) {
            return;
        }
        printLog("queryOneLivePhoto for id = " + tempId.get());
        uri = DataCenterStore.LivePhotos.getLocalUri(tempId.get());
        cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
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
        }
        printLog("queryOneLivePhoto for id end");
    }

    private void testIsLivePhoto(String livePhotoPath) {
        long startTime = System.currentTimeMillis();
        boolean res = false;
        try {
            res = LivePhotoUtils.isLivePhoto(livePhotoPath);
        } catch (IOException e) {
            Log.w(TAG, "test IsLivePhoto ByPath IOException:" + e.getMessage());
            printLog("test IsLivePhoto ByPath IOException:" + e.getMessage());
        }
        long endTime = System.currentTimeMillis();
        printLog("test IsLivePhoto ByPath cost time:" + (endTime - startTime));
        printLog("test IsLivePhoto ByPath result:" + res);
    }

    private void testIsLivePhotoByUri(String livePhotoPath) {
        Uri uri = Uri.fromFile(new File(livePhotoPath));
        long startTime = System.currentTimeMillis();
        boolean res = false;
        try {
            res = LivePhotoUtils.isLivePhoto(this, uri);
        } catch (IOException e) {
            Log.w(TAG, "test IsLivePhoto ByUri IOException:" + e.getMessage());
            printLog("test IsLivePhoto ByUri IOException:" + e.getMessage());
        }
        long endTime = System.currentTimeMillis();
        printLog("test IsLivePhoto ByUri cost time:" + (endTime - startTime));
        printLog("test IsLivePhoto ByUri result:" + res);
    }

    private void testDecodeByPath(String livePhotoPath) {
        long startTime = System.currentTimeMillis();
        LivePhoto livePhoto = null;
        try {
            livePhoto = LivePhotoUtils.decode(livePhotoPath);
        } catch (IOException e) {
            printLog("testDecodeByPath " + e.getMessage());
        }
        long endTime = System.currentTimeMillis();
        printLog("test Decode ByPath cost time:" + (endTime - startTime));
        if (livePhoto == null) {
            printLog("test Decode ByPath livePhoto: Null");
        } else {
            printLog("LivePhoto start:" + livePhoto.getStart() + ",duration:" + livePhoto.getDuration()
                    + ",index:" + livePhoto.getFrameIndex() + ",videoOffset:" + livePhoto.getVideoOffset()
                    + ",videoLength:" + livePhoto.getVideoLength());
        }
    }

    private void testDecodeByUri(String livePhotoPath) {
        Uri uri = Uri.fromFile(new File(livePhotoPath));
        long startTime = System.currentTimeMillis();
        LivePhoto livePhoto = null;
        try {
            livePhoto = LivePhotoUtils.decode(this, uri);
        } catch (IOException e) {
            printLog("testDecodeByUri " + e.getMessage());
        }
        long endTime = System.currentTimeMillis();
        printLog("test Decode ByUri cost time:" + (endTime - startTime));
        if (livePhoto == null) {
            printLog("testDecodeByUri livePhoto: Null");
        } else {
            printLog("LivePhoto start:" + livePhoto.getStart() + ",duration:" + livePhoto.getDuration()
                    + ",index:" + livePhoto.getFrameIndex() + ",videoOffset:" + livePhoto.getVideoOffset()
                    + ",videoLength:" + livePhoto.getVideoLength());
        }
    }

    private void testExtractVideoByPath(String livePhotoPath) {
        long startTime = System.currentTimeMillis();
        boolean res = false;
        File videoFile = null;
        try {
            videoFile = createFile(MEDIA_TYPE_VIDEO);
            if (videoFile == null) {
                printLog("test extractVideo create file error");
                return;
            }
            printLog("video:" + videoFile.getPath());
            startTime = System.currentTimeMillis();
            res = LivePhotoUtils.extractVideo(livePhotoPath, videoFile);
        } catch (IOException e) {
            printLog("test extractVideo " + e.getMessage());
        }
        long endTime = System.currentTimeMillis();
        printLog("test extractVideo ByPath cost time:" + (endTime - startTime) + " res:" + res);
        if (res && videoFile != null) {
            scanFile(videoFile.getPath());
        }
    }

    private void testExtractVideoByUri(String livePhotoPath) {
        Uri uri = Uri.fromFile(new File(livePhotoPath));
        long startTime = System.currentTimeMillis();
        boolean res = false;
        File videoFile = null;
        try {
            videoFile = createFile(MEDIA_TYPE_VIDEO);
            if (videoFile == null) {
                printLog("test extractVideo create file error");
                return;
            }
            printLog("video:" + videoFile.getPath());
            startTime = System.currentTimeMillis();
            res = LivePhotoUtils.extractVideo(this, uri, videoFile);
        } catch (IOException e) {
            printLog("test extractVideo " + e.getMessage());
        }
        long endTime = System.currentTimeMillis();
        printLog("test extractVideo ByUri cost time:" + (endTime - startTime) + " res:" + res);
        if (res && videoFile != null) {
            scanFile(videoFile.getPath());
        }
    }

    private void testExtractImageByPath(String livePhotoPath) {
        long startTime = System.currentTimeMillis();
        boolean res = false;
        File imageFile = null;
        try {
            imageFile = createFile(MEDIA_TYPE_IMAGE);
            if (imageFile == null) {
                printLog("test extractImage create file error");
                return;
            }
            printLog("image:" + imageFile.getPath());
            startTime = System.currentTimeMillis();
            res = LivePhotoUtils.extractImage(livePhotoPath, imageFile);
        } catch (IOException e) {
            printLog("test extractImage " + e.getMessage());
        }
        long endTime = System.currentTimeMillis();
        printLog("test extractImage ByPath cost time:" + (endTime - startTime) + " res:" + res);
        if (res && imageFile != null) {
            scanFile(imageFile.getPath());
        }
    }

    private void testExtractImageByUri(String livePhotoPath) {
        Uri uri = Uri.fromFile(new File(livePhotoPath));
        long startTime = System.currentTimeMillis();
        boolean res = false;
        File imageFile = null;
        try {
            imageFile = createFile(MEDIA_TYPE_IMAGE);
            if (imageFile == null) {
                printLog("test extractImage create file error");
                return;
            }
            printLog("image:" + imageFile.getPath());
            startTime = System.currentTimeMillis();
            res = LivePhotoUtils.extractImage(this, uri, imageFile);
        } catch (IOException e) {
            printLog("test extractImage " + e.getMessage());
        }
        long endTime = System.currentTimeMillis();
        printLog("test extractImage ByUri cost time:" + (endTime - startTime) + " res:" + res);
        if (res && imageFile != null) {
            scanFile(imageFile.getPath());
        }
    }

    private void testExtractVideoStreamByPath(String livePhotoPath) {
        try {
            File videoFile = createFile(MEDIA_TYPE_VIDEO);
            if (videoFile == null) {
                printLog("test extractVideo create file error");
                return;
            }
            printLog("video:" + videoFile.getPath());
            long startTime = System.currentTimeMillis();
            InputStream in = LivePhotoUtils.extractVideoStream(livePhotoPath);
            long endTime = System.currentTimeMillis();
            printLog("test extractVideoStream ByPath cost time:" + (endTime - startTime));
            boolean res = saveInputStreamToFile(in, videoFile);
            printLog("save file:" + res);
            if (res) {
                scanFile(videoFile.getPath());
            }
        } catch (IOException e) {
            printLog("test extractVideo " + e.getMessage());
        }
    }

    private void testExtractVideoStreamByUri(String livePhotoPath) {
        Uri uri = Uri.fromFile(new File(livePhotoPath));
        try {
            File videoFile = createFile(MEDIA_TYPE_VIDEO);
            if (videoFile == null) {
                printLog("test extractVideo create file error");
                return;
            }
            printLog("video:" + videoFile.getPath());
            long startTime = System.currentTimeMillis();
            InputStream in = LivePhotoUtils.extractVideoStream(this, uri);
            long endTime = System.currentTimeMillis();
            printLog("test extractVideoStream ByUri cost time:" + (endTime - startTime));
            boolean res = saveInputStreamToFile(in, videoFile);
            printLog("save file:" + res);
            if (res) {
                scanFile(videoFile.getPath());
            }
        } catch (IOException e) {
            printLog("test extractVideo " + e.getMessage());
        }
    }

    private void testExtractImageStreamByPath(String livePhotoPath) {
        try {
            File imageFile = createFile(MEDIA_TYPE_IMAGE);
            if (imageFile == null) {
                printLog("test extractImage create file error");
                return;
            }
            printLog("image:" + imageFile.getPath());
            long startTime = System.currentTimeMillis();
            InputStream in = LivePhotoUtils.extractImageStream(livePhotoPath);
            long endTime = System.currentTimeMillis();
            printLog("test extractImageStream ByPath cost time:" + (endTime - startTime));
            boolean res = saveInputStreamToFile(in, imageFile);
            printLog("save file:" + res);
            if (res) {
                scanFile(imageFile.getPath());
            }
        } catch (IOException e) {
            printLog("test extractImage " + e.getMessage());
        }
    }

    private void testExtractImageStreamByUri(String livePhotoPath) {
        Uri uri = Uri.fromFile(new File(livePhotoPath));
        try {
            File imageFile = createFile(MEDIA_TYPE_IMAGE);
            if (imageFile == null) {
                printLog("test extractImage create file error");
                return;
            }
            printLog("image:" + imageFile.getPath());
            long startTime = System.currentTimeMillis();
            InputStream in = LivePhotoUtils.extractImageStream(this, uri);
            long endTime = System.currentTimeMillis();
            printLog("test extractImageStream ByUri cost time:" + (endTime - startTime));
            boolean res = saveInputStreamToFile(in, imageFile);
            printLog("save file:" + res);
            if (res) {
                scanFile(imageFile.getPath());
            }
        } catch (IOException e) {
            printLog("test extractImage " + e.getMessage());
        }
    }

    private void testPlay(String livePhotoPath) {
        try {
            if (LivePhotoUtils.isLivePhoto(livePhotoPath)) {
                // 加载并显示图片
                LivePhoto livePhoto = LivePhotoUtils.decode(livePhotoPath);
                mediaPlayer.reset();
                FileInputStream in = new FileInputStream(livePhotoPath);
                mediaPlayer.setDataSource(in.getFD(), livePhoto.getVideoOffset(), livePhoto.getVideoLength());
                mediaPlayer.setDisplay(surfaceView.getHolder());
                mediaPlayer.setLooping(false);
                mediaPlayer.prepare();
                InputStream imgStream = LivePhotoUtils.extractImageStream(livePhotoPath);
                Bitmap bitmap = BitmapFactory.decodeStream(imgStream);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        livePhotoJpegView.setImageBitmap(bitmap);
                    }
                });
            } else {
                Toast.makeText(this, "不是动态照片", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Log.w(TAG, e.getMessage());
            printLog(e.getMessage());
        }
    }

    private void testEncodeCustom() {
        LivePhoto livePhoto = new LivePhoto();
        livePhoto.setFrameIndex(frameIndex);
        String strStart = mEditTextStart.getText().toString();
        if (!TextUtils.isEmpty(strStart)) {
            livePhoto.setStart(Integer.parseInt(strStart));
        }
        String strDuration = mEditTextDuration.getText().toString();
        if (!TextUtils.isEmpty(strDuration)) {
            livePhoto.setDuration(Integer.parseInt(strDuration));
        }
        printLog("LivePhoto start:" + livePhoto.getStart() + ",duration:" + livePhoto.getDuration()
                + ",index:" + livePhoto.getFrameIndex());
        ContentResolver contentResolver = getContentResolver();
        InputStream photoStream = null;
        InputStream videoStream = null;
        try {
            photoStream = contentResolver.openInputStream(pictureUri);
            videoStream = contentResolver.openInputStream(videoUri);
            livePhoto.setPhoto(photoStream);
            livePhoto.setVideo(videoStream);
            File file = createFile(MEDIA_TYPE_IMAGE, true);
            if (file == null) {
                return;
            }
            printLog("encode file:" + file.getPath());
            boolean res = LivePhotoUtils.encode(file, livePhoto);
            printLog("encode result:" + res);
            if (res) {
                scanFile(file.getPath());
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            closeSilently(photoStream);
            closeSilently(videoStream);
        }
    }

    private File createFile(int fileType) {
        return createFile(fileType, false);
    }

    private File createFile(int fileType, boolean live) {
        String time = mFileDateFormat.format(new Date());
        String filePath = "";
        if (fileType == MEDIA_TYPE_IMAGE) {
            if (live) {
                filePath = getExternalFilesDir(null) + "/Live_" + time + ".jpg";
            } else {
                filePath = getExternalFilesDir(null) + "/" + time + ".jpg";
            }
        } else if (fileType == MEDIA_TYPE_VIDEO) {
            filePath = getExternalFilesDir(null) + "/" + time + ".mp4";
        }
        Log.i(TAG, "createFile filePath:" + filePath);
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            return file;
        } catch (IOException e) {
            Log.w(TAG, e.getMessage());
            printLog("test createFile " + e.getMessage());
        }
        return null;
    }

    private static boolean saveInputStreamToFile(InputStream inputStream, File savePath) {
        if (inputStream == null || savePath == null) {
            return false;
        }
        OutputStream outputStream = null;
        long totalLen = 0;
        try {
            outputStream = new FileOutputStream(savePath);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
                totalLen += length;
            }
            scanFile(savePath.getCanonicalPath());
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        Log.d(TAG, "totalLen:" + totalLen);
        if (totalLen <= 0) {
            return false;
        }
        return true;
    }

    private Uri getUriFromInputStream(InputStream videoData) {
        try {
            // 将InputStream转换为byte数组
            byte[] data = readInputStream(videoData);
            // 将byte数组保存为文件
            File videoFile = createTempFile(data);
            // 返回文件的Uri
            return Uri.fromFile(videoFile);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            printLog(e.getMessage());
        }
        return null;
    }

    private byte[] readInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    private File createTempFile(byte[] data) throws IOException {
        File videoFile = File.createTempFile("video", ".tmp", getCacheDir());
        FileOutputStream fos = new FileOutputStream(videoFile);
        fos.write(data);
        fos.close();
        return videoFile;
    }

    private boolean loadLivePhoto(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        // 加载图片
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        // 设置ImageView的图片
        if (bitmap != null) {
            livePhotoJpegView.setImageBitmap(bitmap);
        }
        return true;
    }

    private static void scanFile(String path) {
        try {
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(new File(path)));
            context.sendBroadcast(intent);
        } catch (Throwable t) {
            Log.e(TAG, t.getMessage());
        }
    }

    class EchoViewHolder extends RecyclerView.ViewHolder {
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
                if (!TextUtils.isEmpty(str)) {
                    mEditText.setText(str);
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
        Log.d(TAG, "echo=" + echoText);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int size = mEchoList.size();
                mEchoList.add(echoText);
                mEchoAdapter.notifyItemInserted(size);
            }
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

    private static void closeSilently(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Throwable t) {
            Log.w(TAG, "close fail.");
        }
    }

    private void testMediaCodecPlay(String path) {
        try {
            if (!LivePhotoUtils.isLivePhoto(path)) {
                return;
            }
            LivePhoto livePhoto = LivePhotoUtils.decode(path);
            if (livePhoto == null) {
                return;
            }
            InputStream imgStream = LivePhotoUtils.extractImageStream(path);
            Bitmap bitmap = BitmapFactory.decodeStream(imgStream);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    livePhotoJpegView.setImageBitmap(bitmap);
                }
            });
            testMediaCodecPlayAudio(path, livePhoto);
            testMediaCodecPlayVideo(path, livePhoto);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void testMediaCodecPlayVideo(String path, LivePhoto livePhoto) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MediaExtractor extractor = new MediaExtractor();
                    FileInputStream inputStream = new FileInputStream(path);
                    FileDescriptor fd = inputStream.getFD();
                    Log.i(TAG, "zpy offset:" + livePhoto.getVideoOffset() + " len:" + livePhoto.getVideoLength());
                    extractor.setDataSource(fd, livePhoto.getVideoOffset(), livePhoto.getVideoLength());
                    int videoTrackIndex = -1;
                    for (int i = 0; i < extractor.getTrackCount(); i++) {
                        MediaFormat format = extractor.getTrackFormat(i);
                        String mime = format.getString(MediaFormat.KEY_MIME);
                        if (mime.startsWith("video/")) {
                            videoTrackIndex = i;
                            break;
                        }
                    }

                    MediaFormat format = extractor.getTrackFormat(videoTrackIndex);
                    extractor.selectTrack(videoTrackIndex);
                    String mime = format.getString(MediaFormat.KEY_MIME);
                    MediaCodec codec = MediaCodec.createDecoderByType(mime);
                    codec.configure(format, surfaceView.getHolder().getSurface(), null, 0);
                    codec.start();
                    boolean isEOS = false;
                    boolean firstFrame = true;
                    long startWhen = 0;
                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                    while (!isEOS) {
                        int inputIndex = codec.dequeueInputBuffer(10000); // 10ms超时
                        if (inputIndex >= 0) {
                            ByteBuffer inputBuffer = codec.getInputBuffer(inputIndex);
                            int sampleSize = extractor.readSampleData(inputBuffer, 0);
                            if (sampleSize < 0) {
                                // 输入结束，发送end-of-stream标志
                                codec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            } else {
                                long presentationTimeUs = extractor.getSampleTime();
                                Log.i(TAG,
                                        "zpy sampleSize:" + sampleSize + " presentationTimeUs:" + presentationTimeUs);
                                codec.queueInputBuffer(inputIndex, 0, sampleSize, presentationTimeUs, 0);
                                extractor.advance();
                            }
                        }

                        int outputIndex = codec.dequeueOutputBuffer(bufferInfo, 10000);
                        if (outputIndex >= 0) {
                            if (firstFrame) {
                                startWhen = System.currentTimeMillis();
                                firstFrame = false;
                            }
                            long sleepTime =
                                    bufferInfo.presentationTimeUs / 1000 - (System.currentTimeMillis() - startWhen);
                            Log.d(
                                    TAG,
                                    "info.presentationTimeUs : " + (bufferInfo.presentationTimeUs / 1000) + " " +
                                            "playTime: " + (System.currentTimeMillis() - startWhen) + " sleepTime : " + sleepTime
                            );
                            if (sleepTime > 0) Thread.sleep(sleepTime);
                            codec.releaseOutputBuffer(outputIndex, true);
                            Log.i(TAG, "zpy outputIndex---");
                            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                Log.i(TAG, "zpy end");
                                // 解码结束
                                break;
                            }
                        }
                    }
                    codec.stop();
                    codec.release();
                    extractor.release();
                    inputStream.close();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void testMediaCodecPlayAudio(String path, LivePhoto livePhoto) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MediaExtractor extractor = new MediaExtractor();
                    FileInputStream inputStream = new FileInputStream(path);
                    FileDescriptor fd = inputStream.getFD();
                    extractor.setDataSource(fd, livePhoto.getVideoOffset(), livePhoto.getVideoLength());
                    int trackIndex = 0;
                    for (int i = 0; i < extractor.getTrackCount(); i++) {
                        MediaFormat format = extractor.getTrackFormat(i);
                        String mime = format.getString(MediaFormat.KEY_MIME);
                        if (mime.startsWith("audio/")) {
                            trackIndex = i;
                            break;
                        }
                    }

                    MediaFormat format = extractor.getTrackFormat(trackIndex);
                    extractor.selectTrack(trackIndex);
                    String mime = format.getString(MediaFormat.KEY_MIME);
                    MediaCodec codec = MediaCodec.createDecoderByType(mime);
                    codec.configure(format, null, null, 0);
                    // 声道数
                    int audioChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                    int sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                    long duration = format.getLong(MediaFormat.KEY_DURATION);
                    int coding = AudioFormat.ENCODING_PCM_16BIT;
                    AudioTrack audioTrack = new AudioTrack(
                            new AudioAttributes.Builder()
                                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                                    .build(),
                            new AudioFormat.Builder()
                                    .setChannelMask(audioChannels == 1 ? AudioFormat.CHANNEL_OUT_MONO :
                                            AudioFormat.CHANNEL_OUT_STEREO)
                                    .setEncoding(coding)
                                    .setSampleRate(sampleRate)
                                    .build(),
                            AudioTrack.getMinBufferSize(sampleRate,
                                    audioChannels == 1 ? AudioFormat.CHANNEL_OUT_MONO :
                                            AudioFormat.CHANNEL_OUT_STEREO, coding),
                            AudioTrack.MODE_STREAM,
                            AudioManager.AUDIO_SESSION_ID_GENERATE);
                    //启动AudioTrack
                    audioTrack.play();
                    codec.start();

                    ByteBuffer[] inputBuffers = codec.getInputBuffers();
                    ByteBuffer[] outputBuffers = codec.getOutputBuffers();

                    MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                    boolean isEOF = false;
                    long startMs = System.currentTimeMillis();
                    while (!isEOF) {
                        int index = codec.dequeueInputBuffer(0);
                        if (index >= 0) {
                            ByteBuffer byteBuffer = inputBuffers[index];
                            Log.d("lpf", "bytebuffer is " + byteBuffer);
                            //从MediaExtractor中读取一帧待解数据
                            int sampleSize = extractor.readSampleData(byteBuffer, 0);
                            Log.d("lpf", "sampleSize is " + sampleSize);
                            if (sampleSize < 0) {
                                Log.d("lpf", "inputBuffer is BUFFER_FLAG_END_OF_STREAMING");
                                codec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                isEOF = true;
                            } else {
                                //向MediaDecoder输入一帧待解码数据
                                codec.queueInputBuffer(index, 0, sampleSize, extractor.getSampleTime(), 0);
                                extractor.advance();  //下一帧数据
                            }
                        }
                        int outIndex = codec.dequeueOutputBuffer(info, 100000);
                        if (outIndex >= 0) {
                            ByteBuffer outBuffer = outputBuffers[outIndex];
                            //Log.v(TAG, "outBuffer: " + outBuffer);

                            final byte[] out = new byte[info.size];
                            // Read the buffer all at once
                            outBuffer.get(out);
                            //清空buffer,否则下一次得到的还会得到同样的buffer
                            outBuffer.clear();
                            // AudioTrack write data
                            audioTrack.write(out, info.offset, info.offset + info.size);
                            codec.releaseOutputBuffer(outIndex, true);
                            Log.i(TAG, "zpy outIndex---");
                            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                Log.i(TAG, "zpy audio end");
                                // 解码结束
                                break;
                            }
                        }
                    }
                    audioTrack.stop();
                    audioTrack.release();
                    codec.stop();
                    codec.release();
                    extractor.release();
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
