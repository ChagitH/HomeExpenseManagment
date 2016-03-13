package com.hazani.chagit.homeexpensemanagment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class CustomDatePickerFragment extends DialogFragment {
    interface DateSelectionCallback{
        public void dateSelected(Date selectedDate);
    }
    public CustomDatePickerFragment(){ }

    private static String PARAM1_LISTENER = "listener";
    private static String PARAM2_MONTH_YEAR = "monthyearonly";
    private static String PARAM3_DATE = "datetoset";

    private DateSelectionCallback listener;
    private boolean mMonthYearOnly;
    private Date mDate = new Date();

    public static CustomDatePickerFragment getInstance(DateSelectionCallback listener, boolean monthYearOnly, Date dateToSet){
        CustomDatePickerFragment frag = new CustomDatePickerFragment();
        frag.listener = listener;
        frag.mMonthYearOnly = monthYearOnly;
        frag.mDate = dateToSet;
        return  frag;
    }

    private void setPickerWithArgs(){
        setMonthYearOnlyPicker(this.mMonthYearOnly);
        setSelectedDate(this.mDate);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private NumberPicker dayPicker , monthPicker ,yearPicker;
    private ArrayList yearsArray;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_custom_date_picker, container, false);

        dayPicker = (NumberPicker) rootView.findViewById(R.id.datePickerDaysPicker);
        monthPicker = (NumberPicker) rootView.findViewById(R.id.datePickerMonthsPicker);
        yearPicker = (NumberPicker) rootView.findViewById(R.id.datePickerYearsPicker);

        String[] days = {"1","2","3","4","5","6"};
        dayPicker.setDisplayedValues(days);
        dayPicker.setMinValue(0);
        dayPicker.setMaxValue(days.length - 1);
        dayPicker.setWrapSelectorWheel(true);
        monthPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        String[] months = getResources().getStringArray(R.array.months);
        monthPicker.setDisplayedValues(months);
        monthPicker.setMinValue(0);
        int maxMonths = 0;
        if (months.length > 1){
            maxMonths =  months.length -1;
        }
        monthPicker.setMaxValue(maxMonths);
        monthPicker.setWrapSelectorWheel(true);
        monthPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        monthPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                setDaysByMonthAndYear(picker, newVal);
            }
        });

        String [] years = getResources().getStringArray(R.array.years);
        yearsArray = new ArrayList<>(Arrays.asList(years));

        yearPicker.setDisplayedValues(years);
        yearPicker.setMinValue(0);
        yearPicker.setMaxValue(years.length - 1);
        yearPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        yearPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                setDaysByMonthAndYear(picker, newVal);
            }
        });

        setPickerWithArgs();

        Button bChoose = (Button) rootView.findViewById(R.id.datePickerBchoose);
        bChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.dateSelected(getSelectedDate());
                }
                dismiss();
            }
        });
        Button bCancel = (Button) rootView.findViewById(R.id.datePickerBcancel);
        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        TextView title = (TextView)getDialog().findViewById(android.R.id.title);
        if(title != null) {
            //title.setTextColor(android.R.color.black);
            title.setTextColor(getResources().getColor(android.R.color.black));
            title.setText(getString(R.string.choose_date));
        } else {
            getDialog().setTitle(getString(R.string.choose_date));
        }



        return rootView;
    }

    private void setDaysByMonthAndYear(NumberPicker picker, int newVal){
        int monthIndex = 0, yearIndex = 0;
        if (picker == monthPicker){
            monthIndex = newVal;
            yearIndex = yearPicker.getValue();
        } else if (picker == yearPicker){
            yearIndex = newVal;
            monthIndex = monthPicker.getValue();
        }
        int month = monthIndex+1;
        int year = getYearFromYearIndex(yearIndex);
        setDaysByMonthAndYear(year,month);
    }

    private void setDaysByMonthAndYear(int year, int month) {
        Calendar calendar = new GregorianCalendar(year, month, 0);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        // java 8
//        java.time.YearMonth yearMonthObject = YearMonth.of(1999, 2);
//        int daysInMonth = yearMonthObject.lengthOfMonth(); //28
        if(daysInMonth <= 0){
            return;
        }
        ArrayList<String> daysArray = new ArrayList<>();
        for(int i = 1 ; i <= daysInMonth ; i++){
            daysArray.add(String.valueOf(i));
        }
        String[] days = new String[daysArray.size()];
        dayPicker.setDisplayedValues(null);
        dayPicker.setMaxValue(daysArray.size() - 1);
        dayPicker.setMinValue(0);
        dayPicker.setDisplayedValues(daysArray.toArray(days));
    }

    public void setSelectedDate(Date selectedDate){
        Log.i("XXXXXXXXXXXX", "in setSelectedDate() date = " + selectedDate);
        if(selectedDate == null){
            return;
        }
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(selectedDate);

        //setting year
        int year = calendar.get(Calendar.YEAR);
        Log.i("XXXXXXXXXXXX", "in setSelectedDate() year = " + year);
        int yearIndex = yearsArray.indexOf(String.valueOf(year));
        if (yearIndex < 0){ // invalid year for this DatePicker
            return;
        }
        yearPicker.setValue(yearIndex);

        //setting month
        int month = calendar.get(Calendar.MONTH);
        Log.i("XXXXXXXXXXXX", "in setSelectedDate() month = " + month);
        monthPicker.setValue(month); //-1

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        Log.i("XXXXXXXXXXXX", "in setSelectedDate() day = " + day);
        setDaysByMonthAndYear(year, month);
        dayPicker.setValue(day - 1);
    }

    public Date getSelectedDate(){
        int yearIndex = yearPicker.getValue();
        int monthIndex = monthPicker.getValue();
        int dayIndex = dayPicker.getValue();

        int year = getYearFromYearIndex(yearIndex);
        int month = monthIndex;//+1;
        int day = dayIndex+1;

        Log.i("XXXXXXXXXXX", "SELECTED DATE = " + day + "/" + month + "/" + year );
        //return DateHelper.dateFromComponents(year,month,day);
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        //Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(year,month,day,0,0,0);
        Date d = calendar.getTime();
        Log.i("XXXXXXXXXXX", "DATE = " + d );
        return d;
    }

    private int getYearFromYearIndex(int yearIndex){
        if (yearIndex < 0 || yearIndex > yearsArray.size()-1){ // invalid year for this DatePicker
            return -1;
        }
        String yearStr = (String)yearsArray.get(yearIndex);
        return Integer.parseInt(yearStr);
    }

    public void setMonthYearOnlyPicker(boolean monthYearOnly){
        if(mMonthYearOnly){
            dayPicker.setVisibility(View.GONE);
            yearPicker.setOnValueChangedListener(null);
            monthPicker.setOnValueChangedListener(null);
        }
    }

}

