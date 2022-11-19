package taipt4.kotlin.eatitkotlinserver.ui.foodlist

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dmax.dialog.SpotsDialog
import org.greenrobot.eventbus.EventBus
import taipt4.kotlin.eatitkotlinserver.R
import taipt4.kotlin.eatitkotlinserver.SizeAddonEditActivity
import taipt4.kotlin.eatitkotlinserver.adapter.FoodListAdapter
import taipt4.kotlin.eatitkotlinserver.callback.IButtonCallback
import taipt4.kotlin.eatitkotlinserver.common.Common
import taipt4.kotlin.eatitkotlinserver.common.SwipeHelper
import taipt4.kotlin.eatitkotlinserver.databinding.FragmentFoodListBinding
import taipt4.kotlin.eatitkotlinserver.eventbus.AddonSizeEditEvent
import taipt4.kotlin.eatitkotlinserver.eventbus.ChangeMenuClick
import taipt4.kotlin.eatitkotlinserver.eventbus.ToastEvent
import taipt4.kotlin.eatitkotlinserver.model.Food
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FoodListFragment : Fragment() {

    private lateinit var foodListViewModel: FoodListViewModel
    private lateinit var recyclerFoodList: RecyclerView
    private lateinit var layoutAnimationController: LayoutAnimationController
    private var listFood: List<Food> = ArrayList()
    private var adapter: FoodListAdapter? = null
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private var imageUri: Uri? = null

    private var _binding: FragmentFoodListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var ivFoodImage: ImageView
    private lateinit var dialog: android.app.AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        foodListViewModel =
            ViewModelProvider(this).get(FoodListViewModel::class.java)

        _binding = FragmentFoodListBinding.inflate(inflater, container, false)
        val root: View = binding.root
        initView(root)

        foodListViewModel.getFoodModelList().observe(viewLifecycleOwner, {
            listFood = it
            adapter = FoodListAdapter(requireContext(), it)
            recyclerFoodList.adapter = adapter
            recyclerFoodList.layoutAnimation = layoutAnimationController
        })
        return root
    }

    private fun initView(root: View) {

        setHasOptionsMenu(true) // Enable options menu on Fragment

        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        dialog = SpotsDialog.Builder().setContext(requireContext()).setCancelable(false).build()

        recyclerFoodList = root.findViewById(R.id.recycler_food_list) as RecyclerView
        recyclerFoodList.setHasFixedSize(true)
        recyclerFoodList.layoutManager = LinearLayoutManager(context)

        layoutAnimationController =
            AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)

        (activity as AppCompatActivity).supportActionBar!!.title = Common.selectedCategory!!.name

        val displayMetrics = DisplayMetrics()
        var width: Int = 0
        var height: Int = 0
        val version =
            Build.VERSION.SDK_INT   //Check android version. Returns API # ie 29 (Android 10), 30 (Android 11)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {    //If API is version 30 (Android 11) or greater, use the new method for getting width/height
            requireActivity().display?.getRealMetrics(displayMetrics) //New method
            width = displayMetrics.widthPixels
            height = displayMetrics.heightPixels
        } else {
            @Suppress("DEPRECATION")    //Suppress the "Deprecation" warning
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            width = displayMetrics.widthPixels
            height = displayMetrics.heightPixels
        }

        object : SwipeHelper(requireContext(), recyclerFoodList, width / 6) {
            override fun instantiateCustomButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<CustomButton>
            ) {
                buffer.add(
                    CustomButton(
                        requireContext(),
                        "Delete",
                        35,
                        0,
                        Color.parseColor("#9b0000"),
                        object : IButtonCallback {
                            override fun onClick(pos: Int) {
                                Common.selectedFood = listFood[pos]
                                val builder = AlertDialog.Builder(requireContext())
                                builder.setTitle("Delete")
                                    .setMessage("Do you really want to delete this food?")
                                    .setNegativeButton("CANCEL") { dialogInterface, _ ->
                                        dialogInterface.dismiss()
                                    }
                                    .setPositiveButton("DELETE") { dialogInterface, _ ->
                                        val food = adapter!!.getItemAtPosition(pos)
                                        if (food.positionInList == -1)
                                            Common.selectedCategory!!.foods!!.removeAt(pos)
                                        else
                                            Common.selectedCategory!!.foods!!.removeAt(food.positionInList)
                                        updateFood(Common.selectedCategory!!.foods, true)
                                    }
                                builder.create().show()
                            }
                        })
                )
                buffer.add(
                    CustomButton(
                        requireContext(),
                        "Update",
                        35,
                        0,
                        Color.parseColor("#560027"),
                        object : IButtonCallback {
                            override fun onClick(pos: Int) {
                                val food = adapter!!.getItemAtPosition(pos)
                                if (food.positionInList == -1)
                                    showUpdateDialog(pos, food)
                                else
                                    showUpdateDialog(food.positionInList, food)
                            }
                        })
                )
                buffer.add(
                    CustomButton(
                        requireContext(),
                        "Size",
                        35,
                        0,
                        Color.parseColor("#12005e"),
                        object : IButtonCallback {
                            override fun onClick(pos: Int) {
                                val food = adapter!!.getItemAtPosition(pos)
                                if (food.positionInList == -1)
                                    Common.selectedFood = listFood[pos]
                                else
                                    Common.selectedFood = food
                                startActivity(Intent(requireContext(), SizeAddonEditActivity::class.java))
                                if (food.positionInList == -1)
                                    EventBus.getDefault().postSticky(AddonSizeEditEvent(false, pos))
                                else
                                    EventBus.getDefault().postSticky(AddonSizeEditEvent(false, food.positionInList))
                            }
                        })
                )
                buffer.add(
                    CustomButton(
                        requireContext(),
                        "Addon",
                        35,
                        0,
                        Color.parseColor("#333639"),
                        object : IButtonCallback {
                            override fun onClick(pos: Int) {
                                val food = adapter!!.getItemAtPosition(pos)
                                if (food.positionInList == -1)
                                    Common.selectedFood = listFood[pos]
                                else
                                    Common.selectedFood = food
                                startActivity(Intent(requireContext(), SizeAddonEditActivity::class.java))
                                if (food.positionInList == -1)
                                    EventBus.getDefault().postSticky(AddonSizeEditEvent(true, pos))
                                else
                                    EventBus.getDefault().postSticky(AddonSizeEditEvent(true, food.positionInList))
                            }
                        })
                )
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.food_list_menu, menu)

        // Create search view
        val menuItem = menu.findItem(R.id.action_search)
        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menuItem.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))

        //Event
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                startSearchFood(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        // Clear text when click Clear button
        val closeButton = searchView.findViewById<ImageView>(R.id.search_close_btn)
        closeButton.setOnClickListener {
            val edtSearch = searchView.findViewById<EditText>(R.id.search_src_text)
            // Clear text
            edtSearch.setText("")
            // Clear query
            searchView.setQuery("", false)
            // Collapse the action view
            searchView.onActionViewCollapsed()
            // Collapse the search widget
            menuItem.collapseActionView()
            // Restore result to original
            foodListViewModel.getFoodModelList().value = Common.selectedCategory!!.foods
        }
    }

    private fun startSearchFood(query: String?) {
        val resultFood: MutableList<Food> = ArrayList()
        for (i in Common.selectedCategory!!.foods!!.indices) {
            val food = Common.selectedCategory!!.foods!![i]
            if (food.name!!.lowercase().contains(query!!.lowercase())) {
                food.positionInList = i
                resultFood.add(food)
            }
        }
        // Update search result
        foodListViewModel.getFoodModelList().value = resultFood
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data != null && result.data!!.data != null) {
                    imageUri = result.data!!.data
                    ivFoodImage.setImageURI(imageUri)
                }
            }
        }

    private fun showUpdateDialog(pos: Int, food: Food) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Update")
            .setMessage("Please fill information")
        val itemView = LayoutInflater.from(context).inflate(R.layout.layout_update_food, null)
        val edtFoodName = itemView.findViewById<TextInputEditText>(R.id.update_food_food_name)
        val edtFoodPrice = itemView.findViewById<TextInputEditText>(R.id.update_food_food_price)
        val edtFoodDescription =
            itemView.findViewById<TextInputEditText>(R.id.update_food_food_description)
        ivFoodImage = itemView.findViewById(R.id.update_food_food_image)

        // Set data
        edtFoodName.setText(food.name)
        edtFoodPrice.setText(food.toString())
        edtFoodDescription.setText(food.description)
        Glide.with(requireContext()).load(food.image).into(ivFoodImage)

        // Set event
        ivFoodImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startForResult.launch(Intent.createChooser(intent, "Select Picture"))
        }

        builder.setNegativeButton("CANCEL") { dialogInterface, _ -> dialogInterface.dismiss() }
            .setPositiveButton("UPDATE") { dialogInterface, _ ->
                food.name = edtFoodName.text.toString()
                food.description = edtFoodDescription.text.toString()
                food.price =
                    if (TextUtils.isEmpty(edtFoodPrice.text.toString())) 0 else edtFoodPrice.text.toString()
                        .toLong()

                if (imageUri != null) {
                    dialog.setMessage("Uploading...")
                    dialog.show()

                    val imageName = UUID.randomUUID().toString()
                    val imageFolder = storageReference.child("images/$imageName")
                    Log.d("TaiPT4", imageFolder.toString())
                    imageFolder.putFile(imageUri!!)
                        .addOnFailureListener { e ->
                            dialog.dismiss()
                            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                        }
                        .addOnProgressListener { task ->
                            val progress = 100.0 * task.bytesTransferred / task.totalByteCount
                            dialog.setMessage("Uploaded $progress%")
                        }
                        .addOnSuccessListener {
                            dialogInterface.dismiss()
                            imageFolder.downloadUrl.addOnSuccessListener { uri ->
                                dialog.dismiss()
                                food.image = uri.toString()
                                Common.selectedCategory!!.foods!![pos] = food
                                updateFood(Common.selectedCategory!!.foods, false)
                            }
                        }
                } else {
                    Common.selectedCategory!!.foods!![pos] = food
                    updateFood(Common.selectedCategory!!.foods, false)
                }
            }
        builder.setView(itemView)
        builder.create().show()
    }

    private fun updateFood(foods: MutableList<Food>?, isDelete: Boolean) {
        val updateData = HashMap<String, Any>()
        updateData["foods"] = foods!!

        FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF)
            .child(Common.selectedCategory!!.menu_id!!)
            .updateChildren(updateData)
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    foodListViewModel.getFoodModelList()
                    EventBus.getDefault().postSticky(ToastEvent(!isDelete, true))
                }
            }
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(ChangeMenuClick(true))
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}