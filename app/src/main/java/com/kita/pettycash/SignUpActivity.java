package com.kita.pettycash;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.kita.androidlib.client.AsyncExecuteMethod;
import com.kita.androidlib.client.AsyncRetrieveObject;
import com.kita.pettycash.client.interfaces.AsyncResponse;
import com.kitap.lib.bean.BEANPettyCashTransaction;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class SignUpActivity extends AppCompatActivity implements AsyncResponse {

    private static String HOST = "10.0.2.2";
    private static int PORT = 45678;
    Context m_context;

    String m_strNewUsername;
    String m_strNewPassword;
    String m_strConfirmPassword;

    ProgressBar prgBar;
    EditText m_edtNewUsername;
    EditText m_edtNewPassword;
    EditText m_edtConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        prgBar = findViewById(R.id.prgbarSignUp);
        m_context = this;

        m_edtNewUsername = findViewById(R.id.edt_NewUsername);
        m_edtNewPassword = findViewById(R.id.edt_ChangePassword);
        m_edtConfirmPassword = findViewById(R.id.edt_ConfirmChangePassword);
    }

    public void onSignUp(View view) {
        prgBar.setVisibility(View.INVISIBLE);

        m_strNewUsername =  m_edtNewUsername.getText().toString().toLowerCase();
        m_strNewPassword =  m_edtNewPassword.getText().toString();
        m_strConfirmPassword =  m_edtConfirmPassword.getText().toString();

        String strClassName = "User";
        String strMethodName = "isExistingUser";
        List<String> lsUsername = new ArrayList<>();
        lsUsername.add(m_strNewUsername);

        if(m_strNewPassword.length() < 8) {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(SignUpActivity.this);

            dlgAlert.setTitle("Error");
            dlgAlert.setMessage("Password must be at least 8 characters long.");
            dlgAlert.setPositiveButton("OK", null);
            dlgAlert.setCancelable(true);
            dlgAlert.create().show();

            dlgAlert.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

        } else if(!m_strNewPassword.equals(m_strConfirmPassword)) {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(SignUpActivity.this);

            dlgAlert.setTitle("Error");
            dlgAlert.setMessage("Passwords do not match");
            dlgAlert.setPositiveButton("OK", null);
            dlgAlert.setCancelable(true);
            dlgAlert.create().show();

            dlgAlert.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

        } else {
            AsyncTask<Void, Void, Object> asyncUserExists = new AsyncRetrieveObject(HOST, PORT, this,
                    strClassName, strMethodName, lsUsername);

            ((AsyncRetrieveObject) asyncUserExists).delegate = this;
            ((AsyncRetrieveObject) asyncUserExists).setProgressbar(prgBar);

            asyncUserExists.execute();

        }
    }

    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        for(byte b : hashPassword) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    public void processFinish(Object p_objIsExistingUser) throws NoSuchAlgorithmException {
        Intent intent = new Intent(this, LoginActivity.class);

        boolean isExistingUser = (boolean) p_objIsExistingUser;

        if(isExistingUser) {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(SignUpActivity.this);

            dlgAlert.setTitle("Error");
            dlgAlert.setMessage("Username has been taken");
            dlgAlert.setPositiveButton("OK", null);
            dlgAlert.setCancelable(true);
            dlgAlert.create().show();

            dlgAlert.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

        } else if(!isExistingUser && m_strNewPassword.equals(m_strConfirmPassword)) {
            m_strNewPassword = hashPassword(m_strNewPassword);

            List<String> lsUser = new ArrayList<>();
            lsUser.add(m_strNewUsername);
            lsUser.add(m_strNewPassword);

            AsyncTask<Void, Void, Void> asyncExecuteMethod = new AsyncExecuteMethod(HOST,
                    PORT, "User", "CreateUser", lsUser);
            ((AsyncExecuteMethod) asyncExecuteMethod).setProgressbar(prgBar);
            asyncExecuteMethod.execute();

            startActivity(intent);
            finish();
        }
    }

    @Override
    public void processFinish(List<BEANPettyCashTransaction> lsBEANPettyCashTransaction) throws Exception {

    }
}
