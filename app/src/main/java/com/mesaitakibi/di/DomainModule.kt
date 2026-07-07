package com.mesaitakibi.di

import com.mesaitakibi.domain.finance.BudgetCalculator
import com.mesaitakibi.domain.overtime.OvertimeDetector
import com.mesaitakibi.domain.payroll.PayrollCalculator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Saf Kotlin domain motorları (durumsuz) DI sağlayıcıları. */
@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    @Provides @Singleton fun overtimeDetector() = OvertimeDetector()
    @Provides @Singleton fun payrollCalculator() = PayrollCalculator()
    @Provides @Singleton fun budgetCalculator() = BudgetCalculator()
}
