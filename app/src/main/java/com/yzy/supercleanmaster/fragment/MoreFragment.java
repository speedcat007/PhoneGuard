package com.yzy.supercleanmaster.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.umeng.update.UmengUpdateAgent;
import com.yzy.supercleanmaster.R;
import com.yzy.supercleanmaster.base.BaseFragment;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class MoreFragment extends BaseFragment {


    Context mContext;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_more, container, false);
        ButterKnife.inject(this, view);
        mContext = getActivity();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        UmengUpdateAgent.update(getActivity());
    }



    @OnClick(R.id.card1)
    void showWifiPassword() {
        Intent intent = new Intent();
        intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
        intent.putExtra("extra_prefs_show_button_bar", true);
        //intent.putExtra("extra_prefs_set_next_text", "完成");
        //intent.putExtra("extra_prefs_set_back_text", "返回");
        intent.putExtra("wifi_enable_next_on_connect", true);
        startActivity(intent);
    }


    @OnClick(R.id.card2)
    void swipeback() {
        ComponentName componetName = new ComponentName("info.papdt.swipeback", "info.papdt.swipeback.ui.base.GlobalActivity");
        try {
            Intent intent = new Intent();
            intent.setComponent(componetName);
            intent.setData(Uri.parse("com.android.example://AuthActivity"));
            startActivity(intent);
        } catch (Exception e) {

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

}
