package com.kita.pettycash;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import com.kitap.lib.bean.BEANPettyCashTransaction;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<PettyCashTransactionHolder> implements Filterable {

    Context m_context;
    List<BEANPettyCashTransaction> m_lsBEANPettyCashTransactions, m_lsFilter;
    CustomFilter filter;

    public MyAdapter(Context p_context, List<BEANPettyCashTransaction> p_lsBEANPettyCashTransactions){
        m_context = p_context;
        m_lsBEANPettyCashTransactions = p_lsBEANPettyCashTransactions;
        m_lsFilter = p_lsBEANPettyCashTransactions;
    }

    @NonNull
    @Override
    public PettyCashTransactionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.model, null);

        PettyCashTransactionHolder holder = new PettyCashTransactionHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final PettyCashTransactionHolder holder, int position) {

        SharedPreferences sharedPreferences = m_context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");

        if(m_lsBEANPettyCashTransactions.get(position).getUsernamePayer().equals(username)) {
            holder.m_txt_otherUsername.setText(m_lsBEANPettyCashTransactions.get(position).getUsernamePayer());
            holder.m_txt_userType.setText(m_context.getString(R.string.payer));

        } else if (m_lsBEANPettyCashTransactions.get(position).getUsernamePayee().equals(username)) {
            holder.m_txt_otherUsername.setText(m_lsBEANPettyCashTransactions.get(position).getUsernamePayee());
            holder.m_txt_userType.setText(m_context.getString(R.string.payee));

        }

        holder.m_txt_openingBalance.setText(String.valueOf(m_lsBEANPettyCashTransactions.get(position).getAmount()));
        holder.m_txt_note.setText(m_lsBEANPettyCashTransactions.get(position).getNote());

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(View view, int pos) {
                Toast.makeText(m_context, "Item clicked", Toast.LENGTH_SHORT).show(); //to do
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    public int getItemCount() {
        if(m_lsBEANPettyCashTransactions == null) {
            return 0;
        } else return m_lsBEANPettyCashTransactions.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null) {
            filter = new CustomFilter(m_lsFilter, this);
        }

        return filter;
    }
}
