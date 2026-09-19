package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun TacticalPitchView(
    drill: SoccerDrill,
    currentPhase: DrillPhase,
    nextPhase: DrillPhase,
    animationFraction: Float, // 0f..1f between currentPhase and nextPhase
    highlightedPlayerNumber: Int? = null,
    isEditable: Boolean = true,
    annotations: List<TacticalAnnotation> = emptyList(),
    laserPoints: List<LaserPoint> = emptyList(),
    quickNotes: List<QuickTacticalNote> = emptyList(),
    activeTool: AnnotationTool = AnnotationTool.MOVE,
    activeColor: Long = 0xFFFFEE58,
    activeStrokeWidth: Float = 5f,
    isDashed: Boolean = false,
    isFullscreen: Boolean = false,
    onPlayerMoved: (playerId: String, newX: Float, newY: Float) -> Unit = { _, _, _ -> },
    onPlayerSelected: (TacticalPlayer) -> Unit = {},
    onAddAnnotation: (TacticalAnnotation) -> Unit = {},
    onEraseNear: (Float, Float) -> Unit = { _, _ -> },
    onLaserMoved: (Float, Float) -> Unit = { _, _ -> },
    onPitchTapForNote: (Float, Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val isHalfPitch = drill.pitchView == "HALF"

    // Drag tracking for manual coach repositioning
    var draggingPlayerId by remember { mutableStateOf<String?>(null) }

    // In-progress telestrator draft state
    var draftPoints by remember { mutableStateOf<List<AnnotationPoint>>(emptyList()) }
    var draftStartPoint by remember { mutableStateOf<AnnotationPoint?>(null) }
    var draftEndPoint by remember { mutableStateOf<AnnotationPoint?>(null) }

    Surface(
        modifier = if (isFullscreen) {
            modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(0.dp))
        } else {
            modifier
                .fillMaxWidth()
                .aspectRatio(if (isHalfPitch) 1.25f else 0.72f)
                .clip(RoundedCornerShape(16.dp))
        },
        color = PitchDarkBg,
        shadowElevation = 8.dp
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(activeTool, isEditable, currentPhase, activeColor, activeStrokeWidth, isDashed) {
                    when (activeTool) {
                        AnnotationTool.MOVE -> {
                            if (!isEditable) return@pointerInput
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val normX = offset.x / size.width
                                    val normY = offset.y / size.height
                                    val player = currentPhase.players.minByOrNull { pl ->
                                        val dx = pl.x - normX
                                        val dy = pl.y - normY
                                        dx * dx + dy * dy
                                    }
                                    if (player != null) {
                                        val dist = hypot(player.x - normX, player.y - normY)
                                        if (dist < 0.12f) {
                                            draggingPlayerId = player.id
                                            onPlayerSelected(player)
                                        }
                                    }
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    draggingPlayerId?.let { id ->
                                        val newX = (change.position.x / size.width).coerceIn(0.05f, 0.95f)
                                        val newY = (change.position.y / size.height).coerceIn(0.05f, 0.95f)
                                        onPlayerMoved(id, newX, newY)
                                    }
                                },
                                onDragEnd = { draggingPlayerId = null },
                                onDragCancel = { draggingPlayerId = null }
                            )
                        }
                        AnnotationTool.PEN -> {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val normX = (offset.x / size.width).coerceIn(0f, 1f)
                                    val normY = (offset.y / size.height).coerceIn(0f, 1f)
                                    draftPoints = listOf(AnnotationPoint(normX, normY))
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val normX = (change.position.x / size.width).coerceIn(0f, 1f)
                                    val normY = (change.position.y / size.height).coerceIn(0f, 1f)
                                    draftPoints = draftPoints + AnnotationPoint(normX, normY)
                                },
                                onDragEnd = {
                                    if (draftPoints.size >= 2) {
                                        onAddAnnotation(
                                            TacticalAnnotation(
                                                tool = AnnotationTool.PEN,
                                                points = draftPoints,
                                                color = activeColor,
                                                strokeWidth = activeStrokeWidth,
                                                isDashed = isDashed
                                            )
                                        )
                                    }
                                    draftPoints = emptyList()
                                },
                                onDragCancel = { draftPoints = emptyList() }
                            )
                        }
                        AnnotationTool.ARROW -> {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val normX = (offset.x / size.width).coerceIn(0f, 1f)
                                    val normY = (offset.y / size.height).coerceIn(0f, 1f)
                                    draftStartPoint = AnnotationPoint(normX, normY)
                                    draftEndPoint = AnnotationPoint(normX, normY)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val normX = (change.position.x / size.width).coerceIn(0f, 1f)
                                    val normY = (change.position.y / size.height).coerceIn(0f, 1f)
                                    draftEndPoint = AnnotationPoint(normX, normY)
                                },
                                onDragEnd = {
                                    val s = draftStartPoint
                                    val e = draftEndPoint
                                    if (s != null && e != null && hypot(e.x - s.x, e.y - s.y) > 0.02f) {
                                        onAddAnnotation(
                                            TacticalAnnotation(
                                                tool = AnnotationTool.ARROW,
                                                points = listOf(s, e),
                                                color = activeColor,
                                                strokeWidth = activeStrokeWidth,
                                                isDashed = isDashed
                                            )
                                        )
                                    }
                                    draftStartPoint = null
                                    draftEndPoint = null
                                },
                                onDragCancel = {
                                    draftStartPoint = null
                                    draftEndPoint = null
                                }
                            )
                        }
                        AnnotationTool.ZONE -> {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val normX = (offset.x / size.width).coerceIn(0f, 1f)
                                    val normY = (offset.y / size.height).coerceIn(0f, 1f)
                                    draftStartPoint = AnnotationPoint(normX, normY)
                                    draftEndPoint = AnnotationPoint(normX, normY)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val normX = (change.position.x / size.width).coerceIn(0f, 1f)
                                    val normY = (change.position.y / size.height).coerceIn(0f, 1f)
                                    draftEndPoint = AnnotationPoint(normX, normY)
                                },
                                onDragEnd = {
                                    val s = draftStartPoint
                                    val e = draftEndPoint
                                    if (s != null && e != null && hypot(e.x - s.x, e.y - s.y) > 0.02f) {
                                        onAddAnnotation(
                                            TacticalAnnotation(
                                                tool = AnnotationTool.ZONE,
                                                points = listOf(s, e),
                                                color = activeColor,
                                                strokeWidth = activeStrokeWidth,
                                                isDashed = isDashed
                                            )
                                        )
                                    }
                                    draftStartPoint = null
                                    draftEndPoint = null
                                },
                                onDragCancel = {
                                    draftStartPoint = null
                                    draftEndPoint = null
                                }
                            )
                        }
                        AnnotationTool.LASER -> {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    onLaserMoved(offset.x / size.width, offset.y / size.height)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    onLaserMoved(change.position.x / size.width, change.position.y / size.height)
                                },
                                onDragEnd = {},
                                onDragCancel = {}
                            )
                        }
                        AnnotationTool.NOTE -> {
                            detectTapGestures { offset ->
                                onPitchTapForNote(offset.x / size.width, offset.y / size.height)
                            }
                        }
                        AnnotationTool.ERASER -> {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    onEraseNear(offset.x / size.width, offset.y / size.height)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    onEraseNear(change.position.x / size.width, change.position.y / size.height)
                                },
                                onDragEnd = {},
                                onDragCancel = {}
                            )
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Draw pitch grass stripes
            drawPitchGrass(width, height, isHalfPitch)

            // 2. Draw tactical pitch lines
            drawPitchMarkings(width, height, isHalfPitch)

            // 3. Draw equipment (cones, mini goals, mannequins)
            currentPhase.equipment.forEach { eq ->
                drawEquipment(eq, width, height)
            }

            // 4. Draw trajectory run lines and passing paths
            drawMovementTrajectories(currentPhase, nextPhase, width, height, highlightedPlayerNumber)

            // 5. Draw animated players
            currentPhase.players.forEach { currPlayer ->
                val nextPlayer = nextPhase.players.find { it.id == currPlayer.id } ?: currPlayer
                val interpX = lerp(currPlayer.x, nextPlayer.x, animationFraction) * width
                val interpY = lerp(currPlayer.y, nextPlayer.y, animationFraction) * height

                // Facing angle towards target run
                val targetScreenX = (currPlayer.targetX) * width
                val targetScreenY = (currPlayer.targetY) * height
                val angle = atan2(targetScreenY - interpY, targetScreenX - interpX)

                val isHighlighted = highlightedPlayerNumber != null && currPlayer.number == highlightedPlayerNumber
                val hasBall = (currPlayer.hasBall && animationFraction < 0.5f) || (nextPlayer.hasBall && animationFraction >= 0.5f)

                drawPlayer(
                    player = currPlayer,
                    x = interpX,
                    y = interpY,
                    angle = angle,
                    isHighlighted = isHighlighted,
                    hasBall = hasBall,
                    textMeasurer = textMeasurer
                )
            }

            // 6. Draw animated ball
            val ballStart = currentPhase.ball
            val ballTarget = nextPhase.ball
            val ballX = lerp(ballStart.x, ballTarget.x, animationFraction) * width
            val ballBaseY = lerp(ballStart.y, ballTarget.y, animationFraction) * height

            // Aerial arc bounce
            val arcOffset = if (ballStart.trajectory == BallTrajectory.AERIAL_PASS) {
                -sin(animationFraction * Math.PI.toFloat()) * (height * 0.08f)
            } else 0f

            drawSoccerBall(
                x = ballX,
                y = ballBaseY + arcOffset,
                fraction = animationFraction,
                trajectory = ballStart.trajectory
            )

            // 7. Draw completed annotations (Telestrator overlay)
            annotations.forEach { ann ->
                drawTacticalAnnotation(ann, width, height, textMeasurer)
            }

            // 8. Draw live draft annotation (as coach is currently sketching)
            if (activeTool == AnnotationTool.PEN && draftPoints.size >= 2) {
                drawTacticalAnnotation(
                    TacticalAnnotation(
                        tool = AnnotationTool.PEN,
                        points = draftPoints,
                        color = activeColor,
                        strokeWidth = activeStrokeWidth,
                        isDashed = isDashed
                    ),
                    width, height, textMeasurer
                )
            } else if ((activeTool == AnnotationTool.ARROW || activeTool == AnnotationTool.ZONE) && draftStartPoint != null && draftEndPoint != null) {
                drawTacticalAnnotation(
                    TacticalAnnotation(
                        tool = activeTool,
                        points = listOf(draftStartPoint!!, draftEndPoint!!),
                        color = activeColor,
                        strokeWidth = activeStrokeWidth,
                        isDashed = isDashed
                    ),
                    width, height, textMeasurer
                )
            }

            // 9. Draw on-the-fly tactical notes pins
            quickNotes.forEach { note ->
                drawQuickTacticalNotePin(note, width, height, textMeasurer)
            }

            // 10. Draw interactive presentation Laser pointer trail
            if (laserPoints.isNotEmpty()) {
                drawLaserPointerBeam(laserPoints, width, height)
            }
        }
    }
}

