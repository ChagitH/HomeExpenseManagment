package com.hazani.chagit.homeexpensemanagment;

import android.util.Log;

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
public class Debt {

    public interface DebtSavedCallback{
        public void debtSaved(Debt debt);
    }
    public interface DebtsFetchedCallback{
        public void debtsFetched(List<ParseObject> debts);
    }

    private ParseObject object;
    private static int ID = 1;
    private int myID;

    public int getID(){
        return myID;
    }
    public String getCreditorName(){
        return this.object.getString(Constants.c_CREDITOR_NAME);
    }
    public int getNumOfPayments(){
        return object.getInt(Constants.c_NUM_OF_PAYMENTS_LEFT);
    }
    public double getInterestRate(){
        return object.getDouble(Constants.c_INTEREST_RATE);
    }
    public int getSum(){
        return object.getInt(Constants.c_SUM);
    }
    public Date getDateOfPayment(){
        return object.getDate(Constants.c_MONTH_AND_YEAR);
    }
    public int getLeftoverSum() {
        return object.getInt(Constants.c_LEFTOVER_SUM);
    }
    public int getSumOfMonthlyPayment() {
        return object.getInt(Constants.c_SUM_OF_PAYMENT);
    }


    public Debt(ParseObject object){
        myID = ID++;
        this.object = object;
    }
    public Debt(String creditorName, int sum, double interestRate, int numOfPayments, int sumOfMonthlyPayment, ParseUser user, Date paymentForMonth, Date firstPaymentDate, ParseObject createdInPowerOf, final DebtSavedCallback callback){
        myID = ID++;
        this.object = new ParseObject(Constants.t_DEBT);
        this.object.put(Constants.c_CREDITOR_NAME, creditorName);
        this.object.put(Constants.c_SUM, sum);
        this.object.put(Constants.c_USER, ParseHelper.getAccount(user));
        this.object.put(Constants.c_INTEREST_RATE, interestRate);
        this.object.put(Constants.c_NUM_OF_PAYMENTS_LEFT, numOfPayments);
        this.object.put(Constants.c_SUM_OF_PAYMENT, sumOfMonthlyPayment);
        this.object.put(Constants.c_LEFTOVER_SUM, sum);

//        if(firstPaymentDate != null) {
//            this.object.put(Constants.c_FIRST_PAYMENT_DATE, firstPaymentDate);
//        }
        if(paymentForMonth != null) {
            this.object.put(Constants.c_MONTH_AND_YEAR, paymentForMonth);
        }
        if(createdInPowerOf != null) {
            this.object.put(Constants.c_CREATED_IN_POWER_OF_TEMPLATE, createdInPowerOf);
        }
        this.object.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null) {
                    if (callback != null) {
                        callback.debtSaved(Debt.this);
                    }
                } else {
                    Log.e("XXXXXXXXXX", e.getMessage());
                }
            }
        });
    }

    public void update(String creditorName, int sum, Double interestRate, int paymentsLeft, int monthlyPayment, Date startPayOnDate, final DebtSavedCallback callback) {
        boolean changed = false;
        if(! creditorName.equalsIgnoreCase(getCreditorName())) {
            object.put(Constants.c_CREDITOR_NAME, creditorName);
            changed = true;
        }
        if(sum != getLeftoverSum()){
            object.put(Constants.c_LEFTOVER_SUM, sum);
            changed = true;
        }
        if(interestRate != getInterestRate()) {
            object.put(Constants.c_INTEREST_RATE, interestRate);
            changed = true;
        }
        if(paymentsLeft != getNumOfPayments()) {
            object.put(Constants.c_NUM_OF_PAYMENTS_LEFT, paymentsLeft);
            changed = true;
        }
        if(monthlyPayment != getSumOfMonthlyPayment()) {
            object.put(Constants.c_SUM_OF_PAYMENT, monthlyPayment);
            changed = true;
        }

        if(changed){
            object.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (callback != null) callback.debtSaved(Debt.this);
                }
            });
        }
    }

    public static void makeDebtFromTemplate(ParseObject debtTemplate, Date month){
        ParseObject newDebt = new ParseObject(Constants.t_DEBT);
        newDebt.put(Constants.c_CREDITOR_NAME, debtTemplate.get(Constants.c_CREDITOR_NAME));
        int sumOfPayment = debtTemplate.getInt(Constants.c_SUM_OF_PAYMENT);
        newDebt.put(Constants.c_SUM, sumOfPayment);
        newDebt.put(Constants.c_USER, debtTemplate.get(Constants.c_USER));
        int numOfPaymentsLeft = debtTemplate.getInt(Constants.c_NUM_OF_PAYMENTS_LEFT);
        newDebt.put(Constants.c_PAYMENT_NUMBER, numOfPaymentsLeft);
        newDebt.put(Constants.c_CREATED_IN_POWER_OF_TEMPLATE, debtTemplate);
        newDebt.put(Constants.c_MONTH_AND_YEAR, month);
        //newDebt.put(Constants.c_INTEREST_RATE, debtTemplate.get());
        //newDebt.put(Constants.c_SUM_OF_PAYMENT, debtTemplate.get());
        //newDebt.put(Constants.c_LEFTOVER_SUM, debtTemplate.get(Constants));
        debtTemplate.put(Constants.c_NUM_OF_PAYMENTS_LEFT,numOfPaymentsLeft-1);
        debtTemplate.put(Constants.c_LEFTOVER_SUM,(debtTemplate.getInt(Constants.c_LEFTOVER_SUM)-sumOfPayment));

        newDebt.saveEventually();
        debtTemplate.saveEventually();
    }


    public void delete() {
        this.object.deleteInBackground();
    }

//    public static void makeDebt(boolean isTemplate, final String creditorName, final double sum, final ParseUser user, final Date paymentForMonth, final ParseObject createdInPowerOf, final DebtSavedCallback callback){
//        if(isTemplate){ //make a Template and then all regular debts.
//            new Debt(creditorName, sum, user, null, null, new DebtSavedCallback() {
//                @Override
//                public void debtSaved(Debt debt) {
//
//                    new Income(name, sum, user, month, income.object, callback);
//                }
//            });
//        } else {
//            new Income(name, sum, user, month, null, callback);
//        }
//    }
}
