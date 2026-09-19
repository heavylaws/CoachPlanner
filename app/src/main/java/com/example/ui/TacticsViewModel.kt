package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.VoiceNoteManager
import com.example.data.GeminiTacticsService
import com.example.data.SampleTacticsData
import com.example.data.TacticsRepository
import com.example.db.AppDatabase
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TacticsUiState(
    val drills: List<SoccerDrill> = emptyList(),
    val activeDrill: SoccerDrill? = null,
    val currentPhaseIndex: Int = 0,
    val animationFraction: Float = 0f,
    val isPlaying: Boolean = true,
    val playbackSpeed: Float = 1.0f,
    val currentUser: UserProfile = SampleTacticsData.defaultUsers.first(),
    val allUsers: List<UserProfile> = SampleTacticsData.defaultUsers,
    val isGenerating: Boolean = false,
    val highlightedPlayerNumber: Int? = null,
    val statusMessage: String? = null,
    // Annotation & Telestrator System
    val annotations: List<TacticalAnnotation> = emptyList(),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val activeTool: AnnotationTool = AnnotationTool.MOVE,
    val activeColor: Long = 0xFFFFEE58, // Neon Tactical Yellow
    val activeStrokeWidth: Float = 5f,
    val isDashed: Boolean = false,
    val isFullscreenBoardroom: Boolean = false,
    val laserPoints: List<LaserPoint> = emptyList(),
    val quickNotes: List<QuickTacticalNote> = emptyList()
)

class TacticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TacticsRepository(AppDatabase.getInstance(application))
    private val geminiService = GeminiTacticsService()
    val voiceNoteManager = VoiceNoteManager(application)

    private val _uiState = MutableStateFlow(TacticsUiState())
    val uiState: StateFlow<TacticsUiState> = _uiState.asStateFlow()

    private var animationJob: Job? = null

    // Undo / Redo history stacks
    private val undoStack = mutableListOf<List<TacticalAnnotation>>()
    private val redoStack = mutableListOf<List<TacticalAnnotation>>()

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
        }

        viewModelScope.launch {
            repository.allDrills.collect { drillList ->
                _uiState.update { current ->
                    val active = if (current.activeDrill != null) {
                        drillList.find { it.id == current.activeDrill.id } ?: drillList.firstOrNull()
                    } else {
                        drillList.firstOrNull()
                    }
                    current.copy(drills = drillList, activeDrill = active)
                }
            }
        }

        viewModelScope.launch {
            repository.allUsers.collect { userList ->
                _uiState.update { current ->
                    val activeUser = userList.find { it.id == current.currentUser.id } ?: userList.firstOrNull() ?: current.currentUser
                    current.copy(allUsers = userList, currentUser = activeUser)
                }
            }
        }

        startAnimationLoop()
    }

    private fun startAnimationLoop() {
        animationJob?.cancel()
        animationJob = viewModelScope.launch {
            val tickIntervalMs = 25L // ~40 FPS smooth interpolation
            while (isActive) {
                val state = _uiState.value
                val drill = state.activeDrill
                if (state.isPlaying && drill != null && drill.phases.isNotEmpty()) {
                    val phases = drill.phases
                    val currPhase = phases[state.currentPhaseIndex]
                    val durationMs = (currPhase.durationSec * 1000f / state.playbackSpeed).coerceAtLeast(400f)
                    val stepFraction = tickIntervalMs.toFloat() / durationMs

                    var newFraction = state.animationFraction + stepFraction
                    var newIndex = state.currentPhaseIndex

                    if (newFraction >= 1f) {
                        newFraction = 0f
                        newIndex = (state.currentPhaseIndex + 1) % phases.size
                    }

                    _uiState.update {
                        it.copy(currentPhaseIndex = newIndex, animationFraction = newFraction)
                    }
                }

                // Decay laser points for smooth glowing comet trail
                val now = System.currentTimeMillis()
                if (state.laserPoints.isNotEmpty()) {
                    val freshPoints = state.laserPoints.filter { now - it.timestamp < 900 }
                    if (freshPoints.size != state.laserPoints.size) {
                        _uiState.update { it.copy(laserPoints = freshPoints) }
                    }
                }

                delay(tickIntervalMs)
            }
        }
    }

    fun togglePlayPause() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun nextPhase() {
        val drill = _uiState.value.activeDrill ?: return
        if (drill.phases.isEmpty()) return
        val nextIdx = (_uiState.value.currentPhaseIndex + 1).coerceAtMost(drill.phases.size - 1)
        _uiState.update { it.copy(currentPhaseIndex = nextIdx, animationFraction = 0f) }
    }

    fun previousPhase() {
        val prevIdx = (_uiState.value.currentPhaseIndex - 1).coerceAtLeast(0)
        _uiState.update { it.copy(currentPhaseIndex = prevIdx, animationFraction = 0f) }
    }

    fun restartDrill() {
        _uiState.update { it.copy(currentPhaseIndex = 0, animationFraction = 0f) }
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    fun togglePitchView() {
        val drill = _uiState.value.activeDrill ?: return
        val newView = if (drill.pitchView == "HALF") "FULL" else "HALF"
        val updated = drill.copy(pitchView = newView)
        _uiState.update { it.copy(activeDrill = updated) }
        viewModelScope.launch {
            repository.saveDrill(updated)
        }
    }

    fun selectDrill(drill: SoccerDrill) {
        _uiState.update {
            it.copy(
                activeDrill = drill,
                currentPhaseIndex = 0,
                animationFraction = 0f,
                highlightedPlayerNumber = null
            )
        }
    }

    fun setHighlightedPlayer(playerNumber: Int?) {
        _uiState.update { it.copy(highlightedPlayerNumber = playerNumber) }
    }

    fun selectUser(user: UserProfile) {
        _uiState.update { current ->
            // If user is a player, auto-highlight their jersey number
            val pNum = if (user.role == UserRole.PLAYER) {
                // e.g. "Marcus Rashford (#9)"
                val regex = Regex("""#(\d+)""")
                regex.find(user.name)?.groupValues?.get(1)?.toIntOrNull() ?: 9
            } else null

            current.copy(
                currentUser = user,
                highlightedPlayerNumber = pNum,
                statusMessage = "Switched profile: ${user.name} (${user.role.displayName})"
            )
        }
    }

    fun createNewUser(name: String, email: String, role: UserRole, team: String) {
        viewModelScope.launch {
            val newUser = UserProfile(
                id = "user_${System.currentTimeMillis()}",
                name = name,
                email = email,
                role = role,
                team = team,
                avatarColor = when (role) {
                    UserRole.HEAD_COACH -> 0xFF00E5FF
                    UserRole.ASSISTANT_COACH -> 0xFF00E676
                    UserRole.PLAYER -> 0xFFFFD600
                }
            )
            repository.saveUser(newUser)
            selectUser(newUser)
        }
    }

    /**
     * Accept changes as fast as possible:
     * Applies instantaneous tactical changes (defender, tempo, overlap, goals, etc.)
     */
    fun applyFastChange(mutation: String) {
        val currentDrill = _uiState.value.activeDrill ?: return
        val updatedDrill = geminiService.applyFastChange(currentDrill, mutation)

        _uiState.update {
            it.copy(
                activeDrill = updatedDrill,
                currentPhaseIndex = 0,
                animationFraction = 0f,
                statusMessage = "Fast adjustment applied: $mutation"
            )
        }

        viewModelScope.launch {
            repository.saveDrill(updatedDrill)
        }
    }

    /**
     * AI generation from coach's voice note or text prompt.
     */
    fun generateDrillFromPrompt(prompt: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, statusMessage = "AI analyzing prompt...") }

            val result = geminiService.generateDrillFromPrompt(prompt, _uiState.value.currentUser.role)
            result.onSuccess { generatedDrill ->
                repository.saveDrill(generatedDrill)
                _uiState.update {
                    it.copy(
                        activeDrill = generatedDrill,
                        currentPhaseIndex = 0,
                        animationFraction = 0f,
                        isGenerating = false,
                        isPlaying = true,
                        statusMessage = "Animation generated: ${generatedDrill.title}"
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(isGenerating = false, statusMessage = "Generation error, fallback applied.")
                }
            }
        }
    }

    fun movePlayer(playerId: String, newX: Float, newY: Float) {
        val drill = _uiState.value.activeDrill ?: return
        val phaseIdx = _uiState.value.currentPhaseIndex
        if (phaseIdx !in drill.phases.indices) return

        val phase = drill.phases[phaseIdx]
        val updatedPlayers = phase.players.map { pl ->
            if (pl.id == playerId) pl.copy(x = newX, y = newY, targetX = newX, targetY = newY) else pl
        }
        val updatedPhase = phase.copy(players = updatedPlayers)
        val updatedPhases = drill.phases.toMutableList().apply { set(phaseIdx, updatedPhase) }
        val updatedDrill = drill.copy(phases = updatedPhases)

        _uiState.update { it.copy(activeDrill = updatedDrill) }
    }

    fun deleteCurrentDrill() {
        val drill = _uiState.value.activeDrill ?: return
        if (!_uiState.value.currentUser.role.canDeleteDrills) return

        viewModelScope.launch {
            repository.deleteDrill(drill.id)
            _uiState.update { it.copy(statusMessage = "Drill deleted") }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    // --- Telestrator & Annotation Controls ---

    fun setTool(tool: AnnotationTool) {
        _uiState.update { it.copy(activeTool = tool) }
    }

    fun setColor(color: Long) {
        _uiState.update { it.copy(activeColor = color) }
    }

    fun setStrokeWidth(width: Float) {
        _uiState.update { it.copy(activeStrokeWidth = width) }
    }

    fun toggleDashed() {
        _uiState.update { it.copy(isDashed = !it.isDashed) }
    }

    fun toggleFullscreenBoardroom() {
        _uiState.update { it.copy(isFullscreenBoardroom = !it.isFullscreenBoardroom) }
    }

    fun addAnnotation(annotation: TacticalAnnotation) {
        undoStack.add(_uiState.value.annotations)
        redoStack.clear()
        _uiState.update { current ->
            current.copy(
                annotations = current.annotations + annotation,
                canUndo = true,
                canRedo = false
            )
        }
    }

    fun undoAnnotation() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(_uiState.value.annotations)
            _uiState.update { current ->
                current.copy(
                    annotations = previous,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = true
                )
            }
        }
    }

    fun redoAnnotation() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(_uiState.value.annotations)
            _uiState.update { current ->
                current.copy(
                    annotations = next,
                    canUndo = true,
                    canRedo = redoStack.isNotEmpty()
                )
            }
        }
    }

    fun clearAnnotations() {
        if (_uiState.value.annotations.isNotEmpty()) {
            undoStack.add(_uiState.value.annotations)
            redoStack.clear()
            _uiState.update {
                it.copy(
                    annotations = emptyList(),
                    canUndo = true,
                    canRedo = false,
                    statusMessage = "Annotations cleared"
                )
            }
        }
    }

    fun eraseAnnotationNear(normX: Float, normY: Float) {
        val currentAnnotations = _uiState.value.annotations
        val toRemove = currentAnnotations.find { ann ->
            ann.points.any { p ->
                val dx = p.x - normX
                val dy = p.y - normY
                dx * dx + dy * dy < 0.005f // ~0.07 distance
            }
        }
        if (toRemove != null) {
            undoStack.add(currentAnnotations)
            redoStack.clear()
            _uiState.update {
                it.copy(
                    annotations = it.annotations - toRemove,
                    canUndo = true,
                    canRedo = false
                )
            }
        }
    }

    fun addLaserPoint(x: Float, y: Float) {
        val now = System.currentTimeMillis()
        val newPoint = LaserPoint(x, y, now)
        _uiState.update {
            it.copy(laserPoints = (it.laserPoints.filter { pt -> now - pt.timestamp < 900 } + newPoint))
        }
    }

    fun addQuickNote(text: String, x: Float = 0.5f, y: Float = 0.5f) {
        val newNote = QuickTacticalNote(
            id = "note_${System.currentTimeMillis()}",
            phaseIndex = _uiState.value.currentPhaseIndex,
            x = x,
            y = y,
            text = text,
            authorRole = _uiState.value.currentUser.role
        )
        _uiState.update {
            it.copy(
                quickNotes = it.quickNotes + newNote,
                statusMessage = "Tactical note placed on pitch"
            )
        }
    }

    fun deleteQuickNote(noteId: String) {
        _uiState.update { it.copy(quickNotes = it.quickNotes.filter { note -> note.id != noteId }) }
    }

    override fun onCleared() {
        super.onCleared()
        animationJob?.cancel()
        voiceNoteManager.destroy()
    }
}
