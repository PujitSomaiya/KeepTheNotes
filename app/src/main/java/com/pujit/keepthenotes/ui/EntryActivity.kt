package com.pujit.keepthenotes.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.room.Room
import com.pujit.keepthenotes.R
import com.pujit.keepthenotes.adapter.EntryViewAdapter
import com.pujit.keepthenotes.database.EntryData
import com.pujit.keepthenotes.database.MyDatabase
import com.pujit.keepthenotes.databinding.ActivityEntryBinding
import com.pujit.keepthenotes.interfaces.OnClickListener


class EntryActivity : AppCompatActivity(), OnClickListener {

    lateinit var binding: ActivityEntryBinding
    lateinit var database: MyDatabase
    lateinit var entryViewAdapter: EntryViewAdapter
    val NEW_TODO_REQUEST_CODE = 200
    val UPDATE_TODO_REQUEST_CODE = 300
    lateinit var entryDataList: List<EntryData>
    var isSearching = false
    var isAscending = false
    private var edNewPasscode: EditText? = null

    companion object{
        var isDarkTheme = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_entry)

        initViews()
        binding.fab.setOnClickListener {
            startActivityForResult(
                Intent(this, AddUpdateActivity::class.java),
                NEW_TODO_REQUEST_CODE
            )
        }

