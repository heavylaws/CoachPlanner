package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.model.SoccerDrill
import com.example.model.UserRole
import com.example.ui.theme.AttackTeamCyan
import com.example.ui.theme.PitchSurfaceVariant

@Composable
fun CoachingExplanationOverlay(
    drill: SoccerDrill,
    currentPhase: DrillPhase,
    phaseIndex: Int,
    totalPhases: Int,
    currentUserRole: UserRole,
    onOpenVoiceExplanation: () -> Unit,
    onAddQuickNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        color = PitchSurfaceVariant.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Phase Tag + Title + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AttackTeamCyan.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AttackTeamCyan)
                    ) {
                        Text(
                            text = "PHASE ${phaseIndex + 1}/$totalPhases",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AttackTeamCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentPhase.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Voice Explanation Button
                    if (currentUserRole.canCreateDrills) {
                        IconButton(
                            onClick = onOpenVoiceExplanation,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("coach_quick_voice_explanation_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Mic,
                                contentDescription = "Record Voice Explanation",
                                tint = AttackTeamCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Add on-the-fly tactical note
                        IconButton(
                            onClick = onAddQuickNote,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("coach_quick_note_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NoteAdd,
                                contentDescription = "Add Tactical Note",
                                tint = Color(0xFFFFEE58),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Expand / Collapse details
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = "Toggle Cues",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Tactical Directive Subtitle
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currentPhase.instruction,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 16.sp
            )

            // Expanded Coaching Points & Tactical Checklist
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = Color(0x22FFFFFF))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "KEY COACHING POINTS & TRIGGERS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        color = AttackTeamCyan
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    drill.coachingCues.forEachIndexed { idx, cue ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(AttackTeamCyan)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cue,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
