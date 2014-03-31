package net.notifly.core.gui.activity.main;

import android.app.Fragment;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ListView;

import com.fortysevendeg.swipelistview.SwipeListView;

import net.notifly.core.R;
import net.notifly.core.entity.Note;
import net.notifly.core.gui.activity.note.NewNoteActivity_;
import net.notifly.core.sql.NotesDAO;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

@EFragment(R.layout.fragment_main)
@OptionsMenu(R.menu.main)
public class NotesMainFragment extends Fragment {
    public static final int NEW_NOTE_CODE = 1;
    public static final String EXTRA_NOTE = "net.notifly.core.note";

    public static NotesMainFragment newInstance() {
        NotesMainFragment fragment = new NotesMainFragment_();
        fragment.setHasOptionsMenu(true);
        return fragment;
    }

    @AfterViews
    void createNotesListView() {
        NotesDAO notesDAO = new NotesDAO(getActivity());
        final NotesAdapter adapter = new NotesAdapter(getActivity(), notesDAO.getAllNotes());
        notesDAO.close();

        SwipeListView list = (SwipeListView) getActivity().findViewById(R.id.notes_list_view);
        list.setAdapter(adapter);
    }

    @OptionsItem(R.id.action_add_note)
    void openNewNoteActivity() {
        SwipeListView list = (SwipeListView) getActivity().findViewById(R.id.notes_list_view);
        list.closeOpenedItems();

        Intent intent = new Intent(getActivity(), NewNoteActivity_.class);
        startActivityForResult(intent, NEW_NOTE_CODE);
    }

    @OnActivityResult(NEW_NOTE_CODE)
    void afterNewNote(int resultCode, Intent intent) {
        if (resultCode == MainActivity.RESULT_OK) {
            Note note = intent.getParcelableExtra(EXTRA_NOTE);
            ListView list = (ListView) getActivity().findViewById(R.id.notes_list_view);
            NotesAdapter adapter = (NotesAdapter) list.getAdapter();
            adapter.insert(note, 0);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getActionBar().setTitle(getString(R.string.title_section_notes));
    }
}