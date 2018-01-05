package com.teahouse.marvelmovies

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.teahouse.marvelmovies.databinding.ItemCollectionBinding
import com.squareup.picasso.Picasso

class CollectionItemFragment : Fragment(), ViewModel.MarvelItemViewModel.DetailFragmentViewModel {

    lateinit var viewModel: ViewModel.MarvelItemViewModel
    lateinit var binding: ItemCollectionBinding
    var loaded = false

    override fun endCallProgress(any: Any?) {
        if (any is Model.DetailResponse) {
            println("fragment response working: " + any.toString())
            viewModel.detailModel = any.data.results[0]
            binding.itemName.text = viewModel.detailModel.title
            Picasso.with(context).load(viewModel.imageUrl()).into(binding.itemImage)
        }
    }

    companion object {
        fun newInstance(model: Model.Item): CollectionItemFragment {
            var fragment = CollectionItemFragment.newInstance()
            fragment.arguments = ViewModel.MarvelItemViewModel.putBundleArgs(model)

            return fragment
        }

        fun newInstance(): CollectionItemFragment {
            return CollectionItemFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<ItemCollectionBinding>(inflater, R.layout.item_collection, container, false)

        if(!loaded && arguments != null && arguments.containsKey(DetailActivity.MODEL_EXTRA)) {
            viewModel = ViewModel.MarvelItemViewModel(context, ViewModel.MarvelItemViewModel.getModelFromBundle(arguments))
            viewModel.loadDetail(this, viewModel.model.resourceURI)
        }

        return binding.root
    }

}