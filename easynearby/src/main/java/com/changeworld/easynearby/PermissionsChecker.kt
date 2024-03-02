package com.changeworld.easynearby

/**
 * Handles permissions checking
 */
interface PermissionsChecker {

    /**
     * Checks if all required permissions are granted
     */
    fun hasAllPermissions(): Boolean

    /**
     * Gets missing permissions
     */
    fun getMissingPermissions(): List<String>
}