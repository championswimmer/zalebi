package com.hasgeek.zalebi.fragments.space;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hasgeek.zalebi.R;
import com.hasgeek.zalebi.adapters.ProposalsAdapter;
import com.hasgeek.zalebi.api.model.Space;
import com.hasgeek.zalebi.eventbus.BusProvider;
import com.hasgeek.zalebi.eventbus.event.api.APIErrorEvent;
import com.hasgeek.zalebi.eventbus.event.api.APIRequestSingleSpaceEvent;
import com.hasgeek.zalebi.eventbus.event.loader.LoadSingleSpaceEvent;
import com.hasgeek.zalebi.eventbus.event.loader.SingleSpaceLoadedEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.parceler.Parcels;

/**
 * Created by karthik on 30-12-2014.
 */
public class ProposalFragment extends Fragment {

    String LOG_TAG = "ProposalFragment";
    RecyclerView mRecyclerView;
    private Bus mBus;
    private SwipeRefreshLayout swipeLayout;
    private ProposalsAdapter mAdapter;
    private Space space;
    Bundle spaceBundle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (getArguments() != null) {
            spaceBundle = getArguments();
            space = Parcels.unwrap(spaceBundle.getParcelable("space"));

        } else if (savedInstanceState != null) {
            spaceBundle = savedInstanceState.getBundle("state");
            space = Parcels.unwrap(spaceBundle.getParcelable("space"));

        }


        View v = inflater.inflate(R.layout.fragment_space_proposal, container, false);

        swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.fragment_proposal_swipe_container);
        swipeLayout.setOnRefreshListener(mOnSwipeListener);
        swipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_space_proposal_recyclerview);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setOnScrollListener(scrollListener);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getBus().register(this);
        mBus.post(new LoadSingleSpaceEvent(space.getJsonUrl()));
    }

    @Override
    public void onPause() {
        super.onPause();
        getBus().unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (outState != null) {
            outState.putBundle("state", spaceBundle);
        }
    }

    private SwipeRefreshLayout.OnRefreshListener mOnSwipeListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            Log.i(LOG_TAG, "onRefresh() POST LoadSpacesEvent");
//            getBus().post(new APIErrorEvent(space_id));
            getBus().post(new APIRequestSingleSpaceEvent(space.getJsonUrl()));
        }
    };

    RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int topRowVerticalPosition = (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
            swipeLayout.setEnabled(topRowVerticalPosition >= 0);

        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }
    };

    @Subscribe
    public void onSingleSpaceLoaded(SingleSpaceLoadedEvent event) {
        Log.i(LOG_TAG, "onSpacesLoaded() SUBSCRIPTION SpacesLoadedEvent");
        mAdapter = new ProposalsAdapter(getActivity(), event.getProposals(), spaceBundle);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        swipeLayout.setRefreshing(false);
    }

    @Subscribe
    public void onAPIError(APIErrorEvent event) {
        Toast.makeText(getActivity(), "Network trouble?", Toast.LENGTH_SHORT).show();
        swipeLayout.setRefreshing(false);
    }

    private Bus getBus() {
        if (mBus == null) {
            mBus = BusProvider.getInstance();
        }
        return mBus;
    }

}
