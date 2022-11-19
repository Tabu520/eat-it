package taipt4.kotlin.eatitkotlinserver

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import taipt4.kotlin.eatitkotlinserver.common.Common
import taipt4.kotlin.eatitkotlinserver.databinding.ActivityHomeBinding
import taipt4.kotlin.eatitkotlinserver.eventbus.CategoryClick
import taipt4.kotlin.eatitkotlinserver.eventbus.ChangeMenuClick
import taipt4.kotlin.eatitkotlinserver.eventbus.ToastEvent

class HomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController
    private lateinit var navView: NavigationView

    private var menuClick: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarHome.toolbar)

        subscribeToTopic(Common.getNewOrderTopic())

        drawerLayout = binding.drawerLayout
        navView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_home)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_category, R.id.nav_food_list, R.id.nav_order, R.id.nav_shipper
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setNavigationItemSelectedListener { item ->
            item.isChecked = true
            drawerLayout.closeDrawers()
            when (item.itemId) {
                R.id.nav_sign_out -> signOut()
                R.id.nav_category -> {
                    if (menuClick != item.itemId) {
                        navController.popBackStack() // Clear back stack
                        navController.navigate(R.id.nav_category)
                    }
                }
                R.id.nav_order -> {
                    if (menuClick != item.itemId) {
                        navController.popBackStack()
                        navController.navigate(R.id.nav_order)
                    }
                }
                R.id.nav_shipper -> {
                    if (menuClick != item.itemId) {
                        navController.popBackStack()
                        navController.navigate(R.id.nav_shipper)
                    }
                }
            }
            menuClick = item.itemId
            true
        }

        // View
        val headerView = navView.getHeaderView(0)
        val txtUser = headerView.findViewById<TextView>(R.id.header_txt_user)
        Common.setSpanString("Hey, ", Common.currentServerUser!!.name, txtUser)
        menuClick = R.id.nav_category // Default
    }

    private fun subscribeToTopic(newOrderTopic: String) {
        FirebaseMessaging.getInstance()
            .subscribeToTopic(newOrderTopic)
            .addOnFailureListener { e ->
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                if (!it.isSuccessful) {
                    Toast.makeText(this, "Subscribe topic failed!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signOut() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Sign out")
            .setMessage("Do you really want to exit?")
            .setNegativeButton("CANCEL") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("OK") { dialog, _ ->
                Common.selectedFood = null
                Common.selectedCategory = null
                Common.currentServerUser = null
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                dialog.dismiss()
                finish()
            }
        builder.create().show()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onCategoryClick(event: CategoryClick) {
        if (event.isSuccess) {
           if (menuClick != R.id.nav_food_list) {
               navController.navigate(R.id.nav_food_list)
               menuClick = R.id.nav_food_list
           }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onChangeMenuEvent(event: ChangeMenuClick) {
        if (!event.isFromFoodList) {
            // Clear
            navController.popBackStack(R.id.nav_category, true)
            navController.navigate(R.id.nav_category)
        }
        menuClick = -1
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onToastEvent(event: ToastEvent) {
        if (event.isUpdate) {
            Toast.makeText(this, "Update success", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Delete success", Toast.LENGTH_SHORT).show()
        }
        EventBus.getDefault().postSticky(ChangeMenuClick(event.isBackFromFoodList))
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }
}