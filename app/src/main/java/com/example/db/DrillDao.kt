package com.example.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DrillDao {
    @Query("SELECT * FROM soccer_drills ORDER BY timestamp DESC")
    fun getAllDrills(): Flow<List<DrillEntity>>

    @Query("SELECT * FROM soccer_drills WHERE id = :id LIMIT 1")
    suspend fun getDrillById(id: String): DrillEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrill(drill: DrillEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrills(drills: List<DrillEntity>)

    @Query("DELETE FROM soccer_drills WHERE id = :id")
    suspend fun deleteDrillById(id: String)

    @Query("SELECT COUNT(*) FROM soccer_drills")
    suspend fun getCount(): Int
}
