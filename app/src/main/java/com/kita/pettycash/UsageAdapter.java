package com.kita.pettycash;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kitap.lib.bean.BEANPettyCashUsage;

import java.math.BigDecimal;
import java.util.List;

public class UsageAdapter extends RecyclerView.Adapter<UsageHolder> {

    Context m_context;
    List<BEANPettyCashUsage> m_lsBEANPettyCashUsages;

    PettyCashTransactionActivity m_pettyCashTransactionActivity;

    public UsageAdapter(Context p_context, List<BEANPettyCashUsage> p_lsBEANPettyCashUsages) {
        m_context = p_context;
        m_lsBEANPettyCashUsages = p_lsBEANPettyCashUsages;

        m_pettyCashTransactionActivity = (PettyCashTransactionActivity) p_context;
    }

    @NonNull
    @Override
    public UsageHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.usage_model, null);

        UsageHolder holder = new UsageHolder(view, m_context);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull UsageHolder holder, int position) {

        holder.m_txtUsage.setText(m_lsBEANPettyCashUsages.get(position).getUsage());
        holder.m_txtExpenditure.setText(String.valueOf(m_lsBEANPettyCashUsages.get(position).getUsageAmount()));
        holder.m_txtDateTimeUsage.setText(String.valueOf(m_lsBEANPettyCashUsages.get(position).getDateTimeUsage()));

    }

    @Override
    public int getItemCount() {
        if(m_lsBEANPettyCashUsages == null) {
            return 0;
        } else return m_lsBEANPettyCashUsages.size();
    }

    public void updateUsagesList(List<BEANPettyCashUsage> p_lsBEANPettyCashUsages) {
        m_lsBEANPettyCashUsages.clear();
        m_lsBEANPettyCashUsages.addAll(p_lsBEANPettyCashUsages);
        this.notifyDataSetChanged();
    }

    public BigDecimal getTotalExpenditure() {
        BigDecimal bdTotalExpenditure = new BigDecimal(0);

        for(BEANPettyCashUsage beanPettyCashUsage : m_lsBEANPettyCashUsages) {
            bdTotalExpenditure = bdTotalExpenditure.add(beanPettyCashUsage.getUsageAmount());

        }
        return bdTotalExpenditure;
    }

}
