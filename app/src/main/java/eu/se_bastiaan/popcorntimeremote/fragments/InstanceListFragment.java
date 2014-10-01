package eu.se_bastiaan.popcorntimeremote.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import eu.se_bastiaan.popcorntimeremote.R;
import eu.se_bastiaan.popcorntimeremote.activities.ControllerActivity;
import eu.se_bastiaan.popcorntimeremote.database.InstanceEntry;
import eu.se_bastiaan.popcorntimeremote.database.InstanceProvider;

public class InstanceListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private Boolean mActionMode = false;
    private SimpleCursorAdapter mAdapter;
    private Integer mSelectedPosition;
    private ActionMode mMode;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        getListView().setOnItemLongClickListener(mOnLongClickListener);
        getListView().setSelector(R.drawable.selectable_background_popcorntimeremote);

        mAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.fragment_listinstance_item, null, new String[]{
                InstanceEntry.COLUMN_NAME_NAME,
                InstanceEntry.COLUMN_NAME_IP}, new int[]{
                R.id.text1, R.id.text2}, 0);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mMode != null) mMode.finish();
    }

    @Override
    public void onListItemClick(ListView parent, View view, int position, long id) {
        if(!mActionMode) {
            Cursor cursor = (Cursor) parent.getItemAtPosition(position);
            Intent intent = new Intent(getActivity(), ControllerActivity.class);
            intent.putExtra(ControllerActivity.KEY_IP, cursor.getString(1));
            intent.putExtra(ControllerActivity.KEY_PORT, cursor.getString(2));
            intent.putExtra(ControllerActivity.KEY_NAME, cursor.getString(3));
            intent.putExtra(ControllerActivity.KEY_USERNAME, cursor.getString(4));
            intent.putExtra(ControllerActivity.KEY_PASSWORD, cursor.getString(5));
            startActivity(intent);
        } else {
            getListView().getChildAt(mSelectedPosition).setBackgroundDrawable(null);
            mSelectedPosition = position;
            getListView().setItemChecked(mSelectedPosition, true);
            view.setBackgroundResource(R.color.list_selected);
        }
    }

    private AdapterView.OnItemLongClickListener mOnLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if(mSelectedPosition != null) {
                View v = getListView().getChildAt(mSelectedPosition);
                if(v != null) v.setBackgroundDrawable(null);
            }
            mSelectedPosition = position;
            view.setBackgroundResource(R.color.list_selected);
            mMode = ((ActionBarActivity) getActivity()).startSupportActionMode(new ActionBarCallBack());
            return true;
        }
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), InstanceProvider.INSTANCES_URI, new String[]{"_id",
                InstanceEntry.COLUMN_NAME_IP, InstanceEntry.COLUMN_NAME_PORT,
                InstanceEntry.COLUMN_NAME_NAME, InstanceEntry.COLUMN_NAME_USERNAME, InstanceEntry.COLUMN_NAME_PASSWORD}, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if(getListAdapter() == null) {
            setListAdapter(mAdapter);
        }
        mAdapter.swapCursor(cursor);
        if(mAdapter.getCount() <= 0) {
            setEmptyText(getActivity().getString(R.string.no_instances));
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_instancelist, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                openEditorFragment(null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class ActionBarCallBack implements ActionMode.Callback {

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Cursor cursor = (Cursor) getListView().getItemAtPosition(mSelectedPosition);
            switch (item.getItemId()) {
                case R.id.action_delete:
                    getActivity().getContentResolver().delete(Uri.withAppendedPath(InstanceProvider.INSTANCES_URI, "/" + cursor.getString(0)), null, null);
                    mMode.finish();
                    break;
                case R.id.action_edit:
                    openEditorFragment(cursor.getString(0));
                    mMode.finish();
                    break;
            }
            return true;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.fragment_instancelist_contextual, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            getListView().getChildAt(mSelectedPosition).setBackgroundDrawable(null);
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
    }

    private void openEditorFragment(String id) {
        try {
            InstanceEditorDialogFragment fragment = new InstanceEditorDialogFragment();
            if (id != null) {
                Bundle args = new Bundle();
                args.putString("_id", id);
                fragment.setArguments(args);
            }
            fragment.show(getActivity().getSupportFragmentManager(), "editor_fragment");
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }
}
