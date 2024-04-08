package io.github.easynearby.core

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