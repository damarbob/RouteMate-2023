package id.my.dsm.routemate.ui.model

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.ScrollCaptureCallback
import android.view.View
import android.view.View.GONE
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import id.my.dsm.routemate.MainActivity
import id.my.dsm.routemate.R
import id.my.dsm.routemate.data.event.network.OnMapboxGeocodingResponse
import id.my.dsm.routemate.data.event.view.OnBottomSheetStateChanged
import id.my.dsm.routemate.data.event.view.OnMainActivityFeatureRequest
import id.my.dsm.routemate.ui.recyclerview.PlaceSearchRecViewAdapter
import id.my.dsm.routemate.usecase.place.RequestMapboxGeocodingUseCase
import id.my.dsm.routemate.usecase.repository.AlterRepositoryUseCase
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

class PlaceSearch(
    var context: Context,
    var searchBar: SearchBar,
    var searchView: SearchView,
    var progressIndicator: LinearProgressIndicator,
    var recyclerView: RecyclerView,
) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Entry {
        fun getAlterRepositoryUseCase(): AlterRepositoryUseCase
        fun getRequestMapboxGeocodingUseCase(): RequestMapboxGeocodingUseCase
    }

     private val injector = EntryPointAccessors.fromApplication(context, Entry::class.java)
     private val alterRepositoryUseCase = injector.getAlterRepositoryUseCase()
     private val requestMapboxGeocodingUseCase = injector.getRequestMapboxGeocodingUseCase()

    lateinit var adapter:PlaceSearchRecViewAdapter
    var searchShown: Boolean = false // SearchView state

    fun onCreate() {

        EventBus.getDefault().register(this)

        progressIndicator.visibility = GONE // Hide loading indicator

//        searchBar.inflateMenu(R.menu.menu_action_bar)

        // Adapter
        adapter = PlaceSearchRecViewAdapter(ArrayList(), alterRepositoryUseCase) // Create new PlaceSearchRecViewAdapter

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        // Set Listener
        searchBar.setNavigationOnClickListener {
            (context as MainActivity)._053803052023(OnMainActivityFeatureRequest(OnMainActivityFeatureRequest.Event.OPEN_DRAWER))
            // Open drawer
//            EventBus.getDefault().post(OnMainActivityFeatureRequest(OnMainActivityFeatureRequest.Event.OPEN_DRAWER))
        }
        searchView.addTransitionListener { searchView, previousState, newState ->
            // Update search state
            searchShown = newState === SearchView.TransitionState.SHOWN
        }
        searchView.editText.isCursorVisible = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            searchView.editText.setTextCursorDrawable(R.drawable.bg_cursor_text) // Set text cursor
        } else {
            // Use reflection for Android version under Q
            try {
                // https://github.com/android/platform_frameworks_base/blob/kitkat-release/core/java/android/widget/TextView.java#L562-564
                @SuppressLint("DiscouragedPrivateApi") val f =
                    TextView::class.java.getDeclaredField("mCursorDrawableRes")
                f.isAccessible = true
                f[searchView.editText] = R.drawable.bg_cursor_text
            } catch (ignored: Exception) {
            }
        }

        searchView.editText.setOnEditorActionListener { v, actionId, event ->

            // Get string from searchView if not null otherwise empty string
            val query =
                if (searchView.text != null) searchView.text
                    .toString() else ""

            // Cancel operation if query is empty
            if (query.isEmpty()) {
                searchView.hide()
                return@setOnEditorActionListener false
            }
            searchBar.text = query // Update searchBar text
            requestMapboxGeocodingUseCase.invoke(query) // Request geocoding with OnMapboxGeocodingResponse response
            progressIndicator.visibility = View.VISIBLE // Display loading indicator
            true
        }

    }

    fun onDestroy() {
        EventBus.getDefault().unregister(this)
    }

    /*
        Geocoding result
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun _093203052023(event: OnMapboxGeocodingResponse) {

        // Check if it's a searchBar request
        if (event.status != OnMapboxGeocodingResponse.Status.SUCCESS ||
            event.type != OnMapboxGeocodingResponse.Type.FORWARD
        ) return

        progressIndicator.visibility = GONE // Hide progress indicator

        // If no places found
        if (event.result == null) {
            Toast.makeText(context, "No place found", Toast.LENGTH_SHORT).show()
            return
        }

        // If results found
        adapter.setObjects(event.result) // Put result in the result list
        adapter.notifyDataSetChanged() // Refresh
    }

}