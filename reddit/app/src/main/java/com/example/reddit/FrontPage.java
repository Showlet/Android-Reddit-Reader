package com.example.reddit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by vincent on 2016-01-25.
 */
public class FrontPage {

    public String kind;
    public Data data;

    public class Data {
        public String after;
        public String before;
        public String modhash;
        public ArrayList<Children> children;

        public class Children {
            public String kind;
            public Post data;

            public class Post {
                public boolean archived;
                public String author;
                public boolean clicked;
                public long created;
                public int gilded;
                public boolean hidden;
                public boolean hide_score;
                public String id;
                public boolean is_self;
                public boolean locked;
                public String name;
                public int num_comments;
                public int num_reports;
                public boolean over_18;
                public String permalink;
                public boolean quarantine;
                public boolean saved;
                public int score;
                public String self_text;
                public boolean stickied;
                public String subreddit;
                public String subreddit_id;
                public String title;
                public int ups;
                public String url;
                public boolean visited;
                public Preview preview;

                public class Preview {
                    public ArrayList<Image> images;

                    public class Image{
                        public Url source;
                        public ArrayList<Url> resolutions;

                        public class Url {
                            public int width;
                            public int height;
                            public String url;
                        }
                    }
                }
            }
        }
    }
}

