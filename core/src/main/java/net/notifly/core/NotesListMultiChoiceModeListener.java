package net.notifly.core;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;

import com.fortysevendeg.swipelistview.SwipeListView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class NotesListMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener
{
  private final SwipeListView list;

  public NotesListMultiChoiceModeListener(SwipeListView list)
  {
    this.list = list;
  }

  @Override
  public void onItemCheckedStateChanged(ActionMode mode, int position,
                                        long id, boolean checked) {
    mode.setTitle("Selected (" + list.getCountSelected() + ")");
  }

  @Override
  public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_add_note:
        list.closeOpenedItems();
        mode.finish();
        return true;
      default:
        return false;
    }
  }

  @Override
  public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    MenuInflater inflater = mode.getMenuInflater();
    inflater.inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public void onDestroyActionMode(ActionMode mode) {
    list.unselectedChoiceStates();
  }

  @Override
  public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
    return false;
  }
}

