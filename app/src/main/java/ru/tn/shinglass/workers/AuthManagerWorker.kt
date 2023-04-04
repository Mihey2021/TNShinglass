package ru.tn.shinglass.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import ru.tn.shinglass.auth.AppAuth

class AuthManagerWorker(context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result =
        try {
            AppAuth.getInstance().clearAuthData()
            println("LOGGGGG: Устройство в спящем режиме, завершаем сеанс пользователя.")
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
}