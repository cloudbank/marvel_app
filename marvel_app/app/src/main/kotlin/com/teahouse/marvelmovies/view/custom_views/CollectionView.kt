package com.teahouse.marvelmovies

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v4.app.FragmentManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.teahouse.marvelmovies.databinding.ViewCollectionBinding

class CollectionView : RelativeLayout {

    lateinit var viewModel: ViewModel.CollectionItemViewModel
    lateinit var fm: FragmentManager

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, model: Model.CollectionItem, fragmentManager: FragmentManager, title: String) : super(context) {
        viewModel = ViewModel.CollectionItemViewModel(context, model, title)
        fm = fragmentManager
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { init() }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init() }

    fun init() {
        var binding = DataBindingUtil.inflate<ViewCollectionBinding>(LayoutInflater.from(context), R.layout.view_collection, this, true)
        binding.viewmodel = viewModel
        binding.collectionPager.adapter = CollectionPagerAdapter(fm, viewModel.model.items)

    }

}