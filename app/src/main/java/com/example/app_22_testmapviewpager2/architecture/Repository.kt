package com.example.app_22_testmapviewpager2.architecture

import android.app.Application
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Repository(val application : Application) {

    private val dao : DiscussionsDao
    private val api : CrazyagroApi
    private val discussions : LiveData<DiscussionsEntity>

    init {
        val database = DiscussionsDatabase.getInstance(application)
        this.dao = database.getDao()
        this.discussions = dao.getDiscussions()

        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://app.crazyagro.com:8777/api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        this.api = retrofit.create(CrazyagroApi::class.java)
    }

    fun getDiscussions() = discussions

    fun updateDiscussions(lat : Float, lng : Float, radius : Int) {
        api.getDiscussions(lat, lng, radius).enqueue(object : Callback<Array<Discussions>> {
            override fun onFailure(call: Call<Array<Discussions>>, t: Throwable) {
                Log.e("updateDiscussions", "onFailure: ${t.message}")
                Toast.makeText(application.applicationContext, "Виникла помилка при спробі налаштувати з'еднання", Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<Array<Discussions>>, response: Response<Array<Discussions>>) {
                if (response.isSuccessful) {
                    UpdateDiscussionsAsyncTask(dao).execute(response.body())
                }
                else
                    Log.e("updateDiscussions", "onResponse: ${response.message()}")
            }
        })
    }

    class UpdateDiscussionsAsyncTask(private val dao: DiscussionsDao) : AsyncTask<Array<Discussions>, Unit, Unit>() {

        override fun doInBackground(vararg params: Array<Discussions>?) {
            if (params[0] != null) {
                dao.deleteDiscussions()
                dao.insertDiscussions(DiscussionsEntity(params[0]!!))
            }
        }
    }

}