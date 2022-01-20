package com.pmggroup.keepthenotes.adapter


import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pmggroup.keepthenotes.R
import com.pmggroup.keepthenotes.database.EntryData
import com.pmggroup.keepthenotes.database.MyDatabase
import com.pmggroup.keepthenotes.databinding.ListitemEntryListBinding
import com.pmggroup.keepthenotes.interfaces.OnClickListener
import com.pmggroup.keepthenotes.ui.EntryActivity
import java.util.*
import java.util.concurrent.Executors


class EntryViewAdapter(
    contextActivity: EntryActivity,
     database: MyDatabase,
    isDarkTheme: Boolean,
     onClickListener: OnClickListener,
    private val itemClickEdit: (result: EntryData) -> Unit
)
    : RecyclerView.Adapter<EntryViewAdapter.ItemViewHolder>() , Filterable {

    private val entry: ArrayList<EntryData> = ArrayList()
    private var entryFilterList: ArrayList<EntryData> = ArrayList()
    private val context: EntryActivity= contextActivity
    private val database: MyDatabase = database
    val mRandom: Random  =  Random(System.currentTimeMillis())
    val onClickListener: OnClickListener = onClickListener
    var isDarkTheme: Boolean = isDarkTheme
    private val TYPE_FULL = 0
    private val TYPE_HALF = 1
    private val TYPE_QUARTER = 2

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
       val binding= ListitemEntryListBinding.inflate(LayoutInflater.from(context), parent, false)
        return ItemViewHolder(binding)
    }


    override fun getItemViewType(position: Int): Int {
        val modeEight = position % 8
        when (modeEight) {
            0, 5 -> return TYPE_HALF
            1, 2, 4, 6 -> return TYPE_QUARTER
        }
        return TYPE_FULL
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(position)

        holder.itemView.setOnClickListener {
            itemClickEdit.invoke(entryFilterList[position])
        }

        holder.itemView.setOnLongClickListener(OnLongClickListener {
            openAlterDialog(position)
            true
        })


    }

    private fun openAlterDialog(position: Int) {
        AlertDialog.Builder(context)
            .setCancelable(false)
            .setMessage("Are you want to delete?")
            .setCancelable(true)
            .setPositiveButton("Yes") { arg0, arg1 ->
                deleteEntry(entryFilterList[position],position)
            }
            .setNegativeButton(
                "No"
            ) { dialog, which -> //dismiss dialog

            }.create().show()

    }

    private fun deleteEntry(entryData: EntryData,position: Int){
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            database.daoAccess()!!.deleteEntry(entryData)
            handler.post {
                Toast.makeText(context, (position+1).toString()+" entry deleted", Toast.LENGTH_SHORT)
                    .show()
                context.loadAllEntries()
            }
        }
    }

    override fun getItemCount() = entryFilterList.size

    fun updateEntryList(data: List<EntryData>,onClickListener: OnClickListener,isAscending:Boolean) {
        if (isAscending){
            entry.clear()
            entryFilterList.clear()
            entryFilterList.addAll(data)
            entry.addAll(data)
            notifyDataSetChanged()
            onClickListener.onSearch(entryFilterList.size, "")
        } else {
            val reverse: List<EntryData> = data.reversed();
            entry.clear()
            entryFilterList.clear()
            entryFilterList.addAll(reverse)
            entry.addAll(reverse)
            notifyDataSetChanged()
            onClickListener.onSearch(entryFilterList.size, "")
        }

    }

    fun addRow(data: EntryData) {
        entryFilterList.add(data)
        notifyDataSetChanged()
    }

    fun refreshTheme(darkTheme:Boolean) {
        this.isDarkTheme = darkTheme
        notifyDataSetChanged()
    }

    inner class ItemViewHolder(private val binding: ListitemEntryListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {

            if (isDarkTheme){
                binding.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.black))
                binding.tvLastUpdateDate.setTextColor(ContextCompat.getColor(context, R.color.black))
                binding.cardView.setCardBackgroundColor(entryFilterList[position].bgLightColor!!)
                binding.imgDelete.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY)
                binding.imgPrivate.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY)
            } else {
                binding.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.white))
                binding.tvLastUpdateDate.setTextColor(ContextCompat.getColor(context, R.color.white))
                binding.cardView.setCardBackgroundColor(entryFilterList[position].bgDarkColor!!)
                binding.imgDelete.setColorFilter(ContextCompat.getColor(context, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY)
                binding.imgPrivate.setColorFilter(ContextCompat.getColor(context, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY)
            }

            if (entryFilterList[position].isPrivate){
                binding.imgPrivate.visibility = View.VISIBLE
            }else{
                binding.imgPrivate.visibility = View.GONE
            }

            binding.imgDelete.setOnClickListener { openAlterDialog(position) }
            binding.tvEntryNumber.text = (position+1).toString()
            binding.tvLastUpdateDate.text = entryFilterList[position].lastUpdatedDate
            /*if (entryFilterList[position].isPrivate){
                binding.tvTitle.text = "Locked note"
            }else{
                binding.tvTitle.text = entryFilterList[position].title
            }*/
            binding.tvTitle.text = entryFilterList[position].title

            binding.cardView.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5F, context.resources.displayMetrics)
        }
    }

   /* class ItemViewHolder(view: View) : RecyclerView.ViewHolder(
        view
    ) {
        val tvEntryNumber: TextView = view.findViewById(R.id.tvEntryNumber)
        val tvLastUpdateDate: TextView = view.findViewById(R.id.tvLastUpdateDate)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val cardView: CardView = view.findViewById(R.id.cardView)

    }*/

    fun generateRandomColor(): Int {

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

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                entryFilterList = if (charSearch.isEmpty()) {
                    entry
                } else {
                    val resultList = ArrayList<EntryData>()
                    for (row in entry) {
                        if (charSearch.uppercase(Locale.getDefault())
                                .let {
                                    row.title?.uppercase(Locale.getDefault())?.contains(it)!!
                                }
                        ) {
                            /*if (!row.isPrivate)*/
                                resultList.add(row)
                        }
                        /*if (charSearch.uppercase(Locale.getDefault()).let {
                                row.description?.uppercase(
                                    Locale.getDefault()
                                )?.contains(it)!!
                            }) {
                            if (!row.isPrivate)
                                resultList.add(row)
                        } else if (charSearch.uppercase(Locale.getDefault())
                                .let {
                                    row.title?.uppercase(Locale.getDefault())?.contains(it)!!
                                }
                        ) {
                            if (!row.isPrivate)
                                resultList.add(row)
                        }*/
                    }

                    resultList
                }

                val filterResults = FilterResults()
                filterResults.values = entryFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                entryFilterList =
                    results?.values as ArrayList<EntryData>
                notifyDataSetChanged()
                onClickListener.onSearch(entryFilterList.size, constraint.toString())
            }
        }
    }
}