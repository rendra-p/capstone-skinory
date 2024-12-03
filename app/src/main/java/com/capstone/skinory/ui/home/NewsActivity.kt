package com.capstone.skinory.ui.home

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.skinory.BuildConfig
import com.capstone.skinory.R
import com.capstone.skinory.data.remote.retrofit.ApiNewsConfig
import com.capstone.skinory.databinding.ActivityNewsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NewsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewsBinding
    private lateinit var newsAdapter: NewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView
        setupRecyclerView()

        // Fetch news
        fetchNews()
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@NewsActivity)
            adapter = newsAdapter
        }
    }

    private fun fetchNews() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val newsResponse = ApiNewsConfig.getApiService().getNews(BuildConfig.API_NEWS_KEY)
                newsAdapter.submitList(newsResponse.articles)
            } catch (e: Exception) {
                Toast.makeText(this@NewsActivity, "Failed to load news", Toast.LENGTH_SHORT).show()
            }
        }
    }
}