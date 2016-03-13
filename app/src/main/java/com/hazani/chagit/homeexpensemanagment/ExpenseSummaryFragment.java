package com.hazani.chagit.homeexpensemanagment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hazani.chagit.homeexpensemanagment.callbacks.CategoriesFetchCallback;
import com.hazani.chagit.homeexpensemanagment.callbacks.DoItCallback;
import com.hazani.chagit.homeexpensemanagment.callbacks.LongClickCallback;
import com.hazani.chagit.homeexpensemanagment.callbacks.UserChangedCallback;
import com.hazani.chagit.homeexpensemanagment.callbacks.UserOkToDeleteItemCallback;
import com.hazani.chagit.homeexpensemanagment.dialogs.CustomAlertDialog;
import com.hazani.chagit.homeexpensemanagment.dialogs.SimpleErrorDialog;
import com.parse.ParseException;
import com.parse.ParseObject;
import java.util.Date;
import java.util.List;

public class ExpenseSummaryFragment extends Fragment implements CategoriesFetchCallback,
                                                                CustomDatePickerFragment.DateSelectionCallback,
                                                                SwipeRefreshLayout.OnRefreshListener ,LongClickCallback, UserChangedCallback {

    public ExpenseSummaryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nis = getString(R.string.nis);
        mAdapter = new ExpandableBudgetStateListAdapter(getContext(), this.date);
    }

    ExpandableBudgetStateListAdapter mAdapter;
    ExpandableMultiLevelListView mlvDataTable;
    SwipeRefreshLayout swipeR;
    private TextView tvDate, tvTotalExpenses, tvTotalIncomes, tvBalance, tvBalanceIncludingDebts;
    int expenseSum = 0;
    int incomeSum = 0;
    int debtsSum = 0;
    String nis = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_expense_summary, container, false);
        mlvDataTable = (ExpandableMultiLevelListView) rootView.findViewById(R.id.expenseSummaryListView);
        mlvDataTable.setAdapter(mAdapter);
        mlvDataTable.refreshDrawableState();
        mAdapter.setOnClickListener(mlvDataTable);
        mAdapter.setOnExpenseLongClickedListener(this);
        tvDate = (TextView) rootView.findViewById(R.id.expenseSummaryTVDate);
        updateDate(this.date, false);
        ImageButton bSelectDate = (ImageButton) rootView.findViewById(R.id.expenseSummaryBChooseDate);
        bSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSelectDateDialog();
            }
        });
        swipeR = (SwipeRefreshLayout) rootView.findViewById(R.id.expenseSummary_swipe_refresh_layout);
        swipeR.setOnRefreshListener(this);

        tvTotalExpenses = (TextView) rootView.findViewById(R.id.expenseSummaryTvTotalExpenses);
        tvTotalIncomes = (TextView) rootView.findViewById(R.id.expenseSummaryTvTotalIncomes);
        tvBalance = (TextView) rootView.findViewById(R.id.expenseSummaryTvMonthlyalance);
        tvBalanceIncludingDebts = (TextView) rootView.findViewById(R.id.expenseSummaryTvMonthlyalanceIncludingDebts);
        tvTotalExpenses.setText(getString(R.string.summary_total_expenses) + " " + nis + String.valueOf(expenseSum));
        tvTotalIncomes.setText(getString(R.string.summary_total_incomes) + " " + nis + String.valueOf(incomeSum));
        tvBalance.setText(getString(R.string.summary_total_balance) + " " + nis + String.valueOf(incomeSum - expenseSum));
        tvBalanceIncludingDebts.setText(getString(R.string.summary_total_balance_include_debts) + " " + nis + String.valueOf(incomeSum - expenseSum - debtsSum));

        return rootView;
    }

    @Override
    public void dateSelected(Date selectedDate) {
        updateDate(selectedDate, true);
    }

    private Date date = new Date();

    private void updateDate(Date selectedDate, boolean fetchExpenses) {
        this.date = selectedDate;
        tvDate.setText(DateHelper.dateToFormatedStringMMYYYY(this.date));
        if(fetchExpenses) {
            if (ParseHelper.categories != null) {
                fetchExpensesByDate();
            }
        }
    }

    int counter = 0;
    private void fetchExpensesByDate(){
        new AsyncTask() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if(swipeR != null) swipeR.setRefreshing(true);
                mAdapter.removeAllItems();
                mAdapter.notifyDataSetChanged();
            }

            @Override
            protected Object doInBackground(Object[] params) {
                counter++;
                if (ParseHelper.categories != null) {
                    try {ParseHelper.fetchAllExpensesByDate(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return e.getMessage();
                    }

                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if(getActivity() != null && isAdded()) {
                    if (o == null) { // o == null means success
                        resetBalancePanelInBackground();
                        resetList();
                        if (swipeR != null) swipeR.setRefreshing(false);
                    } else {
                        if (swipeR != null) swipeR.setRefreshing(false);
                        SimpleErrorDialog.createDialog(getContext(), getString(R.string.error), getString(R.string.error_problem_with_fetching_data) + o.toString(), -1).show();
                    }
                }
            }
        }.execute();

    }

    private void openSelectDateDialog() {
        CustomDatePickerFragment datePickerDialog = CustomDatePickerFragment.getInstance(this,true, this.date);
        datePickerDialog.show(getFragmentManager(), "datePicker");
    }

    @Override
    public void categoriesFetched(boolean success) {
        if(success) {
            if (ParseHelper.categories != null) {
                fetchExpensesByDate();
            }
        }
    }

    private void resetList(){
        mAdapter.removeAllItems();
        mAdapter.addAllItems(ParseHelper.categories);
        mAdapter.notifyDataSetChanged();
        mlvDataTable.invalidate();
    }

    //private static boolean firstTime = true;
    /*
    Used to run code as late as possible, just before use of user, so will give enoghe time for all data to be fetched.
     */
