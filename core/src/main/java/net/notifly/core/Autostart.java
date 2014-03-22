package net.notifly.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Barak on 21/03/2014.
 */
public class Autostart extends BroadcastReceiver
{
  public void onReceive(Context context, Intent intent)
  {
    Log.d("net.notifly.core.Autostart", "before starting service");
    context.startService(new Intent(context, BackgroundService.class));
    Log.d("net.notifly.core.Autostart", "after starting service");
  }
}
