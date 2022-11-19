package taipt4.kotlin.eatitv2.remote

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitFCMClient {

    private var sInstance: Retrofit? = null

    fun getInstance(): Retrofit {
        if (sInstance == null) {
            sInstance = Retrofit.Builder().baseUrl("https://fcm.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        }
        return sInstance!!
    }

}