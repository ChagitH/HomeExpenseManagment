package com.hazani.chagit.homeexpensemanagment;

import android.util.Log;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;

/**
 * Created by chagithazani on 1/19/16.
 */
public abstract class ExpandableItem {

    // Expandable
    ArrayList<ExpandableItem> children;
    public boolean canOpen(){
        if(children != null){
            return (children.size() > 0);
        } else {
            return false;
        }
    }
    /*
   will add child to children array + will stamp the child with parent id, for use of ExpandableList
    */
    public void addChild(ExpandableItem child){
        if(children == null){
            children = new ArrayList<>();
        }
        child.setParentID(getID());
        children.add(child);
    }

    ExpandableItem getChild(int position){
        if (children != null && getChildrenSize() >= position){
            return  children.get(position);
        }
        return null;
    }

    protected int getChildrenSize(){
        if(children != null){
            return children.size();
        } else {
            return 0;
        }
    }
    abstract void fetchChildren();
    static int id = 0;
    int getID(){
        return  id++;
    }

    private boolean open = false;
    public void setOpen(boolean open) {
        this.open = open;
    }
    public boolean isOpen() {
        return open;
    }

    private int parentID = -1;
    public int getParentID() {
        return parentID;
    }
    public void setParentID(int parentID) {
        this.parentID = parentID;
    }

//        /*
//is this at all necessary?
// */
//    public enum ItemLevel {ORPFAN, HAS_PARENT}
////    ItemLevel getLevel() {
////        if(getParentID() == -1){
////            return ItemLevel.ORPFAN;
////        }else {
////            return ItemLevel.HAS_PARENT;
////        }
////    }


    // local for this app

    /*
    delete() will delete all childeren and itself from parse
     */
    protected void delete(){
        if(children != null) {
            for (ExpandableItem item : children) {
                item.delete();
            }
        }
        this.object.deleteInBackground();
    }

    ParseObject object;

    public ParseObject getObject() {
        return object;
    }

    protected String name;
    public String getName(){
        return name;
    }

    // all items will
    protected int sum = 0;
    public int getSum(){
        return sum;
    }
    abstract void setSum(int sum);

    abstract void fetchSum(ParseObject object);
    
    protected boolean holo;
    protected ExpandableItem(String name, boolean holo) {
        this.name = name;
        this.holo = holo;

    }
    protected ExpandableItem(ParseObject object, boolean fetchChildren){
        this.object = object;
        this.name = object.getString(Constants.c_NAME);

        this.fetchSum(object);

        if(fetchChildren) {
            fetchChildren();
        }

    }





//    static final public int CATEGORY = 1;
//    static final public int BUDGET_ITEM = 2;
//    static final public int EXPENSE = 3;

    public enum ItemType {CATEGORY, BUDGET_ITEM, EXPENSE}
    abstract ItemType getType();
    public static int typeToInt(ItemType type){
        switch (type){
            case EXPENSE: return 2;
            case BUDGET_ITEM: return 1;
            case CATEGORY: return 0;
        }
        return -1;
    }

    @Override
    public String toString(){
        return this.name;
    }
}
