package com.hazani.chagit.homeexpensemanagment.callbacks;

/**
 * Created by chagithazani on 2/15/16.
 */
public interface DoItCallback {
    public static int EXPENSE_OBJECT = 11;
    public static int INCOME_OBJECT = 22;
    public static int DEBT_OBJECT = 33;
    public void okSelected(Object object, int objectType);
}
