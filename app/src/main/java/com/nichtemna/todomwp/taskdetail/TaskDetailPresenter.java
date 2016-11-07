package com.nichtemna.todomwp.taskdetail;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.nichtemna.todomwp.data.Task;
import com.nichtemna.todomwp.data.source.TaskDataSource;
import com.nichtemna.todomwp.data.source.TaskRepository;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by shyslin on 11/7/16.
 */

public class TaskDetailPresenter implements TaskDetailContract.Presenter {

    private final TaskRepository taskRepository;

    private final TaskDetailContract.View taskDetailView;

    @Nullable
    private String taskId;

    public TaskDetailPresenter(String taskId, TaskRepository taskRepository, TaskDetailContract.View taskDetailView) {
        this.taskId = taskId;
        this.taskDetailView = checkNotNull(taskDetailView, "taskDetailView cannot be null");
        this.taskRepository = checkNotNull(taskRepository, "taskRepository cannot be null");

        taskDetailView.setPresenter(this);
    }

    @Override
    public void start() {
        openTask();
    }

    private void openTask() {
        if (TextUtils.isEmpty(taskId)) {
            taskDetailView.showMissingTask();
            return;
        }

        taskDetailView.setLoadingIndicator(true);
        taskRepository.getTask(taskId, new TaskDataSource.GetTaskCallback() {
            @Override
            public void onTaskLoaded(Task task) {
                // The view may not be able to handle UI updates anymore
                if (!taskDetailView.isActive()) {
                    return;
                }

                taskDetailView.setLoadingIndicator(false);
                if (null == task) {
                    taskDetailView.showMissingTask();
                } else {
                    showTask(task);
                }
            }

            @Override
            public void onDataNotAvailable() {
                // The view may not be able to handle UI updates anymore
                if (!taskDetailView.isActive()) {
                    return;
                }
                taskDetailView.showMissingTask();
            }
        });
    }

    @Override
    public void editTask() {
        if (TextUtils.isEmpty(taskId)) {
            taskDetailView.showMissingTask();
            return;
        }

        taskDetailView.showEditTask(taskId);
    }

    @Override
    public void deleteTask() {
        if (TextUtils.isEmpty(taskId)) {
            taskDetailView.showMissingTask();
            return;
        }

        taskRepository.deleteTask(taskId);
        taskDetailView.showTaskDeleted();
    }

    @Override
    public void completeTask() {
        if (TextUtils.isEmpty(taskId)) {
            taskDetailView.showMissingTask();
            return;
        }

        taskRepository.completeTask(taskId);
        taskDetailView.showTaskMarkedComplete();
    }

    @Override
    public void activateTask() {
        if (TextUtils.isEmpty(taskId)) {
            taskDetailView.showMissingTask();
            return;
        }
    }

    private void showTask(@NonNull Task task) {
        String title = task.getTitle();
        String description = task.getDescription();

        if (TextUtils.isEmpty(title)) {
            taskDetailView.hideTitle();
        } else {
            taskDetailView.showTitle(title);
        }

        if (TextUtils.isEmpty(description)) {
            taskDetailView.hideDescription();
        } else {
            taskDetailView.showDescription(description);
        }
        taskDetailView.showCompletionStatus(task.isCompleted());
    }
}
