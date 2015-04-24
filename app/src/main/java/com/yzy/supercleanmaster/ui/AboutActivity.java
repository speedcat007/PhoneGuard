package com.yzy.supercleanmaster.ui;

import android.os.Bundle;
import android.text.util.Linkify;
import android.view.MenuItem;
import android.widget.TextView;

import com.yzy.supercleanmaster.R;
import com.yzy.supercleanmaster.base.BaseSwipeBackActivity;
import com.yzy.supercleanmaster.utils.AppUtils;

import butterknife.InjectView;

public class AboutActivity extends BaseSwipeBackActivity {

    @InjectView(R.id.subVersion)
    TextView subVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setTitle("关于");
        TextView tv = (TextView) findViewById(R.id.app_information);
        Linkify.addLinks(tv, Linkify.ALL);

        subVersion.setText("V"+ AppUtils.getVersionName(mContext));

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
