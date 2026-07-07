package com.mesaitakibi.ui.screens.backup

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mesaitakibi.data.backup.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val manager: BackupManager
) : ViewModel() {

    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status.asStateFlow()

    fun clearStatus() { _status.value = null }

    fun createAndShareBackup(onReady: (Intent) -> Unit) = viewModelScope.launch {
        runCatching {
            val file = withContext(Dispatchers.IO) { manager.writeBackupFile() }
            manager.shareIntent(file, "application/json")
        }.onSuccess {
            onReady(it)
            _status.value = "Yedek oluşturuldu, paylaşım menüsü açılıyor."
        }.onFailure { _status.value = "Yedek oluşturulamadı: ${it.message}" }
    }

    fun exportCsvAndShare(onReady: (Intent) -> Unit) = viewModelScope.launch {
        runCatching {
            val file = withContext(Dispatchers.IO) { manager.writeTimeEntriesCsv() }
            manager.shareIntent(file, "text/csv")
        }.onSuccess {
            onReady(it)
            _status.value = "CSV oluşturuldu, paylaşım menüsü açılıyor."
        }.onFailure { _status.value = "CSV oluşturulamadı: ${it.message}" }
    }

    fun importBackup(uri: Uri) = viewModelScope.launch {
        runCatching {
            withContext(Dispatchers.IO) {
                val content = manager.readUri(uri)
                manager.importJson(content)
            }
        }.onSuccess { _status.value = "Yedek başarıyla geri yüklendi." }
            .onFailure { _status.value = "Geri yükleme başarısız: ${it.message}" }
    }

    fun clearAll() = viewModelScope.launch {
        runCatching { withContext(Dispatchers.IO) { manager.clearAll() } }
            .onSuccess { _status.value = "Tüm veriler silindi." }
            .onFailure { _status.value = "Silme başarısız: ${it.message}" }
    }
}
