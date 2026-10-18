package com.gathin.tvhome;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/** Current temperature and sky for the pill beside the clock: IP location (ipwho.is) + Open-Meteo, no API keys. */
final class Weather {

    private static final long MAX_AGE_MS = 30 * 60_000L;

    private Weather() {
    }

    /** "Temuco  14°  Nublado" or null; blocking, call off the main thread. Cached for 30 minutes. */
    static String fetch(Context ctx) {
        SharedPreferences p = ctx.getSharedPreferences("tvhome", Context.MODE_PRIVATE);
        long at = p.getLong("wx_at", 0);
        String cached = p.getString("wx_text", null);
        if (cached != null && System.currentTimeMillis() - at < MAX_AGE_MS) return cached;
        try {
            JSONObject loc = new JSONObject(get("https://ipwho.is/"));
            double lat = loc.getDouble("latitude"), lon = loc.getDouble("longitude");
            String city = loc.optString("city", "");
            JSONObject cur = new JSONObject(get("https://api.open-meteo.com/v1/forecast?latitude=" + lat
                    + "&longitude=" + lon + "&current=temperature_2m,weather_code&timezone=auto"))
                    .getJSONObject("current");
            long temp = Math.round(cur.getDouble("temperature_2m"));
            String text = (city.isEmpty() ? "" : city + "  ") + temp + "°  " + ctx.getString(sky(cur.getInt("weather_code")));
            p.edit().putString("wx_text", text).putLong("wx_at", System.currentTimeMillis()).apply();
            return text;
        } catch (Exception e) {
            return cached;
        }
    }

    private static int sky(int code) {
        if (code == 0) return R.string.w_clear;
        if (code <= 3) return R.string.w_cloudy;
        if (code <= 48) return R.string.w_fog;
        if (code <= 67 || (code >= 80 && code <= 82)) return R.string.w_rain;
        if (code <= 77 || code == 85 || code == 86) return R.string.w_snow;
        return R.string.w_storm;
    }

    private static String get(String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(6000);
        c.setReadTimeout(6000);
        try (BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            for (String line; (line = r.readLine()) != null; ) sb.append(line);
            return sb.toString();
        } finally {
            c.disconnect();
        }
    }
}
