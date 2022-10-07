package ru.tn.shinglass.api.httpsettings

import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.Interceptor

class BasicAuthInterceptor(username: String, password: String) : Interceptor {
    private var credentials: String = Credentials.basic(username, password)
    private var username: String = username
    private var password: String = password

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var request = chain.request()
        val formBody = FormBody.Builder()
            .add("username", username)
            .add("password", password)
            .build()
        request = request.newBuilder()
            .header("Authorization", credentials)
            .build()
        return chain.proceed(request)
    }
}