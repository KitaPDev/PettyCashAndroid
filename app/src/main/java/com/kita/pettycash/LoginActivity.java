package com.kita.pettycash;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.kita.androidlib.client.AsyncRetrieveObject;
import com.kita.pettycash.client.interfaces.AsyncResponse;
import com.kitap.lib.bean.BEANPettyCashTransaction;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements AsyncResponse {

    String HOST = "10.0.2.2";
    int PORT = 45678;

    Context m_context;
    ProgressBar prgBar;
    EditText m_edtUsername;
    EditText m_edtPassword;
    Button m_btnForgotPassword;

    String m_strUsername;
    String m_strPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        m_context = this;

        prgBar = findViewById(R.id.prgbarLogin);

        m_edtUsername = findViewById(R.id.edt_Username);
        m_edtPassword = findViewById(R.id.edt_Password);
        m_btnForgotPassword = findViewById(R.id.btn_ForgotPassword);

    }

    public void onLogin(View view) throws NoSuchAlgorithmException {
        prgBar.setVisibility(View.INVISIBLE);

        m_strUsername = m_edtUsername.getText().toString();
        m_strPassword = m_edtPassword.getText().toString();

        String hashedPassword = hashPassword(m_strPassword);

        List<String> lsUser = new ArrayList<>();
        lsUser.add(m_strUsername);
        lsUser.add(hashedPassword);

        AsyncTask<Void, Void, Object> asyncAuthenticateUser = new AsyncRetrieveObject(HOST, PORT, m_context,
                "User", "AuthenticateUser", lsUser);

        ((AsyncRetrieveObject) asyncAuthenticateUser).delegate = this;
        ((AsyncRetrieveObject) asyncAuthenticateUser).setProgressbar(prgBar);

        asyncAuthenticateUser.execute();
    }

    @Override
    public void processFinish(Object p_objIsValidUser) throws Exception {
        Intent intent = new Intent(this, HomeActivity.class);

        boolean isValidUser = (boolean) p_objIsValidUser;

        if(isValidUser) {

            SharedPreferences sharedPreferences = getSharedPreferences("userInfo",
                    Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username", m_edtUsername.getText().toString());
            editor.apply();

            startActivity(intent);
            finish();

        } else {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);

            dlgAlert.setTitle("Error");
            dlgAlert.setMessage("Wrong password or username");
            dlgAlert.setPositiveButton("OK", null);
            dlgAlert.setCancelable(true);
            dlgAlert.create().show();

            dlgAlert.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
        }
    }

    public void onSignUp(View view) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    public void onForgotPassword(View view) {
        Intent intent = new Intent(this, ChangePasswordActivity.class);
        startActivity(intent);
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

    @Override
    public void processFinish(List<BEANPettyCashTransaction> lsBEANPettyCashTransaction) throws Exception {

    }
}
