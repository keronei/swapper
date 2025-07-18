package keronei.swapper.dashboard

sealed class RequestUIState {

    data class Success(val message: String) : RequestUIState()
    data class Failure(val error: String) : RequestUIState()
    data object Loading : RequestUIState()

}