private fun DrawScope.drawPitchGrass(width: Float, height: Float, isHalf: Boolean) {
    val stripes = if (isHalf) 7 else 12
    val stripeHeight = height / stripes
    for (i in 0 until stripes) {
        val color = if (i % 2 == 0) GrassStripeA else GrassStripeB
        drawRect(
            color = color,
            topLeft = Offset(0f, i * stripeHeight),
            size = Size(width, stripeHeight)
        )
    }

    // Outer subtle pitch border
    drawRect(
        color = Color(0x33000000),
        topLeft = Offset.Zero,
        size = Size(width, height),
        style = Stroke(width = 4.dp.toPx())
    )
}

private fun DrawScope.drawPitchMarkings(width: Float, height: Float, isHalf: Boolean) {
    val pad = width * 0.05f
    val pitchW = width - (pad * 2)
    val pitchH = height - (pad * 2)
    val stroke = Stroke(width = 2.dp.toPx())
    val lineColor = PitchLineColor

    // Main touchline boundary
    drawRoundRect(
        color = lineColor,
        topLeft = Offset(pad, pad),
        size = Size(pitchW, pitchH),
        cornerRadius = CornerRadius(2.dp.toPx()),
        style = stroke
    )

    if (isHalf) {
        // Half Pitch (e.g. defending goal at top, attacking towards top goal)
        // Goal line at pad (top)
        val goalW = pitchW * 0.28f
        val goalLeft = pad + (pitchW - goalW) / 2
        // Goal net (extending outside pitch top)
        drawRect(
            color = Color(0x90FFFFFF),
            topLeft = Offset(goalLeft, pad - 12.dp.toPx()),
            size = Size(goalW, 12.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )

        // 6-yard box
        val sixW = pitchW * 0.38f
        val sixH = pitchH * 0.14f
        drawRect(
            color = lineColor,
            topLeft = Offset(pad + (pitchW - sixW) / 2, pad),
            size = Size(sixW, sixH),
            style = stroke
        )

        // 18-yard penalty box
        val penW = pitchW * 0.68f
        val penH = pitchH * 0.35f
        drawRect(
            color = lineColor,
            topLeft = Offset(pad + (pitchW - penW) / 2, pad),
            size = Size(penW, penH),
            style = stroke
        )

        // Penalty spot
        val penSpotY = pad + penH * 0.65f
        drawCircle(
            color = lineColor,
            radius = 3.dp.toPx(),
            center = Offset(width / 2, penSpotY)
        )

        // Penalty Arc (D outside the box)
        drawArc(
            color = lineColor,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(width / 2 - 32.dp.toPx(), pad + penH - 24.dp.toPx()),
            size = Size(64.dp.toPx(), 48.dp.toPx()),
            style = stroke
        )

        // Halfway line at bottom
        drawLine(
            color = lineColor,
            start = Offset(pad, height - pad),
            end = Offset(width - pad, height - pad),
            strokeWidth = 2.dp.toPx()
        )
        // Center circle arc at bottom
        drawArc(
            color = lineColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(width / 2 - 40.dp.toPx(), height - pad - 40.dp.toPx()),
            size = Size(80.dp.toPx(), 80.dp.toPx()),
            style = stroke
        )
    } else {
        // Full Pitch
        // Halfway line
        val midY = height / 2
        drawLine(
            color = lineColor,
            start = Offset(pad, midY),
            end = Offset(width - pad, midY),
            strokeWidth = 2.dp.toPx()
        )
        // Center circle
        val centerR = pitchW * 0.15f
        drawCircle(
            color = lineColor,
            radius = centerR,
            center = Offset(width / 2, midY),
            style = stroke
        )
        // Center spot
        drawCircle(
            color = lineColor,
            radius = 3.dp.toPx(),
            center = Offset(width / 2, midY)
        )

        // Top Goal & Penalty Box
        val penW = pitchW * 0.60f
        val penH = pitchH * 0.20f
        drawRect(
            color = lineColor,
            topLeft = Offset(pad + (pitchW - penW) / 2, pad),
            size = Size(penW, penH),
            style = stroke
        )
        // Top 6-yard box
        val sixW = pitchW * 0.32f
        val sixH = pitchH * 0.08f
        drawRect(
            color = lineColor,
            topLeft = Offset(pad + (pitchW - sixW) / 2, pad),
            size = Size(sixW, sixH),
            style = stroke
        )
        // Top Goal net
        val goalW = pitchW * 0.24f
        drawRect(
            color = Color(0x90FFFFFF),
            topLeft = Offset(pad + (pitchW - goalW) / 2, pad - 10.dp.toPx()),
            size = Size(goalW, 10.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )

        // Bottom Goal & Penalty Box
        drawRect(
            color = lineColor,
            topLeft = Offset(pad + (pitchW - penW) / 2, height - pad - penH),
            size = Size(penW, penH),
            style = stroke
        )
        // Bottom 6-yard box
        drawRect(
            color = lineColor,
            topLeft = Offset(pad + (pitchW - sixW) / 2, height - pad - sixH),
            size = Size(sixW, sixH),
            style = stroke
        )
        // Bottom Goal net
        drawRect(
            color = Color(0x90FFFFFF),
            topLeft = Offset(pad + (pitchW - goalW) / 2, height - pad),
            size = Size(goalW, 10.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

private fun DrawScope.drawMovementTrajectories(
    currentPhase: DrillPhase,
    nextPhase: DrillPhase,
    width: Float,
    height: Float,
    highlightedPlayerNumber: Int?
) {
    // 1. Draw player running paths (dashed lines)
    currentPhase.players.forEach { pl ->
        val nextPl = nextPhase.players.find { it.id == pl.id } ?: pl
        val isHighlighted = highlightedPlayerNumber != null && pl.number == highlightedPlayerNumber

        val start = Offset(pl.x * width, pl.y * height)
        val end = Offset(nextPl.x * width, nextPl.y * height)

        val dist = hypot(end.x - start.x, end.y - start.y)
        if (dist > 15f) {
            val color = if (isHighlighted) TrajectoryLineColor else when (pl.role) {
                TeamRole.ATTACK -> Color(0x9900E5FF)
                TeamRole.DEFENSE -> Color(0x99FF6D00)
                TeamRole.GOALKEEPER -> Color(0x99FFD600)
                TeamRole.NEUTRAL -> Color(0x99B388FF)
            }
            val strokeWidth = if (isHighlighted) 3.5.dp.toPx() else 2.dp.toPx()

            // Draw dashed path
            drawLine(
                color = color,
                start = start,
                end = end,
                strokeWidth = strokeWidth,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            )

            // Draw directional arrowhead at end
            val angle = atan2(end.y - start.y, end.x - start.x)
            val arrowHeadSize = 10.dp.toPx()
            val arrowP1 = Offset(
                end.x - arrowHeadSize * cos(angle - 0.4f),
                end.y - arrowHeadSize * sin(angle - 0.4f)
            )
            val arrowP2 = Offset(
                end.x - arrowHeadSize * cos(angle + 0.4f),
                end.y - arrowHeadSize * sin(angle + 0.4f)
            )
            val path = Path().apply {
                moveTo(end.x, end.y)
                lineTo(arrowP1.x, arrowP1.y)
                lineTo(arrowP2.x, arrowP2.y)
                close()
            }
            drawPath(path, color)
        }
    }

    // 2. Draw ball passing trajectory vector
    val ballStart = Offset(currentPhase.ball.x * width, currentPhase.ball.y * height)
    val ballEnd = Offset(nextPhase.ball.x * width, nextPhase.ball.y * height)
    val ballDist = hypot(ballEnd.x - ballStart.x, ballEnd.y - ballStart.y)

    if (ballDist > 20f) {
        val passColor = Color(0xFFFFF59D)
        when (currentPhase.ball.trajectory) {
            BallTrajectory.GROUND_PASS, BallTrajectory.SHOT -> {
                drawLine(
                    color = passColor,
                    start = ballStart,
                    end = ballEnd,
                    strokeWidth = 2.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
                )
            }
            BallTrajectory.AERIAL_PASS -> {
                // Curved lofted path
                val midX = (ballStart.x + ballEnd.x) / 2
                val midY = (ballStart.y + ballEnd.y) / 2 - (height * 0.08f)
                val path = Path().apply {
                    moveTo(ballStart.x, ballStart.y)
                    quadraticTo(midX, midY, ballEnd.x, ballEnd.y)
                }
                drawPath(
                    path = path,
                    color = passColor,
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )
                )
            }
            BallTrajectory.DRIBBLE -> {
                // Wavy/solid dribble line
                drawLine(
                    color = Color(0xFFFFCC80),
                    start = ballStart,
                    end = ballEnd,
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawPlayer(
    player: TacticalPlayer,
    x: Float,
    y: Float,
    angle: Float,
    isHighlighted: Boolean,
    hasBall: Boolean,
    textMeasurer: TextMeasurer
) {
    val radius = 15.dp.toPx()

    val baseColor = when (player.role) {
        TeamRole.ATTACK -> AttackTeamCyan
        TeamRole.DEFENSE -> DefenseTeamOrange
        TeamRole.GOALKEEPER -> GoalkeeperGold
        TeamRole.NEUTRAL -> NeutralPurple
    }

    // Glow aura for highlighted player or ball carrier
    if (isHighlighted) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x99FFFF00), Color(0x00FFFF00)),
                center = Offset(x, y),
                radius = radius * 2.2f
            ),
            radius = radius * 2.2f,
            center = Offset(x, y)
        )
    } else if (hasBall) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x66FFFFFF), Color(0x00FFFFFF)),
                center = Offset(x, y),
                radius = radius * 1.8f
            ),
            radius = radius * 1.8f,
            center = Offset(x, y)
        )
    }

    // Direction cone wedge
    val coneDist = radius + 6.dp.toPx()
    val coneW = 0.35f
    val tipX = x + coneDist * cos(angle)
    val tipY = y + coneDist * sin(angle)
    val leftX = x + (radius * 0.9f) * cos(angle - coneW)
    val leftY = y + (radius * 0.9f) * sin(angle - coneW)
    val rightX = x + (radius * 0.9f) * cos(angle + coneW)
    val rightY = y + (radius * 0.9f) * sin(angle + coneW)

    val conePath = Path().apply {
        moveTo(tipX, tipY)
        lineTo(leftX, leftY)
        lineTo(rightX, rightY)
        close()
    }
    drawPath(conePath, baseColor)

    // Player body circle
    drawCircle(
        color = baseColor,
        radius = radius,
        center = Offset(x, y)
    )

    // Inner shadow ring
    drawCircle(
        color = Color(0x33000000),
        radius = radius,
        center = Offset(x, y),
        style = Stroke(width = 2.dp.toPx())
    )

    // High-contrast white jersey number
    val numStr = player.number.toString()
    val textLayoutResult = textMeasurer.measure(
        text = AnnotatedString(numStr),
        style = TextStyle(
            fontSize = 12.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = Color.Black
        )
    )
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(
            x - textLayoutResult.size.width / 2f,
            y - textLayoutResult.size.height / 2f
        )
    )

    // Player label below
    if (player.label.isNotBlank()) {
        val labelLayout = textMeasurer.measure(
            text = AnnotatedString(player.label),
            style = TextStyle(
                fontSize = 9.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                color = Color.White
            )
        )
        // Background badge
        val bgPad = 3.dp.toPx()
        val bgTop = y + radius + 3.dp.toPx()
        drawRoundRect(
            color = Color(0xCC000000),
            topLeft = Offset(x - labelLayout.size.width / 2f - bgPad, bgTop - 1f),
            size = Size(labelLayout.size.width + bgPad * 2f, labelLayout.size.height.toFloat() + 2f),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
        drawText(
            textLayoutResult = labelLayout,
            topLeft = Offset(x - labelLayout.size.width / 2f, bgTop)
        )
    }
}

