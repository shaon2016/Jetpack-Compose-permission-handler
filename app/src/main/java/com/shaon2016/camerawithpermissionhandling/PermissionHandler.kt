package com.shaon2016.camerawithpermissionhandling

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionHandler(
    permissions: List<String>,
    onPermissionResult: (Boolean) -> Unit
) {
    val context = LocalContext.current

    val isPermissionAlreadyRequested = remember { mutableStateOf(false) }
    val multiplePermissionsState = rememberMultiplePermissionsState(permissions) {
        isPermissionAlreadyRequested.value = true
    }

    val showSettingsDialog = remember { mutableStateOf(false) }
    val showRationalDialog = remember { mutableStateOf(false) }

    val openAppSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            showSettingsDialog.value = false
            onPermissionResult(multiplePermissionsState.allPermissionsGranted)
        }
    )

    if (multiplePermissionsState.allPermissionsGranted)
        onPermissionResult(true)

    if(!isPermissionAlreadyRequested.value)
        LaunchedEffect(isPermissionAlreadyRequested.value) {
            multiplePermissionsState.launchMultiplePermissionRequest()
        }
    else if (multiplePermissionsState.shouldShowRationale) {
        showRationalDialog.value = true
    } else if (!multiplePermissionsState.allPermissionsGranted && !multiplePermissionsState.shouldShowRationale) {
        showSettingsDialog.value = true
    }

    if (showRationalDialog.value) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Permission rationale") },
            text = {
                Text("permission is required")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRationalDialog.value = false
                        multiplePermissionsState.launchMultiplePermissionRequest()
                    }
                ) {
                    Text("Grant Permissions")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showRationalDialog.value = false
                        onPermissionResult(false)
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    if (showSettingsDialog.value) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Go to Settings") },
            text = {
                Text("permission is required")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSettingsDialog.value = false

                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        openAppSettingsLauncher.launch(intent)
                    }
                ) {
                    Text("Grant Permissions")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showRationalDialog.value = false
                        onPermissionResult(false)
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun checkAllPermissionsGranted(context: Context, permissions: List<String>): Boolean {
    for (permission in permissions) {
        if (ContextCompat.checkSelfPermission(
                context,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
    }
    return true
}