package net.djmacgyver.bgt.event;

import org.json.JSONObject;

public class Prognosis {
    //{"image":"\/weather\/icons\/29.png","text":"Teilweise bewölkt (nachts)","code":"29","high":"17","day":"Wed","low":"9","date":"28 May 2014"}

    private String image;
    private int weatherCode;
    private int low;
    private int high;
    private String text;

    public Prognosis(JSONObject source) {
        image = source.optString("image");
        weatherCode = source.optInt("code");
        low = source.optInt("low");
        high = source.optInt("high");
        text = source.optString("text");
    }

    public String getImage() {
        return image;
    }

    @SuppressWarnings("unused")
    public int getWeatherCode() {
        return weatherCode;
    }

    public int getLow() {
        return low;
    }

    public int getHigh() {
        return high;
    }

    public String getText() {
        return text;
    }
}
