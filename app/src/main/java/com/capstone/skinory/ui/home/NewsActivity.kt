package com.capstone.skinory.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.skinory.BuildConfig
import com.capstone.skinory.data.remote.retrofit.ApiNewsConfig
import com.capstone.skinory.databinding.ActivityNewsBinding
import kotlinx.coroutines.DelicateCoroutinesApi
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
        supportActionBar?.title = "Article"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
        setupLoading()
        fetchNews()
    }

    private fun setupLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@NewsActivity)
            adapter = newsAdapter
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun fetchNews() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val newsResponse = ApiNewsConfig.getApiService().getNews(BuildConfig.API_NEWS_KEY)
                newsAdapter.submitList(newsResponse.articles)

                binding.progressBar.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@NewsActivity, "Failed to load news", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}