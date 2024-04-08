package io.github.easynearby.core.exceptions

class PermissionsNotGrantedException(val notGrantedPermissions:List<String>):RuntimeException("permissions ${notGrantedPermissions.joinToString(",")} not granted")