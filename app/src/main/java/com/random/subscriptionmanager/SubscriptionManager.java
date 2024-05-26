package com.random.subscriptionmanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SubscriptionManager {
    private static SubscriptionManager instance;
    private List<Subscription> subscriptions;
    int nextId;
    private Context context; // To hold the context

    public SubscriptionManager(Context context) { // Updated constructor
        this.context = context;
        subscriptions = new ArrayList<>();
        nextId = 1;
    }
    public static SubscriptionManager getInstance(Context context) {
        if (instance == null){
            instance = new SubscriptionManager(context.getApplicationContext());
        }
        return instance;
    }

    public int getNextId() {
        return nextId;
    }

    public void setNextId(int nextId) {
        this.nextId = nextId;
    }


    public void addSubscription(String name, Date startDate, Date endDate, double cost, String category) {
        Subscription subscription = new Subscription(nextId++, name, startDate, endDate, cost, category);
        subscriptions.add(subscription);
        setNotification(subscription); // Set notification for the new subscription
    }

    public void addSubscription(Subscription subscription) {
        subscriptions.add(subscription);
        setNotification(subscription); // Set notification for the added subscription
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void removeSubscription(Subscription subscription) {
        subscriptions.remove(subscription);
        cancelNotification(subscription); // Cancel notification for the removed subscription
    }

    public void updateSubscription(Subscription oldSubscription, Subscription newSubscription) {
        int index = subscriptions.indexOf(oldSubscription);
        if (index != -1) {
            subscriptions.set(index, newSubscription);
            cancelNotification(oldSubscription); // Cancel the old notification
            setNotification(newSubscription); // Set a new notification
        }
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(subscriptions);
    }

    public void fromJson(String json) {
        Gson gson = new Gson();
        Type subscriptionListType = new TypeToken<ArrayList<Subscription>>() {}.getType();
        subscriptions = gson.fromJson(json, subscriptionListType);
    }







    private void setNotification(Subscription subscription) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class); // Create a receiver class NotificationReceiver
        intent.putExtra("subscriptionName", subscription.getName());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, subscription.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(subscription.getEndDate());
        calendar.set(Calendar.HOUR_OF_DAY, 9); // Set notification time to 9 AM

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private void cancelNotification(Subscription subscription) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, subscription.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
