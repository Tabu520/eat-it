package taipt4.kotlin.eatitv2

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.andremion.counterfab.CounterFab
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dmax.dialog.SpotsDialog
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import taipt4.kotlin.eatitv2.common.Common
import taipt4.kotlin.eatitv2.database.CartDataSource
import taipt4.kotlin.eatitv2.database.CartDatabase
import taipt4.kotlin.eatitv2.database.LocalCartDataSource
import taipt4.kotlin.eatitv2.eventbus.*
import taipt4.kotlin.eatitv2.model.Category
import taipt4.kotlin.eatitv2.model.Food

class HomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var cartDataSource: CartDataSource
    private lateinit var navController: NavController
    private lateinit var drawer: DrawerLayout

    private lateinit var fab: CounterFab
    private lateinit var dialog: android.app.AlertDialog

    private var menuItemClick = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()

        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(this).cartDAO())

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        fab = findViewById(R.id.fab)
        fab.setOnClickListener {
            navController.navigate(R.id.nav_cart)
        }
        drawer = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_menu, R.id.nav_food_detail, R.id.nav_cart
            ), drawer
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val headerView = navView.getHeaderView(0)
        val txtUser = headerView.findViewById<TextView>(R.id.header_txt_user)
        Common.setSpanString("Hey, ", Common.currentUser!!.name, txtUser)

        navView.setNavigationItemSelectedListener { item ->
            item.isChecked = true
            drawer.closeDrawers()
            when (item.itemId) {
                R.id.nav_sign_out -> signOut()
                R.id.nav_home -> {
                    if (menuItemClick != item.itemId)
                        navController.navigate(R.id.nav_home)
                }
                R.id.nav_cart -> {
                    if (menuItemClick != item.itemId)
                        navController.navigate(R.id.nav_cart)
                }
                R.id.nav_menu -> {
                    if (menuItemClick != item.itemId)
                        navController.navigate(R.id.nav_menu)
                }
                R.id.nav_view_order -> {
                    if (menuItemClick != item.itemId)
                        navController.navigate(R.id.nav_view_order)
                }
            }
            menuItemClick = item.itemId
            true
        }

        countCartItem()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onResume() {
        super.onResume()
        countCartItem()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onCategorySelected(event: CategoryClick) {
        if (event.isSuccess) {
//            Toast.makeText(this, "Click to " + event.category.name, Toast.LENGTH_SHORT).show()
            findNavController(R.id.nav_host_fragment).navigate(R.id.nav_food_list)
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onFoodSelected(event: FoodItemClick) {
        if (event.isSuccess) {
//            Toast.makeText(this, "Click to " + event.category.name, Toast.LENGTH_SHORT).show()
            findNavController(R.id.nav_host_fragment).navigate(R.id.nav_food_detail)
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onCountCartEvent(event: CountCartEvent) {
        if (event.isSuccess) {
            countCartItem()
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onHideFabEvent(event: HideFabCart) {
        if (event.isHide) {
            fab.hide()
        } else {
            fab.show()
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onPopularFoodItemClick(event: PopularFoodItemClick) {
        dialog.show()
        FirebaseDatabase.getInstance()
            .getReference("Category")
            .child(event.popularCategory.menu_id!!)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Common.selectedCategory = snapshot.getValue(Category::class.java)
                        Common.selectedCategory!!.menu_id = snapshot.key
                        // Load food
                        loadFood(event)
                    } else {
                        dialog.dismiss()
                        Toast.makeText(this@HomeActivity, "Item doesn't exist!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    dialog.dismiss()
                    Toast.makeText(this@HomeActivity, error.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onBestDealItemClick(event: BestDealItemClick) {
        dialog.show()
        FirebaseDatabase.getInstance()
            .getReference("Category")
            .child(event.bestDeal.menu_id!!)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Common.selectedCategory = snapshot.getValue(Category::class.java)
                        Common.selectedCategory!!.menu_id = snapshot.key
                        // Load best deal
                        loadBestDeal(event)
                    } else {
                        dialog.dismiss()
                        Toast.makeText(this@HomeActivity, "Item doesn't exist!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    dialog.dismiss()
                    Toast.makeText(this@HomeActivity, error.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMenuItemBack(event: MenuItemBack){
        menuItemClick = -1
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        }
    }

    private fun loadBestDeal(event: BestDealItemClick) {
        FirebaseDatabase.getInstance()
            .getReference("Category")
            .child(event.bestDeal.menu_id!!)
            .child("foods")
            .orderByChild("id")
            .equalTo(event.bestDeal.food_id)
            .limitToLast(1)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (food in snapshot.children) {
                            Common.selectedFood = food.getValue(Food::class.java)
                            Common.selectedFood!!.key = food.key
                        }
                        navController.navigate(R.id.nav_food_detail)
                    } else {
                        Toast.makeText(this@HomeActivity, "Item doesn't exist!", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }

                override fun onCancelled(error: DatabaseError) {
                    dialog.dismiss()
                    Toast.makeText(this@HomeActivity, error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadFood(event: PopularFoodItemClick) {
        FirebaseDatabase.getInstance()
            .getReference("Category")
            .child(event.popularCategory.menu_id!!)
            .child("foods")
            .orderByChild("id")
            .equalTo(event.popularCategory.food_id)
            .limitToLast(1)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (food in snapshot.children) {
                            Common.selectedFood = food.getValue(Food::class.java)
                            Common.selectedFood!!.key = food.key
                        }
                        navController.navigate(R.id.nav_food_detail)
                    } else {
                        Toast.makeText(this@HomeActivity, "Item doesn't exist!", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }

                override fun onCancelled(error: DatabaseError) {
                    dialog.dismiss()
                    Toast.makeText(this@HomeActivity, error.message, Toast.LENGTH_SHORT).show()
                }

            })
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
                Common.currentUser = null
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                dialog.dismiss()
                finish()
            }
        builder.create().show()
    }

    private fun countCartItem() {
        cartDataSource.countItemInCart(Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Int> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(t: Int) {
                    fab.count = t
                }

                override fun onError(e: Throwable) {
                    if (!e.message!!.contains("Query returned empty"))
                        Toast.makeText(this@HomeActivity, "[COUNT CART] ${e.message}", Toast.LENGTH_SHORT).show()
                    else
                        fab.count = 0
                }

            })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }
}