package com.hazani.chagit.homeexpensemanagment;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by chagithazani on 1/19/16.
 */
public abstract class ExpandableMultilevelListAdapter extends BaseAdapter{

    private Context context;
    ArrayList<ExpandableItem> items;

    ExpandableMultilevelListAdapter(/*Context context*/){
        items = new ArrayList();
        //this.context = context;
    }
    public void addItem(ExpandableItem item){
        if(item != null) items.add(item);
    }

//    public void addItems(Collection items){
//        this.items.addAll(items);
//    }
//    @Override
//    public void notifyDataSetChanged(){
//        super.notifyDataSetChanged();
//
//    }
    public void addItems(Collection items, int position){
        if(items != null) this.items.addAll(position + 1, items);
    }
    public void addAllItems(Collection items){
        this.items.addAll(items);
    }
    public void replaceAllItems(Collection items){
        this.items.clear();
        addAllItems(items);
    }
    public void removeAllItems(){
        items.clear();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ExpandableItem getItem(int position){
        //if(position >= 0 && position < getCount()) {
            return items.get(position);
        //}
    }
    @Override
    public long getItemId(int position) {
        return items.get(position).getID();
    }

    public abstract ArrayList<ExpandableItem> getChildren(int position);
    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent) ;

    @Override
    public abstract int getItemViewType(int position);

    @Override
    public abstract int getViewTypeCount();

    public void removeItems(ArrayList<ExpandableItem> children/*, int position*/) {
        items.removeAll(children);
    }
}
