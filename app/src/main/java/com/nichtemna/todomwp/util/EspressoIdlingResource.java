package com.nichtemna.todomwp.util;

import android.support.test.espresso.IdlingResource;

/**
 * Created by Lina Shyshova on 06.11.16.
 */

public class EspressoIdlingResource {
    public static final String RESOURCE = "GLOBAL";

    private static SimpleCountingIdlingResource countingIdlingResource = new SimpleCountingIdlingResource(RESOURCE);

    public static void increment() {
        countingIdlingResource.increment();
    }

    public static void decrement() {
        countingIdlingResource.decrement();
    }

    public static IdlingResource getIdlingResource() {
        return countingIdlingResource;
    }
}
