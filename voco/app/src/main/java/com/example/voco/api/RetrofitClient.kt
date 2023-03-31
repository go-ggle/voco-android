package com.example.voco.api

import com.example.voco.login.GlobalApplication
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

object RetrofitClient {
    private const val BASE_URL = "http://3.39.130.119:8080"

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
    fun getOkHttpClient(interceptor: AppInterceptor) : OkHttpClient
    = OkHttpClient.Builder().run{
        addInterceptor(interceptor)
        build()
    }
    class AppInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain) : Response = with(chain) {
            val newRequest = request().newBuilder()
                .addHeader("Authorization", GlobalApplication.prefs.getString("id","logout"))
                .build()
            proceed(newRequest)
        }
    }
}