package com.teahouse.marvelmovies

import android.content.Context
import android.content.Intent
import android.databinding.BindingAdapter
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.widget.SearchView
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import utils.Utils
import java.util.*

object ViewModel {
    class CharacterViewModel(val context: Context, var model: Model.Character) {

        companion object {
            val IMAGE_TYPE = "/landscape_incredible."
        }

        var imageUrl = modelImageUrl()

        fun modelImageUrl(): String = model.thumbnail.path + IMAGE_TYPE + model.thumbnail.extension

        object ImageViewBindingAdapter {
            @BindingAdapter("bind:imageUrl")
            @JvmStatic
            fun loadImage(view: ImageView, url: String) {
                Picasso.with(view.context).load(url).into(view)
            }
        }

        public fun openDetailActivity() {
            var intent = Intent(context, DetailActivity::class.java)
            val json = Gson().toJson(model)
            intent.putExtra(DetailActivity.MODEL_EXTRA, json)
            context.startActivity(intent)
        }
    }

    class MainViewModel(val context: Context) {

        lateinit var originalList: MutableList<Model.Character>
        val defaultLimit = 20
        var countLimit = 0
        lateinit var service: MarvelService


        init {
            service = MarvelService.create()
        }

        interface MainActivityViewModel {
            fun endCallProgress(any: Any?)
        }

        private var _compoSub = CompositeSubscription()
        private val compoSub: CompositeSubscription
            get() {
                if (_compoSub.isUnsubscribed) {
                    _compoSub = CompositeSubscription()
                }
                return _compoSub
            }

        protected final fun manageSub(s: Subscription) = compoSub.add(s)

        fun unsubscribe() {
            compoSub.unsubscribe()
        }

        fun filterList(term: String, adapter: CharactersAdapter) {
            if (term != "") {
                val list = adapter.characterResponse.filter { it.name.contains(term.trim(), true) } as MutableList<Model.Character>
                adapter.characterResponse = list
                adapter.notifyDataSetChanged()

            } else {
                adapter.characterResponse = originalList
                adapter.notifyDataSetChanged()
            }

        }

        fun getOnQueryTextChange(adapter: CharactersAdapter): SearchView.OnQueryTextListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(term: String?): Boolean {
                if (term != null) {
                    filterList(term, adapter)
                }
                return false
            }

            override fun onQueryTextSubmit(term: String?): Boolean {
                if (term != null) {
                    filterList(term, adapter)
                }
                return false
            }
        }

        fun filterLogic(c: Model.Character): Boolean {
            return c.thumbnail != null && !(c.thumbnail.path.endsWith("image_not_available"))
        }

        fun loadCharactersList(callback: MainActivityViewModel) {

            val timestamp = Date().time;
            val hash = Utils.md5(timestamp.toString() + BuildConfig.MARVEL_PRIVATE_KEY + BuildConfig.MARVEL_PUBLIC_KEY)

            manageSub(
                    service.getCharacters(timestamp.toString(), BuildConfig.MARVEL_PUBLIC_KEY, hash, defaultLimit)
                            .subscribeOn(Schedulers.io())
                            .map {
                                 Pair((it.data.results.filter(this::filterLogic).toMutableList()), it.data.limit)
                            }

                            //change to subject
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe( {(r,l) ->

                                        callback.endCallProgress(r)
                                        originalList = r
                                        countLimit = l

                                    },
                                    { e ->
                                        callback.endCallProgress(e)
                                        Log.e(MainActivity::class.java.simpleName, e.message)
                                    })
            )
        }

