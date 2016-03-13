package com.hazani.chagit.homeexpensemanagment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.hazani.chagit.homeexpensemanagment.callbacks.DoItCallback;
import com.hazani.chagit.homeexpensemanagment.callbacks.LongClickCallback;
import com.hazani.chagit.homeexpensemanagment.callbacks.UserChangedCallback;
import com.hazani.chagit.homeexpensemanagment.callbacks.UserOkToDeleteItemCallback;
import com.hazani.chagit.homeexpensemanagment.dialogs.CustomAlertDialog;
import com.hazani.chagit.homeexpensemanagment.dialogs.SimpleErrorDialog;
import com.parse.ParseObject;

import java.util.Date;
import java.util.List;


public class DebtsFragment extends Fragment implements Debt.DebtsFetchedCallback, Debt.DebtSavedCallback, CustomDatePickerFragment.DateSelectionCallback, LongClickCallback, UserChangedCallback {


    public DebtsFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new DebtsListAdapter(getContext());
        ParseHelper.fetchParentDebts(ParseHelper.getAccount(), this);
    }

    ListView lvDebts;
    DebtsListAdapter adapter;
    EditText etCreditorName, etOriginalSum, etInterestRate, etMonthlyPaymentSum, etPaymentsLeft;
    TextView tvAllTheRemainingDebtsSum;
    Button bSave, bCancel;
    TextView tvDate;
    //boolean saveItFromScratch = true;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_debts, container, false);

        etCreditorName = (EditText) rootView.findViewById(R.id.debtsETcreditorName);
        etOriginalSum = (EditText) rootView.findViewById(R.id.debtsETsum);
        etInterestRate = (EditText) rootView.findViewById(R.id.debtsETinterestRate);
        etMonthlyPaymentSum = (EditText) rootView.findViewById(R.id.debtsETsumOfMonthlyPayment);
        etPaymentsLeft = (EditText) rootView.findViewById(R.id.debtsETnumOfMonthlyPayments);
        bSave = (Button) rootView.findViewById(R.id.debtsBupdateDebt);
        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDebt();
            }
        });
        bCancel= (Button) rootView.findViewById(R.id.debtsBcancelUpdate);
        bCancel.setVisibility(View.GONE); //will GONE make it not remeber the click listener?
        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                debtSaved(debtToUpdate);
                debtToUpdate = null;
                clearScreen();
            }
        });
        ImageButton bSelectDate = (ImageButton) rootView.findViewById(R.id.debtsBselectDate);
        bSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSelectDateDialog();
            }
        });
        tvDate = (TextView) rootView.findViewById(R.id.debtsTVdate);
        this.updateDate(this.date);
        lvDebts = (ListView) rootView.findViewById(R.id.debtsListView);
        lvDebts.setAdapter(adapter);
        adapter.setLongClickCallback(this);
        tvAllTheRemainingDebtsSum = (TextView) rootView.findViewById(R.id.debtsTVsumaryCalculated);
        return rootView;
    }

    private void saveDebt() {
            String creditorName = etCreditorName.getText().toString();
            int originalSum = (etOriginalSum.getText().toString().equalsIgnoreCase("")) ? 0 : Integer.valueOf(etOriginalSum.getText().toString());
            if (creditorName.equalsIgnoreCase("") || originalSum == 0) {
                SimpleErrorDialog.createDialog(getContext(), getString(R.string.error), getString(R.string.missing_fields),-1).show();
            }
            Double interestRate = (etInterestRate.getText().toString().equalsIgnoreCase("")) ? 0 : Double.valueOf(etInterestRate.getText().toString());
            int monthlyPayment = (etMonthlyPaymentSum.getText().toString().equalsIgnoreCase("")) ? 0 : (Integer.valueOf(etMonthlyPaymentSum.getText().toString()));
            int paymentsLeft = (etPaymentsLeft.getText().toString().equalsIgnoreCase("")) ? 0 : Integer.valueOf(etPaymentsLeft.getText().toString());
            Date startPayOnDate = this.date;
        if(debtToUpdate == null) {
            new Debt(creditorName, originalSum, interestRate, paymentsLeft, monthlyPayment, ParseHelper.getAccount(), null, startPayOnDate, null, this);
        } else {
            debtToUpdate.update(creditorName, originalSum, interestRate, paymentsLeft, monthlyPayment, startPayOnDate, this);
        }
        debtToUpdate = null;
    }


    @Override
    public void debtSaved(Debt debt) {
        clearScreen();
        adapter.addDebt(debt);
        adapter.notifyDataSetChanged();
        this.totalDebtsCalculated += debt.getLeftoverSum();
        setAllTheRemainingDebtsSum(totalDebtsCalculated);
    }

    private int totalDebtsCalculated = 0;
    @Override
    public void debtsFetched(List<ParseObject> debtObjects) {
        totalDebtsCalculated = 0;
        if(adapter != null) {
            for (ParseObject debtObject : debtObjects) {
                Debt debt = new Debt(debtObject);
                adapter.addDebt(debt);
                totalDebtsCalculated += debt.getLeftoverSum();
            }
            adapter.notifyDataSetChanged();
            setFragmentEnabled(true);
            setAllTheRemainingDebtsSum(totalDebtsCalculated);
        }
    }

    private void setAllTheRemainingDebtsSum(int total){
        tvAllTheRemainingDebtsSum.setText(getString(R.string.debts_total_calculated) + " " + getString(R.string.nis) + String.valueOf(total));
    }

    private void clearScreen(){
        etCreditorName.setText("");
        etOriginalSum.setText("");
        etInterestRate.setText("");
        etMonthlyPaymentSum.setText("");
        etPaymentsLeft.setText("");
        bSave.setText(getString(R.string.save));
        bCancel.setVisibility(View.GONE);
        updateDate(new Date());
    }

    private Debt debtToUpdate = null;
    private void setScreenToUpdateDebt(Debt debt) {
        //remove from list
        adapter.removeDebt(debt);
        adapter.notifyDataSetChanged();
        // prepare panel for updating
        this.debtToUpdate = debt;
        etCreditorName.setText(debt.getCreditorName());
        int leftover = debt.getLeftoverSum();
        etOriginalSum.setText(Integer.toString(leftover));
        etInterestRate.setText(Double.toString(debt.getInterestRate()));
        etMonthlyPaymentSum.setText(Integer.toString(debt.getSumOfMonthlyPayment()));
        etPaymentsLeft.setText(Integer.toString(debt.getNumOfPayments()));
        bSave.setText(getString(R.string.update));
        bCancel.setVisibility(View.VISIBLE);
        updateDate(new Date());
        setAllTheRemainingDebtsSum(totalDebtsCalculated -=  leftover);
    }

    @Override
    public void dateSelected(Date selectedDate) {
        updateDate(selectedDate);
    }

    private Date date = new Date();
    private void updateDate(Date selectedDate) {
        this.date = selectedDate;
        tvDate.setText(DateHelper.dateToFormatedStringDDMMYYYY(this.date));
    }

    private void openSelectDateDialog() {
        CustomDatePickerFragment datePickerDialog = CustomDatePickerFragment.getInstance(this,false, this.date);
        datePickerDialog.show(getFragmentManager(), "datePicker");
    }

    @Override
    public void onLongClick(Object object, int type) {
        if(type == DoItCallback.DEBT_OBJECT){
            if(object != null){
                Debt debt = (Debt)object;
                Log.i("##########","got here! DEBT = " + debt.getCreditorName() );
                openDialogWithDebtMenu(debt);
            }
        }
    }

    private void openDialogWithDebtMenu(final Debt debt) {
        final String TAG_1 = "debt";
        String title = getString(R.string.debt_possibilities) + ": " + debt.getCreditorName();
        String[] items = {title,TAG_1, getString(R.string.delete_item),getString(R.string.update_item)};
        PopupMenuFragment dialog = PopupMenuFragment.newInstance(getContext(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = (String)v.getTag();
                if (tag.equalsIgnoreCase(TAG_1+getString(R.string.delete_item))){
                    deleteDebt(debt);
                } else if (tag.equalsIgnoreCase(TAG_1+getString(R.string.update_item))){
                    setScreenToUpdateDebt(debt);
                }

            }
        }, items);
        dialog.show(getFragmentManager(), "debtMenu");
    }



    private void deleteDebt(final Debt debt){
        adapter.removeDebt(debt);
        adapter.notifyDataSetChanged();
        setAllTheRemainingDebtsSum(totalDebtsCalculated -=  debt.getLeftoverSum());
        debt.delete();
    }


    @Override
    public void userChanged() {
        setFragmentEnabled(false);
        if (adapter != null) {
            adapter.removeAllItems();
            adapter.notifyDataSetChanged();
        }
        ParseHelper.fetchParentDebts(ParseHelper.getAccount(), this);
    }

    private void setFragmentEnabled(boolean enabled){
        if (bSave != null) bSave.setEnabled(enabled);
    }
}
