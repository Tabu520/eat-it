package taipt4.kotlin.eatitkotlinshipper

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import dmax.dialog.SpotsDialog
import taipt4.kotlin.eatitkotlinshipper.common.Common
import taipt4.kotlin.eatitkotlinshipper.model.ShipperUserModel

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener
    private lateinit var dialog: AlertDialog
    private lateinit var serverRef: DatabaseReference
    private lateinit var providers: List<AuthUI.IdpConfig>

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("TaiPT4", "#onActivityResult() -- resultCode == Activity.RESULT_OK")
            FirebaseAuth.getInstance().currentUser
        } else {
            Toast.makeText(this, "Failed to sign in!!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {
        providers = arrayListOf(AuthUI.IdpConfig.PhoneBuilder().build())
        serverRef = FirebaseDatabase.getInstance().getReference(Common.SHIPPER_REF)
        firebaseAuth = FirebaseAuth.getInstance()

        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()

        listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // Already login
                checkServerUserFromFirebase(user)
            } else {
                phoneLogin()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(listener)
    }

    private fun checkServerUserFromFirebase(user: FirebaseUser) {
        dialog.show()
        serverRef.child(user.uid).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userModel = snapshot.getValue(ShipperUserModel::class.java)
                    if (userModel!!.isActive) {
                        goToHomeActivity(userModel)
                    } else {
                        dialog.dismiss()
                        Toast.makeText(this@MainActivity, "You must be allowed from Admin to access this application", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    dialog.dismiss()
                    showRegisterDialog(user)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                dialog.dismiss()
                Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun goToHomeActivity(userModel: ShipperUserModel) {
        dialog.dismiss()
        Common.currentShipperUser = userModel
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun showRegisterDialog(user: FirebaseUser) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Register")
        builder.setMessage("Please fill the information")

        val itemView = LayoutInflater.from(this@MainActivity).inflate(R.layout.layout_register, null)
        val edtName = itemView.findViewById<EditText>(R.id.edt_name)
        val edtMail = itemView.findViewById<EditText>(R.id.edt_email)

        edtMail.setText(user.email)

        builder.setNegativeButton("CANCEL") { dialogInterface, _ -> dialogInterface.dismiss() }
            .setPositiveButton("REGISTER") { _, _ ->
                if (TextUtils.isEmpty(edtName.text)) {
                    Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val serverUser = ShipperUserModel()
                serverUser.uid = user.uid
                serverUser.name = edtName.text.toString()
                serverUser.phone= edtMail.text.toString()
                serverUser.isActive = false

                dialog.show()
                serverRef.child(serverUser.uid!!).setValue(serverUser)
                    .addOnFailureListener { e ->
                        dialog.dismiss()
                        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    }
                    .addOnCompleteListener {
                        dialog.dismiss()
                        Toast.makeText(this, "Register success! Admin will check and active account soon", Toast.LENGTH_SHORT).show()
                    }
            }
        builder.setView(itemView)
        // Show dialog
        val registerDialog = builder.create()
        registerDialog.show()
    }

    private fun phoneLogin() {
        Log.d("TaiPT4", "#phoneLogin() $providers")
        startForResult.launch(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build())
    }

    override fun onStop() {
        firebaseAuth.removeAuthStateListener(listener)
        super.onStop()
    }
}