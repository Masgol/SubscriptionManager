package com.random.subscriptionmanager;

import android.content.Context;
import android.widget.ProgressBar;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartsService {
    private Context context;

    public ChartsService(Context context) {
        this.context = context;
    }

    public static void setupProgressBar(ProgressBar progressBarRemainingDuration, Subscription subscription) {
        long totalDuration = subscription.getEndDate().getTime() - subscription.getStartDate().getTime();
        long remainingDuration = subscription.getEndDate().getTime() - new Date().getTime();
        int progress = (int) ((remainingDuration * 100) / totalDuration);
        progressBarRemainingDuration.setProgress(progress);
    }

    public static void setupLineChart(LineChart lineChart, Subscription subscription) {
        List<Entry> entries = new ArrayList<>();
        Map<Date, Double> costOverTime = getCostOverTimeData(subscription);
        if (costOverTime != null) {
            int index = 0;
            for (Map.Entry<Date, Double> entry : costOverTime.entrySet()) {
                entries.add(new Entry(index++, entry.getValue().floatValue()));
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, "Cost Over Time");
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate(); // refresh
    }

    private static Map<Date, Double> getCostOverTimeData(Subscription subscription) {
        Map<Date, Double> costData = new HashMap<>();
        double dailyCost = subscription.getCost() / getTotalDays(subscription);

        Calendar start = Calendar.getInstance();
        start.setTime(subscription.getStartDate());

        Calendar end = Calendar.getInstance();
        end.setTime(subscription.getEndDate());

        while (!start.after(end)) {
            costData.put(start.getTime(), dailyCost);
            start.add(Calendar.DAY_OF_MONTH, 1);
        }

        return costData;
    }

    public static void setupPieChart(PieChart pieChart, Subscription subscription) {
        List<PieEntry> entries = new ArrayList<>();
        double dailyCost = subscription.getCost() / getTotalDays(subscription);
        double monthlyCost = dailyCost * 30;
        double yearlyCost = dailyCost * 365;

        entries.add(new PieEntry((float) dailyCost, "Daily"));
        entries.add(new PieEntry((float) monthlyCost, "Monthly"));
        entries.add(new PieEntry((float) yearlyCost, "Yearly"));

        PieDataSet dataSet = new PieDataSet(entries, "Cost Breakdown");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate(); // refresh
    }

    private static int getTotalDays(Subscription subscription) {
        long diff = subscription.getEndDate().getTime() - subscription.getStartDate().getTime();
        return (int) (diff / (1000 * 60 * 60 * 24));
    }

    public static void prepareChartData(List<Subscription> subscriptions, BarChart barChart) {
        Map<String, Double> categoryCostMap = new HashMap<>();
        for (Subscription subscription : subscriptions) {
            String category = subscription.getCategory();
            if (category == null) {
                category = "Other";
            }
            double cost = subscription.getCost();
            categoryCostMap.put(category, categoryCostMap.getOrDefault(category, 0.0) + cost);
        }

        List<BarEntry> barEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Double> entry : categoryCostMap.entrySet()) {
            barEntries.add(new BarEntry(index, entry.getValue().floatValue()));
            labels.add(entry.getKey());
            index++;
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Subscription Categories");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextSize(16f);

        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        // Customize the chart
        barChart.getDescription().setEnabled(false);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setGranularityEnabled(true);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getAxisLeft().setGranularity(1f);
        barChart.getAxisLeft().setGranularityEnabled(true);
        barChart.getAxisRight().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate(); // Refresh the chart
    }
}
