package taipt4.kotlin.eatitkotlinserver

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.FirebaseDatabase
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import taipt4.kotlin.eatitkotlinserver.adapter.AddonAdapter
import taipt4.kotlin.eatitkotlinserver.adapter.SizeAdapter
import taipt4.kotlin.eatitkotlinserver.common.Common
import taipt4.kotlin.eatitkotlinserver.databinding.ActivitySizeAddonEditBinding
import taipt4.kotlin.eatitkotlinserver.eventbus.AddonSizeEditEvent
import taipt4.kotlin.eatitkotlinserver.model.*

class SizeAddonEditActivity : AppCompatActivity() {

    private var _binding: ActivitySizeAddonEditBinding? = null
    private val binding get() = _binding!!

    private var sizeAdapter: SizeAdapter? = null
    private var addonAdapter: AddonAdapter? = null
    private var editedFoodPosition: Int = -1
    private var needSave = false
    private var isAddon = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySizeAddonEditBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        init()
    }

    private fun init() {
        setSupportActionBar(binding.addonSizeToolBar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        binding.addonSizeRecyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        binding.addonSizeRecyclerView.layoutManager = layoutManager
        binding.addonSizeRecyclerView.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))

        //Event
        binding.addonSizeBtnCreate.setOnClickListener {
            if (!isAddon) //Size
            {
                if (sizeAdapter != null) {
                    val sizeModel = SizeModel()
                    sizeModel.name = binding.edtAddonSizeName.text.toString()
                    sizeModel.price = binding.edtAddonSizePrice.text.toString().toLong()
                    sizeAdapter!!.addNewSize(sizeModel)
                }
            } else // Addon
            {
                if (addonAdapter != null) {
                    val addonModel = AddonModel()
                    addonModel.name = binding.edtAddonSizeName.text.toString()
                    addonModel.price = binding.edtAddonSizePrice.text.toString().toLong()
                    addonAdapter!!.addNewAddon(addonModel)
                }
            }
        }
        binding.addonSizeBtnEdit.setOnClickListener {
            if (!isAddon) //Size
            {
                if (sizeAdapter != null) {
                    val sizeModel = SizeModel()
                    sizeModel.name = binding.edtAddonSizeName.text.toString()
                    sizeModel.price = binding.edtAddonSizePrice.text.toString().toLong()
                    sizeAdapter!!.editSize(sizeModel)
                }
            } else // Addon
            {
                if (addonAdapter != null) {
                    val addonModel = AddonModel()
                    addonModel.name = binding.edtAddonSizeName.text.toString()
                    addonModel.price = binding.edtAddonSizePrice.text.toString().toLong()
                    addonAdapter!!.editAddon(addonModel)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_size_addon, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_save -> saveData()
            android.R.id.home -> {
                if (needSave) {
                    val builder = AlertDialog.Builder(this)
                        .setTitle("Cancel")
                        .setMessage("Do you really want to close without saving?")
                        .setNegativeButton("CANCEL") { dialogInterface, _ ->
                            dialogInterface.dismiss()
                        }
                        .setPositiveButton("OK") { dialogInterface, i ->
                            needSave = false
                            closeActivity()
                        }
                        .create().show()
                } else {
                    closeActivity()
                }
            }
        }
        return true
    }

    private fun saveData() {
        if (editedFoodPosition != -1) {
            Common.selectedCategory!!.foods!![editedFoodPosition] = Common.selectedFood!!
            val updateData: MutableMap<String, Any> = HashMap()
            updateData["foods"] = Common.selectedCategory!!.foods!!
            FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child(Common.selectedCategory!!.menu_id!!)
                .updateChildren(updateData)
                .addOnFailureListener { e ->
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Reload success", Toast.LENGTH_SHORT).show()
                        needSave = false
                        binding.edtAddonSizeName.setText("")
                        binding.edtAddonSizePrice.setText("0")
                    }
                }
        }
    }

    private fun closeActivity() {
        binding.edtAddonSizeName.setText("")
        binding.edtAddonSizePrice.setText("0")
        finish()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onAddonSizeReceive(event: AddonSizeEditEvent) {
        if (!event.isAddon) // Size
        {
            if (Common.selectedFood!!.size != null) {
                sizeAdapter = SizeAdapter(this, Common.selectedFood!!.size!!.toMutableList())
                editedFoodPosition = event.pos
                binding.addonSizeRecyclerView.adapter = sizeAdapter
                isAddon = event.isAddon
            }
        } else // Addon
        {
            if (Common.selectedFood!!.addon != null) {
                addonAdapter = AddonAdapter(this, Common.selectedFood!!.addon!!.toMutableList())
                editedFoodPosition = event.pos
                binding.addonSizeRecyclerView.adapter = addonAdapter
                isAddon = event.isAddon
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onSizeModelUpdate(event: UpdateSizeModel) {
        if (event.listSizeModel != null) {
            needSave = true
            Common.selectedFood!!.size = event.listSizeModel
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onAddonModelUpdate(event: UpdateAddonModel) {
        if (event.listAddonModel != null) {
            needSave = true
            Common.selectedFood!!.addon = event.listAddonModel
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onSelectSizeEvent(event: SelectSizeModel) {
        if (event.sizeModel != null) {
            binding.edtAddonSizeName.setText(event.sizeModel.name)
            binding.edtAddonSizePrice.setText(event.sizeModel.price.toString())
            binding.addonSizeBtnEdit.isEnabled = true
        } else {
            binding.addonSizeBtnEdit.isEnabled = false
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onSelectAddonEvent(event: SelectAddonModel) {
        if (event.addonModel != null) {
            binding.edtAddonSizeName.setText(event.addonModel.name)
            binding.edtAddonSizePrice.setText(event.addonModel.price.toString())
            binding.addonSizeBtnEdit.isEnabled = true
        } else {
            binding.addonSizeBtnEdit.isEnabled = false
        }
    }

    override fun onStop() {
        EventBus.getDefault().removeStickyEvent(UpdateSizeModel::class.java)
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}