private fun DrawScope.drawSoccerBall(
    x: Float,
    y: Float,
    fraction: Float,
    trajectory: BallTrajectory
) {
    val r = 7.5.dp.toPx()

    // Halo glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x80FFF59D), Color(0x00FFF59D)),
            center = Offset(x, y),
            radius = r * 1.8f
        ),
        radius = r * 1.8f,
        center = Offset(x, y)
    )

    // White base
    drawCircle(
        color = BallColor,
        radius = r,
        center = Offset(x, y)
    )
    // Ball outer ring
    drawCircle(
        color = Color(0xFF333333),
        radius = r,
        center = Offset(x, y),
        style = Stroke(width = 1.dp.toPx())
    )

    // Rolling rotation angle
    val rot = fraction * 360f * 2
    val pCenter = Offset(
        x + (r * 0.25f) * cos(Math.toRadians(rot.toDouble()).toFloat()),
        y + (r * 0.25f) * sin(Math.toRadians(rot.toDouble()).toFloat())
    )
    drawCircle(
        color = Color(0xFF1E1E1E),
        radius = r * 0.35f,
        center = pCenter
    )
}

private fun DrawScope.drawEquipment(equipment: TacticalEquipment, width: Float, height: Float) {
    val x = equipment.x * width
    val y = equipment.y * height

    when (equipment.type) {
        EquipmentType.CONE -> {
            // Fluorescent orange cone
            val coneW = 10.dp.toPx()
            val coneH = 12.dp.toPx()
            val path = Path().apply {
                moveTo(x, y - coneH / 2)
                lineTo(x - coneW / 2, y + coneH / 2)
                lineTo(x + coneW / 2, y + coneH / 2)
                close()
            }
            drawPath(path, Color(0xFFFF5722))
            drawCircle(color = Color(0xFFFFEB3B), radius = 2.dp.toPx(), center = Offset(x, y - coneH / 3))
        }
        EquipmentType.MINI_GOAL -> {
            val gW = 24.dp.toPx()
            val gH = 10.dp.toPx()
            drawRoundRect(
                color = Color(0xFFFFD600),
                topLeft = Offset(x - gW / 2, y - gH / 2),
                size = Size(gW, gH),
                cornerRadius = CornerRadius(2.dp.toPx()),
                style = Stroke(width = 2.5.dp.toPx())
            )
            drawLine(
                color = Color(0x80FFFFFF),
                start = Offset(x - gW / 4, y - gH / 2),
                end = Offset(x - gW / 4, y + gH / 2),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color(0x80FFFFFF),
                start = Offset(x + gW / 4, y - gH / 2),
                end = Offset(x + gW / 4, y + gH / 2),
                strokeWidth = 1.dp.toPx()
            )
        }
        EquipmentType.MANNEQUIN -> {
            val mRadius = 6.dp.toPx()
            drawCircle(color = Color(0xFF78909C), radius = mRadius, center = Offset(x, y))
            drawLine(
                color = Color(0xFF78909C),
                start = Offset(x - 8.dp.toPx(), y + 4.dp.toPx()),
                end = Offset(x + 8.dp.toPx(), y + 4.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
        }
        EquipmentType.AGILITY_LADDER -> {
            val ladW = 12.dp.toPx()
            val ladH = 32.dp.toPx()
            drawRect(
                color = Color(0xFFFFEB3B),
                topLeft = Offset(x - ladW / 2, y - ladH / 2),
                size = Size(ladW, ladH),
                style = Stroke(width = 1.5.dp.toPx())
            )
            for (i in 1..3) {
                val stepY = y - ladH / 2 + (ladH / 4) * i
                drawLine(
                    color = Color(0xFFFFEB3B),
                    start = Offset(x - ladW / 2, stepY),
                    end = Offset(x + ladW / 2, stepY),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
        }
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawTacticalAnnotation(
    ann: TacticalAnnotation,
    width: Float,
    height: Float,
    textMeasurer: TextMeasurer
) {
    if (ann.points.isEmpty()) return
    val strokePx = ann.strokeWidth.dp.toPx()
    val color = Color(ann.color)
    val dashEffect = if (ann.isDashed) PathEffect.dashPathEffect(floatArrayOf(18f, 14f)) else null

    when (ann.tool) {
        AnnotationTool.PEN -> {
            if (ann.points.size < 2) return
            val path = Path().apply {
                val p0 = ann.points.first()
                moveTo(p0.x * width, p0.y * height)
                for (i in 1 until ann.points.size) {
                    val prev = ann.points[i - 1]
                    val curr = ann.points[i]
                    val midX = ((prev.x + curr.x) / 2f) * width
                    val midY = ((prev.y + curr.y) / 2f) * height
                    quadraticBezierTo(prev.x * width, prev.y * height, midX, midY)
                }
                val pLast = ann.points.last()
                lineTo(pLast.x * width, pLast.y * height)
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(
                    width = strokePx,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = dashEffect
                )
            )
        }
        AnnotationTool.ARROW -> {
            if (ann.points.size < 2) return
            val p1 = ann.points.first()
            val p2 = ann.points.last()
            val sx = p1.x * width
            val sy = p1.y * height
            val ex = p2.x * width
            val ey = p2.y * height

            val angle = atan2(ey - sy, ex - sx)
            val headLen = (strokePx * 3.5f).coerceIn(16.dp.toPx(), 36.dp.toPx())

            // Line shaft
            drawLine(
                color = color,
                start = Offset(sx, sy),
                end = Offset(ex, ey),
                strokeWidth = strokePx,
                cap = StrokeCap.Round,
                pathEffect = dashEffect
            )

            // Arrow head
            val arrowPath = Path().apply {
                moveTo(ex, ey)
                lineTo(
                    ex - headLen * cos(angle - Math.PI.toFloat() / 6f),
                    ey - headLen * sin(angle - Math.PI.toFloat() / 6f)
                )
                lineTo(
                    ex - headLen * 0.7f * cos(angle),
                    ey - headLen * 0.7f * sin(angle)
                )
                lineTo(
                    ex - headLen * 3.5f / 3.5f * headLen / headLen * cos(angle + Math.PI.toFloat() / 6f),
                    ey - headLen * sin(angle + Math.PI.toFloat() / 6f)
                )
                close()
            }
            drawPath(arrowPath, color)
        }
        AnnotationTool.ZONE -> {
            if (ann.points.size < 2) return
            val p1 = ann.points.first()
            val p2 = ann.points.last()
            val cx = ((p1.x + p2.x) / 2f) * width
            val cy = ((p1.y + p2.y) / 2f) * height
            val radius = (hypot((p2.x - p1.x) * width, (p2.y - p1.y) * height) / 2f).coerceAtLeast(20.dp.toPx())

            // Translucent tactical space fill
            drawCircle(
                color = color.copy(alpha = 0.22f),
                radius = radius,
                center = Offset(cx, cy)
            )
            // Tactical boundary outline
            drawCircle(
                color = color.copy(alpha = 0.85f),
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(
                    width = strokePx,
                    pathEffect = dashEffect
                )
            )
        }
        AnnotationTool.NOTE -> {
            val p = ann.points.firstOrNull() ?: return
            val noteText = ann.noteText ?: "Note"
            drawNoteBadgeOnPitch(p.x * width, p.y * height, noteText, color, textMeasurer)
        }
        else -> {}
    }
}

private fun DrawScope.drawLaserPointerBeam(laserPoints: List<LaserPoint>, width: Float, height: Float) {
    val now = System.currentTimeMillis()
    if (laserPoints.isEmpty()) return

    // Draw fading trail lines
    for (i in 1 until laserPoints.size) {
        val p0 = laserPoints[i - 1]
        val p1 = laserPoints[i]
        val age = (now - p1.timestamp).toFloat()
        val alpha = (1f - (age / 900f)).coerceIn(0f, 1f)
        if (alpha > 0.05f) {
            drawLine(
                color = Color(0xFF00E5FF).copy(alpha = alpha * 0.85f),
                start = Offset(p0.x * width, p0.y * height),
                end = Offset(p1.x * width, p1.y * height),
                strokeWidth = (7.dp.toPx() * alpha).coerceAtLeast(2f),
                cap = StrokeCap.Round
            )
        }
    }

    // Draw glowing pointer dot at head
    val tip = laserPoints.last()
    val tipAge = (now - tip.timestamp).toFloat()
    val tipAlpha = (1f - (tipAge / 900f)).coerceIn(0f, 1f)
    if (tipAlpha > 0.05f) {
        val tx = tip.x * width
        val ty = tip.y * height
        // Outer radiant laser halo
        drawCircle(
            color = Color(0x6600E5FF),
            radius = 18.dp.toPx() * tipAlpha,
            center = Offset(tx, ty)
        )
        // Mid neon glow
        drawCircle(
            color = Color(0xFF00E5FF).copy(alpha = tipAlpha),
            radius = 9.dp.toPx() * tipAlpha,
            center = Offset(tx, ty)
        )
        // Core white-hot spot
        drawCircle(
            color = Color.White.copy(alpha = tipAlpha),
            radius = 4.dp.toPx() * tipAlpha,
            center = Offset(tx, ty)
        )
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawQuickTacticalNotePin(
    note: QuickTacticalNote,
    width: Float,
    height: Float,
    textMeasurer: TextMeasurer
) {
    val px = note.x * width
    val py = note.y * height
    val pinColor = Color(0xFFFFEE58) // Yellow sticky note accent
    drawNoteBadgeOnPitch(px, py, note.text, pinColor, textMeasurer)
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawNoteBadgeOnPitch(
    x: Float,
    y: Float,
    text: String,
    accentColor: Color,
    textMeasurer: TextMeasurer
) {
    val style = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
    )
    val layoutResult = textMeasurer.measure(
        text = text,
        style = style,
        maxLines = 2
    )

    val padH = 8.dp.toPx()
    val padV = 5.dp.toPx()
    val badgeW = layoutResult.size.width + (padH * 2) + 16.dp.toPx()
    val badgeH = layoutResult.size.height + (padV * 2)

    val badgeLeft = (x - badgeW / 2).coerceIn(4f, size.width - badgeW - 4f)
    val badgeTop = (y - badgeH - 8.dp.toPx()).coerceIn(4f, size.height - badgeH - 4f)

    // Dark translucent background with border
    drawRoundRect(
        color = Color(0xDD121A21),
        topLeft = Offset(badgeLeft, badgeTop),
        size = Size(badgeW, badgeH),
        cornerRadius = CornerRadius(6.dp.toPx())
    )
    drawRoundRect(
        color = accentColor.copy(alpha = 0.8f),
        topLeft = Offset(badgeLeft, badgeTop),
        size = Size(badgeW, badgeH),
        cornerRadius = CornerRadius(6.dp.toPx()),
        style = Stroke(width = 1.5.dp.toPx())
    )

    // Pin indicator dot on left
    drawCircle(
        color = accentColor,
        radius = 3.dp.toPx(),
        center = Offset(badgeLeft + 10.dp.toPx(), badgeTop + badgeH / 2)
    )

    // Text label
    drawText(
        textLayoutResult = layoutResult,
        topLeft = Offset(badgeLeft + 18.dp.toPx(), badgeTop + padV)
    )

    // Pointer pin line connecting to coordinate
    drawLine(
        color = accentColor,
        start = Offset(x, y),
        end = Offset(x, badgeTop + badgeH),
        strokeWidth = 2.dp.toPx()
    )
    drawCircle(
        color = accentColor,
        radius = 4.dp.toPx(),
        center = Offset(x, y)
    )
}

