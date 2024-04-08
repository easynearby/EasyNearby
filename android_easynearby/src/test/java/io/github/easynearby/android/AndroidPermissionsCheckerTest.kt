package io.github.easynearby.android

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class AndroidPermissionsCheckerTest {
    @Test
    fun `test hasAllPermissions When all permissions are granted returns true`() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val androidPermissionsChecker = AndroidPermissionsChecker(context)
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.checkSelfPermission(any(), any())
        } returns PackageManager.PERMISSION_GRANTED

        assertThat(androidPermissionsChecker.hasAllPermissions(), equalTo(true))
        unmockkStatic(ContextCompat::class)
    }

    @Test
    fun `test hasAllPermissions When some permissions are not granted returns false`() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val androidPermissionsChecker = AndroidPermissionsChecker(context)
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.checkSelfPermission(any(), any())
        } returns PackageManager.PERMISSION_DENIED

        assertThat(androidPermissionsChecker.hasAllPermissions(), equalTo(false))
        unmockkStatic(ContextCompat::class)
    }

    @Test
    fun `test getMissingPermissions When all permissions are granted returns empty list`() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val androidPermissionsChecker = AndroidPermissionsChecker(context)
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.checkSelfPermission(any(), any())
        } returns PackageManager.PERMISSION_GRANTED

        assertThat(androidPermissionsChecker.getMissingPermissions(), equalTo(emptyList()))
    }

    @Test
    fun `test getMissingPermissions When some permissions are not granted returns list of missing permissions`() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val androidPermissionsChecker = AndroidPermissionsChecker(context)
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.checkSelfPermission(any(), any())
        } returns PackageManager.PERMISSION_GRANTED

        every {
            ContextCompat.checkSelfPermission(any(), Manifest.permission.BLUETOOTH)
        } returns PackageManager.PERMISSION_DENIED

        assertThat(
            androidPermissionsChecker.getMissingPermissions(),
            equalTo(listOf(Manifest.permission.BLUETOOTH))
        )
        unmockkStatic(ContextCompat::class)
    }
}