package com.kita.pettycash;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.kita.androidlib.client.AsyncRetrieveList;
import com.kita.androidlib.client.AsyncRetrieveObject;
import com.kita.pettycash.client.interfaces.AsyncResponse;
import com.kitap.lib.bean.BEANPettyCashTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HomeActivity extends AppCompatActivity implements AsyncResponse {

    private static String HOST = "10.0.2.2";
    private static int PORT = 45678;

    private ProgressBar prgBar;

    private String m_strUsername = null;
    private Context m_context = this;

    private MyAdapter m_adapter;
    private RecyclerView m_recyclerView;

    private List<BEANPettyCashTransaction> m_lsBEANPettyCashTransactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        prgBar = findViewById(R.id.prgBarHome);

        m_context = this;

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");

        m_strUsername = username;

        if(username.equals("")) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();

        } else {
            Toolbar toolbarHome = findViewById(R.id.tb_home);
            toolbarHome.setTitle("");
            setSupportActionBar(toolbarHome);

            toolbarHome.setTitle(m_strUsername.toUpperCase());

            android.support.v7.widget.SearchView searchView = findViewById(R.id.mySearchView);

            try {

                m_adapter = new MyAdapter(this, getPettyCashTransactions());

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

            m_recyclerView = findViewById(R.id.myRecyclerView);

            m_recyclerView.setLayoutManager(new LinearLayoutManager(this));
            m_recyclerView.setItemAnimator(new DefaultItemAnimator());

            m_recyclerView.setAdapter(m_adapter);

            searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    m_adapter.getFilter().filter(query);
                    return false;
                }
            });

            FloatingActionButton fab = findViewById(R.id.fab_newTransaction);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(m_context, NewPettyCashTransactionActivity.class);
                    startActivity(intent);
                }
            });

        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case R.id.action_select:

                //todo prime

                return true;

            case R.id.action_logout:
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);

                dlgAlert.setTitle("Logout");
                dlgAlert.setMessage("Are you sure you want to log out?");
                dlgAlert.setPositiveButton("Yes", null);
                dlgAlert.setCancelable(true);

                dlgAlert.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.clear();
                                editor.apply();

                                Intent intent = new Intent(m_context, LoginActivity.class);
                                startActivity(intent);
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

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private List<BEANPettyCashTransaction> getPettyCashTransactions() throws ExecutionException, InterruptedException {
        prgBar.setVisibility(View.INVISIBLE);

        List<String> lsUsername = new ArrayList<>();
        lsUsername.add(m_strUsername);

        AsyncTask<Void, Void, List> asyncGetPettyCashTransactions = new AsyncRetrieveList(HOST, PORT, m_context,
                "PettyCash", "getPettyCashTransactions", lsUsername);
        ((AsyncRetrieveList) asyncGetPettyCashTransactions).delegate = this;
        ((AsyncRetrieveList) asyncGetPettyCashTransactions).setProgressbar(prgBar);

        return asyncGetPettyCashTransactions.execute().get();
    }

    @Override
    public void processFinish(List<BEANPettyCashTransaction> lsBEANPettyCashTransaction) { }

    public void processFinish(Object p_objBEANPettyCashTransactions) {

    }
}
