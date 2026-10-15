package com.zap.keyboard;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Хранилище шаблонов Zap. Формат: JSON-массив [{name, text}, ...]
 */
public final class ZapManager {

    private static final String PREFS = "ZapData";
    private static final String KEY_ITEMS = "zap_items";

    public static final class ZapItem {
        public final String name;
        public final String text;

        public ZapItem(String name, String text) {
            this.name = name;
            this.text = text;
        }

        public JSONObject toJson() throws Exception {
            JSONObject o = new JSONObject();
            o.put("name", name);
            o.put("text", text);
            return o;
        }

        public static ZapItem fromJson(JSONObject o) {
            return new ZapItem(
                    o.optString("name", ""),
                    o.optString("text", ""));
        }
    }

    private ZapManager() { }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Получить все шаблоны. */
    public static synchronized List<ZapItem> getAll(Context c) {
        List<ZapItem> out = new ArrayList<>();
        try {
            String raw = prefs(c).getString(KEY_ITEMS, "[]");
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o != null) out.add(ZapItem.fromJson(o));
            }
        } catch (Exception ignored) { }
        return out;
    }

    /** Добавить шаблон. */
    public static synchronized boolean add(Context c, String name, String text) {
        if (name == null || name.trim().isEmpty()) return false;
        if (text == null) text = "";
        name = name.trim();
        text = text.trim();

        try {
            List<ZapItem> list = getAll(c);
            // Проверка на дубликат по имени
            for (ZapItem item : list) {
                if (item.name.equalsIgnoreCase(name)) return false;
            }
            list.add(0, new ZapItem(name, text));
            save(c, list);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Удалить шаблон по имени. */
    public static synchronized boolean remove(Context c, String name) {
        if (name == null) return false;
        try {
            List<ZapItem> list = getAll(c);
            boolean removed = false;
            for (int i = list.size() - 1; i >= 0; i--) {
                if (list.get(i).name.equals(name)) {
                    list.remove(i);
                    removed = true;
                }
            }
            if (removed) save(c, list);
            return removed;
        } catch (Exception e) {
            return false;
        }
    }

    private static void save(Context c, List<ZapItem> list) {
        try {
            JSONArray arr = new JSONArray();
            for (ZapItem item : list) arr.put(item.toJson());
            prefs(c).edit().putString(KEY_ITEMS, arr.toString()).apply();
        } catch (Exception ignored) { }
    }
              }
