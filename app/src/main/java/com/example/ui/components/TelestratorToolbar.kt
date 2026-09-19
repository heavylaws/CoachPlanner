package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AnnotationTool
import com.example.ui.theme.AttackTeamCyan
import com.example.ui.theme.PitchDarkBg
import com.example.ui.theme.PitchSurfaceVariant

val AnnotationColors = listOf(
    0xFFFFEE58, // Neon Tactical Yellow
    0xFF00E5FF, // Tactical Cyan
    0xFFFF5252, // Warning Red
    0xFF69F0AE, // Space Lime
    0xFFFFFFFF, // White Chalk
    0xFFFFD600  // Gold Focus
)

@Composable
fun TelestratorToolbar(
    activeTool: AnnotationTool,
    activeColor: Long,
    activeStrokeWidth: Float,
    isDashed: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    isFullscreen: Boolean,
    onToolSelected: (AnnotationTool) -> Unit,
    onColorSelected: (Long) -> Unit,
    onStrokeWidthChanged: (Float) -> Unit,
    onToggleDashed: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClearAll: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onAddQuickNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showColorAndStrokePicker by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = PitchSurfaceVariant.copy(alpha = 0.95f),
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            // Main Tools Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Panel Mode Indicator Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AttackTeamCyan.copy(alpha = 0.15f),
                    modifier = Modifier.height(38.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = null,
                            tint = AttackTeamCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Telestrator",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AttackTeamCyan
                        )
                    }
                }

                VerticalDivider(modifier = Modifier.height(24.dp), color = Color(0x33FFFFFF))

                // Primary Tool buttons
                ToolButton(
                    tool = AnnotationTool.MOVE,
                    icon = Icons.Rounded.PanTool,
                    label = "Move",
                    isSelected = activeTool == AnnotationTool.MOVE,
                    onClick = { onToolSelected(AnnotationTool.MOVE) }
                )

                ToolButton(
                    tool = AnnotationTool.PEN,
                    icon = Icons.Rounded.Brush,
                    label = "Draw",
                    isSelected = activeTool == AnnotationTool.PEN,
                    onClick = {
                        onToolSelected(AnnotationTool.PEN)
                        showColorAndStrokePicker = true
                    }
                )

                ToolButton(
                    tool = AnnotationTool.ARROW,
                    icon = Icons.Rounded.NearMe,
                    label = "Arrow",
                    isSelected = activeTool == AnnotationTool.ARROW,
                    onClick = {
                        onToolSelected(AnnotationTool.ARROW)
                        showColorAndStrokePicker = true
                    }
                )

                ToolButton(
                    tool = AnnotationTool.ZONE,
                    icon = Icons.Rounded.Highlight,
                    label = "Zone",
                    isSelected = activeTool == AnnotationTool.ZONE,
                    onClick = {
                        onToolSelected(AnnotationTool.ZONE)
                        showColorAndStrokePicker = true
                    }
                )

                ToolButton(
                    tool = AnnotationTool.LASER,
                    icon = Icons.Rounded.WbSunny,
                    label = "Laser",
                    isSelected = activeTool == AnnotationTool.LASER,
                    onClick = { onToolSelected(AnnotationTool.LASER) }
                )

                ToolButton(
                    tool = AnnotationTool.NOTE,
                    icon = Icons.Rounded.StickyNote2,
                    label = "Note",
                    isSelected = activeTool == AnnotationTool.NOTE,
                    onClick = { onAddQuickNote() }
                )

                ToolButton(
                    tool = AnnotationTool.ERASER,
                    icon = Icons.Rounded.CleaningServices,
                    label = "Eraser",
                    isSelected = activeTool == AnnotationTool.ERASER,
                    onClick = { onToolSelected(AnnotationTool.ERASER) }
                )

                VerticalDivider(modifier = Modifier.height(24.dp), color = Color(0x33FFFFFF))

                // Undo / Redo
                IconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("undo_annotation_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Undo,
                        contentDescription = "Undo Annotation",
                        tint = if (canUndo) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onRedo,
                    enabled = canRedo,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("redo_annotation_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Redo,
                        contentDescription = "Redo Annotation",
                        tint = if (canRedo) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Clear All Annotations
                IconButton(
                    onClick = onClearAll,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("clear_annotations_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteSweep,
                        contentDescription = "Clear All Annotations",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(20.dp)
                    )
                }

                VerticalDivider(modifier = Modifier.height(24.dp), color = Color(0x33FFFFFF))

                // Interactive Boardroom / Fullscreen Toggle (Crucial for Interactive Smart Panels)
                Surface(
                    onClick = onToggleFullscreen,
                    shape = RoundedCornerShape(10.dp),
                    color = if (isFullscreen) AttackTeamCyan else Color(0x22FFFFFF),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("fullscreen_boardroom_toggle")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                            contentDescription = "Boardroom Fullscreen",
                            tint = if (isFullscreen) Color.Black else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isFullscreen) "Exit Panel" else "Interactive Panel",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFullscreen) Color.Black else Color.White
                        )
                    }
                }
            }

            // Expanded Palette & Stroke Settings
            AnimatedVisibility(
                visible = activeTool in listOf(AnnotationTool.PEN, AnnotationTool.ARROW, AnnotationTool.ZONE) || showColorAndStrokePicker
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Colors
                        Text("Color:", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                        AnnotationColors.forEach { colLong ->
                            val isChosen = activeColor == colLong
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(colLong))
                                    .clickable { onColorSelected(colLong) }
                                    .then(
                                        if (isChosen) Modifier.border(2.5.dp, Color.White, CircleShape)
                                        else Modifier
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))
                        VerticalDivider(modifier = Modifier.height(18.dp), color = Color(0x33FFFFFF))

                        // Stroke Widths
                        Text("Thickness:", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                        listOf(3f to "Fine", 6f to "Medium", 10f to "Bold").forEach { (w, label) ->
                            val isSel = activeStrokeWidth == w
                            Surface(
                                onClick = { onStrokeWidthChanged(w) },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) AttackTeamCyan.copy(alpha = 0.3f) else Color(0x1AFFFFFF),
                                border = if (isSel) androidx.compose.foundation.BorderStroke(1.dp, AttackTeamCyan) else null,
                                modifier = Modifier.height(26.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) AttackTeamCyan else Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))
                        VerticalDivider(modifier = Modifier.height(18.dp), color = Color(0x33FFFFFF))

                        // Dashed toggle (great for tactical run lines vs pass lines)
                        Surface(
                            onClick = onToggleDashed,
                            shape = RoundedCornerShape(6.dp),
                            color = if (isDashed) AttackTeamCyan.copy(alpha = 0.3f) else Color(0x1AFFFFFF),
                            border = if (isDashed) androidx.compose.foundation.BorderStroke(1.dp, AttackTeamCyan) else null,
                            modifier = Modifier.height(26.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = if (isDashed) "Dashed Line" else "Solid Line",
                                    fontSize = 10.sp,
                                    fontWeight = if (isDashed) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isDashed) AttackTeamCyan else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolButton(
    tool: AnnotationTool,
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) AttackTeamCyan else Color(0x1AFFFFFF),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, AttackTeamCyan) else null,
        modifier = Modifier
            .height(38.dp)
            .testTag("tool_button_${tool.name.lowercase()}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.Black else Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.Black else Color.White
            )
        }
    }
}
