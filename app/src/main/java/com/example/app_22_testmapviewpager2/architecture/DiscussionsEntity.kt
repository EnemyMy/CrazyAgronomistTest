package com.example.app_22_testmapviewpager2.architecture

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson


@Entity
class DiscussionsEntity(discussions: Array<Discussions>) {

    @PrimaryKey
    var id : Int = 0

    @TypeConverters(DiscussionsTypeConverter::class)
    var discussions : Array<Discussions> = discussions
        private set

    class DiscussionsTypeConverter {

        var gson = Gson()

        @TypeConverter
        fun stringToSomeObjectList(data: String?): Array<Discussions> {
            if (data == null) {
                return arrayOf()
            }

            val arrayType = object : TypeToken<Array<Discussions>>() {}.type

            return gson.fromJson(data, arrayType)
        }

        @TypeConverter
        fun someObjectListToString(someObjects: Array<Discussions>): String {
            return gson.toJson(someObjects)
        }

    }
}