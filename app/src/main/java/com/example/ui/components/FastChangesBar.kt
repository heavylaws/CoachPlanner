package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.*
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
import com.example.ui.theme.DefenseTeamOrange
import com.example.ui.theme.GoalkeeperGold
import com.example.ui.theme.PitchSurfaceVariant

@Composable
fun FastChangesBar(
    canEdit: Boolean,
    onApplyChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var customChangeText by remember { mutableStateOf("") }
    var isExpandingInput by remember { mutableStateOf(false) }

    val quickPills = listOf(
        QuickChangePill("+1 Defender Press", Icons.Rounded.Shield, DefenseTeamOrange),
        QuickChangePill("High Tempo 1-Touch", Icons.Rounded.Speed, AttackTeamCyan),
        QuickChangePill("Fullback Overlap", Icons.AutoMirrored.Rounded.DirectionsRun, GoalkeeperGold),
        QuickChangePill("Add 2 Mini-Goals", Icons.Rounded.SportsSoccer, Color(0xFF69F0AE)),
        QuickChangePill("Add Cones & Grid", Icons.Rounded.ChangeHistory, Color(0xFFFF8A80)),
        QuickChangePill("Flip Pitch View", Icons.Rounded.SwapVert, Color(0xFFB388FF))
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Bolt,
                    contentDescription = null,
                    tint = AttackTeamCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "FAST ADJUSTMENTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    color = AttackTeamCyan
                )
            }
            if (canEdit) {
                TextButton(
                    onClick = { isExpandingInput = !isExpandingInput },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = if (isExpandingInput) "Cancel" else "+ Custom Change",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Custom change text field if expanded
        if (isExpandingInput && canEdit) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customChangeText,
                    onValueChange = { customChangeText = it },
                    placeholder = { Text("e.g. Add recovering CM, faster switch", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("custom_change_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AttackTeamCyan,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (customChangeText.isNotBlank()) {
                            onApplyChange(customChangeText)
                            customChangeText = ""
                            isExpandingInput = false
                        }
                    },
                    enabled = customChangeText.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .testTag("apply_custom_change_button")
                        .height(52.dp)
                ) {
                    Text("Apply", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Horizontal scrolling quick adjustment pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickPills.forEach { pill ->
                SuggestionChip(
                    onClick = {
                        if (canEdit) onApplyChange(pill.label)
                    },
                    enabled = canEdit,
                    label = {
                        Text(
                            text = pill.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = pill.icon,
                            contentDescription = null,
                            tint = pill.color,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = PitchSurfaceVariant
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = canEdit,
                        borderColor = pill.color.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.testTag("quick_pill_${pill.label.replace(" ", "_")}")
                )
            }
        }
    }
}

private data class QuickChangePill(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)
