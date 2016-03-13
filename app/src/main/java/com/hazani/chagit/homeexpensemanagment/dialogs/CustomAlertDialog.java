package com.hazani.chagit.homeexpensemanagment.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hazani.chagit.homeexpensemanagment.*;
import com.hazani.chagit.homeexpensemanagment.callbacks.DoItCallback;
import com.hazani.chagit.homeexpensemanagment.callbacks.UserOkToDeleteItemCallback;
import com.hazani.chagit.homeexpensemanagment.callbacks.UserOkToDeleteItemCallback;

/**
 * Created by chagithazani on 2/14/16.
 */
//todo: check how the buttons apear with long text on smaller devices
public class CustomAlertDialog extends DialogFragment implements View.OnClickListener{
    String title, msg, buttonText1, buttonText2;
    boolean cancelable;
    String cancelTag = "cancel";
    String allOkTag = "all";
    String fromHereAndOnTag = "fromHereAndOn";
    UserOkToDeleteItemCallback callback;
    public CustomAlertDialog(){}
    public static CustomAlertDialog newInstance(String title, String msg, String button1Text,String button2Text, boolean cancelable, UserOkToDeleteItemCallback callback) {
        CustomAlertDialog fragment = new CustomAlertDialog();
        fragment.title = title;
        fragment.msg = msg;
        fragment.buttonText1 = button1Text;
        fragment.buttonText2 = button2Text;
        fragment.cancelable = cancelable;
        fragment.callback = callback;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.custom_dialog_fragment, container, false);
        TextView title = (TextView)getDialog().findViewById(android.R.id.title);
        if(title != null) {
            title.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);
            title.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            title.setText(this.title);
        } else {
            getDialog().setTitle(this.title);
        }
        TextView tvMessage = (TextView) rootView.findViewById(R.id.custom_dialog_textview);
        tvMessage.setText(this.msg);
        //button 1
        Button button1 = (Button) rootView.findViewById(R.id.custom_dialog_button_option1);
        button1.setTag(allOkTag);
        button1.setText(this.buttonText1);
        button1.setOnClickListener(this);

        //button 2
        Button button2 = (Button) rootView.findViewById(R.id.custom_dialog_button_option2);
        if(this.buttonText2 != null) {
            button2.setTag(fromHereAndOnTag);
            button2.setText(this.buttonText2);
            button2.setOnClickListener(this);
        }else {
            button2.setVisibility(View.GONE);
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            button1.setLayoutParams(param);
        }
        // cancel button
        Button cancelButton = (Button) rootView.findViewById(R.id.custom_dialog_button_cancel);
        if( ! cancelable ) {
            cancelButton.setVisibility(View.GONE);
        } else {
            cancelButton.setOnClickListener(this);
            cancelButton.setTag(cancelTag);
        }

        return rootView;
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() == this.allOkTag){
            if(callback != null) callback.deleteAll();
        }else if (v.getTag() == this.fromHereAndOnTag){
            if(callback != null) callback.deleteFromNowOn();
        }
        dismiss();
    }
}
