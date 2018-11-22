package com.kita.pettycash;

import android.widget.Filter;

import com.kitap.lib.bean.BEANPettyCashTransaction;

import java.util.ArrayList;
import java.util.List;

public class CustomFilter extends Filter {

    MyAdapter m_adapter;
    List<BEANPettyCashTransaction> m_lsFilter;

    public CustomFilter(List<BEANPettyCashTransaction> p_lsFilter, MyAdapter p_adapter) {
        m_adapter = p_adapter;
        m_lsFilter = p_lsFilter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults filterResults = new FilterResults();

        if(constraint != null && constraint.length() > 0) {
            constraint = constraint.toString().toUpperCase();

            ArrayList<BEANPettyCashTransaction> filteredTransaction = new ArrayList<>();

            for(int i = 0; i < filteredTransaction.size(); i++) {

                if(m_lsFilter.get(i).getUsernamePayee().toUpperCase().contains(constraint)) {
                    filteredTransaction.add(m_lsFilter.get(i));

                } else if(m_lsFilter.get(i).getUsernamePayer().toUpperCase().contains(constraint)) {
                    filteredTransaction.add(m_lsFilter.get(i));

                }
            }

            filterResults.count = filteredTransaction.size();
            filterResults.values = filteredTransaction;

        } else {
            filterResults.count = m_lsFilter.size();
            filterResults.values = m_lsFilter;

        }

        return filterResults;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults filterResults) {
        m_adapter.m_lsBEANPettyCashTransactions = (ArrayList<BEANPettyCashTransaction>) filterResults.values;

        m_adapter.notifyDataSetChanged();
    }
}
