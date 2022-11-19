package taipt4.kotlin.eatitkotlinserver.callback

import android.widget.Button
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import taipt4.kotlin.eatitkotlinserver.model.Order
import taipt4.kotlin.eatitkotlinserver.model.Shipper

interface ILoadShipperCallback {
    fun onLoadShipperSuccess(listShipper: List<Shipper>)
    fun onLoadShipperSuccess(
        pos: Int, order: Order?, listShipper: List<Shipper>?, dialog: AlertDialog?,
        ok: Button?, cancel: Button?, rdiShipping: RadioButton?, rdiShipped: RadioButton?,
        rdiCancelled: RadioButton?, rdiDelete: RadioButton?, rdiRestorePlaced: RadioButton?
    )
    fun onLoadShipperFailed(errorMessage: String)
}