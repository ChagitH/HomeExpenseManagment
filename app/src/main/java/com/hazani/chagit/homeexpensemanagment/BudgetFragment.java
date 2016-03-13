package com.hazani.chagit.homeexpensemanagment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

import com.hazani.chagit.homeexpensemanagment.callbacks.CategoriesFetchCallback;
import com.hazani.chagit.homeexpensemanagment.callbacks.UserChangedCallback;
import com.hazani.chagit.homeexpensemanagment.dialogs.AddCategoryFragment;
import com.hazani.chagit.homeexpensemanagment.dialogs.CustomAlertDialog;
import com.hazani.chagit.homeexpensemanagment.dialogs.SimpleErrorDialog;

import java.util.ArrayList;

public class BudgetFragment extends Fragment implements  AddCategoryFragment.OnDialogClickListener, View.OnClickListener, CategoriesFetchCallback, SwipeRefreshLayout.OnRefreshListener, UserChangedCallback {


    ExpandableListView mELVlist;
    ExpandableCategoryBudgetListAdapter listAdapter;
    SwipeRefreshLayout swipeR;
    public BudgetFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listAdapter = new ExpandableCategoryBudgetListAdapter(getContext());
        //listAdapter.addItems(ParseHelper.getCategories());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_budget, container, false);
        mELVlist = (ExpandableListView) rootView.findViewById(R.id.budgetExpandableListView);
        mELVlist.setAdapter(listAdapter);
        swipeR = (SwipeRefreshLayout) rootView.findViewById(R.id.budget_swipe_refresh_layout);
        swipeR.setOnRefreshListener(this);
        //mELVlist.setGroupIndicator(null); // hides the arrow
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        DELETE_CATEGORY = getString(R.string.delete_item);
        UPDATE_CATEGORY = getString(R.string.update_item);
        ADD_BUDGET_ITEM = getString(R.string.add_budget_item);
//
//        mELVlist.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
//            @Override
//            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
//                Log.i("XXXXXXXXXXXXXX", "onChildClick groupPosition = " + groupPosition);
//                return false;
//            }
//        });
        mELVlist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                ExpandableItem item = (ExpandableItem) parent.getItemAtPosition(position);
                // if category
                if(item instanceof Category){
                    if(position == parent.getCount()-1){ // if add new Category -> get name and add new Category
                        openDialogForAddingNewCategory(null, position);
                    } else { // if exsiting Category -> open category menu
                        openDialogWithCategoryMenu(parent, (Category) item, position);
                    }
                } else if (item instanceof BudgetLine){ // if Budget Item -> open budget menu
                    openDialogWithBudgetLineMenu(parent, (BudgetLine) item, position);
                }

