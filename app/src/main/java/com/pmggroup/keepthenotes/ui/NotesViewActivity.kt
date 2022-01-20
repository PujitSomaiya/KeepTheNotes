package com.pmggroup.keepthenotes.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.room.Room
import com.pmggroup.keepthenotes.R
import com.pmggroup.keepthenotes.database.EntryData
import com.pmggroup.keepthenotes.database.MyDatabase
import com.pmggroup.keepthenotes.databinding.ActivityNotesViewBinding
import java.util.concurrent.Executors


class NotesViewActivity : AppCompatActivity() {

    lateinit var binding: ActivityNotesViewBinding
    lateinit var databse: MyDatabase
    val UPDATE_TODO_REQUEST_CODE = 300
    var entryData: EntryData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notes_view)

        checkTheme()

        initViews()
        onClicks()
    }

    private fun checkTheme() {
        if (EntryActivity.isDarkTheme){
            binding.clMain.setBackgroundColor(ContextCompat.getColor(this@NotesViewActivity,R.color.color_main_252525))
            binding.llBack.background= ContextCompat.getDrawable(this@NotesViewActivity,R.drawable.bg_icons)
            binding.llEdit.background= ContextCompat.getDrawable(this@NotesViewActivity,R.drawable.bg_icons)
            binding.llDelete.background= ContextCompat.getDrawable(this@NotesViewActivity,R.drawable.bg_icons)
            binding.llShare.background= ContextCompat.getDrawable(this@NotesViewActivity,R.drawable.bg_icons)
            binding.imgBack.setColorFilter(ContextCompat.getColor(this@NotesViewActivity, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
            binding.imgEdit.setColorFilter(ContextCompat.getColor(this@NotesViewActivity, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
            binding.imgShare.setColorFilter(ContextCompat.getColor(this@NotesViewActivity, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
            binding.imgDelete.setColorFilter(ContextCompat.getColor(this@NotesViewActivity, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
            binding.tvTitle.setTextColor(ContextCompat.getColor(this@NotesViewActivity,R.color.white))
            binding.tvLastUpdateDate.setTextColor(ContextCompat.getColor(this@NotesViewActivity,R.color.white))
            binding.tvDescription.setTextColor(ContextCompat.getColor(this@NotesViewActivity,R.color.white))
            statusBarColor(R.color.color_main_252525)

        } else {
            binding.clMain.setBackgroundColor(ContextCompat.getColor(this@NotesViewActivity,R.color.white))
            binding.llBack.background= ContextCompat.getDrawable(this@NotesViewActivity,R.drawable.bg_icons_light)
            binding.llEdit.background= ContextCompat.getDrawable(this@NotesViewActivity,R.drawable.bg_icons_light)
            binding.llDelete.background= ContextCompat.getDrawable(this@NotesViewActivity,R.drawable.bg_icons_light)
            binding.llShare.background= ContextCompat.getDrawable(this@NotesViewActivity,R.drawable.bg_icons_light)
            binding.imgBack.setColorFilter(ContextCompat.getColor(this@NotesViewActivity, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY);
            binding.imgEdit.setColorFilter(ContextCompat.getColor(this@NotesViewActivity, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY);
            binding.imgShare.setColorFilter(ContextCompat.getColor(this@NotesViewActivity, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY);
            binding.imgDelete.setColorFilter(ContextCompat.getColor(this@NotesViewActivity, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY);
            binding.tvTitle.setTextColor(ContextCompat.getColor(this@NotesViewActivity,R.color.black))
            binding.tvLastUpdateDate.setTextColor(ContextCompat.getColor(this@NotesViewActivity,R.color.black))
            binding.tvDescription.setTextColor(ContextCompat.getColor(this@NotesViewActivity,R.color.black))
            statusBarColor(R.color.white,true)
        }
    }

    private fun onClicks() {
        binding.llBack.setOnClickListener { onBackPressed() }
        binding.llEdit.setOnClickListener {
            startActivityForResult(
                Intent(this, AddUpdateActivity::class.java).putExtra("id", entryData?.entry_id),
                UPDATE_TODO_REQUEST_CODE
            )
        }

       binding.llDelete.setOnClickListener {
           openAlterDialog()
       }

       binding.llShare.setOnClickListener {
           shareNote(entryData?.title!!,entryData?.description!!)
       }
    }

    private fun openAlterDialog() {
        AlertDialog.Builder(this@NotesViewActivity)
            .setCancelable(false)
            .setMessage("Are you want to delete?")
            .setCancelable(true)
            .setPositiveButton("Yes") { arg0, arg1 ->
                deleteEntry(entryData!!)
            }
            .setNegativeButton(
                "No"
            ) { dialog, which -> //dismiss dialog

            }.create().show()

    }

    private fun shareNote(title:String,body:String){
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(
            Intent.EXTRA_SUBJECT, "Note"
        )
        intent.putExtra(Intent.EXTRA_TEXT, title+"\n\n"+body)
        startActivity(Intent.createChooser(intent, "Share Using"))
    }

    private fun deleteEntry(entryData: EntryData){
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            databse.daoAccess()!!.deleteEntry(entryData)
            handler.post {
               onBackPressed()
            }
        }
    }

    private fun initViews() {
        val entry_id = intent.getIntExtra("id", -100)
        databse =
            Room.databaseBuilder(applicationContext, MyDatabase::class.java, MyDatabase.DB_NAME)
                .build()
        fetchEntryById(entry_id)
    }

    @SuppressLint("StaticFieldLeak")
    private fun fetchEntryById(todo_id: Int) {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            entryData = databse.daoAccess()!!.fetchEntryListById(todo_id)
            handler.post {
                binding.tvDescription.text = entryData!!.description
                binding.tvTitle.text = entryData!!.title
                binding.tvLastUpdateDate.text = entryData!!.lastUpdatedDate
            }
        }

    }

    @SuppressLint("NewApi")
    fun statusBarColor(id: Int) {
        window.decorView.systemUiVisibility = 0
        window.statusBarColor = ContextCompat.getColor(this, id)
    }

    @SuppressLint("NewApi")
    fun statusBarColor(id: Int, boolean: Boolean) {
        if (boolean) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = ContextCompat.getColor(this, id)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode === RESULT_OK) {
            if (requestCode === UPDATE_TODO_REQUEST_CODE) {
                val isDeleted: Boolean = data!!.getBooleanExtra("isDeleted", false)
                val number: Int = data.getIntExtra("number", -1)
                if (isDeleted) {
                    Toast.makeText(applicationContext, "$number note deleted", Toast.LENGTH_SHORT)
                        .show()
                    onBackPressed()
                } else {
                    Toast.makeText(applicationContext, "Note updated", Toast.LENGTH_SHORT)
                        .show()
                    fetchEntryById(entryData?.entry_id!!)
                }

            }
        }
    }
}