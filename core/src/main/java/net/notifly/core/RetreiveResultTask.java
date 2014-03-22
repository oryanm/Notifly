package net.notifly.core;

import android.os.AsyncTask;

import org.json.JSONException;

import java.io.IOException;

/**
 * Created by Barak on 08/03/14.
 */
class RetreiveResultTask extends AsyncTask<String, Void, DistanceMatrix>
{
  private Exception exception;

  @Override
  protected DistanceMatrix doInBackground(String... paramses)
  {
    try
    {
      try
      {
        return LocationHandler.getDistanceMatrix(paramses[0], paramses[1], paramses[2]);
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
      catch (JSONException e)
      {
        e.printStackTrace();
      }
    }
    catch (Exception e)
    {
      this.exception = e;
      return null;
    }
    return null;
  }

  @Override
  protected void onPostExecute(DistanceMatrix feed)
  {
    // TODO: check this.exception
    // TODO: do something with the feed
  }
}