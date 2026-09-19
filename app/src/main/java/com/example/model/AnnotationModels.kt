package com.example.model

enum class AnnotationTool(val displayName: String) {
    MOVE("Move"),
    PEN("Draw"),
    ARROW("Arrow"),
    ZONE("Zone"),
    LASER("Laser"),
    NOTE("Note"),
    ERASER("Eraser")
}

data class AnnotationPoint(
    val x: Float, // Normalized 0f..1f
    val y: Float  // Normalized 0f..1f
)

data class TacticalAnnotation(
    val id: String = "ann_${System.currentTimeMillis()}_${(100..999).random()}",
    val tool: AnnotationTool,
    val points: List<AnnotationPoint>,
    val color: Long = 0xFFFFEE58, // Neon Yellow default
    val strokeWidth: Float = 5f,
    val isDashed: Boolean = false,
    val noteText: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class LaserPoint(
    val x: Float,
    val y: Float,
    val timestamp: Long = System.currentTimeMillis()
)

data class QuickTacticalNote(
    val id: String,
    val phaseIndex: Int,
    val x: Float,
    val y: Float,
    val text: String,
    val authorRole: UserRole = UserRole.HEAD_COACH,
    val timestamp: Long = System.currentTimeMillis()
)
