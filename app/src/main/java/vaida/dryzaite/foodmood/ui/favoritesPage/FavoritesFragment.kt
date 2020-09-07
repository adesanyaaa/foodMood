package vaida.dryzaite.foodmood.ui.favoritesPage

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import vaida.dryzaite.foodmood.R
import vaida.dryzaite.foodmood.databinding.FragmentFavoritesBinding
import vaida.dryzaite.foodmood.model.RecipeEntry
import vaida.dryzaite.foodmood.ui.homePage.HomeViewModel
import vaida.dryzaite.foodmood.ui.main.MainActivity
import javax.inject.Inject


class FavoritesFragment : Fragment(), FavoritesAdapter.FavoritesAdapterListener {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var binding: FragmentFavoritesBinding
    private lateinit var adapter: FavoritesAdapter
    private lateinit var gridItemDecoration: RecyclerView.ItemDecoration

    private val viewModel: FavoritesViewModel by viewModels { viewModelFactory }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).mainComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate with View binding to optimize space, as no databinding used
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        setupRecyclerView()

        setupItemDecoration()
        setupScrollListener()

        //updating Live data observer with ViewModel data
        viewModel.getFavorites().observe(viewLifecycleOwner, Observer { recipes ->
            recipes?.let {
                adapter.updateRecipes(recipes)
            }
            checkForEmptyState()
        })

        //set up observer to react on item taps and enable navigation
        viewModel.navigateToRecipeDetail.observe(viewLifecycleOwner, Observer { keyId->
            keyId?.let {
                this.findNavController().navigate(
                    FavoritesFragmentDirections.actionFavoritesFragmentToRecipeFragment(keyId))
                viewModel.onRecipeDetailNavigated()
            }
        })

        // observer to react to favorite button click state
        viewModel.favoriteStatusChange.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                viewModel.onFavoriteClickCompleted()
            }
        })
    }

    //adding scroll listener for smooth animation
    private fun setupScrollListener() {
        binding.favoritesRecyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                adapter.scrollDirection = if (dy > 0) {
                    FavoritesAdapter.ScrollDirection.DOWN
                } else {
                    FavoritesAdapter.ScrollDirection.UP
                }
            }
        })
    }

    private fun setupAdapter() {
        adapter = FavoritesAdapter(mutableListOf(), FavoritesOnClickListener { id ->
            viewModel.onRecipeClicked(id)
        }, this)
    }

    private fun setupRecyclerView() {
        //setting up a Recyclerview
        val layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        binding.favoritesRecyclerview.layoutManager = layoutManager
        binding.favoritesRecyclerview.adapter = adapter
    }

    //if no items, empty state text is shown
    private fun checkForEmptyState() {
        binding.emptyState.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.INVISIBLE
    }

//    adding spacing decorator for equal spacing between grid items
    private fun setupItemDecoration() {
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.recipe_card_grid_layout_margin)
        gridItemDecoration = SpacingItemDecorator(3, spacingInPixels)
        binding.favoritesRecyclerview.addItemDecoration(gridItemDecoration)
    }

    override fun addFavorites(recipe: RecipeEntry) {
        viewModel.addFavorites(recipe)
    }

    override fun removeFavorites(recipe: RecipeEntry) {
        viewModel.removeFavorites(recipe)
    }


}