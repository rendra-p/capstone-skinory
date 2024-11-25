package com.capstone.skinory.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.skinory.data.Injection
import com.capstone.skinory.databinding.FragmentNotificationsBinding
import com.capstone.skinory.ui.ViewModelFactory
import com.capstone.skinory.ui.login.LoginViewModel
import com.capstone.skinory.ui.notifications.chose.ChoseActivity

class NotificationsFragment : Fragment() {

    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var binding: FragmentNotificationsBinding
    private lateinit var routineViewModel: RoutineViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        val viewModelFactory = Injection.provideViewModelFactory(requireContext())
        routineViewModel = ViewModelProvider(this, viewModelFactory)[RoutineViewModel::class.java]

        setupRecyclerView()
        observeRoutines()
        setupFloatingActionButton()

        // Fetch routines when fragment is created
        routineViewModel.fetchRoutines()

        return binding.root
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter()
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
}