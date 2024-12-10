package com.capstone.skinory.ui.notifications

import android.Manifest
import android.app.AlarmManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.skinory.data.Injection
import com.capstone.skinory.data.UserPreferences
import com.capstone.skinory.data.remote.response.GroupedRoutinesItem
import com.capstone.skinory.databinding.FragmentNotificationsBinding
import com.capstone.skinory.ui.notifications.chose.ChoseActivity
import com.capstone.skinory.ui.notifications.notify.NotificationHelper
import java.util.Locale

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var routineViewModel: RoutineViewModel
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        userPreferences = UserPreferences(requireContext())
        notificationHelper = NotificationHelper(requireContext())

        val viewModelFactory = Injection.provideViewModelFactory(requireContext())
        routineViewModel = ViewModelProvider(this, viewModelFactory)[RoutineViewModel::class.java]

        if (userPreferences.areNotificationsEnabled()) {
            if (hasExactAlarmPermission() &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
            ) {
                notificationHelper.scheduleNotifications()
            } else {
                userPreferences.setNotificationsEnabled(false)
                binding.switchNotification.isChecked = false
            }
        }

        checkAndRequestAlarmPermission()
        hasExactAlarmPermission()
        setupNotificationSwitch()
        setupRecyclerView()
        observeRoutines()
        setupFloatingActionButton()
        routineViewModel.fetchRoutines()

        return binding.root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("NotificationsFragment", "Notification permission granted")

                    if (hasExactAlarmPermission()) {
                        binding.switchNotification.isChecked = true
                        notificationHelper.scheduleNotifications()
                        userPreferences.setNotificationsEnabled(true)
                    } else {
                        checkAndRequestAlarmPermission()
                    }
                } else {
                    Log.d("NotificationsFragment", "Notification permission denied")
                    binding.switchNotification.isChecked = false

                    showPermissionRationaleDialog()
                }
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("Notifications are required to remind you of your skin care schedule. Please provide permission.")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun checkAndRequestAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                try {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Permission Required")
                        .setMessage("The application needs permission to schedule proper alarms. Go to Settings?")
                        .setPositiveButton("Open Settings") { _, _ ->
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                            startActivity(intent)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } catch (e: ActivityNotFoundException) {
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.parse("package:${requireContext().packageName}")
                        startActivity(intent)
                    } catch (ex: Exception) {
                        Log.e("NotificationsFragment", "Unable to open settings", ex)
                    }
                }
            }
        }
    }

    private fun hasExactAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun setupNotificationSwitch() {
        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            when {
                isChecked -> {
                    if (!userPreferences.isAutoStartPermissionRequested()) {
                        checkAutoStartPermission()
                    }

                    if (!hasExactAlarmPermission()) {
                        checkAndRequestAlarmPermission()
                        binding.switchNotification.isChecked = false
                        return@setOnCheckedChangeListener
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            requestNotificationPermission()
                            binding.switchNotification.isChecked = false
                            return@setOnCheckedChangeListener
                        }
                    }

                    checkBatteryOptimization()

                    notificationHelper.scheduleNotifications()
                    userPreferences.setNotificationsEnabled(true)

                    Log.d("NotificationsFragment", "Notifications enabled")
                }
                else -> {
                    notificationHelper.cancelNotifications()
                    userPreferences.setNotificationsEnabled(false)
                    userPreferences.setAutoStartPermissionRequested(false)
                    Log.d("NotificationsFragment", "Notifications disabled")
                }
            }
        }

        binding.switchNotification.isChecked = userPreferences.areNotificationsEnabled()
    }

    private fun checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
            val packageName = requireContext().packageName

            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Battery Optimization")
                    .setMessage("Disable battery optimization to ensure notifications run smoothly?")
                    .setPositiveButton("Open Settings") { _, _ ->
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                            intent.data = Uri.parse("package:$packageName")
                            startActivity(intent)
                        } catch (e: Exception) {
                            Log.e("NotificationsFragment", "Unable to open battery optimization settings", e)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(
            onDeleteClick = { routine ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Routine")
                    .setMessage("Are you sure you want to delete this routine?")
                    .setPositiveButton("Yes") { _, _ ->
                        routineViewModel.deleteRoutine(routine.applied == "Day")
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = notificationAdapter
    }

    private fun observeRoutines() {
        routineViewModel.dayRoutines.observe(viewLifecycleOwner) { dayRoutines ->
            routineViewModel.nightRoutines.observe(viewLifecycleOwner) { nightRoutines ->
                val combinedRoutines = (dayRoutines + nightRoutines)
                    .groupBy { it.applied }
                    .map { (applied, routines) ->
                        GroupedRoutinesItem(
                            applied = applied ?: "Unknown",
                            products = routines.flatMap { it.nameProduct?.split(",")?.map { product -> product.trim() } ?: emptyList() }
                        )
                    }

                notificationAdapter.submitList(combinedRoutines)

                updateFloatingActionButtonState(combinedRoutines.size)
            }
        }
    }

    private fun updateFloatingActionButtonState(routinesCount: Int) {
        binding.floatingActionButton.isEnabled = routinesCount < 2
        binding.floatingActionButton.alpha = if (routinesCount < 2) 1f else 0.5f
    }

    private fun setupFloatingActionButton() {
        binding.floatingActionButton.setOnClickListener {
            startActivity(Intent(requireContext(), ChoseActivity::class.java))
        }
    }

    private fun checkAutoStartPermission() {
        val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
        if (isAutoStartPermissionRequired(manufacturer)) {
            if (!isAutoStartPermissionGranted(manufacturer)) {
                showAutoStartPermissionDialog()
            }
        }
    }

    private fun showAutoStartPermissionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Auto-Start Permission")
            .setMessage("To ensure notifications run smoothly, please allow the app to run in the background.")
            .setPositiveButton("Open Settings") { _, _ ->
                try {
                    val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
                    when (manufacturer) {
                        "xiaomi" -> openXiaomiAutoStartSettings()
                        "oppo" -> openOppoAutoStartSettings()
                        "vivo" -> openVivoAutoStartSettings()
                        "huawei" -> openHuaweiAutoStartSettings()
                        else -> openGenericAutoStartSettings()
                    }

                    userPreferences.setAutoStartPermissionRequested(true)
                } catch (e: Exception) {
                    Log.e("NotificationsFragment", "Unable to open auto-start settings", e)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun openXiaomiAutoStartSettings() {
        val intent = Intent().apply {
            component = ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            )
        }
        startActivity(intent)
    }

    private fun openOppoAutoStartSettings() {
        val intent = Intent().apply {
            component = ComponentName(
                "com.color.safecenter",
                "com.color.safecenter.permission.startup.StartupAppListActivity"
            )
        }
        startActivity(intent)
    }

    private fun openVivoAutoStartSettings() {
        val intent = Intent().apply {
            component = ComponentName(
                "com.vivo.permissionmanager",
                "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
            )
        }
        startActivity(intent)
    }

    private fun openHuaweiAutoStartSettings() {
        val intent = Intent().apply {
            component = ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.startsupport.dialog.ExtraDialogActivity"
            )
            putExtra("package", context?.packageName)
        }
        startActivity(intent)
    }

    private fun openGenericAutoStartSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun isAutoStartPermissionRequired(manufacturer: String): Boolean {
        return listOf("xiaomi", "oppo", "vivo", "huawei")
            .contains(manufacturer)
    }

    private fun isAutoStartPermissionGranted(manufacturer: String): Boolean {
        return try {
            when (manufacturer) {
                "xiaomi" -> isXiaomiAutoStartPermissionGranted()
                "oppo" -> isOppoAutoStartPermissionGranted()
                "vivo" -> isVivoAutoStartPermissionGranted()
                "huawei" -> isHuaweiAutoStartPermissionGranted()
                else -> true
            }
        } catch (e: Exception) {
            Log.e("NotificationsFragment", "Error checking auto-start permission", e)
            true // Default to true if we can't determine
        }
    }

    private fun isXiaomiAutoStartPermissionGranted(): Boolean {
        return try {
            val intent = Intent().apply {
                component = ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            }
            val resolveInfo = context?.packageManager?.resolveActivity(intent, 0)
            resolveInfo == null
        } catch (e: Exception) {
            Log.e("NotificationsFragment", "Error checking Xiaomi auto-start permission", e)
            true
        }
    }

    private fun isOppoAutoStartPermissionGranted(): Boolean {
        return try {
            val intent = Intent().apply {
                component = ComponentName(
                    "com.color.safecenter",
                    "com.color.safecenter.permission.startup.StartupAppListActivity"
                )
            }
            val resolveInfo = context?.packageManager?.resolveActivity(intent, 0)
            resolveInfo == null
        } catch (e: Exception) {
            Log.e("NotificationsFragment", "Error checking Oppo auto-start permission", e)
            true
        }
    }

    private fun isVivoAutoStartPermissionGranted(): Boolean {
        return try {
            val intent = Intent().apply {
                component = ComponentName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                )
            }
            val resolveInfo = context?.packageManager?.resolveActivity(intent, 0)
            resolveInfo == null
        } catch (e: Exception) {
            Log.e("NotificationsFragment", "Error checking Vivo auto-start permission", e)
            true
        }
    }

    private fun isHuaweiAutoStartPermissionGranted(): Boolean {
        return try {
            val intent = Intent().apply {
                component = ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.startsupport.dialog.ExtraDialogActivity"
                )
                putExtra("package", context?.packageName)
            }
            val resolveInfo = context?.packageManager?.resolveActivity(intent, 0)
            resolveInfo == null
        } catch (e: Exception) {
            Log.e("NotificationsFragment", "Error checking Huawei auto-start permission", e)
            true
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }
}