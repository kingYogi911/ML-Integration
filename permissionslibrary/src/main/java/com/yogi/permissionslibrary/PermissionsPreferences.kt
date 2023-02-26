package com.yogi.permissionslibrary

import android.content.Context

class PermissionsPreferences(context: Context) {
    private val sharedPreference = context.getSharedPreferences("permissions_prefences",Context.MODE_PRIVATE)

    fun save(permission:PermissionsHelper){
        with(sharedPreference.edit()){
            putBoolean(permission.manifestPermission,true)
            apply()
        }
    }

    fun check(permission: PermissionsHelper):Boolean{
        return sharedPreference.getBoolean(permission.manifestPermission,false)
    }
}