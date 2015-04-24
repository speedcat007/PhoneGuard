package com.yzy.supercleanmaster.model;

import android.graphics.drawable.Drawable;

/**
 * Created by apple on 15/4/3.
 */
public class TrafficInfo {
    /**
     * 程序名称
     */
    private String appName;

    /**
     * 包名
     */
    private String packageName;

    /**
     * The icon.
     */

    private Drawable appIcon;

    /**
     * 接收移动数据流量
     */
    private long mobileRx;

    /**
     * 发送移动数据流量
     */
    private long mobileTx;

    /**
     * 接收WiFi数据流量
     */
    private long wifiRx;

    /**
     * 发送WiFi数据流量
     */
    private long wifiTx;

    public TrafficInfo() {
    }
    /**
     * Instantiates a new ab process info.
     *
     * @param appName
     * @param packageName
     * @param appIcon
     */
    public TrafficInfo(String appName, String packageName, Drawable appIcon) {
        super();
        this.appName = appName;
        this.packageName = packageName;
        this.appIcon = appIcon;
    }

    public void setAppName(String appName){
        this.appName = appName;
    }
    public String getAppName(){
        return this.appName;
    }
    public void setPackageName(String packageName){
        this.packageName = packageName;
    }
    public String getPackageName(){
        return this.packageName;
    }
    public void setAppIcon(Drawable appIcon){
        this.appIcon = appIcon;
    }
    public Drawable getAppIcon(){
        return this.appIcon;
    }
    public void setMobileRx(long mobileRx){
        this.mobileRx = mobileRx;
    }
    public long getMobileRx(){
        return this.mobileRx;
    }
    public void setMobileTx(long mobileTx){
        this.mobileTx = mobileTx;
    }
    public long getMobileTx(){
        return this.mobileTx;
    }
    public void setWifiRx(long wifiRx){
        this.wifiRx = wifiRx;
    }
    public long getWifiRx(){
        return this.wifiRx;
    }
    public void setWifiTx(long wifiTx){
        this.wifiTx = wifiTx;
    }
    public long getWifiTx(){
        return this.wifiTx;
    }
    public long getAllMobile(){
        return this.mobileTx + this.mobileRx;
    }
    public long getAllWifi(){
        return this.wifiTx + this.wifiRx;
    }
}
