package com.yzy.supercleanmaster.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;

import com.yzy.supercleanmaster.database.DbConstants;
import com.yzy.supercleanmaster.database.DbManager;
import com.yzy.supercleanmaster.model.TrafficInfo;

import java.util.List;

/**
 * Created by apple on 15/4/6.
 */
public class TrafficUtils {

    public static final int KB = 1024;
    public static final int MB = 1024 * 1024;
    public static final int GB = 1024 * 1024 * 1024;
    public static List<TrafficInfo> getTrafficInfo(Context context) {
        DbManager dbManager = new DbManager(context);
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo appInfo = null;
        recordTraffic(context);
        List<TrafficInfo> list = dbManager.queryTotal();
        for (int i = 0; i < list.size(); i++) {
            try {
                appInfo = packageManager.getApplicationInfo(list.get(i).getPackageName(), 0);
                Drawable icon = appInfo.loadIcon(packageManager);
                list.get(i).setAppIcon(icon);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static String getTodayTotal(Context context) {
        DbManager dbManager = new DbManager(context);
        return refreshTraffic(dbManager.queryTodayTotal());
    }
    public static String getMonthTotal(Context context) {
        DbManager dbManager = new DbManager(context);
        return refreshTraffic(dbManager.queryMonthTotal());
    }

    public static String getLeftTraffic(Context context){
        DbManager dbManager = new DbManager(context);
        Long flow = (Long)SPUtils.get(context,"trafficNum", new Long(0));
        if(flow.intValue() == 0)
        {
            return "设置";
        }
        return refreshTraffic( flow.longValue() - dbManager.queryMonthTotal());
    }
    public static String refreshTraffic(long lg) {
        String str = "0K";
        if (lg < KB) {
            str = "0KB";
        } else if (lg >= KB && lg < MB) {
            int d = (int) (lg / MB);
            str = d + "KB";
        } else {
            double e = (double) lg / MB;
            java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
            str = df.format(e) + "MB";
        }
        return str;
    }

    public static void recordTraffic(Context context) {
        DbManager dbManager = new DbManager(context);
        String networkType = "";
        if(NetUtils.isConnected(context)){
            return;
        }
        else if (NetUtils.isWifi(context)) {
            networkType = DbConstants.NETWORK_TYPE_WIFI;
        }
        else if (NetUtils.isMobile(context)) {
            networkType = DbConstants.NETWORK_TYPE_MOBILE;
        }
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packinfos = packageManager.getInstalledPackages(PackageManager.GET_SIGNATURES);
        for (PackageInfo info : packinfos) {
            String[] premissions = info.requestedPermissions;
            if (premissions != null && premissions.length > 0) {
                for (String premission : premissions) {
                    if ("android.permission.INTERNET".equals(premission)) {
                        int uid = info.applicationInfo.uid;
                        long rx = TrafficStats.getUidRxBytes(uid);
                        long tx = TrafficStats.getUidTxBytes(uid);
                        dbManager.updateEnd(info.packageName, System.currentTimeMillis(), rx, tx);
                        dbManager.insertStart(
                                info.packageName,
                                info.applicationInfo.loadLabel(context.getPackageManager()).toString(),
                                System.currentTimeMillis(),
                                networkType, rx, tx);
                    }
                }
            }
        }
    }

    /**
     * @return Mobile接收的字节总数
     */
    public static long getMobileRxBytes() {
        return TrafficStats.getMobileRxBytes();
    }

    /**
     * @return Mobile发送的总字节数
     */
    public static long getMobileTxBytes() {
        return TrafficStats.getMobileTxBytes();
    }

    /**
     * @return Mobile和WiFi接收的总字节数
     */
    public static long getTotalRxBytes() {
        return TrafficStats.getTotalRxBytes();
    }

    /**
     * @return  Mobile和WiFi发送的总字节数
     */
    public static long getTotalTxBytes() {
        return TrafficStats.getTotalTxBytes();
    }


}
