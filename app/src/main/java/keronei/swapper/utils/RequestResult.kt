package keronei.swapper.utils

sealed class RequestResult<out T : Any> {
    data class Success<out T : Any>(
        val data: T,
    ) : RequestResult<T>()

    data class Error(
        val error: String?,
    ) : RequestResult<Nothing>()
}
