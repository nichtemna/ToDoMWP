package com.nichtemna.todomwp.data.source;

import android.support.annotation.NonNull;

import com.nichtemna.todomwp.data.Task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Concrete implementation to load tasks from the data sources into a cache.
 * <p>
 * For simplicity, this implements a dumb synchronisation between locally persisted data and data
 * obtained from the server, by using the remote data source only if the local database doesn't
 * exist or is empty.
 */
public class TaskRepository implements TaskDataSource {

    private static TaskRepository INSTANCE = null;
    private final TaskDataSource taskRemoteDataSource;
    private final TaskDataSource taskLocalDataSource;

    Map<String, Task> cachedTasks;

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    boolean cacheIsDirty = false;

    private TaskRepository(@NonNull TaskDataSource taskRemoteDataSource, @NonNull TaskDataSource taskLocalDataSource) {
        this.taskRemoteDataSource = checkNotNull(taskRemoteDataSource);
        this.taskLocalDataSource = checkNotNull(taskLocalDataSource);
    }

    public static TaskRepository getInstance(TaskDataSource taskRemoteDataSource, TaskDataSource taskLocalDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new TaskRepository(taskRemoteDataSource, taskLocalDataSource);
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    /**
     * Gets tasks from cache, local data source (SQLite) or remote data source, whichever is
     * available first.
     * <p>
     * Note: {@link LoadTasksCallback#onDataNotAvailable()} is fired if all data sources fail to
     * get the data.
     */
    @Override
    public void getTasks(@NonNull final LoadTasksCallback callback) {
        checkNotNull(callback);

        // Respond immediately with cache if available and not dirty
        if (cachedTasks != null && !cacheIsDirty) {
            callback.onTasksLoaded(new ArrayList<Task>(cachedTasks.values()));
            return;
        }

        if (cacheIsDirty) {
            // If the cache is dirty we need to fetch new data from the network.
            getTasksFromRemoteDataSource(callback);
        } else {
            taskLocalDataSource.getTasks(new LoadTasksCallback() {
                @Override
                public void onTasksLoaded(List<Task> tasks) {
                    refreshCache(tasks);
                    callback.onTasksLoaded(new ArrayList<Task>(cachedTasks.values()));
                }

                @Override
                public void onDataNotAvailable() {
                    getTasksFromRemoteDataSource(callback);
                }
            });
        }
    }

    /**
     * Gets tasks from local data source (sqlite) unless the table is new or empty. In that case it
     * uses the network data source. This is done to simplify the sample.
     * <p>
     * Note: {@link GetTaskCallback#onDataNotAvailable()} is fired if both data sources fail to
     * get the data.
     */
    @Override
    public void getTask(@NonNull final String taskId, @NonNull final GetTaskCallback callback) {
        checkNotNull(taskId);
        checkNotNull(callback);

        final Task cachedTask = getTaskWithId(taskId);

        // Respond immediately with cache if available
        if (cachedTask != null) {
            callback.onTaskLoaded(cachedTask);
            return;
        }

        // Load from server/persisted if needed.

        // Is the task in the local data source? If not, query the network.
        taskLocalDataSource.getTask(taskId, new GetTaskCallback() {
            @Override
            public void onTaskLoaded(Task task) {
                // Do in memory cache update to keep the app UI up to date
                if (cachedTasks == null) {
                    cachedTasks = new LinkedHashMap();
                }
                cachedTasks.put(taskId, task);
                callback.onTaskLoaded(task);
            }

            @Override
            public void onDataNotAvailable() {
                taskRemoteDataSource.getTask(taskId, new GetTaskCallback() {
                    @Override
                    public void onTaskLoaded(Task task) {
                        // Do in memory cache update to keep the app UI up to date
                        if (cachedTasks == null) {
                            cachedTasks = new LinkedHashMap();
                        }
                        cachedTasks.put(taskId, task);
                        callback.onTaskLoaded(task);
                    }

                    @Override
                    public void onDataNotAvailable() {
                        callback.onDataNotAvailable();
                    }
                });
            }
        });
    }

    @Override
    public void saveTask(@NonNull Task task) {
        checkNotNull(task);

        taskRemoteDataSource.saveTask(task);
        taskLocalDataSource.saveTask(task);

        // Do in memory cache update to keep the app UI up to date
        if (cachedTasks == null) {
            cachedTasks = new LinkedHashMap<>();
        }
        cachedTasks.put(task.getId(), task);
    }

    @Override
    public void completeTask(@NonNull Task task) {
        checkNotNull(task);

        taskRemoteDataSource.completeTask(task);
        taskLocalDataSource.completeTask(task);

        Task completedTask = new Task(task.getTitle(), task.getDescription(), task.getId(), true);

        // Do in memory cache update to keep the app UI up to date
        if (cachedTasks == null) {
            cachedTasks = new LinkedHashMap<>();
        }
        cachedTasks.put(task.getId(), completedTask);
    }

    @Override
    public void completeTask(@NonNull String taskId) {
        checkNotNull(taskId);
        completeTask(getTaskWithId(taskId));
    }

    @Override
    public void activateTask(@NonNull Task task) {
        checkNotNull(task);

        taskRemoteDataSource.activateTask(task);
        taskLocalDataSource.activateTask(task);

        Task activeTask = new Task(task.getTitle(), task.getDescription(), task.getId());

        // Do in memory cache update to keep the app UI up to date
        if (cachedTasks == null) {
            cachedTasks = new LinkedHashMap<>();
        }
        cachedTasks.put(task.getId(), activeTask);
    }

    @Override
    public void activateTask(@NonNull String taskId) {
        checkNotNull(taskId);
        activateTask(getTaskWithId(taskId));
    }

    @Override
    public void clearCompletedTasks() {
        taskRemoteDataSource.clearCompletedTasks();
        taskLocalDataSource.clearCompletedTasks();

        // Do in memory cache update to keep the app UI up to date
        if (cachedTasks == null) {
            cachedTasks = new LinkedHashMap<>();
        }
        Iterator<Map.Entry<String, Task>> it = cachedTasks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Task> entry = it.next();
            if (entry.getValue().isCompleted()) {
                it.remove();
            }
        }
    }

