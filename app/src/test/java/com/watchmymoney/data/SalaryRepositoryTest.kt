package com.watchmymoney.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SalaryRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val dataStore by lazy {
        PreferenceDataStoreFactory.create(
            produceFile = { tempFolder.newFile("test_settings.preferences_pb") }
        )
    }

    @Test
    fun testDefaultValues() = runTest {
        val repository = SalaryRepository(dataStore)
        val config = repository.userConfig.first()
        assertEquals(0.0, config.annualSalary, 0.0)
        assertEquals("$", config.currencySymbol)
        assertEquals(0, config.resetHour)
    }

    @Test
    fun testUpdateAnnualSalary() = runTest {
        val repository = SalaryRepository(dataStore)
        repository.updateAnnualSalary(120000.0)
        val config = repository.userConfig.first()
        assertEquals(120000.0, config.annualSalary, 0.0)
    }

    @Test
    fun testUpdateCurrencySymbol() = runTest {
        val repository = SalaryRepository(dataStore)
        repository.updateCurrencySymbol("€")
        val config = repository.userConfig.first()
        assertEquals("€", config.currencySymbol)
    }

    @Test
    fun testUpdateResetHour() = runTest {
        val repository = SalaryRepository(dataStore)
        repository.updateResetHour(8)
        val config = repository.userConfig.first()
        assertEquals(8, config.resetHour)
    }

    @Test
    fun testUpdateConfigBulk() = runTest {
        val repository = SalaryRepository(dataStore)
        repository.updateConfig(
            annualSalary = 75000.0,
            currencySymbol = "£",
            resetHour = 6
        )
        val config = repository.userConfig.first()
        assertEquals(75000.0, config.annualSalary, 0.0)
        assertEquals("£", config.currencySymbol)
        assertEquals(6, config.resetHour)
    }

    @Test
    fun testUpdateConfigPartial() = runTest {
        val repository = SalaryRepository(dataStore)
        // Set initial values
        repository.updateConfig(
            annualSalary = 50000.0,
            currencySymbol = "$",
            resetHour = 0
        )
        
        // Update only currency symbol
        repository.updateConfig(currencySymbol = "¥")
        
        val config1 = repository.userConfig.first()
        assertEquals(50000.0, config1.annualSalary, 0.0)
        assertEquals("¥", config1.currencySymbol)
        assertEquals(0, config1.resetHour)

        // Update annual salary and reset hour
        repository.updateConfig(annualSalary = 60000.0, resetHour = 12)
        val config2 = repository.userConfig.first()
        assertEquals(60000.0, config2.annualSalary, 0.0)
        assertEquals("¥", config2.currencySymbol)
        assertEquals(12, config2.resetHour)
    }
}
