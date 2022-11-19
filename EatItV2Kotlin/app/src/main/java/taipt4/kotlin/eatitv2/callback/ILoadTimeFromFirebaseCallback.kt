package taipt4.kotlin.eatitv2.callback

import taipt4.kotlin.eatitv2.model.Order

interface ILoadTimeFromFirebaseCallback {
    fun onLoadTimeSuccess(order: Order, estimatedTime: Long)
    fun onLoadTimeFailed(message: String)
}