//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        if(firstTime) {
//            if (isVisibleToUser) {
//                updateFragment(false);
//            }
//            firstTime = false;
//        }
//    }

    @Override
    public void onRefresh() {
        updateFragment(true);
    }

    private void updateFragment(final boolean reloadCategories){
        try {
            ParseHelper.fetchCategoriesInBackThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetBalancePanelInBackground(){
        new AsyncTask() {


            @Override
            protected Object doInBackground(Object[] params) {
                //get total expenses for month
                expenseSum = 0;
                for(Category cat : ParseHelper.categories){
                    if(cat.children != null){
                        for(ExpandableItem budgetLine : cat.children){
                            if(budgetLine.children != null){
                                for(ExpandableItem expense : budgetLine.children){
                                    expenseSum += expense.getSum();
                                }
                            }
                        }
                    }
                }

                //get total incomes for month
                try {
                    incomeSum = 0;
                    List<ParseObject> monthlyIncomes = ParseHelper.fetchMonthlyIncomesNow(ParseHelper.getAccount(), ExpenseSummaryFragment.this.date);
                    if (monthlyIncomes != null) {
                        for (ParseObject income : monthlyIncomes) {
                            incomeSum += income.getInt(Constants.c_SUM);
                        }
                    }
                    // get total debts payments
                    debtsSum = 0;
                    List<ParseObject> monthlyDebts = ParseHelper.fetchMonthlyDebtsNow(ParseHelper.getAccount(), ExpenseSummaryFragment.this.date);
                    if (monthlyDebts != null) {
                        for (ParseObject debt : monthlyDebts) {
                            debtsSum += debt.getInt(Constants.c_SUM);
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    incomeSum = 0;
                    debtsSum = 0;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                if(getActivity() != null && isAdded()) {
                    tvTotalExpenses.setText(getString(R.string.summary_total_expenses) + " " + nis + String.valueOf(expenseSum));
                    tvTotalIncomes.setText(getString(R.string.summary_total_incomes) + " " + nis + String.valueOf(incomeSum));
                    tvBalance.setText(getString(R.string.summary_total_balance) + " " + nis + String.valueOf(incomeSum - expenseSum));
                    if (debtsSum > 0) {
                        tvBalanceIncludingDebts.setVisibility(View.VISIBLE);
                        tvBalanceIncludingDebts.setText(getString(R.string.summary_total_balance_include_debts) + " " + nis + String.valueOf(incomeSum - expenseSum - debtsSum));
                    } else {
                        tvBalanceIncludingDebts.setVisibility(View.GONE);
                    }
                }
            }
        }.execute();
    }


//    /*
//    will be called when user ok'ed to delete the Expense
//     */
//    @Override
//    public void onClick(View v) {
//        //check tag to know what to do
//        if(v.getTag() == GENERAL_OK){
//            //delete
//            if (this.longClickedType == DoItCallback.EXPENSE_OBJECT) {
//                deleteExpense(false);
//            }
//        } else if (v.getTag() == SPECIFIC_PART_OF_PAYMENTS_OK){
//            Log.i("%%%%%%%%%%%%%%%%%%%","STANDING_PAYMENT_OK!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!GOOD!!!!!!!!!");
//            deleteExpense(true);
//
//        }
//
//
//    }

    public static String GENERAL_OK = "general ok";
    public static String SPECIFIC_PART_OF_PAYMENTS_OK = "payments ok";

    /*
    options:
    1. complex expense -> options: 1. all 2. from now and on
    2. simple Expense - > get ok and delete.

    in both options i have to notify the data that it was changed
     */
    private void deleteExpense(final Expense expense) {
        //final Expense expense = (Expense)this.longClickedObj;
        if(expense.isPayment()){ // a complex expense, ask user what he wants to do exactly
            CustomAlertDialog dialog = CustomAlertDialog.newInstance(getString(R.string.erase_expense), expense.getName() + " " + getString(R.string.expense_is_part_of_payments), getString(R.string.delete_all_payments),getString(R.string.delete_payments_from_now_on), true, new UserOkToDeleteItemCallback() {
                        @Override
                        public void deleteAll() {
                            expense.deleteSelfAndAllDescendants(null);
                            updateFragment(true);
//                            try {
//                                ParseHelper.fetchAllExpensesByDate(ParseHelper.getCategories(), date , ExpenseSummaryFragment.this);
//                            } catch (ParseException e) {
//                                e.printStackTrace();
//                            }
                        }

                        @Override
                        public void deleteFromNowOn() {
                            expense.deleteSelfAndAllDescendants(date);
                            updateFragment(true);
//                            try {
//                                ParseHelper.fetchAllExpensesByDate(ParseHelper.getCategories(), date , ExpenseSummaryFragment.this);
//                            } catch (ParseException e) {
//                                e.printStackTrace();
//                            }
                        }
                    });
                    dialog.show(getFragmentManager(), "alert");
        } else { //a simple expense - just make sure it is ok
            String msg = expense.getNote();
            if(expense.getNote() != null && expense.getNote().length() > 0){
                msg = getString(R.string.are_you_sure_to_delete) + " " + expense.getNote();
            } else {
                Date date = expense.getObject().getDate(Constants.c_DATE);
                msg = getString(R.string.are_you_sure_to_delete_expense_from_date) + " " + DateHelper.dateToFormatedStringDDMMYYYY(date);
            }
            CustomAlertDialog dialog = CustomAlertDialog.newInstance(getString(R.string.erase_expense), msg, getString(R.string.erase),null, true, new UserOkToDeleteItemCallback() {
                @Override
                public void deleteAll() {
                    expense.delete();
                    updateFragment(true);
                }

                @Override
                public void deleteFromNowOn() {
                    // will not be used now
                }
            });
            dialog.show(getFragmentManager(), "alert");

        }
    }

    private Object longClickedObj = null;
    private int longClickedType = -1;

    @Override
    public void onLongClick(Object object, int type) {
        this.longClickedObj = object;
        this.longClickedType = type;
        if(type == DoItCallback.EXPENSE_OBJECT) {
            Expense expense = (Expense)object;
            deleteExpense(expense);
        }
    }

    @Override
    public void userChanged() {
        if(getActivity() == null || !isAdded()) return;
        if (swipeR != null) swipeR.setRefreshing(true);
        mAdapter.removeAllItems();
        mAdapter.notifyDataSetChanged();

    }
}
