package com.example.app_22_testmapviewpager2.architecture

class Discussions(val description : String, val address : String, val creatorName : String, val iconUrl : String, val cultureId : Int, val problemId : Int,
                 val createDateUNIX : Long, val commentsAmount : Int, val latitude : Float, val longitude : Float, val imageDTOList : Array<DiscussionImage>, val expectedImageAmount : Int) {

    class DiscussionImage(val position : String, val type : String, val imageUrl : String)

}
