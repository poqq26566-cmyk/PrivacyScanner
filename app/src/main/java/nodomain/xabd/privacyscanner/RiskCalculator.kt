package nodomain.xabd.privacyscanner

import android.content.Context
import android.os.Build

object RiskCalculator {

    private val trustedStores = mapOf(
        "org.fdroid.fdroid" to "F-Droid",
        "org.fdroid.basic" to "F-Droid Basic",
        "com.android.vending" to "Google Play",
        "com.aurora.store" to "Aurora Store",
        "com.izzyondroid.installer" to "IzzyOnDroid",
        "com.looker.droidify" to "Droid-ify",
        "com.machiav3lli.fdroid" to "Neo Store",
    )

    private val otherKnownInstallers = mapOf(
        "dev.imranr.obtainium" to "Obtainium",
        "com.google.android.packageinstaller" to "默认安装程序（Google）",
    )

    private val trustedApps = mapOf(
        "org.schabi.newpipe" to "F-Droid（已验证）",
        "com.aurora.services" to "Aurora Services",
        "com.fsck.k9" to "K-9 邮件"
    )

    private val criticalPerms = listOf(
        "READ_SMS", "SEND_SMS", "RECEIVE_SMS", "READ_CONTACTS", "WRITE_CONTACTS",
        "RECORD_AUDIO", "RECORD_VIDEO", "CALL_PHONE", "READ_CALL_LOG", "WRITE_CALL_LOG",
        "READ_CALENDAR", "WRITE_CALENDAR", "ACCESS_FINE_LOCATION", "ACCESS_COARSE_LOCATION",
    )

    private val mediumPerms = listOf(
        "CAMERA", "READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE", "QUERY_ALL_PACKAGES",
        "READ_PHONE_STATE", "BODY_SENSORS", "ACCESS_WIFI_STATE", "ACCESS_NETWORK_STATE"
    )

    private val lowPerms = listOf(
        "INTERNET", "VIBRATE", "FOREGROUND_SERVICE", "BLUETOOTH", "NFC"
    )

    fun calculate(
        context: Context,
        pkgName: String,
        permissions: List<String>,
        grantedMap: Map<String, Boolean>? = null
    ): Pair<String, String> {

        val prefs = context.getSharedPreferences("trusted_apps", Context.MODE_PRIVATE)
        val pm = context.packageManager
        val effectivePermissions = grantedMap?.filterValues { it }?.keys?.toList() ?: permissions

        if (prefs.getBoolean(pkgName, false)) {
            return "安全（用户信任）" to "由用户标记为信任"
        }

        if (trustedStores.containsKey(pkgName)) {
            return "安全（可信来源）" to "可信应用商店（${trustedStores[pkgName]}）"
        }
        if (trustedApps.containsKey(pkgName)) {
            return "安全（已验证）" to trustedApps[pkgName]!!
        }

        val installer = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                pm.getInstallSourceInfo(pkgName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                pm.getInstallerPackageName(pkgName)
            }
        } catch (_: Exception) {
            null
        }

        val (risk, reason) = scoreRisk(effectivePermissions)

        val source = when {
            installer == null -> "未知（侧载安装）"
            trustedStores.containsKey(installer) -> "可信（来自 ${trustedStores[installer]}）"
            otherKnownInstallers.containsKey(installer) -> "已知（来自 ${otherKnownInstallers[installer]}）"
            installer.contains("samsung", true) -> "可信（三星应用商店）"
            installer.contains("huawei", true) -> "可信（华为应用市场）"
            else -> "未经验证的来源（$installer）"
        }

        return risk to "$source • $reason"
    }

    private fun scoreRisk(permissions: List<String>): Pair<String, String> {
        if (permissions.isEmpty()) {
            return "安全（无权限）" to "未请求任何权限"
        }

        var score = 0.0
        var hasCritical = false
        var hasMedium = false
        val analyzed = permissions.map { it.uppercase() }
        val foundCritical = mutableListOf<String>()
        val foundMedium = mutableListOf<String>()

        analyzed.forEach { p ->
            when {
                criticalPerms.any { p.contains(it) } -> {
                    score += 12.5
                    hasCritical = true
                    foundCritical.add(p)
                }
                mediumPerms.any { p.contains(it) } -> {
                    score += 6.5
                    hasMedium = true
                    foundMedium.add(p)
                }
                lowPerms.any { p.contains(it) } -> score += 1.0
            }
        }

        val hasInternet = analyzed.any { it.contains("INTERNET") }
        val hasCamera = analyzed.any { it.contains("CAMERA") }
        val hasMic = analyzed.any { it.contains("RECORD_AUDIO") }
        val hasLocation = analyzed.any { it.contains("LOCATION") }

        if (hasInternet && (hasCamera || hasMic || hasLocation)) {
            score += 10
        }

        val finalScore = score.coerceIn(0.0, 100.0)

        val risk = when {
            hasCritical -> "高风险（已授权）"
            hasMedium -> "中风险（已授权）"
            finalScore in 10.0..29.9 -> "低风险（已授权）"
            else -> "安全（无敏感权限）"
        }

        val reason = when {
            hasCritical -> "可访问敏感用户数据或传感器（${foundCritical.take(3).joinToString("、")}）"
            hasMedium -> "使用摄像头、存储或手机状态（${foundMedium.take(3).joinToString("、")}）"
            hasInternet -> "仅限网络访问（无敏感数据）"
            else -> "未检测到隐私相关权限"
        }

        return risk to reason
    }
}
