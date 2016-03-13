package com.hazani.chagit.homeexpensemanagment;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Date;
import java.util.List;

/**
 * Created by chagithazani on 1/31/16.
 */
public class Income {

    public interface IncomeSavedCallback{
        public void incomeSaved(Income income);
    }
    private double sum;
    private ParseObject object;
    private Date month;
    private ParseObject createdInPowerOf;

    private static int ID = 1;
    private int myID;
    public Income(ParseObject object){
        myID = ID++;
        this.object = object;
        this.sum = object.getDouble(Constants.c_SUM);
        this.name = object.getString(Constants.c_NAME);
        this.month = object.getDate(Constants.c_DATE);
        this.createdInPowerOf = object.getParseObject(Constants.c_CREATED_IN_POWER_OF_TEMPLATE);
    }

    public Income(String name, double sum, ParseUser user, Date month, ParseObject createdInPowerOf,final IncomeSavedCallback callback){
        myID = ID++;
        this.sum = sum;
        this.name = name;
        this.month = month;
        this.createdInPowerOf = createdInPowerOf;
        this.object = createIncomeObjectToSave(this.name, this.sum, user, this.month, this.createdInPowerOf);
        this.object.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null) {
                    if (callback != null) {
                        callback.incomeSaved(Income.this);
                    }
                } else {
                    Log.e("XXXXXXXXXX", e.getMessage());
                }
            }
        });
    }
    private static ParseObject createIncomeObjectToSave(String name, double sum, ParseUser user, Date month, ParseObject createdInPowerOf){
        ParseObject pobject = new ParseObject(Constants.t_INCOME);
        pobject.put(Constants.c_NAME, name);
        pobject.put(Constants.c_SUM, sum);
        pobject.put(Constants.c_USER, ParseHelper.getAccount(user));
        if(month != null) {
            pobject.put(Constants.c_DATE, month);
        }
        if(createdInPowerOf != null) {
            pobject.put(Constants.c_CREATED_IN_POWER_OF_TEMPLATE, createdInPowerOf);
        }
        return  pobject;
    }

    public int getID(){
        return myID;
    }
    public String getName() {
        return name;
    }
    private String name;
    public int getSum(){
        return (int)this.sum;
    }
    public boolean isRoutineIncome(){
        return !(this.createdInPowerOf == null);
    }

    public static void makeIncome(final String name, final double sum, final ParseUser user, final Date month, boolean isRoutine ,final IncomeSavedCallback callback){
        if(isRoutine){ //make a Template and then a regular one.
            new Income(name, sum, user, null, null, new IncomeSavedCallback() {
                @Override
                public void incomeSaved(Income income) {
                    new Income(name, sum, user, DateHelper.getFirstOfMonth(month,0), income.object, callback);
                }
            });
        } else {
            new Income(name, sum, user, month, null, callback);
        }
    }

    public static void makeIncomeFromTemplate(ParseObject incomeT, Date today) {
        ParseObject obj = createIncomeObjectToSave(incomeT.getString(Constants.c_NAME), incomeT.getDouble(Constants.c_SUM), ((ParseUser)incomeT.get(Constants.c_USER)), today, incomeT);
        obj.saveInBackground();
    }

    /*
        1- find template
        2- find all siblings and remove template
        3- delete template
        4- delete this months income
     */
    public void deleteFromThisMonthOn(Date month) {
        if(isRoutineIncome()){
            final ParseObject template = object.getParseObject(Constants.c_CREATED_IN_POWER_OF_TEMPLATE);

            if(template != null && month != null){
                ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.t_INCOME);
                query.whereEqualTo(Constants.c_CREATED_IN_POWER_OF_TEMPLATE, template);
                query.whereLessThan(Constants.c_DATE, month);
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null) {
                            if (objects != null) {
                                for (ParseObject obj : objects) {
                                    obj.remove(Constants.c_CREATED_IN_POWER_OF_TEMPLATE);
                                    obj.saveInBackground();
                                }
                            }
                            // now i can delete the template
                            template.deleteInBackground();
                        }
                    }
                });
            }
        }
        //delete this months income
        delete();
    }

    public void delete(){
        this.object.deleteInBackground();
    }
}
