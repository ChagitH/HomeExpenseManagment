package com.hazani.chagit.homeexpensemanagment;

/**
 * Created by chagithazani on 1/28/16.
 */
interface SaveToDataBaseCallback{
    public void actionSucceeded();
    public void actionFaild(Exception exception/*String errorMsg*/);
}
