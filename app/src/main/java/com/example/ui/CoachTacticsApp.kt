package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AnnotationTool
import com.example.model.UserRole
import com.example.ui.components.*
import com.example.ui.theme.AttackTeamCyan
import com.example.ui.theme.DefenseTeamOrange
import com.example.ui.theme.GoalkeeperGold
import com.example.ui.theme.PitchDarkBg
import com.example.ui.theme.PitchSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachTacticsApp(
    viewModel: TacticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showVoiceSheet by remember { mutableStateOf(false) }
    var showRoleDialog by remember { mutableStateOf(false) }
    var showDetailsSheet by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var pendingNoteCoord by remember { mutableStateOf<Pair<Float, Float>?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Display status messages via snackbar
    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
            viewModel.clearStatusMessage()
        }
    }

    val activeDrill = uiState.activeDrill

    // ==========================================
    // 1. INTERACTIVE BOARDROOM FULLSCREEN MODE
    // (Optimized for Interactive Panels & Touch Smartboards)
    // ==========================================
    if (uiState.isFullscreenBoardroom && activeDrill != null && activeDrill.phases.isNotEmpty()) {
        val currentPhase = activeDrill.phases[uiState.currentPhaseIndex]
        val nextPhase = activeDrill.phases[(uiState.currentPhaseIndex + 1) % activeDrill.phases.size]

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PitchDarkBg)
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.key) {
                            Key.Spacebar -> {
                                viewModel.togglePlayPause()
                                true
                            }
                            Key.F, Key.Escape -> {
                                viewModel.toggleFullscreenBoardroom()
                                true
                            }
                            Key.C -> {
                                viewModel.clearAnnotations()
                                true
                            }
                            Key.Z -> {
                                viewModel.undoAnnotation()
                                true
                            }
                            Key.DirectionRight -> {
                                viewModel.nextPhase()
                                true
                            }
                            Key.DirectionLeft -> {
                                viewModel.previousPhase()
                                true
                            }
                            else -> false
                        }
                    } else false
                }
        ) {
            // Fullscreen Pitch Canvas
            TacticalPitchView(
                drill = activeDrill,
                currentPhase = currentPhase,
                nextPhase = nextPhase,
                animationFraction = uiState.animationFraction,
                highlightedPlayerNumber = uiState.highlightedPlayerNumber,
                isEditable = uiState.currentUser.role.canCreateDrills,
                annotations = uiState.annotations,
                laserPoints = uiState.laserPoints,
                quickNotes = uiState.quickNotes,
                activeTool = uiState.activeTool,
                activeColor = uiState.activeColor,
                activeStrokeWidth = uiState.activeStrokeWidth,
                isDashed = uiState.isDashed,
                isFullscreen = true,
                onPlayerMoved = { id, nx, ny -> viewModel.movePlayer(id, nx, ny) },
                onPlayerSelected = { pl -> viewModel.setHighlightedPlayer(pl.number) },
                onAddAnnotation = { ann -> viewModel.addAnnotation(ann) },
                onEraseNear = { nx, ny -> viewModel.eraseAnnotationNear(nx, ny) },
                onLaserMoved = { nx, ny -> viewModel.addLaserPoint(nx, ny) },
                onPitchTapForNote = { nx, ny ->
                    pendingNoteCoord = Pair(nx, ny)
                    showNoteDialog = true
                },
                modifier = Modifier.fillMaxSize()
            )

            // Top Floating Controls: Title & Exit Boardroom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AttackTeamCyan.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tv,
                            contentDescription = null,
                            tint = AttackTeamCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Boardroom Mode • ${activeDrill.title} (Phase ${uiState.currentPhaseIndex + 1}/${activeDrill.phases.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { viewModel.toggleFullscreenBoardroom() },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.75f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.FullscreenExit, contentDescription = "Exit Boardroom")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Exit", fontWeight = FontWeight.Bold)
                }
            }

            // Bottom Floating Bar: Docked Telestrator Toolbar & Compact Playback
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Floating Playback controls
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PitchSurfaceVariant),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.previousPhase() }) {
                            Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous Phase", tint = Color.White)
                        }
                        FilledIconButton(
                            onClick = { viewModel.togglePlayPause() },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = AttackTeamCyan)
                        ) {
                            Icon(
                                imageVector = if (uiState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                                tint = Color.Black
                            )
                        }
                        IconButton(onClick = { viewModel.nextPhase() }) {
                            Icon(Icons.Rounded.SkipNext, contentDescription = "Next Phase", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${uiState.playbackSpeed}x",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AttackTeamCyan
                        )
                    }
                }

                // Docked Telestrator Toolbar
                TelestratorToolbar(
                    activeTool = uiState.activeTool,
                    activeColor = uiState.activeColor,
                    activeStrokeWidth = uiState.activeStrokeWidth,
                    isDashed = uiState.isDashed,
                    canUndo = uiState.canUndo,
                    canRedo = uiState.canRedo,
                    isFullscreen = true,
                    onToolSelected = { viewModel.setTool(it) },
                    onColorSelected = { viewModel.setColor(it) },
                    onStrokeWidthChanged = { viewModel.setStrokeWidth(it) },
                    onToggleDashed = { viewModel.toggleDashed() },
                    onUndo = { viewModel.undoAnnotation() },
                    onRedo = { viewModel.redoAnnotation() },
                    onClearAll = { viewModel.clearAnnotations() },
                    onToggleFullscreen = { viewModel.toggleFullscreenBoardroom() },
                    onAddQuickNote = {
                        pendingNoteCoord = Pair(0.5f, 0.5f)
                        showNoteDialog = true
                    }
                )
            }
        }

        // Quick Note Dialog when tapping pitch in Boardroom mode
        if (showNoteDialog) {
            val coord = pendingNoteCoord ?: Pair(0.5f, 0.5f)
            QuickNoteDialog(
                onConfirm = { text ->
                    viewModel.addQuickNote(text, coord.first, coord.second)
                    showNoteDialog = false
                    pendingNoteCoord = null
                },
                onDismiss = {
                    showNoteDialog = false
                    pendingNoteCoord = null
                }
            )
        }
        return
    }

    // ==========================================
    // 2. STANDARD APP MODE (Adaptive Mobile / Large Display Layout)
    // ==========================================
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = PitchDarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = AttackTeamCyan.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.SportsSoccer,
                                    contentDescription = null,
                                    tint = AttackTeamCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CoachTactics",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${uiState.currentUser.team} • ${uiState.currentUser.role.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Interactive Boardroom presentation launcher
                    FilledTonalIconButton(
                        onClick = { viewModel.toggleFullscreenBoardroom() },
                        modifier = Modifier.testTag("interactive_boardroom_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tv,
                            contentDescription = "Interactive Boardroom Mode",
                            tint = AttackTeamCyan
                        )
                    }

                    // Profile / Role Switcher button
                    Surface(
                        onClick = { showRoleDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        color = PitchSurfaceVariant,
                        modifier = Modifier
                            .testTag("role_switcher_button")
                            .height(34.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(uiState.currentUser.avatarColor))
                            ) {
                                Text(
                                    text = uiState.currentUser.name.take(1).uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.currentUser.role.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (uiState.currentUser.role) {
                                    UserRole.HEAD_COACH -> AttackTeamCyan
                                    UserRole.ASSISTANT_COACH -> DefenseTeamOrange
                                    UserRole.PLAYER -> GoalkeeperGold
                                }
                            )
                        }
                    }

                    // Drill info sheet toggle
                    IconButton(
                        onClick = { showDetailsSheet = true },
                        modifier = Modifier.testTag("drill_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "Drill Info & Coaching Points"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (uiState.currentUser.role.canCreateDrills) {
                ExtendedFloatingActionButton(
                    onClick = { showVoiceSheet = true },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = "Voice Note or Prompt"
                        )
                    },
                    text = {
                        Text(
                            text = "Voice Note / Prompt",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    containerColor = AttackTeamCyan,
                    contentColor = Color.Black,
                    modifier = Modifier.testTag("open_voice_sheet_fab")
                )
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.key) {
                            Key.Spacebar -> {
                                viewModel.togglePlayPause()
                                true
                            }
                            Key.F -> {
                                viewModel.toggleFullscreenBoardroom()
                                true
                            }
                            Key.C -> {
                                viewModel.clearAnnotations()
                                true
                            }
                            Key.Z -> {
                                viewModel.undoAnnotation()
                                true
                            }
                            Key.DirectionRight -> {
                                viewModel.nextPhase()
                                true
                            }
                            Key.DirectionLeft -> {
                                viewModel.previousPhase()
                                true
                            }
                            else -> false
                        }
                    } else false
                }
        ) {
            val isLargeScreen = maxWidth >= 780.dp

            if (isLargeScreen) {
                // ==========================================
                // 2A. WIDE SCREEN / WEBSITE / PANEL CANONICAL 2-PANE LAYOUT
                // ==========================================
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Main Tactical Canvas Pane
                    Column(
                        modifier = Modifier
                            .weight(1.35f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Telestrator Toolbar
                        TelestratorToolbar(
                            activeTool = uiState.activeTool,
                            activeColor = uiState.activeColor,
                            activeStrokeWidth = uiState.activeStrokeWidth,
                            isDashed = uiState.isDashed,
                            canUndo = uiState.canUndo,
                            canRedo = uiState.canRedo,
                            isFullscreen = false,
                            onToolSelected = { viewModel.setTool(it) },
                            onColorSelected = { viewModel.setColor(it) },
                            onStrokeWidthChanged = { viewModel.setStrokeWidth(it) },
                            onToggleDashed = { viewModel.toggleDashed() },
                            onUndo = { viewModel.undoAnnotation() },
                            onRedo = { viewModel.redoAnnotation() },
                            onClearAll = { viewModel.clearAnnotations() },
                            onToggleFullscreen = { viewModel.toggleFullscreenBoardroom() },
                            onAddQuickNote = {
                                pendingNoteCoord = Pair(0.5f, 0.5f)
                                showNoteDialog = true
                            },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (activeDrill != null && activeDrill.phases.isNotEmpty()) {
                            val currentPhase = activeDrill.phases[uiState.currentPhaseIndex]
                            val nextPhase = activeDrill.phases[(uiState.currentPhaseIndex + 1) % activeDrill.phases.size]

                            TacticalPitchView(
                                drill = activeDrill,
                                currentPhase = currentPhase,
                                nextPhase = nextPhase,
                                animationFraction = uiState.animationFraction,
                                highlightedPlayerNumber = uiState.highlightedPlayerNumber,
                                isEditable = uiState.currentUser.role.canCreateDrills,
                                annotations = uiState.annotations,
                                laserPoints = uiState.laserPoints,
                                quickNotes = uiState.quickNotes,
                                activeTool = uiState.activeTool,
                                activeColor = uiState.activeColor,
                                activeStrokeWidth = uiState.activeStrokeWidth,
                                isDashed = uiState.isDashed,
                                isFullscreen = false,
                                onPlayerMoved = { id, nx, ny -> viewModel.movePlayer(id, nx, ny) },
                                onPlayerSelected = { pl -> viewModel.setHighlightedPlayer(pl.number) },
                                onAddAnnotation = { ann -> viewModel.addAnnotation(ann) },
                                onEraseNear = { nx, ny -> viewModel.eraseAnnotationNear(nx, ny) },
                                onLaserMoved = { nx, ny -> viewModel.addLaserPoint(nx, ny) },
                                onPitchTapForNote = { nx, ny ->
                                    pendingNoteCoord = Pair(nx, ny)
                                    showNoteDialog = true
                                },
                                modifier = Modifier.testTag("tactical_pitch_canvas")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            TimelinePlayerControls(
                                currentPhase = currentPhase,
                                phaseIndex = uiState.currentPhaseIndex,
                                totalPhases = activeDrill.phases.size,
                                isPlaying = uiState.isPlaying,
                                progressFraction = uiState.animationFraction,
                                playbackSpeed = uiState.playbackSpeed,
                                isHalfPitch = activeDrill.pitchView == "HALF",
                                onTogglePlay = { viewModel.togglePlayPause() },
                                onPreviousPhase = { viewModel.previousPhase() },
                                onNextPhase = { viewModel.nextPhase() },
                                onRestart = { viewModel.restartDrill() },
                                onSpeedChanged = { viewModel.setPlaybackSpeed(it) },
                                onTogglePitchView = { viewModel.togglePitchView() },
                                onScrubPhase = { /* scrub */ }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            CoachingExplanationOverlay(
                                drill = activeDrill,
                                currentPhase = currentPhase,
                                phaseIndex = uiState.currentPhaseIndex,
                                totalPhases = activeDrill.phases.size,
                                currentUserRole = uiState.currentUser.role,
                                onOpenVoiceExplanation = {
                                    showVoiceSheet = true
                                },
                                onAddQuickNote = {
                                    pendingNoteCoord = Pair(0.5f, 0.5f)
                                    showNoteDialog = true
                                }
                            )
                        }
                    }

                    // Right Secondary Pane: Drills Library, Quick Modifications, & Interactive Hotkeys
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Tactical Playbook",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AttackTeamCyan,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Drill Cards
                        uiState.drills.forEach { drill ->
                            val isSelected = drill.id == activeDrill?.id
                            Surface(
                                onClick = { viewModel.selectDrill(drill) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) AttackTeamCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, AttackTeamCyan) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (drill.pitchView == "HALF") Icons.Rounded.CropPortrait else Icons.Rounded.ZoomOutMap,
                                        contentDescription = null,
                                        tint = if (isSelected) AttackTeamCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = drill.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isSelected) AttackTeamCyan else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${drill.category} • ${drill.phases.size} phases",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Fast Changes Bar
                        FastChangesBar(
                            canEdit = uiState.currentUser.role.canCreateDrills,
                            onApplyChange = { mutation -> viewModel.applyFastChange(mutation) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Interactive Panels & Web Shortcuts Card
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PitchSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Keyboard,
                                        contentDescription = null,
                                        tint = AttackTeamCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Interactive Panel & Web Hotkeys",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "• Space: Play / Pause animation\n• F: Toggle Boardroom Fullscreen\n• C: Clear Telestrator Drawings\n• Z: Undo Telestrator stroke\n• ← / →: Navigate Drill Phases",
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                // ==========================================
                // 2B. MOBILE PORTRAIT LAYOUT
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // 1. Drills Selector Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        uiState.drills.forEach { drill ->
                            val isSelected = drill.id == activeDrill?.id
                            Surface(
                                onClick = { viewModel.selectDrill(drill) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) AttackTeamCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, AttackTeamCyan) else null,
                                modifier = Modifier.testTag("drill_selector_${drill.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (drill.pitchView == "HALF") Icons.Rounded.CropPortrait else Icons.Rounded.ZoomOutMap,
                                        contentDescription = null,
                                        tint = if (isSelected) AttackTeamCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = drill.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) AttackTeamCyan else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Player Mode Banner
                    if (uiState.currentUser.role == UserRole.PLAYER) {
                        Surface(
                            color = GoalkeeperGold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoalkeeperGold.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = GoalkeeperGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Player Mode: Tracking your run on the pitch. Follow the dashed route!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GoalkeeperGold
                                )
                            }
                        }
                    }

                    // 2. Telestrator Toolbar for on-the-fly annotations
                    TelestratorToolbar(
                        activeTool = uiState.activeTool,
                        activeColor = uiState.activeColor,
                        activeStrokeWidth = uiState.activeStrokeWidth,
                        isDashed = uiState.isDashed,
                        canUndo = uiState.canUndo,
                        canRedo = uiState.canRedo,
                        isFullscreen = false,
                        onToolSelected = { viewModel.setTool(it) },
                        onColorSelected = { viewModel.setColor(it) },
                        onStrokeWidthChanged = { viewModel.setStrokeWidth(it) },
                        onToggleDashed = { viewModel.toggleDashed() },
                        onUndo = { viewModel.undoAnnotation() },
                        onRedo = { viewModel.redoAnnotation() },
                        onClearAll = { viewModel.clearAnnotations() },
                        onToggleFullscreen = { viewModel.toggleFullscreenBoardroom() },
                        onAddQuickNote = {
                            pendingNoteCoord = Pair(0.5f, 0.5f)
                            showNoteDialog = true
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // 3. Animated Tactical Pitch View
                    if (activeDrill != null && activeDrill.phases.isNotEmpty()) {
                        val currentPhase = activeDrill.phases[uiState.currentPhaseIndex]
                        val nextPhase = activeDrill.phases[(uiState.currentPhaseIndex + 1) % activeDrill.phases.size]

                        TacticalPitchView(
                            drill = activeDrill,
                            currentPhase = currentPhase,
                            nextPhase = nextPhase,
                            animationFraction = uiState.animationFraction,
                            highlightedPlayerNumber = uiState.highlightedPlayerNumber,
                            isEditable = uiState.currentUser.role.canCreateDrills,
                            annotations = uiState.annotations,
                            laserPoints = uiState.laserPoints,
                            quickNotes = uiState.quickNotes,
                            activeTool = uiState.activeTool,
                            activeColor = uiState.activeColor,
                            activeStrokeWidth = uiState.activeStrokeWidth,
                            isDashed = uiState.isDashed,
                            isFullscreen = false,
                            onPlayerMoved = { id, nx, ny -> viewModel.movePlayer(id, nx, ny) },
                            onPlayerSelected = { pl -> viewModel.setHighlightedPlayer(pl.number) },
                            onAddAnnotation = { ann -> viewModel.addAnnotation(ann) },
                            onEraseNear = { nx, ny -> viewModel.eraseAnnotationNear(nx, ny) },
                            onLaserMoved = { nx, ny -> viewModel.addLaserPoint(nx, ny) },
                            onPitchTapForNote = { nx, ny ->
                                pendingNoteCoord = Pair(nx, ny)
                                showNoteDialog = true
                            },
                            modifier = Modifier.testTag("tactical_pitch_canvas")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 4. Timeline & Playback Controls
                        TimelinePlayerControls(
                            currentPhase = currentPhase,
                            phaseIndex = uiState.currentPhaseIndex,
                            totalPhases = activeDrill.phases.size,
                            isPlaying = uiState.isPlaying,
                            progressFraction = uiState.animationFraction,
                            playbackSpeed = uiState.playbackSpeed,
                            isHalfPitch = activeDrill.pitchView == "HALF",
                            onTogglePlay = { viewModel.togglePlayPause() },
                            onPreviousPhase = { viewModel.previousPhase() },
                            onNextPhase = { viewModel.nextPhase() },
                            onRestart = { viewModel.restartDrill() },
                            onSpeedChanged = { viewModel.setPlaybackSpeed(it) },
                            onTogglePitchView = { viewModel.togglePitchView() },
                            onScrubPhase = { /* future phase seek */ }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 5. On-The-Fly Coaching Explanation Overlay
                        CoachingExplanationOverlay(
                            drill = activeDrill,
                            currentPhase = currentPhase,
                            phaseIndex = uiState.currentPhaseIndex,
                            totalPhases = activeDrill.phases.size,
                            currentUserRole = uiState.currentUser.role,
                            onOpenVoiceExplanation = {
                                showVoiceSheet = true
                            },
                            onAddQuickNote = {
                                pendingNoteCoord = Pair(0.5f, 0.5f)
                                showNoteDialog = true
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 6. Fast Changes Toolbar ("Accept changes as fast as possible")
                        FastChangesBar(
                            canEdit = uiState.currentUser.role.canCreateDrills,
                            onApplyChange = { mutation ->
                                viewModel.applyFastChange(mutation)
                            }
                        )
                    } else {
                        // Empty State
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Rounded.SportsSoccer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No tactical drill loaded", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(72.dp)) // Padding for FAB
                }
            }
        }
    }

    // Voice Note & Prompt Modal
    if (showVoiceSheet) {
        VoicePromptSheet(
            voiceNoteManager = viewModel.voiceNoteManager,
            isGenerating = uiState.isGenerating,
            onGenerateFromPrompt = { prompt ->
                viewModel.generateDrillFromPrompt(prompt)
            },
            onDismiss = { showVoiceSheet = false }
        )
    }

    // Role Management Dialog
    if (showRoleDialog) {
        RoleManagementDialog(
            currentUser = uiState.currentUser,
            allUsers = uiState.allUsers,
            onSelectUser = { user -> viewModel.selectUser(user) },
            onCreateNewUser = { name, email, role, team ->
                viewModel.createNewUser(name, email, role, team)
            },
            onDismiss = { showRoleDialog = false }
        )
    }

    // Drill Details & Player Walkthrough Sheet
    if (showDetailsSheet && activeDrill != null) {
        DrillDetailsSheet(
            drill = activeDrill,
            currentUserRole = uiState.currentUser.role,
            highlightedPlayerNumber = uiState.highlightedPlayerNumber,
            onHighlightPlayer = { pNum -> viewModel.setHighlightedPlayer(pNum) },
            onDeleteDrill = { viewModel.deleteCurrentDrill() },
            onDismiss = { showDetailsSheet = false }
        )
    }

    // Quick Tactical Note Dialog
    if (showNoteDialog) {
        val coord = pendingNoteCoord ?: Pair(0.5f, 0.5f)
        QuickNoteDialog(
            onConfirm = { text ->
                viewModel.addQuickNote(text, coord.first, coord.second)
                showNoteDialog = false
                pendingNoteCoord = null
            },
            onDismiss = {
                showNoteDialog = false
                pendingNoteCoord = null
            }
        )
    }
}
