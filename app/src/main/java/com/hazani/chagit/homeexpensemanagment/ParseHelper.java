package com.hazani.chagit.homeexpensemanagment;

import android.os.AsyncTask;
import android.util.Log;

import com.hazani.chagit.homeexpensemanagment.callbacks.CategoriesFetchCallback;
import com.hazani.chagit.homeexpensemanagment.callbacks.ExpenseFetchedCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by chagithazani on 1/26/16.
 */
public class ParseHelper {

    /*
    will return account if account exsits. if not (it means user IS the account), will return user.
     */
    public static ParseUser getAccount(ParseUser user){
        if(user==null){
            return null;
        }
        Object account = null;
        try {
            account = user.fetchIfNeeded().get(Constants.c_ACCOUNT);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return account != null ? (ParseUser)account : user;
    }

    public static ParseUser getAccount(){
        return getAccount(getConnectedUser());
    }

    public static void fetchAllExpensesByDate(Date month) throws ParseException {
        if(categories != null/* && categories.size() > 0*/) {
            for (Category category : categories) {
                if (category.children != null) { //it will not work if called to early. not good!
                    for (ExpandableItem budget : category.children) {
                        ((BudgetLine) budget).fetchChildrenForMonth(month);
                    }
                }
            }
            //if(expenseCallback != null) expenseCallback.expensesFetched(null);
        }
    }

    public static  ArrayList<Category> categories;
    private static ArrayList<CategoriesFetchCallback> listeners = new ArrayList<CategoriesFetchCallback>();
    public static ArrayList<Category> getCategories() {
        return categories;
    }


    private static void notifyAllListenersCategoriesFetched(boolean success){
        for(CategoriesFetchCallback listener : listeners){
            listener.categoriesFetched(success);
        }
    }

    public static void fetchCategoriesInBackThread() throws Exception{
            new AsyncTask(){

                @Override
                protected Object doInBackground(Object[] params) {
                    if(ParseHelper.getConnectedUser() != null) {
                        try {
                            ParseHelper.fetchCategoriesNow();
                        } catch (Exception e) {
                            return e.getMessage();
                        }
                    } else {
                            return "No Connected User";
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Object o) {
                    super.onPostExecute(o);
                    notifyAllListenersCategoriesFetched(o == null);

                }
            }.execute();
    }

    private static  void fetchCategoriesNow() throws Exception, ParseException {

        if (categories == null) {
            categories = new ArrayList<Category>();
        } else {
            categories.clear();
        }

        if(ParseUser.getCurrentUser() == null){
            return;
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.t_CATEGORY);
        query.whereEqualTo(Constants.c_USER, getAccount());
        List<ParseObject> categoryList = query.find();
        if(categoryList != null) {
            for (ParseObject categoryObject : categoryList) {
                Category category = new Category(categoryObject);
                categories.add(category);
                category.fetchChildrenNow();
            }
            //notifyAllListenersCategoriesFetched();

        } else {
            throw new Exception("Fetch data failed");
        }
    }


    public static ParseUser getConnectedUser(){
        return ParseUser.getCurrentUser();
    }

    private static ParseQuery<ParseObject> prepareFetchStandingExpensesQuery(ParseUser account) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.t_EXPENSE);
        query.whereEqualTo(Constants.c_STANDING_EXPENSE, true);
        query.whereEqualTo(Constants.c_USER, account);
        return query;
    }



