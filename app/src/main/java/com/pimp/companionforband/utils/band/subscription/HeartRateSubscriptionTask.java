package com.pimp.companionforband.utils.band.subscription;

import android.os.AsyncTask;
import android.widget.TextView;

import com.microsoft.band.BandException;
import com.microsoft.band.UserConsent;
import com.pimp.companionforband.R;
import com.pimp.companionforband.activities.main.MainActivity;
import com.pimp.companionforband.fragments.sensors.SensorsFragment;
import com.pimp.companionforband.utils.band.BandUtils;

public class HeartRateSubscriptionTask extends AsyncTask<TextView, Void, Void> {
    @Override
    protected Void doInBackground(TextView... params) {
        try {
            if (BandUtils.getConnectedBandClient()) {
                if (MainActivity.client.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
                    MainActivity.client.getSensorManager().registerHeartRateEventListener(SensorsFragment.bandHeartRateEventListener);
                } else {
                    MainActivity.sActivity.runOnUiThread(new Runnable() {
                        @SuppressWarnings("unchecked")
                        @Override
                        public void run() {
                            new HeartRateConsentTask().execute(SensorsFragment.reference);
                        }
                    });
                    SensorsFragment.appendToUI(MainActivity.sContext.getString(R.string.heart_rate_consent) + "\n", params[0]);
                }
            } else {
                MainActivity.appendToUI(MainActivity.sContext.getString(R.string.band_not_found), "Style.ALERT");
            }
        } catch (BandException e) {
            BandUtils.handleBandException(e);
        } catch (Exception e) {
            MainActivity.appendToUI(e.getMessage(), "Style.ALERT");
        }
        return null;
    }
}
