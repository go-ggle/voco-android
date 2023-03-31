package com.example.voco.login

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.voco.ui.SplashActivity
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause


class LoginCallback(context : Context) {
    val kakao: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            when {
                error.toString() == AuthErrorCause.AccessDenied.toString() -> {
                    Toast.makeText(context, "접근이 거부 됨(동의 취소)", Toast.LENGTH_SHORT).show()
                }
                error.toString() == AuthErrorCause.InvalidClient.toString() -> {
                    Toast.makeText(context, "유효하지 않은 앱", Toast.LENGTH_SHORT).show()
                }
                error.toString() == AuthErrorCause.InvalidGrant.toString() -> {
                    Toast.makeText(context, "인증 수단이 유효하지 않아 인증할 수 없는 상태", Toast.LENGTH_SHORT).show()
                }
                error.toString() == AuthErrorCause.InvalidRequest.toString() -> {
                    Toast.makeText(context, "요청 파라미터 오류", Toast.LENGTH_SHORT).show()
                }
                error.toString() == AuthErrorCause.InvalidScope.toString() -> {
                    Toast.makeText(context, "유효하지 않은 scope ID", Toast.LENGTH_SHORT).show()
                }
                error.toString() == AuthErrorCause.Misconfigured.toString() -> {
                    Toast.makeText(context, "설정이 올바르지 않음(android key hash)", Toast.LENGTH_SHORT).show()
                }
                error.toString() == AuthErrorCause.ServerError.toString() -> {
                    Toast.makeText(context, "서버 내부 에러", Toast.LENGTH_SHORT).show()
                }
                error.toString() == AuthErrorCause.Unauthorized.toString() -> {
                    Toast.makeText(context, "앱이 요청 권한이 없음", Toast.LENGTH_SHORT).show()
                }
                error is ClientError && error.reason == ClientErrorCause.Cancelled -> {
                    Toast.makeText(context, "로그인 취소", Toast.LENGTH_SHORT).show()
                }
                else -> { // Unknown
                    Toast.makeText(context, "기타 에러: $error", Toast.LENGTH_SHORT).show()
                    println(error)
                }
            }
        }
        else if (token != null) {
            directToHome(context, token.toString())
            Toast.makeText(context, "카카오 로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show()
        }
    }
    private fun directToHome(context : Context, token: String) {
        GlobalApplication.prefs.setString("id",token)
        val intent = Intent(context, SplashActivity::class.java)
        context.startActivity(intent)
    }
}