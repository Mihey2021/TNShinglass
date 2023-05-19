package ru.tn.shinglass.api.httpsettings


import okhttp3.OkHttpClient
import okhttp3.Protocol
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

fun createSocketFactory(userName: String, password: String): OkHttpClient {

    val sClient = OkHttpClient.Builder().apply {
        addInterceptor(BasicAuthInterceptor(userName, password))
        protocols(mutableListOf(Protocol.HTTP_1_1))
        connectTimeout(30, TimeUnit.SECONDS)
        writeTimeout(60, TimeUnit.SECONDS)
        readTimeout(60, TimeUnit.SECONDS)
    }.build()

    var sc: SSLContext? = null
    try {
        sc = SSLContext.getInstance("SSL")
        sc.init(
            null,
            arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                }


                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate>? {
                    return null
                }


            }), SecureRandom()
        )
    } catch (e: Exception) {
        e.printStackTrace();
    }

    val hv1 = object : HostnameVerifier {
        override fun verify(hostname: String, session: SSLSession): Boolean {
            return true
        }
    }

    val workerClassName = "okhttp3.OkHttpClient";

    try {
        val workerClass = Class.forName(workerClassName)
        val hostnameVerifier = workerClass.getDeclaredField("hostnameVerifier")
        hostnameVerifier.isAccessible = true
        hostnameVerifier.set(sClient, hv1)

        val sslSocketFactory = workerClass.getDeclaredField("sslSocketFactory")
        sslSocketFactory.isAccessible = true
        sslSocketFactory.set(sClient, sc?.socketFactory)
    } catch (e: Exception) {
        e.printStackTrace();
    }

    return sClient
}