package com.nichtemna.todomwp.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Objects;
import java.util.UUID;

/**
 * Created by Lina Shyshova on 06.11.16.
 */

public class Task {
    @NonNull
    private final String mId;

    @Nullable
    private final String mTitle;

    @Nullable
    private final String mDescription;

    private final boolean mCompleted;

    /**
     * Use this constructor to create a new active Task.
     *
     * @param title       title of the task
     * @param description description of the task
     */
    public Task(@Nullable String title, @Nullable String description) {
        this(title, description, UUID.randomUUID().toString(), false);
    }

    /**
     * Use this constructor to create an active Task if the Task already has an id (copy of another
     * Task).
     *
     * @param mTitle       title of the task
     * @param mDescription description of the task
     * @param mId          id of the task
     */
    public Task(@Nullable String mTitle, @Nullable String mDescription, @NonNull String mId) {
        this(mTitle, mDescription, UUID.randomUUID().toString(), false);
    }

    /**
     * Use this constructor to create a new completed Task.
     *
     * @param mTitle       title of the task
     * @param mDescription description of the task
     * @param mCompleted   true if the task is completed, false if it's active
     */
    public Task(@Nullable String mTitle, @Nullable String mDescription, boolean mCompleted) {
        this(mTitle, mDescription, UUID.randomUUID().toString(), mCompleted);
    }

    /**
     * Use this constructor to specify a completed Task if the Task already has an id (copy of
     * another Task).
     *
     * @param mTitle       title of the task
     * @param mDescription description of the task
     * @param mId          id of the task
     * @param mCompleted   true if the task is completed, false if it's active
     */
    public Task(@Nullable String mTitle, @Nullable String mDescription, @NonNull String mId, boolean mCompleted) {
        this.mId = mId;
        this.mTitle = mTitle;
        this.mDescription = mDescription;
        this.mCompleted = mCompleted;
    }

    @NonNull
    public String getId() {
        return mId;
    }

    @Nullable
    public String getTitle() {
        return mTitle;
    }

    @Nullable
    public String getTitleForList() {
        if (!TextUtils.isEmpty(mTitle)) {
            return mTitle;
        } else {
            return mDescription;
        }
    }

    @Nullable
    public String getDescription() {
        return mDescription;
    }

    public boolean isCompleted() {
        return mCompleted;
    }

    public boolean isActive() {
        return !mCompleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return mCompleted == task.mCompleted &&
                Objects.equals(mId, task.mId) &&
                Objects.equals(mTitle, task.mTitle) &&
                Objects.equals(mDescription, task.mDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mId);
    }

    @Override
    public String toString() {
        return "Task with title " + mTitle;
    }
}
