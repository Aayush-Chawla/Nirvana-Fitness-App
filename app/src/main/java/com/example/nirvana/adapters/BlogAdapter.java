package com.example.nirvana.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.nirvana.R;
import com.example.nirvana.models.BlogPost;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.BlogViewHolder> {
    private List<BlogPost> blogs;
    private final OnBlogClickListener listener;

    public interface OnBlogClickListener {
        void onBlogClick(BlogPost blog);
    }

    public BlogAdapter(List<BlogPost> blogs, OnBlogClickListener listener) {
        this.blogs = blogs;
        this.listener = listener;
    }

    public void updateBlogs(List<BlogPost> newBlogs) {
        this.blogs = newBlogs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_blog, parent, false);
        return new BlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        BlogPost blog = blogs.get(position);
        holder.bind(blog, listener);
    }

    @Override
    public int getItemCount() {
        return blogs.size();
    }

    static class BlogViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgBlog;
        private final TextView tvTitle;
        private final TextView tvDescription;
        private final TextView tvAuthor;
        private final TextView tvDate;

        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBlog = itemView.findViewById(R.id.imgBlog);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvDate = itemView.findViewById(R.id.tvDate);
        }

        public void bind(BlogPost blog, OnBlogClickListener listener) {
            tvTitle.setText(blog.getTitle());
            tvDescription.setText(blog.getDescription());
            tvAuthor.setText(blog.getAuthor());
            tvDate.setText(blog.getPublishedAt());

            if (blog.getImageUrl() != null && !blog.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(blog.getImageUrl())
                        .placeholder(R.drawable.placeholder_blog)
                        .error(R.drawable.placeholder_blog)
                        .fallback(R.drawable.placeholder_blog)
                        .into(imgBlog);
            } else {
                imgBlog.setImageResource(R.drawable.placeholder_blog);
            }

            itemView.setOnClickListener(v -> listener.onBlogClick(blog));
        }
    }
} 