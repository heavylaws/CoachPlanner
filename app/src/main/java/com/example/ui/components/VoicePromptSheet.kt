package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.VoiceNoteManager
import com.example.ui.theme.AttackTeamCyan
import com.example.ui.theme.DefenseTeamOrange
import com.example.ui.theme.PitchSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoicePromptSheet(
    voiceNoteManager: VoiceNoteManager,
    isGenerating: Boolean,
    onGenerateFromPrompt: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val isRecording by voiceNoteManager.isRecording.collectAsState()
    val transcription by voiceNoteManager.transcription.collectAsState()
    val waveformAmplitudes by voiceNoteManager.waveformAmplitudes.collectAsState()
    val speechError by voiceNoteManager.speechError.collectAsState()

    var textPrompt by remember { mutableStateOf("") }

    // When transcription finishes or updates, update text prompt
    LaunchedEffect(transcription) {
        if (transcription.isNotBlank()) {
            textPrompt = transcription
        }
    }

    // Pulsing mic animation
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag("voice_prompt_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        tint = AttackTeamCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Voice Note & Drill Generator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close")
                }
            }

            Text(
                text = "Explain your training drill by voice or prompt. Gemini AI will generate the animated players, ball trajectories, and key coaching points.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 16.dp)
            )

            // Live Waveform Visualizer & Microphone Core
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PitchSurfaceVariant)
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isRecording) {
                        // Live animated soundwave bars
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(40.dp)
                        ) {
                            waveformAmplitudes.forEach { amp ->
                                val barHeight = (amp * 36f).coerceIn(6f, 36f).dp
                                Box(
                                    modifier = Modifier
                                        .width(5.dp)
                                        .height(barHeight)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(AttackTeamCyan, DefenseTeamOrange)
                                            )
                                        )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        FilledTonalButton(
                            onClick = { voiceNoteManager.stopListening() },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFFFF5252),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.Stop, contentDescription = "Stop recording")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Stop Voice Note", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Record Voice Note Button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .scale(pulseScale)
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(AttackTeamCyan, Color(0xFF00B0FF))
                                    )
                                )
                                .clickable {
                                    voiceNoteManager.startListening { result ->
                                        textPrompt = result
                                    }
                                }
                                .testTag("mic_record_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Mic,
                                contentDescription = "Record Voice Note",
                                tint = Color.Black,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to Record Coach Voice Note",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Error notice if speech recognition not supported
            if (speechError != null) {
                Text(
                    text = speechError.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text Prompt input field
            OutlinedTextField(
                value = textPrompt,
                onValueChange = { textPrompt = it },
                label = { Text("Tactical Drill Prompt / Voice Transcript") },
                placeholder = { Text("e.g. 3v2 counter attack with overlapping fullback and near post cross finish") },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tactics_prompt_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AttackTeamCyan
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Preset Suggestions
            Text(
                text = "Coach Quick Tactical Voice Presets:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            voiceNoteManager.coachVoicePresets.take(3).forEach { preset ->
                Surface(
                    onClick = { textPrompt = preset },
                    shape = RoundedCornerShape(12.dp),
                    color = PitchSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayCircleOutline,
                            contentDescription = null,
                            tint = AttackTeamCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = preset,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button
            Button(
                onClick = {
                    if (textPrompt.isNotBlank()) {
                        onGenerateFromPrompt(textPrompt)
                        onDismiss()
                    }
                },
                enabled = textPrompt.isNotBlank() && !isGenerating,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("generate_animation_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AttackTeamCyan,
                    contentColor = Color.Black
                )
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.Black,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Creating Tactical Animation...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Rounded.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Tactical Animation", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
