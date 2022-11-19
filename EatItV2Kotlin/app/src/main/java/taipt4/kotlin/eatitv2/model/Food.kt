package taipt4.kotlin.eatitv2.model

class Food {

    var id: String? = null
    var name: String? = null
    var image: String? = null
    var description: String? = null
    var price: Long = 0
    var addon: List<AddonModel>? = ArrayList()
    var size: List<SizeModel> = ArrayList()
    var key: String? = null

    var ratingValue: Double = 0.toDouble()
    var ratingCount: Long = 0.toLong()

    var userSelectedAddonModel: MutableList<AddonModel>? = null
    var userSelectedSize: SizeModel? = null
}