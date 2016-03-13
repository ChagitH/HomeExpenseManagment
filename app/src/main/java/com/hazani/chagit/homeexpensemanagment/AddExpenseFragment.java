package com.hazani.chagit.homeexpensemanagment;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.hazani.chagit.homeexpensemanagment.callbacks.CategoriesFetchCallback;
import com.hazani.chagit.homeexpensemanagment.callbacks.UserChangedCallback;
import com.hazani.chagit.homeexpensemanagment.dialogs.SimpleErrorDialog;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;


public class AddExpenseFragment extends Fragment implements SaveToDataBaseCallback,
        CategoriesFetchCallback, CustomDatePickerFragment.DateSelectionCallback , UserChangedCallback{

    //public static final int LOGIN_ACTIVITY_TAG = 1;

    EditText etSum, etNumPayments, etNote;
    CheckBox cbStandingPayment;
    Button bAddExpense, bLoginOrRegister;
    ImageButton bSelectDate;
    TextView tvHelloUser, tvDate;
    ArrayAdapter<ExpandableItem> categorySpinnerAdapter;
    ArrayAdapter<ExpandableItem> budgetlineSpinnerAdapter;
    Spinner spCat;
    Spinner spBud;

    private int sum = 0;
    private boolean standingPayment = false;
    private int numOfPayments = 0;
    private String note;
    private BudgetLine budgetLine;
    private Date date = new Date();

    public AddExpenseFragment() {
        // Required empty public constructor
    }

    private ArrayList<Category> categories;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        categorySpinnerAdapter = new ArrayAdapter<ExpandableItem>(getContext(), android.R.layout.simple_spinner_item);
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        budgetlineSpinnerAdapter = new ArrayAdapter<ExpandableItem>(getContext(), android.R.layout.simple_spinner_item);
        budgetlineSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_add_expense, container, false);
        etSum = (EditText) rootView.findViewById(R.id.addExpenseEtSum);
        etSum.requestFocus();
        etNumPayments = (EditText) rootView.findViewById(R.id.addExpenseEtNumOfPayments);
        etNote = (EditText) rootView.findViewById(R.id.addExpenseEtNote);
        tvDate = (TextView) rootView.findViewById(R.id.addExpenseTVDate);
        tvDate.setText(DateHelper.dateToFormatedStringDDMMYYYY(DateHelper.getToday()));
        cbStandingPayment = (CheckBox) rootView.findViewById(R.id.addExpenseCbIsStandingPayment);
        cbStandingPayment.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                etNumPayments.setEnabled(!isChecked);
                etNumPayments.setInputType(isChecked ? InputType.TYPE_NULL : InputType.TYPE_CLASS_NUMBER);
                if(isChecked){
                    etNumPayments.setText("");
                }
            }
        });
        bSelectDate = (ImageButton) rootView.findViewById(R.id.addExpenseBChooseDate);
        bSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSelectDateDialog();
            }
        });
        bAddExpense = (Button) rootView.findViewById(R.id.bAddExpenseAddExpense);
        bAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveExpense();

            }
        });
        bLoginOrRegister = (Button) rootView.findViewById(R.id.addExpenseBLoginOrRegister);
        bLoginOrRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity()!= null) ((MainActivity)getActivity()).login(ParseHelper.getConnectedUser() == null);
            }
        });
        tvHelloUser = (TextView) rootView.findViewById(R.id.addExpenseTVwelcomeOrNotLoggedIn);


        spCat = (Spinner) rootView.findViewById(R.id.addExpenseSpinnerCat);
        spCat.setAdapter(categorySpinnerAdapter);
        spBud = (Spinner) rootView.findViewById(R.id.addExpenseSpinnerBudget);
        spBud.setAdapter(budgetlineSpinnerAdapter);

        spCat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Category category = (Category) categorySpinnerAdapter.getItem(position);
                if (category.children == null) {
                    budgetlineSpinnerAdapter.clear();
                    budgetlineSpinnerAdapter.notifyDataSetChanged();
                } else {
                    budgetlineSpinnerAdapter.clear();
                    budgetlineSpinnerAdapter.addAll(category.children);
                    budgetlineSpinnerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // what to do with budgetlines?
            }
        });
        if(ParseHelper.getConnectedUser() != null) setLoginArea();
        return rootView;
    }

    private void openSelectDateDialog() {
        CustomDatePickerFragment datePickerDialog = CustomDatePickerFragment.getInstance(this,false, this.date);
        datePickerDialog.show(getFragmentManager(), "datePicker");
    }

    private void setLoginArea(){
        String helloUser;
        String btnText;
        ParseUser user = ParseHelper.getConnectedUser();
        if (user != null){
            String name = user.getString(Constants.c_NAME);
            helloUser = getString(R.string.exlamation_mark) + name + " " +  getString(R.string.hello);
            btnText = getString(R.string.logout);
        } else {
            helloUser = getString(R.string.you_are_not_logged_in);
            btnText = getString(R.string.login);
        }
        tvHelloUser.setText(helloUser);
        bLoginOrRegister.setText(btnText);
    }

    @Override
    public void categoriesFetched(boolean success) {
        if(success) {
            setCategoriesAndBudgetLines();
            setFragmentEnabled(true);
        } else {
            setFragmentEnabled(false);
        }
    }

    private static int numOfCategories = 0;

    private void setCategoriesAndBudgetLines(){
        categories = ParseHelper.getCategories();
        if(categories != null && isAdded()) {
            numOfCategories = categories.size();
            categorySpinnerAdapter.clear();
            categorySpinnerAdapter.addAll(categories);
            categorySpinnerAdapter.notifyDataSetChanged();
            if (categorySpinnerAdapter.getCount() > 0) {
                spCat.setSelection(0, true);
                Category category = (Category) spCat.getSelectedItem();
                if (category.canOpen()) {
                    budgetlineSpinnerAdapter.clear();
                    budgetlineSpinnerAdapter.addAll(category.children);
                    budgetlineSpinnerAdapter.notifyDataSetChanged();
                    spBud.setSelection(0, true);
                }
            }
        }
    }


    private Date getDate(){
        return this.date;
    }

    private String gatherData(){
        String sumStr = etSum.getText().toString();
        this.sum = Integer.parseInt(sumStr.equalsIgnoreCase("") ? "0":sumStr);
        if(sum <=0 ){
            return getString(R.string.no_sum_entered);
        }
        Object selectedItem = spBud.getSelectedItem();
        if(selectedItem == null ){
            return getString(R.string.no_budget_selected);
        } else {
            this.budgetLine = (BudgetLine) selectedItem;
        }
        this.standingPayment = cbStandingPayment.isChecked();

        String numOfPaymentsStr = etNumPayments.getText().toString();
        this.numOfPayments = Integer.parseInt(numOfPaymentsStr.equalsIgnoreCase("") ? "0" : numOfPaymentsStr);
        this.note = etNote.getText().toString();
        this.date = getDate();
        return null;
    }

    private void saveExpense(){
        String error = gatherData();
        if( error != null){
            SimpleErrorDialog.createDialog(getContext(), getString(R.string.error), error , -1).show();
            return;
        }
        if(this.numOfPayments > 0){ // payments Expense
            this.sum = sum / numOfPayments;

        }
        try {
            Expense.addExpense(this.sum, this.numOfPayments, this.note, this.budgetLine, this.date, this.standingPayment, this);
        } catch (Exception e) {
            SimpleErrorDialog.createDialog(getContext(), getString(R.string.error), e.getMessage(), -1).show();
            e.printStackTrace();
        }

    }


    private void clearScreen() {
        etSum.setText("");
        etNumPayments.setText("");
        etNote.setText("");
        this.date = new Date();
        tvDate.setText(DateHelper.dateToFormatedStringDDMMYYYY(this.date));
        cbStandingPayment.setChecked(false);
        spCat.setSelection(0);
        //spBud.setSelection(0); // is supposed to set itself
    }

    @Override
    public void actionSucceeded() {
        clearScreen();
    }

    @Override
    public void actionFaild(Exception exception) {
        clearScreen();
    }

    @Override
    public void dateSelected(Date selectedDate) {
        if(selectedDate != null) {
            tvDate.setText(DateHelper.dateToFormatedStringDDMMYYYY(selectedDate));
            this.date = selectedDate;
        }
    }

    private void setFragmentEnabled(boolean enabled){
        bAddExpense.setEnabled(enabled);
        if(!enabled) {
            categorySpinnerAdapter.clear();
            budgetlineSpinnerAdapter.clear();
        }
    }

    @Override
    public void userChanged() {
        if(getActivity() == null || !isAdded()) return;
        setLoginArea();
        setFragmentEnabled(false);

    }
}

