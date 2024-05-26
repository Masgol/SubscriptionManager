package com.random.subscriptionmanager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;



import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AnalysisActivity extends AppCompatActivity {

    private TextView tv_totalsubs, tv_totalCost, tv_subName, tv_daily, tv_monthly, tv_yearly, tv_daysleft;
    private Button SubscriptionScreenbutton;
    LinearLayout subscriptionList;



    private SubscriptionManager subscriptionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analysis_screen);
        EdgeToEdge.enable(this);
        initiateViews();

        // Initialize SubscriptionManager with context
        subscriptionManager = SubscriptionManager.getInstance(this);



        // Get subscriptions and update UI
        List<Subscription> subscriptions = subscriptionManager.getSubscriptions();
        int totalSubscriptions = subscriptions.size();
        double totalCost = calculateTotalCost(subscriptions);

        tv_totalsubs.setText("Total Subscriptions: " + totalSubscriptions);
        tv_totalCost.setText("Total Cost: $" + totalCost);


        for (Subscription subscription : subscriptions) {
            TextView textView = createSubscriptionTextView(subscription);
            subscriptionList.addView(textView);

        }
        SubscriptionScreenbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });










    }

    public void initiateViews(){
        SubscriptionScreenbutton = findViewById(R.id.SubscriptionScreenbutton);
        tv_totalsubs = findViewById(R.id.tv_totalsubs);
        tv_totalCost = findViewById(R.id.tv_totalCost);
        subscriptionList = findViewById(R.id.subscriptionList);
        tv_subName = findViewById(R.id.tv_subName);
        tv_daily = findViewById(R.id.tv_daily);
        tv_daysleft = findViewById(R.id.tv_daysleft);
    }
    private double calculateTotalCost(List<Subscription> subscriptions) {
        double totalCost = 0.0;
        for (Subscription subscription : subscriptions) {
            totalCost += subscription.getCost();
        }
        return totalCost;
    }
    private TextView createSubscriptionTextView(Subscription subscription) {
        TextView textView = new TextView(this);
        textView.setText(subscription.getName());
        textView.setTextSize(30);
        textView.setPadding(30, 0, 16, 0);  //left, top, right, bottom
        textView.setBackgroundResource(R.drawable.curvedframe_dark);
        textView.setTextColor(getResources().getColor(R.color.white));
        // Attach the subscription object as a tag to the TextView
        textView.setTag(subscription);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 2, 0, 2);

// Apply LayoutParams to TextView
        textView.setLayoutParams(layoutParams);
        // Add OnClickListener to each TextView
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSubscriptionClick((Subscription) v.getTag());
            }
        });
        return textView;
    }

    private void handleSubscriptionClick(Subscription subscription) {
        // Calculate total cost for a day, month, and year
        double monthlyCost = subscription.getCost();
        double dailyCost = monthlyCost / 30; // Assuming a month has 30 days
        double yearlyCost = monthlyCost * 12; // Assuming a year has 365 days

        // Calculate the number of days left until the end date
        Date endDate = subscription.getEndDate();
        Date currentDate = new Date(); // Current date
        long diffInMillis = endDate.getTime() - currentDate.getTime();
        int daysLeft = (int) (diffInMillis / (1000 * 60 * 60 * 24));
        tv_subName.setTextSize(25);

        tv_daysleft.setTextSize(15);
        tv_daily.setTextSize(15);

        tv_daysleft.setText("Days Left: " + daysLeft);
        tv_subName.setText(subscription.getName());
        int maxWidth = 20;
        String subscriptionDetails = String.format("Daily Cost:      $%10.2f\nMonthly Cost:      $%10.2f\nYearly Cost:      $%10.2f", dailyCost, monthlyCost, yearlyCost);
        tv_daily.setText(subscriptionDetails);

    }
}
