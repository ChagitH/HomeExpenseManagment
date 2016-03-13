package com.hazani.chagit.homeexpensemanagment;

import android.text.style.TtsSpan;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by chagithazani on 1/26/16.
 */
public class Expense extends ExpandableItem {

    public String getNote() {
        return this.name;
    }

    protected Expense(ParseObject object) {
        super(object,false);
    }

    @Override
    void fetchSum(ParseObject object) {
        this.sum = object.getInt(Constants.c_SUM);
    }

    @Override
    public void setSum(int sum) {
        // Expense saves sum as sum
        this.sum = sum;
        this.object.add(Constants.c_SUM, sum);
        this.object.saveEventually();
    }

    @Override
    void fetchChildren() {
        // do nothing. Expense has no childeren
    }

    @Override
    ItemType getType() {
        return ItemType.EXPENSE;
    }

    public static void addPaymentsExpenseToDataBase(int sum,
                                                    int numOfPayments,
                                                    String note,
                                                    ParseObject budgetLine,
                                                    Date date,
                                                    final SaveToDataBaseCallback callback) throws Exception{

        ParseUser user = (ParseUser) budgetLine.get(Constants.c_USER);

        // create Template Expense
        ParseObject templateObject = new ParseObject(Constants.t_EXPENSE);
        //templateObject.put(Constants.c_SUM, sum);
        templateObject.put(Constants.c_TOTAL_NUM_OF_PAYMENTS, numOfPayments);
        //templateObject.put(Constants.c_DATE, DateHelper.geDateAddMonth(date, 0));// will be saved with the original date
        templateObject.put(Constants.c_BUDGET_LINE, budgetLine);
        templateObject.put(Constants.c_NAME, note);
        templateObject.put(Constants.c_USER, ParseHelper.getAccount(user));
        templateObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    callback.actionFaild(e);
                } else {
                    callback.actionSucceeded();
                }
            }
        });
        for (int i = 1; i <= numOfPayments; i++) {
            ParseObject expenseObject = createExpenseObject(sum, DateHelper.geDateAddMonth(date, i - 1), budgetLine, note + " תשלום " + i + "  מתוך" + " " + numOfPayments);
            expenseObject.put(Constants.c_EXPENSE_TEMPLATE, templateObject);
            //expenseObject.put(Constants.c_TOTAL_NUM_OF_PAYMENTS, numOfPayments);
            //expenseObject.put(Constants.c_PAYMENT_NUMBER, i);
            expenseObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        callback.actionFaild(e);
                    } else {
                        callback.actionSucceeded();
                    }
                }
            });

        }
    }

    private static ParseObject createExpenseObject(int sum, Date date, ParseObject budgetLine, String note) throws Exception{
        ParseUser user = (ParseUser) budgetLine.fetchIfNeeded().getParseUser(Constants.c_USER);
        ParseObject object = new ParseObject(Constants.t_EXPENSE);
        object.put(Constants.c_SUM, sum);
        object.put(Constants.c_DATE, date);
        object.put(Constants.c_BUDGET_LINE, budgetLine);
        object.put(Constants.c_NAME, note);
        object.put(Constants.c_USER, user);
        return object;
    }

    public static void addStandingExpenseToDataBase(int permanentSum, String note, ParseObject budgetLine, Date date, final SaveToDataBaseCallback callback) throws Exception {
        ParseObject expenseObject = createExpenseObject(permanentSum, date, budgetLine, note);
        expenseObject.put(Constants.c_STANDING_EXPENSE, true);
        expenseObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    callback.actionFaild(e);
                } else {
                    callback.actionSucceeded();
                }
            }
        });

    }

    public static void addExpense(int sum,
                                  int numOfPayments,
                                  String note,
                                  BudgetLine budgetLine,
                                  Date date,
                                  boolean standingExpense,
                                  final SaveToDataBaseCallback callback) throws Exception{
        addExpense(sum, numOfPayments, note, budgetLine.getObject(), date, standingExpense, callback);

    }

    private static void addExpense(int sum,
                                   int numOfPayments,
                                   String note,
                                   ParseObject budgetLine,
                                   Date date,
                                   boolean standingExpense,
                                   final SaveToDataBaseCallback callback) throws Exception {
        if (numOfPayments > 0) {
            addPaymentsExpenseToDataBase(sum, numOfPayments, note, budgetLine, date, callback);
        } else if (standingExpense) {
            addStandingExpenseToDataBase(sum, note, budgetLine, date, callback);
        } else {
            createExpenseObject(sum, date, budgetLine, note).saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        callback.actionFaild(e);
                    } else {
                        callback.actionSucceeded();
                    }
                }
            });
        }

    }


    public static void expenseFromStandingExpense(ParseObject standingOrderExpense, Date dateToAdd) throws Exception{

        int sum = standingOrderExpense.getInt(Constants.c_SUM);
        String note = standingOrderExpense.getString(Constants.c_NAME) + " ה. קבע";
        ParseObject budgetLineParent = (ParseObject) standingOrderExpense.get(Constants.c_BUDGET_LINE);

        ParseObject expense = createExpenseObject(sum, dateToAdd, budgetLineParent, note);
        expense.put(Constants.c_EXPENSE_TEMPLATE, standingOrderExpense); // saved for future development
        expense.save();
    }

    /*
    will retrun true if is part of payments or standing order
     */
    public boolean isPayment(){
        Object template = object.getParseObject(Constants.c_EXPENSE_TEMPLATE);
        return template != null;
    }


    public void deleteSelfAndAllDescendants(final Date fromDate){
        //find my template
        final ParseObject parent = object.getParseObject(Constants.c_EXPENSE_TEMPLATE);
        if(parent == null){
            return;
        }
        //find my siblings and erase them
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.t_EXPENSE);
        query.whereEqualTo(Constants.c_EXPENSE_TEMPLATE, parent);
        if(fromDate != null) query.whereGreaterThanOrEqualTo(Constants.c_DATE, DateHelper.getFirstOfMonth(fromDate,0));
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){
                    if(objects != null) {
                        for (ParseObject obj : objects) obj.deleteInBackground();
                    }
                    if(fromDate != null){
                        //find the rest of the siblings (from prior to the selected date), and remove the pointer to the template
                        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.t_EXPENSE);
                        query.whereEqualTo(Constants.c_EXPENSE_TEMPLATE, parent);
                        query.whereLessThan(Constants.c_DATE, fromDate);
                        query.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {
                                if (e == null) {
                                    if (objects != null) {
                                        for (ParseObject obj : objects) {
                                            obj.remove(Constants.c_EXPENSE_TEMPLATE);
                                            obj.saveInBackground();
                                        }
                                    }
                                    // now i can delete the template
                                    parent.deleteInBackground();
                                }
                            }
                        });
                    } else {
                        parent.deleteInBackground();
                    }

                }
            }
        });

    }

}
