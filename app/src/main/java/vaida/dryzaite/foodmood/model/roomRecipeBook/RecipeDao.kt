package vaida.dryzaite.foodmood.model.roomRecipeBook

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.selects.select
import vaida.dryzaite.foodmood.model.CacheRecipeEntry
import vaida.dryzaite.foodmood.model.RecipeEntry

//creating interface for DAO
@Dao
interface RecipeDao {
    @Insert(entity = RecipeEntry::class, onConflict = OnConflictStrategy.REPLACE)
    fun insertRecipe(recipe: RecipeEntry)

    @Update(entity = RecipeEntry::class)
   fun updateRecipe(recipe: RecipeEntry)

    @Delete(entity = RecipeEntry::class)
    fun deleteRecipe(recipe: RecipeEntry)

    //live data so that room update it automatically
    @Query("SELECT * from recipe_table ORDER BY title ASC")
    fun getAllRecipes(): LiveData<List<RecipeEntry>>

    //Selects and returns the recipe with given id(live data)
    @Query("SELECT * from recipe_table WHERE id = :key")
    fun getRecipeWithId(key: String): LiveData<RecipeEntry>

    //Select Favorites
    @Query("SELECT * from recipe_table WHERE is_favorite = 1 ORDER BY title ASC")
    fun getFavorites(): LiveData<List<RecipeEntry>>

    //Filter by meal and/or fish/veg
    @Query("SELECT * from recipe_table WHERE type_meal = :meal ORDER BY title ASC")
    fun getFilteredRecipes(meal: Int): LiveData<List<RecipeEntry>>

    // DAO methods for Cached recipes - to add all and to retrieve

    @Query("SELECT * from cached_recipe_table ORDER BY title ASC")
    fun getCachedRecipes(): LiveData<List<CacheRecipeEntry>>

    @Insert(entity = CacheRecipeEntry::class, onConflict = OnConflictStrategy.REPLACE)
    fun insertCachedRecipes(results: List<CacheRecipeEntry>)

}