package io.github.kumarjit.openstandby.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat

class StandbyNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() = refreshAll()

    override fun onListenerDisconnected() = NotificationRepository.clear()

    override fun onNotificationPosted(sbn: StatusBarNotification, rankingMap: RankingMap) {
        val item = sbn.toStandbyNotification(rankingMap)
        if (item != null) NotificationRepository.upsert(item) else NotificationRepository.remove(sbn.key)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        NotificationRepository.remove(sbn.key)
    }

    private fun refreshAll() {
        val active = runCatching { activeNotifications }.getOrNull() ?: return
        val ranking = runCatching { currentRanking }.getOrNull()
        NotificationRepository.replaceAll(active.mapNotNull { it.toStandbyNotification(ranking) })
    }

    private fun StatusBarNotification.toStandbyNotification(rankingMap: RankingMap?): StandbyNotification? {
        if (packageName == this@StandbyNotificationListener.packageName) return null
        val n = notification
        if (isOngoing || (n.flags and Notification.FLAG_GROUP_SUMMARY) != 0) return null

        val extras = n.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString().orEmpty()
        if (title.isBlank() && text.isBlank()) return null

        val ranking = Ranking()
        val importance = if (rankingMap?.getRanking(key, ranking) == true) ranking.importance
        else NotificationManager.IMPORTANCE_DEFAULT
        val important = n.category in IMPORTANT_CATEGORIES || importance >= NotificationManager.IMPORTANCE_HIGH

        return StandbyNotification(
            key = key,
            packageName = packageName,
            appName = appLabel(packageName),
            title = title,
            text = text,
            postTime = postTime,
            systemImportant = important,
        )
    }

    private fun appLabel(pkg: String): String = runCatching {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
    }.getOrDefault(pkg)

    companion object {
        private val IMPORTANT_CATEGORIES = setOf(
            Notification.CATEGORY_MESSAGE,
            Notification.CATEGORY_CALL,
            Notification.CATEGORY_ALARM,
            Notification.CATEGORY_REMINDER,
            Notification.CATEGORY_EVENT,
        )

        fun isEnabled(context: Context): Boolean =
            NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
    }
}
