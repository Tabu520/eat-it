package taipt4.kotlin.eatitv2.ui.fooddetail

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.andremion.counterfab.CounterFab
import com.bumptech.glide.Glide
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.google.gson.Gson
import dmax.dialog.SpotsDialog
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import taipt4.kotlin.eatitv2.R
import taipt4.kotlin.eatitv2.common.Common
import taipt4.kotlin.eatitv2.database.CartDataSource
import taipt4.kotlin.eatitv2.database.CartDatabase
import taipt4.kotlin.eatitv2.database.LocalCartDataSource
import taipt4.kotlin.eatitv2.eventbus.CountCartEvent
import taipt4.kotlin.eatitv2.eventbus.MenuItemBack
import taipt4.kotlin.eatitv2.model.AddonModel
import taipt4.kotlin.eatitv2.model.CartItem
import taipt4.kotlin.eatitv2.model.CommentModel
import taipt4.kotlin.eatitv2.model.Food
import taipt4.kotlin.eatitv2.ui.comment.CommentFragment
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.roundToInt

class FoodDetailFragment : Fragment(), TextWatcher {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var cartDataSource: CartDataSource

    private lateinit var foodDetailViewModel: FoodDetailViewModel

    private lateinit var imFoodImage: ImageView
    private lateinit var btnCart: CounterFab
    private lateinit var btnRating: FloatingActionButton
    private lateinit var foodName: TextView
    private lateinit var foodDescription: TextView
    private lateinit var foodPrice: TextView
    private lateinit var btnNumber: ElegantNumberButton
    private lateinit var ratingBar: RatingBar
    private lateinit var btnShowComment: Button
    private lateinit var radioGroupSize: RadioGroup
    private lateinit var imageAddon: ImageView
    private lateinit var chipGroupUserSelectedAddon: ChipGroup

    private var waitingDialog: android.app.AlertDialog? = null
    private lateinit var addonBottomSheetDialog: BottomSheetDialog

