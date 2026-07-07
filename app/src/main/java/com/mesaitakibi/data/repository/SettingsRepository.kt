package com.mesaitakibi.data.repository

import com.mesaitakibi.data.local.DefaultData
import com.mesaitakibi.data.local.dao.SettingsDao
import com.mesaitakibi.data.mapper.toDomain
import com.mesaitakibi.data.mapper.toEntity
import com.mesaitakibi.domain.model.EmployeeProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dao: SettingsDao
) {
    fun observeProfile(): Flow<EmployeeProfile> =
        dao.observe().map { (it ?: DefaultData.defaultSettings()).toDomain() }

    suspend fun getProfile(): EmployeeProfile =
        (dao.get() ?: DefaultData.defaultSettings()).toDomain()

    suspend fun save(profile: EmployeeProfile) = dao.upsert(profile.toEntity())
}
