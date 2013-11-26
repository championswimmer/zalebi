package com.hasgeek.funnel.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hasgeek.funnel.R;
import com.hasgeek.funnel.misc.EventSession;
import com.hasgeek.funnel.misc.EventSessionRow;
import com.hasgeek.funnel.misc.SessionDetailRowPart;
import com.hasgeek.funnel.misc.SessionsListLoader;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


public class DaysListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<EventSessionRow>> {

    public static final int BOOKMARKED_SESSIONS = 198263;
    public static final int All_SESSIONS = 198264;

    private TextView mBookmarkedOnlyNotice;
    private StickyListHeadersListView mListView;
    private TextView mEmptyViewForList;
    private SessionsListAdapter mAdapter;
    private static final int REQUEST_SESSION_DETAIL = 4201;
    private List<EventSessionRow> mSessionsList;
    private int mListMode;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mSessionsList = new ArrayList<EventSessionRow>();
        mListMode = All_SESSIONS;
        mAdapter = new SessionsListAdapter(
                getActivity(),
                R.layout.row_session,
                mSessionsList);

        getLoaderManager().initLoader(0, null, this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sessionslist, container, false);
        mBookmarkedOnlyNotice = (TextView) v.findViewById(R.id.tv_showing_only_bookmarked);
        mListView = (StickyListHeadersListView) v.findViewById(R.id.list);
        mEmptyViewForList = (TextView) v.findViewById(R.id.empty);
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
    public Loader<List<EventSessionRow>> onCreateLoader(int i, Bundle bundle) {
        if (mListMode == All_SESSIONS) {
            return new SessionsListLoader(getActivity(), All_SESSIONS);
        } else if (mListMode == BOOKMARKED_SESSIONS) {
            return new SessionsListLoader(getActivity(), BOOKMARKED_SESSIONS);
        } else {
            throw new RuntimeException("List mode missing");
        }
    }


    @Override
    public void onLoadFinished(Loader<List<EventSessionRow>> listLoader, List<EventSessionRow> eventSessions) {
        mSessionsList = eventSessions;

        if (mListView.getAdapter() == null) {
//            mListView.setAdapter(new SlideExpandableListAdapter(mAdapter, R.id.ll_top, R.id.ll_bottom));
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }

        mListView.setEmptyView(mEmptyViewForList);
    }


    @Override
    public void onLoaderReset(Loader<List<EventSessionRow>> listLoader) {

    }


    /**
     * The adapter that fills the ListView with session data
     */
    private class SessionsListAdapter extends ArrayAdapter<EventSessionRow> implements StickyListHeadersAdapter {

        Context nContext;


        public SessionsListAdapter(Context context, int textViewResourceId, List<EventSessionRow> objects) {
            super(context, textViewResourceId, objects);
            nContext = context;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(nContext);
                convertView = inflater.inflate(R.layout.row_session, parent, false);
            }

            TextView timeslot = (TextView) convertView.findViewById(R.id.tv_session_timeslot);
            timeslot.setText(mSessionsList.get(position).getTimeslotInIst24Hrs());

            List<EventSession> sessions = mSessionsList.get(position).getSessions();

            LinearLayout sessionsLayout = (LinearLayout) convertView.findViewById(R.id.ll_sessions);
            sessionsLayout.removeAllViews();
            for (EventSession e : sessions) {
                int color;
                if (TextUtils.isEmpty(e.getRoomColor())) {
                    color = Color.GRAY;
                } else {
                    color = Color.parseColor("#" + e.getRoomColor());
                }
                SessionDetailRowPart rowPart = new SessionDetailRowPart(nContext, e.getTitle(), e.getSpeaker(), color);
                sessionsLayout.addView(rowPart);
            }

            if (sessions.size() > 1) {
                View sep = new View(nContext);
                sep.setBackgroundColor(Color.parseColor("#e5e5e5"));
                sep.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics())));
                sessionsLayout.addView(sep, 1);
            }

            return convertView;
        }


        @Override
        public int getCount() {
            return mSessionsList.size();
        }


        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.header_view, parent, false);
            }

            TextView tv = (TextView) convertView.findViewById(R.id.tv_lv_stickyheader);
            tv.setText(mSessionsList.get(position).getDateInIst());

            return convertView;
        }


        @Override
        public long getHeaderId(int position) {
            return Long.parseLong(mSessionsList.get(position).getDateInIst().replaceAll("-", "0"));
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
