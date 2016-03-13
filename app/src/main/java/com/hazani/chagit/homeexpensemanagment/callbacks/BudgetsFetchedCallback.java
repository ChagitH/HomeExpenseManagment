package com.hazani.chagit.homeexpensemanagment.callbacks;

import com.hazani.chagit.homeexpensemanagment.BudgetLine;

/**
 * Created by chagithazani on 2/3/16.
 */
public interface BudgetsFetchedCallback {
    public void budgetFetched(BudgetLine budget, int notificationOfBudgetFetchAll);
}
