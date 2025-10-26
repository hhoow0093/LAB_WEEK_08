package eu.tutorials.lab_week_08

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import eu.tutorials.lab_week_08.worker.FirstWorker
import eu.tutorials.lab_week_08.worker.SecondWorker

class MainActivity : AppCompatActivity() {
    private val workManager = WorkManager.getInstance(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val networkConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val Id = "001"
        val firstRequest = OneTimeWorkRequest
            .Builder(FirstWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(FirstWorker
                .INPUT_DATA_ID, Id)
            ).build()
        val secondRequest = OneTimeWorkRequest
            .Builder(SecondWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(SecondWorker
                .INPUT_DATA_ID, Id)
            ).build()
        workManager.beginWith(firstRequest).then(secondRequest)
            .enqueue()
        workManager.getWorkInfoByIdLiveData(firstRequest.id)
            .observe(this) { info ->
                info?.let {
                    if (it.state.isFinished) {
                        showResult("First process is done")
                    }
                }
            }
        workManager.getWorkInfoByIdLiveData(secondRequest.id)
            .observe(this) { info ->
                info?.let {
                    if (it.state.isFinished) {
                        showResult("Second process is done")
                        launchNotificationService()
                    }
                }
            }
    }
    private fun getIdInputData(IdKey: String, IdValue: String) =
        Data.Builder()
            .putString(IdKey, IdValue)
            .build()
    private fun showResult(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun launchNotificationService() {
        NotificationService.trackingCompletion.observe(
            this) { Id ->
            showResult("Process for Notification Channel ID $Id is done!")
        }
        val serviceIntent = Intent(
            this,
            NotificationService::class.java
        ).apply {
            putExtra(EXTRA_ID, "001")
        }
        ContextCompat.startForegroundService(this, serviceIntent)
    }
    companion object{
        const val EXTRA_ID = "Id"
    }

}



