package com.nichtemna.todomwp.taskdetail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.nichtemna.todomwp.R;
import com.nichtemna.todomwp.addedittask.AddEditTaskActivity;
import com.nichtemna.todomwp.addedittask.AddEditTaskFragment;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by shyslin on 11/7/16.
 */

public class TaskDetailFragment extends Fragment implements TaskDetailContract.View {

    @NonNull
    private static final String ARGUMENT_TASK_ID = "TASK_ID";

    @NonNull
    private static final int REQUEST_EDIT_TASK = 1;

    private TaskDetailContract.Presenter presenter;

    private TextView detailTitle;

    private TextView detailDescription;

    private CheckBox detailCompleteStatus;

    public static TaskDetailFragment newInstance(@Nullable String taskId) {
        Bundle arguments = new Bundle();
        arguments.putString(ARGUMENT_TASK_ID, taskId);
        TaskDetailFragment fragment = new TaskDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_task_detail, container, false);
        setHasOptionsMenu(true);
        detailTitle = (TextView) root.findViewById(R.id.task_detail_title);
        detailDescription = (TextView) root.findViewById(R.id.task_detail_description);
        detailCompleteStatus = (CheckBox) root.findViewById(R.id.task_detail_complete);

        // Set up floating action button
        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_edit_task);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.editTask();
            }
        });

        return root;
    }


    @Override
    public void setPresenter(@NonNull TaskDetailContract.Presenter presenter) {
       this.presenter = checkNotNull(presenter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                presenter.deleteTask();
                return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.taskdetail_fragment_menu, menu);
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        if (active) {
            detailTitle.setText("");
            detailDescription.setText(R.string.loading);
        }
    }

    @Override
    public void showMissingTask() {
        detailTitle.setText("");
        detailDescription.setText(R.string.no_data);
    }

    @Override
    public void hideTitle() {
        detailTitle.setVisibility(View.GONE);
    }

    @Override
    public void showTitle(String title) {
        detailTitle.setVisibility(View.VISIBLE);
        detailTitle.setText(title);
    }

    @Override
    public void hideDescription() {
        detailDescription.setVisibility(View.GONE);
    }

    @Override
    public void showDescription(String description) {
        detailDescription.setVisibility(View.VISIBLE);
        detailDescription.setText(description);
    }

    @Override
    public void showCompletionStatus(boolean complete) {

    }

    @Override
    public void showEditTask(String taskId) {
        Intent intent = new Intent(getContext(), AddEditTaskActivity.class);
        intent.putExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId);
        startActivityForResult(intent, REQUEST_EDIT_TASK);
    }

    @Override
    public void showTaskDeleted() {
        getActivity().finish();
    }

    @Override
    public void showTaskMarkedComplete() {
        Snackbar.make(getView(), getString(R.string.task_marked_complete), Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void showTaskMarkedActive() {
        Snackbar.make(getView(), getString(R.string.task_marked_active), Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