    private static ParseQuery<ParseObject> prepareFetchTemplateDebtsQuery(ParseUser user){
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.t_DEBT);
        query.whereEqualTo(Constants.c_USER, ParseHelper.getAccount(user));
        query.whereDoesNotExist(Constants.c_MONTH_AND_YEAR).whereDoesNotExist(Constants.c_CREATED_IN_POWER_OF_TEMPLATE);
        //query.whereGreaterThan(Constants.c_LEFTOVER_SUM > 0) //not doing this because my calculation is not accurate and it will reach 0 before it will in reality.
        return query;
    }
    private static ParseQuery<ParseObject> prepareFetchAllDebtsQueryWithPaymentPlan(ParseUser user){
        ParseQuery<ParseObject> query = prepareFetchTemplateDebtsQuery(user);
        query.whereGreaterThan(Constants.c_SUM_OF_PAYMENT, 0).whereGreaterThan(Constants.c_NUM_OF_PAYMENTS_LEFT, 0);
        //query.whereGreaterThan(Constants.c_LEFTOVER_SUM , 0); //not doing this because my calculation is not accurate and it will reach 0 before it will in reality.
        return query;
    }
    public static void fetchParentDebts(ParseUser user, final Debt.DebtsFetchedCallback callback){
        ParseQuery<ParseObject> query = prepareFetchTemplateDebtsQuery(user);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> debtList, ParseException e) {
                if (e == null) {
                    callback.debtsFetched(debtList);
                } else {
                    Log.d("fetchIncomes", "Error: " + e.getMessage());
                }

            }
        });
    }

    /*
    method MonthlyRoutine(Date sinceWhenNotUpdated) will preform rutine job of copping Debts and updating the relevant fields of the changes:
    1. creating a new Debt object, that has the Template Debt in the column CreatedInPowerOf, and has a date of that month.
    2. the new Debt will have a sum of the amount of a monthly payment, and will have the payment number it is.
    3. in the Template Debt - the fields numOfPayments will update to reduce the number of payments, and the calculated sum will be updated as well to reduce the payments done
*/
    public static void monthlyRoutine(Date sinceWhenNotUpdated) throws Exception {
        Date today = DateHelper.getFirstOfMonth(new Date(), 0);
        sinceWhenNotUpdated = DateHelper.getFirstOfMonth(sinceWhenNotUpdated,0);

        ParseQuery<ParseObject> queryDebts = prepareFetchAllDebtsQueryWithPaymentPlan(ParseHelper.getAccount());
        List<ParseObject> debtTemplates = queryDebts.find();

        ParseQuery<ParseObject> queryIncomes = prepareFetchTemplateIncomesQuery(ParseHelper.getAccount());
        List<ParseObject> incomeTemplates = queryIncomes.find();

        ParseQuery<ParseObject> queryStandingExpenses = prepareFetchStandingExpensesQuery(ParseHelper.getAccount());
        List<ParseObject> standingExpenses = queryStandingExpenses.find();

        ;
        while (! DateHelper.isSameMonthAndYear(today, sinceWhenNotUpdated)){
            /*
            I am adding a month the first thing because the month that it was last updated, it was already updated.
            now we need to update from the next month on- until it meats today's month.
             */
            sinceWhenNotUpdated = DateHelper.addMonthSameDay(sinceWhenNotUpdated,1);
            if(debtTemplates != null) {
                for (ParseObject debtT : debtTemplates) {
                    Debt.makeDebtFromTemplate(debtT, sinceWhenNotUpdated);
                }
            }

            if(incomeTemplates != null) {
                for (ParseObject incomeT : incomeTemplates) {
                    Income.makeIncomeFromTemplate(incomeT, sinceWhenNotUpdated);
                }
            }

            if(standingExpenses != null) {
                for (ParseObject standingExpense : standingExpenses) {
                    Expense.expenseFromStandingExpense(standingExpense, sinceWhenNotUpdated);
                }
            }

        }
    }



    public static List<ParseObject> fetchMonthlyDebtsNow(ParseUser user, Date monthAndYear) throws ParseException {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.t_DEBT);
        query.whereEqualTo(Constants.c_USER, ParseHelper.getAccount(user));
        Date beginningOfMonth = DateHelper.getFirstOfMonthForFetchMethods(monthAndYear, 0);
        Date beginningOfNextMonth = DateHelper.getFirstOfMonthForFetchMethods(monthAndYear, 1);
