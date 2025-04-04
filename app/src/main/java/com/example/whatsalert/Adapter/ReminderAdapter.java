package com.example.whatsalert.Adapter;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkManager;

import com.example.whatsalert.Model.Reminder;
import com.example.whatsalert.R;
import com.example.whatsalert.Util.DBOperations;

import java.util.Collections;
import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private static final String TAG = "ReminderAdapter";

    private List<Reminder> reminderList;
    private Context context;

    public ReminderAdapter(List<Reminder> reminderList, Context context) {
        this.reminderList = reminderList;
        this.context = context;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_card, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminderList.get(position);

        // Set reminder time and date
        holder.reminderTime.setText(reminder.getTime());
        holder.reminderDate.setText(reminder.getDate());

        // Create SpannableString for message
        String messageText = " " + reminder.getMessage();
        SpannableString messageSpannable = new SpannableString(messageText);

        // Find the end index of the "Message:" label part
        int endOfLabel = messageText.indexOf(reminder.getMessage());

        // Apply bold style to the "Message:" label
        messageSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, endOfLabel, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the formatted text to the message TextView
        holder.reminderMessage.setText(messageSpannable);

        // Create SpannableString to format the contact text
        String contactText = "Contact: " + reminder.getContactName();
        SpannableString spannableString = new SpannableString(contactText);

        // Find the end index of the label part
        int endOfLabel2 = contactText.indexOf(reminder.getContactName());

        // Apply bold style to the label part
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, endOfLabel2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the formatted text to the TextView
        holder.reminderContact.setText(spannableString);

        // Highlight the card if the notification has been sent
        if (reminder.isNotified()) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            TextView deletingSoonText = holder.itemView.findViewById(R.id.deletingSoonTextView);
            if (deletingSoonText != null) {
                deletingSoonText.setVisibility(View.VISIBLE);
            }

        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            TextView deletingSoonText = holder.itemView.findViewById(R.id.deletingSoonTextView);
            if (deletingSoonText != null) {
                deletingSoonText.setVisibility(View.GONE);
            }

        }

        // Add shadow programmatically
        if (holder.itemView instanceof CardView) {
            CardView cardView = (CardView) holder.itemView;
            cardView.setCardElevation(8f); // Set the elevation in dp
            cardView.setRadius(16f);      // Set the corner radius in dp
        }

        // Set click listener for the info icon
        holder.infoIcon.setOnClickListener(v -> {
            // Show the dialog with full info
            showReminderDialog(reminder, position);
        });

        // Set onClickListener to show full message in a dialog
        holder.itemView.setOnClickListener(v -> showReminderDialog(reminder, position));
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    /**
     * Displays a dialog with the details of a reminder.
     * The dialog provides options to edit, delete, or close the reminder.
     *
     * @param reminder The reminder object containing the details.
     * @param position The position of the reminder in the list.
     */
    private void showReminderDialog(Reminder reminder, int position) {
        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_reminder, null);
        builder.setView(dialogView);

        // Prevent the dialog from closing on outside touch
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

        // Find the views in the dialog layout
        TextView reminderMessageView = dialogView.findViewById(R.id.reminder_message);
        ImageView deleteIcon = dialogView.findViewById(R.id.delete_reminder_icon);
        ImageView editIcon = dialogView.findViewById(R.id.edit_reminder_icon);
        ImageView closeIcon = dialogView.findViewById(R.id.close_reminder_icon);

        if (reminder.isNotified()) {
            editIcon.setVisibility(View.GONE);
        }

        // Set the message in the dialog
        String messageText = "Message: " + reminder.getMessage();

        // Create a SpannableString from the text
        SpannableString spannableString = new SpannableString(messageText);

        // Apply bold style to the "Message:" part of the text
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the formatted text to your TextView
        reminderMessageView.setText(spannableString);
        reminderMessageView.setMovementMethod(new ScrollingMovementMethod());

        // Set onClickListener for the delete icon
        deleteIcon.setOnClickListener(v -> {
            // Call deleteReminder without modifying its implementation
            deleteReminder(position);
            // Dismiss the dialog
            dialog.dismiss();
        });


        // Set onClickListener for the edit icon
        editIcon.setOnClickListener(v -> {
            // Open the edit reminder dialog
            showEditReminderDialog(reminder, position);
            // Dismiss the current dialog
            dialog.dismiss();
        });

        // Set onClickListener for the close icon
        closeIcon.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog
        dialog.show();

        // Decrease the width of the dialog programmatically
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.85); // 85% of screen width
        dialog.getWindow().setAttributes(params);
    }

    /**
     * Deletes the specified reminder from both the database and the UI.
     *
     * @param position The position of the reminder in the list.
     */
    private void deleteReminder(int position) {
        // Get the reminder at the given position
        Reminder reminder = reminderList.get(position);

        // Access the id directly from the reminder object
        int reminderId = reminder.getId();  // Get the id

        // Cancel the scheduled WorkManager task for this reminder
        Log.d(TAG, "Cancelling WorkManager task for reminder ID: " + reminderId);
        WorkManager.getInstance(context).cancelAllWorkByTag(String.valueOf(reminderId));

        // Use the id to delete the reminder from the database
        DBOperations dbOperations = new DBOperations(context);
        dbOperations.deleteReminderById(reminderId);  // Assuming deleteReminderById accepts an id

        // Remove the reminder from the list and notify the adapter
        reminderList.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Displays a dialog that allows the user to edit an existing reminder.
     *
     * @param reminder The reminder object to be edited.
     * @param position The position of the reminder in the list.
     */
    private void showEditReminderDialog(Reminder reminder, int position) {
        // Create the edit dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View editDialogView = LayoutInflater.from(context).inflate(R.layout.edit_reminder, null);
        builder.setView(editDialogView);

        // Prevent the dialog from closing on outside touch
        builder.setCancelable(false);

        AlertDialog editDialog = builder.create();
        editDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

        // Find the views in the edit dialog layout
        EditText editMessageInput = editDialogView.findViewById(R.id.reminder_message_edit);
        TextView timeTextView = editDialogView.findViewById(R.id.reminder_time);
        TextView dateTextView = editDialogView.findViewById(R.id.reminder_date);
        TextView contactTextView = editDialogView.findViewById(R.id.reminder_contact);
        Button saveButton = editDialogView.findViewById(R.id.save_button);
        ImageView closeIcon = editDialogView.findViewById(R.id.close_reminder_icon);

        // Pre-fill the message input with the current message
        editMessageInput.setText(reminder.getMessage());
        editMessageInput.setTextColor(Color.BLACK);

        // Set the time, date, and contact number (non-editable TextViews)
        timeTextView.setText("Time: " + reminder.getTime());
        dateTextView.setText("Date: " + reminder.getDate());
        contactTextView.setText("Contact: " + reminder.getContactName());

        // Set onClickListener for the save button
        saveButton.setOnClickListener(v -> {
            String updatedMessage = editMessageInput.getText().toString().trim();

            if (!updatedMessage.isEmpty()) {
                // Update the reminder with the new message
                editReminder(reminder, position, updatedMessage);
                // Dismiss the edit dialog
                editDialog.dismiss();
            } else {
                // Show an error message (optional)
                editMessageInput.setError("Message cannot be empty");
            }
        });

        // Set onClickListener for the close icon
        closeIcon.setOnClickListener(v -> editDialog.dismiss());

        // Show the edit dialog
        editDialog.show();

        // Decrease the width of the dialog programmatically
        WindowManager.LayoutParams params = editDialog.getWindow().getAttributes();
        params.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.85); // 85% of screen width
        editDialog.getWindow().setAttributes(params);
    }


    /**
     * Updates the specified reminder's message in the database.
     *
     * @param reminderId The ID of the reminder to be updated.
     * @param updatedMessage The new message content for the reminder.
     * @return true if the update was successful, false otherwise.
     */
    private boolean updateReminderInDatabase(int reminderId, String updatedMessage) {
        DBOperations dbOperations = new DBOperations(context);
        ContentValues values = new ContentValues();
        values.put("message", updatedMessage);

        int rowsUpdated = dbOperations.updateReminder(reminderId, values);
        if (rowsUpdated > 0) {
            Log.d(TAG, "Reminder updated successfully in database");
            return true;
        } else {
            Log.e(TAG, "Failed to update reminder in database");
            return false;
        }
    }

    /**
     * Updates the reminder's message in the list and notifies the adapter
     * to refresh the UI accordingly.
     *
     * @param reminder The reminder object to be updated.
     * @param position The position of the reminder in the list.
     * @param updatedMessage The new message content for the reminder.
     */
    private void updateReminderInList(Reminder reminder, int position, String updatedMessage) {
        reminder.setMessage(updatedMessage);
        notifyItemChanged(position);
        Log.d(TAG, "Reminder updated in list and adapter notified");
    }

    /**
     * Acts as an intermediary between updating a reminder in the database
     * and updating it in the displayed list. If the database update is successful,
     * the UI list is also updated.
     *
     * @param reminder The reminder object to be updated.
     * @param position The position of the reminder in the list.
     * @param updatedMessage The new message for the reminder.
     */
    private void editReminder(Reminder reminder, int position, String updatedMessage) {
        if (updateReminderInDatabase(reminder.getId(), updatedMessage)) {
            updateReminderInList(reminder, position, updatedMessage);
        } else {
            Log.e(TAG, "Failed to update reminder");
        }
    }

    /**
     * Updates the reminder list by reversing the order to ensure
     * the latest reminders appear at the top.
     *
     * @param newReminders The updated list of reminders.
     */
    public void updateReminders(List<Reminder> newReminders) {
        Collections.reverse(newReminders);
        this.reminderList = newReminders;
        notifyDataSetChanged();
    }

    public static class ReminderViewHolder extends RecyclerView.ViewHolder {

        TextView reminderMessage;
        TextView reminderTime;
        TextView reminderDate;
        TextView reminderContact;
        TextView deletingSoonText;
        ImageView infoIcon;


        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            reminderMessage = itemView.findViewById(R.id.messageTextView);
            reminderTime = itemView.findViewById(R.id.timeTextView);
            reminderDate = itemView.findViewById(R.id.dateTextView);
            reminderContact = itemView.findViewById(R.id.contactTextView);
            deletingSoonText = itemView.findViewById(R.id.deletingSoonTextView);
            infoIcon = itemView.findViewById(R.id.infoIcon);
        }
    }

}
