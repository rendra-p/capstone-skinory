package com.capstone.skinory.ui.result

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.skinory.data.Injection
import com.capstone.skinory.databinding.FragmentResultBinding

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!
    private lateinit var resultViewModel: ResultViewModel
    private lateinit var bestProductAdapter: BestProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)

        val viewModelFactory = Injection.provideViewModelFactory(requireContext())
        resultViewModel = ViewModelProvider(this, viewModelFactory)[ResultViewModel::class.java]

        setupRecyclerView()
        observeViewModel()

        resultViewModel.fetchBestProducts()

        return binding.root
    }

    private fun setupRecyclerView() {
        bestProductAdapter = BestProductAdapter()
        binding.rvBest.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = bestProductAdapter

            // Optional: Tambahkan dekorasi item
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    private fun observeViewModel() {
        resultViewModel.bestProducts.observe(viewLifecycleOwner) { products ->
            Log.d("ResultFragment", "Products received: ${products.size}")
            products.forEachIndexed { index, product ->
                Log.d("ResultFragment", "Product $index: ${product.nameProduct}")
            }
            bestProductAdapter.submitList(products)
        }

        resultViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        resultViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}