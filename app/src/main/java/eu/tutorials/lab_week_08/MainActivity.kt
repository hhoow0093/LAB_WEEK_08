package eu.tutorials.lab_week_08

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import eu.tutorials.lab_week_08.worker.FirstWorker
import eu.tutorials.lab_week_08.worker.SecondWorker
import eu.tutorials.lab_week_08.worker.ThirdWorker
import eu.tutorials.lab_week_08.worker.NotificationLauncherWorker

class MainActivity : AppCompatActivity() {
    private val workManager by lazy { WorkManager.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val networkConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val Id = "001"
        val Id2 = "002"


        val firstRequest = OneTimeWorkRequest
            .Builder(FirstWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(FirstWorker.INPUT_DATA_ID, Id))
            .build()


        val secondRequest = OneTimeWorkRequest
            .Builder(SecondWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(SecondWorker.INPUT_DATA_ID, Id))
            .build()


        val notificationWorkerRequest = OneTimeWorkRequest
            .Builder(NotificationLauncherWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(NotificationLauncherWorker.INPUT_DATA_ID, Id))
            .build()


        val thirdRequest = OneTimeWorkRequest
            .Builder(ThirdWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(ThirdWorker.INPUT_DATA_ID, Id2))
            .build()


        val notificationWorker2Request = OneTimeWorkRequest
            .Builder(NotificationLauncherWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(NotificationLauncherWorker.INPUT_DATA_ID, Id2))
            .build()

        //  Worker1 → Worker2 → NotificationService → Worker3 → NotificationService2
        workManager
            .beginWith(firstRequest)
            .then(secondRequest)
            .then(notificationWorkerRequest)
            .then(thirdRequest)
            .then(notificationWorker2Request)
            .enqueue()

        // Observe completion
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
                    }
                }
            }

        workManager.getWorkInfoByIdLiveData(notificationWorkerRequest.id)
            .observe(this) { info ->
                info?.let {
                    if (it.state.isFinished) {
                        showResult("NotificationService is done")
                    }
                }
            }

        workManager.getWorkInfoByIdLiveData(thirdRequest.id)
            .observe(this) { info ->
                info?.let {
                    if (it.state.isFinished) {
                        showResult("Third process is done")
                    }
                }
            }

        workManager.getWorkInfoByIdLiveData(notificationWorker2Request.id)
            .observe(this) { info ->
                info?.let {
                    if (it.state.isFinished) {
                        showResult("NotificationService2 is done")
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
}