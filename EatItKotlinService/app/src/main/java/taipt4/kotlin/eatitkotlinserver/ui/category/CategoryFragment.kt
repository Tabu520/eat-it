package taipt4.kotlin.eatitkotlinserver.ui.category

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
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
import taipt4.kotlin.eatitkotlinserver.adapter.CategoryAdapter
import taipt4.kotlin.eatitkotlinserver.callback.IButtonCallback
import taipt4.kotlin.eatitkotlinserver.common.Common
import taipt4.kotlin.eatitkotlinserver.common.SwipeHelper
import taipt4.kotlin.eatitkotlinserver.databinding.FragmentCategoryBinding
import taipt4.kotlin.eatitkotlinserver.eventbus.ToastEvent
import taipt4.kotlin.eatitkotlinserver.model.Category
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CategoryFragment : Fragment() {

    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var dialog: AlertDialog
    private lateinit var layoutAnimationController: LayoutAnimationController
    private var adapter: CategoryAdapter? = null

    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private var imageUri: Uri? = null
    private lateinit var categoryImage: ImageView

    internal var listCategory: List<Category> = ArrayList()

    private lateinit var recyclerView: RecyclerView

    private var _binding: FragmentCategoryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        categoryViewModel =
            ViewModelProvider(this).get(CategoryViewModel::class.java)

        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        val root: View = binding.root
        initView(root)

        categoryViewModel.getErrorMessage().observe(viewLifecycleOwner, {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        })

        categoryViewModel.getCategoryList().observe(viewLifecycleOwner, {
            dialog.dismiss()
            listCategory = it
            adapter = CategoryAdapter(requireContext(), listCategory)
            recyclerView.adapter = adapter
            recyclerView.layoutAnimation = layoutAnimationController
        })
        return root
    }

    private fun initView(root: View) {
        Log.d("TaiPT4", "CategoryFragment --- #initViews")
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        dialog = SpotsDialog.Builder().setContext(context).setCancelable(false).build()
        dialog.show()

        recyclerView = root.findViewById(R.id.recycler_view_menu)

        layoutAnimationController =
            AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))

        val swipe = object : SwipeHelper(requireContext(), recyclerView, 200) {
            override fun instantiateCustomButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<CustomButton>
            ) {
                buffer.add(
                    CustomButton(
                        requireContext(),
                        "Update",
                        35,
                        0,
                        Color.parseColor("#560027"),
                        object : IButtonCallback {
                            override fun onClick(pos: Int) {
                                Common.selectedCategory = listCategory[pos]
                                showUpdateDialog()
                            }
                        })
                )
            }

        }
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data != null && result.data!!.data != null) {
                    imageUri = result.data!!.data
                    categoryImage.setImageURI(imageUri)
                }
            }
        }

    private fun showUpdateDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Update Category")
        builder.setMessage("Please fill information")

        val itemView =
            LayoutInflater.from(requireContext()).inflate(R.layout.layout_update_category, null)
        val edtCategoryName =
            itemView.findViewById(R.id.update_category_category_name) as TextInputEditText
        categoryImage = itemView.findViewById(R.id.update_category_category_image) as ImageView

        edtCategoryName.setText(Common.selectedCategory!!.name)
        Glide.with(requireContext()).load(Common.selectedCategory!!.image).into(categoryImage)

        categoryImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startForResult.launch(Intent.createChooser(intent, "Select Picture"))
        }

        builder.setNegativeButton("CANCEL") { dialogInterface, _ -> dialogInterface.dismiss() }
            .setPositiveButton("UPDATE") { dialogInterface, _ ->
                val updateData = HashMap<String, Any>()
                updateData["name"] = edtCategoryName.text.toString()
                if (imageUri != null) {
                    Log.d("TaiPT4", imageUri.toString())
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
                                updateData["image"] = uri.toString()
                                updateCategoryData(updateData)
                            }
                        }
                } else {
                    updateCategoryData(updateData)
                }
            }

        builder.setView(itemView).create().show()
    }

    private fun updateCategoryData(updateData: HashMap<String, Any>) {
        FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF)
            .child(Common.selectedCategory!!.menu_id!!)
            .updateChildren(updateData)
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener { task ->
                categoryViewModel.loadCategory()
                EventBus.getDefault().postSticky(ToastEvent(true, false))
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}