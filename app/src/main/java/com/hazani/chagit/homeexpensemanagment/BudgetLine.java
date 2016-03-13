package com.hazani.chagit.homeexpensemanagment;

import android.util.Log;

import com.hazani.chagit.homeexpensemanagment.callbacks.BudgetsFetchedCallback;
import com.hazani.chagit.homeexpensemanagment.callbacks.ExpenseFetchedCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by chagithazani on 1/19/16.
 */
public class BudgetLine extends ExpandableItem {
    protected BudgetLine(String name, int sum, ParseUser user, boolean holo, Category category) {
        super(name, holo);
        if (!holo && user != null && category != null) {
            this.object = new ParseObject(Constants.t_BUDGET_LINE);
            this.object.put(Constants.c_NAME, name);
            this.object.put(Constants.c_EXPECTED_BUDGET_SUM, sum);
            this.object.put(Constants.c_CATEGORY, category.getObject());
            this.object.put(Constants.c_USER, ParseHelper.getAccount(user));
            this.object.saveEventually();
        }
    }

    BudgetsFetchedCallback budgetCallback;

    protected BudgetLine(ParseObject object) {
        super(object, false); // in budget line there is no sense to make it fetch childeren, because he needs to get a date
        //this.budgetCallback = budgetCallback;
    }


    /*
    DEFAULT will be to fetch expenses of this month
     */
    @Override
    void fetchChildren() {
        ParseQuery<ParseObject> query = prepareChilderenQuery(new Date());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                addChildren(objects);
            }
        });
    }


//    public void fetchChilderenNow(Date month){
//        ParseQuery<ParseObject> query = prepareChilderenQuery(month);
//        try {
//            List<ParseObject> expenseObjects = query.find();
//            addChildren(expenseObjects);
//
//        } catch (ParseException e){
//            e.printStackTrace();
//        }
//    }

    private void addChildren(List<ParseObject> expenseObjects){
        if(expenseObjects != null){
            for(ParseObject obj : expenseObjects) {
                this.addChild(new Expense(obj));
            }
        }
    }
    private ParseQuery<ParseObject> prepareChilderenQuery(Date month){
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.t_EXPENSE);
        query.whereEqualTo(Constants.c_BUDGET_LINE, object);
        query.whereDoesNotExist(Constants.c_STANDING_EXPENSE);//standing date anyway does not have a date...
        Date beginningOfThisMonth = DateHelper.getFirstOfMonthForFetchMethods(month, 0);
        Date beginningOfNextMonth = DateHelper.getFirstOfMonthForFetchMethods(month, 1);

        query.whereGreaterThanOrEqualTo(Constants.c_DATE, beginningOfThisMonth);// beginning of month
        query.whereLessThan(Constants.c_DATE, beginningOfNextMonth);//beginning of next month
        return query;
    }
    /*
    fetchChildrenForMonth(boolean, Date,ExpenseFetchedCallback) will get add as children Expenses that are of certain month.
     */
    void fetchChildrenForMonth(Date month) throws ParseException {
        if (object != null) {
            if (children == null) {
                children = new ArrayList<ExpandableItem>();
            } else if (children.size() > 0) {
                children.clear();
            }
            ParseQuery<ParseObject> query = prepareChilderenQuery(month);
            List<ParseObject> expenses = query.find();
            if (expenses != null) {
                for (ParseObject expense : expenses) {
                    addChild(new Expense(expense));
                }
            }

        }
    }

    public double getPercentage() {
        // no estimated budget entered
        if(getSum() <= 0) return -1;
        double totalSum = 0;
        if(children != null) {
            for (ExpandableItem item : children) {
                totalSum += item.getSum();
            }
        }
        return totalSum/getSum();
    }

    @Override
    public void setSum(int sum) {
        setSum(sum, true);
    }

    private void setSum(int sum, boolean save) {
        // BudgetLine saves sum as c_EXPECTED_BUDGET_SUM
        this.sum = sum;
        this.object.put(Constants.c_EXPECTED_BUDGET_SUM, sum);
        if(save) this.object.saveEventually();
    }
    @Override
    void fetchSum(ParseObject object) {
        this.sum = object.getInt(Constants.c_EXPECTED_BUDGET_SUM);
    }


    @Override
    ItemType getType() {
        return ItemType.BUDGET_ITEM;
    }


    public ParseUser getUser() {
        return (ParseUser) object.get(Constants.c_USER);
    }

    public void update(String newName, int sum) {
        boolean changed = false;
        if( ! newName.equalsIgnoreCase(getName())){
            object.put(Constants.c_NAME , newName);
            this.name = newName;
            changed = true;
        }
        if(sum != getSum()){
            setSum(sum, false);
            changed = true;
        }
        if(changed) object.saveInBackground();
    }
}
