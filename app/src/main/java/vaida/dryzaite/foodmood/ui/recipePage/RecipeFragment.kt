package vaida.dryzaite.foodmood.ui.recipePage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.recipe_fragment.*
import timber.log.Timber
import vaida.dryzaite.foodmood.R
import vaida.dryzaite.foodmood.databinding.RecipeFragmentBinding
import vaida.dryzaite.foodmood.model.RecipeEntry
import vaida.dryzaite.foodmood.utilities.convertNumericMealTypeToString


class RecipeFragment : Fragment(), Toolbar.OnMenuItemClickListener {

    private lateinit var recipeViewModel: RecipeViewModel
    private lateinit var binding: RecipeFragmentBinding
    private lateinit var viewModelFactory: RecipeViewModelFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = RecipeFragmentBinding.inflate(inflater, container, false)

        val application = requireNotNull(this.activity).application
        val arguments = RecipeFragmentArgs.fromBundle(requireArguments())

        viewModelFactory = RecipeViewModelFactory(arguments.keyId, application)
        recipeViewModel = ViewModelProvider(this, viewModelFactory).get(RecipeViewModel::class.java)

        binding.recipeViewModel = recipeViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
        }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return  when (item?.itemId) {
            R.id.menu_share_item -> {
                shareRecipe()
                true
            }
            else -> false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // inflate the toolbar menu with the heart, set up  observer, handling fav button toggle
        toolbar_item.setOnMenuItemClickListener(this)
        val favoriteMenuItem = toolbar_item.menu.findItem(R.id.favorite_item_selector)
        val checkbox = favoriteMenuItem.actionView as CheckBox

        recipeViewModel.recipeDetail.observe(viewLifecycleOwner, Observer {
            setupFavoriteToggle(checkbox, it)
        })

        //observer handling clicks on URL field
        recipeViewModel.navigateToUrl.observe(viewLifecycleOwner, Observer {
            it?.let {
                Timber.i("$it is the URL to redirect")
                redirectToRecipeUrl(it)
                recipeViewModel.onButtonClicked()
            }
        })

    }


//    intent for sharing a recipe via other apps
    private fun getShareIntent(): Intent {
        val recipe = recipeViewModel.recipeDetail.value!!
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.setType("text/plain").putExtra(Intent.EXTRA_TEXT,
            getString(R.string.share_message, recipe.title, convertNumericMealTypeToString(recipe.meal, resources), recipe.recipe))
        return shareIntent
    }


    private fun shareRecipe() {
        startActivity(getShareIntent())
    }


    private fun setupFavoriteToggle(checkBox: CheckBox, recipe: RecipeEntry) {
        checkBox.setOnCheckedChangeListener { _, boolean ->
            recipe.isFavorite = boolean
            recipeViewModel.updateRecipe(recipe)
        }
        checkBox.isChecked = recipe.isFavorite
    }



private fun redirectToRecipeUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    view?.context?.startActivity(intent)
}


}