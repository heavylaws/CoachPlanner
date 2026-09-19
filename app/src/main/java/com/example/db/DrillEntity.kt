package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "soccer_drills")
data class DrillEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val focusArea: String,
    val durationMinutes: Int,
    val pitchView: String,
    val description: String,
    val coachingCuesJson: String,
    val phasesJson: String,
    val createdByRole: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): SoccerDrill {
        val cues = mutableListOf<String>()
        try {
            val cuesArray = JSONArray(coachingCuesJson)
            for (i in 0 until cuesArray.length()) {
                cues.add(cuesArray.getString(i))
            }
        } catch (_: Exception) {}

        val phases = mutableListOf<DrillPhase>()
        try {
            val phasesArray = JSONArray(phasesJson)
            for (i in 0 until phasesArray.length()) {
                val phaseObj = phasesArray.getJSONObject(i)
                val step = phaseObj.optInt("step", i + 1)
                val title = phaseObj.optString("title", "Phase $step")
                val instruction = phaseObj.optString("instruction", "")
                val durationSec = phaseObj.optDouble("durationSec", 3.0).toFloat()

                // Players
                val players = mutableListOf<TacticalPlayer>()
                val playersArray = phaseObj.optJSONArray("players") ?: JSONArray()
                for (p in 0 until playersArray.length()) {
                    val pObj = playersArray.getJSONObject(p)
                    players.add(
                        TacticalPlayer(
                            id = pObj.optString("id", "p_$p"),
                            number = pObj.optInt("number", p + 1),
                            role = TeamRole.valueOf(pObj.optString("role", TeamRole.ATTACK.name)),
                            x = pObj.optDouble("x", 0.5).toFloat(),
                            y = pObj.optDouble("y", 0.5).toFloat(),
                            targetX = pObj.optDouble("targetX", pObj.optDouble("x", 0.5)).toFloat(),
                            targetY = pObj.optDouble("targetY", pObj.optDouble("y", 0.5)).toFloat(),
                            label = pObj.optString("label", ""),
                            hasBall = pObj.optBoolean("hasBall", false)
                        )
                    )
                }

                // Ball
                val ballObj = phaseObj.optJSONObject("ball") ?: JSONObject()
                val ball = TacticalBall(
                    x = ballObj.optDouble("x", 0.5).toFloat(),
                    y = ballObj.optDouble("y", 0.5).toFloat(),
                    targetX = ballObj.optDouble("targetX", ballObj.optDouble("x", 0.5)).toFloat(),
                    targetY = ballObj.optDouble("targetY", ballObj.optDouble("y", 0.5)).toFloat(),
                    trajectory = try {
                        BallTrajectory.valueOf(ballObj.optString("trajectory", BallTrajectory.GROUND_PASS.name))
                    } catch (_: Exception) {
                        BallTrajectory.GROUND_PASS
                    }
                )

                // Equipment
                val equipmentList = mutableListOf<TacticalEquipment>()
                val equipArray = phaseObj.optJSONArray("equipment") ?: JSONArray()
                for (e in 0 until equipArray.length()) {
                    val eObj = equipArray.getJSONObject(e)
                    equipmentList.add(
                        TacticalEquipment(
                            id = eObj.optString("id", "eq_$e"),
                            type = try {
                                EquipmentType.valueOf(eObj.optString("type", EquipmentType.CONE.name))
                            } catch (_: Exception) {
                                EquipmentType.CONE
                            },
                            x = eObj.optDouble("x", 0.5).toFloat(),
                            y = eObj.optDouble("y", 0.5).toFloat()
                        )
                    )
                }

                phases.add(
                    DrillPhase(
                        step = step,
                        title = title,
                        instruction = instruction,
                        durationSec = durationSec,
                        players = players,
                        ball = ball,
                        equipment = equipmentList
                    )
                )
            }
        } catch (_: Exception) {}

        return SoccerDrill(
            id = id,
            title = title,
            category = category,
            focusArea = focusArea,
            durationMinutes = durationMinutes,
            pitchView = pitchView,
            description = description,
            coachingCues = cues,
            phases = phases,
            createdByRole = try {
                UserRole.valueOf(createdByRole)
            } catch (_: Exception) {
                UserRole.HEAD_COACH
            }
        )
    }

    companion object {
        fun fromDomain(drill: SoccerDrill): DrillEntity {
            val cuesArray = JSONArray()
            drill.coachingCues.forEach { cuesArray.put(it) }

            val phasesArray = JSONArray()
            drill.phases.forEach { phase ->
                val pObj = JSONObject()
                pObj.put("step", phase.step)
                pObj.put("title", phase.title)
                pObj.put("instruction", phase.instruction)
                pObj.put("durationSec", phase.durationSec)

                val playersArray = JSONArray()
                phase.players.forEach { pl ->
                    val plObj = JSONObject()
                    plObj.put("id", pl.id)
                    plObj.put("number", pl.number)
                    plObj.put("role", pl.role.name)
                    plObj.put("x", pl.x.toDouble())
                    plObj.put("y", pl.y.toDouble())
                    plObj.put("targetX", pl.targetX.toDouble())
                    plObj.put("targetY", pl.targetY.toDouble())
                    plObj.put("label", pl.label)
                    plObj.put("hasBall", pl.hasBall)
                    playersArray.put(plObj)
                }
                pObj.put("players", playersArray)

                val ballObj = JSONObject()
                ballObj.put("x", phase.ball.x.toDouble())
                ballObj.put("y", phase.ball.y.toDouble())
                ballObj.put("targetX", phase.ball.targetX.toDouble())
                ballObj.put("targetY", phase.ball.targetY.toDouble())
                ballObj.put("trajectory", phase.ball.trajectory.name)
                pObj.put("ball", ballObj)

                val equipArray = JSONArray()
                phase.equipment.forEach { eq ->
                    val eqObj = JSONObject()
                    eqObj.put("id", eq.id)
                    eqObj.put("type", eq.type.name)
                    eqObj.put("x", eq.x.toDouble())
                    eqObj.put("y", eq.y.toDouble())
                    equipArray.put(eqObj)
                }
                pObj.put("equipment", equipArray)

                phasesArray.put(pObj)
            }

            return DrillEntity(
                id = drill.id,
                title = drill.title,
                category = drill.category,
                focusArea = drill.focusArea,
                durationMinutes = drill.durationMinutes,
                pitchView = drill.pitchView,
                description = drill.description,
                coachingCuesJson = cuesArray.toString(),
                phasesJson = phasesArray.toString(),
                createdByRole = drill.createdByRole.name
            )
        }
    }
}
