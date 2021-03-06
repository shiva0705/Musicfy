package com.shiva0705.sample.musicfy.ui.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.widget.Button
import butterknife.bindView
import com.shiva0705.sample.musicfy.R
import com.shiva0705.sample.musicfy.core.dagger.componentProvider.AppComponentProvider
import com.shiva0705.sample.musicfy.data.api.SpotifyApi
import com.shiva0705.sample.musicfy.data.preferences.SelectionPreference
import com.shiva0705.sample.musicfy.models.Tracks
import com.shiva0705.sample.musicfy.ui.activities.MainActivity
import com.shiva0705.sample.musicfy.ui.adapters.SongAdapter
import com.shiva0705.sample.musicfy.ui.adapters.core.OnStartDragListener
import com.shiva0705.sample.musicfy.ui.adapters.core.SimpleItemTouchHelperCallback
import com.shiva0705.sample.musicfy.ui.fragments.core.BaseFragment
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

class SelectSongFragment : BaseFragment(), OnStartDragListener {

    override val viewId = R.layout.fragment_select_song

    lateinit var genre: String
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var adapter: SongAdapter
    lateinit var itemTouchHelper: ItemTouchHelper

    @Inject lateinit var spotifyApi : SpotifyApi
    @Inject lateinit var selectionPreference : SelectionPreference

    val recylerView: RecyclerView by bindView(R.id.recylcer_view)
    val nextBtn : Button by bindView(R.id.btn_next)

    var tryAgain: View.OnClickListener = View.OnClickListener { getSongList() }
    val maxLimit = 5


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppComponentProvider.appComponent.inject(this)

        getSongList()
    }

    fun getSongList() {
        spotifyApi.search(genre, maxLimit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ res -> showSongList(res) },
                        { e -> handleNetworkError(e, tryAgain) })
    }

    fun showSongList(tracks: Tracks) {

        recylerView.layoutManager = LinearLayoutManager(activity)
        recylerView.setHasFixedSize(true)

        adapter = SongAdapter(tracks.tracks)
        recylerView.adapter = adapter

        var callback = SimpleItemTouchHelperCallback(adapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recylerView)

        nextBtn.setOnClickListener({next()})
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    fun next(){
        selectionPreference.saveSelectionOrder(genre, Tracks(adapter.songs))
        (activity as MainActivity).nextStep()
    }


    companion object {
        fun newInstance(genre: String): SelectSongFragment {
            var fragment = SelectSongFragment()
            fragment.genre = genre
            return fragment
        }
    }

}