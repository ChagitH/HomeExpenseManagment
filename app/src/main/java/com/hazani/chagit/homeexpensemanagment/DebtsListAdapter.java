package com.hazani.chagit.homeexpensemanagment;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hazani.chagit.homeexpensemanagment.callbacks.DoItCallback;
import com.hazani.chagit.homeexpensemanagment.callbacks.LongClickCallback;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

/**
 * Created by chagithazani on 2/1/16.
 */
public class DebtsListAdapter extends BaseAdapter{

    ArrayList<Debt> debts;
    Context context;
    public DebtsListAdapter(Context context){
        debts = new ArrayList<Debt>();
        this.context = context;
    }
    public void addDebt(Debt debt){
        debts.add(debt);
    }
    public void addDebt(Collection debts){
        this.debts.addAll(debts);
    }
    @Override
    public int getCount() {
        return debts.size();
    }

    @Override
    public Debt getItem(int position) {
        return debts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getID();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final Debt debt = getItem(position);
        if (convertView == null) {

            convertView = infalInflater.inflate(R.layout.list_item_debt, parent, false);
        }
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (longClickCallback != null) longClickCallback.onLongClick(debt, DoItCallback.DEBT_OBJECT);
                return true;
            }
        });
//
//        if(income.isRoutineIncome()){
//            convertView.setBackgroundColor(context.getResources().getColor(R.color.colorForLevelOneCell));
//        } else {
//            convertView.setBackgroundColor(context.getResources().getColor(R.color.colorForLevelThreeCell));
//        }
        TextView tvCreditorName = (TextView) convertView.findViewById(R.id.debt_list_item_TV_creditor_name);
        TextView tvSum = (TextView) convertView.findViewById(R.id.debt_list_item_TV_original_sum);
        TextView tvPaymentsLeft = (TextView) convertView.findViewById(R.id.debt_list_item_TV_payments_left);
        TextView tvCalculatedSum = (TextView) convertView.findViewById(R.id.debt_list_item_TV_calculated_sum);
        TextView tvInterestRate = (TextView) convertView.findViewById(R.id.debt_list_item_TV_interest_rate);


        //String interestRateText = context.getString(R.string.debts_interest_rate) + " " + String.valueOf(debt.getInterestRate());

        //String leftoverSumText = context.getString(R.string.debts_total_calculated) + " " + String.valueOf(debt.getInterestRate());

        NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
        int leftSum = debt.getLeftoverSum();
        format.format(leftSum);

        tvCreditorName.setText(debt.getCreditorName());
        tvCreditorName.setTypeface(null, Typeface.BOLD);

        String originalSumText = context.getString(R.string.debts_original_sum) + "  " + String.valueOf(debt.getSum());
        tvSum.setText(originalSumText);

        String interestRateText = String.valueOf(debt.getInterestRate()) + " " +context.getString(R.string.percent);
        tvInterestRate.setText(interestRateText);

        String paymentsLeftText = context.getString(R.string.debts_payments_left) + "  " + String.valueOf(debt.getNumOfPayments());
        tvPaymentsLeft.setText(paymentsLeftText);

        //String leftoverSumText = context.getString(R.string.debts_calculated_sum) + "  " + String.valueOf(debt.getLeftoverSum());
        String leftoverSumText = context.getString(R.string.debts_calculated_sum) + "  " + String.valueOf(leftSum);
        tvCalculatedSum.setText(leftoverSumText);
        return convertView;
    }

    private LongClickCallback longClickCallback;
    public void setLongClickCallback(LongClickCallback callback) {
        this.longClickCallback = callback;
    }

    public void removeDebt(Debt debt) {
        this.debts.remove(debt);
    }

    public void removeAllItems() {
        if(debts != null) debts.clear();
    }

}
