package com.team17.gsbts;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class AdapterFeed extends RecyclerView.Adapter<AdapterFeed.MyViewHolder> {

    Context context;
    ArrayList<ModelFeed> modelFeedArrayList = new ArrayList<>();

    public AdapterFeed(Context context, ArrayList<ModelFeed> modelFeedArrayList) {
        this.context = context;
        this.modelFeedArrayList = modelFeedArrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_feed, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view);


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final ModelFeed modelFeed = modelFeedArrayList.get(position);

        holder.notifications_Text2.setText(modelFeed.getTimeStamp());
        holder.notifications_Text3.setText(modelFeed.getNotificationText());
    }

    @Override
    public int getItemCount() {

        return modelFeedArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView notifications_Text2, notifications_Text3;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            notifications_Text2 = (TextView) itemView.findViewById(R.id.notifications_Text2);
            notifications_Text3 = (TextView) itemView.findViewById(R.id.notifications_Text3);
        }
    }
}
