package taipt4.kotlin.eatitv2.remote

import io.reactivex.Observable
import retrofit2.http.*
import taipt4.kotlin.eatitv2.model.BraintreeToken
import taipt4.kotlin.eatitv2.model.BraintreeTransaction

interface ICloudFunctions {

    @GET("token")
    fun getToken(@HeaderMap headers: Map<String, String>): Observable<BraintreeToken>

    @POST("checkout")
    @FormUrlEncoded
    fun submitPayment(
        @HeaderMap headers: Map<String, String>,
        @Field("amount") amount: Double,
        @Field("payment_method_nonce") nonce: String
    ): Observable<BraintreeTransaction>
}