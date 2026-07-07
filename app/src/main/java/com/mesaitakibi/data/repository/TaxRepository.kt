package com.mesaitakibi.data.repository

import com.mesaitakibi.data.local.dao.TaxParametersDao
import com.mesaitakibi.data.mapper.toDomain
import com.mesaitakibi.data.mapper.toEntity
import com.mesaitakibi.domain.payroll.TaxParameters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaxRepository @Inject constructor(
    private val dao: TaxParametersDao
) {
    fun observeLatest(): Flow<TaxParameters?> =
        dao.observeLatest().map { it?.toDomain() }

    /** Belirtilen yıl için parametre; yoksa 2026 varsayılanına döner. */
    suspend fun getForYear(year: Int): TaxParameters =
        dao.getByYear(year)?.toDomain() ?: TaxParameters.default2026().copy(year = year)

    suspend fun save(params: TaxParameters) = dao.upsert(params.toEntity())
}
