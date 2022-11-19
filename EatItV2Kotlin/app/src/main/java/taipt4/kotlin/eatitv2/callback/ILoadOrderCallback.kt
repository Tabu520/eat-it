package taipt4.kotlin.eatitv2.callback

import taipt4.kotlin.eatitv2.model.Order

interface ILoadOrderCallback {

    fun onLoadOrderSuccess(orderList: List<Order>)
    fun onLoadOrderFailed(message: String)
}