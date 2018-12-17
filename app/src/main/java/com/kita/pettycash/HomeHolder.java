package com.kita.pettycash;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import org.w3c.dom.Text;

public class HomeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    TextView m_txtUserType;
    TextView m_txtOtherUsername;
    TextView m_txtOpeningBalance;
    TextView m_txtNote;
    TextView m_txtStatus;
    CheckBox m_chkSelect;

    Context m_context;

    HomeActivity m_homeActivity;

    ItemClickListener m_itemClickListener;

    public HomeHolder(@NonNull View itemView, Context p_context) {
        super(itemView);

        m_context = p_context;

        m_txtUserType = itemView.findViewById(R.id.txt_userType);
        m_txtOtherUsername = itemView.findViewById(R.id.txt_otherUsername);
        m_txtOpeningBalance = itemView.findViewById(R.id.txt_openingBalance);
        m_txtNote = itemView.findViewById(R.id.txt_note);
        m_txtStatus = itemView.findViewById(R.id.txt_homeStatus);
        m_chkSelect = itemView.findViewById(R.id.chk_item);
        m_chkSelect.setOnClickListener(this);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        m_itemClickListener.onItemClick(v, getLayoutPosition());

        m_homeActivity = (HomeActivity) m_context;
        m_homeActivity.prepareSelection(v, getAdapterPosition());

    }

    public void setItemClickListener(ItemClickListener ic) {
        m_itemClickListener = ic;
    }

}
