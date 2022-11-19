package taipt4.kotlin.eatitv2.remote

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitCloudClient {

    private var sInstance: Retrofit? = null

    fun getInstance(): Retrofit {
        if (sInstance == null) {
            sInstance = Retrofit.Builder().baseUrl("https://us-central1-eatitkotlin-60ac5.cloudfunctions.net/widgets/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        }
        return sInstance!!
    }
}