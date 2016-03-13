package com.hazani.chagit.homeexpensemanagment;


import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hazani.chagit.homeexpensemanagment.dialogs.SimpleErrorDialog;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SignUpCallback;

import org.json.JSONObject;

public class LoginFragment extends Fragment implements View.OnClickListener{
    //LoginDoneCallback listener = null;

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    TextView mTVtitle;
    EditText mETemail, mETpassword, mETaccountPassword, mETaccountEmail, mETname;
    CheckBox mCBisAccountCreatedAsMainAccount;
    Button mBcancel, mBlogin, mBforgotPassword, mBnotRegistered;
    LinearLayout mCBcreateAsMainAccount;

    boolean mIsRegister = false, mCreateAsMainAccount = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = (View) inflater.inflate(R.layout.fragment_login, container, false);
        mTVtitle = (TextView) rootView.findViewById(R.id.loginTVtitle);
        mETemail = (EditText) rootView.findViewById(R.id.loginEtEmail);
        mETpassword = (EditText) rootView.findViewById(R.id.loginEtPassword);
        mETaccountPassword = (EditText) rootView.findViewById(R.id.loginEtPasswordMainAccount);
        mETaccountEmail = (EditText) rootView.findViewById(R.id.loginEtEmailMainAccount);
        mETname = (EditText) rootView.findViewById(R.id.loginEtName);

