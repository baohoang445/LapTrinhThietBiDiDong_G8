package com.example.onlineshop.ViewModel

import androidx.lifecycle.ViewModel
import com.example.onlineshop.Repository.MainRepository
import androidx.lifecycle.LiveData
import com.example.onlineshop.Model.CategoryModel
import com.example.onlineshop.Model.ItemsModel
import com.example.onlineshop.Model.SliderModel

class MainViewModel():ViewModel() {
    private val repository = MainRepository()
    fun loadBanner(): LiveData<MutableList<SliderModel>> {
        return repository.loadBanner()
    }

    fun loadCategory():LiveData<MutableList<CategoryModel>>{
        return repository.loadCategory()
    }

    fun loadPopular(): LiveData<MutableList<ItemsModel>> {
        return repository.loadPopular()
    }

    fun loadFiltered(id:String): LiveData<MutableList<ItemsModel>> {
        return repository.loadFilterd(id)
    }

    fun loadAllItems(): LiveData<MutableList<ItemsModel>> {
        return repository.loadAllItems()
    }
}