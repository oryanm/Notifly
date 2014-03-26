package net.notifly.core;

import android.content.Context;

import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;

import net.notifly.core.entity.Note;
import net.notifly.core.gui.activity.main.NotesAdapter;
import net.notifly.core.sql.NotesDAO;

public class NotesSwipeListViewListener extends BaseSwipeListViewListener
{
  private NotesAdapter adapter;
  private Context context;

  public NotesSwipeListViewListener(Context context, NotesAdapter adapter)
  {
    this.adapter = adapter;
    this.context = context;
  }

  @Override
  public void onDismiss(int[] reverseSortedPositions)
  {
    NotesDAO notesDAO = new NotesDAO(context);

    for (int position : reverseSortedPositions)
    {
      Note note = adapter.getItem(position);
      adapter.remove(note);
      notesDAO.deleteNote(note);
    }

    notesDAO.close();
    adapter.notifyDataSetChanged();
  }
}

