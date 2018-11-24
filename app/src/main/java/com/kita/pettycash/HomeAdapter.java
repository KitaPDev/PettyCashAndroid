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

public class HomeAdapter extends RecyclerView.Adapter<HomeHolder> implements Filterable {

    Context m_context;
    List<BEANPettyCashTransaction> m_lsBEANPettyCashTransactions, m_lsFilter;
    CustomFilter filter;

    HomeActivity m_homeActivity;

    public HomeAdapter(Context p_context, List<BEANPettyCashTransaction> p_lsBEANPettyCashTransactions){
        m_context = p_context;
        m_lsBEANPettyCashTransactions = p_lsBEANPettyCashTransactions;
        m_lsFilter = p_lsBEANPettyCashTransactions;

        m_homeActivity = (HomeActivity) p_context;
    }

    public void updateAdapter(List<BEANPettyCashTransaction> p_lsBEANPettyCashTransactions){
        for (BEANPettyCashTransaction beanPettyCashTransaction : p_lsBEANPettyCashTransactions){
            m_lsBEANPettyCashTransactions.remove(beanPettyCashTransaction);

        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HomeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.model, null);

        HomeHolder holder = new HomeHolder(view, m_context);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final HomeHolder holder, int position) {

        SharedPreferences sharedPreferences = m_context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");

        if(m_lsBEANPettyCashTransactions.get(position).getUsernamePayer().equals(username)) {
            holder.m_txtOtherUsername.setText(m_lsBEANPettyCashTransactions.get(position).getUsernamePayee());
            holder.m_txtUserType.setText(m_context.getString(R.string.payer));

        } else if (m_lsBEANPettyCashTransactions.get(position).getUsernamePayee().equals(username)) {
            holder.m_txtOtherUsername.setText(m_lsBEANPettyCashTransactions.get(position).getUsernamePayer());
            holder.m_txtUserType.setText(m_context.getString(R.string.payee));

        }

        if(!m_homeActivity.isInSelection) {
            holder.m_chkSelect.setVisibility(View.GONE);

        } else {
            holder.m_chkSelect.setVisibility(View.VISIBLE);
            holder.m_chkSelect.setChecked(false);
        }

        holder.m_txtOpeningBalance.setText(String.valueOf(m_lsBEANPettyCashTransactions.get(position).getAmount()));
        holder.m_txtNote.setText(m_lsBEANPettyCashTransactions.get(position).getNote());

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(View view, int pos) {
                 //todo
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(m_context, "Item clicked", Toast.LENGTH_SHORT).show();
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
