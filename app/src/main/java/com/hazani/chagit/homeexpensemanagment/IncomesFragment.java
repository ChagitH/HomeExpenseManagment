package com.hazani.chagit.homeexpensemanagment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.hazani.chagit.homeexpensemanagment.R;
import com.hazani.chagit.homeexpensemanagment.callbacks.DoItCallback;
import com.hazani.chagit.homeexpensemanagment.callbacks.LongClickCallback;
import com.hazani.chagit.homeexpensemanagment.callbacks.UserChangedCallback;
import com.hazani.chagit.homeexpensemanagment.callbacks.UserOkToDeleteItemCallback;
import com.hazani.chagit.homeexpensemanagment.dialogs.CustomAlertDialog;
import com.hazani.chagit.homeexpensemanagment.dialogs.SimpleErrorDialog;
import com.parse.ParseException;
import com.parse.ParseObject;

import java.lang.Override;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IncomesFragment extends Fragment implements IncomesFetchCallback, Income.IncomeSavedCallback, CustomDatePickerFragment.DateSelectionCallback, SwipeRefreshLayout.OnRefreshListener, LongClickCallback, UserChangedCallback {


    public IncomesFragment() {
        // Required empty public constructor
    }

    Date month = new Date();
    EditText etSum, etName;
    TextView tvRoutineOrOnetime;
    CheckBox checkBox;
    boolean isRoutine = true;
    IncomesListAdapter adapter;
    private TextView tvDate;
    TextView tvTotleIncomsForMonth;
    SwipeRefreshLayout swipeR;
    Button bSaveIncome;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new IncomesListAdapter(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_incomes, container, false);
        etSum = (EditText) rootView.findViewById(R.id.incomesETsum);
        etName = (EditText) rootView.findViewById(R.id.incomesETname);
        tvRoutineOrOnetime = (TextView) rootView.findViewById(R.id.incomesTVincomeType);
        checkBox = (CheckBox) rootView.findViewById(R.id.incomesCbIsRutineIncome);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isRoutine = isChecked;
            }
        });
        bSaveIncome = (Button)rootView.findViewById(R.id.incomesBaddIncome);
        bSaveIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createIncome();
            }
        });
        tvDate = (TextView) rootView.findViewById(R.id.incomsTVDate);
        updateDate(this.date);
        ImageButton bSelectDate = (ImageButton) rootView.findViewById(R.id.incomsBChooseDate);
        bSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSelectDateDialog();
            }
        });

        ListView list = (ListView)rootView.findViewById(R.id.incomesListView);
        list.setAdapter(adapter);
        adapter.setLongClickCallback(this);
        tvTotleIncomsForMonth = (TextView) rootView.findViewById(R.id.incomsTVmonthlySummary);
        swipeR = (SwipeRefreshLayout) rootView.findViewById(R.id.incomes_swipe_refresh_layout);
        swipeR.setOnRefreshListener(this);
        etName.requestFocus();
        return rootView;

    }

    @Override
    public void dateSelected(Date selectedDate) {
        updateDate(selectedDate);
    }

    private Date date = new Date();
    private void updateDate(Date selectedDate) {
        this.date = selectedDate;
        tvDate.setText(DateHelper.dateToFormatedStringMMYYYY(this.date));
        refreshIncomes();
    }

    private void openSelectDateDialog() {
        CustomDatePickerFragment datePickerDialog = CustomDatePickerFragment.getInstance(this,true, this.date);
        datePickerDialog.show(getFragmentManager(), "datePicker");
    }

    private void clearScreen(){
        etSum.setText("");
        etName.setText("");
        checkBox.setChecked(true);
    }

    private void createIncome() {
        String errorMsg = "";
        String name = etName.getText().toString();
        if(name.equalsIgnoreCase("")){
            errorMsg += getString(R.string.no_name);
        }
        String sumStr = etSum.getText().toString();
        int sum = 0;
        if(sumStr.equalsIgnoreCase("")){
            errorMsg += " " + getString(R.string.no_sum);
        } else {
            sum = Integer.parseInt(sumStr);
        }
        if(errorMsg.length() <= 1) {
            Income.makeIncome(name, sum, ParseHelper.getAccount(), month, isRoutine, this);
            clearScreen();
        } else {
            SimpleErrorDialog.createDialog(getContext(),getString(R.string.error),errorMsg, -1).show();
        }
    }

    @Override
    public void incomeSaved(Income income) {
        adapter.addIncome(income);
        adapter.notifyDataSetChanged();
    }

    int totalSum = 0;
    @Override
    public void incomeFetched(List<ParseObject> parseObjects) {
        if(isAdded()) {
            if (parseObjects != null && adapter != null) {
                adapter.clearIncomes();
                totalSum = 0;
                for (ParseObject object : parseObjects) {
                    Income income = new Income(object);
                    adapter.addIncome(income);
                    totalSum += income.getSum();
                }

                adapter.notifyDataSetChanged();
                setTotleIncomsForMonthTextView();
            }
            setFragmentEnabled(true);
            if (swipeR != null) swipeR.setRefreshing(false);
        }
    }

    @Override //SwipeRefresh listener
    public void onRefresh() {
        refreshIncomes();
    }

    private void refreshIncomes() {
        if(swipeR != null) swipeR.setRefreshing(true);
        ParseHelper.fetchMonthlyIncomes(ParseHelper.getAccount(), this, this.date);
    }

    @Override
    public void onLongClick(Object object, int type) {
        if(type == DoItCallback.INCOME_OBJECT){
            if(object != null){
                Income income = (Income)object;
                deleteIncome(income);
            }
        }
    }

    private void deleteIncome(final Income income){
        final String title = getString(R.string.income_deletion);
        if(income.isRoutineIncome()){
            // ask if 1- delete from here on or 2- only this month income
            CustomAlertDialog dialog = CustomAlertDialog.newInstance(title, income.getName() + " " + getString(R.string.income_is_routine), getString(R.string.delete_income_this_month_only),getString(R.string.delete_income_from_now_on), true, new UserOkToDeleteItemCallback() {
                @Override
                public void deleteAll() { //e.g. delete this month only
                    removeIncomeFromAdapter(income);
                    income.delete();
                }

                @Override
                public void deleteFromNowOn() {
                    removeIncomeFromAdapter(income);
                    income.deleteFromThisMonthOn(date);
                }
            });
            dialog.show(getFragmentManager(), "alert");
        } else {
            // just delete after ok
            CustomAlertDialog dialog = CustomAlertDialog.newInstance(title, getString(R.string.are_you_sure_to_delete) + " " + income.getName(), getString(R.string.erase),null, true, new UserOkToDeleteItemCallback() {
                @Override
                public void deleteAll() {
                    // will be used as ok
                    removeIncomeFromAdapter(income);
                    income.delete();
                }

                @Override
                public void deleteFromNowOn() {
                    // will not be used now
                }
            });
            dialog.show(getFragmentManager(), "alert");
        }
    }

    private void removeIncomeFromAdapter(Income income){
        adapter.removeIncome(income);
        adapter.notifyDataSetChanged();
        //update botton text view
        totalSum -= income.getSum();
        setTotleIncomsForMonthTextView();
    }

    private void setTotleIncomsForMonthTextView(){
        tvTotleIncomsForMonth.setText(getString(R.string.income_incomes_for_this_month) + " " + getString(R.string.nis) + String.valueOf(totalSum));
    }
    @Override
    public void userChanged() {
        setFragmentEnabled(false);
        refreshIncomes();

    }

    private void setFragmentEnabled(boolean enabled){
        if(bSaveIncome != null) bSaveIncome.setEnabled(enabled);
    }
}
