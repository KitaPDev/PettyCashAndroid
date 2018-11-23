package com.kita.pettycash;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class HomeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    TextView m_txtUserType;
    TextView m_txtOtherUsername;
    TextView m_txtOpeningBalance;
    TextView m_txtNote;
    CheckBox m_chkSelect;

    HomeActivity homeActivity;

    ItemClickListener m_itemClickListener;

    public HomeHolder(@NonNull View itemView) {
        super(itemView);

        m_txtUserType = itemView.findViewById(R.id.txt_userType);
        m_txtOtherUsername = itemView.findViewById(R.id.txt_otherUsername);
        m_txtOpeningBalance = itemView.findViewById(R.id.txt_openingBalance);
        m_txtNote = itemView.findViewById(R.id.txt_note);
        m_chkSelect = itemView.findViewById(R.id.chk_item);
        m_chkSelect.setOnClickListener(this);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        m_itemClickListener.onItemClick(v, getLayoutPosition());
        homeActivity.prepareSelection(v, getAdapterPosition());

    }

    public void setItemClickListener(ItemClickListener ic) {
        m_itemClickListener = ic;
    }
}
