package taipt4.kotlin.eatitv2.ui.cart

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import taipt4.kotlin.eatitv2.common.Common
import taipt4.kotlin.eatitv2.database.CartDataSource
import taipt4.kotlin.eatitv2.database.CartDatabase
import taipt4.kotlin.eatitv2.database.LocalCartDataSource
import taipt4.kotlin.eatitv2.model.CartItem

class CartViewModel : ViewModel() {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var cartDataSource: CartDataSource
    private var listCartItemMutableLiveData: MutableLiveData<List<CartItem>>? = null

    fun initCartDataSource(context: Context) {
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).cartDAO())
    }

    fun getMutableLiveDataCartItem(): MutableLiveData<List<CartItem>> {
        if (listCartItemMutableLiveData == null) {
            listCartItemMutableLiveData = MutableLiveData()
        }
        getListCartItem()
        return listCartItemMutableLiveData!!
    }

    private fun getListCartItem() {
        compositeDisposable.addAll(cartDataSource.getAllCart(Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                listCartItemMutableLiveData!!.value = it
            }, {
                listCartItemMutableLiveData!!.value = null
            })
        )
    }

    fun onStop() {
        compositeDisposable.clear()
    }

}