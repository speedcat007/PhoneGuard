package com.yzy.supercleanmaster.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.yzy.supercleanmaster.R;
import com.yzy.supercleanmaster.adapter.FileChooserAdapter;
import com.yzy.supercleanmaster.base.BaseSwipeBackActivity;
import com.yzy.supercleanmaster.model.FileInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class FileChooserActivity extends BaseSwipeBackActivity implements AdapterView.OnItemClickListener
{
    public static final String EXTRA_FILE_NAME = "file_name";
    public static final String EXTRA_FILE_PATH = "file_path";
    private static final int FILE_INFO = 0;
    private static final int SCAN_FINISH = 1;

    @InjectView(R.id.empty)
    TextView mEmptyView;

    @InjectView(R.id.listview)
    ListView mListView;

    @InjectView(R.id.scan_info)
    TextView scanInfo;

    private FileChooserAdapter mFileChooserAdapter;
    private List<FileInfo> mFileList;

    private String mSdcardRootPath;

    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case SCAN_FINISH:
                    scanInfo.setVisibility(View.GONE);
                    break;
                case FILE_INFO:
                    FileInfo fileInfo = (FileInfo)msg.obj;
                    scanInfo.setText(fileInfo.getFilePath());
                    mFileList.add(fileInfo);
                    mFileChooserAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_chooser);
        ButterKnife.inject(this);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        mFileList = new ArrayList<FileInfo>();
        mFileChooserAdapter = new FileChooserAdapter(mContext, mFileList);
        mListView.setAdapter(mFileChooserAdapter);
        mListView.setEmptyView(mEmptyView);
        mListView.setOnItemClickListener(this);
        new AsyncTask<Void, Integer, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                mSdcardRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                updateFileItems(mSdcardRootPath);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Message msg = Message.obtain();
                msg.what = SCAN_FINISH;
                handler.sendMessage(msg);
            }
        }.execute();
    }


    private File[] folderScan(String path)
    {
        File file = new File(path);
        return file.listFiles();
    }

    private void updateFileItems(String path) {
        File[] fileList = folderScan(path);
        for (File file : fileList) {
            if (file.isHidden()) {
                continue;
            }
            if (file.isDirectory()) {
                updateFileItems(file.getAbsolutePath());
            }
            String filePath = file.getAbsolutePath();
            String fileName = file.getName();
            if (fileName.lastIndexOf(".") < 0) {
                continue;
            }

            String type = fileName.substring(fileName.lastIndexOf("."));
            if ((!".apk".equalsIgnoreCase(type)) && (!".zip".equalsIgnoreCase(type))) {
                continue;
            }
            Drawable fileTypeIcon = null;
            if (".zip".equalsIgnoreCase(type))
            {
                fileTypeIcon = getResources().getDrawable(R.drawable.zip);
            }
            else
            {
                fileTypeIcon = getResources().getDrawable(R.drawable.apk);
            }
            FileInfo fileInfo = new FileInfo(fileTypeIcon, fileName, filePath);
            Message msg = Message.obtain();
            msg.what = FILE_INFO;
            msg.obj = fileInfo;
            handler.sendMessage(msg);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileInfo fileInfo = ((FileChooserAdapter)parent.getAdapter()).getItem(position);
        Intent intent = new Intent();
        intent.putExtra(EXTRA_FILE_NAME, fileInfo.getFileName());
        intent.putExtra(EXTRA_FILE_PATH, fileInfo.getFilePath());
        this.setResult(RESULT_OK, intent);
        finish();
    }
}