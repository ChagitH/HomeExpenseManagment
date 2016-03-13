package com.hazani.chagit.homeexpensemanagment.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.view.ContextThemeWrapper;
import android.widget.TextView;

import com.hazani.chagit.homeexpensemanagment.R;


/**
 * Created by chagithazani on 2/28/16.
 */
public class SimpleErrorDialog extends AlertDialog {

    protected SimpleErrorDialog(Context context) {
        super(context);
    }

    public static AlertDialog  createDialog(Context context, String title, String msg, int imgId){
        ContextThemeWrapper themedContext = new ContextThemeWrapper(context,R.style.DialogTheme);
        AlertDialog alertDialog = new AlertDialog.Builder(themedContext).create();

        alertDialog.setTitle(title);

        alertDialog.setMessage(msg);

        if(imgId >0) {
            alertDialog.setIcon(imgId);
        }

        alertDialog.setButton("אישור", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return alertDialog;
    }
}
