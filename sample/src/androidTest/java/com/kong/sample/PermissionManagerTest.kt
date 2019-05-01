package com.kong.sample

import android.Manifest
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import kong.permission.PermissionManager
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PermissionManagerTest {

    @Rule
    @JvmField
    val activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    lateinit var activity: MainActivity
    @Before
    fun setUp() {
        activityRule.launchActivity(Intent())
        activity = activityRule.activity
    }

    @Test
    fun testCreatePermissionManager() {
        val permissionManager = PermissionManager.Builder().build()
        Assert.assertNotNull(permissionManager)
    }

    @Test
    fun testRequestPermissionManager() {
        val permissionManager = PermissionManager.Builder()
                .addPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .whenAllPass { }
                .whenDenied { deniedPermissions, foreverDeniedPermissions -> }
                .build()
        Assert.assertNotNull(permissionManager)
    }
}