package com.example.cinestream.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.cinestream.data.update.UpdateChecker
import com.example.cinestream.data.update.UpdateInfo
import com.example.cinestream.ui.theme.CinemaRed

@Composable
fun MandatoryUpdateDialog(
    updateInfo: UpdateInfo,
    onDismissRequest: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val isMandatory = updateInfo.isUpdateRequired || updateInfo.forceUpdate

    // Prevent back button press if update is mandatory
    if (isMandatory) {
        BackHandler(enabled = true) {
            // Do nothing to prevent dismissing mandatory update
        }
    }

    Dialog(
        onDismissRequest = {
            if (!isMandatory) {
                onDismissRequest?.invoke()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !isMandatory,
            dismissOnClickOutside = !isMandatory,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(CinemaRed, CinemaRed.copy(alpha = 0.3f))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .testTag("mandatory_update_dialog"),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Icon
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(CinemaRed.copy(alpha = 0.25f), Color.Transparent)
                                )
                            )
                            .border(2.dp, CinemaRed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = "Update Required",
                            tint = CinemaRed,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Title & Badge
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = CinemaRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NewReleases,
                                    contentDescription = null,
                                    tint = CinemaRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isMandatory) "MANDATORY UPDATE REQUIRED" else "NEW UPDATE AVAILABLE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CinemaRed,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Text(
                            text = "Upgrade to v${updateInfo.latestVersionName}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Description Message
                    Text(
                        text = if (isMandatory) {
                            "A mandatory system update is required to continue using CineStream. Please update to the latest release to access video sources and bug fixes."
                        } else {
                            "A new version of CineStream is available with performance improvements and new features."
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    // Release Notes Card
                    if (updateInfo.releaseNotes.isNotBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .heightIn(max = 120.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = "What's New:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = CinemaRed
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = updateInfo.releaseNotes,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Update Button
                    Button(
                        onClick = {
                            UpdateChecker.openDownloadUrl(context, updateInfo.downloadUrl)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("update_now_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaRed),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Text(
                                text = "UPDATE NOW",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }

                    // Optional dismiss button if NOT mandatory
                    if (!isMandatory) {
                        TextButton(
                            onClick = { onDismissRequest?.invoke() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Remind Me Later", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
