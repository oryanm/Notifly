package net.notifly.core;

import android.os.AsyncTask;

import org.json.JSONException;

import java.io.IOException;

/**
 * Created by Barak on 08/03/14.
 */
class RetreiveResultTask extends AsyncTask<String, Void, String>
{
  private Exception exception;

  @Override
  protected String doInBackground(String... paramses)
  {
    try
    {
      try
      {
        return LocationUtils.getDistanceMatrix(paramses[0], paramses[1], paramses[2]);
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
  protected void onPostExecute(String feed)
  {
    // TODO: check this.exception
    // TODO: do something with the feed
  }
}