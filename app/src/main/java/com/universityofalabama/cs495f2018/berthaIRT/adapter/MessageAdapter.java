package com.universityofalabama.cs495f2018.berthaIRT.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.universityofalabama.cs495f2018.berthaIRT.R;
import com.universityofalabama.cs495f2018.berthaIRT.Report;
import com.universityofalabama.cs495f2018.berthaIRT.Report.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    public interface RecyclerViewClickListener {
        void onClick(View view, int position);
    }

    private Context ctx;
    private List<Report.Message> messages;
    private RecyclerViewClickListener mListener;


    public MessageAdapter(Context c, List<Report.Message> m, RecyclerViewClickListener l) {
        ctx = c;
        messages = m;
        mListener = l;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.adapter_message, parent, false);
        return new MessageViewHolder(view,mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);

        if(diffDate(position,message)) {
            holder.msgDate.setVisibility(View.VISIBLE);
            holder.msgDate.setText(message.date);
        }

        if(message.isSentByCurrentUser()) {
            holder.rightMsgLayout.setVisibility(RelativeLayout.VISIBLE);
            holder.rightMsgText.setText(message.text);
            holder.rightMsgTime.setText(message.time);

            holder.leftMsgLayout.setVisibility(RelativeLayout.GONE);
            holder.msgSendError.setVisibility(message.getSendingErrorVisibility());

            //make time invisible because error message will be in it's place
            if(holder.msgSendError.getVisibility() == View.VISIBLE)
                holder.rightMsgTime.setVisibility(View.GONE);

            else {
                //Listener for showing time
                holder.rightMsgText.setOnClickListener(v -> {
                    if (holder.rightMsgTime.getVisibility() == View.GONE)
                        holder.rightMsgTime.setVisibility(View.VISIBLE);
                    else
                        holder.rightMsgTime.setVisibility(View.GONE);
                });
            }

            //makes the time visible if it was the last sent
            if(message.lastSent && (holder.msgSendError.getVisibility() != View.VISIBLE))
                holder.rightMsgTime.setVisibility(View.VISIBLE);
            else
                holder.rightMsgTime.setVisibility(View.GONE);
        }
        else {
            holder.leftMsgLayout.setVisibility(RelativeLayout.VISIBLE);
            holder.leftMsgText.setText(message.text);
            holder.leftMsgTime.setText(message.time);
            holder.rightMsgLayout.setVisibility(RelativeLayout.GONE);

            //Listener for showing time
            holder.leftMsgText.setOnClickListener(v -> {
                if (holder.leftMsgTime.getVisibility() == View.GONE)
                    holder.leftMsgTime.setVisibility(View.VISIBLE);
                else
                    holder.leftMsgTime.setVisibility(View.GONE);
            });
        }
    }

    //returns true if the index is 0 or the dates are different
    private boolean diffDate(int i, Message message) {
        if (i == 0) return true;
        return !(messages.get(i-1).date.equals(message.date));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        LinearLayout leftMsgLayout;
        LinearLayout rightMsgLayout;

        TextView leftMsgText, rightMsgText, leftMsgTime, rightMsgTime, msgDate, msgSendError;

        MessageViewHolder(View itemView, RecyclerViewClickListener listener) {
            super(itemView);

            leftMsgLayout =  itemView.findViewById(R.id.chat_left_msg_layout);
            rightMsgLayout = itemView.findViewById(R.id.chat_right_msg_layout);
            leftMsgText = itemView.findViewById(R.id.left_message_body);
            rightMsgText = itemView.findViewById(R.id.right_message_body);
            leftMsgTime = itemView.findViewById(R.id.left_message_time);
            rightMsgTime = itemView.findViewById(R.id.right_message_time);
            msgDate = itemView.findViewById(R.id.message_date);
            msgSendError = itemView.findViewById(R.id.message_error);
            mListener = listener;
            rightMsgText.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mListener.onClick(view, getAdapterPosition());
        }
    }
}