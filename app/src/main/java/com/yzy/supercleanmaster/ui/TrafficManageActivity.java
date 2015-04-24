package com.yzy.supercleanmaster.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.etiennelawlor.quickreturn.library.enums.QuickReturnType;
import com.etiennelawlor.quickreturn.library.listeners.QuickReturnListViewOnScrollListener;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.yzy.supercleanmaster.R;
import com.yzy.supercleanmaster.adapter.TrafficAdapter;
import com.yzy.supercleanmaster.base.BaseSwipeBackActivity;
import com.yzy.supercleanmaster.model.TrafficInfo;
import com.yzy.supercleanmaster.service.TrafficService;
import com.yzy.supercleanmaster.utils.SPUtils;
import com.yzy.supercleanmaster.utils.TrafficUtils;
import com.yzy.supercleanmaster.views.SlidingLayer;
import com.yzy.supercleanmaster.views.WaterWaveView;

import java.util.ArrayList;
import java.util.List;

import butterknife.InjectView;


public class TrafficManageActivity extends BaseSwipeBackActivity implements OnDismissCallback, TrafficService.OnFlowListener {

    @InjectView(R.id.header)
    RelativeLayout header;

    @InjectView(R.id.wave_view)
    WaterWaveView mwaveView;

    @InjectView(R.id.month_used)
    TextView month_used;

    @InjectView(R.id.today_used)
    TextView today_used;

    @InjectView(R.id.listview)
    ListView mListView;

    List<TrafficInfo> mTrafficInfos = new ArrayList<>();
    TrafficAdapter mTrafficAdapter;

    @InjectView(R.id.bottom_lin)
    LinearLayout bottom_lin;

    @InjectView(R.id.edit_traffic)
    EditText trafficEdit;

    @InjectView(R.id.btn_switch)
    Button switchButton;

    @InjectView(R.id.btn_confirm)
    Button confirmButton;

    @InjectView(R.id.btn_cancel)
    Button cancelButton;

    @InjectView(R.id.btn_set)
    Button setButton;

    @InjectView(R.id.progressBar)
    View mProgressBar;

    @InjectView(R.id.slidingLayer)
    SlidingLayer mSlidingLayer;



    private TrafficService mTrafficService;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mTrafficService = ((TrafficService.TrafficServiceBinder) service).getService();
            mTrafficService.setOnFlowListener(TrafficManageActivity.this);
            mTrafficService.scanRunProcess();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mTrafficService.setOnFlowListener(null);
            mTrafficService = null;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic_manage);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        bindService(new Intent(mContext, TrafficService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        mTrafficAdapter = new TrafficAdapter(mContext, mTrafficInfos);
        mListView.setAdapter(mTrafficAdapter);
        mSlidingLayer.setSlidingEnabled(false);
        int footerHeight = mContext.getResources().getDimensionPixelSize(R.dimen.footer_height);
        mListView.setOnScrollListener(new QuickReturnListViewOnScrollListener(QuickReturnType.FOOTER, null, 0, bottom_lin, footerHeight));


        mwaveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSlidingLayer.openLayer(true);
            }
        });
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSlidingLayer.openLayer(true);
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSlidingLayer.closeLayer(true);
            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long flow = Long.parseLong(trafficEdit.getText().toString());
                flow *= TrafficUtils.MB;
                if("GB".equals(switchButton.getText())){
                    flow *= 1024;
                }
                SPUtils.put(mContext,"trafficNum", flow);
                mwaveView.setFlowNum(TrafficUtils.getLeftTraffic(mContext));
                mSlidingLayer.closeLayer(true);
            }
        });
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
    public void onDismiss(@NonNull ViewGroup viewGroup, @NonNull int[] ints) {

    }

    @Override
    public void onScanStarted(Context context) {
        showProgressBar(true);
    }

    @Override
    public void onScanProgressUpdated(Context context, int current, int max) {

    }

    @Override
    public void onScanCompleted(Context context, List<TrafficInfo> trafficInfos) {
        mTrafficInfos.clear();
        mTrafficInfos.addAll(trafficInfos);
        mTrafficAdapter.notifyDataSetChanged();
        showProgressBar(false);
        if (mTrafficInfos.size() > 0)
        {
            header.setVisibility(View.VISIBLE);
            bottom_lin.setVisibility(View.VISIBLE);
        }
        else
        {
            header.setVisibility(View.GONE);
            bottom_lin.setVisibility(View.GONE);
        }
        mwaveView.setFlowNum(TrafficUtils.getLeftTraffic(context));
        today_used.setText("今日已用：" + TrafficUtils.getTodayTotal(context));
        month_used.setText("本月已用：" + TrafficUtils.getMonthTotal(context));
        mwaveView.startWave();
    }



    private void showProgressBar(boolean show) {
        if (show) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.startAnimation(AnimationUtils.loadAnimation(
                    mContext, android.R.anim.fade_out));
            mProgressBar.setVisibility(View.GONE);
        }
    }
    @Override
    public void onDestroy() {
        unbindService(mServiceConnection);
        super.onDestroy();
    }
}
