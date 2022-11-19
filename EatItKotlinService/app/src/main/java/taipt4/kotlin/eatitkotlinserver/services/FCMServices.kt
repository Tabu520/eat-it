package taipt4.kotlin.eatitkotlinserver.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import taipt4.kotlin.eatitkotlinserver.common.Common
import kotlin.random.Random

class FCMServices: FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Common.updateToken(this, p0)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val receivedData = remoteMessage.data
        Common.showNotification(this, Random.nextInt(), receivedData[Common.NOTI_TITLE], receivedData[Common.NOTI_CONTENT], null)
    }
}