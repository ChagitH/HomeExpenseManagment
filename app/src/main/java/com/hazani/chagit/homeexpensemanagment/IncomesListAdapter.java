package com.hazani.chagit.homeexpensemanagment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hazani.chagit.homeexpensemanagment.callbacks.DoItCallback;
import com.hazani.chagit.homeexpensemanagment.callbacks.LongClickCallback;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by chagithazani on 1/31/16.
 */
public class IncomesListAdapter extends BaseAdapter {
    ArrayList<Income> incomes;
    Context context;
    public IncomesListAdapter(Context context){
        incomes = new ArrayList<Income>();
        this.context = context;
    }
    public void addIncome(Income income){
        incomes.add(income);
    }
    public void addIncome(Collection incomes){
        this.incomes.addAll(incomes);
    }
    public void replaceIncoms(Collection incomes){
        clearIncomes();
        addIncome(incomes);
    }
    public void clearIncomes(){
        this.incomes.clear();
    }
    @Override
    public int getCount() {
        return incomes.size();
    }

    @Override
    public Income getItem(int position) {
        return incomes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getID();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final Income income = getItem(position);
        if (convertView == null) {
            convertView = infalInflater.inflate(R.layout.list_item_income, parent, false);
        }

        if(income.isRoutineIncome()){
            convertView.setBackgroundColor(context.getResources().getColor(R.color.colorForLevelOneCell));
        } else {
            convertView.setBackgroundColor(context.getResources().getColor(R.color.colorForLevelThreeCell));
        }
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(longClickCallback != null) longClickCallback.onLongClick(income, DoItCallback.INCOME_OBJECT);
                return true;
            }
        });
        TextView tvSum = (TextView) convertView.findViewById(R.id.income_list_item_TVsum);
        TextView tvName = (TextView) convertView.findViewById(R.id.income_list_item_TVname);

        tvName.setText(income.getName());
        tvSum.setText(String.valueOf(income.getSum()));
        return convertView;
    }

    private LongClickCallback longClickCallback;
    public void setLongClickCallback(LongClickCallback callback) {
        this.longClickCallback = callback;
    }

    public void removeIncome(Income income) {
        incomes.remove(income);
    }
}
