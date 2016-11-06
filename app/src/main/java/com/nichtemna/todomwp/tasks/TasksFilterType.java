package com.nichtemna.todomwp.tasks;

/**
 * Created by Lina Shyshova on 06.11.16.
 */

public enum TasksFilterType {

    /**
     * Do not filter tasks.
     */
    ALL_TASKS,

    /**
     * Filters only the active (not completed yet) tasks.
     */
    ACTIVE_TASKS,

    /**
     * Filters only the completed tasks.
     */
    COMPLETED_TASKS
}