//        Date beginningOfMonth = DateHelper.getFirstOfMonth(monthAndYear, 0);
//        Date beginningOfNextMonth = DateHelper.getFirstOfMonth(monthAndYear, 1);
        query.whereGreaterThanOrEqualTo(Constants.c_MONTH_AND_YEAR, beginningOfMonth).whereLessThan(Constants.c_MONTH_AND_YEAR, beginningOfNextMonth);
        return query.find();
    }

    public static String preformDatabaseUpdate(){
        Log.d("%%%%%%%%%%%%%%%%", "preformDatabaseUpdate()");
        final String[] retrunStatment = {null};
        ParseQuery query = new ParseQuery(Constants.t_LAST_UPDATE);
        query.whereEqualTo(Constants.c_USER, ParseHelper.getAccount());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    // success
                    if (objects != null && objects.size() > 0) {
                        ParseObject lastUpdatedObject = objects.get(0);
                        Date lastUpdated = (Date) lastUpdatedObject.get(Constants.c_UPDATE);
                        if (DateHelper.didMonthYearChangeSinceDate(lastUpdated)) {
                            // do maintenance
                            Log.d("%%%%%%%%%%%%%%%%", "DateHelper.didMonthChangeSinceDate(lastUpdated) = true");
                            // Expenses - standing orders

                            // incomes - copy templates

                            // Debts - create monthly payment and update Template Debts calculated sum and payments left

                            try {
                                monthlyRoutine(lastUpdated);
                                lastUpdatedObject.put(Constants.c_UPDATE, new Date());
                                lastUpdatedObject.save();
                            } catch (Exception e1) {
                                e1.printStackTrace();
                                retrunStatment[0] = e1.getMessage();
                            }
                            // at the end,  update the date
                        } else {
                            Log.d("%%%%%%%%%%%%%%%%", "DateHelper.didMonthChangeSinceDate(lastUpdated) = false");
                        }
                    } else if (getAccount() != null) {
                        ParseObject lastUpdatedObject = ParseObject.create(Constants.t_LAST_UPDATE);
                        lastUpdatedObject.put(Constants.c_USER, getAccount());
                        lastUpdatedObject.put(Constants.c_UPDATE, new Date());
                        lastUpdatedObject.saveInBackground();
                    }
                } else {
                    //failure
                    retrunStatment[0] = e.getMessage();
                }
            }
        });
        return retrunStatment[0]; // if null -> success
    }

    private static ParseQuery<ParseObject> prepareFetchTemplateIncomesQuery(ParseUser account) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.t_INCOME);
        query.whereEqualTo(Constants.c_USER, ParseHelper.getAccount());
        query.whereDoesNotExist(Constants.c_DATE); // no date == Template. (no CreatedInPowerOf == Template || OneTimeIncome)
        return query;
    }
    public static void fetchMonthlyIncomes(ParseUser user, final IncomesFetchCallback callback, Date month){
        ParseQuery<ParseObject> query = prepareFetchIncomesByMonthQuery(user, month);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> incomeList, ParseException e) {
                if (e == null) {
                    callback.incomeFetched(incomeList);
                } else {
                    Log.d("fetchIncomes", "Error: " + e.getMessage());
                }

            }
        });
    }
    public static List<ParseObject> fetchMonthlyIncomesNow(ParseUser account, Date date) throws ParseException {
        ParseQuery<ParseObject> query = prepareFetchIncomesByMonthQuery(account, date);
        return query.find();
    }
    private static ParseQuery<ParseObject> prepareFetchIncomesByMonthQuery(ParseUser user, Date month){
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.t_INCOME);
        query.whereEqualTo(Constants.c_USER, ParseHelper.getAccount(user));
        Date beginingOfMonth = DateHelper.getFirstOfMonthForFetchMethods(month,0);
        Date beginingOfNextMonth  = DateHelper.getFirstOfMonthForFetchMethods(month, 1);
//        Date beginingOfMonth = DateHelper.getFirstOfMonth(month,0);
//        Date beginingOfNextMonth  = DateHelper.getFirstOfMonth(month, 1);
        Log.i("fetchIncomes", "beginingOfMonth: " + beginingOfMonth + " beginingOfNextMonth: " + beginingOfNextMonth);
        query.whereGreaterThanOrEqualTo(Constants.c_DATE, beginingOfMonth);
        query.whereLessThan(Constants.c_DATE, beginingOfNextMonth);
        return query;
    }


    public static void addCategoryFetchListener(CategoriesFetchCallback listener) {
        if( ! listeners.contains(listener)) listeners.add(listener);
    }

    public static ParseUser getAccount(String username, String password) throws ParseException{
        //parse does not find this!
//        ParseQuery<ParseUser> query = ParseUser.getQuery();
//        query.whereEqualTo(Constants.c_USERNAME, username);
//        query.whereEqualTo(Constants.c_PASSWORD, password);
//        query.whereDoesNotExist(Constants.c_ACCOUNT);
//        List<ParseUser> account = query.find();

//        if(account == null) return null;
//        if(account.size() > 0){
//            return account.get(0);
//        } else {
//            return null;
//        }
        ParseUser.logIn(username, password);

        ParseUser accountUser = ParseUser.getCurrentUser();
        if (accountUser != null) {
            if ( ! accountUser.containsKey(Constants.c_ACCOUNT)) {
                ParseUser.logOut();
                return accountUser;
            }

        }
        ParseUser.logOut();
        return null;

    }
}
