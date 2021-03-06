package com.novoda.downloadmanager.demo.extended.extra_data;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.demo.R;
import com.novoda.downloadmanager.demo.extended.QueryTimestamp;
import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.notifications.NotificationVisibility;
import com.novoda.downloadmanager.lib.Query;
import com.novoda.downloadmanager.lib.Request;
import com.novoda.downloadmanager.lib.logger.LLog;

import java.util.List;

public class ExtraDataActivity extends AppCompatActivity implements QueryForExtraDataDownloadsAsyncTask.Callback {
    private static final String BIG_FILE = "http://download.thinkbroadband.com/20MB.zip";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final QueryTimestamp lastQueryTimestamp = new QueryTimestamp();

    private DownloadManager downloadManager;
    private ExtraDataAdapter downloadAdapter;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra_data);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.main_downloads_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        downloadManager = DownloadManagerBuilder.from(this)
                .build();
        downloadAdapter = new ExtraDataAdapter();
        recyclerView.setAdapter(downloadAdapter);

        emptyView = findViewById(R.id.main_no_downloads_view);

        findViewById(R.id.download_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(@NonNull View v) {
                        enqueueSingleDownload();
                    }
                }
        );

        setupQueryingExample();
    }

    private void setupQueryingExample() {
        queryForDownloads();
    }

    private void queryForDownloads() {
        QueryForExtraDataDownloadsAsyncTask.newInstance(downloadManager, this).execute(new Query());
    }

    private void enqueueSingleDownload() {
        Uri uri = Uri.parse(BIG_FILE);
        final Request request = new Request(uri)
                .setTitle("A Single Beard")
                .setDescription("Fine facial hair")
                .setExtraData("Hey you forgot your beard comb.")
                .setNotificationVisibility(NotificationVisibility.ACTIVE_OR_COMPLETE);

        long requestId = downloadManager.enqueue(request);
        LLog.d("Download enqueued with request ID: " + requestId);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getContentResolver().registerContentObserver(downloadManager.getContentUri(), true, updateSelf);
    }

    private final ContentObserver updateSelf = new ContentObserver(handler) {

        @Override
        public void onChange(boolean selfChange) {
            if (lastQueryTimestamp.updatedRecently()) {
                return;
            }
            queryForDownloads();
            lastQueryTimestamp.setJustUpdated();
        }

    };

    @Override
    protected void onStop() {
        super.onStop();
        getContentResolver().unregisterContentObserver(updateSelf);
    }

    @Override
    public void onQueryResult(List<ExtraDataDownload> extraDataDownloads) {
        downloadAdapter.updateDownloads(extraDataDownloads);
        emptyView.setVisibility(extraDataDownloads.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
