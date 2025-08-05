package com.desaysv.psmap.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.desaysv.psmap.R
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by uidq0728 on 2023-5-17
 * Describe:
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

    @get:Rule
    val  activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun checkTextView() {
        //onView(withId(R.id.btn)).perform(click())
        //onView(withId(R.id.textView)).check(matches(withText("Data Change")))
    }
}
