package com.kita.pettycash;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kita.androidlib.client.AsyncExecuteMethod;
import com.kita.androidlib.client.AsyncRetrieveList;
import com.kita.pettycash.client.interfaces.AsyncResponse;
import com.kitap.lib.bean.BEANPettyCashTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class HomeActivity extends AppCompatActivity implements AsyncResponse, SearchView.OnQueryTextListener {

    private static String HOST = "10.0.2.2";
    private static int PORT = 45678;

    private static final int NEW_TRANSACTION_REQUEST = 123;

    private ProgressBar prgBar;

    private String m_strUsername = null;
    private Context m_context = this;

    private HomeAdapter m_adapter;

    private List<BEANPettyCashTransaction> m_lsBEANPettyCashTransactions = new ArrayList<>();

    Toolbar m_tbHome;
    FloatingActionButton m_fab;
    boolean isInSelection;
    TextView txtCounter;

    List<BEANPettyCashTransaction> m_lsSelected = new ArrayList<>();

    int counter = 0;

    private FragmentTabAdapter m_tabAdapter;
    private TabLayout m_tabLayout;
    public ViewPager m_viewPager;

    PendingTransactionsFragment m_pendingTransactionsFragment;
    ReturnedTransactionsFragment m_returnedTransactionsFragment;
    ReceivedTransactionsFragment m_receivedTransactionsFragment;

    int m_currentFragment = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        prgBar = findViewById(R.id.prgBarHome);

        txtCounter = findViewById(R.id.txt_counter);
        txtCounter.setVisibility(View.GONE);

        m_context = this;

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");

        m_strUsername = username;

        if(username.equals("")) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();

        } else {
            m_tbHome = findViewById(R.id.tb_home);
            m_tbHome.setTitle("");
            setSupportActionBar(m_tbHome);

            m_tbHome.setTitle(m_strUsername.toUpperCase());

        try {
            m_adapter = new HomeAdapter(this, getPettyCashTransactions());

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        m_viewPager = findViewById(R.id.homeViewPager);
        m_tabLayout = findViewById(R.id.homeTabLayout);

        m_tabAdapter = new FragmentTabAdapter(getSupportFragmentManager());

        m_pendingTransactionsFragment = new PendingTransactionsFragment();
        m_returnedTransactionsFragment = new ReturnedTransactionsFragment();
        m_receivedTransactionsFragment = new ReceivedTransactionsFragment();
        m_tabAdapter.addFragment(m_pendingTransactionsFragment, "Pending");
        m_tabAdapter.addFragment(m_returnedTransactionsFragment, "Returned");
        m_tabAdapter.addFragment(m_receivedTransactionsFragment, "Received");

        m_tabLayout.setTabTextColors(Color.parseColor("#000000"), Color.parseColor("#FFFFFF"));

        m_viewPager.setAdapter(m_tabAdapter);
        m_viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(m_tabLayout));
        m_tabLayout.setupWithViewPager(m_viewPager);
        m_tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                m_viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

            m_fab = findViewById(R.id.fab_newTransaction);
            m_fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(m_context, NewPettyCashTransactionActivity.class);
                    intent.putExtra("FragmentNumber", m_viewPager.getCurrentItem());
                    startActivityForResult(intent, NEW_TRANSACTION_REQUEST);
                }
            });
        }
        if(m_viewPager != null) m_viewPager.setCurrentItem(m_currentFragment);
    }

    public void onPause() {
        super.onPause();
        if(m_viewPager != null) m_currentFragment = m_viewPager.getCurrentItem();
    }

    public void onResume() {
        super.onResume();
        m_tabAdapter.notifyDataSetChanged();
        m_viewPager.setCurrentItem(m_currentFragment);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_home, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @SuppressLint("RestrictedApi")
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                counter = 0;

                if (isInSelection) {
                    clearActionMode();
                    m_adapter.notifyDataSetChanged();
                    m_tbHome.setTitle(m_strUsername.toUpperCase());

                }
                return true;

            case R.id.action_select:
                m_tbHome.getMenu().clear();

                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) m_fab.getLayoutParams();
                params.setAnchorId(View.NO_ID);

                m_fab.setLayoutParams(params);
                m_fab.setVisibility(View.INVISIBLE);
                m_tbHome.inflateMenu(R.menu.menu_selection);

                txtCounter.setVisibility(View.INVISIBLE);
                isInSelection = true;
                notifyFragmentAdapter();

                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle(txtCounter.getText().toString());

                m_tbHome.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clearActionMode();
                        notifyFragmentAdapter();
                    }
                });

                return true;

            case R.id.action_logout:
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);

                dlgAlert.setTitle("Logout");
                dlgAlert.setMessage("Are you sure you want to log out?");
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

            case R.id.action_delete:
                dlgAlert = new AlertDialog.Builder(this);

                dlgAlert.setTitle("Delete");
                dlgAlert.setMessage("Are you sure you want to delete " + counter + " transactions?");
                dlgAlert.setPositiveButton("Yes", null);
                dlgAlert.setCancelable(true);

                dlgAlert.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean isPayee = false;
                                for(BEANPettyCashTransaction beanPettyCashTransaction : m_lsSelected) {
                                    if(m_strUsername.equals(beanPettyCashTransaction.getUsernamePayee())) {
                                        isPayee = true;
                                        break;
                                    }
                                }

                                if(isPayee) {
                                    AlertDialog.Builder erDlgAlert = new AlertDialog.Builder(m_context);

                                    erDlgAlert.setTitle("Error");
                                    erDlgAlert.setMessage("Only Payer can delete transactions!");

                                    erDlgAlert.setPositiveButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            });

                                    erDlgAlert.create().show();

                                } else {
                                    List<BEANPettyCashTransaction> lsBEANPettyCashTransaction = new ArrayList<>();
                                    for(int i = 0; i < m_lsSelected.size(); i++) {
                                        lsBEANPettyCashTransaction.add(m_lsSelected.get(i));

                                        AsyncTask<Void, Void, Void> asyncDeleteTransactions = new AsyncExecuteMethod(HOST,
                                                PORT, "PettyCash", "deletePettyCashTransaction",
                                                lsBEANPettyCashTransaction);
                                        ((AsyncExecuteMethod) asyncDeleteTransactions).setProgressbar(prgBar);

                                        try {
                                            asyncDeleteTransactions.execute().get();

                                        } catch (ExecutionException | InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        lsBEANPettyCashTransaction.clear();
                                    }
                                    deleteFromFragmentDataSet(m_lsSelected);
                                    clearActionMode();
                                    notifyFragmentAdapter();
                                }
                            }
                        });

                dlgAlert.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                notifyFragmentAdapter();
                                clearActionMode();
                            }
                        });
                dlgAlert.create().show();


                return true;

            case R.id.home:
                clearActionMode();
                notifyFragmentAdapter();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        query = query.toLowerCase();

        List<BEANPettyCashTransaction> searchedList = new ArrayList<>();
        List<BEANPettyCashTransaction> lsBEANPettyCashTransactions = getFragmentTransactions();

        for (BEANPettyCashTransaction beanPettyCashTransaction : lsBEANPettyCashTransactions){
            String strUsernamePayee = beanPettyCashTransaction.getUsernamePayee().toLowerCase();
            String strUsernamePayer = beanPettyCashTransaction.getUsernamePayer().toLowerCase();
            String strNote = beanPettyCashTransaction.getNote().toLowerCase();

            if (strUsernamePayee.contains(query) || strUsernamePayer.contains(query) || strNote.contains(query)){
                searchedList.add(beanPettyCashTransaction);

            }
        }
        getCurrentAdapter().setFilter(searchedList);

        return true;
    }

    public List<BEANPettyCashTransaction> getPettyCashTransactions() throws ExecutionException, InterruptedException {
        prgBar.setVisibility(View.INVISIBLE);

        List<String> lsUsername = new ArrayList<>();
        lsUsername.add(m_strUsername);

        AsyncTask<Void, Void, List> asyncGetPettyCashTransactions = new AsyncRetrieveList(HOST, PORT, m_context,
                "PettyCash", "getPettyCashTransactions", lsUsername);
        ((AsyncRetrieveList) asyncGetPettyCashTransactions).delegate = this;
        ((AsyncRetrieveList) asyncGetPettyCashTransactions).setProgressbar(prgBar);

        m_lsBEANPettyCashTransactions = asyncGetPettyCashTransactions.execute().get();

        return m_lsBEANPettyCashTransactions;
    }

    public List<BEANPettyCashTransaction> getListPettyCashTransactions() throws ExecutionException, InterruptedException { return getPettyCashTransactions(); }

    public void prepareSelection(View view, int position) {
        List<BEANPettyCashTransaction> lsBEANPettyCashTransactions = getFragmentTransactions();

        if (((CheckBox) view).isChecked()) {
            m_lsSelected.add(lsBEANPettyCashTransactions.get(position));
            counter++;
            updateCounter(counter);

        } else {
            if (counter > 0) {
                m_lsSelected.remove(lsBEANPettyCashTransactions.get(position));
                counter--;
                updateCounter(counter);
            }
        }
    }

    public void updateCounter(final int counter) {
//        final String strCounter;
//        if (counter == 0 || counter == 1) {
//            strCounter = counter + " item selected";
//            txtCounter.setText(strCounter);
//
//        } else {
//            strCounter = counter + " items selected";
//            txtCounter.setText(strCounter);
//        }
//        this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                txtCounter.setText(strCounter);
//            }
//        });
    }

    @SuppressLint("RestrictedApi")
    public void clearActionMode() {
        isInSelection = false;

        m_tbHome.getMenu().clear();
        m_tbHome.inflateMenu(R.menu.menu_home);
        m_tbHome.setTitle(m_strUsername.toUpperCase());
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        txtCounter.setVisibility(View.INVISIBLE);

        CoordinatorLayout.LayoutParams param = (CoordinatorLayout.LayoutParams) m_fab.getLayoutParams();
        param.setAnchorId(View.NO_ID);
        m_fab.setLayoutParams(param);
        m_fab.setVisibility(View.VISIBLE);

        counter = 0;
        m_lsSelected.clear();
    }

    public void onBackPressed() {
        counter = 0;

        if (isInSelection) {
            clearActionMode();
            notifyFragmentAdapter();
            m_tbHome.setTitle(m_strUsername.toUpperCase());

        } else {
            super.onBackPressed();

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_TRANSACTION_REQUEST) {

            if (resultCode == RESULT_OK) {
                m_adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void processFinish(List<BEANPettyCashTransaction> lsBEANPettyCashTransaction) {}

    @Override
    public void processFinish(Object p_objBEANPettyCashTransactions) {}

    public HomeAdapter getCurrentAdapter() {
        switch(m_viewPager.getCurrentItem()) {
            case 0:
                return m_pendingTransactionsFragment.getAdapter();
            case 1:
                return m_returnedTransactionsFragment.getAdapter();
            case 2:
                return m_receivedTransactionsFragment.getAdapter();
            default:
                return null;
        }
    }

    public List<BEANPettyCashTransaction> getFragmentTransactions() {
        List<BEANPettyCashTransaction> lsBEANPettyCashTransactions = new ArrayList<>();
        switch(m_viewPager.getCurrentItem()){
            case 0:
                try {
                    lsBEANPettyCashTransactions = m_pendingTransactionsFragment.getPendingTransactions();

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case 1:
                try {
                    lsBEANPettyCashTransactions = m_returnedTransactionsFragment.getReturnedTransactions();

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                try {
                    lsBEANPettyCashTransactions = m_receivedTransactionsFragment.getReceivedTransactions();

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                break;
        }
        return lsBEANPettyCashTransactions;
    }

    public void deleteFromFragmentDataSet(List<BEANPettyCashTransaction> p_lsBEANPettyCashTransactions) {
        switch(m_viewPager.getCurrentItem()) {
            case 0:
                m_pendingTransactionsFragment.getAdapter().deleteFromDataSet(p_lsBEANPettyCashTransactions);
                break;
            case 1:
                m_returnedTransactionsFragment.getAdapter().deleteFromDataSet(p_lsBEANPettyCashTransactions);
                break;
            case 2:
                m_receivedTransactionsFragment.getAdapter().deleteFromDataSet(p_lsBEANPettyCashTransactions);
                break;
        }
    }

    public void notifyFragmentAdapter() {
        switch(m_viewPager.getCurrentItem()) {
            case 0:
                m_pendingTransactionsFragment.getAdapter().notifyDataSetChanged();
                break;
            case 1:
                m_returnedTransactionsFragment.getAdapter().notifyDataSetChanged();
                break;
            case 2:
                m_receivedTransactionsFragment.getAdapter().notifyDataSetChanged();
                break;
        }
    }
}
