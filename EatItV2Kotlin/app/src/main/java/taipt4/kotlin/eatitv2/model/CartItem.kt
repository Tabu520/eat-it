package taipt4.kotlin.eatitv2.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "Cart", primaryKeys = ["uid", "foodId", "foodSize", "foodAddon"])
class CartItem {

    @NonNull
    @ColumnInfo(name = "foodId")
    var foodId: String = ""

    @ColumnInfo(name = "foodName")
    var foodName: String = ""

    @ColumnInfo(name = "foodImage")
    var foodImage: String = ""

    @ColumnInfo(name = "foodPrice")
    var foodPrice: Double = 0.0

    @ColumnInfo(name = "foodQuantity")
    var foodQuantity: Int = 0

    @NonNull
    @ColumnInfo(name = "foodAddon")
    var foodAddon: String = ""

    @NonNull
    @ColumnInfo(name = "foodSize")
    var foodSize: String = ""

    @ColumnInfo(name = "userPhone")
    var userPhone: String = ""

    @ColumnInfo(name = "foodExtraPrice")
    var foodExtraPrice: Double = 0.0

    @NonNull
    @ColumnInfo(name = "uid")
    var uid: String = ""

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is CartItem) return false
        val cartItem = other as CartItem?
        return cartItem!!.foodId == this.foodId
                && cartItem.foodSize == this.foodSize
                && cartItem.foodAddon == this.foodAddon
    }

    override fun hashCode(): Int {
        var result = foodId.hashCode()
        result = 31 * result + foodName.hashCode()
        result = 31 * result + foodImage.hashCode()
        result = 31 * result + foodPrice.hashCode()
        result = 31 * result + foodQuantity
        result = 31 * result + foodAddon.hashCode()
        result = 31 * result + foodSize.hashCode()
        result = 31 * result + userPhone.hashCode()
        result = 31 * result + foodExtraPrice.hashCode()
        result = 31 * result + uid.hashCode()
        return result
    }
}