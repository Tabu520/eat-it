package taipt4.kotlin.eatitv2.remote

import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import taipt4.kotlin.eatitv2.model.FCMResponse
import taipt4.kotlin.eatitv2.model.FCMSendData

interface IFCMServices {

    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAAByCWZyI:APA91bHXHp5cpZraEwjt_GVMY32Qy6zWFvzEot6t_59Jx8O4_SAfLF6kPyLbVZt3hEFOQ4QxPgOAKCemAmgHqYkbBP_zQXal9F7ZzUheVRJuTqAg-gbHu4jGXr6lIT28N4pF-KlUrnSp"
    )
    @POST("fcm/send")
    fun sendNotification(@Body body: FCMSendData): Observable<FCMResponse>
}