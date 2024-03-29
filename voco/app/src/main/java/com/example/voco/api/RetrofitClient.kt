package com.example.voco.api

import com.example.voco.login.Glob
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://3.39.130.119:8080"
    private const val FLASK_URL = "http://58.142.29.186:52424"
    fun getRetrofitClient(isHeader: Boolean) : Retrofit {
        return when(isHeader){
            true -> Retrofit.Builder()
                //서버 url설정
                .baseUrl(BASE_URL)
                // Header 붙이기
                .client(getOkHttpClient(AppInterceptor()))
                //데이터 파싱 설정
                .addConverterFactory(EmptyResponseConverterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                // 객체정보 반환
                .build()
            else -> Retrofit.Builder()
                //서버 url설정
                .baseUrl(BASE_URL)
                //데이터 파싱 설정
                .addConverterFactory(EmptyResponseConverterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                // 객체정보 반환
                .build()
        }
    }
    fun getFlaskRetrofitClient(isHeader: Boolean) : Retrofit {
        return when(isHeader){
            true -> Retrofit.Builder()
                //서버 url설정
                .baseUrl(FLASK_URL)
                // Header 붙이기
                .client(getOkHttpClient(AppInterceptor()))
                //데이터 파싱 설정
                .addConverterFactory(EmptyResponseConverterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                // 객체정보 반환
                .build()
            else -> Retrofit.Builder()
                //서버 url설정
                .baseUrl(FLASK_URL)
                //데이터 파싱 설정
                .addConverterFactory(EmptyResponseConverterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                // 객체정보 반환
                .build()
        }
    }
    private fun getOkHttpClient(interceptor: AppInterceptor) : OkHttpClient
    = OkHttpClient.Builder().run{
        addInterceptor(interceptor)
        connectTimeout(300, TimeUnit.SECONDS); // connect timeout
        readTimeout(300, TimeUnit.SECONDS);    // socket timeout
        build()
    }
    class AppInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain) : Response = with(chain) {
            val newRequest = request().newBuilder()
                .addHeader("Authorization", "Bearer "+Glob.prefs.getString("token","logout"))
                .build()
            proceed(newRequest)
        }
    }
}