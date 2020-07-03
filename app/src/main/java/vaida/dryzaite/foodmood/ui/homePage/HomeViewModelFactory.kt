package vaida.dryzaite.foodmood.ui.homePage

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import vaida.dryzaite.foodmood.model.room.RecipeDao

class HomeViewModelFactory(private val dataSource: RecipeDao, private val application: Application)
    : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(dataSource, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }



}