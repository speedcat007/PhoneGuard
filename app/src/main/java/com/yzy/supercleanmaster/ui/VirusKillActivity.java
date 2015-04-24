package com.yzy.supercleanmaster.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.yzy.supercleanmaster.utils.ViewUtil;
import com.yzy.supercleanmaster.views.SlidingLayer;
import com.yzy.supercleanmaster.widget.circleprogress.ArcProgress;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.InjectView;

public class VirusKillActivity extends BaseSwipeBackActivity {



    @InjectView(R.id.arc_scan)
    ArcProgress arcScan;

    @InjectView(R.id.progress)
    TextView mProgressBarText;

    @InjectView(R.id.scan_info)
    TextView InfoScan;

    //file
    @InjectView(R.id.scan_file)
    TextView FileScan;

    @InjectView(R.id.l_file_scan)
    LinearLayout LinearFile;

    @InjectView(R.id.edit_file)
    EditText fileEdit;

    @InjectView(R.id.btn_file)
    Button fileBtn;

    //url
    @InjectView(R.id.scan_url)
    TextView URLScan;

    @InjectView(R.id.l_url_scan)
    LinearLayout  LinearURL;

    @InjectView(R.id.edit_url)
    EditText URLEdit;

    @InjectView(R.id.btn_url)
    Button URLBtn;

    @InjectView(R.id.slidingLayer)
    SlidingLayer mSlidingLayer;


    @InjectView(R.id.listview)
    ListView listview;

    SoftwareAdapter VirusAppAdapter;
    List<AppInfo> appinfos = new ArrayList<AppInfo>();

    TextAdapter textAdapter;
    List<String> textList = new ArrayList<String>();

    private AntiVirusDao dao;

    AsyncTask<Void, Integer, List<PackageInfo>> task;

    private static final int SCAN_INFO = 0;
    private static final int SCAN_RESULT = 1;
    private static final int URL_SCAN_RESULT = 2;
    private static final int FILE_SCAN_RESULT = 3;
    private static final int FILE_SELECT_CODE = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_virus_kill);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        VirusAppAdapter = new SoftwareAdapter(mContext, appinfos);
        textAdapter = new TextAdapter(mContext, textList);
        listview.setAdapter(VirusAppAdapter);

        dao = new AntiVirusDao(this);
        mSlidingLayer.setSlidingEnabled(false);
        initAnimator();
//        kill();
        arcScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listview.setAdapter(VirusAppAdapter);
                kill();
            }
        });
        FileScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator anim = ObjectAnimator.ofFloat(FileScan,"x",0F, ViewUtil.getDisplayMetrics(mContext).widthPixels).setDuration(1000);
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        FileScan.setVisibility(View.GONE);
                    }
                });
                anim.start();
                ObjectAnimator anim1 = ObjectAnimator.ofFloat(LinearFile,"x", -ViewUtil.getDisplayMetrics(mContext).widthPixels,0F).setDuration(1000);
                anim1.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        LinearFile.setVisibility(View.VISIBLE);
                    }
                });
                anim1.start();
            }
        });

        URLScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator anim = ObjectAnimator.ofFloat(URLScan,"x",0F, ViewUtil.getDisplayMetrics(mContext).widthPixels).setDuration(1000);
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        URLScan.setVisibility(View.GONE);
                    }
                });
                anim.start();
                ObjectAnimator anim1 = ObjectAnimator.ofFloat(LinearURL,"x", -ViewUtil.getDisplayMetrics(mContext).widthPixels,0F).setDuration(1000);
                anim1.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        LinearURL.setVisibility(View.VISIBLE);
                    }
                });
                anim1.start();
            }
        });
        URLBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listview.setAdapter(textAdapter);
                URLScan();
            }
        });

        FileScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });
        fileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listview.setAdapter(textAdapter);
                FileScan();
            }
        });
    }



    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), 1);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)  {
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
                    if(file != null) {
                       System.out.println(file.getAbsolutePath());
                       System.out.println(FileMD5.getFileMD5(file));
                    }
                }
                break;
        }
    }

    public void initAnimator(){
        ObjectAnimator anim = ObjectAnimator.ofFloat(LinearFile,"x",0F, -ViewUtil.getDisplayMetrics(mContext).widthPixels).setDuration(0);
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(LinearURL,"x",0F, -ViewUtil.getDisplayMetrics(mContext).widthPixels).setDuration(0);
        anim.start();
        anim1.start();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private Handler handler = new Handler() {

       public void handleMessage(android.os.Message msg) {
           switch (msg.what) {
               case SCAN_RESULT:
                   List<PackageInfo> packInfos = (List<PackageInfo>) msg.obj;
                   appinfos.clear();
                   if(packInfos.size() == 0)
                   {
                       T.showShort(mContext, "扫面完成没有发现病毒");
                   }
                   else
                   {
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
                           textList.add(key+":\t" + virusInfo.getResult());
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

       };
   };




    public void FileScan(){

    }
    public void URLScan(){
        new AsyncTask<Void, Integer, FileScanReport[]>(){
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

                for (PackageInfo packInfo : packInfos)
                {
                    publishProgress(++mAppCount, packInfos.size());
                    String md5 = Md5Encoder.encode(packInfo.signatures[0].toCharsString());

                    String result = dao.getVirusInfo(md5);

                    Message msg = Message.obtain();
                    msg.what = SCAN_INFO;
                    msg.obj = packInfo;
                    handler.sendMessage(msg);

                    if (result != null)
                    {
                        virusPackInfos.add(packInfo);
                    }



                }
                return virusPackInfos;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                try {
                    final double progress = (values[0]/(double)values[1] * 100);
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
