package com.example.nirvana.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.models.ChatMessage;
import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private static final int USER_MESSAGE = 1;
    private static final int BOT_MESSAGE = 2;
    
    private List<ChatMessage> chatMessages;
    private Context context;
    
    public ChatAdapter() {
        this.chatMessages = new ArrayList<>();
    }
    
    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages != null ? chatMessages : new ArrayList<>();
    }
    
    public ChatAdapter(List<ChatMessage> chatMessages, Context context) {
        this.chatMessages = chatMessages != null ? chatMessages : new ArrayList<>();
        this.context = context;
    }
    
    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        
        if (message.isUserMessage()) {
            // User message
            holder.cardUserMessage.setVisibility(View.VISIBLE);
            holder.cardBotMessage.setVisibility(View.GONE);
            holder.tvUserMessage.setText(message.getMessage());
        } else {
            // Bot message
            holder.cardUserMessage.setVisibility(View.GONE);
            holder.cardBotMessage.setVisibility(View.VISIBLE);
            holder.tvBotMessage.setText(message.getMessage());
        }
    }
    
    @Override
    public int getItemCount() {
        return chatMessages.size();
    }
    
    @Override
    public int getItemViewType(int position) {
        return chatMessages.get(position).isUserMessage() ? USER_MESSAGE : BOT_MESSAGE;
    }
    
    public void addMessage(ChatMessage message) {
        chatMessages.add(message);
        notifyItemInserted(chatMessages.size() - 1);
    }
    
    public void setMessages(List<ChatMessage> messages) {
        this.chatMessages = messages;
        notifyDataSetChanged();
    }
    
    public void clearMessages() {
        chatMessages.clear();
        notifyDataSetChanged();
    }
    
    static class ChatViewHolder extends RecyclerView.ViewHolder {
        CardView cardUserMessage;
        CardView cardBotMessage;
        TextView tvUserMessage;
        TextView tvBotMessage;
        
        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            cardUserMessage = itemView.findViewById(R.id.cardUserMessage);
            cardBotMessage = itemView.findViewById(R.id.cardBotMessage);
            tvUserMessage = itemView.findViewById(R.id.tvUserMessage);
            tvBotMessage = itemView.findViewById(R.id.tvBotMessage);
        }
    }
} 