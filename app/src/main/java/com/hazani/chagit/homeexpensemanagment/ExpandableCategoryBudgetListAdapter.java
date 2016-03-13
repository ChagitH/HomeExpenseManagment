package com.hazani.chagit.homeexpensemanagment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by chagithazani on 1/19/16.
 */
public class ExpandableCategoryBudgetListAdapter extends BaseExpandableListAdapter{//} implements View.OnClickListener {

    private Context context;
    private ArrayList items;

    public ExpandableCategoryBudgetListAdapter(Context context){
        this.context = context;
        items = new ArrayList<>();
        items.add(new Category(context.getString(R.string.new_category),null,true));
    }

    public void addItem(ExpandableItem item){
        items.add(0,item);
    }

    public void addItems(Collection items){
        this.items.addAll(0, items);
    }

    public void removeItemAt(int selectedCategoryPosition) {
        this.items.remove(selectedCategoryPosition);
    }
    public void removeAllItems(){
        items.clear();
        items.add(new Category(context.getString(R.string.new_category), null, true));
    }
    @Override
    public int getGroupCount() {
        return items.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ExpandableItem item = (ExpandableItem) items.get(groupPosition);
        return item.getChildrenSize();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return items.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ExpandableItem parent = (ExpandableItem) getGroup(groupPosition);
        if(parent.canOpen() && parent.getChildrenSize() > childPosition){
            return parent.children.get(childPosition);
        }
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ExpandableItem item = (ExpandableItem) getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item_category, null);
        }

        convertView.setBackgroundColor(context.getResources().getColor(R.color.colorForLevelThreeCell));
        EditText etName = (EditText) convertView.findViewById(R.id.categoryItemETname);
        etName.setClickable(false); // so long click will not be called from it
        etName.setText(item.getName());

        if(groupPosition == getGroupCount()-1){ //the last cell,  prepared for adding category
            etName.setTypeface(null, Typeface.ITALIC);
        } else {
            etName.setTypeface(null, Typeface.NORMAL);
        }

        return convertView;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }


    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ExpandableItem itemCategory = (ExpandableItem) getGroup(groupPosition);
        ExpandableItem item = itemCategory.getChild(childPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item_budget, null);
        }
        convertView.setBackgroundColor(context.getResources().getColor(R.color.colorForLevelTwoCell));
        TextView etName = (TextView) convertView.findViewById(R.id.budgetItemTVname);
        TextView etSum = (TextView) convertView.findViewById(R.id.budgetItemTVsum);

        etName.setTypeface(null, Typeface.BOLD);

        etName.setText(item.getName());// throws NULL POINTER EXCEPTION WHEN OPENED FOR CHILD!!!

        etSum.setText(item.getSum() > 0 ? String.valueOf(item.getSum()) : "");

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