    public void refreshTasks() {
        cacheIsDirty = true;
    }

    @Override
    public void deleteAllTasks() {
        taskLocalDataSource.deleteAllTasks();
        taskRemoteDataSource.deleteAllTasks();

        if (cachedTasks == null) {
            cachedTasks = new LinkedHashMap<>();
        }
        cachedTasks.clear();
    }

    @Override
    public void deleteTask(@NonNull String taskId) {
        taskLocalDataSource.deleteTask(checkNotNull(taskId));
        taskRemoteDataSource.deleteTask(checkNotNull(taskId));

        cachedTasks.remove(taskId);
    }

    private void refreshCache(List<Task> tasks) {
        if (cachedTasks == null) {
            cachedTasks = new LinkedHashMap<>();
        }
        cachedTasks.clear();
        for (Task task : tasks) {
            cachedTasks.put(task.getId(), task);
        }
        cacheIsDirty = false;
    }

    private void getTasksFromRemoteDataSource(@NonNull final LoadTasksCallback callback) {
        taskRemoteDataSource.getTasks(new LoadTasksCallback() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                refreshCache(tasks);
                refreshLocalDataSource(tasks);
                callback.onTasksLoaded(tasks);
            }

            @Override
            public void onDataNotAvailable() {
                callback.onDataNotAvailable();
            }
        });
    }

    private void refreshLocalDataSource(List<Task> tasks) {
        taskLocalDataSource.deleteAllTasks();
        for (Task task : tasks) {
            taskLocalDataSource.saveTask(task);
        }
    }

    private Task getTaskWithId(String taskId) {
        checkNotNull(taskId);
        if (cachedTasks == null || cachedTasks.isEmpty()) {
            return null;
        } else {
            return cachedTasks.get(taskId);
        }
    }
}
