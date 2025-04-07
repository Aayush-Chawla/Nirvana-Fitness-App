package com.example.nirvana.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.nirvana.R;
import com.example.nirvana.data.models.Blog;
import java.util.List;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.BlogViewHolder> {

    private List<Blog> blogs;
    private OnBlogClickListener listener;

    public interface OnBlogClickListener {
        void onBlogClick(Blog blog);
    }

    public BlogAdapter(List<Blog> blogs, OnBlogClickListener listener) {
        this.blogs = blogs;
        this.listener = listener;
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
        Blog blog = blogs.get(position);
        holder.bind(blog);
    }

    @Override
    public int getItemCount() {
        return blogs.size();
    }

    public void updateBlogs(List<Blog> newBlogs) {
        this.blogs = newBlogs;
        notifyDataSetChanged();
    }

    class BlogViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgBlog;
        private TextView txtTitle;
        private TextView txtDescription;
        private TextView txtAuthor;
        private TextView txtDate;

        BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBlog = itemView.findViewById(R.id.imgBlog);
            txtTitle = itemView.findViewById(R.id.txtBlogTitle);
//            txtDescription = itemView.findViewById(R.id.txtBlogDescription);
            txtAuthor = itemView.findViewById(R.id.txtBlogAuthor);
            txtDate = itemView.findViewById(R.id.txtBlogDate);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onBlogClick(blogs.get(position));
                }
            });
        }

        void bind(Blog blog) {
            txtTitle.setText(blog.getTitle());
            txtDescription.setText(blog.getDescription());
            txtAuthor.setText(blog.getAuthor());
            txtDate.setText(blog.getDate());

            Glide.with(itemView.getContext())
                    .load(blog.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(imgBlog);
        }
    }
}
