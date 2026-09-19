package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SoccerDrill
import com.example.model.TeamRole
import com.example.model.UserRole
import com.example.ui.theme.AttackTeamCyan
import com.example.ui.theme.DefenseTeamOrange
import com.example.ui.theme.GoalkeeperGold
import com.example.ui.theme.PitchSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrillDetailsSheet(
    drill: SoccerDrill,
    currentUserRole: UserRole,
    highlightedPlayerNumber: Int?,
    onHighlightPlayer: (Int?) -> Unit,
    onDeleteDrill: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag("drill_details_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = drill.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${drill.category} • ${drill.focusArea} • ${drill.durationMinutes} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description card
            Surface(
                color = PitchSurfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = drill.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Player Position Focus (Player Walkthrough mode)
            Text(
                text = "Player Position Walkthrough (Tap to Highlight Run):",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Highlight your specific jersey number to track your run trajectories and tactical mission.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Distinct players from all phases
            val uniquePlayers = drill.phases.flatMap { it.players }.distinctBy { it.number }.sortedBy { it.number }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All / Reset" chip
                FilterChip(
                    selected = highlightedPlayerNumber == null,
                    onClick = { onHighlightPlayer(null) },
                    label = { Text("All", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )

                uniquePlayers.take(6).forEach { pl ->
                    val isSel = highlightedPlayerNumber == pl.number
                    val pColor = when (pl.role) {
                        TeamRole.ATTACK -> AttackTeamCyan
                        TeamRole.DEFENSE -> DefenseTeamOrange
                        TeamRole.GOALKEEPER -> GoalkeeperGold
                        TeamRole.NEUTRAL -> Color(0xFFB388FF)
                    }

                    FilterChip(
                        selected = isSel,
                        onClick = { onHighlightPlayer(if (isSel) null else pl.number) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(pColor)
                            )
                        },
                        label = {
                            Text(
                                text = "#${pl.number} ${pl.label}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Coaching Cues
            Text(
                text = "Key Coaching Points:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            drill.coachingCues.forEachIndexed { idx, cue ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(AttackTeamCyan.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = "${idx + 1}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AttackTeamCyan
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = cue,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Delete drill action (Head Coach only)
            if (currentUserRole.canDeleteDrills) {
                OutlinedButton(
                    onClick = {
                        onDeleteDrill()
                        onDismiss()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Rounded.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Training Drill from Library")
                }
            }
        }
    }
}
