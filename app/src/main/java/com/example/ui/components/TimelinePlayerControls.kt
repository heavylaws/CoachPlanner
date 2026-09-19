package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DrillPhase
import com.example.ui.theme.AttackTeamCyan
import com.example.ui.theme.PitchSurfaceVariant

@Composable
fun TimelinePlayerControls(
    currentPhase: DrillPhase,
    phaseIndex: Int,
    totalPhases: Int,
    isPlaying: Boolean,
    progressFraction: Float, // 0f..1f within current phase
    playbackSpeed: Float,
    isHalfPitch: Boolean,
    onTogglePlay: () -> Unit,
    onPreviousPhase: () -> Unit,
    onNextPhase: () -> Unit,
    onRestart: () -> Unit,
    onSpeedChanged: (Float) -> Unit,
    onTogglePitchView: () -> Unit,
    onScrubPhase: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Phase title & pitch view toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = AttackTeamCyan.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "PHASE ${phaseIndex + 1}/$totalPhases",
                                color = AttackTeamCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentPhase.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (currentPhase.instruction.isNotBlank()) {
                        Text(
                            text = currentPhase.instruction,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // Pitch view mode button
                IconButton(
                    onClick = onTogglePitchView,
                    modifier = Modifier
                        .testTag("toggle_pitch_view_button")
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isHalfPitch) Icons.Rounded.ZoomOutMap else Icons.Rounded.CropFree,
                        contentDescription = "Toggle pitch zoom view",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Scrubbing step dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until totalPhases) {
                    val isActive = i == phaseIndex
                    val isPast = i < phaseIndex
                    val dotColor = when {
                        isActive -> AttackTeamCyan
                        isPast -> AttackTeamCyan.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(dotColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main playback controls row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Restart button
                IconButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .testTag("restart_drill_button")
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Replay,
                        contentDescription = "Restart animation",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Previous phase
                IconButton(
                    onClick = onPreviousPhase,
                    enabled = phaseIndex > 0,
                    modifier = Modifier
                        .testTag("previous_phase_button")
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous Phase",
                        tint = if (phaseIndex > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                    )
                }

                // Play / Pause Primary FAB
                FilledIconButton(
                    onClick = onTogglePlay,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .testTag("play_pause_button")
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Pause animation" else "Play animation",
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Next phase
                IconButton(
                    onClick = onNextPhase,
                    enabled = phaseIndex < totalPhases - 1,
                    modifier = Modifier
                        .testTag("next_phase_button")
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next Phase",
                        tint = if (phaseIndex < totalPhases - 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                    )
                }

                // Speed selector cycle (0.5x -> 1.0x -> 1.5x -> 2.0x)
                Surface(
                    onClick = {
                        val nextSpeed = when (playbackSpeed) {
                            0.5f -> 1.0f
                            1.0f -> 1.5f
                            1.5f -> 2.0f
                            else -> 0.5f
                        }
                        onSpeedChanged(nextSpeed)
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = PitchSurfaceVariant,
                    modifier = Modifier
                        .testTag("speed_selector_chip")
                        .height(36.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Text(
                            text = "${playbackSpeed}x",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AttackTeamCyan
                        )
                    }
                }
            }
        }
    }
}
