package com.yzy.supercleanmaster.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.umeng.update.UmengUpdateAgent;
import com.yzy.supercleanmaster.R;
import com.yzy.supercleanmaster.base.BaseFragment;
import com.yzy.supercleanmaster.ui.AutoStartManageActivity;
import com.yzy.supercleanmaster.ui.RubbishCleanActivity;

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
    void simNumberChanger() {

    }


    @OnClick(R.id.card2)
    void rubbishClean() {
        startActivity(RubbishCleanActivity.class);
    }

    @OnClick(R.id.card3)
    void AutoStartManage() {
        startActivity(AutoStartManageActivity.class);
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

}
