
package com.hihonor.mediadata.demo;

import android.app.Activity;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hihonor.mcs.media.datacenter.DataCenterManager;
import com.hihonor.mcs.media.datacenter.search.MediaData;
import com.hihonor.mcs.media.datacenter.search.Recommendation;
import com.hihonor.mcs.media.datacenter.search.SearchRequest;
import com.hihonor.mcs.media.datacenter.search.SearchResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TestSearchActivity extends Activity {
    private static final String TAG = TestSearchActivity.class.getSimpleName();

    private static final String STR_NEW_LINE = "\r\n";
    private static final String STR_SEPARATOR = ",";
    private static final int MSG_BIND = 1;
    private static final int MSG_GET_FEATURE = 2;
    private static final int MSG_SEARCH_COUNT = 3;
    private static final int MSG_INIT_SEARCH = 4;
    private static final int MSG_RELEASE_SEARCH = 5;
    private static final int MSG_SEARCH_RECOM = 6;
    private static final int MSG_SEARCH_COVER = 7;
    private static final int MSG_SEARCH_MEDIA = 8;
    private static final int MSG_SEARCH_SCOPE = 9;

    private RadioButton mImageType;

    private RadioButton mVideoType;

    private RadioButton mAllType;

    private int mMediaType = -1;
    private EditText mSearchWord;
    private Button mBtnBindService;
    private Button mBtnFeature;
    private Button mBtnClear;
    private Button mBtnInit;
    private Button mBtnRelease;
    private Button mBtnSearchCount;
    private Button mBtnSearchRecom;
    private Button mBtnSearchRecomCover;
    private Button mBtnSearchMedia;
    private Button mBtnSearchScope;
    private int mScope = SearchRequest.SCOPE_ALL;
    private RecyclerView mEchoListView;
    private EchoListAdapter mEchoAdapter;
    private List<String> mEchoList;
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("HH:mm:ss:SSS", Locale.getDefault());
    private EditText mRecommendationEditText;
    private ImageButton mClearBtn;
    private HandlerThread mHandlerThread;
    private Handler mHandler;

    private DataCenterManager mDataCenterManager;
    private boolean mConnect;

    private boolean initFeature;
    private boolean mSupport;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_search);
        initView();
        initEchoList();
        initHandlerThread();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandlerThread.quit();
    }

    private void initView() {
        mImageType = findViewById(R.id.media_type_image);
        mImageType.setOnClickListener(v -> {
            mMediaType = DataCenterManager.MEDIA_TYPE_IMAGE;
        });
        mVideoType = findViewById(R.id.media_type_video);
        mVideoType.setOnClickListener(v -> {
            mMediaType = DataCenterManager.MEDIA_TYPE_VIDEO;
        });
        mAllType = findViewById(R.id.media_type_all);
        mAllType.setOnClickListener(v -> {
            mMediaType = DataCenterManager.MEDIA_TYPE_ALL;
        });
        if (mImageType.isChecked()) {
            mMediaType = DataCenterManager.MEDIA_TYPE_IMAGE;
        }
        if (mVideoType.isChecked()) {
            mMediaType = DataCenterManager.MEDIA_TYPE_VIDEO;
        }
        if (mAllType.isChecked()) {
            mMediaType = DataCenterManager.MEDIA_TYPE_ALL;
        }
        mSearchWord = findViewById(R.id.search_word);
        mRecommendationEditText = findViewById(R.id.recommendation_input_text);
        mRecommendationEditText.setMaxLines(5);
        mClearBtn = findViewById(R.id.clear_img_view);
        mClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecommendationEditText.setText("");
            }
        });

        mBtnBindService = findViewById(R.id.btn_bind);
        mBtnBindService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessage(MSG_BIND);
            }
        });
        mBtnFeature = findViewById(R.id.btn_feature);
        mBtnFeature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessage(MSG_GET_FEATURE);
            }
        });
        mBtnSearchCount = findViewById(R.id.btn_search);
        mBtnSearchCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessage(MSG_SEARCH_COUNT);
            }
        });
        mBtnSearchRecom = findViewById(R.id.btn_search_recom);
        mBtnSearchRecom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessage(MSG_SEARCH_RECOM);
            }
        });
        mBtnSearchRecomCover = findViewById(R.id.btn_search_cover);
        mBtnSearchRecomCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessage(MSG_SEARCH_COVER);
            }
        });
        mBtnSearchMedia = findViewById(R.id.btn_search_media);
        mBtnSearchMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessage(MSG_SEARCH_MEDIA);
            }
        });

        mBtnClear = findViewById(R.id.btn_clear);
        mBtnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearEcho();
            }
        });
        mBtnInit = findViewById(R.id.btn_init);
        mBtnInit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessage(MSG_INIT_SEARCH);
            }
        });
        mBtnRelease = findViewById(R.id.btn_release);
        mBtnRelease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessage(MSG_RELEASE_SEARCH);
            }
        });

        RadioButton allBtn = findViewById(R.id.scope_all);
        allBtn.setOnClickListener(v -> {
            mScope = SearchRequest.SCOPE_ALL;
        });
        RadioButton locationBtn = findViewById(R.id.scope_location);
        locationBtn.setOnClickListener(v -> {
            mScope = SearchRequest.SCOPE_LOCATION;
        });
        RadioButton categoryBtn = findViewById(R.id.scope_category);
        categoryBtn.setOnClickListener(v -> {
            mScope = SearchRequest.SCOPE_CATEGORY;
        });
        RadioButton ocrBtn = findViewById(R.id.scope_ocr);
        ocrBtn.setOnClickListener(v -> {
            mScope = SearchRequest.SCOPE_OCR;
        });
        mBtnSearchScope = findViewById(R.id.btn_search_new);
        mBtnSearchScope.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessage(MSG_SEARCH_SCOPE);
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

    private void initHandlerThread() {
        mHandlerThread = new HandlerThread("TestThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_BIND:
                        connect();
                        break;
                    case MSG_GET_FEATURE:
                        getFeature();
                        break;
                    case MSG_SEARCH_COUNT:
                        searchCount();
                        break;
                    case MSG_INIT_SEARCH:
                        initSearch();
                        break;
                    case MSG_RELEASE_SEARCH:
                        releaseSearch();
                        break;
                    case MSG_SEARCH_RECOM:
                        searchRecommendation();
                        break;
                    case MSG_SEARCH_COVER:
                        searchRecomCover();
                        break;
                    case MSG_SEARCH_MEDIA:
                        searchMedia();
                        break;
                    case MSG_SEARCH_SCOPE:
                        searchScope();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void connect() {
        if (mDataCenterManager == null) {
            long start = System.currentTimeMillis();
            mDataCenterManager = new DataCenterManager(this, new DataCenterManager.ClientCallback() {
                @Override
                public void onServiceConnected() {
                    mConnect = true;
                    Log.i(TAG, "kit time onServiceConnected :" + (System.currentTimeMillis() - start));
                    printLog("onServiceConnected");
                }

                @Override
                public void onServiceDisconnected() {
                    Log.d(TAG, "onServiceDisconnected");
                    printLog("onServiceDisconnected");
                    mConnect = false;
                }
            });
        }
        mDataCenterManager.bindService();
    }

    private void getFeature() {
        boolean res = DataCenterManager.isMediaDataCenterAvailable(this);
        if (!res) {
            Toast.makeText(this, "不支持媒体数据服务", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!mConnect) {
            Toast.makeText(this, "请先连接服务", Toast.LENGTH_SHORT).show();
            return;
        }
        long start = System.currentTimeMillis();
        List<Integer> list = mDataCenterManager.getSupportedFeatures();
        Log.i(TAG, "kit time getSupportedFeatures :" + (System.currentTimeMillis() - start));
        initFeature = true;
        if (list != null && list.contains(DataCenterManager.FEATURE_SEARCH_MEDIA)) {
            mSupport = true;
            Toast.makeText(this, "当前设备支持搜索" + list, Toast.LENGTH_SHORT).show();
        } else {
            mSupport = false;
            Toast.makeText(this, "当前设备不支持搜索" + list, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean searchCheck() {
        if (!mConnect) {
            Toast.makeText(this, "请先连接服务", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!initFeature) {
            Toast.makeText(this, "请先检查是否支持搜索", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!mSupport) {
            Toast.makeText(this, "当前设备or版本不支持搜索", Toast.LENGTH_SHORT).show();
            return false;
        }
        String word = mSearchWord.getText().toString();
        if (TextUtils.isEmpty(word) && TextUtils.isEmpty(mRecommendationEditText.getText().toString())) {
            Toast.makeText(this, "请输入搜索词", Toast.LENGTH_SHORT).show();
            return false;
        }
        int type = mMediaType;
        if (type != DataCenterManager.MEDIA_TYPE_ALL && type != DataCenterManager.MEDIA_TYPE_IMAGE
                && type != DataCenterManager.MEDIA_TYPE_VIDEO) {
            Toast.makeText(this, "请选择类型", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private List<Recommendation> getRecommendationList() {
        String inputs = mRecommendationEditText.getText().toString();
        List<Recommendation> list = null;
        if (!TextUtils.isEmpty(inputs)) {
            String[] groups = inputs.split(STR_NEW_LINE);
            if (groups.length != 0) {
                list = new ArrayList<>();
                for (int i = 0; i < groups.length; i++) {
                    String str = groups[i];
                    if (str.startsWith("推荐词：")) {
                        str = str.substring(4);
                        String[] recommStrs = str.split(STR_SEPARATOR);
                        if (recommStrs.length == 3) {
                            Recommendation recommendation =
                                    new Recommendation(recommStrs[0], recommStrs[1], Long.valueOf(recommStrs[2]));
                            list.add(recommendation);
                        }
                    }
                }
            }
        }
        return list;
    }

    private void searchCount() {
        if (!searchCheck()) {
            return;
        }
        String word = mSearchWord.getText().toString();
        int type = mMediaType;
        List<Recommendation> list = getRecommendationList();
        long start = System.currentTimeMillis();
        SearchResult result = mDataCenterManager.getSearchLocalResultCount(type, word, list);
        Log.i(TAG, "kit time getSearchLocalResultCount :" + (System.currentTimeMillis() - start));
        printData("searchCount errorCode:" + result.getErrorCode() + ", type:" + type + ", count:" + result.getCount());
    }

    private void searchRecommendation() {
        if (!searchCheck()) {
            return;
        }
        String word = mSearchWord.getText().toString();
        int type = mMediaType;
        List<Recommendation> list = getRecommendationList();
        long start = System.currentTimeMillis();
        SearchResult result = mDataCenterManager.searchLocalRecommend(type, word, list, false);
        Log.i(TAG, "kit time searchLocalRecommend :" + (System.currentTimeMillis() - start));
        printData("searchGroup errorCode:" + result.getErrorCode() + ", type:" + type);
        List<Recommendation> resultList = result.getRecommendationList();
        printData("result count:" + (resultList == null ? 0 : resultList.size()));
        if (resultList != null) {
            for (Recommendation recom : resultList) {
                printData("推荐词：" + recom.getDomain() + STR_SEPARATOR + recom.getRecomValue() + STR_SEPARATOR
                        + recom.getCount());
            }
        }
    }

    private void searchRecomCover() {
        if (!searchCheck()) {
            return;
        }
        String word = mSearchWord.getText().toString();
        int type = mMediaType;
        List<Recommendation> list = getRecommendationList();
        long start = System.currentTimeMillis();
        SearchResult result = mDataCenterManager.searchLocalRecommend(type, word, list, true);
        Log.i(TAG, "kit time searchLocalRecommend Cover :" + (System.currentTimeMillis() - start));
        printData("searchGroupCover errorCode:" + result.getErrorCode() + ", type:" + type);
        List<Recommendation> resultList = result.getRecommendationList();
        printData("result count:" + (resultList == null ? 0 : resultList.size()));
        if (resultList != null) {
            for (Recommendation recom : resultList) {
                MediaData mediaData = recom.getMediaData();
                printData("推荐词：" + recom.getDomain() + STR_SEPARATOR + recom.getRecomValue() + STR_SEPARATOR
                        + recom.getCount() + STR_SEPARATOR + (mediaData == null ? "" : mediaData.getData()));
            }
        }
    }

    private void searchMedia() {
        if (!searchCheck()) {
            return;
        }
        String word = mSearchWord.getText().toString();
        int type = mMediaType;
        List<Recommendation> list = getRecommendationList();
        long start = System.currentTimeMillis();
        SearchResult result = mDataCenterManager.searchLocal(type, word, list, 0, 10000);
        Log.i(TAG, "kit time searchLocal :" + (System.currentTimeMillis() - start));
        printData("searchResult errorCode:" + result.getErrorCode() + ", type:" + type);
        List<MediaData> dataList = result.getMediaDataList();
        printData("result count:" + (dataList == null ? 0 : dataList.size()));
        if (dataList != null) {
            for (MediaData mediaData : dataList) {
                printData(mediaData.getId() + " " + mediaData.getData());
            }
        }
    }

    private void initSearch() {
        if (!mConnect || mDataCenterManager == null) {
            Toast.makeText(this, "请先连接服务", Toast.LENGTH_SHORT).show();
            return;
        }
        long start = System.currentTimeMillis();
        boolean res = mDataCenterManager.initSearch();
        Log.i(TAG, "kit time initSearch :" + (System.currentTimeMillis() - start));
        printData("initSearch res:" + res);
    }

    private void releaseSearch() {
        if (!mConnect || mDataCenterManager == null) {
            Toast.makeText(this, "请先连接服务", Toast.LENGTH_SHORT).show();
            return;
        }
        long start = System.currentTimeMillis();
        mDataCenterManager.releaseSearch();
        Log.i(TAG, "kit time releaseSearch :" + (System.currentTimeMillis() - start));
        printData("releaseSearch");
    }

    private void searchScope() {
        if (!mConnect || mDataCenterManager == null) {
            Toast.makeText(this, "请先连接服务", Toast.LENGTH_SHORT).show();
            return;
        }
        String word = mSearchWord.getText().toString();
        int type = mMediaType;
        List<Recommendation> list = getRecommendationList();
        SearchRequest.Builder builder = new SearchRequest.Builder()
                .setSearchWord(word)
                .setSearchType(SearchRequest.SEARCH_COUNT)
                .setMediaType(type)
                .setSearchScope(mScope)
                .setRecommendation(list)
                .setStart(0)
                .setLimit(10000);

        long start = System.currentTimeMillis();
        SearchResult result = mDataCenterManager.search(builder.build());
        Log.i(TAG, "kit time search count :" + (System.currentTimeMillis() - start));
        if (result != null) {
            printData("searchResult errorCode:" + result.getErrorCode() + ", count:" + result.getCount());
        }
        builder.setSearchType(SearchRequest.SEARCH_RECOMMEND);
        start = System.currentTimeMillis();
        result = mDataCenterManager.search(builder.build());
        Log.i(TAG, "kit time search Recommend :" + (System.currentTimeMillis() - start));
        if (result != null) {
            printData("searchGroup errorCode:" + result.getErrorCode() + ", type:" + type);
            List<Recommendation> resultList = result.getRecommendationList();
            printData("result count:" + (resultList == null ? 0 : resultList.size()));
            if (resultList != null) {
                for (Recommendation recom : resultList) {
                    printData("推荐词：" + recom.getDomain() + STR_SEPARATOR + recom.getRecomValue() + STR_SEPARATOR
                            + recom.getCount());
                }
            }
        }

        builder.setSearchType(SearchRequest.SEARCH_MEDIA);
        start = System.currentTimeMillis();
        result = mDataCenterManager.search(builder.build());
        Log.i(TAG, "kit time search :" + (System.currentTimeMillis() - start));
        if (result != null) {
            printData("searchResult errorCode:" + result.getErrorCode() + ", type:" + type);
            List<MediaData> dataList = result.getMediaDataList();
            printData("result count:" + (dataList == null ? 0 : dataList.size()));
            if (dataList != null) {
                for (MediaData mediaData : dataList) {
                    printData(mediaData.getId() + " " + mediaData.getData());
                }
            }
        }
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
                String editStr = mRecommendationEditText.getText().toString();
                if (TextUtils.isEmpty(editStr)) {
                    mRecommendationEditText.setText(str);
                } else {
                    mRecommendationEditText.setText(editStr + STR_NEW_LINE + str);
                    mRecommendationEditText.setSelection(mRecommendationEditText.getText().length());
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
}