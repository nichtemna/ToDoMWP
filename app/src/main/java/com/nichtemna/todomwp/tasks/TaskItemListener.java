package com.nichtemna.todomwp.tasks;

import com.nichtemna.todomwp.data.Task;

/**
 * Created by shyslin on 11/7/16.
 */

public interface TaskItemListener {

    void onTaskClick(Task clickedTask);

    void onCompleteTaskClick(Task completedTask);

    void onActivateTaskClick(Task activatedTask);
}
