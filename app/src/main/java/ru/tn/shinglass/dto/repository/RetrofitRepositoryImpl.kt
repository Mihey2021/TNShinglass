package ru.tn.shinglass.dto.repository

//import ru.tn.shinglass.api.ApiUtils
//import ru.tn.shinglass.domain.repository.RetrofitRepository
//import ru.tn.shinglass.dto.models.RequestLogin
//import ru.tn.shinglass.dto.models.User1C

//Пока НЕ ИСПОЛЬЗУЕТСЯ!
//class RetrofitRepositoryImpl: RetrofitRepository {
//
//    private val apiService = ApiUtils.getApiService()
//
//    init {
//        if (apiService == null) {
//            throw RuntimeException("Клиент Retrofit не инициализирован!")
//        }
//    }
//
//    override fun authorization(user: RequestLogin, callback: RetrofitRepository.Callback<User1C>) {
//        apiService?.authorization(user)
//    }
//}