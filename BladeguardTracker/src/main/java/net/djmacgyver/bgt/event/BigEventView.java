package net.djmacgyver.bgt.event;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.djmacgyver.bgt.R;

import java.io.InputStream;
import java.text.DateFormat;

public class BigEventView extends SmallEventView {
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                bitmap = BitmapFactory.decodeStream(in);
                //bitmap = Bitmap.createScaledBitmap(original, 128, 128, false);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    private TextView startTime;
    private TextView weatherText;

    public BigEventView(Context context) {
        super(context);

        startTime = (TextView) root.findViewById(R.id.startTime);
        weatherText = (TextView) root.findViewById(R.id.weatherText);
    }

    @Override
    protected int getLayout() {
        return R.layout.eventlistitem_big;
    }

    @Override
    public void setEvent(Event event) {
        super.setEvent(event);
        startTime.setText(getStartTimeText(event));
        String text = getWeatherText(event);
        if (text != null) {
            weatherText.setVisibility(View.VISIBLE);
            weatherText.setText(text);
        } else {
            weatherText.setVisibility(View.GONE);
        }
    }

    @Override
    protected String getMapText(Event event) {
        return event.getMapName();
    }

    public String getStartTimeText(Event event) {
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        return dateFormat.format(event.getStart());
    }

    public String getWeatherText(Event event) {
        if (event.hasWeatherDecision()) {
            return getContext().getResources().getString(event.getWeatherDecision() ? R.string.yes_rolling : R.string.no_cancelled);
        }
        if (!event.hasPrognosis()) return null;

        Prognosis p = event.getPrognosis();
        return getContext().getResources().getString(R.string.forecast, p.getLow(), p.getHigh(), p.getText());
    }

    @Override
    protected void updateWeatherIcon(Event event) {
        int weatherDrawable = getWeatherDrawable(event);
        if (weatherDrawable >= 0) {
            super.updateWeatherIcon(event);
            return;
        }

        if (event.hasPrognosis()) {
            Prognosis p = event.getPrognosis();
            weatherLayout.setVisibility(View.VISIBLE);
            new DownloadImageTask(weatherIcon).execute(getContext().getResources().getString(R.string.servername) + p.getImage());
        } else {
            weatherLayout.setVisibility(View.GONE);
        }
    }
}
