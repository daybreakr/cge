package com.cmge.cge.sdk.network;

public abstract class ThreadTask<Params, Progress, Result>{
    
    public void execute(final Params params) {
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                Result result = doInBackground(params);
                onPostExecute(result);
            }
        }).start();
    }
    
    protected abstract Result doInBackground(Params params);
    
    protected void onPostExecute(Result result) {
    }
    
    protected boolean isCancelled() {
        return false;
    }
}
