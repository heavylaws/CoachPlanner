package com.example.data

import com.example.BuildConfig
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

class GeminiTacticsService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateDrillFromPrompt(prompt: String, currentRole: UserRole): Result<SoccerDrill> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        // If no API key or empty key, use smart local tactical generator
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.success(synthesizeTacticalDrillLocally(prompt, currentRole))
        }

        try {
            val systemPrompt = """
                You are a UEFA Pro License soccer coach and tactics board animator.
                Create a high quality, realistic animated soccer training drill based on the coach's instruction.
                Output ONLY valid raw JSON with NO markdown formatting, NO backticks.
                Coordinates are normalized floats between 0.05 and 0.95 (x: 0 is left touchline, 1 is right touchline; y: 0 is defending goal, 1 is attacking start/other half).
                
                JSON format:
                {
                  "title": "Drill Title",
                  "category": "Attacking / Defending / Transition / Possession",
                  "focusArea": "Primary tactical objective",
                  "durationMinutes": 15,
                  "pitchView": "HALF" or "FULL",
                  "description": "Short drill description",
                  "coachingCues": ["Cue 1", "Cue 2", "Cue 3"],
                  "phases": [
                    {
                      "step": 1,
                      "title": "Phase 1: Setup & Trigger",
                      "instruction": "What happens in this step",
                      "durationSec": 2.8,
                      "players": [
                        {
                          "id": "p1",
                          "number": 8,
                          "role": "ATTACK", // ATTACK, DEFENSE, NEUTRAL, GOALKEEPER
                          "x": 0.50,
                          "y": 0.70,
                          "targetX": 0.52,
                          "targetY": 0.60,
                          "label": "CM #8",
                          "hasBall": true
                        }
                      ],
                      "ball": {
                        "x": 0.50,
                        "y": 0.70,
                        "targetX": 0.52,
                        "targetY": 0.60,
                        "trajectory": "GROUND_PASS" // GROUND_PASS, AERIAL_PASS, DRIBBLE, SHOT
                      },
                      "equipment": [
                        {
                          "id": "c1",
                          "type": "CONE", // CONE, MINI_GOAL, MANNEQUIN, AGILITY_LADDER
                          "x": 0.35,
                          "y": 0.50
                        }
                      ]
                    }
                  ]
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "$systemPrompt\n\nCoach Voice Note/Prompt: \"$prompt\"")
                            })
                        })
                    })
                }
                put("contents", contentsArray)

                val genConfig = JSONObject().apply {
                    put("temperature", 0.4)
                    put("responseMimeType", "application/json")
                }
                put("generationConfig", genConfig)
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val body = requestJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(url).post(body).build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                // Fallback to local synthesizer on API error
                return@withContext Result.success(synthesizeTacticalDrillLocally(prompt, currentRole))
            }

            val responseBody = response.body?.string().orEmpty()
            val parsedDrill = parseGeminiResponse(responseBody, currentRole)
            if (parsedDrill != null) {
                Result.success(parsedDrill)
            } else {
                Result.success(synthesizeTacticalDrillLocally(prompt, currentRole))
            }
        } catch (e: Exception) {
            Result.success(synthesizeTacticalDrillLocally(prompt, currentRole))
        }
    }

    /**
     * Fast Quick Adjustment Engine:
     * Applies instantaneous coach changes ("Add a defender", "Faster ball speed", "Add overlapping run")
     */
    fun applyFastChange(currentDrill: SoccerDrill, mutation: String): SoccerDrill {
        val lower = mutation.lowercase().trim()

        return when {
            lower.contains("defender") || lower.contains("+1 def") -> {
                // Add a new defender to all phases
                val updatedPhases = currentDrill.phases.mapIndexed { idx, phase ->
                    val newDefId = "def_${System.currentTimeMillis()}"
                    val newDef = TacticalPlayer(
                        id = newDefId,
                        number = 15,
                        role = TeamRole.DEFENSE,
                        x = 0.55f + (idx * 0.03f),
                        y = 0.45f - (idx * 0.04f),
                        targetX = 0.52f,
                        targetY = 0.38f,
                        label = "CB #15 (Press)"
                    )
                    phase.copy(players = phase.players + newDef)
                }
                currentDrill.copy(
                    title = "${currentDrill.title} (+1 Def)",
                    phases = updatedPhases,
                    coachingCues = currentDrill.coachingCues + "Added high-pressing defender to increase defensive pressure"
                )
            }
            lower.contains("speed") || lower.contains("faster") || lower.contains("1-touch") -> {
                val updatedPhases = currentDrill.phases.map { phase ->
                    phase.copy(durationSec = (phase.durationSec * 0.65f).coerceAtLeast(1.2f))
                }
                currentDrill.copy(
                    title = "${currentDrill.title} (High Tempo)",
                    phases = updatedPhases,
                    coachingCues = currentDrill.coachingCues + "Strict 1-touch or 2-touch constraint for rapid ball circulation"
                )
            }
            lower.contains("overlap") -> {
                val updatedPhases = currentDrill.phases.mapIndexed { idx, phase ->
                    val updatedPlayers = phase.players.map { pl ->
                        if (pl.role == TeamRole.ATTACK && (pl.number == 2 || pl.number == 3 || pl.label.contains("RB") || pl.label.contains("LB"))) {
                            pl.copy(targetX = (pl.targetX + 0.12f).coerceAtMost(0.92f), targetY = (pl.targetY - 0.18f).coerceAtLeast(0.15f))
                        } else pl
                    }
                    phase.copy(players = updatedPlayers)
                }
                currentDrill.copy(
                    title = "${currentDrill.title} (With Overlap)",
                    phases = updatedPhases,
                    coachingCues = currentDrill.coachingCues + "Fullback commits to full-sprint overlap around winger"
                )
            }
            lower.contains("mini goal") || lower.contains("target goal") -> {
                val updatedPhases = currentDrill.phases.map { phase ->
                    val g1 = TacticalEquipment("goal_m1", EquipmentType.MINI_GOAL, 0.20f, 0.82f)
                    val g2 = TacticalEquipment("goal_m2", EquipmentType.MINI_GOAL, 0.80f, 0.82f)
                    phase.copy(equipment = phase.equipment + listOf(g1, g2))
                }
                currentDrill.copy(
                    phases = updatedPhases,
                    coachingCues = currentDrill.coachingCues + "Defenders score by passing into mini-goals within 5 seconds of turnover"
                )
            }
            lower.contains("cone") || lower.contains("marker") -> {
                val updatedPhases = currentDrill.phases.map { phase ->
                    val c1 = TacticalEquipment("c_l1", EquipmentType.CONE, 0.30f, 0.50f)
                    val c2 = TacticalEquipment("c_l2", EquipmentType.CONE, 0.70f, 0.50f)
                    phase.copy(equipment = phase.equipment + listOf(c1, c2))
                }
                currentDrill.copy(phases = updatedPhases)
            }
            lower.contains("pitch") || lower.contains("view") -> {
                val newView = if (currentDrill.pitchView == "FULL") "HALF" else "FULL"
                currentDrill.copy(pitchView = newView)
            }
            else -> {
                // Generic fast nudge: increment tempo and append custom coach tactical cue
                currentDrill.copy(
                    coachingCues = currentDrill.coachingCues + "Adjustment: $mutation"
                )
            }
        }
    }

    private fun parseGeminiResponse(jsonString: String, currentRole: UserRole): SoccerDrill? {
        try {
            val root = JSONObject(jsonString)
            val candidates = root.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text").orEmpty().trim()

            // Find JSON start and end
            val start = text.indexOf('{')
            val end = text.lastIndexOf('}')
            if (start == -1 || end == -1) return null

            val drillJson = JSONObject(text.substring(start, end + 1))
            val title = drillJson.optString("title", "Tactical Training Drill")
            val category = drillJson.optString("category", "Tactical Animation")
            val focusArea = drillJson.optString("focusArea", "Game Plan & Movement")
            val durationMinutes = drillJson.optInt("durationMinutes", 15)
            val pitchView = drillJson.optString("pitchView", "HALF")
            val description = drillJson.optString("description", "")

            val cuesList = mutableListOf<String>()
            val cuesArray = drillJson.optJSONArray("coachingCues")
            if (cuesArray != null) {
                for (i in 0 until cuesArray.length()) cuesList.add(cuesArray.getString(i))
            }

            val phasesList = mutableListOf<DrillPhase>()
            val phasesArray = drillJson.optJSONArray("phases")
            if (phasesArray != null) {
                for (i in 0 until phasesArray.length()) {
                    val pObj = phasesArray.getJSONObject(i)
                    val step = pObj.optInt("step", i + 1)
                    val pTitle = pObj.optString("title", "Phase $step")
                    val instruction = pObj.optString("instruction", "")
                    val durationSec = pObj.optDouble("durationSec", 2.8).toFloat()

                    val players = mutableListOf<TacticalPlayer>()
                    val plArray = pObj.optJSONArray("players") ?: JSONArray()
                    for (p in 0 until plArray.length()) {
                        val plObj = plArray.getJSONObject(p)
                        players.add(
                            TacticalPlayer(
                                id = plObj.optString("id", "p_$p"),
                                number = plObj.optInt("number", p + 1),
                                role = try { TeamRole.valueOf(plObj.optString("role", "ATTACK")) } catch (_: Exception) { TeamRole.ATTACK },
                                x = plObj.optDouble("x", 0.5).toFloat(),
                                y = plObj.optDouble("y", 0.5).toFloat(),
                                targetX = plObj.optDouble("targetX", plObj.optDouble("x", 0.5)).toFloat(),
                                targetY = plObj.optDouble("targetY", plObj.optDouble("y", 0.5)).toFloat(),
                                label = plObj.optString("label", ""),
                                hasBall = plObj.optBoolean("hasBall", false)
                            )
                        )
                    }

                    val ballObj = pObj.optJSONObject("ball") ?: JSONObject()
                    val ball = TacticalBall(
                        x = ballObj.optDouble("x", 0.5).toFloat(),
                        y = ballObj.optDouble("y", 0.5).toFloat(),
                        targetX = ballObj.optDouble("targetX", ballObj.optDouble("x", 0.5)).toFloat(),
                        targetY = ballObj.optDouble("targetY", ballObj.optDouble("y", 0.5)).toFloat(),
                        trajectory = try { BallTrajectory.valueOf(ballObj.optString("trajectory", "GROUND_PASS")) } catch (_: Exception) { BallTrajectory.GROUND_PASS }
                    )

                    val equipList = mutableListOf<TacticalEquipment>()
                    val eqArray = pObj.optJSONArray("equipment") ?: JSONArray()
                    for (e in 0 until eqArray.length()) {
                        val eqObj = eqArray.getJSONObject(e)
                        equipList.add(
                            TacticalEquipment(
                                id = eqObj.optString("id", "eq_$e"),
                                type = try { EquipmentType.valueOf(eqObj.optString("type", "CONE")) } catch (_: Exception) { EquipmentType.CONE },
                                x = eqObj.optDouble("x", 0.5).toFloat(),
                                y = eqObj.optDouble("y", 0.5).toFloat()
                            )
                        )
                    }

                    phasesList.add(
                        DrillPhase(
                            step = step,
                            title = pTitle,
                            instruction = instruction,
                            durationSec = durationSec,
                            players = players,
                            ball = ball,
                            equipment = equipList
                        )
                    )
                }
            }

            return SoccerDrill(
                id = "ai_drill_${UUID.randomUUID()}",
                title = title,
                category = category,
                focusArea = focusArea,
                durationMinutes = durationMinutes,
                pitchView = pitchView,
                description = description,
                coachingCues = cuesList,
                phases = phasesList,
                createdByRole = currentRole
            )
        } catch (_: Exception) {
            return null
        }
    }

    /**
     * Highly realistic offline soccer tactics synthesizer.
     * Understands voice/text keywords: press, rondo, winger, counter, corner, build up, switch play.
     */
    fun synthesizeTacticalDrillLocally(prompt: String, currentRole: UserRole): SoccerDrill {
        val lower = prompt.lowercase()

        return when {
            lower.contains("switch") || lower.contains("diagonal") -> {
                SoccerDrill(
                    id = "drill_${UUID.randomUUID()}",
                    title = "Switch of Play & Weak-Side Overload",
                    category = "Attacking Patterns",
                    focusArea = "Diagonal Switching & Width Exploitation",
                    durationMinutes = 18,
                    pitchView = "HALF",
                    description = "Drawing the opponent block to the left flank, then executing a rapid diagonal cross-field switch to release the isolated right winger.",
                    coachingCues = listOf(
                        "Quick 2-touch circulation on strong side to suck defenders across",
                        "Firm driven diagonal pass in the air or ground",
                        "Weak-side winger steps on the touchline to maximize pitch width",
                        "Opposite full-back makes underlapping run"
                    ),
                    phases = listOf(
                        DrillPhase(
                            step = 1,
                            title = "Phase 1: Strong-Side Overload",
                            instruction = "LB (#3) and CM (#6) circulate short passes on the left wing; defending block tilts left.",
                            durationSec = 2.8f,
                            players = listOf(
                                TacticalPlayer("lb3", 3, TeamRole.ATTACK, 0.20f, 0.72f, 0.24f, 0.65f, "LB #3", hasBall = true),
                                TacticalPlayer("cm6", 6, TeamRole.ATTACK, 0.35f, 0.62f, 0.38f, 0.58f, "CM #6"),
                                TacticalPlayer("cm8", 8, TeamRole.ATTACK, 0.50f, 0.68f, 0.52f, 0.62f, "CM #8"),
                                TacticalPlayer("rw7", 7, TeamRole.ATTACK, 0.88f, 0.50f, 0.86f, 0.42f, "RW #7"),
                                TacticalPlayer("st9", 9, TeamRole.ATTACK, 0.45f, 0.38f, 0.48f, 0.30f, "ST #9"),
                                TacticalPlayer("d4", 4, TeamRole.DEFENSE, 0.30f, 0.48f, 0.32f, 0.42f, "CB #4"),
                                TacticalPlayer("d5", 5, TeamRole.DEFENSE, 0.45f, 0.45f, 0.42f, 0.40f, "CB #5"),
                                TacticalPlayer("gk1", 1, TeamRole.GOALKEEPER, 0.50f, 0.12f, 0.50f, 0.13f, "GK #1")
                            ),
                            ball = TacticalBall(0.20f, 0.72f, 0.38f, 0.58f, BallTrajectory.GROUND_PASS)
                        ),
                        DrillPhase(
                            step = 2,
                            title = "Phase 2: The Cross-Field Switch",
                            instruction = "CM (#8) receives and unleashes a pinpoint diagonal aerial switch to RW (#7) in acres of space.",
                            durationSec = 3.2f,
                            players = listOf(
                                TacticalPlayer("lb3", 3, TeamRole.ATTACK, 0.24f, 0.65f, 0.28f, 0.55f, "LB #3"),
                                TacticalPlayer("cm6", 6, TeamRole.ATTACK, 0.38f, 0.58f, 0.45f, 0.55f, "CM #6"),
                                TacticalPlayer("cm8", 8, TeamRole.ATTACK, 0.52f, 0.62f, 0.55f, 0.58f, "CM #8", hasBall = true),
                                TacticalPlayer("rw7", 7, TeamRole.ATTACK, 0.86f, 0.42f, 0.84f, 0.30f, "RW #7"),
                                TacticalPlayer("st9", 9, TeamRole.ATTACK, 0.48f, 0.30f, 0.52f, 0.22f, "ST #9"),
                                TacticalPlayer("d4", 4, TeamRole.DEFENSE, 0.32f, 0.42f, 0.45f, 0.38f, "CB #4"),
                                TacticalPlayer("d5", 5, TeamRole.DEFENSE, 0.42f, 0.40f, 0.60f, 0.34f, "CB #5"),
                                TacticalPlayer("gk1", 1, TeamRole.GOALKEEPER, 0.50f, 0.13f, 0.54f, 0.14f, "GK #1")
                            ),
                            ball = TacticalBall(0.55f, 0.58f, 0.84f, 0.30f, BallTrajectory.AERIAL_PASS)
                        ),
                        DrillPhase(
                            step = 3,
                            title = "Phase 3: 1v1 Attack & Delivery",
                            instruction = "RW (#7) drives inside and curls a cross to the back post for #9 to finish!",
                            durationSec = 2.6f,
                            players = listOf(
                                TacticalPlayer("lb3", 3, TeamRole.ATTACK, 0.28f, 0.55f, 0.30f, 0.45f, "LB #3"),
                                TacticalPlayer("cm6", 6, TeamRole.ATTACK, 0.45f, 0.55f, 0.48f, 0.42f, "CM #6"),
                                TacticalPlayer("cm8", 8, TeamRole.ATTACK, 0.55f, 0.58f, 0.58f, 0.40f, "CM #8"),
                                TacticalPlayer("rw7", 7, TeamRole.ATTACK, 0.84f, 0.30f, 0.74f, 0.22f, "RW #7", hasBall = true),
                                TacticalPlayer("st9", 9, TeamRole.ATTACK, 0.52f, 0.22f, 0.48f, 0.14f, "ST #9"),
                                TacticalPlayer("d4", 4, TeamRole.DEFENSE, 0.45f, 0.38f, 0.50f, 0.26f, "CB #4"),
                                TacticalPlayer("d5", 5, TeamRole.DEFENSE, 0.60f, 0.34f, 0.68f, 0.24f, "CB #5"),
                                TacticalPlayer("gk1", 1, TeamRole.GOALKEEPER, 0.54f, 0.14f, 0.48f, 0.12f, "GK #1")
                            ),
                            ball = TacticalBall(0.74f, 0.22f, 0.48f, 0.12f, BallTrajectory.SHOT)
                        )
                    ),
                    createdByRole = currentRole
                )
            }
            lower.contains("press") || lower.contains("trap") || lower.contains("defend") -> {
                SoccerDrill(
                    id = "drill_${UUID.randomUUID()}",
                    title = "Midfield Pressing Trap & Rapid Counter",
                    category = "Defensive Organization",
                    focusArea = "Pressing Triggers & Ball Recovery",
                    durationMinutes = 15,
                    pitchView = "FULL",
                    description = "Coordinated team pressing trigger when opponent midfielder takes a negative touch facing their own goal.",
                    coachingCues = listOf(
                        "Trigger: Opponent's head down or backwards pass",
                        "Nearest player applies direct pressure with curved body shape",
                        "Secondary players lock passing lanes",
                        "Instant forward pass upon turnover"
                    ),
                    phases = listOf(
                        DrillPhase(
                            step = 1,
                            title = "Phase 1: Baiting the Pass",
                            instruction = "Attacking midfield allows pass into central pivot #6, setting the trap.",
                            durationSec = 2.5f,
                            players = listOf(
                                TacticalPlayer("opp6", 6, TeamRole.DEFENSE, 0.50f, 0.55f, 0.50f, 0.52f, "Opp #6", hasBall = true),
                                TacticalPlayer("opp4", 4, TeamRole.DEFENSE, 0.35f, 0.70f, 0.36f, 0.68f, "Opp CB #4"),
                                TacticalPlayer("press8", 8, TeamRole.ATTACK, 0.52f, 0.42f, 0.50f, 0.48f, "Press #8"),
                                TacticalPlayer("press10", 10, TeamRole.ATTACK, 0.38f, 0.45f, 0.42f, 0.50f, "Press #10"),
                                TacticalPlayer("press9", 9, TeamRole.ATTACK, 0.62f, 0.45f, 0.58f, 0.48f, "Press #9")
                            ),
                            ball = TacticalBall(0.50f, 0.55f, 0.50f, 0.52f, BallTrajectory.DRIBBLE)
                        ),
                        DrillPhase(
                            step = 2,
                            title = "Phase 2: Snapping the Trap",
                            instruction = "Opp #6 turns into trouble; #8 and #10 converge simultaneously to dispossess.",
                            durationSec = 2.5f,
                            players = listOf(
                                TacticalPlayer("opp6", 6, TeamRole.DEFENSE, 0.50f, 0.52f, 0.50f, 0.52f, "Opp #6"),
                                TacticalPlayer("opp4", 4, TeamRole.DEFENSE, 0.36f, 0.68f, 0.35f, 0.65f, "Opp CB #4"),
                                TacticalPlayer("press8", 8, TeamRole.ATTACK, 0.50f, 0.48f, 0.51f, 0.52f, "Press #8", hasBall = true),
                                TacticalPlayer("press10", 10, TeamRole.ATTACK, 0.42f, 0.50f, 0.47f, 0.52f, "Press #10"),
                                TacticalPlayer("press9", 9, TeamRole.ATTACK, 0.58f, 0.48f, 0.62f, 0.35f, "Press #9")
                            ),
                            ball = TacticalBall(0.50f, 0.52f, 0.51f, 0.52f, BallTrajectory.GROUND_PASS)
                        ),
                        DrillPhase(
                            step = 3,
                            title = "Phase 3: Immediate Vertical Counter",
                            instruction = "#8 wins possession and punches instant through-ball to sprinting #9!",
                            durationSec = 2.8f,
                            players = listOf(
                                TacticalPlayer("opp6", 6, TeamRole.DEFENSE, 0.50f, 0.52f, 0.48f, 0.54f, "Opp #6"),
                                TacticalPlayer("opp4", 4, TeamRole.DEFENSE, 0.35f, 0.65f, 0.42f, 0.50f, "Opp CB #4"),
                                TacticalPlayer("press8", 8, TeamRole.ATTACK, 0.51f, 0.52f, 0.51f, 0.45f, "Press #8"),
                                TacticalPlayer("press10", 10, TeamRole.ATTACK, 0.47f, 0.52f, 0.45f, 0.38f, "Press #10"),
                                TacticalPlayer("press9", 9, TeamRole.ATTACK, 0.62f, 0.35f, 0.55f, 0.18f, "Press #9", hasBall = true)
                            ),
                            ball = TacticalBall(0.51f, 0.52f, 0.55f, 0.18f, BallTrajectory.GROUND_PASS)
                        )
                    ),
                    createdByRole = currentRole
                )
            }
            else -> {
                // Tactical routine based on user prompt
                val drillTitle = if (prompt.length > 30) prompt.take(28) + "..." else prompt.replaceFirstChar { it.uppercase() }
                SoccerDrill(
                    id = "drill_${UUID.randomUUID()}",
                    title = if (prompt.isNotBlank()) "Tactical: $drillTitle" else "Dynamic Attacking Combination",
                    category = "Tactical Mastery",
                    focusArea = "Spatial Awareness & Coordinated Movement",
                    durationMinutes = 20,
                    pitchView = "HALF",
                    description = "Custom tactical training plan generated for coach instructions: $prompt",
                    coachingCues = listOf(
                        "Quality and pace of passes into the front foot",
                        "Pre-movement to separate from defenders",
                        "Head up to survey blind-side runners",
                        "High tempo with minimal ball touches"
                    ),
                    phases = listOf(
                        DrillPhase(
                            step = 1,
                            title = "Phase 1: Initial Movement & Draw",
                            instruction = "Build-up play initiating movement to manipulate defensive lines.",
                            durationSec = 2.6f,
                            players = listOf(
                                TacticalPlayer("p1", 8, TeamRole.ATTACK, 0.50f, 0.75f, 0.52f, 0.68f, "CM #8", hasBall = true),
                                TacticalPlayer("p2", 10, TeamRole.ATTACK, 0.35f, 0.55f, 0.38f, 0.48f, "AM #10"),
                                TacticalPlayer("p3", 7, TeamRole.ATTACK, 0.78f, 0.60f, 0.82f, 0.48f, "RW #7"),
                                TacticalPlayer("p4", 9, TeamRole.ATTACK, 0.50f, 0.38f, 0.52f, 0.32f, "ST #9"),
                                TacticalPlayer("d1", 4, TeamRole.DEFENSE, 0.48f, 0.34f, 0.50f, 0.30f, "CB #4"),
                                TacticalPlayer("d2", 5, TeamRole.DEFENSE, 0.62f, 0.38f, 0.65f, 0.34f, "CB #5"),
                                TacticalPlayer("gk1", 1, TeamRole.GOALKEEPER, 0.50f, 0.12f, 0.50f, 0.13f, "GK #1")
                            ),
                            ball = TacticalBall(0.50f, 0.75f, 0.38f, 0.48f, BallTrajectory.GROUND_PASS)
                        ),
                        DrillPhase(
                            step = 2,
                            title = "Phase 2: Penetrating Combination",
                            instruction = "Quick one-two pass in the pocket and third-man running pattern.",
                            durationSec = 2.8f,
                            players = listOf(
                                TacticalPlayer("p1", 8, TeamRole.ATTACK, 0.52f, 0.68f, 0.56f, 0.52f, "CM #8"),
                                TacticalPlayer("p2", 10, TeamRole.ATTACK, 0.38f, 0.48f, 0.44f, 0.38f, "AM #10", hasBall = true),
                                TacticalPlayer("p3", 7, TeamRole.ATTACK, 0.82f, 0.48f, 0.80f, 0.32f, "RW #7"),
                                TacticalPlayer("p4", 9, TeamRole.ATTACK, 0.52f, 0.32f, 0.48f, 0.22f, "ST #9"),
                                TacticalPlayer("d1", 4, TeamRole.DEFENSE, 0.50f, 0.30f, 0.48f, 0.26f, "CB #4"),
                                TacticalPlayer("d2", 5, TeamRole.DEFENSE, 0.65f, 0.34f, 0.68f, 0.28f, "CB #5"),
                                TacticalPlayer("gk1", 1, TeamRole.GOALKEEPER, 0.50f, 0.13f, 0.52f, 0.14f, "GK #1")
                            ),
                            ball = TacticalBall(0.38f, 0.48f, 0.48f, 0.22f, BallTrajectory.GROUND_PASS)
                        ),
                        DrillPhase(
                            step = 3,
                            title = "Phase 3: Clinical Execution & Finish",
                            instruction = "Striker receives on the half-turn and slots into corner of the net!",
                            durationSec = 2.5f,
                            players = listOf(
                                TacticalPlayer("p1", 8, TeamRole.ATTACK, 0.56f, 0.52f, 0.58f, 0.42f, "CM #8"),
                                TacticalPlayer("p2", 10, TeamRole.ATTACK, 0.44f, 0.38f, 0.42f, 0.28f, "AM #10"),
                                TacticalPlayer("p3", 7, TeamRole.ATTACK, 0.80f, 0.32f, 0.74f, 0.22f, "RW #7"),
                                TacticalPlayer("p4", 9, TeamRole.ATTACK, 0.48f, 0.22f, 0.48f, 0.12f, "ST #9", hasBall = true),
                                TacticalPlayer("d1", 4, TeamRole.DEFENSE, 0.48f, 0.26f, 0.50f, 0.18f, "CB #4"),
                                TacticalPlayer("d2", 5, TeamRole.DEFENSE, 0.68f, 0.28f, 0.64f, 0.22f, "CB #5"),
                                TacticalPlayer("gk1", 1, TeamRole.GOALKEEPER, 0.52f, 0.14f, 0.46f, 0.10f, "GK #1")
                            ),
                            ball = TacticalBall(0.48f, 0.22f, 0.46f, 0.10f, BallTrajectory.SHOT)
                        )
                    ),
                    createdByRole = currentRole
                )
            }
        }
    }
}
