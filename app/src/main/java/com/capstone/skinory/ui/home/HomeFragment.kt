package com.capstone.skinory.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.skinory.BuildConfig
import com.capstone.skinory.R
import com.capstone.skinory.data.Injection
import com.capstone.skinory.data.remote.retrofit.ApiNewsConfig
import com.capstone.skinory.databinding.FragmentHomeBinding
import com.capstone.skinory.ui.analysis.AnalysisActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        _binding!!.progressBar.visibility = View.VISIBLE
        _binding!!.fragmentHomeContainer.visibility = View.GONE

        fetchDataSequentially()
        setText()
        setupButton()
        setupRecyclerView()

        return binding.root
    }

    private fun fetchDataSequentially() {
        homeViewModel.fetchTokenAndProfile()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.fragmentHomeContainer.visibility = View.GONE

                val newsResponse = withContext(Dispatchers.IO) {
                    ApiNewsConfig.getApiService().getNews(BuildConfig.API_NEWS_KEY)
                }

                withContext(Dispatchers.Main) {
                    newsAdapter.submitList(newsResponse.articles.take(5))

                    binding.progressBar.visibility = View.GONE
                    binding.fragmentHomeContainer.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to load news", Toast.LENGTH_SHORT).show()

                    binding.progressBar.visibility = View.GONE
                    binding.fragmentHomeContainer.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setText() {
        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.fragmentHomeContainer.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        homeViewModel.username.observe(viewLifecycleOwner) { username ->
            binding.tvHelloName.text = getString(R.string.hello_username, username)
        }

        homeViewModel.skinType.observe(viewLifecycleOwner) { skinType ->
            binding.tvSkinType.text = getString(R.string.skin_type_result, skinType)
        }

        homeViewModel.profileError.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupButton() {
        binding.btnMyresult.setOnClickListener {
            val bottomNavView: BottomNavigationView = requireActivity().findViewById(R.id.nav_view)
            bottomNavView.selectedItemId = R.id.navigation_result
        }
        binding.btnMypicture.setOnClickListener {
            startActivity(Intent(requireContext(), AnalysisActivity::class.java))
        }
        binding.tvBtnSeemore.setOnClickListener {
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
}