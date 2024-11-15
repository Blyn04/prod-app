package com.example.moodchecker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moodchecker.R;
import com.example.moodchecker.model.TodoItem;

import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
    private List<TodoItem> todoList;

    public TodoAdapter(List<TodoItem> todoList) {
        this.todoList = todoList;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        TodoItem item = todoList.get(position);
        holder.name.setText(item.getName());
        holder.status.setText(item.getStatus());
        holder.deadline.setText(item.getDeadline());

        if (item.getStatus().equals("In Progress")) {
            holder.status.setTextColor(ContextCompat.getColor(holder.status.getContext(), R.color.yellow));
        } else if (item.getStatus().equals("Complete")) {
            holder.status.setTextColor(ContextCompat.getColor(holder.status.getContext(), R.color.green));
        } else if (item.getStatus().equals("Not Started")) {
            holder.status.setTextColor(ContextCompat.getColor(holder.status.getContext(), R.color.red));
        }
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public static class TodoViewHolder extends RecyclerView.ViewHolder {
        TextView name, status, deadline;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.todoName);
            status = itemView.findViewById(R.id.todoStatus);
            deadline = itemView.findViewById(R.id.todoDeadline);
        }
    }
}