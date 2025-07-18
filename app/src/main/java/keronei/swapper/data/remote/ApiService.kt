package keronei.swapper.data.remote

import keronei.swapper.utils.syncAPI
import okhttp3.Response
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST(syncAPI)
    fun sendLocalData(requests: List<BatterySyncDTO>): Response

    @GET(syncAPI)
    fun fetchServerData(): retrofit2.Response<List<BatteryRequestsRemote>>
}