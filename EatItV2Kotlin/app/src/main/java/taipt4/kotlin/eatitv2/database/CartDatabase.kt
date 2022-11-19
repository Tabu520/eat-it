package taipt4.kotlin.eatitv2.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import taipt4.kotlin.eatitv2.model.CartItem

@Database(version = 1, entities = [CartItem::class], exportSchema = false)
abstract class CartDatabase: RoomDatabase() {

    abstract fun  cartDAO(): CartDAO

    companion object {
        private var instance: CartDatabase? = null

        fun getInstance(context: Context): CartDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(context, CartDatabase::class.java, "EatItV2DB2").build()
            }
            return instance!!
        }
    }

}