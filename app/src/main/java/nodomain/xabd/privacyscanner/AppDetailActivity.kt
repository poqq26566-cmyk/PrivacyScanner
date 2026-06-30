package nodomain.xabd.privacyscanner

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.content.edit

class AppDetailActivity : BaseActivity() {

    // 🈶 常见权限的中文说明对照表
    private val permissionTranslations = mapOf(
    "READ_SMS" to "读取短信",
    "SEND_SMS" to "发送短信",
    "RECEIVE_SMS" to "接收短信",
    "READ_CONTACTS" to "读取联系人",
    "WRITE_CONTACTS" to "修改联系人",
    "RECORD_AUDIO" to "录音",
    "RECORD_VIDEO" to "录制视频",
    "CALL_PHONE" to "拨打电话",
    "READ_CALL_LOG" to "读取通话记录",
    "WRITE_CALL_LOG" to "修改通话记录",
    "READ_CALENDAR" to "读取日历",
    "WRITE_CALENDAR" to "修改日历",
    "ACCESS_FINE_LOCATION" to "精确定位",
    "ACCESS_COARSE_LOCATION" to "大致定位",
    "ACCESS_BACKGROUND_LOCATION" to "后台定位",
    "CAMERA" to "使用摄像头",
    "READ_EXTERNAL_STORAGE" to "读取存储空间",
    "WRITE_EXTERNAL_STORAGE" to "写入存储空间",
    "READ_MEDIA_IMAGES" to "读取图片",
    "READ_MEDIA_VIDEO" to "读取视频",
    "READ_MEDIA_AUDIO" to "读取音频",
    "QUERY_ALL_PACKAGES" to "查看已安装应用列表",
    "READ_PHONE_STATE" to "读取设备信息",
    "BODY_SENSORS" to "读取身体传感器",
    "ACCESS_WIFI_STATE" to "查看 Wi-Fi 状态",
    "ACCESS_NETWORK_STATE" to "查看网络状态",
    "INTERNET" to "访问互联网",
    "VIBRATE" to "控制震动",
    "FOREGROUND_SERVICE" to "运行前台服务",
    "FOREGROUND_SERVICE_DATA_SYNC" to "前台数据同步服务",
    "FOREGROUND_SERVICE_SPECIAL_USE" to "前台特殊用途服务",
    "BLUETOOTH" to "使用蓝牙",
    "NFC" to "使用 NFC",
    "POST_NOTIFICATIONS" to "发送通知",
    "RECEIVE_BOOT_COMPLETED" to "开机自启动",
    "WAKE_LOCK" to "保持设备唤醒",
    "EXPAND_STATUS_BAR" to "展开状态栏",
    "READ_APP_SPECIFIC_LOCALES" to "读取应用语言设置",
    "REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" to "忽略电池优化",
    "SET_ALARM" to "设置闹钟",
    "SCHEDULE_EXACT_ALARM" to "设置精确闹钟",
    "USE_BIOMETRIC" to "使用生物识别",
    "USE_FINGERPRINT" to "使用指纹识别",
    "MANAGE_EXTERNAL_STORAGE" to "管理所有文件",
    "SYSTEM_ALERT_WINDOW" to "悬浮窗显示",
    "AUTHENTICATE_ACCOUNTS" to "验证账户",
    "BILLING" to "应用内购买",
    "BIND_GET_INSTALL_REFERRER" to "获取安装来源",
    "CONFIGURATOR_READ" to "读取配置",
    "FOREGROUND_SERVICE_MICROPHONE" to "前台麦克风服务",
    "AD_ID" to "广告 ID",
    "READ_GSERVICES" to "读取 Google 服务配置",
    "ACCESS_ADSERVICES_ATTRIBUTION" to "广告服务归因",
    "BLUETOOTH_CONNECT" to "蓝牙连接",
    "BLUETOOTH_ADVERTISE" to "蓝牙广播",
    "BLUETOOTH_SCAN" to "蓝牙扫描",
    "REORDER_TASKS" to "重新排序任务",
    "REQUEST_INSTALL_PACKAGES" to "请求安装包",
    "CAPTURE_KEYBOARD" to "捕获键盘输入",
    "DOWNLOAD_WITHOUT_NOTIFICATION" to "静默下载",
    "FOREGROUND_SERVICE_CAMERA" to "前台相机服务",
    "FOREGROUND_SERVICE_MEDIA_PROJECTION" to "前台媒体投影服务",
    "FOREGROUND_SERVICE_MEDIA_PLAYBACK" to "前台媒体播放服务",
    "RUN_USER_INITIATED_JOBS" to "用户发起任务",
    "GET_ACCOUNTS" to "获取账户",
    "MANAGE_ACCOUNTS" to "管理账户",
    "MODIFY_AUDIO_SETTINGS" to "修改音频设置",
    "READ_MEDIA_VISUAL_USER_SELECTED" to "读取用户选择的多媒体",
    "REPOSITION_SELF_WINDOWS" to "重定位自身窗口",
    "REQUEST_FULLSCREEN_MODE" to "请求全屏模式",
    "USE_CREDENTIALS" to "使用凭证",
    "CREDENTIAL_MANAGER_QUERY_CANDIDATE_CREDENTIALS" to "查询候选凭证",
    "CREDENTIAL_MANAGER_SET_ALLOWED_PROVIDERS" to "设置凭证提供者",
    "CREDENTIAL_MANAGER_SET_ORIGIN" to "设置凭证来源",
    "QUERY_ADVANCED_PROTECTION_MODE" to "查询高级保护模式",
    "USE_LOOPBACK_INTERFACE" to "使用环回接口",
    "SCENE_UNDERSTANDING_FINE" to "场景理解（精细）",
    "HAND_TRACKING" to "手势追踪",
    "C2D_MESSAGE" to "C2D 消息",
    "READ_WRITE_BOOKMARK_FOLDERS" to "读写书签文件夹",
    "TOS_ACKED" to "条款确认",
    "DEVICE_EXTRAS" to "设备附加信息",
    "RECEIVE" to "接收消息",
    "INSTALL_SHORTCUT" to "创建快捷方式",
    "CURRENT_ACCOUNT_ACCESS" to "当前账户访问",
    "USE_PINNED_WINDOWING_LAYER" to "固定窗口层",
    "SupplementaryDID_ACCESS" to "补充设备 ID",
    "MSA_ACCESS" to "MSA 访问",
    "BIND_SERVICE" to "绑定服务",
    "PROCESS_PUSH_MSG" to "处理推送消息",
    "PUSH_PROVIDER" to "推送提供者",
    "FLASHLIGHT" to "闪光灯",
    "HIGH_SAMPLING_RATE_SENSORS" to "高采样率传感器",
    "DETECT_SCREEN_CAPTURE" to "检测屏幕捕获",
    "READ_ATTRIBUTION" to "读取归因信息",
    "MIPUSH_RECEIVE" to "小米推送接收",
    // 新增权限
    "USE_EXACT_ALARM" to "精确闹钟",
    "ACCESS_MEDIA_LOCATION" to "访问媒体位置",
    "CHANGE_WIFI_STATE" to "修改 Wi-Fi 状态",
    "CHANGE_NETWORK_STATE" to "修改网络状态",
    "WRITE_SETTINGS" to "修改系统设置",
    "READ_SETTINGS" to "读取系统设置",
    "CAPTURE_AUDIO_OUTPUT" to "捕获音频输出",
    "WRITE_SYNC_SETTINGS" to "写入同步设置",
    "CHANGE_WIFI_MULTICAST_STATE" to "修改 Wi-Fi 组播状态",
    "READ_AWARE_PROVIDER" to "读取感知服务",
    "WRITE_AWARE_PROVIDER" to "写入感知服务",
    "NETWORK_CHANGE_REPORT" to "网络变化报告",
    "BROADCAST_BADGE" to "广播角标",
    "PROVIDER_INSERT_BADGE" to "插入角标",
    "UPDATE_BADGE" to "更新角标",
    "CHANGE_BADGE" to "修改角标",
    "UPDATE_SHORTCUT" to "更新快捷方式",
    "BROADCAST_STICKY" to "粘性广播",
    "SYSTEM_OVERLAY_WINDOW" to "系统悬浮窗",
    "HIDE_OVERLAY_WINDOWS" to "隐藏悬浮窗",
    "READ_APP_BADGE" to "读取应用角标",
    "BADGE_COUNT_READ" to "读取角标数量",
    "BADGE_COUNT_WRITE" to "写入角标数量",
    "UPDATE_APP_BADGE" to "更新应用角标",
    "BADGE_ICON" to "角标图标",
    "ACCESS_HW_KEYSTORE" to "访问硬件密钥库",
    "ACCESS_SOTER_KEYSTORE" to "访问 Soter 密钥库",
    "USE_DEVICE_CREDENTIAL" to "使用设备凭证",
    "READ_CLIPBOARD" to "读取剪贴板",
    "READ_DATABASE" to "读取数据库",
    "WRITE_DATABASE" to "写入数据库",
    "READ_DATA" to "读取数据",
    "CHANGE_CONFIGURATION" to "修改配置",
    "INSTALL_WIDGET" to "安装小部件",
    "GET_COMMON_DATA" to "获取通用数据",
    "ACCESS_SEARCH_SERVICE" to "访问搜索服务",
    "POWER_MODE_LEVEL" to "电源模式等级",
    "BIND_ONETOUCHSHARE_SERVICE" to "绑定一键分享服务",
    "ACCESS_APP_HANDOFF" to "访问应用接力",
    "READ_PUSH_NOTIFICATION_INFO" to "读取推送通知信息",
    "ACCESS_THREAT_DETECTION" to "访问威胁检测",
    "ACCESS_XSOF" to "访问 XSOF",
    "KIT_SERVICE_ACCESS" to "访问套件服务",
    "YUMME_SESSION_INSTALL_BROADCAST" to "Yumme 会话安装广播",
    "CUSTOM_INSTALL_BROADCAST" to "自定义安装广播",
    "LUNA_SESSION_INSTALL_BROADCAST" to "Luna 会话安装广播",
    "MINIAPP_PROCESS_COMMUNICATION" to "小程序进程通信",
    "MY_BROADCAST_PERMISSION" to "我的广播权限",
    "DETECT_SCREEN_RECORDING" to "检测屏幕录制",
    "ACCESS_MEDIA_LOCATION" to "访问媒体位置",
    "READ_STEPS" to "读取步数",
    "ACCELEROMETER" to "加速度传感器",
    "GYROSCOPE" to "陀螺仪传感器",
    "WRITE_SETTINGS" to "修改系统设置",
    "READ_SETTINGS" to "读取系统设置"
)

