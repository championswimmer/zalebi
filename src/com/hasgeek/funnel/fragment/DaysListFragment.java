package com.hasgeek.funnel.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.haarman.listviewanimations.itemmanipulation.ExpandableListItemAdapter;
import com.hasgeek.funnel.R;
import com.hasgeek.funnel.misc.EventSession;
import com.hasgeek.funnel.misc.SessionsListLoader;

import java.util.ArrayList;
import java.util.List;


public class DaysListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<List<EventSession>> {

    public static final int BOOKMARKED_SESSIONS = 198263;
    public static final int All_SESSIONS = 198264;

    private TextView mBookmarkedOnlyNotice;
    private SessionsListAdapter mAdapter;
    private static final int REQUEST_SESSION_DETAIL = 4201;
    private List<EventSession> mSessionsList;
    private int mListMode;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mSessionsList = new ArrayList<EventSession>();
        mListMode = All_SESSIONS;
        mAdapter = new SessionsListAdapter(
                getActivity(),
                R.layout.row_session,
                R.id.ll_top,
                R.id.ll_bottom);

        getLoaderManager().initLoader(0, null, this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sessionslist, container, false);
        mBookmarkedOnlyNotice = (TextView) v.findViewById(R.id.tv_showing_only_bookmarked);
        return v;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_eventdetail, menu);
        if (mListMode == All_SESSIONS) {
            menu.findItem(R.id.action_toggle_show_bookmarks)
                    .setIcon(R.drawable.ic_show_bookmarked)
                    .setTitle(R.string.show_only_bookmarked);
        } else {
            menu.findItem(R.id.action_toggle_show_bookmarks)
                    .setIcon(R.drawable.ic_show_not_bookmarked)
                    .setTitle(R.string.show_all_sessions);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_toggle_show_bookmarks:
                toggleBookmarkedSessions();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<List<EventSession>> onCreateLoader(int i, Bundle bundle) {
        if (mListMode == All_SESSIONS) {
            return new SessionsListLoader(getActivity(), All_SESSIONS);
        } else if (mListMode == BOOKMARKED_SESSIONS) {
            return new SessionsListLoader(getActivity(), BOOKMARKED_SESSIONS);
        } else {
            throw new RuntimeException("List mode missing");
        }
    }


    @Override
    public void onLoadFinished(Loader<List<EventSession>> listLoader, List<EventSession> eventSessions) {
        mSessionsList = eventSessions;

        if (getListView().getAdapter() == null) {
            setListAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }

    }


    @Override
    public void onLoaderReset(Loader<List<EventSession>> listLoader) {

    }


    /**
     * The adapter that fills the ListView with session data
     */
    private class SessionsListAdapter extends ExpandableListItemAdapter<EventSession> {

        Context nContext;
        int nLayoutResId;
        int nTitleResId;
        int nContentResId;

        protected SessionsListAdapter(Context context, int layoutResId, int titleParentResId, int contentParentResId) {
            super(context, layoutResId, titleParentResId, contentParentResId, mSessionsList);
            nContext = context;
            nLayoutResId = layoutResId;
            nTitleResId = titleParentResId;
            nContentResId = contentParentResId;
        }


        @Override
        public int getCount() {
            return mSessionsList.size();
        }


        @Override
        public View getTitleView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = parent;
            }

            TextView title = (TextView) convertView.findViewById(R.id.tv_session_title);
            title.setText(mSessionsList.get(position).getTitle());
            return title;
        }


        @Override
        public View getContentView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = parent;
            }

            TextView speaker = (TextView) convertView.findViewById(R.id.tv_session_speaker);
            speaker.setText(mSessionsList.get(position).getSpeaker());
            return speaker;
        }

    }

    //todo move this elsewhere
//    @Override
//    public void onListItemClick(ListView l, View v, int position, long id) {
//        Intent i = new Intent(getActivity(), SessionDetailActivity.class);
//        i.putExtra("session", mSessionsList.get(position));
//        startActivityForResult(i, REQUEST_SESSION_DETAIL);
//    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SESSION_DETAIL:
                getLoaderManager().restartLoader(0, null, this);
        }
    }


    public void toggleBookmarkedSessions() {
        if (mListMode == All_SESSIONS) {
            mListMode = BOOKMARKED_SESSIONS;
            mBookmarkedOnlyNotice.setVisibility(View.VISIBLE);
        } else {
            mListMode = All_SESSIONS;
            mBookmarkedOnlyNotice.setVisibility(View.GONE);
        }

        getActivity().invalidateOptionsMenu();
        getLoaderManager().restartLoader(0, null, this);
    }

}