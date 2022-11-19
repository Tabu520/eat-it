package taipt4.kotlin.eatitkotlinserver.ui.shipper

import android.util.Log
import android.widget.Button
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import taipt4.kotlin.eatitkotlinserver.callback.ILoadShipperCallback
import taipt4.kotlin.eatitkotlinserver.common.Common
import taipt4.kotlin.eatitkotlinserver.model.Order
import taipt4.kotlin.eatitkotlinserver.model.Shipper

class ShipperViewModel : ViewModel(), ILoadShipperCallback {

    private var mutableListShipper: MutableLiveData<List<Shipper>>? = null
    private var errorMessages: MutableLiveData<String> = MutableLiveData()
    private val loadShipperCallback: ILoadShipperCallback = this

    fun getListShipper(): MutableLiveData<List<Shipper>> {
        if (mutableListShipper == null) {
            mutableListShipper = MutableLiveData()
            loadShipper()
        }
        return mutableListShipper!!
    }

    fun getErrorMessage(): MutableLiveData<String> {
        return errorMessages
    }

    fun loadShipper() {
        val tempList = ArrayList<Shipper>()
        val shipperRef = FirebaseDatabase.getInstance().getReference(Common.SHIPPER_REF)
        shipperRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.children) {
                    val model = item.getValue(Shipper::class.java)
                    model!!.key = item.key
                    Log.d("TaiPT4", model.name!!)
                    tempList.add(model)
                }
                loadShipperCallback.onLoadShipperSuccess(tempList)
            }

            override fun onCancelled(error: DatabaseError) {
                loadShipperCallback.onLoadShipperFailed(error.message)
            }

        })
    }

    override fun onLoadShipperSuccess(listShipper: List<Shipper>) {
        Log.d("TaiPT4", listShipper[0].name!!)
        mutableListShipper!!.value = listShipper
    }

    override fun onLoadShipperSuccess(
        pos: Int,
        order: Order?,
        listShipper: List<Shipper>?,
        dialog: AlertDialog?,
        ok: Button?,
        cancel: Button?,
        rdiShipping: RadioButton?,
        rdiShipped: RadioButton?,
        rdiCancelled: RadioButton?,
        rdiDelete: RadioButton?,
        rdiRestorePlaced: RadioButton?
    ) {
        // do nothing
    }

    override fun onLoadShipperFailed(errorMessage: String) {
        errorMessages.value = errorMessage
    }


}