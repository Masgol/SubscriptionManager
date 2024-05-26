package com.random.subscriptionmanager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder> {
    private OnItemClickListener listener;
    private List<Subscription> subscriptions;
    private SubscriptionManager subscriptionManager;
    private OnUpdateClickListener updateClickListener;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public SubscriptionAdapter(List<Subscription> subscriptions, SubscriptionManager subscriptionManager, OnUpdateClickListener updateClickListener) {
        this.subscriptions = subscriptions;
        this.subscriptionManager = subscriptionManager;
        this.updateClickListener = updateClickListener;

    }
    public Subscription getSubscriptionAt(int position) {
        return subscriptions.get(position);
    }
    public interface OnUpdateClickListener {
        void onUpdateClick(Subscription subscription);
    }



    @NonNull
    @Override
    public SubscriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subscription, parent, false);
        return new SubscriptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubscriptionViewHolder holder, int position) {
        Subscription subscription = subscriptions.get(position);
        holder.textViewName.setText(subscription.getName());
        holder.textViewDates.setText(dateFormat.format(subscription.getStartDate()) + " |-| " + dateFormat.format(subscription.getEndDate()));
        holder.textViewCost.setText(String.valueOf(subscription.getCost()));

        holder.updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (updateClickListener != null) {
                    updateClickListener.onUpdateClick(subscription);
                }
            }
        });


        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ensure the position is valid
                if (position >= 0 && position < subscriptions.size()) {
                    // Remove the subscription from the list
                    Subscription removedSubscription = subscriptions.remove(position);
                    notifyItemRemoved(position);

                    // Optionally, you can also remove the subscription from the data source here
                    subscriptionManager.removeSubscription(removedSubscription);
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.extraInfoLayout.getVisibility() == View.VISIBLE) {
                    ObjectAnimator hideAnimation = ObjectAnimator.ofFloat(holder.extraInfoLayout, "translationY", 0f, -holder.extraInfoLayout.getHeight());
                    hideAnimation.setDuration(500);
                    ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(holder.extraInfoLayout, "alpha", 1f, 0f);
                    alphaAnimation.setDuration(500);
                    AnimatorSet hideAnimatorSet = new AnimatorSet();
                    hideAnimatorSet.playTogether(hideAnimation, alphaAnimation);
                    hideAnimatorSet.start();


                    hideAnimatorSet.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            holder.extraInfoLayout.setVisibility(View.GONE);
                        }
                    });
                } else {
                    // If extra info is hidden, animate it to show
                    holder.extraInfoLayout.setVisibility(View.VISIBLE);
                    holder.extraInfoLayout.setAlpha(0f);
                    ObjectAnimator showAnimation = ObjectAnimator.ofFloat(holder.extraInfoLayout, "translationY", -holder.extraInfoLayout.getHeight(), 0f);
                    showAnimation.setDuration(500);
                    ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(holder.extraInfoLayout, "alpha", 0f, 1f);
                    alphaAnimation.setDuration(500);
                    AnimatorSet showAnimatorSet = new AnimatorSet();
                    showAnimatorSet.playTogether(showAnimation, alphaAnimation);
                    showAnimatorSet.start();


                    Subscription subscription = subscriptions.get(position);
                    Date endDate = subscription.getEndDate();
                    Date currentDate = new Date(); // Current date
                    long diffInMillies = endDate.getTime() - currentDate.getTime();
                    long diffInDays = diffInMillies / (1000 * 60 * 60 * 24);

                    // Update the extra info TextView with the calculated number of days
                    holder.extraTextView.setText(diffInDays + " days");
                    holder.categoryTextView.setText(subscription.getCategory());



















                }
            }
        });


    }
    public void filterList(List<Subscription> filteredList) {
        subscriptions = filteredList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return subscriptions.size();
    }

    public static class SubscriptionViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        TextView extraTextView;
        TextView textViewDates;
        TextView textViewCost;
        View extraInfoLayout;
        Button removeButton, updateButton;
        TextView  categoryTextView;
        private Subscription subscription;

        public SubscriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            extraTextView = itemView.findViewById(R.id.extraTextView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewDates = itemView.findViewById(R.id.textViewDates);
            textViewCost = itemView.findViewById(R.id.textViewCost);
            extraInfoLayout = itemView.findViewById(R.id.extraInfoLayout); // Assuming this is the layout for additional info
            removeButton = itemView.findViewById(R.id.removeButton); // Assuming this is the remove button
            updateButton = itemView.findViewById(R.id.updateButton); // Assuming this is the update button
            // Initially hide the extra info layout
            extraInfoLayout.setVisibility(View.GONE);
        }
    }
    public interface OnItemClickListener {
        void onItemClick(int position);
        void onDeleteClick(int position);
        void onUpdateClick(int position);
    }

}

