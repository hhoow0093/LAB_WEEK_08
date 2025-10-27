package eu.tutorials.lab_week_08.worker


import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import eu.tutorials.lab_week_08.NotificationService
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class NotificationLauncherWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val id = inputData.getString(INPUT_DATA_ID) ?: return Result.failure()

        val latch = CountDownLatch(1)


        NotificationService.setCompletionCallback(id) {
            latch.countDown()
        }


        val serviceIntent = Intent(
            applicationContext,
            NotificationService::class.java
        ).apply {
            putExtra(NotificationService.EXTRA_ID, id)
        }
        ContextCompat.startForegroundService(applicationContext, serviceIntent)


        val completed = latch.await(30, TimeUnit.SECONDS)


        NotificationService.clearCompletionCallback(id)

        return if (completed) {
            val outputData = Data.Builder()
                .putString(OUTPUT_DATA_ID, id)
                .build()
            Result.success(outputData)
        } else {
            Result.failure()
        }
    }

    companion object {
        const val INPUT_DATA_ID = "inId"
        const val OUTPUT_DATA_ID = "outId"
    }
}