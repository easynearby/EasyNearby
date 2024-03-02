package com.changeworld.easynearby.exceptions

class PermissionsNotGrantedException(val notGrantedPermissions:List<String>):RuntimeException("permissions ${notGrantedPermissions.joinToString(",")} not granted")