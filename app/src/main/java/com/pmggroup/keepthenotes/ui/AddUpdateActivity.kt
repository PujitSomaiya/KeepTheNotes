package com.pmggroup.keepthenotes.ui

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.room.Room
import com.pmggroup.keepthenotes.R
import com.pmggroup.keepthenotes.database.EntryData
import com.pmggroup.keepthenotes.database.MyDatabase
import com.pmggroup.keepthenotes.databinding.ActivityAddUpdateBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class AddUpdateActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    View.OnClickListener {

    lateinit var binding: ActivityAddUpdateBinding
    var isNewEntry = false
    var isPrivate = false
    var isPrivateProtected = false
    lateinit var databse: MyDatabase
    lateinit var updateEntryData: EntryData
    private var edNewPasscode: EditText? = null
    private var edConfirmNewPasscode: EditText? = null
    private val mRandom: Random = Random(System.currentTimeMillis());

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_update)

        checkTheme()
        initView()
        onClickListeners()
    }

    private fun checkTheme() {
        if (EntryActivity.isDarkTheme) {
            binding.clMain.setBackgroundColor(
                ContextCompat.getColor(
                    this@AddUpdateActivity,
                    R.color.color_main_252525
                )
            )
            binding.llBack.background =
                ContextCompat.getDrawable(this@AddUpdateActivity, R.drawable.bg_icons)
            binding.llPrivate.background =
                ContextCompat.getDrawable(this@AddUpdateActivity, R.drawable.bg_icons)
            binding.btnDone.background =
                ContextCompat.getDrawable(this@AddUpdateActivity, R.drawable.bg_icons)
            binding.imgBack.setColorFilter(
                ContextCompat.getColor(
                    this@AddUpdateActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            );
            binding.imgPrivate.setColorFilter(
                ContextCompat.getColor(
                    this@AddUpdateActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            );
            binding.tvDone.setTextColor(
                ContextCompat.getColor(
                    this@AddUpdateActivity,
                    R.color.white
                )
            );
            binding.contentTodo.edTitle.setTextColor(
                ContextCompat.getColor(
                    this@AddUpdateActivity,
                    R.color.white
                )
            )
            binding.contentTodo.edDescription.setTextColor(
                ContextCompat.getColor(
                    this@AddUpdateActivity,
                    R.color.white
                )
            )
            statusBarColor(R.color.color_main_252525)

        } else {
            binding.clMain.setBackgroundColor(
                ContextCompat.getColor(
                    this@AddUpdateActivity,
                    R.color.white
                )
            )
            binding.llBack.background =
                ContextCompat.getDrawable(this@AddUpdateActivity, R.drawable.bg_icons_light)
            binding.llPrivate.background =
                ContextCompat.getDrawable(this@AddUpdateActivity, R.drawable.bg_icons_light)
            binding.btnDone.background =
                ContextCompat.getDrawable(this@AddUpdateActivity, R.drawable.bg_icons_light)
            binding.imgBack.setColorFilter(
                ContextCompat.getColor(
                    this@AddUpdateActivity,
                    R.color.black
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            );
            binding.imgPrivate.setColorFilter(
                ContextCompat.getColor(
                    this@AddUpdateActivity,
                    R.color.black
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            );
            binding.tvDone.setTextColor(
                ContextCompat.getColor(
                    this@AddUpdateActivity,
                    R.color.black
                )
            );
            binding.contentTodo.edTitle.setTextColor(
                ContextCompat.getColor(
                    this@AddUpdateActivity,
                    R.color.black
                )
            )
            binding.contentTodo.edDescription.setTextColor(
                ContextCompat.getColor(
                    this@AddUpdateActivity,
                    R.color.black
                )
            )
            statusBarColor(R.color.white, true)
        }
    }

    private fun onClickListeners() {
        binding.btnDone.setOnClickListener(this)
        binding.llBack.setOnClickListener { onBackPressed() }
        binding.llPrivate.setOnClickListener {
            isPrivate = !isPrivate

            if (isPrivate) {
                binding.imgPrivate.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@AddUpdateActivity,
                        R.drawable.ic_lock
                    )
                )

            } else {
                binding.imgPrivate.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@AddUpdateActivity,
                        R.drawable.ic_unlock
                    )
                )
            }

            if (EntryActivity.isDarkTheme) {
                binding.imgPrivate.setColorFilter(
                    ContextCompat.getColor(
                        this@AddUpdateActivity,
                        R.color.white
                    ), android.graphics.PorterDuff.Mode.MULTIPLY
                );
            } else {
                binding.imgPrivate.setColorFilter(
                    ContextCompat.getColor(
                        this@AddUpdateActivity,
                        R.color.black
                    ), android.graphics.PorterDuff.Mode.MULTIPLY
                );
            }
        }

    }

    override fun onBackPressed() {
        closeKeyboard(this@AddUpdateActivity)
        super.onBackPressed()
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        isPrivateProtected = sharedPreferences.getBoolean("isPrivateProtected", false)

        databse =
            Room.databaseBuilder(applicationContext, MyDatabase::class.java, MyDatabase.DB_NAME)
                .build()

        val entry_id = intent.getIntExtra("id", -100)
        if (entry_id == -100) isNewEntry = true
        if (!isNewEntry) {
            fetchEntryById(entry_id)
            /*binding.contentTodo.btnDelete.visibility = View.VISIBLE*/
        }
        if (isNewEntry) {
            binding.tvDone.text = "Save"
        } else {
            binding.tvDone.text = "Update"
        }
        keyBoardCloseOnEditextTap(binding.contentTodo.edDescription, binding.root)
    }

    private fun addUpdateEntry() {
        if (binding.contentTodo.edTitle.text.toString().trim().isEmpty()) {
            Toast.makeText(this@AddUpdateActivity, "Title can't be empty", Toast.LENGTH_LONG).show()
        } else if (binding.contentTodo.edDescription.text.toString().trim().isEmpty()) {
            Toast.makeText(this@AddUpdateActivity, "Note can't be empty", Toast.LENGTH_LONG).show()
        } else {
            val c = Calendar.getInstance().time
            /*val calendar = Calendar.getInstance()
            calendar.time = c
            calendar.add(Calendar.DAY_OF_YEAR, +5)
            val newDate = calendar.time*/
            println("Current time => $c")

            val df = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val formattedDate = df.format(c)
            /*val formattedDate = df.format(newDate)*/

            if (isPrivate) {
                if (!isPrivateProtected) {
                    openToSetNewPasscode(formattedDate)
                } else {
                    if (isNewEntry) {
                        val entry = EntryData()
                        entry.lastUpdatedDate = formattedDate
                        entry.description = binding.contentTodo.edDescription.text.toString()
                        entry.title = binding.contentTodo.edTitle.text.toString()
                        entry.bgLightColor = generateLightColor()
                        entry.bgDarkColor = generateDarkColor()
                        entry.isPrivate = isPrivate
                        insertRow(entry)
                    } else {
                        updateEntryData.lastUpdatedDate = formattedDate
                        updateEntryData.description =
                            binding.contentTodo.edDescription.text.toString()
                        updateEntryData.title = binding.contentTodo.edTitle.text.toString()
                        updateEntryData.bgLightColor = generateLightColor()
                        updateEntryData.bgDarkColor = generateDarkColor()
                        updateEntryData.isPrivate = isPrivate
                        updateRow(updateEntryData)
                    }
                }
            } else {
                if (isNewEntry) {
                    val entry = EntryData()
                    entry.lastUpdatedDate = formattedDate
                    entry.description = binding.contentTodo.edDescription.text.toString()
                    entry.title = binding.contentTodo.edTitle.text.toString()
                    entry.bgLightColor = generateLightColor()
                    entry.bgDarkColor = generateDarkColor()
                    entry.isPrivate = isPrivate
                    insertRow(entry)
                } else {
                    updateEntryData.lastUpdatedDate = formattedDate
                    updateEntryData.description = binding.contentTodo.edDescription.text.toString()
                    updateEntryData.title = binding.contentTodo.edTitle.text.toString()
                    updateEntryData.bgLightColor = generateLightColor()
                    updateEntryData.bgDarkColor = generateDarkColor()
                    updateEntryData.isPrivate = isPrivate
                    updateRow(updateEntryData)
                }
            }


        }


    }

    private fun openToSetNewPasscode(formattedDate: String) {
        val builder = AlertDialog.Builder(this@AddUpdateActivity)
        val inflater = layoutInflater
        val view: View = inflater.inflate(R.layout.dialog_create_lock, null)
        builder.setView(view)

        val show = builder.setCancelable(true).show()

        edNewPasscode = view.findViewById<EditText>(R.id.edNewPasscode)
        edConfirmNewPasscode = view.findViewById<EditText>(R.id.edConfirmNewPasscode)

        view.findViewById<View>(R.id.btnSubmit).setOnClickListener { //dismiss dialog
            edNewPasscode!!.error = null
            edConfirmNewPasscode!!.error = null
            if (edNewPasscode!!.text.toString().trim().length != 4) {
                edNewPasscode!!.error = "Please enter 4 digits"
            } else if (edConfirmNewPasscode!!.text.toString().trim().length != 4) {
                edConfirmNewPasscode!!.error = "Please enter 4 digits"
            } else if (edNewPasscode!!.text.toString().trim()
                    .toInt() != edConfirmNewPasscode!!.text.toString().trim().toInt()
            ) {
                edNewPasscode!!.error = "Password can't match"
                edConfirmNewPasscode!!.error = "Password can't match"
            } else {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                sharedPreferences.edit().putBoolean("isPrivateProtected", true).apply()
                sharedPreferences.edit()
                    .putInt("passcode", edConfirmNewPasscode!!.text.toString().trim().toInt())
                    .apply()

                if (isNewEntry) {
                    val entry = EntryData()
                    entry.lastUpdatedDate = formattedDate
                    entry.description = binding.contentTodo.edDescription.text.toString()
                    entry.title = binding.contentTodo.edTitle.text.toString()
                    entry.bgLightColor = generateLightColor()
                    entry.bgDarkColor = generateDarkColor()
                    entry.isPrivate = isPrivate
                    insertRow(entry)
                } else {
                    updateEntryData.lastUpdatedDate = formattedDate
                    updateEntryData.description = binding.contentTodo.edDescription.text.toString()
                    updateEntryData.title = binding.contentTodo.edTitle.text.toString()
                    updateEntryData.bgLightColor = generateLightColor()
                    updateEntryData.bgDarkColor = generateDarkColor()
                    updateEntryData.isPrivate = isPrivate
                    updateRow(updateEntryData)
                }
            }
        }
        view.findViewById<View>(R.id.btnCancel).setOnClickListener { //dismiss dialog
            show.dismiss()
        }
    }

    private fun closeKeyboard(context: Context) {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(
            InputMethodManager.HIDE_IMPLICIT_ONLY,
            0
        )
    }

    fun generateLightColor(): Int {

        // This is the base color which will be mixed with the generated one
        val baseColor = Color.WHITE
        val baseRed = Color.red(baseColor)
        val baseGreen = Color.green(baseColor)
        val baseBlue = Color.blue(baseColor)
        val red: Int = (baseRed + mRandom.nextInt(256)) / 2
        val green: Int = (baseGreen + mRandom.nextInt(256)) / 2
        val blue: Int = (baseBlue + mRandom.nextInt(256)) / 2
        return Color.rgb(red, green, blue)
    }


    fun generateDarkColor(): Int {
        /*return Color.argb(255, rnd.nextInt(50), rnd.nextInt(50), rnd.nextInt(50))*/
        val baseColor = Color.BLACK
        val baseRed = Color.red(baseColor)
        val baseGreen = Color.green(baseColor)
        val baseBlue = Color.blue(baseColor)
        val red: Int = (baseRed + mRandom.nextInt(256)) / 2
        val green: Int = (baseGreen + mRandom.nextInt(256)) / 2
        val blue: Int = (baseBlue + mRandom.nextInt(256)) / 2
        return Color.rgb(red, green, blue)
    }

    @SuppressLint("StaticFieldLeak")
    private fun insertRow(todo: EntryData) {
        object : AsyncTask<EntryData, Void, Long>() {
            override fun doInBackground(vararg params: EntryData): Long? {
                return databse.daoAccess()!!.insertEntry(params[0])
            }

            override fun onPostExecute(id: Long?) {
                super.onPostExecute(id)
                val intent = intent
                intent.putExtra("isNew", true).putExtra("id", id)
                setResult(RESULT_OK, intent)
                finish()
            }
        }.execute(todo)
    }

    @SuppressLint("StaticFieldLeak")
    private fun deleteRow(todo: EntryData) {
        object : AsyncTask<EntryData, Void, Int>() {
            override fun doInBackground(vararg params: EntryData): Int? {
                return databse.daoAccess()!!.deleteEntry(params[0])
            }

            override fun onPostExecute(number: Int?) {
                super.onPostExecute(number)
                val intent = intent
                intent.putExtra("isDeleted", true).putExtra("number", number)
                setResult(RESULT_OK, intent)
                finish()
            }
        }.execute(todo)
    }


    @SuppressLint("StaticFieldLeak")
    private fun updateRow(todo: EntryData) {
        object : AsyncTask<EntryData, Void, Int>() {
            override fun doInBackground(vararg params: EntryData): Int? {
                return databse.daoAccess()!!.updateEntry(params[0])
            }

            override fun onPostExecute(number: Int?) {
                super.onPostExecute(number)
                val intent = intent
                intent.putExtra("isNew", false).putExtra("number", number)
                setResult(RESULT_OK, intent)
                finish()
            }
        }.execute(todo)
    }

    @SuppressLint("StaticFieldLeak")
    private fun fetchEntryById(todo_id: Int) {
        var entryData: EntryData? = null
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            entryData = databse.daoAccess()!!.fetchEntryListById(todo_id)
            handler.post {
                binding.contentTodo.edDescription.setText(entryData!!.description)
                binding.contentTodo.edTitle.setText(entryData!!.title)

                if (entryData!!.isPrivate) {
                    binding.imgPrivate.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@AddUpdateActivity,
                            R.drawable.ic_lock
                        )
                    )


                } else {
                    binding.imgPrivate.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@AddUpdateActivity,
                            R.drawable.ic_unlock
                        )
                    )
                }

                if (EntryActivity.isDarkTheme) {
                    binding.imgPrivate.setColorFilter(
                        ContextCompat.getColor(
                            this@AddUpdateActivity,
                            R.color.white
                        ), android.graphics.PorterDuff.Mode.MULTIPLY
                    );
                } else {
                    binding.imgPrivate.setColorFilter(
                        ContextCompat.getColor(
                            this@AddUpdateActivity,
                            R.color.black
                        ), android.graphics.PorterDuff.Mode.MULTIPLY
                    );
                }

                updateEntryData = entryData!!
            }
        }

    }

    private fun openDatePicker(isForUpdate: Boolean) {
        val calendar: Calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        val datePickerDialog =
            DatePickerDialog(this, this, year, month, day)
        /*if (isForUpdate) {
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        }*/
        datePickerDialog.show()
    }

    @SuppressLint("SetTextI18n")
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        /*binding.contentTodo.edDate.setText(
            changeDateFormat(
                "$dayOfMonth${"/"}$month${"/"}$year",
                "dd/MM/yyyy",
                "dd MMM, yyyy"
            )
        )*/
    }

    private fun keyBoardCloseOnEditextTap(editText: EditText, view: View) {
        editText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE
                || actionId == KeyEvent.ACTION_DOWN
                || actionId == KeyEvent.KEYCODE_ENTER
            ) {
                val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            true
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun changeDateFormat(
        dateToConvert: String,
        currentDateFormat: String,
        desiredDateFormat: String
    ): String {
        try {
            if (dateToConvert.isNotEmpty()) {
                val formatter = SimpleDateFormat(currentDateFormat)
                val date = formatter.parse(dateToConvert) as Date
                val newFormat = SimpleDateFormat(desiredDateFormat)
                return newFormat.format(date)
            }
        } catch (e: Exception) {
            /*showLog(e.localizedMessage)*/
        }
        return ""
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

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.btnDone -> {
                addUpdateEntry()
            }
        }
    }
}