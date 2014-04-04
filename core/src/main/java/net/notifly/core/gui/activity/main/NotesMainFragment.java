package net.notifly.core.gui.activity.main;

import android.app.Fragment;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;

import com.fortysevendeg.swipelistview.SwipeListView;

import net.notifly.core.Notifly;
import net.notifly.core.R;
import net.notifly.core.entity.Note;
import net.notifly.core.gui.activity.note.NewNoteActivity_;
import net.notifly.core.util.LocationHandler;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_main)
@OptionsMenu(R.menu.main)
public class NotesMainFragment extends Fragment implements AddressLoader.Callbacks {
    public static final int NEW_NOTE_CODE = 1;
    public static final String EXTRA_NOTE = "net.notifly.core.note";

    @App
    Notifly notifly;
    @ViewById(R.id.notes_list_view)
    SwipeListView swipeListView;
    @Bean
    NotesAdapter adapter;

    LocationHandler locationHandler;

    public static NotesMainFragment newInstance() {
        NotesMainFragment fragment = new NotesMainFragment_();
        fragment.setHasOptionsMenu(true);
        return fragment;
    }

    @AfterViews
    void createNotesListView() {
        locationHandler = new LocationHandler(getActivity());

        adapter.addAll(notifly.getNotes());
        swipeListView.setAdapter(adapter);
    }

    @OptionsItem(R.id.action_add_note)
    void openNewNoteActivity() {
        swipeListView.closeOpenedItems();
        Intent intent = new Intent(getActivity(), NewNoteActivity_.class);
        startActivityForResult(intent, NEW_NOTE_CODE);
    }

    @OnActivityResult(NEW_NOTE_CODE)
    void afterNewNote(int resultCode, Intent intent) {
        if (resultCode == MainActivity.RESULT_OK) {
            Note note = intent.getParcelableExtra(EXTRA_NOTE);
            adapter.insert(note, 0);
            adapter.notifyDataSetChanged();
            notifly.addNote(note, this);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getActionBar().setTitle(getString(R.string.title_section_notes));
    }

    @Override
    public void notifyPostExecute() {
        adapter.notifyDataSetChanged();
    }
}