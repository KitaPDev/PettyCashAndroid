package com.kita.pettycash;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.kita.androidlib.client.AsyncRetrieveObject;
import com.kita.lib.rpc.BEANRemoteExecution;
import com.kita.pettycash.client.interfaces.AsyncResponse;
import com.kitap.lib.bean.BEANPettyCashTransaction;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.transform.Result;


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

    Context m_context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_pettycash_transaction);

        m_context = this;

        m_edtPayee = findViewById(R.id.edt_PayeeUsername);
        m_edtAmount = findViewById(R.id.edt_Amount);
        m_edtNote = findViewById(R.id.edt_Note);
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
                                NewPettyCashTransactionActivity newPettyCashTransactionActivity = (NewPettyCashTransactionActivity) m_context;
                                NavUtils.navigateUpFromSameTask(newPettyCashTransactionActivity);
                            }
                        });

                dlgAlert.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                dlgAlert.create().show();
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

                    if(m_strUsername.equals(m_strPayeeUsername)) {
                        dlgAlert = new AlertDialog.Builder(this);

                        dlgAlert.setTitle("Error!");
                        dlgAlert.setMessage("Payee cannot be yourself!");
                        dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        dlgAlert.setCancelable(true);

                        dlgAlert.create().show();

                    } else {
                        List<String> lsUsername = new ArrayList<>();
                        lsUsername.add(m_strPayeeUsername);

                        AsyncTask<Void, Void, Object> asyncUserExists = new AsyncRetrieveObject(HOST, PORT, this,
                                "User", "isExistingUser", lsUsername);

                        ((AsyncRetrieveObject) asyncUserExists).delegate = this;
                        ((AsyncRetrieveObject) asyncUserExists).setProgressbar(prgBar);

                        asyncUserExists.execute();

                    }
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

    private class AsyncNewPettyCashTransaction extends AsyncTask<Void, Void, List<BEANPettyCashTransaction>> {
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
        protected List<BEANPettyCashTransaction> doInBackground(Void... voids) {
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

            return m_lsMethodParameters;
        }

        protected void onPostExecute(List<BEANPettyCashTransaction> result) {
            super.onPostExecute(result);
            prgBar.setVisibility(View.GONE);
            try {
                delegate.processFinish(result);

            } catch (Exception e) {
                e.printStackTrace();
            }
            finish();
            NewPettyCashTransactionActivity newPettyCashTransactionActivity = (NewPettyCashTransactionActivity) m_context;
            NavUtils.navigateUpFromSameTask(newPettyCashTransactionActivity);
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

            strNote = strNote.replace("'", "''");

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

            AsyncTask<Void, Void, List<BEANPettyCashTransaction>> asyncNewPettyCashTransaction =
                    new AsyncNewPettyCashTransaction("PettyCash",
                            "createNewTransaction", lsPettyCash);
            ((AsyncNewPettyCashTransaction) asyncNewPettyCashTransaction).delegate = this;
            asyncNewPettyCashTransaction.execute();

        }
    }

    @Override
    public void processFinish(List<BEANPettyCashTransaction> lsBEANPettyCashTransaction) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("BEANPettyCashTransaction", lsBEANPettyCashTransaction.get(0));
        setResult(Activity.RESULT_OK, intent);

    }

}
