package com.random.subscriptionmanager;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SubscriptionAdapter.OnUpdateClickListener {

    private Calendar calendar;
    private RecyclerView recyclerViewSubscriptions;
    private SubscriptionAdapter subscriptionAdapter;
    private Subscription subscriptions;
    private ListView expiredSubscriptionsListView;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final String FILE_NAME = "subscriptions.json";
    private EditText searchEditText;
    private ImageButton analysisSectionButton, expiredSubscriptionsButton, newSubscriptionButton;
    private SubscriptionManager subscriptionManager;
    ArrayAdapter<Subscription> finalAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);

        subscriptionManager = SubscriptionManager.getInstance(this);

        initializeViews();
        loadSubscriptions();
        setupRecyclerView();
        setupSearchBar();
        setNewSubscriptionClickListener();
        loadAnalysisScreen(analysisSectionButton);

        calendar = Calendar.getInstance();
        Spinner sortSpinner = findViewById(R.id.sortSpinner);
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                this, R.array.sort_options, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SortCriteria criteria = SortCriteria.values()[position];
                sortSubscriptions(criteria);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        List<Subscription> expiredSubscriptions = subscriptionManager != null ? subscriptionManager.getExpiredSubscriptions() : new ArrayList<>();


            finalAdapter = new ArrayAdapter<Subscription>(this, android.R.layout.simple_list_item_1, expiredSubscriptions) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View listItemView = convertView;
                if (listItemView == null) {
                    listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_expired_subscription, parent, false);
                }

                Subscription currentSubscription = getItem(position);

                TextView subscriptionNameTextView = listItemView.findViewById(R.id.subscriptionNameTextView);
                Button restoreButton = listItemView.findViewById(R.id.restoreButton);

                assert currentSubscription != null;
                subscriptionNameTextView.setText(currentSubscription.getName());

                restoreButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Implement logic to restore the subscription
                        if (currentSubscription != null) {
                            // Example: Call a method to restore the subscription
                            onUpdateClick(currentSubscription);
                                            expiredSubscriptions.remove(currentSubscription);
                                            subscriptionManager.addSubscription(currentSubscription);

                            finalAdapter.notifyDataSetChanged();
                        }
                    }
                });

                return listItemView;
            }
        };

        expiredSubscriptionsListView.setAdapter(finalAdapter);



        expiredSubscriptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expiredSubscriptionsListView.getVisibility() == View.GONE) {
                    expiredSubscriptionsListView.setVisibility(View.VISIBLE);
                    recyclerViewSubscriptions.setVisibility(View.GONE);
                } else {
                    expiredSubscriptionsListView.setVisibility(View.GONE);
                    recyclerViewSubscriptions.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void loadAnalysisScreen(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AnalysisActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveSubscriptions();
    }

    private void setupSearchBar() {
        searchEditText = findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSubscriptions(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterSubscriptions(String query) {
        List<Subscription> filteredList = new ArrayList<>();
        for (Subscription subscription : subscriptionManager.getSubscriptions()) {
            if (subscription.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(subscription);
            }
        }
        subscriptionAdapter.filterList(filteredList);
    }

    private void initializeViews() {
        calendar = Calendar.getInstance();
        expiredSubscriptionsListView = findViewById(R.id.expiredSubscriptionsListView);
        analysisSectionButton = findViewById(R.id.analysisSectionButton);
        newSubscriptionButton = findViewById(R.id.new_subscription);
        recyclerViewSubscriptions = findViewById(R.id.recyclerViewSubscriptions);
        expiredSubscriptionsButton = findViewById(R.id.expiredSubscriptionsButton);
    }

    public void showDatePickerDialog(View view) {
        final EditText editText = (EditText) view;
        int[] location = new int[2];
        editText.getLocationOnScreen(location);
        int editTextY = location[1];

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        editText.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                    }
                }, year, month, dayOfMonth);

        Window window = datePickerDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.y = editTextY + editText.getHeight();
            window.setAttributes(layoutParams);
            window.setGravity(Gravity.TOP);
        }

        datePickerDialog.show();
    }

    private void setDateClickListener(EditText editText) {
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });
    }

    private void setNewSubscriptionClickListener() {
        newSubscriptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.subscriptionpopup);
                dialog.show();

                EditText editTextStartDate = dialog.findViewById(R.id.editTextStartDate);
                EditText editTextEndDate = dialog.findViewById(R.id.editTextEndDate);
                setDateClickListener(editTextStartDate);
                setDateClickListener(editTextEndDate);

                dialog.findViewById(R.id.saveButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onSaveButtonClick(v, dialog);
                    }
                });
            }
        });
    }

    public void onSaveButtonClick(View view, Dialog dialog) {
        EditText editTextSubscriptionName = dialog.findViewById(R.id.editTextSubscriptionName);
        EditText editTextCost = dialog.findViewById(R.id.editTextCost);
        EditText editTextStartDate = dialog.findViewById(R.id.editTextStartDate);
        EditText editTextEndDate = dialog.findViewById(R.id.editTextEndDate);
        Spinner spinnerCategory = dialog.findViewById(R.id.spinnerCategory);

        String name = editTextSubscriptionName.getText().toString();
        String costText = editTextCost.getText().toString();

        if (costText.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please enter a valid cost", Toast.LENGTH_SHORT).show();
            return;
        }
        double cost = Double.parseDouble(costText);
        Date startDate = parseDate(editTextStartDate.getText().toString());
        Date endDate = parseDate(editTextEndDate.getText().toString());

        if (endDate.before(new Date()) || endDate.before(startDate)) {
            Toast.makeText(MainActivity.this, "End date cannot be prior to current date or start date", Toast.LENGTH_SHORT).show();
            return;
        }
        if (name.isEmpty() || startDate == null || endDate == null) {
            Toast.makeText(MainActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        String category = spinnerCategory.getSelectedItem().toString();
        if (spinnerCategory.getSelectedItem().toString().isEmpty()){category="Other";}

        Subscription newSubscription = new Subscription(subscriptionManager.getNextId(), name, startDate, endDate, cost, category);
        subscriptionManager.addSubscription(newSubscription);
        saveSubscriptions();

        subscriptionAdapter.notifyDataSetChanged();
        dialog.dismiss();
    }

    private Date parseDate(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setupRecyclerView() {
        subscriptionAdapter = new SubscriptionAdapter(subscriptionManager.getSubscriptions(), subscriptionManager, this);
        recyclerViewSubscriptions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSubscriptions.setAdapter(subscriptionAdapter);
    }

    private void saveSubscriptions() {
        String json = new Gson().toJson(subscriptionManager.getSubscriptions());
        try (FileOutputStream fos = openFileOutput(FILE_NAME, MODE_PRIVATE)) {
            fos.write(json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadSubscriptions() {
        try (FileInputStream fis = openFileInput(FILE_NAME)) {
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            String json = new String(buffer);

            Type listType = new TypeToken<ArrayList<Subscription>>() {}.getType();
            List<Subscription> loadedSubscriptions = new Gson().fromJson(json, listType);

            for (Subscription subscription : loadedSubscriptions) {
                if (subscriptionManager.getNextId() >= subscriptionManager.getNextId()) {
                    subscriptionManager.setNextId(subscriptionManager.getNextId() + 1); // Ensure nextId is always greater than the highest ID
                }
                subscriptionManager.addSubscription(subscription); // Directly add the loaded subscription
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onUpdateClick(Subscription subscription) {
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.subscriptionpopup);

        EditText editTextSubscriptionName = dialog.findViewById(R.id.editTextSubscriptionName);
        EditText editTextCost = dialog.findViewById(R.id.editTextCost);
        EditText editTextStartDate = dialog.findViewById(R.id.editTextStartDate);
        EditText editTextEndDate = dialog.findViewById(R.id.editTextEndDate);

        // Set subscription info to the fields
        editTextSubscriptionName.setText(subscription.getName());
        editTextCost.setText(String.valueOf(subscription.getCost()));
        editTextStartDate.setText(dateFormat.format(subscription.getStartDate()));
        editTextEndDate.setText(dateFormat.format(subscription.getEndDate()));

        setDateClickListener(editTextStartDate);
        setDateClickListener(editTextEndDate);

        // Show the dialog
        dialog.show();
        onSaveButtonClick(view, dialog);
        // Set onClickListener for save button
      /*  dialog.findViewById(R.id.saveButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update the subscription with new info
                subscription.setName(editTextSubscriptionName.getText().toString());
                subscription.setCost(Double.parseDouble(editTextCost.getText().toString()));
                subscription.setStartDate(parseDate(editTextStartDate.getText().toString()));
                subscription.setEndDate(parseDate(editTextEndDate.getText().toString()));

                // Update the subscription in the list
                subscriptionManager.updateSubscription(subscription, subscription);

                // Save subscriptions
                saveSubscriptions();

                // Notify adapter
                subscriptionAdapter.notifyDataSetChanged();

                // Dismiss dialog
                dialog.dismiss();
            }
        });*/
    }
    public enum SortCriteria {
        NAME,
        COST,
        START_DATE,
        END_DATE
    }
    private void sortSubscriptions(SortCriteria criteria) {
        List<Subscription> subscriptions = subscriptionManager.getSubscriptions();

        switch (criteria) {
            case NAME:
                Collections.sort(subscriptions, new Comparator<Subscription>() {
                    @Override
                    public int compare(Subscription s1, Subscription s2) {
                        return s1.getName().compareToIgnoreCase(s2.getName());
                    }
                });
                break;
            case COST:
                Collections.sort(subscriptions, new Comparator<Subscription>() {
                    @Override
                    public int compare(Subscription s1, Subscription s2) {
                        return Double.compare(s1.getCost(), s2.getCost());
                    }
                });
                break;
            case START_DATE:
                Collections.sort(subscriptions, new Comparator<Subscription>() {
                    @Override
                    public int compare(Subscription s1, Subscription s2) {
                        return s1.getStartDate().compareTo(s2.getStartDate());
                    }
                });
                break;
            case END_DATE:
                Collections.sort(subscriptions, new Comparator<Subscription>() {
                    @Override
                    public int compare(Subscription s1, Subscription s2) {
                        return s1.getEndDate().compareTo(s2.getEndDate());
                    }
                });
                break;
        }

        // Update RecyclerView
        subscriptionAdapter.notifyDataSetChanged();
    }
    private void setNotification(Subscription subscription) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("subscription_name", subscription.getName());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, subscriptionManager.getNextId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(subscription.getEndDate());
        calendar.add(Calendar.DAY_OF_YEAR, -3); // Notify 3 days before the end date

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }







}