        loadAllEntries()
        onClick()
        searchNotes()
        checkTheme()
    }

    private fun checkTheme() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        isDarkTheme = sharedPreferences.getBoolean("isDarkTheme", true)
        refreshThemeUi(isDarkTheme)
    }

    private fun refreshThemeUi(darkTheme: Boolean) {
        if (darkTheme){
            binding.rlMain.setBackgroundColor(ContextCompat.getColor(this@EntryActivity,R.color.color_main_252525))
            binding.tvEntries.setTextColor(ContextCompat.getColor(this@EntryActivity,R.color.white))
            binding.llTheme.background= ContextCompat.getDrawable(this@EntryActivity,R.drawable.bg_icons)
            binding.llSearch.background= ContextCompat.getDrawable(this@EntryActivity,R.drawable.bg_icons)
            binding.btnSort.background= ContextCompat.getDrawable(this@EntryActivity,R.drawable.bg_icons)
            binding.imgTheme.setColorFilter(ContextCompat.getColor(this@EntryActivity, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
            binding.imgSearch.setColorFilter(ContextCompat.getColor(this@EntryActivity, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
            binding.imgSorting.setColorFilter(ContextCompat.getColor(this@EntryActivity, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
            binding.edSearch.setTextColor(ContextCompat.getColor(this@EntryActivity,R.color.white))
            binding.tvNoEntries.setTextColor(ContextCompat.getColor(this@EntryActivity,R.color.white))
            statusBarColor(R.color.color_main_252525)
            entryViewAdapter.refreshTheme(isDarkTheme)
        } else {
            binding.rlMain.setBackgroundColor(ContextCompat.getColor(this@EntryActivity,R.color.white))
            binding.tvEntries.setTextColor(ContextCompat.getColor(this@EntryActivity,R.color.color_main_252525))
            binding.llTheme.background= ContextCompat.getDrawable(this@EntryActivity,R.drawable.bg_icons_light)
            binding.llSearch.background= ContextCompat.getDrawable(this@EntryActivity,R.drawable.bg_icons_light)
            binding.btnSort.background= ContextCompat.getDrawable(this@EntryActivity,R.drawable.bg_icons_light)
            binding.imgTheme.setColorFilter(ContextCompat.getColor(this@EntryActivity, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY);
            binding.imgSearch.setColorFilter(ContextCompat.getColor(this@EntryActivity, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY);
            binding.imgSorting.setColorFilter(ContextCompat.getColor(this@EntryActivity, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY);
            binding.edSearch.setTextColor(ContextCompat.getColor(this@EntryActivity,R.color.black))
            binding.tvNoEntries.setTextColor(ContextCompat.getColor(this@EntryActivity,R.color.black))
            statusBarColor(R.color.white,true)
            entryViewAdapter.refreshTheme(isDarkTheme)
        }

    }

    private fun onClick() {
        binding.llSearch.setOnClickListener {
            resetHeader()
        }

        binding.llTheme.setOnClickListener {
            isDarkTheme = !isDarkTheme
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            sharedPreferences.edit().putBoolean("isDarkTheme", isDarkTheme).apply()
            refreshThemeUi(isDarkTheme)
        }

        binding.btnSort.setOnClickListener {
            isAscending = !isAscending

            if (isAscending){
                binding.imgSorting.setImageDrawable(ContextCompat.getDrawable(this@EntryActivity,R.drawable.ic_ascending))
            }else{
                binding.imgSorting.setImageDrawable(ContextCompat.getDrawable(this@EntryActivity,R.drawable.ic_descending))
            }

            if (isDarkTheme){
                binding.imgSorting.setColorFilter(ContextCompat.getColor(this@EntryActivity, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
            }else{
                binding.imgSorting.setColorFilter(ContextCompat.getColor(this@EntryActivity, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY);
            }
            entryViewAdapter.updateEntryList(entryDataList,this@EntryActivity,isAscending)
        }
    }

    private fun resetHeader() {
        if (isSearching) {
            binding.imgSearch.setImageDrawable(
                ContextCompat.getDrawable(
                    this@EntryActivity,
                    R.drawable.ic_cancel
                )
            )
            binding.edSearch.visibility = View.VISIBLE
            binding.tvEntries.visibility = View.GONE
            binding.llTheme.visibility = View.GONE
            binding.btnSort.visibility = View.GONE
            isSearching = false
            binding.edSearch.requestFocus()
        } else {
            binding.imgSearch.setImageDrawable(
                ContextCompat.getDrawable(
                    this@EntryActivity,
                    R.drawable.ic_search
                )
            )
            binding.edSearch.visibility = View.GONE
            binding.tvEntries.visibility = View.VISIBLE
            binding.llTheme.visibility = View.VISIBLE
            binding.btnSort.visibility = View.VISIBLE
            isSearching = true
            binding.edSearch.clearFocus()
            binding.edSearch.text?.clear()
            closeKeyboard(this@EntryActivity)

        }
    }

    private fun searchNotes() {
        binding.edSearch.addTextChangedListener(object :
            TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length!! > 0) {
                    entryViewAdapter.filter.filter(s.toString())
                    //                    selectCountry?.setData(filter(s.toString()))
                } else {
                    if (::entryDataList.isInitialized)
                    entryViewAdapter.updateEntryList(
                        entryDataList,this@EntryActivity,isAscending
                    )
                }
            }
        })
    }

    private fun checkCount() {
        if (entryViewAdapter.itemCount != 0) {
            binding.recyclerView.visibility = View.VISIBLE
            binding.btnSort.visibility = View.VISIBLE
            binding.tvEntries.visibility = View.VISIBLE
            binding.tvNoEntries.visibility = View.GONE
            binding.llSearch.visibility = View.VISIBLE
        } else {
            binding.llSearch.visibility = View.GONE
            binding.recyclerView.visibility = View.GONE
            binding.btnSort.visibility = View.GONE
            binding.tvEntries.visibility = View.VISIBLE
            binding.tvNoEntries.visibility = View.VISIBLE
            binding.tvNoEntries.text = this@EntryActivity.getString(R.string.no_entries_found)
        }
    }

    private fun initViews() {

        database =
            Room.databaseBuilder(this@EntryActivity, MyDatabase::class.java, MyDatabase.DB_NAME)
                .fallbackToDestructiveMigration().build()

        entryViewAdapter = EntryViewAdapter(this@EntryActivity,database, isDarkTheme,this@EntryActivity) {
            if (it.isPrivate){
                unlockPasscode(it)
            }else{
                binding.edSearch.text?.clear()
                isSearching = false
                resetHeader()
                startActivity(Intent(this, NotesViewActivity::class.java).putExtra("id", it.entry_id))
            }

        }

        val staggeredGridLayoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        binding.recyclerView.layoutManager = staggeredGridLayoutManager

        binding.recyclerView.adapter = entryViewAdapter
    }

    private fun unlockPasscode(entryData:EntryData){
        val builder = AlertDialog.Builder(this@EntryActivity)
        val inflater = layoutInflater
        val view: View = inflater.inflate(R.layout.dialog_enter_lock, null)
        builder.setView(view)

        val show = builder.setCancelable(true).show()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val passcode = sharedPreferences.getInt("passcode", 0)


        edNewPasscode = view.findViewById<EditText>(R.id.edNewPasscode)

        view.findViewById<View>(R.id.btnSubmit).setOnClickListener { //dismiss dialog
            edNewPasscode!!.error = null
            if (edNewPasscode!!.text.toString().trim().length!=4){
                edNewPasscode!!.error = "Please enter 4 digit passcode"
            }else if (edNewPasscode!!.text.toString().trim().toInt()!=passcode){
                edNewPasscode!!.error = "Wrong passcode"
            }else{
                show.dismiss()
                binding.edSearch.text?.clear()
                isSearching = false
                resetHeader()
                startActivity(Intent(this, NotesViewActivity::class.java).putExtra("id", entryData.entry_id))
            }
        }
        view.findViewById<View>(R.id.btnCancel).setOnClickListener { //dismiss dialog
            show.dismiss()
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

    @SuppressLint("StaticFieldLeak")
    private fun fetchEntryByIdAndInsert(id: Int) {
        object : AsyncTask<Int, Void, EntryData>() {
            override fun onPostExecute(entryData: EntryData?) {
                entryViewAdapter.addRow(entryData!!)
                checkCount()
            }

            override fun doInBackground(vararg params: Int?): EntryData {
                return database.daoAccess()!!.fetchEntryListById(params[0]!!)
            }
        }.execute(id)
    }


    @SuppressLint("StaticFieldLeak")
    public fun loadAllEntries() {
        closeKeyboard(this@EntryActivity)
        object : AsyncTask<String, Void, List<EntryData>>() {
            override fun doInBackground(vararg params: String): List<EntryData> {
                return database.daoAccess()!!.fetchAllEntries()
            }

            override fun onPostExecute(entryDataList: List<EntryData>) {
                this@EntryActivity.entryDataList = entryDataList
                entryViewAdapter.updateEntryList(entryDataList,this@EntryActivity,isAscending)
                binding.edSearch.text?.clear()
                isSearching = false
                resetHeader()
                checkCount()
            }
        }.execute()
    }

    fun closeKeyboard(context: Context) {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(
            InputMethodManager.HIDE_IMPLICIT_ONLY,
            0
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode === RESULT_OK) {
            if (requestCode === NEW_TODO_REQUEST_CODE) {
                val id: Long = data!!.getLongExtra("id", -1)
                /*Toast.makeText(applicationContext, "Entry inserted", Toast.LENGTH_SHORT).show()*/
                fetchEntryByIdAndInsert(id.toInt())
            } else if (requestCode === UPDATE_TODO_REQUEST_CODE) {
                val isDeleted: Boolean = data!!.getBooleanExtra("isDeleted", false)
                val number: Int = data.getIntExtra("number", -1)
                if (isDeleted) {
                    /*Toast.makeText(applicationContext, "$number entry deleted", Toast.LENGTH_SHORT)
                        .show()*/
                } else {
                    /*Toast.makeText(applicationContext, "$number entry updated", Toast.LENGTH_SHORT)
                        .show()*/
                }
                loadAllEntries()
            }
            checkCount()
        } else {
            /*Toast.makeText(applicationContext, "No action done by user", Toast.LENGTH_SHORT).show()*/
        }

    }

    @SuppressLint("SetTextI18n")
    fun showNoData(
        context: Context,
        rv: RecyclerView,
        noDataView: TextView,
        isNullOrEmpty: Boolean,
        searchString: String?
    ) {
        if (isNullOrEmpty) {
            rv.visibility = View.GONE
            noDataView.visibility = View.VISIBLE
            noDataView.text =
                "${context.resources.getString(R.string.no_search_result_found)} \"$searchString\""
        } else {
            rv.visibility = View.VISIBLE
            noDataView.visibility = View.GONE
            noDataView.text = ""
        }
    }



    override fun onResume() {
        super.onResume()
        loadAllEntries()
    }

    override fun onSearch(size: Int, searchString: String) {
        showNoData(this@EntryActivity,binding.recyclerView,binding.tvNoEntries,size <=0,searchString)
    }

}