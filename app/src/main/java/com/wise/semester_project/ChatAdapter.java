package com.wise.semester_project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.view.Gravity;
import android.widget.LinearLayout;
import io.noties.markwon.Markwon;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private List<ChatMessage> messages;
    private final Markwon markwon;

    public ChatAdapter(List<ChatMessage> messages, Markwon markwon) {
        this.messages = messages;
        this.markwon = markwon;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        
        // 使用Markwon渲染Markdown内容
        markwon.setMarkdown(holder.messageText, message.getContent());
        
        // 设置消息对齐方式
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.messageText.getLayoutParams();
        if (message.isUser()) {
            holder.messageText.setBackgroundResource(R.drawable.message_bubble_user);
            params.gravity = Gravity.END;
            params.setMarginStart(100);
            params.setMarginEnd(16);
        } else {
            holder.messageText.setBackgroundResource(R.drawable.message_bubble_assistant);
            params.gravity = Gravity.START;
            params.setMarginStart(16);
            params.setMarginEnd(100);
        }
        holder.messageText.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void updateMessage(int position, String newContent) {
        if (position >= 0 && position < messages.size()) {
            messages.get(position).setContent(newContent);
            notifyItemChanged(position);
        }
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
        }
    }
} 