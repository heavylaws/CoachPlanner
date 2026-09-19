package com.example.model

enum class TeamRole {
    ATTACK,
    DEFENSE,
    NEUTRAL,
    GOALKEEPER
}

enum class UserRole(val displayName: String, val badgeColor: Long) {
    HEAD_COACH("Head Coach", 0xFF00E5FF),
    ASSISTANT_COACH("Assistant Coach", 0xFF00E676),
    PLAYER("Player", 0xFFFFD600);

    val canCreateDrills: Boolean get() = this != PLAYER
    val canEditDrills: Boolean get() = this != PLAYER
    val canDeleteDrills: Boolean get() = this == HEAD_COACH
    val canUseAiPrompt: Boolean get() = this != PLAYER
    val canManageRoles: Boolean get() = this == HEAD_COACH
}

enum class BallTrajectory {
    GROUND_PASS,
    AERIAL_PASS,
    DRIBBLE,
    SHOT
}

enum class EquipmentType {
    CONE,
    MINI_GOAL,
    MANNEQUIN,
    AGILITY_LADDER
}

data class TacticalPlayer(
    val id: String,
    val number: Int,
    val role: TeamRole,
    val x: Float, // 0f..1f (pitch width ratio)
    val y: Float, // 0f..1f (pitch length ratio)
    val targetX: Float = x,
    val targetY: Float = y,
    val label: String = "",
    val hasBall: Boolean = false
)

data class TacticalBall(
    val x: Float,
    val y: Float,
    val targetX: Float = x,
    val targetY: Float = y,
    val trajectory: BallTrajectory = BallTrajectory.GROUND_PASS
)

data class TacticalEquipment(
    val id: String,
    val type: EquipmentType,
    val x: Float,
    val y: Float
)

data class DrillPhase(
    val step: Int,
    val title: String,
    val instruction: String,
    val durationSec: Float = 3f,
    val players: List<TacticalPlayer>,
    val ball: TacticalBall,
    val equipment: List<TacticalEquipment> = emptyList()
)

data class SoccerDrill(
    val id: String,
    val title: String,
    val category: String,
    val focusArea: String,
    val durationMinutes: Int,
    val pitchView: String = "FULL", // "FULL" or "HALF"
    val description: String,
    val coachingCues: List<String>,
    val phases: List<DrillPhase>,
    val createdByRole: UserRole = UserRole.HEAD_COACH
)

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val team: String,
    val avatarColor: Long
)
