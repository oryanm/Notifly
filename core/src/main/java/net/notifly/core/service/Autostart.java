package net.notifly.core.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AutoStart extends BroadcastReceiver
{
  public void onReceive(Context context, Intent intent)
  {
    Log.d("net.notifly.core.service.AutoStart", "before starting service");
    context.startService(new Intent(context, BackgroundService.class));
    Log.d("net.notifly.core.service.AutoStart", "after starting service");
  }
}
