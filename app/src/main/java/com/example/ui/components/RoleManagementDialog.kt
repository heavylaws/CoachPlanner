package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.UserProfile
import com.example.model.UserRole
import com.example.ui.theme.AttackTeamCyan
import com.example.ui.theme.DefenseTeamOrange
import com.example.ui.theme.GoalkeeperGold
import com.example.ui.theme.PitchSurfaceVariant

@Composable
fun RoleManagementDialog(
    currentUser: UserProfile,
    allUsers: List<UserProfile>,
    onSelectUser: (UserProfile) -> Unit,
    onCreateNewUser: (name: String, email: String, role: UserRole, team: String) -> Unit,
    onDismiss: () -> Unit
) {
    var showCreateUser by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var newTeam by remember { mutableStateOf(currentUser.team) }
    var selectedRole by remember { mutableStateOf(UserRole.ASSISTANT_COACH) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("role_management_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AdminPanelSettings,
                            contentDescription = null,
                            tint = AttackTeamCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Role Management & Sign In",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Control access roles: Head Coach, Assistant Coach, and Player view.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                if (showCreateUser) {
                    // Create New User Form
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Full Name / Jersey Name") },
                            placeholder = { Text("e.g. Coach Thomas Tuchel") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newEmail,
                            onValueChange = { newEmail = it },
                            label = { Text("Email") },
                            placeholder = { Text("coach@club.com") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newTeam,
                            onValueChange = { newTeam = it },
                            label = { Text("Team Squad") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Select Role:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            UserRole.entries.forEach { role ->
                                FilterChip(
                                    selected = selectedRole == role,
                                    onClick = { selectedRole = role },
                                    label = { Text(role.displayName, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showCreateUser = false }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newName.isNotBlank()) {
                                        onCreateNewUser(newName, newEmail, selectedRole, newTeam)
                                        showCreateUser = false
                                    }
                                },
                                enabled = newName.isNotBlank()
                            ) {
                                Text("Add User")
                            }
                        }
                    }
                } else {
                    // User List / Sign In Switcher
                    Text(
                        text = "Active Profiles (Tap to Switch):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allUsers) { user ->
                            val isCurrent = user.id == currentUser.id
                            Surface(
                                onClick = {
                                    onSelectUser(user)
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isCurrent) PitchSurfaceVariant else MaterialTheme.colorScheme.surface,
                                border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.5.dp, AttackTeamCyan) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar circle
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color(user.avatarColor))
                                    ) {
                                        Text(
                                            text = user.name.take(1).uppercase(),
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = user.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${user.role.displayName} • ${user.team}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Role Badge
                                    val badgeBg = when (user.role) {
                                        UserRole.HEAD_COACH -> AttackTeamCyan.copy(alpha = 0.2f)
                                        UserRole.ASSISTANT_COACH -> DefenseTeamOrange.copy(alpha = 0.2f)
                                        UserRole.PLAYER -> GoalkeeperGold.copy(alpha = 0.2f)
                                    }
                                    val badgeTxt = when (user.role) {
                                        UserRole.HEAD_COACH -> AttackTeamCyan
                                        UserRole.ASSISTANT_COACH -> DefenseTeamOrange
                                        UserRole.PLAYER -> GoalkeeperGold
                                    }

                                    Surface(
                                        color = badgeBg,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = user.role.displayName,
                                            color = badgeTxt,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Permissions Matrix Explainer
                    Surface(
                        color = PitchSurfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Current Role Permissions:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AttackTeamCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (currentUser.role.canDeleteDrills) {
                                    "• Head Coach: Full control (create, AI voice prompt, fast changes, delete drills, manage squad)"
                                } else if (currentUser.role.canCreateDrills) {
                                    "• Assistant Coach: Can generate AI drills, adjust animations, run pitch timeline"
                                } else {
                                    "• Player: Read-only simulation mode with personal position highlights & coaching points"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Add Profile Button (if head coach)
                    if (currentUser.role.canManageRoles) {
                        OutlinedButton(
                            onClick = { showCreateUser = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_user_profile_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Coach / Player Account")
                        }
                    }
                }
            }
        }
    }
}
