package com.kita.pettycash;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class PettyCashTransactionHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    TextView m_txt_userType;
    TextView m_txt_otherUsername;
    TextView m_txt_openingBalance;
    TextView m_txt_note;

    ItemClickListener m_itemClickListener;

    public PettyCashTransactionHolder(@NonNull View itemView) {
        super(itemView);

        m_txt_userType = itemView.findViewById(R.id.txt_userType);
        m_txt_otherUsername = itemView.findViewById(R.id.txt_otherUsername);
        m_txt_openingBalance = itemView.findViewById(R.id.txt_openingBalance);
        m_txt_note = itemView.findViewById(R.id.txt_note);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        m_itemClickListener.onItemClick(v, getLayoutPosition());

    }

    public void setItemClickListener(ItemClickListener ic) {
        m_itemClickListener = ic;
    }
}
