package com.hazani.chagit.homeexpensemanagment;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by chagithazani on 1/26/16.
 */
public class ExpandableMultiLevelListView extends ListView implements View.OnClickListener {
    public ExpandableMultiLevelListView(Context context) {
        super(context);
    }
    public ExpandableMultiLevelListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ExpandableMultiLevelListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    interface ExpandableMultiLevelListViewCallbacks{
        void onItemExpanded(int position);
        void onItemCollapsed(int position);
    }

    public void setAdapter(ExpandableMultilevelListAdapter adapter){
        super.setAdapter(adapter);
        this.mLevelAdapter = adapter;
    }

    ExpandableMultilevelListAdapter mLevelAdapter;

    public void setOnExpandCollapseListener(ExpandableMultiLevelListViewCallbacks listener){
        callback = listener;
    }


    ExpandableMultiLevelListViewCallbacks callback;


    @Override
    //public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    public void onClick(View v) {
        if (mLevelAdapter == null){
            return;
        }
        int position = (int)v.getTag();
        ExpandableItem item = mLevelAdapter.getItem(position);
        if(! item.canOpen()){
            // maybe first try to open?
            return;
        }
        if(item.isOpen()){
            //collapse
            collapseCell(position);
            if(callback != null) callback.onItemCollapsed(position);
        } else {
            //expand
            expandCell(position);
            if(callback != null) callback.onItemExpanded(position);

        }
    }



    private void expandCell(int position){
        ExpandableItem parentItem = mLevelAdapter.getItem(position);
        if(! parentItem.canOpen()){
            return;
        }
        mLevelAdapter.addItems(mLevelAdapter.getChildren(position), position);
        parentItem.setOpen(true);
        mLevelAdapter.notifyDataSetChanged();
    }

    private void collapseCell(int position){
        ExpandableItem parentItem = mLevelAdapter.getItem(position);
        if(! parentItem.isOpen()){
            return;
        }
        collapseCell(parentItem);
        mLevelAdapter.notifyDataSetChanged();
    }

    private void collapseCell(ExpandableItem parent){
        ArrayList<ExpandableItem> children = parent.children;
        if(parent.children != null) {
            for (ExpandableItem child : parent.children){
                if(child.isOpen()){
                    collapseCell(child);
                }
            }
            mLevelAdapter.removeItems(parent.children);
        }
        parent.setOpen(false);
    }

//    public void collaplseList(){
//        for(int i = 0 ; i < mLevelAdapter.getCount() ; i++){
//            collapseCell(i);
//        }
//    }
}

