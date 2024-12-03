package com.capstone.skinory.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.skinory.BuildConfig
import com.capstone.skinory.R
import com.capstone.skinory.data.Injection
import com.capstone.skinory.data.remote.retrofit.ApiNewsConfig
import com.capstone.skinory.databinding.FragmentHomeBinding
import com.capstone.skinory.ui.notifications.RoutineViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var newsAdapter: NewsAdapter

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

        setText()
        setupButton()
        setupRecyclerView()
        fetchNews()

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

    private fun setupButton() {
        binding.button2.setOnClickListener {
            val bottomNavView: BottomNavigationView = requireActivity().findViewById(R.id.nav_view)
            bottomNavView.selectedItemId = R.id.navigation_result
        }
        binding.textView12.setOnClickListener {
            startActivity(Intent(requireContext(), NewsActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = newsAdapter
        }
    }

    private fun fetchNews() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val newsResponse = ApiNewsConfig.getApiService().getNews(BuildConfig.API_NEWS_KEY)
                // Limit to first 3 items for home fragment
                newsAdapter.submitList(newsResponse.articles.take(3))
            } catch (e: Exception) {
                // Handle error
                Toast.makeText(context, "Failed to load news", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}