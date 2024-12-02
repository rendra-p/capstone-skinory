package com.capstone.skinory.ui.notifications

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi permission launcher
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Izin diberikan, lanjutkan dengan pengaturan notifikasi
                setupNotificationSwitch()
                Toast.makeText(
                    requireContext(),
                    "Notification permission granted",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Izin ditolak
                Toast.makeText(
                    requireContext(),
                    "Notification permission is required",
                    Toast.LENGTH_LONG
                ).show()

                // Nonaktifkan switch
                binding.switch1.isChecked = false
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        notificationHelper = NotificationHelper(requireContext())
        userPreferences = UserPreferences(requireContext())

        val viewModelFactory = Injection.provideViewModelFactory(requireContext())
        routineViewModel = ViewModelProvider(this, viewModelFactory)[RoutineViewModel::class.java]

        checkNotificationPermission()
        setupRecyclerView()
        observeRoutines()
        setupFloatingActionButton()

        // Fetch routines when fragment is created
        routineViewModel.fetchRoutines()

        return binding.root


    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("NotificationPermission", "Permission already granted")
                    setupNotificationSwitch()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Log.d("NotificationPermission", "Show rationale dialog")
                    showPermissionRationaleDialog()
                }
                else -> {
                    Log.d("NotificationPermission", "Requesting permission")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            Log.d("NotificationPermission", "Below Android 13, setup switch directly")
            setupNotificationSwitch()
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Notification Permission")
            .setMessage("This app needs notification permission to send you routine reminders. Would you like to grant permission?")
            .setPositiveButton("Yes") { _, _ ->
                // Minta izin
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                // Nonaktifkan switch
                binding.switch1.isChecked = false
            }
            .create()
            .show()
    }

    private fun setupNotificationSwitch() {
        // Ambil status terakhir dari UserPreferences
        val isNotificationEnabled = userPreferences.getNotificationStatus()
        binding.switch1.isChecked = isNotificationEnabled

        binding.switch1.setOnCheckedChangeListener { _, isChecked ->
            // Pastikan izin sudah diberikan sebelum mengaktifkan notifikasi
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (isChecked && ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Minta izin jika belum diberikan
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    return@setOnCheckedChangeListener
                }
            }

            // Simpan status switch di UserPreferences
            Log.d("NotificationSwitch", "Switch state changed: $isChecked")
            userPreferences.saveNotificationStatus(isChecked)

            if (isChecked) {
                Log.d("NotificationSwitch", "Notifications enabled")
                // Aktifkan notifikasi untuk Day (jam 6 pagi)
                notificationHelper.scheduleRoutineReminder(true)
                // Aktifkan notifikasi untuk Night (jam 8 malam)
                notificationHelper.scheduleRoutineReminder(false)
            } else {
                Log.d("NotificationSwitch", "Notifications disabled")
                // Matikan notifikasi untuk Day
                notificationHelper.cancelRoutineReminder(true)
                // Matikan notifikasi untuk Night
                notificationHelper.cancelRoutineReminder(false)
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