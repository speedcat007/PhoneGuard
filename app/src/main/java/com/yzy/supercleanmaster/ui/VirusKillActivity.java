package com.yzy.supercleanmaster.ui;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.kanishka.virustotal.dto.FileScanReport;
import com.kanishka.virustotal.dto.VirusScanInfo;
import com.kanishka.virustotal.exception.APIKeyNotFoundException;
import com.kanishka.virustotal.exception.UnauthorizedAccessException;
import com.kanishka.virustotalv2.VirusTotalConfig;
import com.kanishka.virustotalv2.VirustotalPublicV2;
import com.kanishka.virustotalv2.VirustotalPublicV2Impl;
import com.xp.utils.FileMD5;
import com.yzy.supercleanmaster.R;
import com.yzy.supercleanmaster.adapter.TextAdapter;
import com.yzy.supercleanmaster.base.BaseSwipeBackActivity;
import com.yzy.supercleanmaster.utils.T;
import com.yzy.supercleanmaster.views.SlidingLayer;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class VirusKillActivity extends BaseSwipeBackActivity {

    @InjectView(R.id.file_edit)
    EditText fileEdit;

    @InjectView(R.id.url_edit)
    EditText urlEdit;

    @InjectView(R.id.topText)
    TextView topText;

    @InjectView(R.id.listview)
    ListView listview;

    @InjectView(R.id.slidingLayer)
    SlidingLayer slidingLayer;



    TextAdapter textAdapter;
    List<String> textList = new ArrayList<String>();


    AsyncTask<Void, Integer, List<PackageInfo>> task;

    private static final int FILE_CHOOSER = 4;
    private static final int FILE_SCAN_RESULT = 3;
    private static final int URL_SCAN_RESULT = 2;
    private static final int VIRUS_SCAN_RESULT = 1;
    private static final int SCAN_INFO = 0;

    private String filePath;
    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                default:
                    break;
                case URL_SCAN_RESULT:
                    FileScanReport[] reports = (FileScanReport[])msg.obj;
                    updateUrlScanResult(reports);
                    return;
                case FILE_SCAN_RESULT:
                    FileScanReport report = (FileScanReport)msg.obj;
                    updateFileScanResult(report);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_virus_kill);
        ButterKnife.inject(this);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        textAdapter = new TextAdapter(mContext, textList);
        listview.setAdapter(textAdapter);
        slidingLayer.setSlidingEnabled(false);
    }


    @OnClick(R.id.file_edit)
    void fileChooser()
    {
        Intent intent = new Intent(this, FileChooserActivity.class);
        startActivityForResult(intent, FILE_CHOOSER);
    }

    @OnClick({R.id.file_btn})
    void fileScan()
    {
        setTitle("文件扫描");
        topText.setText("以下是此次文件扫描的结果");
        textList.clear();
        slidingLayer.openLayer(true);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try
                {
                    VirusTotalConfig.getConfigInstance().setVirusTotalAPIKey("9f4bb3d92809cd86871c38ccd4711f2401545a109b8c673685acf00e6a291974");
                    VirustotalPublicV2 virusTotalRef = new VirustotalPublicV2Impl();
                    File file = new File(filePath);
                    String MD5 = FileMD5.getFileMD5(file);
                    FileScanReport report = virusTotalRef.getScanReport(MD5);
                    Message msg = Message.obtain();
                    msg.what = FILE_SCAN_RESULT;
                    msg.obj = report;
                    handler.sendMessage(msg);
                    return null;
                }
                catch (APIKeyNotFoundException ex) {
                    System.err.println("API Key not found! " + ex.getMessage());
                } catch (UnsupportedEncodingException ex) {
                    System.err.println("Unsupported Encoding Format!" + ex.getMessage());
                } catch (UnauthorizedAccessException ex) {
                    System.err.println("Invalid API Key " + ex.getMessage());
                } catch (Exception ex) {
                    System.err.println("Something Bad Happened! " + ex.getMessage());
                }
                return null;
            }

        }.execute();
    }

    @OnClick(R.id.url_btn)
    void urlScan()
    {
        setTitle("URL扫描");
        topText.setText("以下是此次URL扫描的结果");
        textList.clear();
        slidingLayer.openLayer(true);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    VirusTotalConfig.getConfigInstance().setVirusTotalAPIKey("9f4bb3d92809cd86871c38ccd4711f2401545a109b8c673685acf00e6a291974");
                    VirustotalPublicV2 virusTotalRef = new VirustotalPublicV2Impl();

                    String[] urls = { urlEdit.getText().toString().trim() };
                    FileScanReport[] reports = virusTotalRef.getUrlScanReport(urls, false);
                    Message msg = Message.obtain();
                    msg.what = URL_SCAN_RESULT;
                    msg.obj = reports;
                    handler.sendMessage(msg);
                    return null;
                } catch (APIKeyNotFoundException ex) {
                    System.err.println("API Key not found! " + ex.getMessage());
                } catch (UnsupportedEncodingException ex) {
                    System.err.println("Unsupported Encoding Format!" + ex.getMessage());
                } catch (UnauthorizedAccessException ex) {
                    System.err.println("Invalid API Key " + ex.getMessage());
                } catch (Exception ex) {
                    System.err.println("Something Bad Happened! " + ex.getMessage());
                }
                return null;
            }
        }.execute();
    }

    private void updateFileScanResult(FileScanReport report)
    {
        textList.clear();
        slidingLayer.openLayer(true);
        textList.add("扫描文件 :\t" + fileEdit.getText().toString().trim() + "\n"
                + "扫描时间 :\t" + report.getScanDate() + "\n"
                + "危险/总共:\t" + report.getPositives() + "/" + report.getTotal());
        Map<String, VirusScanInfo> scans = report.getScans();
        for (String key : scans.keySet()) {
            VirusScanInfo virusInfo = scans.get(key);
            textList.add(key+":\t" + virusInfo.getResult());
            textAdapter.notifyDataSetChanged();
        }

        T.showShort(this.mContext, "文件扫描完成");
    }

    private void updateUrlScanResult(FileScanReport[] reports)
    {
        textList.clear();
        slidingLayer.openLayer(true);
        for (FileScanReport report : reports) {
            if (report.getResponseCode() == 0) {
                textList.add("Verbose Msg :\t" + report.getVerboseMessage());
                continue;
            }
            textList.add("网址 :\t" + report.getResource() + "\n" +
                            "扫描时间 :\t" + report.getScanDate() + "\n" +
                            "危险/总共:\t" + report.getPositives() + "/" + report.getTotal()
            );

            Map<String, VirusScanInfo> scans = report.getScans();
            for (String key : scans.keySet()) {
                VirusScanInfo virusInfo = scans.get(key);
                textList.add(key+":\t" + virusInfo.getResult());
                textAdapter.notifyDataSetChanged();
            }
        }

        T.showShort(mContext, "URL扫描完成");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case FILE_CHOOSER:
                    String fileName = data.getStringExtra("file_name");
                    filePath = data.getStringExtra("file_path");
                    fileEdit.setText(fileName);
                    break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (slidingLayer.isOpened())
            {
                setTitle("在线查杀");
                slidingLayer.closeLayer(true);
                return true;
            }
            else
            {
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (slidingLayer.isOpened())
            {
                setTitle("在线查杀");
                this.slidingLayer.closeLayer(true);
                return true;
            }
            else
            {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

}
