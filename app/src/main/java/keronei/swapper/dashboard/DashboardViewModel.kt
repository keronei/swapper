package keronei.swapper.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import keronei.swapper.data.domain.BatteryRequestModel
import keronei.swapper.data.domain.BatteryRequestRepository
import keronei.swapper.data.domain.BatteryRequestUpdateModel
import keronei.swapper.data.domain.RequestsWithUpdatesModel
import keronei.swapper.utils.RequestResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val batteryRequestRepository: BatteryRequestRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<RequestUIState?> = MutableStateFlow(null)
    val uiState: StateFlow<RequestUIState?> = _uiState

    private val _requests = MutableStateFlow<List<RequestsWithUpdatesModel>>(emptyList())
    val requests: StateFlow<List<RequestsWithUpdatesModel>> = _requests

    var loggedIn = false

    init {
        observeLocalRequests()
    }

    private fun observeLocalRequests() {
        viewModelScope.launch {
            batteryRequestRepository.queryLocalRequests()
                .collect { result ->
                    _requests.value = result
                }
        }
    }

    fun createRequest(batteryRequestModel: BatteryRequestModel) {
        viewModelScope.launch {
            val result = batteryRequestRepository.createBatteryRequest(batteryRequestModel)

            when (result) {
                is RequestResult.Error -> {
                    _uiState.value = RequestUIState.Failure(result.error ?: "Some error occurred.")
                }

                is RequestResult.Success -> {
                    _uiState.value = RequestUIState.Success(result.data)
                }
            }

        }
    }

    fun removeRequest(batteryRequestModel: BatteryRequestModel) {
        viewModelScope.launch {
            val result = batteryRequestRepository.removeBatteryRequest(batteryRequestModel)

            when (result) {
                is RequestResult.Error -> {
                    _uiState.value = RequestUIState.Failure(result.error ?: "Some error occurred.")
                }

                is RequestResult.Success -> {
                    _uiState.value = RequestUIState.Success(result.data)
                }
            }
        }
    }

    fun updateRequest(batteryRequestUpdateModel: BatteryRequestUpdateModel) {
        viewModelScope.launch {
            val result = batteryRequestRepository.updateBatteryRequest(batteryRequestUpdateModel)
            when (result) {
                is RequestResult.Error -> {
                    _uiState.value = RequestUIState.Failure(result.error ?: "Some error occurred.")
                }

                is RequestResult.Success -> {
                    _uiState.value = RequestUIState.Success(result.data)
                }
            }
        }
    }


}