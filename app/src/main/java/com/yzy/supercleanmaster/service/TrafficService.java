package com.yzy.supercleanmaster.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import com.yzy.supercleanmaster.model.TrafficInfo;
import com.yzy.supercleanmaster.utils.TrafficUtils;

import java.util.List;

/**
 * Created by apple on 15/4/5.
 */
public class TrafficService extends Service {

    private static final String TAG = "TrafficService";
    private OnFlowListener mOnFlowListener;
    private boolean mIsScanning = false;
    private double mouth_used = 100;
    private double mouth_leave = 100;
    private double today_used = 100;

    public static interface OnFlowListener {
        public void onScanStarted(Context context);

        public void onScanProgressUpdated(Context context, int current, int max);

        public void onScanCompleted(Context context, List<TrafficInfo> flowInfos);


    }

    public class TrafficServiceBinder extends Binder {
        public TrafficService getService() {
            return TrafficService.this;
        }
    }

    private TrafficServiceBinder mBinder = new TrafficServiceBinder();

    private class TaskScan extends AsyncTask<Void, Integer, List<TrafficInfo>> {

        @Override
        protected void onPreExecute() {
            if (mOnFlowListener != null) {
                mOnFlowListener.onScanStarted(TrafficService.this);
            }
        }

        @Override
        protected List<TrafficInfo> doInBackground(Void... params) {
            // TrafficUtil.getTrafficInfo(FlowService.this);
            return TrafficUtils.getTrafficInfo(TrafficService.this);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (mOnFlowListener != null) {
                mOnFlowListener.onScanProgressUpdated(TrafficService.this, values[0], values[1]);
            }
        }

        @Override
        protected void onPostExecute(List<TrafficInfo> result) {
            if (mOnFlowListener != null) {
                mOnFlowListener.onScanCompleted(TrafficService.this, result);
            }
            mIsScanning = false;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {

    }

    public void scanRunProcess() {
        mIsScanning = true;
        new TaskScan().execute();
    }



    public void setOnFlowListener(OnFlowListener listener) {
        mOnFlowListener = listener;
    }

    public boolean isScanning() {
        return mIsScanning;
    }


}
