package com.yzy.supercleanmaster.ui;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import com.yzy.supercleanmaster.utils.FileUtils;
import com.yzy.supercleanmaster.utils.L;
import com.yzy.supercleanmaster.utils.Md5Encoder;
import com.yzy.supercleanmaster.utils.T;
import com.yzy.supercleanmaster.views.SlidingLayer;
import com.yzy.supercleanmaster.widget.circleprogress.ArcProgress;

import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
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
    TextView InfoScan;

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
        this.filePath = null;
        Intent intent = new Intent(this, FileChooserActivity.class);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.addFlags();
        startActivityForResult(intent, FILE_CHOOSER);
    }
    @OnClick({R.id.file_btn})
    void fileScan()
    {
        this.listview.setAdapter(this.textAdapter);
        setTitle("文件扫描");
        this.topText.setText("以下是此次文件扫描的结果");
        this.textList.clear();
        this.slidingLayer.openLayer(true);

    }
    @OnClick({R.id.arc_scan})
    void virusScan()
    {
        this.listview.setAdapter(this.VirusAppAdapter);
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

    private void updateFileScanResult(FileScanReport paramFileScanReport)
    {
        List localList1 = this.textList;
        StringBuilder localStringBuilder1 = new StringBuilder();
        localList1.add("扫描文件 :\t" + this.fileEdit.getText().toString().trim() + "\n" + "扫描时间 :\t" + paramFileScanReport.getScanDate() + "\n" + "危险/总共:\t" + paramFileScanReport.getPositives() + "/" + paramFileScanReport.getTotal());
        Map localMap = paramFileScanReport.getScans();
        Iterator localIterator = localMap.keySet().iterator();
        while (localIterator.hasNext())
        {
            String str = (String)localIterator.next();
            VirusScanInfo localVirusScanInfo = (VirusScanInfo)localMap.get(str);
            List localList2 = this.textList;
            StringBuilder localStringBuilder2 = new StringBuilder();
            localList2.add(str + ":\t" + localVirusScanInfo.getResult());
            this.textAdapter.notifyDataSetChanged();
        }
        T.showShort(this.mContext, "文件扫描完成");
    }

    private void updateUrlScanResult(FileScanReport[] paramArrayOfFileScanReport)
    {
        int i = paramArrayOfFileScanReport.length;
        int j = 0;
        if (j < i)
        {
            FileScanReport localFileScanReport = paramArrayOfFileScanReport[j];
            if (localFileScanReport.getResponseCode().intValue() == 0)
            {
                List localList3 = this.textList;
                StringBuilder localStringBuilder3 = new StringBuilder();
                localList3.add("Verbose Msg :\t" + localFileScanReport.getVerboseMessage());
            }
            while (true)
            {
                j++;
                break;
                List localList1 = this.textList;
                StringBuilder localStringBuilder1 = new StringBuilder();
                localList1.add("网址 :\t" + localFileScanReport.getResource() + "\n" + "扫描时间 :\t" + localFileScanReport.getScanDate() + "\n" + "危险/总共:\t" + localFileScanReport.getPositives() + "/" + localFileScanReport.getTotal());
                Map localMap = localFileScanReport.getScans();
                Iterator localIterator = localMap.keySet().iterator();
                while (localIterator.hasNext())
                {
                    String str = (String)localIterator.next();
                    VirusScanInfo localVirusScanInfo = (VirusScanInfo)localMap.get(str);
                    List localList2 = this.textList;
                    StringBuilder localStringBuilder2 = new StringBuilder();
                    localList2.add(str + ":\t" + localVirusScanInfo.getResult());
                    this.textAdapter.notifyDataSetChanged();
                }
            }
        }
        T.showShort(this.mContext, "URL扫描完成");
    }

    private void updateVirusScanResult(List<PackageInfo> paramList)
    {
        if (paramList.size() == 0)
            T.showShort(this.mContext, "扫面完成没有发现病毒");
        while (true)
        {
            this.arcScan.setBottomText("重新扫描");
            return;
            this.appinfos.clear();
            PackageManager localPackageManager1 = this.mContext.getPackageManager();
            Iterator localIterator = paramList.iterator();
            while (true)
                if (localIterator.hasNext())
                {
                    PackageInfo localPackageInfo = (PackageInfo)localIterator.next();
                    final AppInfo localAppInfo = new AppInfo();
                    localAppInfo.setAppIcon(localPackageInfo.applicationInfo.loadIcon(localPackageManager1));
                    int i = localPackageInfo.applicationInfo.flags;
                    localAppInfo.setUid(localPackageInfo.applicationInfo.uid);
                    label131: String str;
                    if ((i & 0x1) != 0)
                    {
                        localAppInfo.setUserApp(false);
                        if ((i & 0x40000) == 0)
                            break label295;
                        localAppInfo.setInRom(false);
                        localAppInfo.setAppName(localPackageInfo.applicationInfo.loadLabel(localPackageManager1).toString());
                        str = localPackageInfo.packageName;
                        localAppInfo.setPackname(str);
                        localAppInfo.setVersion(localPackageInfo.versionName);
                    }
                    try
                    {
                        Method localMethod = this.mContext.getPackageManager().getClass().getMethod("getPackageSizeInfo", new Class[] { String.class, IPackageStatsObserver.class });
                        PackageManager localPackageManager2 = this.mContext.getPackageManager();
                        Object[] arrayOfObject = new Object[2];
                        arrayOfObject[0] = str;
                        IPackageStatsObserver.Stub local5 = new IPackageStatsObserver.Stub()
                        {
                            public void onGetStatsCompleted(PackageStats paramAnonymousPackageStats, boolean paramAnonymousBoolean)
                                    throws RemoteException
                            {
                                synchronized (localAppInfo)
                                {
                                    localAppInfo.setPkgSize(paramAnonymousPackageStats.cacheSize + paramAnonymousPackageStats.codeSize + paramAnonymousPackageStats.dataSize);
                                    return;
                                }
                            }
                        };
                        arrayOfObject[1] = local5;
                        localMethod.invoke(localPackageManager2, arrayOfObject);
                        this.appinfos.add(localAppInfo);
                        continue;
                        localAppInfo.setUserApp(true);
                        break label131;
                        label295: localAppInfo.setInRom(true);
                    }
                    catch (Exception localException)
                    {
                        while (true)
                            localException.printStackTrace();
                    }
                }
            this.VirusAppAdapter.notifyDataSetChanged();
            this.mSlidingLayer.openLayer(true);
            T.showShort(this.mContext, "扫面完成发现病毒");
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    String path = FileUtils.getPath(this, uri);

                    String extStorage = FileUtils.getNormalSDCardPath();
                    String phoneCardPath = FileUtils.getPhoneCardPath();
//                    System.out.println(FileUtils.getSDCardPathEx());
                    path = "/storage/sdcard1/zcw/log.txt";
//                    path = extStorage+"/360/permmgr8009";
                    File file = new File(path);
                    if (file != null) {
                        System.out.println(file.getAbsolutePath());
                        System.out.println(FileMD5.getFileMD5(file));
                    }
                }
                break;
        }
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCAN_RESULT:
                    List<PackageInfo> packInfos = (List<PackageInfo>) msg.obj;
                    appinfos.clear();
                    if (packInfos.size() == 0) {
                        T.showShort(mContext, "扫面完成没有发现病毒");
                    } else {
                        PackageManager pm = mContext.getPackageManager();
                        for (PackageInfo packInfo : packInfos) {
                            final AppInfo appInfo = new AppInfo();
                            Drawable appIcon = packInfo.applicationInfo.loadIcon(pm);
                            appInfo.setAppIcon(appIcon);

                            int flags = packInfo.applicationInfo.flags;

                            int uid = packInfo.applicationInfo.uid;

                            appInfo.setUid(uid);

                            if ((flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                                appInfo.setUserApp(false);//系统应用
                            } else {
                                appInfo.setUserApp(true);//用户应用
                            }
                            if ((flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                                appInfo.setInRom(false);
                            } else {
                                appInfo.setInRom(true);
                            }
                            String appName = packInfo.applicationInfo.loadLabel(pm).toString();
                            appInfo.setAppName(appName);
                            String packname = packInfo.packageName;
                            appInfo.setPackname(packname);
                            String version = packInfo.versionName;
                            appInfo.setVersion(version);
                            try {
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
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            appinfos.add(appInfo);
                        }

                        VirusAppAdapter.notifyDataSetChanged();
                        mSlidingLayer.openLayer(true);
                        T.showShort(mContext, "扫面完成发现病毒");
                    }
                    arcScan.setBottomText("重新扫描");
                    break;
                case URL_SCAN_RESULT:
                    FileScanReport[] reports = (FileScanReport[]) msg.obj;
                    textList.clear();
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
                            textList.add(key + ":\t" + virusInfo.getResult());
                        }
                    }
                    textAdapter.notifyDataSetChanged();
                    mSlidingLayer.openLayer(true);
                    T.showShort(mContext, "URL扫描完成");
                    break;

                case FILE_SCAN_RESULT:
                    break;
                default:
                    PackageInfo packInfo = (PackageInfo) msg.obj;
                    InfoScan.setText(packInfo.applicationInfo.loadLabel(getPackageManager()));
                    break;
            }

        }

        ;
    };


    public void FileScan() {

    }

    public void URLScan() {
        new AsyncTask<Void, Integer, FileScanReport[]>() {
            @Override
            protected FileScanReport[] doInBackground(Void... params) {
                FileScanReport[] reports = null;
                String text = URLEdit.getText().toString().trim();
//                String text = "www.baidu,com";
                String urls[] = {text};
                try {
                    VirusTotalConfig.getConfigInstance().setVirusTotalAPIKey("9f4bb3d92809cd86871c38ccd4711f2401545a109b8c673685acf00e6a291974");
                    VirustotalPublicV2 virusTotalRef = new VirustotalPublicV2Impl();
                    reports = virusTotalRef.getUrlScanReport(urls, false);
                    L.d("=================结束=======================");
                } catch (APIKeyNotFoundException ex) {
                    L.e("API Key not found! " + ex.getMessage());
                } catch (UnsupportedEncodingException ex) {
                    L.e("Unsupported Encoding Format!" + ex.getMessage());
                } catch (UnauthorizedAccessException ex) {
                    L.e("Invalid API Key " + ex.getMessage());
                } catch (Exception ex) {
                    L.e("Something Bad Happened! " + ex.getMessage());
                }

                return reports;
            }

            @Override
            protected void onPostExecute(FileScanReport[] result) {
                super.onPostExecute(result);
                Message msg = Message.obtain();
                msg.what = URL_SCAN_RESULT;
                msg.obj = result;
                handler.sendMessage(msg);
            }
        }.execute();
    }

    public void kill() {

        task = new AsyncTask<Void, Integer, List<PackageInfo>>() {
            private int mAppCount = 0;

            @Override
            protected List<PackageInfo> doInBackground(Void... params) {
                List<PackageInfo> virusPackInfos = new ArrayList<PackageInfo>();
                PackageManager pm = mContext.getPackageManager();
                List<PackageInfo> packInfos = pm.getInstalledPackages(PackageManager.GET_SIGNATURES);
                publishProgress(0, packInfos.size());

                for (PackageInfo packInfo : packInfos) {
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
                msg.what = SCAN_RESULT;
                msg.obj = result;
                handler.sendMessage(msg);
            }

        };
        task.execute();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
