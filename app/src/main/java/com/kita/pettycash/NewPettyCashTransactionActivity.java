package com.kita.pettycash;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.kita.lib.rpc.BEANRemoteExecution;
import com.kita.pettycash.client.interfaces.AsyncResponse;
import com.kitap.lib.bean.BEANPettyCashTransaction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class NewPettyCashTransactionActivity extends AppCompatActivity implements AsyncResponse {

    private static String HOST = "10.0.2.2";
    private static int PORT = 45678;

    private String m_strUsername = null;

    private EditText m_edtPayee;
    private EditText m_edtAmount;
    private EditText m_edtNote;
    private ProgressBar prgBar;

    private String m_strPayeeUsername = null;
    private BigDecimal m_bdAmount = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_pettycash_transaction);

        m_edtPayee = findViewById(R.id.edt_PayeeUsername);
        m_edtAmount = findViewById(R.id.edt_Amount);
        m_edtNote = findViewById(R.id.edtNote);
        prgBar = findViewById(R.id.prgBarNewTransaction);

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);

        m_strUsername = sharedPreferences.getString("username", "");

        Toolbar toolbarNewTransaction = findViewById(R.id.tb_new_transaction);

        toolbarNewTransaction.setTitle("");
        setSupportActionBar(toolbarNewTransaction);

        toolbarNewTransaction.setTitle("New Petty Cash Transaction");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_transaction, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case android.R.id.home:
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);

                dlgAlert.setTitle("Discard?");
                dlgAlert.setMessage("Are you sure you want to discard input?");
                dlgAlert.setCancelable(true);

                dlgAlert.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });

                dlgAlert.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                dlgAlert.create().show();
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.action_save:
                prgBar.setVisibility(View.INVISIBLE);

                m_bdAmount = new BigDecimal(m_edtAmount.getText().toString());

                if(m_bdAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    dlgAlert = new AlertDialog.Builder(this);

                    dlgAlert.setTitle("Invalid Amount");
                    dlgAlert.setMessage("Borrowed amount must be more than 0.");
                    dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    dlgAlert.setCancelable(true);

                    dlgAlert.create().show();

                } else {
                    m_strPayeeUsername = m_edtPayee.getText().toString().toLowerCase();

                    List<String> lsUsername = new ArrayList<>();
                    lsUsername.add(m_strPayeeUsername);

                    AsyncTask<Void, Void, Object> asyncUserExists = new AsyncUserExists("User",
                            "isExistingUser", lsUsername);
                    ((AsyncUserExists) asyncUserExists).delegate = this;
                    asyncUserExists.execute();

                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);

        dlgAlert.setTitle("Discard?");
        dlgAlert.setMessage("Are you sure you want to discard input?");
        dlgAlert.setCancelable(true);

        dlgAlert.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        dlgAlert.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        dlgAlert.create().show();
    }

    private class AsyncNewPettyCashTransaction extends AsyncTask<Void, Void, Void> {
        public AsyncResponse delegate = null;

        String m_strClassName;
        String m_strMethodName;
        List<BEANPettyCashTransaction> m_lsMethodParameters;

        AsyncNewPettyCashTransaction(String p_strClassName, String p_strMethodName, List<BEANPettyCashTransaction> p_lsMethodParameters) {
            m_strClassName = p_strClassName;
            m_strMethodName = p_strMethodName;
            m_lsMethodParameters = p_lsMethodParameters;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            prgBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                BEANRemoteExecution remoteExecution = new BEANRemoteExecution(m_strClassName,
                        m_strMethodName, m_lsMethodParameters);

                Socket clientSocket = new Socket(HOST, PORT);

                ObjectOutputStream clientOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                clientOutputStream.writeObject(remoteExecution);
                clientOutputStream.flush();

                clientOutputStream.close();
                clientSocket.close();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(NewPettyCashTransactionActivity.this, "Error, please try again.", Toast.LENGTH_SHORT).show();

            }

            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            prgBar.setVisibility(View.GONE);

            finish();
        }
    }

    public void processFinish(Object p_objIsExistingUser) {
        boolean isExistingUser = (boolean) p_objIsExistingUser;

        if(!isExistingUser) {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);

            dlgAlert.setTitle("Invalid User");
            dlgAlert.setMessage("User with the given username does not exist.");
            dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            dlgAlert.setCancelable(true);

            dlgAlert.create().show();

        } else {
            Date sqlDate = new java.sql.Date(Calendar.getInstance().getTime().getTime());
            String strNote = m_edtNote.getText().toString();

            BEANPettyCashTransaction beanPettyCashTransaction = new BEANPettyCashTransaction();
            beanPettyCashTransaction.setDatePosting(sqlDate);
            beanPettyCashTransaction.setAmount(m_bdAmount);
            beanPettyCashTransaction.setUsernamePayer(m_strUsername);
            beanPettyCashTransaction.setUsernamePayee(m_strPayeeUsername);
            beanPettyCashTransaction.setNote(strNote);
            beanPettyCashTransaction.setUsernameReturn("");
            beanPettyCashTransaction.setUsernameReturnReceived("");

            List<BEANPettyCashTransaction> lsPettyCash = new ArrayList<>();
            lsPettyCash.add(beanPettyCashTransaction);

            AsyncTask<Void, Void, Void> asyncNewPettyCashTransaction = new AsyncNewPettyCashTransaction(
                    "PettyCash", "createNewTransaction", lsPettyCash);
            asyncNewPettyCashTransaction.execute();

        }
    }

    private class AsyncUserExists extends AsyncTask<Void, Void, Object> {
        public AsyncResponse delegate = null;

        String m_strClassName;
        String m_strMethodName;
        List<String> m_lsMethodParameters;

        AsyncUserExists(String p_strClassName, String p_strMethodName, List<String> p_lsMethodParameters) {
            m_strClassName = p_strClassName;
            m_strMethodName = p_strMethodName;
            m_lsMethodParameters = p_lsMethodParameters;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            prgBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(Void... voids) {
            Object objResult = null;

            try {
                BEANRemoteExecution remoteExecution = new BEANRemoteExecution(m_strClassName,
                        m_strMethodName, m_lsMethodParameters);

                Socket clientSocket = new Socket(HOST, PORT);

                ObjectOutputStream clientOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                clientOutputStream.writeObject(remoteExecution);
                clientOutputStream.flush();

                ObjectInputStream clientInputStream = new ObjectInputStream(clientSocket.getInputStream());
                objResult = clientInputStream.readObject();

                clientOutputStream.close();
                clientInputStream.close();
                clientSocket.close();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(NewPettyCashTransactionActivity.this, "Error, please try again.", Toast.LENGTH_SHORT).show();

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return objResult;
        }

        protected void onPostExecute(Object result) {
            super.onPostExecute(result);

            prgBar.setVisibility(View.GONE);

            try {

                delegate.processFinish(result);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void processFinish(List<BEANPettyCashTransaction> lsBEANPettyCashTransaction) throws Exception {

    }

}
