package com.example.finlogcalc.radio.ui.RadioHome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finlogcalc.radio.data.RadioStation
import com.example.finlogcalc.radio.data.sampleRadioStations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RadioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RadioUiState())
    val uiState = _uiState.asStateFlow()

    fun playStation(station: RadioStation) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                currentStation = station,
                isPlaying = true
            )
        }
    }

    fun toggleFavorite(stationId: String) {
        viewModelScope.launch {
            val favorites = _uiState.value.favoriteStationIds
            val newFavorites = if (favorites.contains(stationId)) {
                favorites - stationId
            } else {
                favorites + stationId
            }
            _uiState.value = _uiState.value.copy(favoriteStationIds = newFavorites)
        }
    }

    fun toggleShowFavorites() {
        _uiState.value = _uiState.value.copy(showFavorites = !_uiState.value.showFavorites)
    }

    fun showFullPlayer() {
        _uiState.value = _uiState.value.copy(isFullPlayerVisible = true)
    }

    fun hideFullPlayer() {
        _uiState.value = _uiState.value.copy(isFullPlayerVisible = false)
    }
}

data class RadioUiState(
    val stations: List<RadioStation> = sampleRadioStations,
    val currentStation: RadioStation? = null,
    val isPlaying: Boolean = false,
    val favoriteStationIds: Set<String> = setOf(),
    val showFavorites: Boolean = false,
    val isFullPlayerVisible: Boolean = false
)
