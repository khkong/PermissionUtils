package kong.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.lang.NullPointerException
import java.lang.RuntimeException
import java.util.*

typealias PermissionName = String

/**
 *
 */
class PermissionManager private constructor(private val classMember: PermissionManagerMembers) {

    private class PermissionManagerMembers {
        var permissions: ArrayList<PermissionName> = arrayListOf()
        var callbackForAllPass: () -> (Unit)? = {}
        var callbackForDenied: (ArrayList<PermissionName>, ArrayList<PermissionName>) -> (Unit)? = { _, _ -> }
        var retryTimes = 0
        var requestCode = DEFAULT_REQUEST_CODE
    }

    private val permissionsDenied: ArrayList<PermissionName> = arrayListOf()
    private val permissionsDeniedForever: ArrayList<PermissionName> = arrayListOf()

    /**
     *
     */
    fun request(activity: Activity) {
        clearClassifiedPermissions()
        if (isBelowMarshmallow()) {
            return callback()
        }

        requestPermissions(activity)
    }

    private fun clearClassifiedPermissions() {
        permissionsDenied.clear()
        permissionsDeniedForever.clear()
    }

    private fun requestPermissions(activity: Activity) {
        val permissionsToRequest = getPermissionsToRequest(activity)
        if (permissionsToRequest.isEmpty())
            return callback()

        ActivityCompat.requestPermissions(activity, permissionsToRequest.toTypedArray(), classMember.requestCode)
    }

    private fun getPermissionsToRequest(activity: Activity): ArrayList<String> {
        val permissionsToRequest = ArrayList<String>()
        for (permission in classMember.permissions) {
            if (!isGranted(activity, permission)) {
                permissionsToRequest.add(permission)
            }
        }
        return permissionsToRequest
    }

    /**
     *
     */
    fun handleResult(activity: Activity, requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != classMember.requestCode)
            return

        try {
            classifyPermissionResults(activity, permissions, grantResults)
            if (classMember.retryTimes == 0) {
                return callback()
            }

            handleAsk(activity)
        } catch (e: NullPointerException) { // The app is terminated or the activity is terminated.
            e.printStackTrace()
        } catch (e: RuntimeException) { // If the activity is finished by the lifecycle.
            e.printStackTrace()
        }
    }

    @Throws(NullPointerException::class, RuntimeException::class)
    private fun classifyPermissionResults(activity: Activity, permissions: Array<out String>, grantResults: IntArray) {
        for (i in permissions.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                val isDeniedForever = ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i])
                if (!isDeniedForever) {
                    permissionsDeniedForever.add(permissions[i])
                }
                permissionsDenied.add(permissions[i])
            }
        }
    }

    @Throws(NullPointerException::class, RuntimeException::class)
    private fun handleAsk(activity: Activity) {
        if (permissionsDenied.isEmpty())
            return callback()

        classMember.retryTimes--
        if (existTemporarilyDeniedPermissions()) {
            request(activity)
        }
    }

    private fun existTemporarilyDeniedPermissions(): Boolean {
        return permissionsDeniedForever.size != permissionsDenied.size
    }

    private fun callback() {
        if (isHaveDeniedPermissions()) {
            classMember.callbackForDenied(permissionsDenied, permissionsDeniedForever)
        } else {
            classMember.callbackForAllPass()
        }
    }

    private fun isHaveDeniedPermissions(): Boolean = permissionsDenied.isNotEmpty()

    /**
     *
     */
    class Builder {
        private var permissionManagerMembers = PermissionManagerMembers()

        fun addPermissions(permissions: ArrayList<PermissionName>): Builder {
            permissionManagerMembers.permissions.addAll(permissions)
            return this
        }

        fun addPermission(permission: PermissionName): Builder {
            permissionManagerMembers.permissions.add(permission)
            return this
        }

        fun addPermission(vararg permissions: PermissionName): Builder {
            permissionManagerMembers.permissions.addAll(permissions)
            return this
        }

        fun setRetry(retryTimes: Int): Builder {
            permissionManagerMembers.retryTimes = retryTimes
            return this
        }

        fun whenAllPass(function: () -> (Unit)): Builder {
            permissionManagerMembers.callbackForAllPass = function
            return this
        }

        fun whenDenied(function: (ArrayList<PermissionName>, ArrayList<PermissionName>) -> (Unit)): Builder {
            permissionManagerMembers.callbackForDenied = function
            return this
        }

        fun setRequestCode(requestCode: Int): Builder {
            permissionManagerMembers.requestCode = requestCode
            return this
        }

        fun build(): PermissionManager {
            return PermissionManager(permissionManagerMembers)
        }
    }

    companion object {
        private const val DEFAULT_REQUEST_CODE = 100

        fun isBelowMarshmallow() = (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)

        fun getIntentForOpenApplicationSettings(packageName: String): Intent {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            return intent
        }

        fun openApplicationSettings(context: Context, packageName: String) {
            context.startActivity(getIntentForOpenApplicationSettings(packageName))
        }

        fun isGranted(context: Context, permission: PermissionName): Boolean {
            return isBelowMarshmallow() || isGrantedPermission(context, permission)
        }

        private fun isGrantedPermission(context: Context, permission: PermissionName) =
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

        fun isGranted(context: Context, vararg permissions: PermissionName): Boolean {
            for (permission in permissions) {
                if (!isGranted(context, permission)) {
                    return false
                }
            }
            return true
        }
    }
}
