package com.yzy.supercleanmaster.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yzy.supercleanmaster.model.TrafficInfo;
import com.yzy.supercleanmaster.utils.DateUtils;

import java.util.LinkedList;
import java.util.List;

public class DbManager {

	private DbHelper dbHelper;
	private SQLiteDatabase wDb;
	private SQLiteDatabase rDb;

	public DbManager(Context context) {
		dbHelper = new DbHelper(new DbContext(context));
		wDb = dbHelper.getWritableDatabase();
		rDb = dbHelper.getReadableDatabase();
	}

	public void close() {
		wDb.close();
		rDb.close();
		dbHelper.close();
	}

	public void insertStart(String pacakgeName, String appName, long startTime,
			String networkType, long rx, long tx) {
		ContentValues value = new ContentValues();
		value.put(DbConstants.COLUMN_PACKAGE_NAME, pacakgeName);
		value.put(DbConstants.COLUMN_APP_NAME, appName);
		value.put(DbConstants.COLUMN_START_TIME, startTime);
		value.put(DbConstants.COLUMN_NETWORK_TYPE, networkType);
		value.put(DbConstants.COLUMN_RX, rx);
		value.put(DbConstants.COLUMN_TX, tx);
		wDb.insert(DbConstants.TABLE_NAME_TRAFFIC, null, value);
	}

	public void updateEnd(String pacakgeName, long endTime, long rx, long tx) {
		// 查询最新的一条记录
		Cursor start = rDb.query(DbConstants.TABLE_NAME_TRAFFIC, null,
				DbConstants.COLUMN_PACKAGE_NAME + " = '" + pacakgeName
						+ "' and " + DbConstants.COLUMN_END_TIME + " is null",
				null, null, null, DbConstants.COLUMN_START_TIME + " desc", "1");
		if (!start.moveToFirst()) {
			return;
		}
		ContentValues value = new ContentValues();
		value.put(DbConstants.COLUMN_END_TIME, endTime);
		value.put(
				DbConstants.COLUMN_RX,
				rx
						- start.getLong(start
								.getColumnIndexOrThrow(DbConstants.COLUMN_RX)));
		value.put(
				DbConstants.COLUMN_TX,
				tx
						- start.getLong(start
								.getColumnIndexOrThrow(DbConstants.COLUMN_TX)));

		wDb.update(
				DbConstants.TABLE_NAME_TRAFFIC,
				value,
				DbConstants.COLUMN_ID
						+ "="
						+ start.getInt(start
								.getColumnIndexOrThrow(DbConstants.COLUMN_ID)),
				null);
		start.close();
	}

    public long queryTodayTotal() {
        Cursor c = rDb.rawQuery("select sum("
                + DbConstants.COLUMN_RX + " + " + DbConstants.COLUMN_TX
                + ") from " + DbConstants.TABLE_NAME_TRAFFIC + " where "
                + DbConstants.COLUMN_NETWORK_TYPE + " = '" + DbConstants.NETWORK_TYPE_MOBILE+ "' and "
                + DbConstants.COLUMN_END_TIME + " is not null and "
                + DbConstants.COLUMN_START_TIME + " > "
                + DateUtils.getCurrentDayMillis(), null);
        long result = 0;
        if(c.moveToNext()){
            result = c.getLong(0);
        }
        return result;
    }

    public long queryMonthTotal() {
        Cursor c = rDb.rawQuery("select sum("
                + DbConstants.COLUMN_RX + " + " + DbConstants.COLUMN_TX
                + ") from " + DbConstants.TABLE_NAME_TRAFFIC + " where "
                + DbConstants.COLUMN_NETWORK_TYPE + " = '" + DbConstants.NETWORK_TYPE_MOBILE+ "' and "
                + DbConstants.COLUMN_END_TIME + " is not null and "
                + DbConstants.COLUMN_START_TIME + " > "
                + DateUtils.getCurrentMonthMillis(), null);
        long result = 0;
        if(c.moveToNext()){
            result = c.getLong(0);
        }
        return result;
    }

    public List<TrafficInfo> queryTotal() {

        Cursor c = rDb.rawQuery("select " + DbConstants.COLUMN_PACKAGE_NAME
                + "," + DbConstants.COLUMN_APP_NAME + ","
                + DbConstants.COLUMN_NETWORK_TYPE + ",sum("
                + DbConstants.COLUMN_RX + "),sum(" + DbConstants.COLUMN_TX
                + ") from " + DbConstants.TABLE_NAME_TRAFFIC + " where "
                + DbConstants.COLUMN_END_TIME + " is not null group by "
                + DbConstants.COLUMN_PACKAGE_NAME + ","
                + DbConstants.COLUMN_APP_NAME + ","
                + DbConstants.COLUMN_NETWORK_TYPE, null);
        List<TrafficInfo> list = new LinkedList<TrafficInfo>();
        while (c.moveToNext()) {
            String packageName = c.getString(0);
            TrafficInfo item = null;
            boolean flag = false;
            int i = 0;
            for ( ; i < list.size(); i++) {
                if(list.get(i).getPackageName().equals(packageName)){
                    flag  = true;
                    break;
                }

            }
            if (!flag) {
                item = new TrafficInfo();
                item.setPackageName(c.getString(0));
                item.setAppName(c.getString(1));
                list.add(item);
            } else {
                item = list.get(i);
            }
            String networkType = c.getString(2);
            if (networkType.equals(DbConstants.NETWORK_TYPE_MOBILE)) {
                item.setMobileRx(c.getLong(3));
                item.setMobileTx(c.getLong(4));
            } else if (networkType.equals(DbConstants.NETWORK_TYPE_WIFI)) {
                item.setWifiRx(c.getLong(3));
                item.setWifiTx(c.getLong(4));
            }

        }
        return list;
    }
}
