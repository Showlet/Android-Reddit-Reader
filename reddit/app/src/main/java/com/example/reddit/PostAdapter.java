package com.example.reddit;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by vincent on 2016-01-28.
 */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder>{

    private List<FrontPage.Data.Children> _LstPost;

    public PostAdapter(List<FrontPage.Data.Children> lstMessage)
    {
        _LstPost = lstMessage;
    }

    @Override
    public int getItemCount() {
        return _LstPost.size();
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PostViewHolder holder, int position) {
        FrontPage.Data.Children post = _LstPost.get(position);
        holder.tvTitle.setText(post.data.title);
        holder.tvSubreddit.setText(post.data.subreddit);
        holder.tvUser.setText(post.data.author);

        holder.imgImage.setImageBitmap(null);
        holder.imgImage.setBackground(null);
        holder.imgImage.setBackgroundResource(R.drawable.defaultimg);

        String urlpreview =  null;

        if(post.data.preview !=null )
            if(post.data.preview.images.size() >= 1)
                urlpreview = post.data.preview.images.get(0).source.url;

        new ImageLoader(holder.imgImage, R.drawable.defaultimg).execute(urlpreview);
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        protected TextView tvTitle;
        protected TextView tvSubreddit;
        protected ImageView imgImage;
        protected TextView tvUser;

        public PostViewHolder(View v) {
            super(v);
            tvTitle = (TextView) v.findViewById(R.id.txtPostTitle);
            tvSubreddit = (TextView) v.findViewById(R.id.txtPostSubreddit);
            imgImage = (ImageView) v.findViewById(R.id.imgPostMedia);
            tvUser = (TextView) v.findViewById(R.id.txtPostUser);
        }
    }
}
