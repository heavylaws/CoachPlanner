package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.UserProfile
import com.example.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val role: String,
    val team: String,
    val avatarColor: Long
) {
    fun toDomain(): UserProfile = UserProfile(
        id = id,
        name = name,
        email = email,
        role = try { UserRole.valueOf(role) } catch (_: Exception) { UserRole.HEAD_COACH },
        team = team,
        avatarColor = avatarColor
    )

    companion object {
        fun fromDomain(user: UserProfile): UserEntity = UserEntity(
            id = user.id,
            name = user.name,
            email = user.email,
            role = user.role.name,
            team = user.team,
            avatarColor = user.avatarColor
        )
    }
}
