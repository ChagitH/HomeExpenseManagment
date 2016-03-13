package com.hazani.chagit.homeexpensemanagment;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.hazani.chagit.homeexpensemanagment.callbacks.DoItCallback;
import com.hazani.chagit.homeexpensemanagment.callbacks.LongClickCallback;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by chagithazani on 1/19/16.
 */
public class ExpandableBudgetStateListAdapter extends ExpandableMultilevelListAdapter {

    private View.OnClickListener listener;
    private Context context;
    //private Date date;

    ExpandableBudgetStateListAdapter(Context context, Date date) {
        super();
        this.context = context;

    }

    @Override
    public ArrayList<ExpandableItem> getChildren(int position) {
        ExpandableItem item = (ExpandableItem) items.get(position);
        return item.children;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ExpandableItem item = (ExpandableItem) items.get(position);
        switch (item.getType()) {
             case CATEGORY:
                if (convertView == null) {
                    convertView = infalInflater.inflate(R.layout.list_item_category_no_button, parent, false);
                }
                convertView.setOnLongClickListener(null);
                convertView.setBackgroundColor(context.getResources().getColor(R.color.colorForLevelThreeCell));
                TextView tvName = (TextView) convertView.findViewById(R.id.categoryItemETname);
                tvName.setTypeface(null, Typeface.BOLD);
                tvName.setText(item.getName());

                break;
            case BUDGET_ITEM:
                if (convertView == null) {
                    convertView = infalInflater.inflate(R.layout.list_item_budget_item_with_percentage, parent, false);
                }
                convertView.setOnLongClickListener(null);
                convertView.setBackgroundColor(context.getResources().getColor(R.color.colorForLevelTwoCell));
                TextView tvBName = (TextView) convertView.findViewById(R.id.budgetItemTVname);
                TextView tvPersentageSum = (TextView) convertView.findViewById(R.id.budgetItemPercentageNum);
                final TextView tvPercentageForground = (TextView) convertView.findViewById(R.id.budgetItemPersentageForground);
                final TextView tvPercentageBackground = (TextView) convertView.findViewById(R.id.budgetItemPersentageBackground);


                tvBName.setTypeface(null, Typeface.NORMAL);
                tvBName.setText(item.getName());

                final double percent = ((BudgetLine) item).getPercentage();
                String strPercent = "%" + (int)(percent * 100);
                //todo: there is a bug that the panels are sometimes the wrong color
                if (percent != -1){
                    tvPercentageForground.setVisibility(View.VISIBLE);
                    tvPercentageBackground.setVisibility(View.VISIBLE);
                    tvPersentageSum.setVisibility(View.VISIBLE);
                    tvPersentageSum.setText(strPercent);
                    tvPercentageBackground.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            int fullWidth = tvPercentageBackground.getWidth();
                            int width = (int) (fullWidth * percent);
                            tvPercentageForground.setLayoutParams(new FrameLayout.LayoutParams(width, FrameLayout.LayoutParams.MATCH_PARENT));
                            tvPercentageBackground.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });
                } else {
                    tvPercentageForground.setVisibility(View.INVISIBLE);
                    tvPercentageBackground.setVisibility(View.INVISIBLE);
                    tvPersentageSum.setVisibility(View.INVISIBLE);
                }

                break;
            case EXPENSE:
                if (convertView == null) {
                    convertView = infalInflater.inflate(R.layout.list_item_expense, parent, false);
                }
                convertView.setBackgroundColor(context.getResources().getColor(R.color.colorForLevelOneCell));
                convertView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (expenseLongClickListener != null) {
                            expenseLongClickListener.onLongClick(item, DoItCallback.EXPENSE_OBJECT);
                        }
                        return true;
                    }
                });
                TextView tvEName = (TextView) convertView.findViewById(R.id.expenseTVname);
                TextView tvESum = (TextView) convertView.findViewById(R.id.expenseTVsum);

                tvEName.setTypeface(null, Typeface.ITALIC);
                String text = ((Expense) item).getNote();
                Date d = item.getObject().getDate(Constants.c_DATE);
                String dStr = DateHelper.dateToFormatedStringDDMMYYYY(d);

                tvEName.setText(dStr + " "  + ( text != null? text : ""));
                tvESum.setText(context.getString(R.string.nis) + " " + String.valueOf(item.getSum()));
                break;
        }
        convertView.setOnClickListener(listener);
        convertView.setTag(position);
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        ExpandableItem item = getItem(position);
        if(item != null) {
            return ExpandableItem.typeToInt(item.getType());
        } else {
            return -1;
        }
    }


    @Override
    public int getViewTypeCount() {
        return 3;
    }


    public void setOnClickListener(View.OnClickListener listener){
        this.listener = listener;
    }

    private LongClickCallback expenseLongClickListener;
    public void setOnExpenseLongClickedListener(LongClickCallback listener) {
        this.expenseLongClickListener = listener;
    }
}
