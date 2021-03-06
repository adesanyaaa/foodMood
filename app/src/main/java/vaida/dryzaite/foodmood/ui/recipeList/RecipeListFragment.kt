package vaida.dryzaite.foodmood.ui.recipeList

import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_recipe_list.*
import timber.log.Timber
import vaida.dryzaite.foodmood.R
import vaida.dryzaite.foodmood.databinding.FragmentRecipeListBinding
import vaida.dryzaite.foodmood.model.RecipeEntry
import vaida.dryzaite.foodmood.utilities.BUNDLE_KEY
import vaida.dryzaite.foodmood.utilities.DividerItemDecoration
import vaida.dryzaite.foodmood.utilities.ItemTouchHelperCallback
import vaida.dryzaite.foodmood.utilities.REQUEST_KEY

@AndroidEntryPoint
class RecipeListFragment : Fragment(), RecipeListAdapter.RecipeListAdapterListener {

    private lateinit var binding: FragmentRecipeListBinding
    private lateinit var adapter: RecipeListAdapter

    private val viewModel: RecipeListViewModel by viewModels()


    //handling back button clicks not to return to add-form
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(RecipeListFragmentDirections.actionRecipeListFragmentToHomeFragment2())
            }
        })
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recipe_list, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel


//      Set chip group checked change listener
        binding.chipMealTypeSelection.setOnCheckedChangeListener { group, checkedId ->
            val titleOrNull = chip_meal_type_selection.findViewById<Chip>(checkedId)?.text.toString()
            viewModel.onMealSelected(titleOrNull, resources)
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)

        setupViews()

        setupAdapter()
        setupRecyclerView()

        setupItemTouchHelper()

        // listening to data change in Favorites fragment and updating data in RV
        setFragmentResultListener(REQUEST_KEY) { key, bundle ->
            val result = bundle.getBoolean(BUNDLE_KEY)
            Timber.i("listening to result : $result")
            if (result) {
                viewModel.allRecipes.observe(viewLifecycleOwner, { recipes ->
                    recipes?.let {
                        adapter.recipes = it
                        Timber.i("adapter updated : ${it.map { recipe -> 
                            recipe.isFavorite }}")
                    }
                })
            }
            Timber.i("UPDATE FINISHED")
        }

        //updating Live data observer with ViewModel data - filtering applied
            viewModel.mealSelection.observe(viewLifecycleOwner, {
                viewModel.initFilter().observe(viewLifecycleOwner, { recipes ->
                    recipes?.let {
                        adapter.recipes = it
                    }
                    checkForEmptyState()
                })
            })

        addListDividerDecoration()


        //set up observer to react on item taps and enable navigation
        viewModel.navigateToRecipeDetail.observe(viewLifecycleOwner, { keyId ->
            keyId?.let {
                this.findNavController().navigate(
                    RecipeListFragmentDirections.actionRecipeListFragmentToRecipeFragment(keyId)
                )
                viewModel.onRecipeDetailNavigated()
            }
        })

        //observer to react on fav button state (if change needed or not)
        viewModel.favoriteStatusChange.observe(viewLifecycleOwner, {
            if (it == true) {
                viewModel.onFavoriteClickCompleted()
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.top_nav_menu, menu)

        //setup clicks on icons in toolbar
        val favoriteIcon = menu.findItem(R.id.favorite_menu_item)
        favoriteIcon.actionView.setOnClickListener {
            menu.performIdentifierAction(
                favoriteIcon.itemId,
                0
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Timber.i("onOptionsItemSelected")
        when (item.itemId) {
            R.id.favorite_menu_item -> {
                navigateToFavoritesPage()
                Timber.i("favorite selected")
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun deleteRecipeAtPosition(recipe: RecipeEntry) {
        viewModel.onDeleteRecipe(recipe)
    }

    override fun addFavorites(recipe: RecipeEntry) {
        Timber.i("addFavorites called  ")
        viewModel.addFavorites(recipe)
    }


    override fun removeFavorites(recipe: RecipeEntry) {
        Timber.i("RemoveFavorites called  ")
        viewModel.removeFavorites(recipe)
    }


    private fun addListDividerDecoration() {
        //adding list divider decorations
        val heightInPixels = resources.getDimensionPixelSize(R.dimen.list_item_divider_height)
        binding.recipeListRecyclerview.addItemDecoration(
            DividerItemDecoration(
                R.color.Text,
                heightInPixels
            )
        )
    }


    private fun setupAdapter() {
        adapter = RecipeListAdapter(this, RecipeListOnClickListener { recipe ->
            viewModel.onRecipeClicked(recipe)
        })
    }

    //setting up a Recyclerview
    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        binding.recipeListRecyclerview.layoutManager = layoutManager
        binding.recipeListRecyclerview.adapter = adapter
    }


    private fun setupViews() {
        viewModel.navigateToAddRecipeFragment.observe(viewLifecycleOwner, {
            if (it == true) {
                this.findNavController()
                    .navigate(R.id.action_recipeListFragment_to_addRecipeFragment)
                viewModel.onFabClicked()
            }
        })
    }

    // setting up drag&drop move
    private fun setupItemTouchHelper() {
        val itemTouchHelper = ItemTouchHelper(ItemTouchHelperCallback(adapter))
        itemTouchHelper.attachToRecyclerView(recipe_list_recyclerview)
    }

    //if no items, empty state text is shown
    private fun checkForEmptyState() {
        binding.emptyState.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.INVISIBLE
    }


    private fun navigateToFavoritesPage() {
        this.findNavController().navigate(
            RecipeListFragmentDirections.actionRecipeListFragmentToFavoritesFragment()
        )
        viewModel.onRecipeDetailNavigated()
    }

}
