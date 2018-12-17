package com.kita.pettycash;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class UsageHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    TextView m_txtUsage;
    TextView m_txtExpenditure;
    TextView m_txtDateTimeUsage;
    ImageButton m_imgbtnEvidence;

    Context m_context;

    ItemClickListener m_itemClickListener;

    public UsageHolder(@NonNull View itemView, Context p_context) {
        super(itemView);

        m_context = p_context;

        m_txtUsage = itemView.findViewById(R.id.txt_usage);
        m_txtExpenditure = itemView.findViewById(R.id.txt_expenditure);
        m_txtDateTimeUsage = itemView.findViewById(R.id.txt_dtUsage);
//        m_imgbtnEvidence = itemView.findViewById(R.id.ibtn_Evidence);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {}

    public void setItemClickListener(ItemClickListener ic) {
        m_itemClickListener = ic;
    }

}
