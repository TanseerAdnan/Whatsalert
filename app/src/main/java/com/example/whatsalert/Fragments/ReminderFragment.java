package com.example.whatsalert.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.whatsalert.R;
import com.example.whatsalert.Util.DBOperations;
import com.example.whatsalert.Util.WhatsappUtil;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;

public class ReminderFragment extends Fragment {

    private static final String TAG = "ReminderFragment";

    private EditText dayInput, monthInput, yearInput, messageInput, contactNumberInput;
    private Button notifyButton;
    private TimePicker timePicker;

    public ReminderFragment() {
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reminder, container, false);

        // Initialize views
        dayInput = view.findViewById(R.id.input_day);

        monthInput = view.findViewById(R.id.input_month);
        yearInput = view.findViewById(R.id.input_year);
        messageInput = view.findViewById(R.id.message_input);

        AutoCompleteTextView contactDropdown = view.findViewById(R.id.whatsapp_contact_dropdown);
        notifyButton = view.findViewById(R.id.notify_button);
        timePicker = view.findViewById(R.id.time_picker);

        ImageView calendarIcon = view.findViewById(R.id.calender_icon);

        // Get current date
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentMonth = calendar.get(Calendar.MONTH) + 1; // Month is 0-based, so add 1
        int currentYear = calendar.get(Calendar.YEAR);
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY); // 24-hour format
        int currentMinute = calendar.get(Calendar.MINUTE);
        // Set the current year and make it non-editable

        yearInput.setText(String.valueOf(currentYear));
        yearInput.setTextColor(Color.GRAY);

        yearInput.setEnabled(false);

        int secondaryColor = ContextCompat.getColor(getContext(), R.color.Secondary_Color);
        int primaryColor = ContextCompat.getColor(getContext(), R.color.Primary_Color);

        messageInput.setTextColor(Color.BLACK);

        // Set default current day and month
        dayInput.setText(String.valueOf(currentDay));
        dayInput.setTextColor(primaryColor);
        monthInput.setText(String.valueOf(currentMonth));
        monthInput.setTextColor(primaryColor);

        // This calendar basically popups to set the date and time directly while setting a reminder
        calendarIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                // Set the selected day, month, and year
                                dayInput.setText(String.format("%02d", dayOfMonth)); // Set day
                                monthInput.setText(String.format("%02d", monthOfYear + 1)); // Set month (0-based, add 1)
                            }
                        },
                        currentYear, currentMonth - 1, currentDay // Initial date (month is 0-based in Calendar)
                );

                // Show the DatePicker
                datePickerDialog.show();

                // Change the "OK" button color
                Button positiveButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE);
                positiveButton.setTextColor(ContextCompat.getColor(getContext(), R.color.Secondary_Color)); // Set "OK" button color

                // Change the "Cancel" button color
                Button negativeButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE);
                negativeButton.setTextColor(ContextCompat.getColor(getContext(), R.color.Secondary_Color)); // Set "Cancel" button color

            }
        });
        contactDropdown.setOnTouchListener((v, event) -> {
            contactDropdown.setDropDownBackgroundResource(R.color.Background_Color2);
            contactDropdown.showDropDown();  // Show dropdown on touch
            return false;  // Ensure the touch event continues as normal
        });

        // Load WhatsApp contacts into dropdown
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, 1);
        } else {
            loadWhatsappContacts(contactDropdown);
        }

        // This will fetch the contact from whatsapp
        ArrayList<String> contactNames = WhatsappUtil.getWhatsappContactNames(getContext());
        if (!contactNames.isEmpty()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),  R.layout.dropdown_item, contactNames);

            contactDropdown.setAdapter(adapter);
            contactDropdown.setTextColor(Color.BLACK);

        } else {
            Toast.makeText(getContext(), "No WhatsApp contacts found.", Toast.LENGTH_SHORT).show();
        }


        // This will set and save all the reminders in db, and will save the reminder as well
        notifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String day = dayInput.getText().toString();
                String month = monthInput.getText().toString();
                String year = yearInput.getText().toString();
                String selectedContact = contactDropdown.getText().toString();

                // Extract selected time from TimePicker
                int hourValue, minuteValue;
                View rootView = getView();

                // Get time from TimePicker based on whether it's in 24-hour mode or AM/PM mode
                if (Build.VERSION.SDK_INT >= 23) {
                    hourValue = timePicker.getHour();
                    minuteValue = timePicker.getMinute();
                } else {
                    hourValue = timePicker.getCurrentHour();
                    minuteValue = timePicker.getCurrentMinute();
                }


                // ================================ VALIDATION ================================================

                // Validate month
                int monthValue = Integer.parseInt(month);
                if (monthValue > 12 || monthValue < 1 ) {
                    Snackbar.make(rootView, "Month cannot be higher than 12 or lower than 1", Snackbar.LENGTH_LONG).show();
                    Log.e(TAG, "Month cannot be higher than 12 or lower than 1");
                    //Toast.makeText(getContext(), "Month cannot be higher than 12  or lower than 1!", Toast.LENGTH_LONG).show();
                    return;
                }

                // Check if the month is lower than the current month
                if (monthValue < currentMonth) {
                    Log.e(TAG, "Month cannot be lower than the current month.");
                    Snackbar.make(rootView, "Month cannot be lower than the current month!", Snackbar.LENGTH_LONG).show();
                    //Toast.makeText(getContext(), "Month cannot be lower than the current month!", Toast.LENGTH_LONG).show();
                    return;
                }

                // Validate day
                int dayValue = Integer.parseInt(day);
                if (dayValue < 1 || dayValue > 31) {
                    Log.e(TAG, "Invalid day entered.");
                    //Toast.makeText(getContext(), "Please enter a valid day!", Toast.LENGTH_LONG).show();
                    Snackbar.make(rootView, "Please enter a valid day!", Snackbar.LENGTH_LONG).show();
                    return;
                }

                //check if the user tries to select a past date of current month
                if (monthValue == currentMonth && dayValue < currentDay) {
                    //Toast.makeText(getContext(), "Your reminder date cannot be in the past.", Toast.LENGTH_LONG).show();
                    Snackbar.make(rootView, "Your reminder date cannot be in the past.", Snackbar.LENGTH_LONG).show();
                    Log.e(TAG, "Your reminder date cannot be in the past.");
                    return;
                }

                // Validate message input is not empty
                String message = messageInput.getText().toString();
                if (message.isEmpty()) {
                    Log.e(TAG, "Message cannot be empty.");
                    Snackbar.make(rootView, "Message cannot be empty.", Snackbar.LENGTH_LONG).show();
                    //Toast.makeText(getContext(), "Message cannot be empty!", Toast.LENGTH_LONG).show();
                    return;
                }


                int yearValue = Integer.parseInt(year);

                // Check if the user has selected the current date
                if (yearValue == currentYear && monthValue == currentMonth && dayValue == currentDay) {
                    // If it's the same day, check the time
                    if (hourValue < currentHour || (hourValue == currentHour && minuteValue < currentMinute)) {
                        // User selected a past time on the same day
                        Log.e(TAG, "Cannot select a past time on the current date.");
                        //Toast.makeText(getContext(), "Cannot select a past time on the current date.", Toast.LENGTH_LONG).show();
                        Snackbar.make(rootView, "Cannot select a past time on the current date.", Snackbar.LENGTH_LONG).show();
                        return;
                    } else if (hourValue == currentHour && minuteValue == currentMinute) {
                        // User selected the exact current time
                        Log.e(TAG, "Cannot set a reminder for the current time as it may not trigger.");
                        //Toast.makeText(getContext(), "Please select a time at least 1 minute in the future.", Toast.LENGTH_LONG).show();
                        Snackbar.make(rootView, "Please select a time at least 1 minute in the future.", Snackbar.LENGTH_LONG).show();
                        return;
                    }
                }

                // Validate contact
                if (selectedContact.isEmpty()) {
                    //Toast.makeText(getContext(), "Please select a contact!", Toast.LENGTH_LONG).show();
                    Snackbar.make(rootView, "Please select a contact!", Snackbar.LENGTH_LONG).show();
                    return;
                }

                // Fetch and validate the contact number using WhatsAppUtil
                String contactName = WhatsappUtil.getContactNumberFromName(getContext(), selectedContact);
                if (contactName == null) {
                    //Toast.makeText(getContext(), "Contact is not linked to WhatsApp!", Toast.LENGTH_LONG).show();
                    Snackbar.make(rootView, "Contact is not linked to WhatsApp!", Snackbar.LENGTH_LONG).show();
                    return;
                }

                // If all validations pass, save the reminder
                DBOperations dbOperations = new DBOperations(getContext());
                String formattedTime = getFormattedTimeFromTimePicker();
                dbOperations.saveReminder(day, month, year, formattedTime, message, contactName,selectedContact);
                //Toast.makeText(getContext(), "Reminder saved!", Toast.LENGTH_SHORT).show();

            }
        });

        return view;
    }

    /**
     * Retrieves the selected time from the TimePicker and formats it as a string.
     * The format follows either 24-hour or 12-hour notation based on the TimePicker settings.
     *
     * @return A formatted time string (e.g., "02:30 PM" for 12-hour format or "14:30" for 24-hour format).
     */
    private String getFormattedTimeFromTimePicker() {
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        boolean is24HourView = timePicker.is24HourView();

        String amPm = "";

        // Convert to 12-hour format if TimePicker is not set to 24-hour mode
        if (!is24HourView) {
            if (hour >= 12) {
                amPm = "PM";
                if (hour > 12) {
                    hour -= 12;  // Convert to 12-hour format
                }
            } else {
                amPm = "AM";
                if (hour == 0) {
                    hour = 12;  // Midnight is 12 AM
                }
            }
        }
        // Format the time string based on the selected format
        return String.format("%02d:%02d %s", hour, minute, amPm);
    }

    /**
     * Loads WhatsApp contacts into an AutoCompleteTextView dropdown.
     * If the necessary permission is not granted, it requests it before proceeding.
     *
     * @param dropdown The AutoCompleteTextView where WhatsApp contacts will be displayed as suggestions.
     */
    public void loadWhatsappContacts(AutoCompleteTextView dropdown) {

        // Check if the READ_CONTACTS permission is granted
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, 1);
            return;
        }

        // Retrieve WhatsApp contact names
        ArrayList<String> contactNames = WhatsappUtil.getWhatsappContactNames(getContext());

        // Initialize ArrayAdapter with the contact names
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, contactNames) {
            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();

                        if (constraint == null || constraint.length() == 0) {
                            // If no input is provided, show all contacts
                            results.values = contactNames;
                            results.count = contactNames.size();
                        } else {
                            // Filter contacts based on user input
                            ArrayList<String> filteredContacts = new ArrayList<>();
                            for (String contact : contactNames) {
                                if (contact.toLowerCase().contains(constraint.toString().toLowerCase())) {
                                    filteredContacts.add(contact);
                                }
                            }


                            results.values = filteredContacts;
                            results.count = filteredContacts.size();
                        }
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        if (results != null && results.count > 0) {
                            // Update the dropdown with filtered contacts
                            clear();
                            addAll((ArrayList<String>) results.values);
                            notifyDataSetChanged();
                        } else {
                            // Handle no results found case
                            clear();
                            add("No contacts found");
                            notifyDataSetChanged();
                        }
                    }
                };
            }
        };

        // Set the adapter for AutoCompleteTextView
        dropdown.setAdapter(adapter);
    }
}
