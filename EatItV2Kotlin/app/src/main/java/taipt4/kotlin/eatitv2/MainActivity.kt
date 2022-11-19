package taipt4.kotlin.eatitv2

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import dmax.dialog.SpotsDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import taipt4.kotlin.eatitv2.common.Common
import taipt4.kotlin.eatitv2.model.User
import taipt4.kotlin.eatitv2.remote.ICloudFunctions
import taipt4.kotlin.eatitv2.remote.RetrofitCloudClient
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener
    private lateinit var dialog: AlertDialog
    private val compositeDisposable = CompositeDisposable()

    private lateinit var userRef: DatabaseReference
    private var providers: List<AuthUI.IdpConfig>? = null
    private lateinit var cloudFunctions: ICloudFunctions

    private val startForResult =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) { result ->
            val response = result.idpResponse
            if (result.resultCode == RESULT_OK) {
                Log.d("TaiPT4", "#onActivityResult() -- resultCode == Activity.RESULT_OK")
                val user = FirebaseAuth.getInstance().currentUser
            } else {
                Toast.makeText(this, "Failed to sign in!!", Toast.LENGTH_SHORT).show()
            }
        }

    companion object {
        private const val APP_REQUEST_CODE = 1995

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {
        Log.d("TaiPT4", "#init()")
        providers = arrayListOf(AuthUI.IdpConfig.PhoneBuilder().build())

        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCE)
        firebaseAuth = FirebaseAuth.getInstance()
        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()
        cloudFunctions = RetrofitCloudClient.getInstance().create(ICloudFunctions::class.java)
        listener = FirebaseAuth.AuthStateListener { firebaseAuth ->

            Dexter.withActivity(this)
                .withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        val user = firebaseAuth.currentUser
                        if (user != null) {
                            // Already login
                            checkUserFromFirebase(user)
                        } else {
                            phoneLogin()
                        }
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        Toast.makeText(this@MainActivity, "You must ", Toast.LENGTH_SHORT).show()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?,
                        token: PermissionToken?
                    ) {

                    }

                }).check()
        }
    }

    private fun checkUserFromFirebase(firebaseUser: FirebaseUser) {
        Log.d("TaiPT4", "#checkUserFromFirebase() -- " + firebaseUser.uid)
        dialog.show()
        userRef.child(firebaseUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    FirebaseAuth.getInstance().currentUser!!
                        .getIdToken(true)
                        .addOnFailureListener { e ->
                            Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                        }
                        .addOnCompleteListener { tokenResult ->
                            Common.authorizeToken = tokenResult.result.token!!
                            val headers = HashMap<String, String>()
                            headers["Authorization"] = Common.buildToken(Common.authorizeToken)
                            compositeDisposable.add(
                                cloudFunctions.getToken(headers)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({ braintreeToken ->
                                        dialog.dismiss()
                                        val userModel = snapshot.getValue(User::class.java)
                                        goToHomeActivity(userModel, braintreeToken.token)
                                    }, { throwable ->
                                        dialog.dismiss()
                                        Toast.makeText(
                                            this@MainActivity,
                                            throwable.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    })
                            )
                        }
                } else {
                    dialog.dismiss()
                    showRegisterDialog(firebaseUser)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@MainActivity,
                    "DatabaseError - " + error.message,
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    override fun onStart() {
        Log.d("TaiPT4", "#onStart()")
        super.onStart()
        firebaseAuth.addAuthStateListener(listener)
    }

    override fun onStop() {
        Log.d("TaiPT4", "#onStop()")
        firebaseAuth.removeAuthStateListener(listener)
        compositeDisposable.clear()
        super.onStop()
    }

    private fun phoneLogin() {
        Log.d("TaiPT4", "#phoneLogin()" + providers!!)
        startForResult.launch(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers!!)
                .build()
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(
            "TaiPT4",
            "#onActivityResult() -- requestCode == $requestCode - resultCode == $resultCode"
        )
        if (requestCode == APP_REQUEST_CODE) {
            Log.d("TaiPT4", "#onActivityResult() -- requestCode == APP_REQUEST_CODE")
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                Log.d("TaiPT4", "#onActivityResult() -- resultCode == Activity.RESULT_OK")
                val user = FirebaseAuth.getInstance().currentUser
            } else {
                Toast.makeText(this, "Failed to sign in!!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToHomeActivity(user: User?, token: String?) {
        Log.d("TaiPT4", "#goToHomeActivvity()")

        FirebaseMessaging.getInstance().token.addOnFailureListener { e ->
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            Common.currentUser = user!!
            Common.currentToken = token!!
            startActivity(Intent(this@MainActivity, HomeActivity::class.java))
            finish()
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Common.currentUser = user!!
                Common.currentToken = token!!
                Common.updateToken(this, task.result)
                startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                finish()
            }
        }
    }

    private fun showRegisterDialog(firebaseUser: FirebaseUser) {
        Log.d("TaiPT4", "#showRegisterDialog() -- " + firebaseUser.uid)
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Register")
        builder.setMessage("Please fill the information")

        val itemView =
            LayoutInflater.from(this@MainActivity).inflate(R.layout.layout_register, null)
        val edtName = itemView.findViewById<EditText>(R.id.edt_name)
        val edtAddress = itemView.findViewById<EditText>(R.id.edt_address)
        val edtPhone = itemView.findViewById<EditText>(R.id.edt_phone_number)

        // set
        edtPhone.setText(firebaseUser.email)

        builder.setView(itemView)
        builder.setNegativeButton("CANCEL") { dialogInterface, _ -> dialogInterface.dismiss() }
        builder.setPositiveButton("REGISTER") { dialogInterface, _ ->
            if (TextUtils.isDigitsOnly(edtName.text.toString())) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            } else if (TextUtils.isDigitsOnly(edtAddress.text.toString())) {
                Toast.makeText(this, "Please enter your address", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val user = User()
            user.uid = firebaseUser.uid
            user.name = edtName.text.toString()
            user.address = edtAddress.text.toString()
            user.phone = edtPhone.text.toString()

            userRef.child(firebaseUser.uid)
                .setValue(user)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val headers = HashMap<String, String>()
                        headers["Authorization"] = Common.buildToken(Common.authorizeToken)
                        compositeDisposable.add(
                            cloudFunctions.getToken(headers)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    dialogInterface.dismiss()
                                    goToHomeActivity(user, it.token)
                                    Toast.makeText(this, "Register success!", Toast.LENGTH_SHORT)
                                        .show()
                                }, { throwable ->
                                    dialogInterface.dismiss()
                                    Toast.makeText(this, throwable.message, Toast.LENGTH_SHORT)
                                        .show()
                                })
                        )
                    }
                }
        }

        // Show dialog
        val dialog = builder.create()
        dialog.show()
    }
}

















































