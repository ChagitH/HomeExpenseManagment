package com.hazani.chagit.homeexpensemanagment;


import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class PopupMenuFragment extends DialogFragment {
    private static final String ARG_NAMES = "param1";

    private String[] mNames;

    private View.OnClickListener listener;
    private Context context;

    public PopupMenuFragment() {
        // Required empty public constructor
    }

    public static PopupMenuFragment newInstance(Context context, View.OnClickListener listener, String[] tagAndNames) {
        PopupMenuFragment fragment = new PopupMenuFragment();
        fragment.listener = listener;
        fragment.context = context;

        Bundle args = new Bundle();
        args.putStringArray(ARG_NAMES, tagAndNames);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mNames = getArguments().getStringArray(ARG_NAMES);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.fragment_popup_menu, container, false);
        //TextView tvTitle = (TextView)rootView.findViewById(R.id.popupMenuTVtitle);
        TextView title = (TextView)getDialog().findViewById(android.R.id.title);
        if(title != null) {
            //title.setTextColor(android.R.color.black);
            title.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            title.setText(mNames[0]); // 1st param is title
        } else {
            getDialog().setTitle(mNames[0]);
        }

        //tvTitle.setText(mNames[0]); // 1st param is title
        String tag = mNames[1]; // 2ed param is tag to add to texts for uniq tag

        LayoutParams lpView = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        for(int i = 2 ; i < mNames.length ; i++) {
            String buttonText = mNames[i];

            Button b = new Button(context);
            b.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            b.setBackgroundColor(getResources().getColor(R.color.transparentColor));
            b.setText(buttonText);
            b.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            b.setTag(tag+buttonText);
            b.setGravity(Gravity.RIGHT);
            b.setPadding(5, 5, 5, 5);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onClick(v);
                        dismiss();
                    }
                }
            });
            rootView.addView(b,lpView);

        }
        return rootView;
    }
}
