package com.example.reddit;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

/**
 * Created by vincent on 2016-01-28.
 */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<FrontPage.Data.Children> _LstPost;
    private boolean IsGrid;

    public PostAdapter(List<FrontPage.Data.Children> lstMessage) {
        _LstPost = lstMessage;
    }

    public PostAdapter(List<FrontPage.Data.Children> lstMessage, boolean isGrid) {
        _LstPost = lstMessage;
        IsGrid = isGrid;
    }

    @Override
    public int getItemCount() {
        return _LstPost.size();
    }

    public FrontPage.Data.Children.Post getItem(int pos) {
        return _LstPost.get(pos).data;
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(IsGrid) {
            View itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.post_item_grid, parent, false);
            return new PostViewHolder(itemView);
        }
        if(!IsGrid) {
            View itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.post_item_linear, parent, false);
            return new PostViewHolder(itemView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(PostViewHolder holder, int position) {
        FrontPage.Data.Children post = _LstPost.get(position);
        holder.tvTitle.setText(post.data.title);
        holder.tvSubreddit.setText("/r/" + post.data.subreddit);

        holder.imgImage.setImageBitmap(null);
        holder.imgImage.setBackground(null);
        holder.imgImage.setBackgroundResource(R.drawable.defaultimg);
        holder.imagePost = post;

        String urlpreview = null;

        if (post.data.preview != null)
            if (post.data.preview.images.size() >= 1)
                urlpreview = post.data.preview.images.get(0).source.url;

        new com.example.reddit.utilities.ImageLoader(holder.imgImage,holder.progressBar,R.drawable.ic_action_alert_warning).execute(urlpreview);
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        protected TextView tvTitle;
        protected TextView tvSubreddit;
        protected ImageView imgImage;
        protected TextView tvUser;
        protected ProgressBar progressBar;
        protected FrontPage.Data.Children imagePost;

        public PostViewHolder(View v) {
            super(v);
            tvTitle = (TextView) v.findViewById(R.id.txtPostTitle);
            tvSubreddit = (TextView) v.findViewById(R.id.txtPostSubreddit);
            imgImage = (ImageView) v.findViewById(R.id.imgPostMedia);
            progressBar = (ProgressBar) v.findViewById(R.id.imgProgress);
        }
    }
}
