package com.hazani.chagit.homeexpensemanagment;

import android.util.Log;

import com.hazani.chagit.homeexpensemanagment.callbacks.BudgetsFetchedCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Date;
import java.util.List;

/**
 * Created by chagithazani on 1/19/16.
 */
public class Category extends ExpandableItem {

    static int NOTIFICATION_OF_BUDGET_FETCH_ONE_BY_ONE = 9;
    static int NOTIFICATION_OF_BUDGET_FETCH_ALL = 7;

    @Override
    public void setSum(int sum) {
        // do nothing! Category, does not have sum.
    }

    protected Category(String name, ParseUser user, boolean holo) {
        super(name, holo);
        if (!holo && user != null) {
            this.object = new ParseObject(Constants.t_CATEGORY);
            this.object.put(Constants.c_NAME, name);
            this.object.put(Constants.c_USER, ParseHelper.getAccount());
            this.object.saveInBackground();
        }
    }

    protected Category(ParseObject object, boolean fetchChildren) {
        super(object, fetchChildren);
    }
    protected Category(ParseObject object) {
        super(object, false);
    }
    @Override
    void fetchSum(ParseObject object) {
        //do nothing. Category has not saved sum
    }

    @Override
    void fetchChildren() {
        ParseQuery<ParseObject> query = getBudgetLineQuery();
        if(query == null) return;
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> biList, ParseException e) {
                if (e == null) {
                   addChildren(biList);
                }
            }
        });
    }


    public void fetchChildrenNow() throws ParseException {
        ParseQuery<ParseObject> query = getBudgetLineQuery();
        List<ParseObject> objects =  query.find();
        addChildren(objects);
    }

    private void addChildren(List<ParseObject> objects){
        if (objects != null){
            for(ParseObject blObject : objects){
                addChild(new BudgetLine(blObject));
            }
        }
    }
    private ParseQuery<ParseObject> getBudgetLineQuery (){
        if (object != null) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.t_BUDGET_LINE);
            query.whereEqualTo(Constants.c_CATEGORY, object);
            return query;
        }
        return null;
    }

    @Override
    ItemType getType() {
        return ItemType.CATEGORY;
    }

    public void setName(String newName) {
        if( newName != null && ! getName().equalsIgnoreCase(newName)){
            this.name = newName;
            this.object.put(Constants.c_NAME, newName);
            this.object.saveEventually();
        }
    }


}
