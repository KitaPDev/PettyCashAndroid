package com.kita.pettycash;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.kita.androidlib.client.AsyncExecuteMethod;
import com.kita.androidlib.client.AsyncRetrieveObject;
import com.kita.androidlib.client.AsyncUserExists;
import com.kita.lib.rpc.BEANRemoteExecution;
import com.kita.pettycash.client.interfaces.AsyncResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class ChangePasswordActivity extends AppCompatActivity implements AsyncResponse {

    String HOST = "10.0.2.2";
    int PORT = 45678;

    ProgressBar prgBar;
    EditText m_edtUsername;
    EditText m_edtChangePassword;
    EditText m_edtConfirmChangePassword;

    String m_strUsername;
    String m_strChangePassword;
    String m_strConfirmChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        prgBar = findViewById(R.id.prgBarChangePassword);
        m_edtUsername = findViewById(R.id.edt_Username);
        m_edtChangePassword = findViewById(R.id.edt_ChangePassword);
        m_edtConfirmChangePassword = findViewById(R.id.edt_ConfirmChangePassword);
    }

    public void onChangePassword(View view) {
        prgBar.setVisibility(View.INVISIBLE);

        m_strUsername = m_edtUsername.getText().toString();
        m_strChangePassword = m_edtChangePassword.getText().toString();
        m_strConfirmChangePassword = m_edtConfirmChangePassword.getText().toString();

        String strClassName = "User";
        String strMethodName = "isExistingUser";
        List<String> lsUsername = new ArrayList<>();
        lsUsername.add(m_strUsername);

        if(m_strChangePassword.length() < 8) {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(ChangePasswordActivity.this);

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

        } else if(!m_strChangePassword.equals(m_strConfirmChangePassword)) {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(ChangePasswordActivity.this);

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
            AsyncTask<Void, Void, Object> asyncRetrieveObject = new AsyncRetrieveObject(HOST, PORT, this,
                    strClassName, strMethodName, lsUsername);

            ((AsyncRetrieveObject) asyncRetrieveObject).delegate = this;
            ((AsyncRetrieveObject) asyncRetrieveObject).setProgressbar(prgBar);

            asyncRetrieveObject.execute();

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
        if(isExistingUser && m_strChangePassword.equals(m_strConfirmChangePassword)) {
            m_strChangePassword = hashPassword(m_strChangePassword);

            List<String> lsUser = new ArrayList<>();
            lsUser.add(m_strUsername);
            lsUser.add(m_strChangePassword);

            AsyncTask<Void, Void, Void> asyncExecuteMethod = new AsyncExecuteMethod(prgBar, HOST,
                    PORT, "User", "ChangePassword", lsUser);
            asyncExecuteMethod.execute();

            startActivity(intent);
            finish();
        }
    }

    @Override
    public void processFinish(List output) throws Exception {}
}
