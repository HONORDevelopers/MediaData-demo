
package com.hihonor.mediadata.demo;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hihonor.mcs.media.datacenter.DataCenterManager;
import com.hihonor.mcs.media.datacenter.DataCenterStore;

import java.util.List;
import java.util.Optional;

public class TestClientViewModel extends ViewModel {
    private static final String[] ACTION_LIST = new String[] {"Clear Echo", "Bind", "Unbind",
        "Test ThumbNail", "Query Favourite", "Query Images", "Query Videos", "Query Portrait",
        "Query Portrait group", "Query Geo", "Query Category", "Search", "Live Photo"};

    private static final String TAG = TestClientViewModel.class.getSimpleName();

    private DataCenterManager mDataCenterManager = null;

    private final MutableLiveData<String> mEchoText = new MutableLiveData<>();

    private final DataCenterManager.ClientCallback mClientCallback = new DataCenterManager.ClientCallback() {
        @Override
        public void onServiceConnected() {
            Log.i("Client", "onServiceConnected");
            printLog("onServiceConnected");
        }

        @Override
        public void onServiceDisconnected() {
            Log.i("Client", "onServiceDisconnected");
            printLog("onServiceDisconnected");
        }
    };

    public String[] getActionList() {
        return ACTION_LIST;
    }

    public LiveData<String> getEchoText() {
        return mEchoText;
    }

    private void printLog(String log) {
        mEchoText.postValue(log);
    }

    public void bind(Context context) {
        if (mDataCenterManager != null) {
            mDataCenterManager.bindService();
            return;
        }
        mDataCenterManager = new DataCenterManager(context, mClientCallback);
        mDataCenterManager.bindService();
    }

    public void unbind() {
        if (mDataCenterManager == null) {
            return;
        }
        mDataCenterManager.unbindService();
    }

    public Optional<Cursor> queryFavourite(Context context, String nodeId) {
        Uri uri = DataCenterStore.Favourite.getLocalUri();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(uri, null, null, null);
        return Optional.ofNullable(cursor);
    }

    public Optional<Cursor> queryImages(Context context, String nodeId) {
        Uri uri = DataCenterStore.Images.getLocalUri();
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null, null);
        return Optional.ofNullable(cursor);
    }

    public Optional<Cursor> queryImages(Context context, String nodeId, long id) {
        Uri uri = DataCenterStore.Images.getLocalUri(id);
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        return Optional.ofNullable(cursor);
    }

    public Optional<Cursor> queryVideos(Context context, String nodeId) {
        Uri uri = DataCenterStore.Videos.getLocalUri();
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        return Optional.ofNullable(cursor);
    }

    public Optional<Cursor> queryVideos(Context context, String nodeId, long id) {
        Uri uri = DataCenterStore.Videos.getLocalUri(id);
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        return Optional.ofNullable(cursor);
    }

    public Optional<Cursor> queryPortrait(Context context, String nodeId) {
        Uri uri = DataCenterStore.PortraitAlbums.getLocalPortraitAlbumListUri();
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        return Optional.ofNullable(cursor);
    }

    public Optional<Cursor> queryPortrait(Context context, String nodeId, String groupTag) {
        Uri uri = DataCenterStore.PortraitAlbums.getLocalPortraitAlbumUri(groupTag);
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        return Optional.ofNullable(cursor);
    }

    public Optional<Cursor> queryGeo(Context context, String nodeId) {
        Uri uri = DataCenterStore.GeoAlbum.getLocalGeoAlbumListUri();
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        return Optional.ofNullable(cursor);
    }

    public Optional<Cursor> queryGeo(Context context, String nodeId, String locality) {
        Uri uri = DataCenterStore.GeoAlbum.getLocalGeoAlbumUri(locality);
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        return Optional.ofNullable(cursor);
    }

    public Optional<Cursor> queryCategory(Context context, String nodeId) {
        Uri uri = DataCenterStore.CategoryAlbums.getLocalCategoryListUri();
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        return Optional.ofNullable(cursor);
    }

    public Optional<Cursor> queryCategory(Context context, String nodeId, int categoryId) {
        Uri uri = DataCenterStore.CategoryAlbums.getLocalCategoryAlbumUri(categoryId);
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        return Optional.ofNullable(cursor);
    }

    public Optional<Cursor> queryPortraitGroup(Context context, String nodeId, List<String> list) {
        Uri uri = DataCenterStore.PortraitAlbums.getLocalPortraitGroupUri(list);
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        return Optional.ofNullable(cursor);
    }
}
