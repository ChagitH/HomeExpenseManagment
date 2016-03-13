package com.hazani.chagit.homeexpensemanagment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.hazani.chagit.homeexpensemanagment.callbacks.UserChangedCallback;
import com.hazani.chagit.homeexpensemanagment.dialogs.SimpleErrorDialog;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        prepareTabs();

    }

    AddExpenseFragment aef;
    ExpenseSummaryFragment esf;
    BudgetFragment bf;
    IncomesFragment iif;
    DebtsFragment df;

    private void prepareTabs(){

        final int[] imagesSelected = {R.drawable.ic_clear, R.drawable.ic_cloudy, R.drawable.ic_fog, R.drawable.ic_snow, R.drawable.ic_storm};
        final int[] imagesNotSelected = {R.drawable.pen, R.drawable.status, R.drawable.money, R.drawable.battery, R.drawable.library};
        final String[] tabTitles = {getString(R.string.title_add_expense), getString(R.string.title_expense_summary), getString(R.string.title_budget), getString(R.string.title_incomes), getString(R.string.title_debts)};
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        aef = new AddExpenseFragment();
        esf = new ExpenseSummaryFragment();
        bf = new BudgetFragment();
        iif = new IncomesFragment();
        df = new DebtsFragment();

        adapter.addFragment(aef, getString(R.string.title_add_expense));
        adapter.addFragment(esf, getString(R.string.title_expense_summary));
        adapter.addFragment(bf, getString(R.string.title_budget));
        adapter.addFragment(iif, getString(R.string.title_incomes));
        adapter.addFragment(df, getString(R.string.title_debts));
        viewPager.setAdapter(adapter);


        tabLayout.setupWithViewPager(viewPager);
        /*
        setting texts and Images of tabs
         */
        int size = viewPager.getAdapter().getCount();
        for (int i = 0 ; i < size ; i++){
            Tab tab = tabLayout.getTabAt(i);
            SpannableString sb = new SpannableString(tabTitles[i]);
            sb.setSpan(new RelativeSizeSpan(0.6f), 0, sb.length(), 0);
            int iconID;
            if(i==0){ // setting first tab to look selected
                iconID = imagesSelected[i];
                sb.setSpan(new ForegroundColorSpan(Color.RED), 0, sb.length(), 0);
            } else {
                iconID = imagesNotSelected[i];
            }
            tab.setIcon(iconID);
            tab.setText(sb);
        }

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(), true);
                SpannableString sb = new SpannableString(tab.getText());
                sb.setSpan(new ForegroundColorSpan(Color.RED), 0, sb.length(), 0);
                tab.setText(sb);
                tab.setIcon(imagesSelected[tab.getPosition()]);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                SpannableString sb = new SpannableString(tab.getText());
                sb.setSpan(new ForegroundColorSpan(Color.GRAY), 0, sb.length(), 0);
                tab.setText(sb);
                tab.setIcon(imagesNotSelected[tab.getPosition()]);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(), true);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        addOnUserChangeListener(aef);
        addOnUserChangeListener(esf);
        addOnUserChangeListener(bf);
        addOnUserChangeListener(iif);
        addOnUserChangeListener(df);

        ParseHelper.addCategoryFetchListener(aef);
        ParseHelper.addCategoryFetchListener(esf);
        ParseHelper.addCategoryFetchListener(bf);

        fetchData();
    }

    void fetchData() {
        if (ParseHelper.getConnectedUser() != null) {
            try {
                ParseHelper.fetchCategoriesInBackThread();
            } catch (Exception e) {
                e.printStackTrace();
                SimpleErrorDialog.createDialog(this, getString(R.string.error), getString(R.string.not_able_to_fetch_from_db), -1).show();
            }
        } else {
            login(true);
            //SimpleErrorDialog.createDialog(getContext(), getString(R.string.error), getString(R.string.no_conncted_user), -1).show();
        }
    }

    public static final int LOGIN_ACTIVITY_TAG = 1;
    /*     login(true) == login     login(false) == logout     */
    public void login(boolean login){
        if(login) {
            if(ParseHelper.getConnectedUser() == null) {
                startActivityForResult(new Intent(this, LoginActivity.class), LOGIN_ACTIVITY_TAG);
            }
        } else { //logout
            ParseUser.logOut();
            login(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == LOGIN_ACTIVITY_TAG){
            if(resultCode == Activity.RESULT_OK){
                if(ParseHelper.getConnectedUser() != null) {
                    notifyUserChanged();
                    fetchData();
                } else {
                    login(true);
                }
            }
        }
    }
    ArrayList<UserChangedCallback> userChangedListeners = new ArrayList<>();
    public void addOnUserChangeListener(UserChangedCallback listener){
        if(! userChangedListeners.contains(listener)) userChangedListeners.add(listener);
    }
    private void notifyUserChanged(){
        for(UserChangedCallback listener : userChangedListeners){
            listener.userChanged();
        }
    }
    /*
        Adding option to swipe down to close keyboard
         */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        //private final List<Drawable> mFragmentImageIDList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {

            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {

            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title/*, int imageID*/) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
            //mFragmentImageList.add(getBaseContext().getResources().getDrawable(imageID));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
