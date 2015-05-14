package com.yzy.supercleanmaster.ui;

import android.content.ComponentName;
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
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.yzy.supercleanmaster.R;
import com.yzy.supercleanmaster.adapter.SoftwareAdapter;
import com.yzy.supercleanmaster.base.BaseSwipeBackActivity;
import com.yzy.supercleanmaster.model.AppInfo;
import com.yzy.supercleanmaster.utils.AntiVirusDao;
import com.yzy.supercleanmaster.utils.L;
import com.yzy.supercleanmaster.utils.Md5Encoder;
import com.yzy.supercleanmaster.utils.T;
import com.yzy.supercleanmaster.views.SlidingLayer;
import com.yzy.supercleanmaster.widget.circleprogress.ArcProgress;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class SafetyProtectionActivity extends BaseSwipeBackActivity {


    @InjectView(R.id.arc_scan)
    ArcProgress arcScan;

    @InjectView(R.id.progress)
    TextView mProgressBarText;

    @InjectView(R.id.scan_info)
    TextView scanInfo;


    @InjectView(R.id.topText)
    TextView topText;

    @InjectView(R.id.listview)
    ListView listview;

    @InjectView(R.id.slidingLayer)
    SlidingLayer slidingLayer;

    SoftwareAdapter VirusAppAdapter;
    List<AppInfo> appinfos = new ArrayList<AppInfo>();

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
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_protection);
        ButterKnife.inject(this);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        VirusAppAdapter = new SoftwareAdapter(mContext, appinfos);
        listview.setAdapter(VirusAppAdapter);
        dao = new AntiVirusDao(this);
        virusScan();
        slidingLayer.setSlidingEnabled(false);
    }

    @OnClick({R.id.arc_scan})
    void virusScan()
    {
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
                    L.e(e.getMessage());
                }
            }

            @Override
            protected void onPreExecute() {
                try {
                    arcScan.setProgress(0);
                    mProgressBarText.setText(R.string.scanning);
                } catch (Exception e) {
                    L.e(e.getMessage());
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (slidingLayer.isOpened())
            {
                setTitle("安全防护");
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
                setTitle("病毒查杀");
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

    @OnClick(R.id.card1)
    void OnlineKill() {
        startActivity(VirusKillActivity.class);
    }


    @OnClick(R.id.card2)
    void xprivacy() {
        ComponentName componetName = new ComponentName("biz.bokhorst.xprivacy", "biz.bokhorst.xprivacy.ActivityMain");
        try {
            Intent intent = new Intent();
            intent.setComponent(componetName);
            intent.setData(Uri.parse("com.android.example://AuthActivity"));
            startActivity(intent);
        } catch (Exception e) {

        }
    }

    @OnClick(R.id.card3)
    void adBlock() {
        ComponentName componetName = new ComponentName("tw.fatminmin.xposed.minminguard", "tw.fatminmin.xposed.minminguard.ui.SettingsActivity");
        try {
            Intent intent = new Intent();
            intent.setComponent(componetName);
            intent.setData(Uri.parse("com.android.example://AuthActivity"));
            startActivity(intent);
        } catch (Exception e) {

        }
    }

    @OnClick(R.id.card4)
    void AutoStartManage() { startActivity(AutoStartManageActivity.class); }

    @OnClick(R.id.card5)
    void simNumberChanger() {
        ComponentName componetName = new ComponentName("pt.oxinarf.simnumberchanger", "pt.oxinarf.simnumberchanger.MainActivity");
        try {
            Intent intent = new Intent();
            intent.setComponent(componetName);
            intent.setData(Uri.parse("com.android.example://AuthActivity"));
            startActivity(intent);
        } catch (Exception e) {

        }
    }

    @OnClick(R.id.card6)
    void secureWindows() {
        ComponentName componetName = new ComponentName("com.xp.apple.securewindows", "com.xp.apple.securewindows.PreferencesActivity");
        try {
            Intent intent = new Intent();
            intent.setComponent(componetName);
            intent.setData(Uri.parse("com.android.example://AuthActivity"));
            startActivity(intent);
        } catch (Exception e) {

        }
    }
}
