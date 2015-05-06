package com.yzy.supercleanmaster.ui;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
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
import com.yzy.supercleanmaster.adapter.SoftwareAdapter;
import com.yzy.supercleanmaster.adapter.TextAdapter;
import com.yzy.supercleanmaster.base.BaseSwipeBackActivity;
import com.yzy.supercleanmaster.model.AppInfo;
import com.yzy.supercleanmaster.utils.AntiVirusDao;
import com.yzy.supercleanmaster.utils.Md5Encoder;
import com.yzy.supercleanmaster.utils.T;
import com.yzy.supercleanmaster.views.SlidingLayer;
import com.yzy.supercleanmaster.widget.circleprogress.ArcProgress;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class VirusKillActivity extends BaseSwipeBackActivity {


    @InjectView(R.id.arc_scan)
    ArcProgress arcScan;

    @InjectView(R.id.progress)
    TextView mProgressBarText;

    @InjectView(R.id.scan_info)
    TextView scanInfo;

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

    SoftwareAdapter VirusAppAdapter;
    List<AppInfo> appinfos = new ArrayList<AppInfo>();

    TextAdapter textAdapter;
    List<String> textList = new ArrayList<String>();

    private AntiVirusDao dao;

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
                    PackageInfo packageInfo = (PackageInfo)msg.obj;
                    scanInfo.setText(packageInfo.applicationInfo.loadLabel(getPackageManager()));
                    break;
                case VIRUS_SCAN_RESULT:
                    List<PackageInfo> packageInfos = (List<PackageInfo>)msg.obj;
                    updateVirusScanResult(packageInfos);
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
        VirusAppAdapter = new SoftwareAdapter(mContext, appinfos);
        textAdapter = new TextAdapter(mContext, textList);
        listview.setAdapter(VirusAppAdapter);
        dao = new AntiVirusDao(this);
        slidingLayer.setSlidingEnabled(false);
    }


    @OnClick(R.id.file_edit)
    void fileChooser()
    {
        Intent intent = new Intent(this, FileChooserActivity.class);
        startActivityForResult(intent, FILE_CHOOSER);
    }

    @OnClick({R.id.arc_scan})
    void virusScan()
    {
        listview.setAdapter(VirusAppAdapter);
        task = new AsyncTask<Void, Integer, List<PackageInfo>>()
        {
            private int mAppCount = 0;

            @Override
            protected List<PackageInfo> doInBackground(Void... params) {
                List<PackageInfo> virusPackInfos = new ArrayList<PackageInfo>();
                PackageManager pm = mContext.getPackageManager();
                List<PackageInfo> packInfos = pm.getInstalledPackages(PackageManager.GET_SIGNATURES);
                publishProgress(0, packInfos.size());

                for (PackageInfo packInfo : packInfos)
                {
                    publishProgress(++mAppCount, packInfos.size());
                    String md5 = Md5Encoder.encode(packInfo.signatures[0].toCharsString());
                    String result = dao.getVirusInfo(md5);
                    Message msg = Message.obtain();
                    msg.what = SCAN_INFO;
                    msg.obj = packInfo;
                    handler.sendMessage(msg);
                    if (result != null) {
                        virusPackInfos.add(packInfo);
                    }
                }
                return virusPackInfos;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                try {
                    final double progress = (values[0] / (double) values[1] * 100);
                    arcScan.setProgress((int) progress);
                    mProgressBarText.setText(getString(R.string.scanning_m_of_n, values[0], values[1]));
                } catch (Exception e) {

                }
            }

            @Override
            protected void onPreExecute() {
                try {
                    arcScan.setProgress(0);
                    mProgressBarText.setText(R.string.scanning);
                } catch (Exception e) {

                }
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(List<PackageInfo> result) {
                super.onPostExecute(result);
                Message msg = Message.obtain();
                msg.what = VIRUS_SCAN_RESULT;
                msg.obj = result;
                handler.sendMessage(msg);
            }

        };
        task.execute();
    }

    @OnClick({R.id.file_btn})
    void fileScan()
    {
        listview.setAdapter(textAdapter);
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
        listview.setAdapter(textAdapter);
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

    private void updateVirusScanResult(List<PackageInfo> packInfos)
    {

        if(packInfos.size() == 0)
        {
            T.showShort(mContext, "扫面完成没有发现病毒");
        }
        else
        {
            appinfos.clear();
            slidingLayer.openLayer(true);
            PackageManager pm = mContext.getPackageManager();
            for (PackageInfo packInfo : packInfos)
            {
                final AppInfo appInfo = new AppInfo();
                Drawable appIcon = packInfo.applicationInfo.loadIcon(pm);
                appInfo.setAppIcon(appIcon);

                int flags = packInfo.applicationInfo.flags;

                int uid = packInfo.applicationInfo.uid;

                appInfo.setUid(uid);

                if ((flags & ApplicationInfo.FLAG_SYSTEM) != 0)
                {
                    appInfo.setUserApp(false);//系统应用
                }
                else
                {
                    appInfo.setUserApp(true);//用户应用
                }
                if ((flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0)
                {
                    appInfo.setInRom(false);
                }
                else
                {
                    appInfo.setInRom(true);
                }
                String appName = packInfo.applicationInfo.loadLabel(pm).toString();
                appInfo.setAppName(appName);
                String packname = packInfo.packageName;
                appInfo.setPackname(packname);
                String version = packInfo.versionName;
                appInfo.setVersion(version);
                try
                {
                    Method mGetPackageSizeInfoMethod = mContext.getPackageManager().getClass().getMethod(
                            "getPackageSizeInfo", String.class, IPackageStatsObserver.class);

                    mGetPackageSizeInfoMethod.invoke(mContext.getPackageManager(), new Object[]{
                            packname,
                            new IPackageStatsObserver.Stub() {
                                @Override
                                public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                                    synchronized (appInfo) {
                                        appInfo.setPkgSize(pStats.cacheSize + pStats.codeSize + pStats.dataSize);

                                    }
                                }
                            }
                    });
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                appinfos.add(appInfo);
                VirusAppAdapter.notifyDataSetChanged();
            }
            T.showShort(mContext, "扫面完成发现病毒");
        }
        arcScan.setBottomText("重新扫描");
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //    private void showFileChooser() {
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("*/*");
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//
//        try {
//            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), 1);
//        } catch (ActivityNotFoundException ex) {
//            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
//        }
//    }

}
