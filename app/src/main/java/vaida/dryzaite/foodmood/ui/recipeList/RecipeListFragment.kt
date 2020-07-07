package vaida.dryzaite.foodmood.ui.recipeList

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_recipe_list.*
import timber.log.Timber
import vaida.dryzaite.foodmood.R
import vaida.dryzaite.foodmood.databinding.FragmentRecipeListBinding
import vaida.dryzaite.foodmood.model.RecipeEntry
import vaida.dryzaite.foodmood.model.room.RecipeDatabase
import vaida.dryzaite.foodmood.utilities.ItemTouchHelperCallback

class RecipeListFragment : Fragment(), RecipeListAdapter.RecipeListAdapterListener {

    private lateinit var recipeListViewModel: RecipeListViewModel
    private lateinit var binding: FragmentRecipeListBinding
    private lateinit var adapter: RecipeListAdapter
//    private val adapter = RecipeListAdapter(mutableListOf(), this, RecipeListOnClickListener { id ->
//        Toast.makeText(context, "$id", Toast.LENGTH_SHORT).show()
//    })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRecipeListBinding.inflate(inflater, container, false)

        //reference to context, to get database instance
        val application = requireNotNull(this.activity).application

        val viewModelFactory = RecipeListViewModelFactory(application)
        recipeListViewModel = ViewModelProvider(this, viewModelFactory).get(RecipeListViewModel::class.java)

        binding.lifecycleOwner = this
        binding.recipeListViewModel = recipeListViewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)

        setupViews()

        setupAdapter()

        setupRecyclerView()

        setSearchInputListener()

        setupItemTouchHelper()

        //updating Live data observer with ViewModel data
        recipeListViewModel.getAllRecipesLiveData().observe(viewLifecycleOwner, Observer { recipes ->
            recipes?.let {
                adapter.updateRecipes(recipes)
            }
            checkForEmptyState()
        })
        addListDividerDecoration()

        recipeListViewModel.navigateToRecipeDetail.observe(viewLifecycleOwner, Observer { keyId->
            keyId?.let {
                this.findNavController().navigate(
                    RecipeListFragmentDirections.actionRecipeListFragmentToRecipeFragment(keyId))
                recipeListViewModel.onRecipeDetailNavigated()
            }
        })
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.top_nav_menu, menu)

        //setup clicks on icons in toolbar
        val searchIcon = menu.findItem(R.id.search_menu_item)
        searchIcon.actionView.setOnClickListener {
            menu.performIdentifierAction(
                searchIcon.itemId,
                0
            )
        }
        val favoriteIcon = menu.findItem(R.id.favorite_menu_item)
        favoriteIcon.actionView.setOnClickListener {
            menu.performIdentifierAction(
                favoriteIcon.itemId,
                0
            )
        }
    }

    //based on selected menu item, layout managers are switched
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Timber.i("onOptionsItemSelected")
        when (item.itemId) {
            R.id.favorite_menu_item -> {
                Timber.i("favorite selected")
            }
            R.id.search_menu_item -> {
                Timber.i("search selected")
                hideShowSearchBar()

            }
            R.id.list_display -> {
                Timber.i("LIST")
//                showListView()
            }
            R.id.grid_display -> {
                Timber.i( "GRID")
//                showGridView()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun deleteRecipeAtPosition(recipe: RecipeEntry) {
        recipeListViewModel.onDeleteRecipe(recipe)
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
        adapter = RecipeListAdapter(mutableListOf(), this, RecipeListOnClickListener { id ->
            recipeListViewModel.onRecipeClicked(id)
        })
    }

    private fun setupRecyclerView() {
        //setting up a Recyclerview
        val layoutManager = LinearLayoutManager(context)
        binding.recipeListRecyclerview.layoutManager = layoutManager
        binding.recipeListRecyclerview.adapter = adapter
    }


    private fun setupViews() {
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_recipeListFragment_to_addRecipeFragment)
        }
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

//
//    private fun showListView() {
//        layoutManager.spanCount = 1
//    }
//    private fun showGridView() {
//        layoutManager.spanCount = 2
//    }

    private fun hideShowSearchBar() {
        binding.searchInput.visibility = if (search_input.visibility == View.GONE) View.VISIBLE else View.GONE
    }

        private fun setSearchInputListener() {
            binding.searchInput.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }
                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter.filter.filter(newText)
                    return false
                }
            })
        }



}
