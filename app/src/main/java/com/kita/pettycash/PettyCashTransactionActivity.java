package com.kita.pettycash;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kita.androidlib.client.AsyncExecuteMethod;
import com.kita.androidlib.client.AsyncRetrieveList;
import com.kita.pettycash.client.interfaces.AsyncResponse;
import com.kitap.lib.bean.BEANPettyCashTransaction;
import com.kitap.lib.bean.BEANPettyCashUsage;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PettyCashTransactionActivity extends AppCompatActivity implements AsyncResponse {

    private static String HOST = "10.0.2.2";
    private static int PORT = 45678;

    Context m_context;
    BEANPettyCashTransaction m_BEANPettyCashTransaction;
    List<BEANPettyCashUsage> m_lsBEANPettyCashUsages;

    private String m_strUsername;

    private UsageAdapter m_adapter;
    private RecyclerView m_recyclerView;

    private FloatingActionButton m_fab;
    private ProgressBar m_prgBar;

    private String m_strNewUsage;
    private BigDecimal m_bdNewUsageAmount;

    private BigDecimal m_bdInitialBalance;
    private BigDecimal m_bdTotalExpenditure;
    private BigDecimal m_bdOutstandingBalance;
    private ImageButton m_imgbtnEvidence;

    TextView m_txtStatus;
    Button m_btnReturn;
    Button m_btnReceived;

    int TAKE_PHOTO_CODE = 0;
    public static int captureCount = 0;

    final String photoDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            + "/pettycash_images";

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pettycashtransaction);

        m_context = this;
        m_prgBar = findViewById(R.id.prgBarTransaction);

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        m_strUsername = sharedPreferences.getString("username", "");

        Intent intent = getIntent();
        m_BEANPettyCashTransaction = (BEANPettyCashTransaction) intent.getSerializableExtra("BEANPettyCashTransaction");
        String strUserType = intent.getStringExtra("userType");

        Toolbar tbTransaction = findViewById(R.id.tb_transaction);

        if(strUserType.equals("Payer")) tbTransaction.setTitle(m_BEANPettyCashTransaction.getUsernamePayee().toUpperCase());
        else if(strUserType.equals("Payee")) tbTransaction.setTitle(m_BEANPettyCashTransaction.getUsernamePayer().toUpperCase());

        setSupportActionBar(tbTransaction);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        m_txtStatus = findViewById(R.id.txt_transactionStatus);
        TextView txtInitialBalance = findViewById(R.id.txt_initialBalance);
        final TextView txtOutstandingBalance = findViewById(R.id.txt_outstandingBalance);
        final TextView txtTotalExpenditure = findViewById(R.id.txt_totalExpenditure);

        try {
            m_adapter = new UsageAdapter(this, getPettyCashUsages());

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        m_bdInitialBalance = m_BEANPettyCashTransaction.getAmount();
        m_bdTotalExpenditure = m_adapter.getTotalExpenditure();
        m_bdOutstandingBalance = m_bdInitialBalance.subtract(m_bdTotalExpenditure);

        m_recyclerView = findViewById(R.id.recyclerViewUsage);
        m_recyclerView.setLayoutManager(new LinearLayoutManager(this));
        m_recyclerView.setItemAnimator(new DefaultItemAnimator());
        m_recyclerView.setAdapter(m_adapter);

        m_fab = findViewById(R.id.fab_newUsage);
        m_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
                builder.setTitle("New Usage");

                View viewInflated = LayoutInflater.from(m_context).inflate(R.layout.dialog_new_usage,
                        null);
                builder.setView(viewInflated);

                final EditText edtUsage = viewInflated.findViewById(R.id.edt_usage);
                final EditText edtUsageAmount = viewInflated.findViewById(R.id.edt_usageAmount);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if(edtUsage.getText().length() == 0 || edtUsageAmount.getText().length() == 0) {
                            Toast.makeText(m_context, "Invalid Inputs: Usage and Amount must contain values",
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            m_strNewUsage = edtUsage.getText().toString();
                            m_bdNewUsageAmount = new BigDecimal(edtUsageAmount.getText().toString());

                            if(m_bdNewUsageAmount.compareTo(m_bdOutstandingBalance) > 0) {
                                Toast.makeText(m_context, "Error! Usage Amount exceeds Outstanding Balance!",
                                        Toast.LENGTH_LONG).show();

                            } else {
                                BEANPettyCashUsage beanPettyCashUsage = new BEANPettyCashUsage();
                                beanPettyCashUsage.setPettyCashID(m_BEANPettyCashTransaction.getPettyCashID());
                                beanPettyCashUsage.setUsage(m_strNewUsage);
                                beanPettyCashUsage.setUsageAmount(m_bdNewUsageAmount);
                                beanPettyCashUsage.setDateTimeUsage(new Timestamp(System.currentTimeMillis()));

                                List<BEANPettyCashUsage> lsBEANPettyCashUsages = new ArrayList<>();
                                lsBEANPettyCashUsages.add(beanPettyCashUsage);

                                AsyncTask<Void, Void, Void> asyncNewUsage = new AsyncExecuteMethod(HOST,
                                        PORT, "PettyCashUsage", "createNewUsage",
                                        lsBEANPettyCashUsages);
                                ((AsyncExecuteMethod) asyncNewUsage).setProgressbar(m_prgBar);
                                try {
                                    asyncNewUsage.execute().get();
                                    m_adapter.updateUsagesList(getPettyCashUsages());

                                    m_bdTotalExpenditure = m_adapter.getTotalExpenditure();
                                    m_bdOutstandingBalance = m_bdInitialBalance.subtract(m_bdTotalExpenditure);
                                    txtTotalExpenditure.setText(m_bdTotalExpenditure.toString());
                                    txtOutstandingBalance.setText(m_bdOutstandingBalance.toString());

                                } catch (ExecutionException | InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                builder.show();
            }
        });

        m_btnReturn = findViewById(R.id.btn_return);
        m_btnReceived = findViewById(R.id.btn_received);

        if(m_BEANPettyCashTransaction.IsReturned()) {
            m_txtStatus.setText(getString(R.string.returned).toUpperCase());
            m_btnReturn.setBackgroundColor(Color.parseColor("#D3D3D3"));

            if(strUserType.equals("Payer")) {
                m_btnReturn.setVisibility(View.INVISIBLE);
                m_fab.setVisibility(View.INVISIBLE);

            } else if(strUserType.equals("Payee")) {
                m_btnReceived.setVisibility(View.INVISIBLE);
                m_fab.setVisibility(View.INVISIBLE);
                m_btnReceived.setClickable(false);

            } else m_fab.setVisibility(View.VISIBLE);

        } else if(m_BEANPettyCashTransaction.IsReceived()) {
            m_txtStatus.setText(getString(R.string.received).toUpperCase());
            m_btnReceived.setBackgroundColor(Color.parseColor("#C0C0C0"));
            m_btnReceived.setClickable(false);
            m_fab.setVisibility(View.INVISIBLE);

            if(strUserType.equals("Payer")) {
                m_btnReturn.setVisibility(View.INVISIBLE);

            } else if(strUserType.equals("Payee")) {
                m_btnReceived.setVisibility(View.INVISIBLE);
                m_btnReturn.setVisibility(View.VISIBLE);
                m_btnReturn.setBackgroundColor(Color.parseColor("#D3D3D3"));
                m_btnReturn.setClickable(false);

            }

        } else {
            m_txtStatus.setText(getString(R.string.pending).toUpperCase());
            if(strUserType.equals("Payer")) {
                m_btnReturn.setVisibility(View.INVISIBLE);
                m_fab.setVisibility(View.INVISIBLE);

            }
            if(strUserType.equals("Payee")) {
                m_btnReceived.setVisibility(View.INVISIBLE);
                m_fab.setVisibility(View.VISIBLE);
            }
        }

        txtInitialBalance.setText(m_bdInitialBalance.toString());
        txtTotalExpenditure.setText(m_bdTotalExpenditure.toString());
        txtOutstandingBalance.setText(m_bdOutstandingBalance.toString());
    }

    public void onResume() {
        super.onResume();
        m_adapter.notifyDataSetChanged();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case android.R.id.home:
                finish();
                PettyCashTransactionActivity pettyCashTransactionActivity = (PettyCashTransactionActivity) m_context;
                NavUtils.navigateUpFromSameTask(pettyCashTransactionActivity);
        }
        return true;
    }

    private List<BEANPettyCashUsage> getPettyCashUsages() throws ExecutionException, InterruptedException {
        m_prgBar.setVisibility(View.VISIBLE);

        List<String> lsPettyCashIDs = new ArrayList<>();
        lsPettyCashIDs.add(String.valueOf(m_BEANPettyCashTransaction.getPettyCashID()));

        AsyncTask<Void, Void, List> asyncGetPettyCashUsages = new AsyncRetrieveList(HOST, PORT, m_context,
                "PettyCashUsage", "getPettyCashUsages", lsPettyCashIDs);
        ((AsyncRetrieveList) asyncGetPettyCashUsages).delegate = this;
        ((AsyncRetrieveList) asyncGetPettyCashUsages).setProgressbar(m_prgBar);

        m_lsBEANPettyCashUsages = asyncGetPettyCashUsages.execute().get();

        return m_lsBEANPettyCashUsages;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == TAKE_PHOTO_CODE && resultCode == RESULT_OK) {
            Toast.makeText(m_context, "Picture saved!", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("RestrictedApi")
    public void onClickReturn(View view) {
        m_BEANPettyCashTransaction.setUsernameReturn(m_strUsername);
        m_txtStatus.setText(getString(R.string.returned).toUpperCase());

        List<BEANPettyCashTransaction> lsBEANPettyCashTransaction = new ArrayList<>();
        lsBEANPettyCashTransaction.add(m_BEANPettyCashTransaction);

        AsyncTask<Void, Void, Void> asyncUpdatePettyCashTransaction =
                new AsyncExecuteMethod(HOST, PORT, "PettyCash", "setUserReturn",
                        lsBEANPettyCashTransaction);
        ((AsyncExecuteMethod) asyncUpdatePettyCashTransaction).setProgressbar(m_prgBar);
        asyncUpdatePettyCashTransaction.execute();

        m_btnReturn.setBackgroundColor(Color.parseColor("#D3D3D3"));
        m_fab.setVisibility(View.INVISIBLE);
    }

    @SuppressLint("RestrictedApi")
    public void onClickReceived(View view) {
        m_BEANPettyCashTransaction.setUsernameReturnReceived(m_strUsername);
        m_txtStatus.setText(getString(R.string.received).toUpperCase());

        List<BEANPettyCashTransaction> lsBEANPettyCashTransaction = new ArrayList<>();
        lsBEANPettyCashTransaction.add(m_BEANPettyCashTransaction);

        AsyncTask<Void, Void, Void> asyncUpdatePettyCashTransaction =
                new AsyncExecuteMethod(HOST, PORT, "PettyCash", "setUserReturnReceived",
                        lsBEANPettyCashTransaction);
        ((AsyncExecuteMethod) asyncUpdatePettyCashTransaction).setProgressbar(m_prgBar);
        asyncUpdatePettyCashTransaction.execute();

        m_btnReceived.setBackgroundColor(Color.parseColor("#C0C0C0"));
        m_btnReceived.setClickable(false);
        m_btnReturn.setVisibility(View.INVISIBLE);
        m_fab.setVisibility(View.INVISIBLE);
    }

//    public void onClickCamera(View view) {
//        captureCount++;
//
//        String file = photoDir + ".jpg";
//        File newFile = new File(file);
//
//        try {
//            newFile.createNewFile();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        Uri outputFileUri = Uri.fromFile(newFile);
//
//        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
//
//        startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
//    }

    @Override
    public void processFinish(Object output) throws Exception {

    }

    @Override
    public void processFinish(List<BEANPettyCashTransaction> lsBEANPettyCashTransaction) throws Exception {

    }
}
