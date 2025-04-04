package com.example.whatsalert.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsalert.Adapter.ReminderAdapter;
import com.example.whatsalert.Model.Reminder;
import com.example.whatsalert.R;
import com.example.whatsalert.databinding.FragmentFirstBinding;
import com.example.whatsalert.Util.DBOperations;

import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private FragmentFirstBinding binding;
    private RecyclerView recyclerView;
    private ReminderAdapter reminderAdapter;
    private DBOperations dbOperations;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize DBOperations and fetch reminders
        dbOperations = new DBOperations(getContext());  // Pass context to the constructor
        List<Reminder> reminders = dbOperations.getAllReminders();  // Fetch reminders

        // Set the adapter for RecyclerView
        reminderAdapter = new ReminderAdapter(reminders,getContext());
        recyclerView.setAdapter(reminderAdapter);

        // Set the onClickListener for the "Set Reminder" card
        View setReminderCard = view.findViewById(R.id.setReminderCard);
        setReminderCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ReminderFragment
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_HomeFragment_to_ReminderFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshReminders();
    }

    /**
     * Refreshes the list of reminders by fetching the latest data from the database.
     * If reminders are found, updates the adapter and notifies it of data changes.
     * Logs a message if no reminders are available.
     */
    private void refreshReminders() {
        // Query the DB for the latest reminders
        DBOperations dbOperations = new DBOperations(getContext());
        List<Reminder> updatedReminders = dbOperations.getAllReminders();

        if (updatedReminders != null) {
            // Update the adapter's data set
            reminderAdapter.updateReminders(updatedReminders);

            // Notify the adapter that the data has changed
            reminderAdapter.notifyDataSetChanged();
        } else {
            Log.d(TAG, "No reminders found.");
        }
    }
}
