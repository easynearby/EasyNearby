package io.github.easynearby.demo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import io.github.easynearby.core.EasyNearby

abstract class PermissionsActivity : AppCompatActivity() {

    private val TAG = PermissionsActivity::class.java.simpleName

    override fun onStart() {
        super.onStart()
        if (!hasPermissions(*REQUIRED_PERMISSIONS)) {
            requestPermissions(
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_REQUIRED_PERMISSIONS
            )
        } else {
            // To initialize singleton object's init function
            EasyNearby
        }
    }

    /** Called when the user has accepted (or denied) our permission request.  */
    @CallSuper
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_REQUIRED_PERMISSIONS) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    Log.w(TAG, "Failed to request the permission  ${permissions[i]}")
                    Toast.makeText(this, "Missing permissions", Toast.LENGTH_LONG).show()
                    finish()
                    return
                }
            }
            recreate()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    companion object {

        const val REQUEST_CODE_REQUIRED_PERMISSIONS = 1

        /**
         * These permissions are required before connecting to Nearby Connections.
         */
        private var REQUIRED_PERMISSIONS: Array<String> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.NEARBY_WIFI_DEVICES,
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                )
            } else {
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            }

        /**
         * Returns `true` if the app was granted all the permissions. Otherwise, returns `false`.
         */
        fun Context.hasPermissions(vararg permissions: String): Boolean {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
            return true
        }
    }
}