                return true;
            }
        });
        listAdapter.addItems(ParseHelper.getCategories());
    }

    private void openDialogWithBudgetLineMenu(AdapterView<?> parent, final BudgetLine item, final int position) {

        final String TAG_1 = "budgetLine";
        String title = getString(R.string.budget_item_possibilities) + ": " + item.getName();
// deleting budget line is not supported yet
        String[] items = {title,TAG_1 /*, getString(R.string.delete_item)*/,getString(R.string.update_item)};
        PopupMenuFragment dialog = PopupMenuFragment.newInstance(getContext(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = (String)v.getTag();
                Log.i("XXXXXXXXXXXXX", "Clicked tag = " + tag);
//                if (tag.equalsIgnoreCase(TAG_1+getString(R.string.delete_item))){ // not supported yet
//                    Log.i("XXXXXXXXXXXXXXXXXXXXXXXXXX", "XXXXXXXXXXXXX DELETE IT!");
//                    deleteBudgetLine();
//
//                } else
                if (tag.equalsIgnoreCase(TAG_1+getString(R.string.update_item))){
                    Log.i("XXXXXXXXXXXXXXXXXXXXXXXXXX", "XXXXXXXXXXXXX UPDATE IT!");
                    updateBudgetLine(item);
                }

            }
        }, items);
        //dialog.getDialog().setTitle(title);
        dialog.show(getFragmentManager(), "budgetMenu");

    }

    private void updateBudgetLine(final BudgetLine budgetline) {
        AddCategoryFragment dialog = AddCategoryFragment.newInstance(new AddCategoryFragment.OnDialogClickListener() {
            @Override
            public void okClicked(int categoryOrBudgetTag, String newName, String sum, Object object) {
                Log.i("XXXXXXXXXXXXXXXXXXXXXXXXXX", "XXXXXXXXXXXXX OK TO UPDATE BUDGET!");
                budgetline.update(newName, Integer.valueOf(sum));
            }

            @Override
            public void cancelClicked() {  }
        }, AddCategoryFragment.UPDATE_BUDGET_TAG, budgetline);
        dialog.show(getFragmentManager(),"updateBudget");
    }

    // not supported yet
    private void deleteBudgetLine(BudgetLine budget, int position){
        if( budget != null && budget.getChildrenSize() <= 0){ // this will not always work...
            //listAdapter.removeItemAt(position);
            budget.delete();
            refreshData(false);
        } else {
            CustomAlertDialog dialog = CustomAlertDialog.newInstance(selectedCategory.getName(),getString(R.string.category_cannot_delete), getString(R.string.cancel),null,false , null);
            dialog.show(getFragmentManager(), "alert");
        }

    }

    private void refreshData(boolean offLine) {
        if(offLine) {
            if (swipeR != null) swipeR.setRefreshing(true);
            ArrayList<Category> data = ParseHelper.getCategories();
            if (getActivity() != null && isAdded()) {
                listAdapter.removeAllItems();
                listAdapter.addItems(data);
                listAdapter.notifyDataSetChanged();
            }
            if(swipeR!=null)swipeR.setRefreshing(false);
        }else {
            try {
                ParseHelper.fetchCategoriesInBackThread();
            } catch (Exception e) {
                SimpleErrorDialog.createDialog(getContext(), getString(R.string.error), getString(R.string.error_problem_with_fetching_data), -1).show();
            }
        }
    }

    private static String CATEGORY_TAG = "category";
    private String DELETE_CATEGORY;
    private String UPDATE_CATEGORY;
    private String ADD_BUDGET_ITEM;
    private Category selectedCategory;
    private int selectedCategoryPosition;
    private View selectedCell;

    private void openDialogWithCategoryMenu(View cell, Category category, int position) {
        selectedCategory = category;
        selectedCategoryPosition = position;
        selectedCell = cell;

        String title = getString(R.string.category_possibilities) + ": " + category.getName();
        String[] items = {title,CATEGORY_TAG, UPDATE_CATEGORY,DELETE_CATEGORY,ADD_BUDGET_ITEM};
        PopupMenuFragment dialog = PopupMenuFragment.newInstance(getContext(), this, items);
        //dialog.getDialog().setTitle(title);
        dialog.show(getFragmentManager(),"CategoryMenu");
    }

    private void openDialogForAddingNewCategory(Category category, int position) {
        AddCategoryFragment dialog = AddCategoryFragment.newInstance(this, AddCategoryFragment.ADD_CATEGORY_TAG, category);
        dialog.show(getFragmentManager(),"AddNewCategory");
        //dialog.show(getFragmentManager(),getString(R.string.new_category));
    }

    @Override
    public void categoriesFetched(boolean success) {
        if (success) {
//            if (swipeR != null) swipeR.setRefreshing(true);
//            ArrayList<Category> data = ParseHelper.getCategories();
//            if (getActivity() != null && isAdded()) {
//                listAdapter.removeAllItems();
//                listAdapter.addItems(data);
//                listAdapter.notifyDataSetChanged();
            refreshData(true);
        }
        //if (swipeR != null) swipeR.setRefreshing(false);
    }


    @Override
    public void okClicked(int categoryOrBudgetTag, String newItemName, String sum, Object object) {
        if(categoryOrBudgetTag == AddCategoryFragment.ADD_CATEGORY_TAG) {

            Category newCategory = new Category(newItemName, ParseHelper.getConnectedUser(), false);
            listAdapter.addItem(newCategory);
        } else if(categoryOrBudgetTag == AddCategoryFragment.ADD_BUDGET_TAG){
            Category parentCategory = (Category)object;
            if(parentCategory != null){
                BudgetLine bl = new BudgetLine(newItemName,((!sum.equalsIgnoreCase("")) ? Integer.parseInt(sum) : 0), ParseHelper.getAccount(), false, parentCategory);
                parentCategory.addChild(bl);
            }
        } else if(categoryOrBudgetTag == AddCategoryFragment.UPDATE_CATEGORY_TAG){
            Category parentCategory = (Category)object;
            if(parentCategory != null){
                parentCategory.setName(newItemName);
            }
        }
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void cancelClicked() {
        //do nothing
    }

    @Override
    public void onClick(View v) {
        String tag = (String)v.getTag();
        if (tag.equalsIgnoreCase(CATEGORY_TAG+UPDATE_CATEGORY)){
            updateCategory(selectedCategory,selectedCategoryPosition,selectedCell);

        } else if (tag.equalsIgnoreCase(CATEGORY_TAG+DELETE_CATEGORY)){
            deleteCategory(selectedCategory,selectedCategoryPosition);


        } else if (tag.equalsIgnoreCase(CATEGORY_TAG+ADD_BUDGET_ITEM)){
            addBudgetItem(selectedCategory,selectedCategoryPosition);
        }

        selectedCategoryPosition = -1;
        selectedCategory = null;
        selectedCell = null;
    }

    private void updateCategory(final Category selectedCategory, int selectedCategoryPosition, View view) {
        AddCategoryFragment dialog = AddCategoryFragment.newInstance(this, AddCategoryFragment.UPDATE_CATEGORY_TAG, selectedCategory);
        dialog.show(getFragmentManager(),"updateCategory");
    }

    private void deleteCategory(Category selectedCategory, int selectedCategoryPosition){
        if( selectedCategory != null && selectedCategory.getChildrenSize() <= 0){
            listAdapter.removeItemAt(selectedCategoryPosition);
            selectedCategory.delete();
            listAdapter.notifyDataSetChanged();//in specific item...
        } else {
            CustomAlertDialog dialog = CustomAlertDialog.newInstance(selectedCategory.getName(),getString(R.string.category_cannot_delete), getString(R.string.cancel),null,false , null);
            dialog.show(getFragmentManager(), "alert");
        }

    }

    private void addBudgetItem(Category selectedCategory, int selectedCategoryPosition){
        AddCategoryFragment dialog = AddCategoryFragment.newInstance(this, AddCategoryFragment.ADD_BUDGET_TAG, selectedCategory);
        dialog.show(getFragmentManager(), "AddNewCategory");
    }

//    private static boolean firstTime = true;
//    /*
//    Used to run code as late as possible, just before use of user, so will give enoghe time for all data to be fetched.
//     */
//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        if(firstTime) {
//            if (isVisibleToUser) {
//                refreshData(true);
//            }
//            firstTime = false;
//        }
//    }
    @Override
    public void onRefresh() {
        refreshData(false);
    }

    @Override
    public void userChanged() {
        if(swipeR != null) swipeR.setRefreshing(true);
    }
}
