package taipt4.kotlin.eatitkotlinserver.callback

import taipt4.kotlin.eatitkotlinserver.model.Order

interface ILoadOrderCallback {

    fun onLoadOrderSuccess(orderList: List<Order>)
    fun onLoadOrderFailed(message: String)
}