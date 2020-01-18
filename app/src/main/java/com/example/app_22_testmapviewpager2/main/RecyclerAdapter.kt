package com.example.app_22_testmapviewpager2.main

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.app_22_testmapviewpager2.R
import com.example.app_22_testmapviewpager2.architecture.DiscussionsEntity
import com.squareup.picasso.Picasso
import android.text.format.DateFormat
import java.util.*
import android.util.DisplayMetrics
import android.util.Log
import java.text.SimpleDateFormat


val CULTURE_IDS = mapOf(
    Pair(1, "Горох"), Pair(2, "Гречка"), Pair(3, "Гірчиця"), Pair(4, "Жито озиме"), Pair(5, "Жито"), Pair(6, "Конопля"), Pair(7, "Кукурудза"),
    Pair(8, "Льон"), Pair(9, "Люпин"), Pair(10, "Люцерна"), Pair(11, "Мак олійний"), Pair(12, "Нут"), Pair(13, "Просо"), Pair(14, "Пшениця"),
    Pair(15, "Пшениця озима"), Pair(16, "Рис"), Pair(17, "Ріпак"), Pair(18, "Соняшник"), Pair(19, "Сорго"), Pair(20, "Сочевиця"), Pair(21, "Соя"),
    Pair(22, "Тритикале"), Pair(23, "Цукрові буряки"), Pair(24, "Ячмінь"), Pair(25, "Ячмінь озимий")
    )

val PROBLEM_IDS = mapOf(
    Pair(1, "Бур'яни"), Pair(2, "Шкідники"), Pair(3, "Погодні умови"), Pair(4, "Нестача живлення"), Pair(5, "Наслідки агрохімічних заходів"), Pair(6, "Хвороби"), Pair(7, "Не визначено")
)

class RecyclerAdapter(private val discussions : DiscussionsEntity, private val context : Context?) : RecyclerView.Adapter<RecyclerAdapter.RecyclerHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerHolder =
        RecyclerHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_item, parent, false))

    override fun getItemCount(): Int {
        return discussions.discussions.size
    }

    override fun onBindViewHolder(holder: RecyclerHolder, position: Int) {
        val discussion = discussions.discussions[position]
        val imageList = discussion.imageDTOList
        var firstImage = ""
        var secondImage = ""
        var thirdImage = ""
        imageList.forEach {
            if (it.type == "RECTANGLE") {
                when (it.position) {
                    "FIRST" -> firstImage += "http://app.crazyagro.com/admin/img/${it.imageUrl}"
                    "SECOND" -> secondImage += "http://app.crazyagro.com/admin/img/${it.imageUrl}"
                    "THIRD" -> thirdImage += "http://app.crazyagro.com/admin/img/${it.imageUrl}"
                }
            }
        }
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        val bigImageWidth = (displayMetrics.widthPixels - 42) * 0.666
        val smallImageWidth = (displayMetrics.widthPixels - 42) * 0.333
        if (firstImage.isNotEmpty()) {
            Picasso.get()
                .load(firstImage)
                .resize(bigImageWidth.toInt() + 100, bigImageWidth.toInt() + 100)
                .centerCrop()
                .noFade()
                .into(holder.image1)
        }
        if (secondImage.isNotEmpty()) {
            Picasso.get()
                .load(secondImage)
                .resize(smallImageWidth.toInt() + 70, smallImageWidth.toInt() + 70)
                .centerCrop()
                .noFade()
                .into(holder.image2)
        }
        if (thirdImage.isNotEmpty()) {
            Picasso.get()
                .load(thirdImage)
                .resize(smallImageWidth.toInt() + 70, smallImageWidth.toInt() + 70)
                .centerCrop()
                .noFade()
                .into(holder.image3)
        }
        if (discussion.creatorName.isNotEmpty()) {
            Log.i("creator: ", discussion.creatorName)
            val name = discussion.creatorName.trim().split(" ")
            holder.icon.text = name[name.size - 1][0].toString()
        }
        else holder.icon.text = ""
        holder.creator.text = discussion.creatorName
        val createDate = GregorianCalendar()
        createDate.timeInMillis = Date().time - discussion.createDateUNIX
        holder.date.text = when {
            createDate.get(Calendar.HOUR_OF_DAY) * Calendar.DAY_OF_YEAR * Calendar.YEAR > 24  -> SimpleDateFormat("d MMM. yyyy г.", Locale.Builder().setLanguage("ukr").build()).format(discussion.createDateUNIX)//DateFormat.format("d MMM. yyyy г.", discussion.createDateUNIX)
            createDate.get(Calendar.HOUR_OF_DAY) == 0 -> "нещодавно"
            createDate.get(Calendar.HOUR_OF_DAY) == 1 -> "${createDate.get(Calendar.HOUR)} годину тому"
            createDate.get(Calendar.HOUR_OF_DAY) in 2..4 -> "${createDate.get(Calendar.HOUR)} години тому"
            else -> "${createDate.get(Calendar.HOUR_OF_DAY)} годин тому"
        }
        holder.address.text = discussion.address
        holder.culture.text = "#${CULTURE_IDS[discussion.cultureId]}"
        holder.problem.text = "#${PROBLEM_IDS[discussion.problemId]}"
        holder.description.text = discussion.description
        holder.commentsCount.text = discussion.commentsAmount.toString()
    }

    class RecyclerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon : TextView = itemView.findViewById(R.id.recycler_item_icon)
        val creator : TextView = itemView.findViewById(R.id.recycler_item_creator_name)
        val date : TextView = itemView.findViewById(R.id.recycler_item_date)
        val address : TextView = itemView.findViewById(R.id.recycler_item_address)
        val culture : TextView = itemView.findViewById(R.id.recycler_item_culture)
        val problem : TextView = itemView.findViewById(R.id.recycler_item_problem)
        val description : TextView = itemView.findViewById(R.id.recycler_item_description)
        val image1 : ImageView = itemView.findViewById(R.id.recycler_item_image1)
        val image2 : ImageView = itemView.findViewById(R.id.recycler_item_image2)
        val image3 : ImageView = itemView.findViewById(R.id.recycler_item_image3)
        val commentsCount : TextView = itemView.findViewById(R.id.recycler_item_comments_count)
    }

}