        mCBisAccountCreatedAsMainAccount = (CheckBox) rootView.findViewById(R.id.loginCbIsCreatedAsMainAccount);
        mCBisAccountCreatedAsMainAccount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setMainAccountChecked(isChecked);
            }
        });
        mCBcreateAsMainAccount = (LinearLayout)rootView.findViewById(R.id.loginLinearLayoutCheckBox);
        mBcancel = (Button) rootView.findViewById(R.id.loginBCancel);
        mBcancel.setOnClickListener(this);
        mBlogin = (Button) rootView.findViewById(R.id.loginBLogin);
        mBlogin.setOnClickListener(this);
        mBforgotPassword = (Button) rootView.findViewById(R.id.loginBForgotPassword);
        mBforgotPassword.setOnClickListener(this);
        mBnotRegistered = (Button) rootView.findViewById(R.id.loginBNotRegistered);
        mBnotRegistered.setOnClickListener(this);

        setActivityForRegistration(mIsRegister);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        if (v == mBcancel){
            closeActivity(Activity.RESULT_CANCELED);
        } else if (v == mBlogin){
            loginAndDismiss();
        } else if (v == mBforgotPassword){
            retrievePassword();
        } else if (v == mBnotRegistered){
            mIsRegister = !mIsRegister;
            setActivityForRegistration(mIsRegister);
        }
    }

    private void loginAndDismiss(){
        String email = mETemail.getText().toString();
        String password = mETpassword.getText().toString();
        if (email.length() > 0 && password.length() > 0) {
            if (mIsRegister) { // registeration
                String name = mETname.getText().toString();
                if(name.length() > 0){
                    if (mCreateAsMainAccount) { // create the new account as main account. Se tu.
                        register(email, password, name, null);
                    } else { // create the new account as regular account and connect it to the main account given.
                        String accountEmail = mETaccountEmail.getText().toString();
                        String accountPassword = mETaccountPassword.getText().toString();
                        if (accountEmail.length() > 0 && accountPassword.length() > 0) {
                            ParseUser accountUser;
                            try {
                                accountUser = ParseHelper.getAccount(accountEmail, accountPassword);
                            } catch (ParseException e) {
                                accountUser = null;
                            }
                            if (accountUser == null) { //main account given, is not correct
                                SimpleErrorDialog.createDialog(getContext(),getString(R.string.login_problem), getString(R.string.login_account_not_exsits),-1).show();
                            } else { // main account found
                                register(email, password, name, accountUser);
                            }
                        } else { // fields of account not full
                            SimpleErrorDialog.createDialog(getContext(),getString(R.string.login_problem), getString(R.string.login_account_fields_not_full),-1).show();
                        }
                    }
                } else { // registration but name not entered
                    SimpleErrorDialog.createDialog(getContext(),getString(R.string.login_problem), getString(R.string.login_name_not_enterd),-1).show();
                }
            } else { // login
                login(email,password);
            }
        }else { // fields of user not full
            SimpleErrorDialog.createDialog(getContext(),getString(R.string.login_problem), getString(R.string.login_account_not_full),-1).show();
        }




    }

    private void closeActivity(int result){
        Activity activity = getActivity();
        activity.setResult(result);
        activity.finish();
    }

    private void retrievePassword(){
        String email = mETemail.getText().toString();
        if (/*email != null && */email.length() > 0) {
            ParseUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        // An email was successfully sent with reset instructions.
                        SimpleErrorDialog.createDialog(getContext(),getString(R.string.login_problem), getString(R.string.login_password_email_sent),-1).show();
                    } else {
                        // Something went wrong. Look at the ParseException to see what's up.
                        SimpleErrorDialog.createDialog(getContext(),getString(R.string.login_problem), e.getMessage(),-1).show();
                    }
                }
            });
        }else {// email not entered
            SimpleErrorDialog.createDialog(getContext(),getString(R.string.login_problem), getString(R.string.login_email_not_entered),-1).show();

        }

    }

    private void setActivityForRegistration(boolean register) {
        mTVtitle.setText(getString(register ? R.string.title_register : R.string.title_login));
        mBnotRegistered.setText(getString(register ? R.string.go_back_to_login : R.string.not_registered_go_to_registration));

        int ifRegisterBeVisible = register ? View.VISIBLE : View.INVISIBLE;
        mETname.setVisibility(ifRegisterBeVisible);
        mCBcreateAsMainAccount.setVisibility(ifRegisterBeVisible);
        if(!mCreateAsMainAccount) {
            mETaccountPassword.setVisibility(ifRegisterBeVisible);
            mETaccountEmail.setVisibility(ifRegisterBeVisible);
        }

        mBlogin.setText(getString(register ? R.string.register : R.string.login));
        mBforgotPassword.setVisibility(register ? View.INVISIBLE : View.VISIBLE);
    }

    private void setMainAccountChecked(boolean isChecked){
        mCreateAsMainAccount = isChecked;
        int ifCheckedBeInvisible = isChecked ? View.INVISIBLE: View.VISIBLE ;
        mETaccountEmail.setVisibility(ifCheckedBeInvisible);
        mETaccountPassword.setVisibility(ifCheckedBeInvisible);
    }

    private void login(String username, String password){
        ParseUser.logOut();
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    closeActivity(Activity.RESULT_OK);
                } else {
                    SimpleErrorDialog.createDialog(getContext(),getString(R.string.login_problem), getString(R.string.login_account_not_exsit),-1).show();
                }
            }
        });
    }

    private void register(String username, String password, String name ,ParseUser account){
        ParseUser.logOut();
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);

        if(account != null) user.put(Constants.c_ACCOUNT, account);
        user.put(Constants.c_NAME, name);

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    closeActivity(Activity.RESULT_OK);
                } else {
                    SimpleErrorDialog.createDialog(getContext(),getString(R.string.login_problem), e.getMessage(),-1).show();
                }
            }
        });
    }
//
//    private void notifyListenerThatDone(String name){
//        if (listener != null){
//            listener.loginDone(name);
//        }
//    }
//    private ParseUser getAccount(String username, String password){
//        try {
//            ParseUser.logIn(username, password);
//        } catch (ParseException e) {
//            e.printStackTrace();
//            return null;
//        }
//         //todo:  this is the better way to do it, not via login and logout
////        ParseQuery<ParseUser> account = ParseQuery.getQuery(Constants.t_USER);
////        account.wher
//        ParseUser accountUser = ParseUser.getCurrentUser();
//        if (accountUser != null){
//            ParseUser.logOut();
//
//            return accountUser;
//        } else {
//            return null;
//        }
//    }

//    interface LoginDoneCallback{
//        public void loginDone(String name);
//    }
}
