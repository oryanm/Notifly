package net.notifly.core.gui.activity.main;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fortysevendeg.swipelistview.SwipeListView;

import net.notifly.core.R;
import net.notifly.core.sql.NotesDAO;

public class PlaceholderFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        int section = getArguments().getInt(ARG_SECTION_NUMBER);

        switch (section) {
            case MainActivity.NAVIGATION_SECTION_NOTES: {
                createNotesListView(rootView);
            }
        }
        return rootView;
    }

    private void createNotesListView(View rootView) {
        NotesDAO notesDAO = new NotesDAO(getActivity());
        final NotesAdapter adapter = new NotesAdapter(getActivity(), notesDAO.getAllNotes());
        notesDAO.close();

        SwipeListView list = (SwipeListView) rootView.findViewById(R.id.notes_list_view);
        list.setAdapter(adapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }
}
