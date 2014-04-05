package net.notifly.core.service;

import android.os.AsyncTask;

import net.notifly.core.entity.DistanceMatrix;
import net.notifly.core.util.LocationHandler;

public class RetreiveDistanceMatrixTask extends AsyncTask<String, Void, DistanceMatrix> {
    private Exception exception;

    @Override
    protected DistanceMatrix doInBackground(String... params) {
        try {
            return LocationHandler.getDistanceMatrix(params[0], params[1], params[2]);
        } catch (Exception e) {
            this.exception = e;
            return null;
        }
    }

    @Override
    protected void onPostExecute(DistanceMatrix feed) {
        // TODO: check this.exception
        // TODO: do something with the feed
    }
}