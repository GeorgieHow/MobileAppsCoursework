package com.example.mobileappscoursework.ui.leaderboard
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class LeaderboardWorker(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")
        val startOfWeek = getStartOfWeek()
        val endOfWeek = getEndOfWeek()

        return try {
            val snapshot = usersCollection.get().await()
            var topUser = "" to 0

            for (document in snapshot.documents) {
                val userId = document.id
                val logsCollection = usersCollection.document(userId).collection("logs")
                val logsSnapshot = logsCollection
                    .whereGreaterThanOrEqualTo("sortableDate", startOfWeek)
                    .whereLessThanOrEqualTo("sortableDate", endOfWeek)
                    .get().await()

                var totalHours = 0
                logsSnapshot.documents.forEach {
                    totalHours += it.getLong("hours")?.toInt() ?: 0
                }

                if (totalHours > topUser.second) {
                    topUser = userId to totalHours
                }
            }

            if (topUser.first.isNotEmpty()) {
                val userDocRef = usersCollection.document(topUser.first)
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(userDocRef)
                    val currentWins = snapshot.getLong("winCount") ?: 0
                    transaction.update(userDocRef, "winCount", currentWins + 1)
                }.await()
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }


    private fun getStartOfWeek(): String {
        val today = LocalDate.now()
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return startOfWeek.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
    private fun getEndOfWeek(): String {
        val today = LocalDate.now()
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        return endOfWeek.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
}