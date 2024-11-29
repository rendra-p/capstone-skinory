package com.capstone.skinory.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.capstone.skinory.R
import com.capstone.skinory.data.Injection
import com.capstone.skinory.databinding.FragmentHomeBinding
import com.capstone.skinory.ui.notifications.RoutineViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val viewModelFactory = Injection.provideViewModelFactory(requireContext())
        homeViewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]

        // Memanggil fungsi untuk mengambil token dan profil
        homeViewModel.fetchTokenAndProfile()

        // Menambahkan listener untuk tombol
        binding.button2.setOnClickListener {
            val bottomNavView: BottomNavigationView = requireActivity().findViewById(R.id.nav_view)
            bottomNavView.selectedItemId = R.id.navigation_result
        }

        setText()

        return binding.root
    }

    private fun setText() {
        homeViewModel.username.observe(viewLifecycleOwner) { username ->
            binding.textView5.text = getString(R.string.hello_username, username)
        }

        homeViewModel.skinType.observe(viewLifecycleOwner) { skinType ->
            binding.textView7.text = getString(R.string.skin_type_result, skinType)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}