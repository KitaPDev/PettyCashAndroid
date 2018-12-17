package com.kita.pettycash;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kitap.lib.bean.BEANPettyCashTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ReceivedTransactionsFragment extends Fragment {

    private HomeAdapter m_adapter;
    private RecyclerView m_recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_received_transactions, container, false);

        try {
            m_adapter = new HomeAdapter(getActivity(), getReceivedTransactions());

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        if(getActivity() != null) {
            m_recyclerView = view.findViewById(R.id.recyclerViewReceived);
            m_recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            m_recyclerView.setItemAnimator(new DefaultItemAnimator());
            m_recyclerView.setAdapter(m_adapter);

        }

        return view;
    }

    public List<BEANPettyCashTransaction> getReceivedTransactions() throws ExecutionException, InterruptedException {
        List<BEANPettyCashTransaction> lsReceivedTransactions = new ArrayList<>();
        HomeActivity homeActivity = (HomeActivity) getActivity();

        for(BEANPettyCashTransaction beanPettyCashTransaction : homeActivity.getListPettyCashTransactions()) {
            if(beanPettyCashTransaction.IsReceived()) {
                lsReceivedTransactions.add(beanPettyCashTransaction);
            }
        }
        return lsReceivedTransactions;
    }

    public HomeAdapter getAdapter() { return m_adapter; }
    public void setAdapter(HomeAdapter p_adapter) { m_adapter = p_adapter; }
}
