package com.hazani.chagit.homeexpensemanagment;

import android.app.Application;
import android.os.AsyncTask;

import com.parse.Parse;

/**
 * Created by chagithazani on 3/3/16.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this);
        //checkForDatabaseUpdates
        new AsyncTask<Object, Object, String>() {
            @Override
            protected String doInBackground(Object... params) {
                return ParseHelper.preformDatabaseUpdate();
            }

        }.execute();

    }
}
