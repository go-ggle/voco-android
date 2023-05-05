package com.example.voco.login

import android.content.Context
import android.widget.Toast
import com.example.voco.api.ApiRepository
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause


class LoginCallback(val context : Context) {
    private val apiRepository = ApiRepository(context)
    val kakao: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            when {
                error.toString() == AuthErrorCause.AccessDenied.toString() -> {
                    Toast.makeText(context, "동의 취소로 접근이 거부되었습니다", Toast.LENGTH_SHORT).show()
                }
                error.toString() == AuthErrorCause.InvalidClient.toString() -> {
                    Toast.makeText(context, "유효하지 않은 앱입니다", Toast.LENGTH_SHORT).show()
                }
                error.toString() == AuthErrorCause.InvalidGrant.toString() -> {
                    Toast.makeText(context, "인증 수단이 유효하지 않아 인증할 수 없습니다", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(context, "로그인에 실패했습니다.\n나중에 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                }
                error.toString() == AuthErrorCause.Unauthorized.toString() -> {
                    Toast.makeText(context, "앱에 요청 권한이 없습니다", Toast.LENGTH_SHORT).show()
                }
                error is ClientError && error.reason == ClientErrorCause.Cancelled -> {
                    // user cancel the sns login
                }
                else -> { // Unknown
                    Toast.makeText(context, "에러: $error", Toast.LENGTH_SHORT).show()
                }
            }
        }
        else if (token != null) {
            // sns login request
                println(token)
            Glob.prefs.setString("refresh_token",token.accessToken)
            apiRepository.snsLogin(token.accessToken, "kakao")
        }
    }
}