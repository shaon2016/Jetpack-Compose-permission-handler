package com.shaon2016.camerawithpermissionhandling

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.Coil
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.shaon2016.camerawithpermissionhandling.ui.theme.CameraWithPermissionHandlingTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CameraWithPermissionHandlingTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    CameraScreen()
                }
            }
        }
    }
}

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showPermissionDialog by remember {
        mutableStateOf(false)
    }

    val permissions = listOf(Manifest.permission.CAMERA)

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { result ->
            if (result) {
                // Photo capture success
            } else {
                // Photo capture failed or was cancelled by the user
            }
        }

    Column {
        if (capturedImageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(capturedImageUri),
                contentDescription = "Captured Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            )
        }
        Button(onClick = {
            val photoFile = createPhotoFile(context)
            capturedImageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )

            if (checkAllPermissionsGranted(context, permissions)) {
                launcher.launch(capturedImageUri)
            } else
                showPermissionDialog = true

        }) {
            Text("Open Camera")
        }
    }

    if (showPermissionDialog)
        PermissionHandler(permissions) { granted ->
            showPermissionDialog = false
            if (granted)
                launcher.launch(capturedImageUri)
        }
}


private fun createPhotoFile(current: Context): File {
    val file = File.createTempFile("temp", ".jpg", current.externalCacheDir)
    file.createNewFile()
    return file
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CameraWithPermissionHandlingTheme {

    }
}