package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Download
import com.example.data.History
import com.example.data.Space
import com.example.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.BuildConfig
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.Part
import com.example.api.RetrofitClient

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    
    val spaces: StateFlow<List<Space>> = db.spaceDao().getAllSpaces()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val downloads: StateFlow<List<Download>> = db.downloadDao().getAllDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    fun getHistoryForSpace(spaceId: Int): kotlinx.coroutines.flow.Flow<List<History>> {
        return db.historyDao().getHistoryForSpace(spaceId)
    }
        
    val themePreference = MutableStateFlow(AppTheme.OCHRE)
    val storagePath = MutableStateFlow("/storage/emulated/0/Download/Spaces")
    val currentSummary = MutableStateFlow<String?>(null)

    fun summarizeWebpage(text: String) {
        currentSummary.value = "Generating summary..."
        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    currentSummary.value = "Error: Gemini API Key not configured. Please add it to your secrets."
                    return@launch
                }
                val prompt = "Summarize the following webpage content concisely:\n\n$text"
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt))))
                )
                val response = RetrofitClient.geminiService.generateContent(apiKey, request)
                val summary = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Could not generate summary."
                currentSummary.value = summary
            } catch (e: Exception) {
                currentSummary.value = "Error generating summary: ${e.message}"
            }
        }
    }

    fun clearSummary() {
        currentSummary.value = null
    }
    
    fun setTheme(theme: AppTheme) {
        themePreference.value = theme
    }
    
    fun setStoragePath(path: String) {
        storagePath.value = path
    }

    fun addSpace(name: String, color: Long, isBusiness: Boolean) {
        viewModelScope.launch {
            db.spaceDao().insertSpace(Space(name = name, color = color, isBusiness = isBusiness))
        }
    }

    fun addDownload(spaceId: Int, fileName: String, url: String) {
        viewModelScope.launch {
            db.downloadDao().insertDownload(Download(spaceId = spaceId, fileName = fileName, url = url))
        }
    }

    fun addHistory(spaceId: Int, url: String, title: String) {
        viewModelScope.launch {
            db.historyDao().insertHistory(History(spaceId = spaceId, url = url, title = title))
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            db.historyDao().clearAllHistory()
        }
    }
}
