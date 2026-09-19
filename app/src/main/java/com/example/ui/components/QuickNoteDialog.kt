package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.StickyNote2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AttackTeamCyan
import com.example.ui.theme.PitchSurfaceVariant

@Composable
fun QuickNoteDialog(
    initialText: String = "",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var noteText by remember { mutableStateOf(initialText) }

    val presetCues = listOf(
        "Overlap on touch!",
        "Lock passing lane",
        "Body open to pitch",
        "Trigger high press",
        "Drive into half-space",
        "2-touch maximum"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PitchSurfaceVariant,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.StickyNote2,
                    contentDescription = null,
                    tint = Color(0xFFFFEE58),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "On-The-Fly Tactical Note",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Add an instant coaching directive or player cue to the chalkboard:",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("e.g. Invert into midfield to create +1 overload") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_note_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AttackTeamCyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Quick Cues:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AttackTeamCyan
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Presets wrap
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetCues.take(3).forEach { cue ->
                        SuggestionChip(
                            onClick = { noteText = cue },
                            label = { Text(cue, fontSize = 10.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(0x1AFFFFFF),
                                labelColor = Color.White
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (noteText.isNotBlank()) {
                        onConfirm(noteText.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AttackTeamCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_quick_note_btn")
            ) {
                Text("Place On Board", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}
