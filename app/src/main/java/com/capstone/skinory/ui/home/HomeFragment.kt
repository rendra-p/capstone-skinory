package com.capstone.skinory.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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

        homeViewModel.fetchTokenAndProfile()

        setText()
        setupButton()
        setupRecyclerView()
        setupLoadingNews()
        fetchNews()

        return binding.root
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

    private fun setupLoadingNews() {
        binding.progressBarNews.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun fetchNews() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val newsResponse = ApiNewsConfig.getApiService().getNews(BuildConfig.API_NEWS_KEY)
                newsAdapter.submitList(newsResponse.articles.take(5))

                binding.progressBarNews.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            } catch (e: Exception) {
                binding.progressBarNews.visibility = View.GONE
                Toast.makeText(context, "Failed to load news", Toast.LENGTH_SHORT).show()
            }
        }
    }
}