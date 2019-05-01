package com.kong.sample

import android.Manifest
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kong.permission.PermissionManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var permissionManager: PermissionManager

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionManager = PermissionManager.Builder()
                .addPermission(Manifest.permission.CAMERA)
                .whenAllPass {
                    Toast.makeText(this, "All pass!!", Toast.LENGTH_SHORT).show()
                    result_tv.text = "All pass!!"
                }
                .whenDenied { deniedPermissions, foreverDeniedPermissions ->
                    Toast.makeText(this, "Denied something.", Toast.LENGTH_SHORT).show()
                    result_tv.text = "Denied something."

                    if (foreverDeniedPermissions.isNotEmpty()) {
                        PermissionManager.openApplicationSettings(this, application.packageName)
                        PermissionManager.isBelowMarshmallow()
                    }
                }
                .build()
    }

    override fun onResume() {
        super.onResume()
        permissionManager.request(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.handleResult(this, requestCode, permissions, grantResults)
    }
}
