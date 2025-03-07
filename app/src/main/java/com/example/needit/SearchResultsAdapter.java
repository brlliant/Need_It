package com.example.needit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
<<<<<<< HEAD

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

=======
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
>>>>>>> origin/master
import java.util.List;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {

    private List<String> searchResults;
    private OnItemClickListener onItemClickListener;

<<<<<<< HEAD
    public SearchResultsAdapter(List<String> searchResults, OnItemClickListener onItemClickListener) {
=======
    public SearchResultsAdapter(List<String> searchResults, OnItemClickListener onItemClickListener)
    {
>>>>>>> origin/master
        this.searchResults = searchResults;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
<<<<<<< HEAD
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
=======
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
>>>>>>> origin/master
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
<<<<<<< HEAD
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
=======
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
>>>>>>> origin/master
        holder.textView.setText(searchResults.get(position));
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(searchResults.get(position)));
    }

    @Override
<<<<<<< HEAD
    public int getItemCount() {
        return searchResults.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
=======
    public int getItemCount()
    {
        return searchResults.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView textView;

        public ViewHolder(@NonNull View itemView)
        {
>>>>>>> origin/master
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }

<<<<<<< HEAD
    public interface OnItemClickListener {
=======
    public interface OnItemClickListener
    {
>>>>>>> origin/master
        void onItemClick(String item);
    }
}
