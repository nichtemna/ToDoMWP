package com.nichtemna.todomwp.tasks;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.nichtemna.todomwp.addedittask.AddEditTaskActivity;
import com.nichtemna.todomwp.data.Task;
import com.nichtemna.todomwp.data.source.TaskDataSource;
import com.nichtemna.todomwp.data.source.TaskRepository;
import com.nichtemna.todomwp.util.EspressoIdlingResource;

import java.util.ArrayList;
import java.util.List;

import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;

/**
 * Created by Lina Shyshova on 06.11.16.
 */

public class TasksPresenter implements TasksContract.Presenter {

    private final TaskRepository taskRepository;

    private final TasksContract.View tasksView;

    private TasksFilterType currentFiltering = TasksFilterType.ALL_TASKS;

    private boolean firstLoad;

    public TasksPresenter(@NonNull TaskRepository taskRepository, @NonNull TasksContract.View tasksView) {
        this.taskRepository = checkNotNull(taskRepository, "tasksRepository cannot be null");
        this.tasksView = checkNotNull(tasksView, "tasksView cannot be null!");

        tasksView.setPresenter(this);
    }

    @Override
    public void start() {
        loadTasks(false);
    }

    @Override
    public void result(int requestCode, int resultCode) {
        if (AddEditTaskActivity.REQUEST_ADD_TASK == requestCode && Activity.RESULT_OK == resultCode) {
            tasksView.showSuccessfullySavedMessage();
        }
    }

    @Override
    public void loadTasks(boolean forceUpdate) {
        // Simplification for sample: a network reload will be forced on first load.
        loadTasks(forceUpdate || firstLoad, true);
        firstLoad = false;
    }

    /**
     * @param forceUpdate   Pass in true to refresh the data in the {@link TaskDataSource}
     * @param showLoadingUI Pass in true to display a loading icon in the UI
     */
    private void loadTasks(final boolean forceUpdate, final boolean showLoadingUI) {
        if (showLoadingUI) {
            tasksView.setLoadingIndicator(true);
        }

        if (forceUpdate) {
            taskRepository.refreshTasks();
        }

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice

        taskRepository.getTasks(new TaskDataSource.LoadTasksCallback() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                List<Task> tasksToShow = new ArrayList<>();

                // This callback may be called twice, once for the cache and once for loading
                // the data from the server API, so we check before decrementing, otherwise
                // it throws "Counter has been corrupted!" exception.
                if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
                    EspressoIdlingResource.decrement();
                }

                // We filter the tasks based on the requestType
                for (Task task : tasks) {
                    switch (currentFiltering) {
                        case ACTIVE_TASKS:
                            if (task.isActive()) {
                                tasksToShow.add(task);
                            }
                            break;
                        case COMPLETED_TASKS:
                            if (task.isCompleted()) {
                                tasksToShow.add(task);
                            }
                            break;
                        case ALL_TASKS:
                            tasksToShow.add(task);
                            break;
                        default:
                            tasksToShow.add(task);
                    }
                }

                if (!tasksView.isActive()) {
                    return;
                }

                if (showLoadingUI) {
                    tasksView.setLoadingIndicator(false);
                }

                processTasks(tasksToShow);
            }

            @Override
            public void onDataNotAvailable() {
                if (!tasksView.isActive()) {
                    return;
                }

                tasksView.showLoadingTasksError();
            }
        });
    }

    private void processTasks(List<Task> tasks) {
        if (tasks.isEmpty()) {
            processEmptyTasks();
        } else {
            tasksView.showTasks(tasks);
            showFilteringLabel();
        }
    }

    private void processEmptyTasks() {
        switch (currentFiltering) {
            case ACTIVE_TASKS:
                tasksView.showNoActiveTasks();
                break;
            case COMPLETED_TASKS:
                tasksView.showNoCompletedTasks();
                break;
            default:
                tasksView.showNoTasks();
                break;
        }
    }

    private void showFilteringLabel() {
        switch (currentFiltering) {
            case ACTIVE_TASKS:
                tasksView.showActiveFilterLabel();
                break;
            case COMPLETED_TASKS:
                tasksView.showCompleteFilterLabel();
                break;
            default:
                tasksView.showAllFilterLabel();
                break;
        }
    }

    @Override
    public void addNewTask() {
        tasksView.showAddTask();
    }

    @Override
    public void openTaskDetails(@NonNull Task requestedTask) {
        checkNotNull(requestedTask, "requestedTask cannot be null!");
        tasksView.showTaskDetailUi(requestedTask.getId());
    }

    @Override
    public void completeTask(@NonNull Task completedTask) {
        checkNotNull(completedTask, "completedTask cannot be null!");
        taskRepository.completeTask(completedTask);
        tasksView.showTaskMarkedComplete();
        loadTasks(false, false);
    }

    @Override
    public void activateTask(@NonNull Task activeTask) {
        checkNotNull(activeTask, "activeTask cannot be null!");
        taskRepository.activateTask(activeTask);
        tasksView.showTaskMarkedActive();
        loadTasks(false, false);
    }

    @Override
    public void clearCompletedTasks() {
        taskRepository.clearCompletedTasks();
        tasksView.showCompletedTasksCleared();
        loadTasks(false, false);
    }

    /**
     * Sets the current task filtering type.
     *
     * @param requestType Can be {@link TasksFilterType#ALL_TASKS},
     *                    {@link TasksFilterType#COMPLETED_TASKS}, or
     *                    {@link TasksFilterType#ACTIVE_TASKS}
     */
    @Override
    public void setFiltering(TasksFilterType requestType) {
        currentFiltering = requestType;
    }

    @Override
    public TasksFilterType getFiltering() {
        return currentFiltering;
    }
}
