package vn.edu.tdtu.tdtbank.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.tdtu.tdtbank.databinding.ItemChatBinding;
import vn.edu.tdtu.tdtbank.ui.chat.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.Holder> {

    public interface OnAction {
        void onAction(ChatMessage message);
    }

    private final List<ChatMessage> items = new ArrayList<>();
    private final OnAction listener;

    public ChatAdapter(OnAction listener) {
        this.listener = listener;
    }

    public void submit(List<ChatMessage> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemChatBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        ChatMessage m = items.get(position);
        h.binding.tvMessage.setText(m.text);
        boolean user = m.fromUser;
        h.binding.bubbleUser.setVisibility(user ? View.VISIBLE : View.GONE);
        h.binding.bubbleBot.setVisibility(user ? View.GONE : View.VISIBLE);
        h.binding.tvUser.setText(m.text);
        h.binding.tvMessage.setVisibility(user ? View.GONE : View.VISIBLE);
        h.binding.btnAction.setVisibility(!user && m.hasAction() ? View.VISIBLE : View.GONE);
        if (!user && m.hasAction()) {
            h.binding.btnAction.setText(m.actionLabelRes);
            h.binding.btnAction.setOnClickListener(v -> listener.onAction(m));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemChatBinding binding;

        Holder(ItemChatBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