        fun loadMoreCharacters(callback: MainActivityViewModel, adapter: CharactersAdapter) {
            val timestamp = Date().time;
            val hash = Utils.md5(timestamp.toString() + BuildConfig.MARVEL_PRIVATE_KEY + BuildConfig.MARVEL_PUBLIC_KEY)

            manageSub(
                    service.getCharacters(timestamp.toString(), BuildConfig.MARVEL_PUBLIC_KEY, hash, countLimit + defaultLimit)
                            .subscribeOn(Schedulers.io())
                            .flatMapIterable { it -> it.data.results }
                            .filter({ filterLogic(it) })
                            .toList()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ c ->
                                callback.endCallProgress(c)
                                updateIndexesForRequests(adapter, c)
                            },
                                    { e ->
                                        callback.endCallProgress(e)
                                        Log.e(MainActivity::class.java.simpleName, e.message)
                                    })
            )
        }

        fun updateIndexesForRequests(adapter: CharactersAdapter, response: MutableList<Model.Character>) {
            adapter.characterResponse = response
            adapter.notifyItemRangeChanged(countLimit, countLimit + defaultLimit)
            originalList = response
            countLimit += defaultLimit
        }

    }


    class CharacterDetailViewModel(val context: Context, var intent: Intent) {

        lateinit var service: MarvelService
        lateinit var model: Model.Character

        companion object {
            val IMAGE_TYPE = "/standard_fantastic."
        }

        init {
            service = MarvelService.create()
            val characterType = object : TypeToken<Model.Character>() {}.type
            model = Gson().fromJson<Model.Character>(intent.getStringExtra(DetailActivity.MODEL_EXTRA), characterType)
        }

        interface DetailViewModel {
            fun endCallProgress(any: Any?)
        }

        var detailImageUrl = detailImageUrl()

        fun detailImageUrl(): String = model.thumbnail.path + IMAGE_TYPE + model.thumbnail.extension

        object ImageViewBindingAdapter {
            @BindingAdapter("bind:detailImageUrl")
            @JvmStatic
            fun loadImage(view: ImageView, url: String) {
                Picasso.with(view.context).load(url).into(view)
            }
        }

        private var _compoSub = CompositeSubscription()
        private val compoSub: CompositeSubscription
            get() {
                if (_compoSub.isUnsubscribed) {
                    _compoSub = CompositeSubscription()
                }
                return _compoSub
            }

        protected final fun manageSub(s: Subscription) = compoSub.add(s)

        fun unsubscribe() {
            compoSub.unsubscribe()
        }

        fun loadCharacter(callback: DetailViewModel) {
            val timestamp = Date().time;
            val hash = Utils.md5(timestamp.toString() + BuildConfig.MARVEL_PRIVATE_KEY + BuildConfig.MARVEL_PUBLIC_KEY)

            manageSub(
                    service.getCharacterDetail(model.id.toString(), timestamp.toString(), BuildConfig.MARVEL_PUBLIC_KEY, hash)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ c -> callback.endCallProgress(c) },
                                    { e ->
                                        callback.endCallProgress(e)
                                        Log.e(DetailActivity::class.java.simpleName, e.message)
                                    })
            )

        }


        fun loadCollectionViews(view: LinearLayout, viewSeries: LinearLayout, fm: FragmentManager) {
            if (model.comics?.items.size != 0) {
                view.addView(CollectionView(context, model.comics, fm, context.getString(R.string.collection_item_comics_title)))
            }
            if (model?.series?.items.size != 0) {
                viewSeries.addView(CollectionView(context, model.series, fm, context.getString(R.string.collection_item_series_title)))
            }
            if (model?.stories?.items.size != 0) {
                view.addView(CollectionView(context, model.stories, fm, context.getString(R.string.collection_item_stories_title)))
            }
            if (model?.events?.items.size != 0) {
                view.addView(CollectionView(context, model.events, fm, context.getString(R.string.collection_item_events_title)))
            }

        }


    }

    class CollectionItemViewModel(val context: Context, var model: Model.CollectionItem, var title: String) {

    }

    class MarvelItemViewModel(val context: Context, var model: Model.Item) {

        lateinit var service: MarvelService
        lateinit var detailModel: Model.Detail

        init {
            service = MarvelService.create()
        }

        companion object {
            fun putBundleArgs(model: Model.Item): Bundle {
                val args = Bundle()
                args.putString(DetailActivity.MODEL_EXTRA, Gson().toJson(model))
                return args
            }

            fun getModelFromBundle(args: Bundle): Model.Item {
                val itemType = object : TypeToken<Model.Item>() {}.type
                return Gson().fromJson<Model.Item>(args.getString(DetailActivity.MODEL_EXTRA), itemType)
            }
        }

        private var _compoSub = CompositeSubscription()
        private val compoSub: CompositeSubscription
            get() {
                if (_compoSub.isUnsubscribed) {
                    _compoSub = CompositeSubscription()
                }
                return _compoSub
            }

        protected final fun manageSub(s: Subscription) = compoSub.add(s)

        fun unsubscribe() {
            compoSub.unsubscribe()
        }

        fun imageUrl(): String = detailModel.images[0].path + "/portrait_medium." + detailModel.images[0].extension

        interface DetailFragmentViewModel {
            fun endCallProgress(any: Any?)
        }

        fun loadDetail(callback: DetailFragmentViewModel, resourceURI: String) {

            var url = resourceURI.replace(MarvelService.BASE_URL + MarvelService.API_URL, "")
            var splittedURI = url.split("/")

            val timestamp = Date().time;
            val hash = Utils.md5(timestamp.toString() + BuildConfig.MARVEL_PRIVATE_KEY + BuildConfig.MARVEL_PUBLIC_KEY)

            manageSub(
                    service.getDetail(splittedURI[0], splittedURI[1], timestamp.toString(), BuildConfig.MARVEL_PUBLIC_KEY, hash)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ c -> callback.endCallProgress(c) },
                                    { e ->
                                        callback.endCallProgress(e)
                                        Log.e(CollectionItemFragment::class.java.simpleName, e.message)
                                    })
            )

        }


    }

}