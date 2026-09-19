package com.example.data

import com.example.model.*

object SampleTacticsData {

    val defaultUsers = listOf(
        UserProfile(
            id = "coach_head",
            name = "Coach Pep Guardiola",
            email = "pep.guardiola@tactics.club",
            role = UserRole.HEAD_COACH,
            team = "Elite FC First Squad",
            avatarColor = 0xFF00E5FF
        ),
        UserProfile(
            id = "coach_asst",
            name = "Coach Mikel Arteta",
            email = "mikel.arteta@tactics.club",
            role = UserRole.ASSISTANT_COACH,
            team = "Elite FC First Squad",
            avatarColor = 0xFF00E676
        ),
        UserProfile(
            id = "player_striker",
            name = "Marcus Rashford (#9)",
            email = "marcus.r@tactics.club",
            role = UserRole.PLAYER,
            team = "Elite FC First Squad",
            avatarColor = 0xFFFFD600
        )
    )

    val defaultDrills = listOf(
        SoccerDrill(
            id = "drill_overlap_wing",
            title = "Overlapping Wing Delivery & Box Attack",
            category = "Attacking Patterns",
            focusArea = "Wide Overload & Timing of Runs",
            durationMinutes = 15,
            pitchView = "HALF",
            description = "Midfield pivot releases winger, fullback overlaps at pace to deliver low cross for striker near-post finish.",
            coachingCues = listOf(
                "Fullback triggers sprint before ball reaches winger",
                "Firm, diagonal pass into running path",
                "Striker attacks near post, opposite winger attacks far post",
                "Quality of cutback cross behind defending line"
            ),
            phases = listOf(
                DrillPhase(
                    step = 1,
                    title = "Phase 1: Pivot Build-Up",
                    instruction = "CM (#8) collects from deep, scans wide option.",
                    durationSec = 2.5f,
                    players = listOf(
                        TacticalPlayer("cm8", 8, TeamRole.ATTACK, 0.50f, 0.78f, 0.50f, 0.72f, "CM #8", hasBall = true),
                        TacticalPlayer("w7", 7, TeamRole.ATTACK, 0.80f, 0.62f, 0.75f, 0.55f, "RW #7"),
                        TacticalPlayer("rb2", 2, TeamRole.ATTACK, 0.85f, 0.82f, 0.88f, 0.65f, "RB #2"),
                        TacticalPlayer("st9", 9, TeamRole.ATTACK, 0.48f, 0.40f, 0.52f, 0.35f, "ST #9"),
                        TacticalPlayer("cam10", 10, TeamRole.ATTACK, 0.35f, 0.50f, 0.38f, 0.42f, "AM #10"),
                        TacticalPlayer("def4", 4, TeamRole.DEFENSE, 0.45f, 0.32f, 0.48f, 0.30f, "CB #4"),
                        TacticalPlayer("def5", 5, TeamRole.DEFENSE, 0.65f, 0.35f, 0.68f, 0.32f, "LB #5"),
                        TacticalPlayer("gk1", 1, TeamRole.GOALKEEPER, 0.50f, 0.12f, 0.50f, 0.14f, "GK #1")
                    ),
                    ball = TacticalBall(0.50f, 0.78f, 0.50f, 0.72f, BallTrajectory.DRIBBLE),
                    equipment = listOf(
                        TacticalEquipment("cone1", EquipmentType.CONE, 0.30f, 0.50f),
                        TacticalEquipment("cone2", EquipmentType.CONE, 0.70f, 0.50f)
                    )
                ),
                DrillPhase(
                    step = 2,
                    title = "Phase 2: Overlapping Release",
                    instruction = "CM slips pass to winger who holds up while fullback (#2) accelerates on outside overlap.",
                    durationSec = 3.0f,
                    players = listOf(
                        TacticalPlayer("cm8", 8, TeamRole.ATTACK, 0.50f, 0.72f, 0.55f, 0.60f, "CM #8"),
                        TacticalPlayer("w7", 7, TeamRole.ATTACK, 0.75f, 0.55f, 0.78f, 0.48f, "RW #7", hasBall = true),
                        TacticalPlayer("rb2", 2, TeamRole.ATTACK, 0.88f, 0.65f, 0.90f, 0.38f, "RB #2"),
                        TacticalPlayer("st9", 9, TeamRole.ATTACK, 0.52f, 0.35f, 0.50f, 0.28f, "ST #9"),
                        TacticalPlayer("cam10", 10, TeamRole.ATTACK, 0.38f, 0.42f, 0.40f, 0.32f, "AM #10"),
                        TacticalPlayer("def4", 4, TeamRole.DEFENSE, 0.48f, 0.30f, 0.50f, 0.26f, "CB #4"),
                        TacticalPlayer("def5", 5, TeamRole.DEFENSE, 0.68f, 0.32f, 0.78f, 0.45f, "LB #5"),
                        TacticalPlayer("gk1", 1, TeamRole.GOALKEEPER, 0.50f, 0.14f, 0.50f, 0.15f, "GK #1")
                    ),
                    ball = TacticalBall(0.50f, 0.72f, 0.78f, 0.48f, BallTrajectory.GROUND_PASS),
                    equipment = listOf(
                        TacticalEquipment("cone1", EquipmentType.CONE, 0.30f, 0.50f),
                        TacticalEquipment("cone2", EquipmentType.CONE, 0.70f, 0.50f)
                    )
                ),
                DrillPhase(
                    step = 3,
                    title = "Phase 3: Byline Delivery",
                    instruction = "Winger drops weight and threads to sprinting fullback (#2) hitting the byline.",
                    durationSec = 2.8f,
                    players = listOf(
                        TacticalPlayer("cm8", 8, TeamRole.ATTACK, 0.55f, 0.60f, 0.58f, 0.45f, "CM #8"),
                        TacticalPlayer("w7", 7, TeamRole.ATTACK, 0.78f, 0.48f, 0.72f, 0.38f, "RW #7"),
                        TacticalPlayer("rb2", 2, TeamRole.ATTACK, 0.90f, 0.38f, 0.88f, 0.20f, "RB #2", hasBall = true),
                        TacticalPlayer("st9", 9, TeamRole.ATTACK, 0.50f, 0.28f, 0.45f, 0.18f, "ST #9"),
                        TacticalPlayer("cam10", 10, TeamRole.ATTACK, 0.40f, 0.32f, 0.35f, 0.22f, "AM #10"),
                        TacticalPlayer("def4", 4, TeamRole.DEFENSE, 0.50f, 0.26f, 0.48f, 0.20f, "CB #4"),
                        TacticalPlayer("def5", 5, TeamRole.DEFENSE, 0.78f, 0.45f, 0.82f, 0.28f, "LB #5"),
                        TacticalPlayer("gk1", 1, TeamRole.GOALKEEPER, 0.50f, 0.15f, 0.55f, 0.14f, "GK #1")
                    ),
                    ball = TacticalBall(0.78f, 0.48f, 0.88f, 0.20f, BallTrajectory.GROUND_PASS),
                    equipment = listOf(
                        TacticalEquipment("cone1", EquipmentType.CONE, 0.30f, 0.50f),
                        TacticalEquipment("cone2", EquipmentType.CONE, 0.70f, 0.50f)
                    )
                ),
                DrillPhase(
                    step = 4,
                    title = "Phase 4: Near-Post Dart & Finish",
                    instruction = "Fullback whips driven low cross to #9 cutting across CB for one-touch finish!",
                    durationSec = 2.5f,
                    players = listOf(
                        TacticalPlayer("cm8", 8, TeamRole.ATTACK, 0.58f, 0.45f, 0.58f, 0.35f, "CM #8"),
                        TacticalPlayer("w7", 7, TeamRole.ATTACK, 0.72f, 0.38f, 0.70f, 0.28f, "RW #7"),
                        TacticalPlayer("rb2", 2, TeamRole.ATTACK, 0.88f, 0.20f, 0.85f, 0.18f, "RB #2"),
                        TacticalPlayer("st9", 9, TeamRole.ATTACK, 0.45f, 0.18f, 0.46f, 0.12f, "ST #9", hasBall = true),
                        TacticalPlayer("cam10", 10, TeamRole.ATTACK, 0.35f, 0.22f, 0.32f, 0.16f, "AM #10"),
                        TacticalPlayer("def4", 4, TeamRole.DEFENSE, 0.48f, 0.20f, 0.52f, 0.16f, "CB #4"),
                        TacticalPlayer("def5", 5, TeamRole.DEFENSE, 0.82f, 0.28f, 0.76f, 0.22f, "LB #5"),
                        TacticalPlayer("gk1", 1, TeamRole.GOALKEEPER, 0.55f, 0.14f, 0.48f, 0.10f, "GK #1")
                    ),
                    ball = TacticalBall(0.88f, 0.20f, 0.46f, 0.10f, BallTrajectory.SHOT),
                    equipment = listOf(
                        TacticalEquipment("cone1", EquipmentType.CONE, 0.30f, 0.50f),
                        TacticalEquipment("cone2", EquipmentType.CONE, 0.70f, 0.50f)
                    )
                )
            )
        ),
        SoccerDrill(
            id = "drill_rondo_4v2",
            title = "4v2 Possession Rondo with Splitting Pass",
            category = "Possession & Pressing",
            focusArea = "1-Touch Speed of Play & Defensive Shift",
            durationMinutes = 12,
            pitchView = "HALF",
            description = "4 perimeter attackers circulate the ball with max 2 touches. 2 central defenders press aggressively. Goal is to thread the splitting pass.",
            coachingCues = listOf(
                "Open body shape to receive across field",
                "Weight of pass: crisp on carpet grass",
                "Defenders work in tandem: 1 presses, 1 covers lane",
                "Disguise passes with eye movements"
            ),
            phases = listOf(
                DrillPhase(
                    step = 1,
                    title = "Phase 1: Perimeter Circulation",
                    instruction = "#6 receives and shifts ball to #8; defenders step to close down.",
                    durationSec = 2.5f,
                    players = listOf(
                        TacticalPlayer("p6", 6, TeamRole.ATTACK, 0.50f, 0.70f, 0.50f, 0.70f, "#6", hasBall = true),
                        TacticalPlayer("p8", 8, TeamRole.ATTACK, 0.75f, 0.50f, 0.75f, 0.50f, "#8"),
                        TacticalPlayer("p10", 10, TeamRole.ATTACK, 0.50f, 0.30f, 0.50f, 0.30f, "#10"),
                        TacticalPlayer("p7", 7, TeamRole.ATTACK, 0.25f, 0.50f, 0.25f, 0.50f, "#7"),
                        TacticalPlayer("d1", 1, TeamRole.DEFENSE, 0.45f, 0.55f, 0.52f, 0.62f, "D #1"),
                        TacticalPlayer("d2", 2, TeamRole.DEFENSE, 0.55f, 0.45f, 0.60f, 0.52f, "D #2")
                    ),
                    ball = TacticalBall(0.50f, 0.70f, 0.75f, 0.50f, BallTrajectory.GROUND_PASS),
                    equipment = listOf(
                        TacticalEquipment("c1", EquipmentType.CONE, 0.22f, 0.28f),
                        TacticalEquipment("c2", EquipmentType.CONE, 0.78f, 0.28f),
                        TacticalEquipment("c3", EquipmentType.CONE, 0.78f, 0.72f),
                        TacticalEquipment("c4", EquipmentType.CONE, 0.22f, 0.72f)
                    )
                ),
                DrillPhase(
                    step = 2,
                    title = "Phase 2: Drawing the Press",
                    instruction = "#8 plays 1-touch back to #6 to suck in defenders.",
                    durationSec = 2.5f,
                    players = listOf(
                        TacticalPlayer("p6", 6, TeamRole.ATTACK, 0.50f, 0.70f, 0.50f, 0.70f, "#6"),
                        TacticalPlayer("p8", 8, TeamRole.ATTACK, 0.75f, 0.50f, 0.75f, 0.50f, "#8", hasBall = true),
                        TacticalPlayer("p10", 10, TeamRole.ATTACK, 0.50f, 0.30f, 0.50f, 0.30f, "#10"),
                        TacticalPlayer("p7", 7, TeamRole.ATTACK, 0.25f, 0.50f, 0.25f, 0.50f, "#7"),
                        TacticalPlayer("d1", 1, TeamRole.DEFENSE, 0.52f, 0.62f, 0.65f, 0.55f, "D #1"),
                        TacticalPlayer("d2", 2, TeamRole.DEFENSE, 0.60f, 0.52f, 0.55f, 0.48f, "D #2")
                    ),
                    ball = TacticalBall(0.75f, 0.50f, 0.50f, 0.70f, BallTrajectory.GROUND_PASS),
                    equipment = listOf(
                        TacticalEquipment("c1", EquipmentType.CONE, 0.22f, 0.28f),
                        TacticalEquipment("c2", EquipmentType.CONE, 0.78f, 0.28f),
                        TacticalEquipment("c3", EquipmentType.CONE, 0.78f, 0.72f),
                        TacticalEquipment("c4", EquipmentType.CONE, 0.22f, 0.72f)
                    )
                ),
                DrillPhase(
                    step = 3,
                    title = "Phase 3: The Killer Splitting Pass",
                    instruction = "Defenders overcommit; #6 punches ground pass directly through the gap to #10!",
                    durationSec = 3.0f,
                    players = listOf(
                        TacticalPlayer("p6", 6, TeamRole.ATTACK, 0.50f, 0.70f, 0.50f, 0.70f, "#6", hasBall = true),
                        TacticalPlayer("p8", 8, TeamRole.ATTACK, 0.75f, 0.50f, 0.75f, 0.50f, "#8"),
                        TacticalPlayer("p10", 10, TeamRole.ATTACK, 0.50f, 0.30f, 0.50f, 0.30f, "#10"),
                        TacticalPlayer("p7", 7, TeamRole.ATTACK, 0.25f, 0.50f, 0.25f, 0.50f, "#7"),
                        TacticalPlayer("d1", 1, TeamRole.DEFENSE, 0.65f, 0.55f, 0.60f, 0.58f, "D #1"),
                        TacticalPlayer("d2", 2, TeamRole.DEFENSE, 0.55f, 0.48f, 0.42f, 0.50f, "D #2")
                    ),
                    ball = TacticalBall(0.50f, 0.70f, 0.50f, 0.30f, BallTrajectory.GROUND_PASS),
                    equipment = listOf(
                        TacticalEquipment("c1", EquipmentType.CONE, 0.22f, 0.28f),
                        TacticalEquipment("c2", EquipmentType.CONE, 0.78f, 0.28f),
                        TacticalEquipment("c3", EquipmentType.CONE, 0.78f, 0.72f),
                        TacticalEquipment("c4", EquipmentType.CONE, 0.22f, 0.72f)
                    )
                )
            )
        ),
        SoccerDrill(
            id = "drill_counter_3v2",
            title = "3v2 Counter-Attack Transition Overload",
            category = "Transitions",
            focusArea = "Pace, Decision-Making & Overload Exploitation",
            durationMinutes = 20,
            pitchView = "FULL",
            description = "Rapid transition from midfield turnover. 3 attackers sprint in triangle against 2 retreating center-backs.",
            coachingCues = listOf(
                "Ball carrier drives at defender to freeze them",
                "Wide runners hold width until the final third",
                "Unselfish final pass when defender commits",
                "Follow through for rebounds"
            ),
            phases = listOf(
                DrillPhase(
                    step = 1,
                    title = "Phase 1: Turnover & Central Drive",
                    instruction = "AM (#10) intercepts in midfield and drives forward with pace.",
                    durationSec = 2.8f,
                    players = listOf(
                        TacticalPlayer("a10", 10, TeamRole.ATTACK, 0.50f, 0.60f, 0.50f, 0.45f, "AM #10", hasBall = true),
                        TacticalPlayer("a7", 7, TeamRole.ATTACK, 0.25f, 0.65f, 0.30f, 0.42f, "LW #7"),
                        TacticalPlayer("a9", 9, TeamRole.ATTACK, 0.75f, 0.65f, 0.70f, 0.40f, "RW #9"),
                        TacticalPlayer("d4", 4, TeamRole.DEFENSE, 0.42f, 0.35f, 0.45f, 0.28f, "CB #4"),
                        TacticalPlayer("d5", 5, TeamRole.DEFENSE, 0.58f, 0.35f, 0.55f, 0.28f, "CB #5"),
                        TacticalPlayer("gk1", 1, TeamRole.GOALKEEPER, 0.50f, 0.10f, 0.50f, 0.12f, "GK #1")
                    ),
                    ball = TacticalBall(0.50f, 0.60f, 0.50f, 0.45f, BallTrajectory.DRIBBLE),
                    equipment = listOf(
                        TacticalEquipment("goal_mini_1", EquipmentType.MINI_GOAL, 0.20f, 0.85f),
                        TacticalEquipment("goal_mini_2", EquipmentType.MINI_GOAL, 0.80f, 0.85f)
                    )
                ),
                DrillPhase(
                    step = 2,
                    title = "Phase 2: Defender Commitment & Slip Pass",
                    instruction = "#10 pulls CB #4 toward him and slips pass into space for #7.",
                    durationSec = 2.6f,
                    players = listOf(
                        TacticalPlayer("a10", 10, TeamRole.ATTACK, 0.50f, 0.45f, 0.48f, 0.35f, "AM #10", hasBall = true),
                        TacticalPlayer("a7", 7, TeamRole.ATTACK, 0.30f, 0.42f, 0.32f, 0.22f, "LW #7"),
                        TacticalPlayer("a9", 9, TeamRole.ATTACK, 0.70f, 0.40f, 0.62f, 0.25f, "RW #9"),
                        TacticalPlayer("d4", 4, TeamRole.DEFENSE, 0.45f, 0.28f, 0.46f, 0.32f, "CB #4"),
                        TacticalPlayer("d5", 5, TeamRole.DEFENSE, 0.55f, 0.28f, 0.54f, 0.24f, "CB #5"),
                        TacticalPlayer("gk1", 1, TeamRole.GOALKEEPER, 0.50f, 0.12f, 0.45f, 0.13f, "GK #1")
                    ),
                    ball = TacticalBall(0.50f, 0.45f, 0.32f, 0.22f, BallTrajectory.GROUND_PASS),
                    equipment = listOf(
                        TacticalEquipment("goal_mini_1", EquipmentType.MINI_GOAL, 0.20f, 0.85f),
                        TacticalEquipment("goal_mini_2", EquipmentType.MINI_GOAL, 0.80f, 0.85f)
                    )
                ),
                DrillPhase(
                    step = 3,
                    title = "Phase 3: Squared Pass & Far Post Tap-in",
                    instruction = "#7 draws the goalkeeper and squares across face of goal for #9 to tap into empty net!",
                    durationSec = 2.5f,
                    players = listOf(
                        TacticalPlayer("a10", 10, TeamRole.ATTACK, 0.48f, 0.35f, 0.48f, 0.25f, "AM #10"),
                        TacticalPlayer("a7", 7, TeamRole.ATTACK, 0.32f, 0.22f, 0.35f, 0.16f, "LW #7", hasBall = true),
                        TacticalPlayer("a9", 9, TeamRole.ATTACK, 0.62f, 0.25f, 0.55f, 0.14f, "RW #9"),
                        TacticalPlayer("d4", 4, TeamRole.DEFENSE, 0.46f, 0.32f, 0.42f, 0.22f, "CB #4"),
                        TacticalPlayer("d5", 5, TeamRole.DEFENSE, 0.54f, 0.24f, 0.50f, 0.18f, "CB #5"),
                        TacticalPlayer("gk1", 1, TeamRole.GOALKEEPER, 0.45f, 0.13f, 0.38f, 0.14f, "GK #1")
                    ),
                    ball = TacticalBall(0.32f, 0.22f, 0.55f, 0.14f, BallTrajectory.GROUND_PASS),
                    equipment = listOf(
                        TacticalEquipment("goal_mini_1", EquipmentType.MINI_GOAL, 0.20f, 0.85f),
                        TacticalEquipment("goal_mini_2", EquipmentType.MINI_GOAL, 0.80f, 0.85f)
                    )
                )
            )
        )
    )
}
