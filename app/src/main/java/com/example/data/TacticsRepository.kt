package com.example.data

import com.example.db.AppDatabase
import com.example.db.DrillEntity
import com.example.db.UserEntity
import com.example.model.SoccerDrill
import com.example.model.UserProfile
import com.example.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TacticsRepository(private val database: AppDatabase) {

    private val drillDao = database.drillDao()
    private val userDao = database.userDao()

    val allDrills: Flow<List<SoccerDrill>> = drillDao.getAllDrills().map { list ->
        list.map { it.toDomain() }
    }

    val allUsers: Flow<List<UserProfile>> = userDao.getAllUsers().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun initializeDefaultsIfNeeded() = withContext(Dispatchers.IO) {
        if (drillDao.getCount() == 0) {
            val entities = SampleTacticsData.defaultDrills.map { DrillEntity.fromDomain(it) }
            drillDao.insertDrills(entities)
        }
        if (userDao.getCount() == 0) {
            val entities = SampleTacticsData.defaultUsers.map { UserEntity.fromDomain(it) }
            userDao.insertUsers(entities)
        }
    }

    suspend fun saveDrill(drill: SoccerDrill) = withContext(Dispatchers.IO) {
        drillDao.insertDrill(DrillEntity.fromDomain(drill))
    }

    suspend fun deleteDrill(drillId: String) = withContext(Dispatchers.IO) {
        drillDao.deleteDrillById(drillId)
    }

    suspend fun saveUser(user: UserProfile) = withContext(Dispatchers.IO) {
        userDao.insertUser(UserEntity.fromDomain(user))
    }
}