    private fun translatePermission(perm: String): String {
        val shortName = perm.substringAfterLast(".")
        val translation = permissionTranslations.entries.firstOrNull {
            shortName.uppercase().contains(it.key)
        }?.value
        return if (translation != null) "• $translation" else "• $shortName"
    }

    private lateinit var tvRisk: TextView
    private lateinit var tvSource: TextView
    private lateinit var tvReason: TextView
    private lateinit var btnTrust: Button
    private lateinit var pkgName: String
    private lateinit var appLabel: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_detail)

        val ivAppIcon = findViewById<ImageView>(R.id.ivAppIcon)
        val tvAppName = findViewById<TextView>(R.id.tvAppName)
        val tvPackageName = findViewById<TextView>(R.id.tvPackageName)
        tvRisk = findViewById(R.id.tvRisk)
        tvSource = findViewById(R.id.tvSource)
        tvReason = findViewById(R.id.tvReason)
        val tvPermissions = findViewById<TextView>(R.id.tvPermissions)
        val btnSettings = findViewById<Button>(R.id.btnSettings)
        btnTrust = findViewById(R.id.btnTrust)

        pkgName = intent.getStringExtra("PACKAGE_NAME") ?: ""
        if (pkgName.isBlank()) {
            Toast.makeText(this, getString(R.string.toast_no_package), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val pm = packageManager
        appLabel = pkgName
        try {
            val ai = pm.getApplicationInfo(pkgName, 0)
            appLabel = pm.getApplicationLabel(ai).toString()
            ivAppIcon.setImageDrawable(pm.getApplicationIcon(pkgName))
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w("AppDetailActivity", "Package not found: $pkgName", e)
        }

        val grantedPermissions = mutableListOf<String>()
        val grantedMap = mutableMapOf<String, Boolean>()
        try {
            val pkgInfo: PackageInfo = pm.getPackageInfo(pkgName, PackageManager.GET_PERMISSIONS)
            val requested = pkgInfo.requestedPermissions
            val flags = pkgInfo.requestedPermissionsFlags
            if (requested != null && flags != null) {
                for (i in requested.indices) {
                    val granted = (flags[i] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
                    grantedMap[requested[i]] = granted
                    if (granted) grantedPermissions.add(requested[i])
                }
            }
        } catch (e: Exception) {
            Log.e("AppDetailActivity", "Failed to load permissions for $pkgName", e)
        }

        val (risk, sourceReason) = RiskCalculator.calculate(this, pkgName, grantedPermissions)
        val splitInfo = sourceReason.split("•", limit = 2)
        val source = splitInfo.getOrNull(0)?.trim() ?: getString(R.string.source_unknown)
        val reason = splitInfo.getOrNull(1)?.trim() ?: getString(R.string.reason_no_context)

        val riskEmoji = when {
            risk.contains("高风险", true) || risk.contains("High", true) -> "🔴"
            risk.contains("中风险", true) || risk.contains("Medium", true) -> "🟠"
            risk.contains("低风险", true) || risk.contains("Low", true) -> "🟡"
            risk.contains("安全", true) || risk.contains("Safe", true) -> "🟢"
            else -> "⚪"
        }

        tvAppName.text = appLabel
        tvPackageName.text = pkgName
        tvRisk.text = getString(R.string.textview_showing_risk, riskEmoji, risk)
        tvSource.text = getString(R.string.textview_source, source)
        tvReason.text = getString(R.string.textview_reason, reason)

        applyRiskColor(risk, animate = false)
        updateTrustButton()

        val sb = SpannableStringBuilder()
        val highRiskKeywords = listOf(
            "READ_SMS", "SEND_SMS", "RECEIVE_SMS", "READ_CONTACTS",
            "WRITE_CONTACTS", "ACCESS_FINE_LOCATION", "ACCESS_COARSE_LOCATION",
            "RECORD_AUDIO", "CALL_PHONE"
        )

        if (grantedMap.isEmpty()) {
            tvPermissions.text = getString(R.string.textview_no_permissions_found)
        } else {
            sb.append("权限列表：\n\n")
            grantedMap.forEach { (perm, granted) ->
                val displayName = translatePermission(perm)
                val start = sb.length
                sb.append(displayName)
                if (!granted) {
                    sb.append("（未授权）")
                }
                val end = sb.length

                val isHigh = highRiskKeywords.any { kw -> perm.uppercase().contains(kw) }
                if (isHigh && granted) {
                    val highlightColor = ContextCompat.getColor(this, R.color.permissionHighlight)
                    sb.setSpan(BackgroundColorSpan(highlightColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    sb.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                sb.append("\n")
            }
            tvPermissions.text = sb
        }

        btnSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = "package:$pkgName".toUri()
            }
            startActivity(intent)
        }

        btnTrust.setOnClickListener {
            val prefs = getSharedPreferences("trusted_apps", MODE_PRIVATE)
            val trusted = prefs.getBoolean(pkgName, false)
            if (trusted) {
                prefs.edit { remove(pkgName) }
                Toast.makeText(this, getString(R.string.toast_app_untrusted, appLabel), Toast.LENGTH_SHORT).show()
            } else {
                prefs.edit { putBoolean(pkgName, true) }
                Toast.makeText(this, getString(R.string.toast_app_trusted, appLabel), Toast.LENGTH_SHORT).show()
            }

            val (newRisk, newSourceReason) = RiskCalculator.calculate(this, pkgName, grantedPermissions)
            val newSplit = newSourceReason.split("•", limit = 2)
            tvRisk.text = getString(R.string.textview_risk_text, riskEmoji, newRisk)
            tvSource.text =
                getString(R.string.textview_source_text, newSplit.getOrNull(0)?.trim() ?: getString(R.string.source_unknown))
            tvReason.text = getString(
                R.string.textview_reason_text,
                newSplit.getOrNull(1)?.trim() ?: getString(R.string.reason_no_context)
            )

            applyRiskColor(newRisk, animate = true)
            updateTrustButton()
        }
    }

    private fun updateTrustButton() {
        val prefs = getSharedPreferences("trusted_apps", MODE_PRIVATE)
        val trusted = prefs.getBoolean(pkgName, false)
        btnTrust.text = if (trusted)
            getString(R.string.button_untrust_app)
        else
            getString(R.string.button_trust_app)
    }

    private fun applyRiskColor(risk: String, animate: Boolean) {
        val label = risk.lowercase()
        val colorText: Int
        val colorCard: Int

        when {
            label.contains("高风险") || label.contains("high") -> {
                colorText = "#FF5252".toColorInt()
                colorCard = "#33FF5252".toColorInt()
            }
            label.contains("中风险") || label.contains("medium") -> {
                colorText = "#FFA000".toColorInt()
                colorCard = "#33FFA000".toColorInt()
            }
            label.contains("低风险") || label.contains("low") -> {
                colorText = "#FFEB3B".toColorInt()
                colorCard = "#33FFEB3B".toColorInt()
            }
            label.contains("安全") || label.contains("safe") -> {
                colorText = "#00C853".toColorInt()
                colorCard = "#3300C853".toColorInt()
            }
            label.contains("信任") || label.contains("trusted") -> {
                colorText = "#2196F3".toColorInt()
                colorCard = "#332196F3".toColorInt()
            }
            else -> {
                colorText = "#9E9E9E".toColorInt()
                colorCard = "#222222".toColorInt()
            }
        }

        val card = findViewById<LinearLayout>(R.id.riskInfoCard)

        if (animate) {
            val currentTextColor = tvRisk.currentTextColor
            val textAnim = ValueAnimator.ofObject(ArgbEvaluator(), currentTextColor, colorText)
            textAnim.addUpdateListener { animator ->
                tvRisk.setTextColor(animator.animatedValue as Int)
            }

            val currentBg = (card.background as? android.graphics.drawable.ColorDrawable)?.color ?: Color.TRANSPARENT
            val bgAnim = ValueAnimator.ofObject(ArgbEvaluator(), currentBg, colorCard)
            bgAnim.addUpdateListener { animator ->
                card.setBackgroundColor(animator.animatedValue as Int)
            }

            textAnim.duration = 400
            bgAnim.duration = 400
            textAnim.start()
            bgAnim.start()
        } else {
            tvRisk.setTextColor(colorText)
            card.setBackgroundColor(colorCard)
        }

        tvRisk.setTypeface(tvRisk.typeface, Typeface.BOLD)
    }
}
