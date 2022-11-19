package taipt4.kotlin.eatitkotlinserver.common

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.database.FirebaseDatabase
import taipt4.kotlin.eatitkotlinserver.R
import taipt4.kotlin.eatitkotlinserver.model.Category
import taipt4.kotlin.eatitkotlinserver.model.Food
import taipt4.kotlin.eatitkotlinserver.model.ServerUserModel
import taipt4.kotlin.eatitkotlinserver.model.TokenModel
import kotlin.math.abs
import kotlin.random.Random

object Common {

    var currentServerUser: ServerUserModel? = null
    var selectedCategory: Category? = null
    var selectedFood: Food? = null

    const val SERVER_REF = "Server"
    const val ORDER_REF: String = "Order"
    const val CATEGORY_REF: String = "Category"
    const val POPULAR_REF: String = "MostPopular"
    const val BEST_DEAL_REF: String = "BestDeals"
    const val USER_REFERENCE = "Users"
    const val COMMENT_REF: String = "Comments"
    const val TOKEN_REF: String = "Tokens"
    const val SHIPPER_REF: String = "Shippers"
    const val SHIPPING_ORDER_REF: String = "ShippingOrder"

    var currentToken: String = ""
    const val DEFAULT_COLUMN_COUNT: Int = 0
    const val FULL_WIDTH_COLUMN: Int = 1
    var authorizeToken: String = ""
    const val NOTI_TITLE: String = "title"
    const val NOTI_CONTENT: String = "content"

    fun setSpanString(welcome: String, name: String?, txtUser: TextView?) {
        val builder = SpannableStringBuilder()
        builder.append(welcome)
        val txtSpannable = SpannableString(name)
        val boldSpan = StyleSpan(Typeface.BOLD)
        txtSpannable.setSpan(boldSpan, 0, name!!.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.append(txtSpannable)
        txtUser!!.setText(builder, TextView.BufferType.SPANNABLE)
    }

    fun setSpanStringColor(welcome: String, name: String, user: TextView?, color: Int) {
        val builder = SpannableStringBuilder()
        builder.append(welcome)
        val txtSpannable = SpannableString(name)
        val boldSpan = StyleSpan(Typeface.BOLD)
        txtSpannable.setSpan(boldSpan, 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        txtSpannable.setSpan(ForegroundColorSpan(color), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.append(txtSpannable)
        user!!.setText(builder, TextView.BufferType.SPANNABLE)
    }

    fun convertStatusToString(orderStatus: Int): String {
        return when (orderStatus) {
            0 -> "Placed"
            1 -> "Shipping"
            2 -> "Shipped"
            -1 -> "Cancelled"
            else -> "Error"
        }
    }

    fun updateToken(context: Context, token: String) {
        FirebaseDatabase.getInstance().getReference(TOKEN_REF)
            .child(currentServerUser!!.uid!!)
            .setValue(TokenModel(currentServerUser!!.phone!!, token))
            .addOnFailureListener { e ->
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    fun getNewOrderTopic(): String {
        return "/topics/new_order"
    }

    @SuppressLint("ServiceCast")
    fun showNotification(
        context: Context,
        id: Int,
        title: String?,
        content: String?,
        intent: Intent?
    ) {
        var pendingIntent: PendingIntent? = null
        if (intent != null) {
            pendingIntent =
                PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val NOTIFICATION_CHANNEL_ID = "taipt4.dev.eatitkotlin"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Eat It Kotlin TaiPT4", NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.description = "Eat It Kotlin TaiPT4"
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)

            notificationManager.createNotificationChannel(notificationChannel)
        }
        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        builder.setContentTitle(title!!).setContentText(content!!).setAutoCancel(true)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.ic_baseline_star_rate_24
                )
            )
        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent)
        }

        val notification = builder.build()
        notificationManager.notify(id, notification)
    }

    fun createOrderNumber(): String {
        return StringBuffer()
            .append(System.currentTimeMillis())
            .append(abs(Random.nextInt()))
            .toString()
    }
}