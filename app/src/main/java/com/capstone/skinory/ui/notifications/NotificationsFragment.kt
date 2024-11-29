package com.capstone.skinory.ui.notifications

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.skinory.data.Injection
import com.capstone.skinory.data.UserPreferences
import com.capstone.skinory.databinding.FragmentNotificationsBinding
import com.capstone.skinory.ui.ViewModelFactory
import com.capstone.skinory.ui.login.LoginViewModel
import com.capstone.skinory.ui.notifications.chose.ChoseActivity
import com.capstone.skinory.ui.notifications.notify.NotificationHelper

class NotificationsFragment : Fragment() {

    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var binding: FragmentNotificationsBinding
    private lateinit var routineViewModel: RoutineViewModel
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        notificationHelper = NotificationHelper(requireContext())
        userPreferences = UserPreferences(requireContext())

        val viewModelFactory = Injection.provideViewModelFactory(requireContext())
        routineViewModel = ViewModelProvider(this, viewModelFactory)[RoutineViewModel::class.java]

        requestNotificationPermission()
        setupRecyclerView()
        observeRoutines()
        setupFloatingActionButton()

        // Fetch routines when fragment is created
        routineViewModel.fetchRoutines()

        return binding.root


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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                // Izin diberikan, lanjutkan dengan pengaturan notifikasi
                setupNotificationSwitch()
            } else {
                // Izin ditolak, beri tahu pengguna
                Toast.makeText(
                    requireContext(),
                    "Notification permission is required to send reminders",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupNotificationSwitch() {
        // Ambil status terakhir dari UserPreferences
        val isNotificationEnabled = userPreferences.getNotificationStatus()
        binding.switch1.isChecked = isNotificationEnabled

        binding.switch1.setOnCheckedChangeListener { _, isChecked ->
            // Simpan status switch di UserPreferences
            userPreferences.saveNotificationStatus(isChecked)

            if (isChecked) {
                // Aktifkan notifikasi untuk Day (jam 6 pagi)
                notificationHelper.scheduleRoutineReminder(true)
                // Aktifkan notifikasi untuk Night (jam 8 malam)
                notificationHelper.scheduleRoutineReminder(false)
            } else {
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
                val combinedRoutines = dayRoutines + nightRoutines

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

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }
}