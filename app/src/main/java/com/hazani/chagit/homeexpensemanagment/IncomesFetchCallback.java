package com.hazani.chagit.homeexpensemanagment;

import com.parse.ParseObject;

import java.util.List;

/**
 * Created by chagithazani on 1/31/16.
 */
public interface IncomesFetchCallback {
    public void incomeFetched(List<ParseObject> parseObjects);

}
