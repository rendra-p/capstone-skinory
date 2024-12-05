package com.capstone.skinory.ui.notifications

import android.Manifest
import android.app.AlarmManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.skinory.data.Injection
import com.capstone.skinory.data.UserPreferences
import com.capstone.skinory.data.remote.response.GroupedRoutinesItem
import com.capstone.skinory.databinding.FragmentNotificationsBinding
import com.capstone.skinory.ui.ViewModelFactory
import com.capstone.skinory.ui.login.LoginViewModel
import com.capstone.skinory.ui.notifications.chose.ChoseActivity
import com.capstone.skinory.ui.notifications.notify.NotificationHelper

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

        checkAndRequestAlarmPermission()
        hasExactAlarmPermission()
        setupNotificationSwitch()
        setupRecyclerView()
        observeRoutines()
        setupFloatingActionButton()

        // Fetch routines when fragment is created
        routineViewModel.fetchRoutines()

        return binding.root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin diberikan, lanjutkan dengan penjadwalan notifikasi
                Log.d("NotificationsFragment", "Notification permission granted")
                notificationHelper.scheduleNotifications()
            } else {
                // Izin ditolak
                Log.d("NotificationsFragment", "Notification permission denied")
                // Bisa menampilkan pesan atau dialog ke pengguna
                binding.switch1.isChecked = false
            }
        }
    }

    private fun checkAndRequestAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                try {
                    // Tampilkan dialog untuk meminta izin
                    AlertDialog.Builder(requireContext())
                        .setTitle("Izin Alarm Diperlukan")
                        .setMessage("Aplikasi membutuhkan izin untuk menjadwalkan alarm tepat. Buka pengaturan?")
                        .setPositiveButton("Buka Pengaturan") { _, _ ->
                            // Buka pengaturan sistem untuk mengizinkan exact alarm
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                            startActivity(intent)
                        }
                        .setNegativeButton("Batal", null)
                        .show()
                } catch (e: ActivityNotFoundException) {
                    // Fallback jika intent tidak tersedia
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.parse("package:${requireContext().packageName}")
                        startActivity(intent)
                    } catch (ex: Exception) {
                        Log.e("NotificationsFragment", "Tidak dapat membuka pengaturan", ex)
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
            true // Untuk versi Android di bawah 12, izin tidak diperlukan
        }
    }

    private fun setupNotificationSwitch() {
        binding.switch1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Untuk Android 13+, minta izin terlebih dahulu
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestNotificationPermission()
                } else {
                    // Untuk versi Android di bawah 13, langsung jadwalkan
                    notificationHelper.scheduleNotifications()
                    userPreferences.setNotificationsEnabled(true)
                    Log.d("NotificationsFragment", "Notifications enabled")
                }
            } else {
                // Matikan notifikasi
                notificationHelper.cancelNotifications()
                userPreferences.setNotificationsEnabled(false)
                Log.d("NotificationsFragment", "Notifications disabled")
            }
        }

        // Set initial switch state from user preferences
        binding.switch1.isChecked = userPreferences.areNotificationsEnabled()
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
        // Inisialisasi notificationAdapter terlebih dahulu
        notificationAdapter = NotificationAdapter(
            viewModel = routineViewModel,
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

        // Setelah menginisialisasi notificationAdapter, atur adapter untuk RecyclerView
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

                // Enable/disable FloatingActionButton based on active routines
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
            // Only allow adding new routine if less than 2 active routines
            startActivity(Intent(requireContext(), ChoseActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }
}