    // Addon Layout
    private lateinit var chipGroupAddon: ChipGroup
    private lateinit var edtAddonSearch: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        foodDetailViewModel =
            ViewModelProvider(this).get(FoodDetailViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_food_detail, container, false)
        initViews(root)
        foodDetailViewModel.getFoodMutableLiveData().observe(viewLifecycleOwner, {
            displayInformation(it)
        })
        foodDetailViewModel.getCommentMutableLiveData().observe(viewLifecycleOwner, {
            submitRatingToFirebase(it)
        })
        return root
    }

    private fun submitRatingToFirebase(commentModel: CommentModel) {
        waitingDialog!!.show()

        // First, submit to Comment Ref
        FirebaseDatabase.getInstance()
            .getReference(Common.COMMENT_REF)
            .child(Common.selectedFood!!.id!!)
            .push()
            .setValue(commentModel)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    addRatingToFood(commentModel.ratingValue.toDouble())
                }
                waitingDialog!!.dismiss()
            }
    }

    private fun addRatingToFood(ratingValue: Double) {
        FirebaseDatabase.getInstance()
            .getReference(Common.CATEGORY_REF) // Select category
            .child(Common.selectedCategory!!.menu_id!!) // Select menu in category
            .child("foods") // Select food array
            .child(Common.selectedFood!!.key!!) // Select key
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val food = snapshot.getValue(Food::class.java)
                        food!!.key = Common.selectedFood!!.key

                        // Apply rating
                        val sumRating = food.ratingValue + ratingValue
                        val ratingCount = food.ratingCount + 1

                        val updateData = HashMap<String, Any>()
                        updateData["ratingCount"] = ratingCount
                        updateData["ratingValue"] = sumRating

                        // Update data into variable
                        food.ratingCount = ratingCount
                        food.ratingValue = sumRating

                        snapshot.ref
                            .updateChildren(updateData)
                            .addOnCompleteListener { task ->
                                waitingDialog!!.dismiss()
                                if (task.isSuccessful) {
                                    Common.selectedFood = food
                                    foodDetailViewModel.setFoodModel(food)
                                    Toast.makeText(context, "Thank you!", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        waitingDialog!!.dismiss()
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    waitingDialog!!.dismiss()
                    Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                }

            })

    }

    private fun displayInformation(food: Food) {
        Glide.with(requireContext()).load(food.image).into(imFoodImage)
        foodName.text = food.name
        foodDescription.text = food.description
        foodPrice.text = food.price.toString()
        ratingBar.rating = food.ratingValue.toFloat() / food.ratingCount

        // set size
        for (sizeModel in food.size) {
            val radioButton = RadioButton(context)
            radioButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    Common.selectedFood!!.userSelectedSize = sizeModel
                }
                calculateTotalPrice()
            }
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
            radioButton.layoutParams = params
            radioButton.text = sizeModel.name
            radioButton.tag = sizeModel.price
            radioGroupSize.addView(radioButton)

            // Default first radio button selected
            if (radioGroupSize.size > 0) {
                val firstRadioButton = radioGroupSize.getChildAt(0) as RadioButton
                firstRadioButton.isChecked = true
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun initViews(root: View) {
        (activity as AppCompatActivity).supportActionBar!!.title = Common.selectedFood!!.name!!
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(requireContext()).cartDAO())

        waitingDialog = SpotsDialog.Builder().setContext(context).setCancelable(false).build()
        addonBottomSheetDialog = BottomSheetDialog(requireContext(), R.style.DialogStyle)
        val layoutUserSelectedAddon = layoutInflater.inflate(R.layout.layout_addon_display, null)
        chipGroupAddon =
            layoutUserSelectedAddon.findViewById(R.id.addon_cg_addon_display) as ChipGroup
        edtAddonSearch = layoutUserSelectedAddon.findViewById(R.id.addon_edt_search) as EditText
        addonBottomSheetDialog.setContentView(layoutUserSelectedAddon)

        addonBottomSheetDialog.setOnDismissListener {
            displayUserSelectedAddon()
            calculateTotalPrice()
        }

        btnCart = root.findViewById(R.id.food_detail_btn_cart) as CounterFab
        imFoodImage = root.findViewById(R.id.food_detail_image) as ImageView
        btnRating = root.findViewById(R.id.food_detail_btn_rating) as FloatingActionButton
        foodName = root.findViewById(R.id.food_detail_food_name) as TextView
        foodDescription = root.findViewById(R.id.food_detail_food_description) as TextView
        foodPrice = root.findViewById(R.id.food_detail_food_price) as TextView
        btnNumber = root.findViewById(R.id.food_detail_btn_number) as ElegantNumberButton
        ratingBar = root.findViewById(R.id.food_detail_rating_bar) as RatingBar
        btnShowComment = root.findViewById(R.id.food_detail_btn_show_comment) as Button
        radioGroupSize = root.findViewById(R.id.food_detail_rdi_group_size) as RadioGroup
        imageAddon = root.findViewById(R.id.food_detail_add_addon) as ImageView
        chipGroupUserSelectedAddon =
            root.findViewById(R.id.food_detail_cg_user_selected_addon) as ChipGroup


        // Event
        imageAddon.setOnClickListener {
            if (Common.selectedFood!!.addon != null) {
                displayAllAddon()
                addonBottomSheetDialog.show()
            }
        }
        btnRating.setOnClickListener {
            showRatingDialog()
        }
        btnShowComment.setOnClickListener {
            val commentFragment = CommentFragment.getInstance()
            commentFragment.show(requireActivity().supportFragmentManager, "CommentFragment")
        }
        btnCart.setOnClickListener {
            val cartItem = CartItem()
            cartItem.uid = Common.currentUser!!.uid!!
            cartItem.userPhone = Common.currentUser!!.phone!!
            cartItem.foodId = Common.selectedFood!!.id!!
            cartItem.foodName = Common.selectedFood!!.name!!
            cartItem.foodImage = Common.selectedFood!!.image!!
            cartItem.foodPrice = Common.selectedFood!!.price.toDouble()
            cartItem.foodQuantity = btnNumber.number.toInt()
            cartItem.foodExtraPrice = Common.calculateExtraPrice(
                Common.selectedFood!!.userSelectedSize,
                Common.selectedFood!!.userSelectedAddonModel
            )

            if (Common.selectedFood!!.userSelectedAddonModel != null) {
                cartItem.foodAddon = Gson().toJson(Common.selectedFood!!.userSelectedAddonModel)
            } else {
                cartItem.foodAddon = "Default"
            }
            if (Common.selectedFood!!.userSelectedSize != null) {
                cartItem.foodSize = Gson().toJson(Common.selectedFood!!.userSelectedSize)
            } else {
                cartItem.foodSize = "Default"
            }

            cartDataSource.getItemWithAllOptionsInCart(
                Common.currentUser!!.uid!!,
                cartItem.foodId,
                cartItem.foodSize,
                cartItem.foodAddon
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<CartItem> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(cartItemFromDB: CartItem) {
                        if (cartItemFromDB.equals(cartItem)) {
                            // If item is already in database, just update
                            cartItemFromDB.foodExtraPrice = cartItem.foodExtraPrice
                            cartItemFromDB.foodAddon = cartItem.foodAddon
                            cartItemFromDB.foodSize = cartItem.foodSize
                            cartItemFromDB.foodQuantity = cartItemFromDB.foodQuantity + cartItem.foodQuantity

                            cartDataSource.updateCart(cartItemFromDB).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(object : SingleObserver<Int> {
                                    override fun onSubscribe(d: Disposable) {

                                    }

                                    override fun onSuccess(t: Int) {
                                        Toast.makeText(context, "Update Cart Success", Toast.LENGTH_SHORT).show()
                                        EventBus.getDefault().postSticky(CountCartEvent(true))
                                    }

                                    override fun onError(e: Throwable) {
                                        Toast.makeText(context, "[UPDATE CART] ${e.message}", Toast.LENGTH_SHORT).show()
                                    }

                                })
                        } else {
                            // If item is not available in database, just insert
                            compositeDisposable.add(
                                cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        Toast.makeText(context, "Add to cart successfully", Toast.LENGTH_SHORT).show()
                                        // Send a notify to HomeActivity to update CounterFab
                                        EventBus.getDefault().postSticky(CountCartEvent(true))
                                    }, { it ->
                                        Toast.makeText(context, "[INSERT CART] " + it!!.message, Toast.LENGTH_SHORT).show()
                                    })
                            )
                        }
                    }

                    override fun onError(e: Throwable) {
                        if (e.message!!.contains("empty")) {
                            compositeDisposable.add(
                                cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        Toast.makeText(context, "Add to cart successfully", Toast.LENGTH_SHORT).show()
                                        // Send a notify to HomeActivity to update CounterFab
                                        EventBus.getDefault().postSticky(CountCartEvent(true))
                                    }, { it ->
                                        Toast.makeText(context, "[INSERT CART] " + it!!.message, Toast.LENGTH_SHORT).show()
                                    })
                            )
                        } else {
                            Toast.makeText(context, "[INSERT CART] $it", Toast.LENGTH_SHORT).show()
                        }
                    }

                })
        }
    }

    private fun displayAllAddon() {
        if (Common.selectedFood!!.addon!!.isNotEmpty()) {
            chipGroupAddon.clearCheck()
            chipGroupAddon.removeAllViews()

            edtAddonSearch.addTextChangedListener(this)
            for (addonModel in Common.selectedFood!!.addon!!) {

                val chip = layoutInflater.inflate(R.layout.layout_chip, null, false) as Chip
                chip.text = StringBuilder(addonModel.name!!).append("(+$").append(addonModel.price)
                    .append(")").toString()
                chip.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        if (Common.selectedFood!!.userSelectedAddonModel == null) {
                            Common.selectedFood!!.userSelectedAddonModel = ArrayList()
                        }
                        Common.selectedFood!!.userSelectedAddonModel!!.add(addonModel)
                    }
                }
                chipGroupAddon.addView(chip)
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun displayUserSelectedAddon() {
        if (Common.selectedFood!!.userSelectedAddonModel != null && Common.selectedFood!!.userSelectedAddonModel!!.isNotEmpty()) {
            chipGroupUserSelectedAddon.removeAllViews()
            for (addonModel in Common.selectedFood!!.userSelectedAddonModel!!) {
                val chip =
                    layoutInflater.inflate(R.layout.layout_chip_with_delete, null, false) as Chip
                chip.text = StringBuilder(addonModel.name!!).append("(+$").append(addonModel.price)
                    .append(")").toString()
                chip.isClickable = false
                chip.setOnCloseIconClickListener {
                    chipGroupUserSelectedAddon.removeView(it)
                    Common.selectedFood!!.userSelectedAddonModel!!.remove(addonModel)
                    calculateTotalPrice()
                }
                chipGroupUserSelectedAddon.addView(chip)
            }
        } else {
            chipGroupUserSelectedAddon.removeAllViews()
        }
    }

    private fun showRatingDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Rating Food")
        builder.setMessage("Please fill information")

        val itemView = LayoutInflater.from(context).inflate(R.layout.layout_rating_comment, null)
        val ratingBar = itemView.findViewById<RatingBar>(R.id.rating_comment_rating_bar)
        val edtComment = itemView.findViewById<EditText>(R.id.rating_comment_comment)

        builder.setView(itemView)
        builder.setNegativeButton("CANCEL") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        builder.setPositiveButton("OK") { _, _ ->
            val commentModel = CommentModel()
            commentModel.name = Common.currentUser!!.name
            commentModel.uid = Common.currentUser!!.uid
            commentModel.comment = edtComment.text.toString()
            commentModel.ratingValue = ratingBar.rating
            val serverTimeStamp = HashMap<String, Any>()
            serverTimeStamp["timeStamp"] = ServerValue.TIMESTAMP
            commentModel.commentTimeStamp = serverTimeStamp

            foodDetailViewModel.setCommentModel(commentModel)
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun calculateTotalPrice() {
        var totalPrice = Common.selectedFood!!.price.toDouble()

        // Addon
        if (Common.selectedFood!!.userSelectedAddonModel != null && Common.selectedFood!!.userSelectedAddonModel!!.size > 0) {
            for (addonModel in Common.selectedFood!!.userSelectedAddonModel!!) {
                totalPrice += addonModel.price
            }
        }

        // Size
        totalPrice += Common.selectedFood!!.userSelectedSize!!.price.toDouble()

        var displayPrice: Double = totalPrice * btnNumber.number.toInt()
        displayPrice = (displayPrice * 100.0).roundToInt() / 100.0

        foodPrice.text = Common.formatPrice(displayPrice)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        chipGroupAddon.clearCheck()
        chipGroupAddon.removeAllViews()

        for (addonModel in Common.selectedFood!!.addon!!) {
            if (addonModel.name!!.toLowerCase(Locale.ROOT)
                    .contains(s.toString().toLowerCase(Locale.ROOT))
            ) {
                val chip = layoutInflater.inflate(R.layout.layout_chip, null, false) as Chip
                chip.text = StringBuilder(addonModel.name!!).append("(+$").append(addonModel.price)
                    .append(")").toString()
                chip.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        if (Common.selectedFood!!.userSelectedAddonModel == null) {
                            Common.selectedFood!!.userSelectedAddonModel = ArrayList()
                        }
                        Common.selectedFood!!.userSelectedAddonModel!!.add(addonModel)
                    }
                }
                chipGroupAddon.addView(chip)
            }
        }
    }

    override fun afterTextChanged(s: Editable?) {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(MenuItemBack())
        super.onDestroy()
    }
}