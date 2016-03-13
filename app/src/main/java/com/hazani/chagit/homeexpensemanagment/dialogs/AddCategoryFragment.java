package com.hazani.chagit.homeexpensemanagment.dialogs;


import android.support.v4.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hazani.chagit.homeexpensemanagment.BudgetLine;
import com.hazani.chagit.homeexpensemanagment.Category;
import com.hazani.chagit.homeexpensemanagment.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddCategoryFragment extends DialogFragment {

    public static int UPDATE_CATEGORY_TAG = 7;
    public static int ADD_CATEGORY_TAG = 9;
    public static int ADD_BUDGET_TAG = 8;
    public static int UPDATE_BUDGET_TAG = 33;
    public static int DELETE_BUDGET_TAG = 44;

    public interface OnDialogClickListener{
        void okClicked(int categoryOrBudgetTag, String newCategoryName, String sum, Object obj);
        void cancelClicked();
    }

    private OnDialogClickListener listener;
    private int categoryOrBudgetTag;
    private Category parentCategory = null;
    private BudgetLine budgetline = null;
    public AddCategoryFragment() {}

    public static AddCategoryFragment newInstance(OnDialogClickListener listener, int addWhatTag, Category parentCategory) {
        AddCategoryFragment fragment = new AddCategoryFragment();
        fragment.listener = listener;
        fragment.categoryOrBudgetTag = addWhatTag;
        fragment.parentCategory = parentCategory;
        return fragment;
    }

    public static AddCategoryFragment newInstance(OnDialogClickListener listener, int addWhatTag, BudgetLine budgetline) {
        AddCategoryFragment fragment = new AddCategoryFragment();
        fragment.listener = listener;
        fragment.categoryOrBudgetTag = addWhatTag;
        fragment.budgetline = budgetline;
        return fragment;
    }

    EditText mETnameOfCategoryOrBudgetLine, mETbudgetSum;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_add_category, container, false);
        Button bSave = (Button) rootView.findViewById(R.id.addCategoryBsave);
        Button bCancel = (Button) rootView.findViewById(R.id.addCategoryBcancel);
        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null){
                    String sum = null;
                    Object obj = parentCategory;
                    if(categoryOrBudgetTag == ADD_BUDGET_TAG){
                        sum = mETbudgetSum.getText().toString();
                    } else if (categoryOrBudgetTag == UPDATE_CATEGORY_TAG){
                        sum = mETbudgetSum.getText().toString();
                    }else if (categoryOrBudgetTag == UPDATE_BUDGET_TAG){
                        sum = mETbudgetSum.getText().toString();
                        obj = budgetline;
                    }
                    listener.okClicked(categoryOrBudgetTag, mETnameOfCategoryOrBudgetLine.getText().toString(), sum, obj);
                }
                AddCategoryFragment.this.dismiss();
            }
        });
        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null){
                    listener.cancelClicked();
                }

                AddCategoryFragment.this.dismiss();
            }
        });
        mETnameOfCategoryOrBudgetLine = (EditText) rootView.findViewById(R.id.addCategoryETname);
        mETbudgetSum = (EditText) rootView.findViewById(R.id.addCategoryETsum);

        if(this.categoryOrBudgetTag == ADD_BUDGET_TAG ){
            mETbudgetSum.setHint(getString(R.string.new_budget_estimated_sum));
            mETnameOfCategoryOrBudgetLine.setHint(getString(R.string.new_budget_name));
        } else if(this.categoryOrBudgetTag == UPDATE_BUDGET_TAG) {
            mETbudgetSum.setText(String.valueOf(budgetline.getSum()));
            mETnameOfCategoryOrBudgetLine.setText(budgetline.getName());
        } else {
            mETbudgetSum.setVisibility(View.GONE);
            mETnameOfCategoryOrBudgetLine.setHint(getString(R.string.add_category_item));
        }

        TextView titleTV = (TextView)getDialog().findViewById(android.R.id.title);
        String title =  "";
        if(this.categoryOrBudgetTag == ADD_CATEGORY_TAG){
            title = getString(R.string.add_category_item);
        } else if( this.categoryOrBudgetTag == ADD_BUDGET_TAG ){
            if(this.parentCategory != null){
                title = getString(R.string.new_budget_added_to) + parentCategory.getName();
            }
        } else if (this.categoryOrBudgetTag == UPDATE_CATEGORY_TAG){
            if(this.parentCategory != null) {
                title = getString(R.string.update_item) + " " + parentCategory.getName();
                mETnameOfCategoryOrBudgetLine.setText(parentCategory.getName());
            }
        }else if (this.categoryOrBudgetTag == UPDATE_BUDGET_TAG){
            if(this.budgetline != null) {
                title = getString(R.string.update_item) + " " + budgetline.getName();
            }
        }

        if(titleTV != null) {
            titleTV.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            titleTV.setText(title);
        } else {
            getDialog().setTitle(title);
        }
        return rootView;
    }

}
