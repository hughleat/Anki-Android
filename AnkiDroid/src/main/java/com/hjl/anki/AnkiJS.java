package com.hjl.anki;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.ichi2.libanki.Card;
import com.ichi2.libanki.Collection;
import com.ichi2.libanki.Note;

import com.ichi2.utils.JSONArray;
import com.ichi2.utils.JSONException;
import com.ichi2.utils.JSONObject;

import java.util.Iterator;

public class AnkiJS {
    private static AnkiJS ankiJS = new AnkiJS();

    private static Card card;
    public static void setCard(Card c) {
        card = c;
    }
    public static void addToWebView(WebView webView) {
        webView.addJavascriptInterface(ankiJS, "ankiJS");
    }

    @JavascriptInterface
    public String getNote() {
        try {
            if(card == null) return null;
            Collection coll = card.getCol();
            Note note = card.note();
            JSONObject jsonNote = new JSONObject();
            jsonNote.put("deckName", coll.getDecks().name(card.getDid()));
            jsonNote.put("modelName", note.model().getString("name"));
            jsonNote.put("tags", new JSONArray(note.getTags()));
            JSONObject jsonFields = new JSONObject();
            for(String[] kv : note.items()) {
                jsonFields.put(kv[0], kv[1]);
            }
            jsonNote.put("fields", jsonFields);
            return jsonNote.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    @JavascriptInterface
    public String addNote(String jsonStringNote) {
        JSONObject r = new JSONObject();
        try {
            r.put("result", null);
            r.put("error", null);
            if (jsonStringNote != null) {
                JSONObject jsonNote = new JSONObject(jsonStringNote);
                Collection coll = card.getCol();

                JSONObject model = coll.getModels().byName(jsonNote.optString("modelName", card.note().model().getString("name")));
                JSONObject deck = coll.getDecks().byName(jsonNote.optString("deckName", coll.getDecks().name(card.getDid())));

                Note ankiNote = coll.newNote(model);
                ankiNote.model().put("did", deck.getLong("id"));

                if(jsonNote.has("tags")) {
                    JSONArray jsonTags = jsonNote.getJSONArray("tags");
                    for (int i = 0; i < jsonTags.length(); ++i) {
                        ankiNote.addTag(jsonTags.getString(i));
                    }
                }

                JSONObject jsonFields = jsonNote.getJSONObject("fields");
                for(Iterator<String> i = jsonFields.keys(); i.hasNext(); ) {
                    String k = i.next();
                    String v = jsonFields.getString(k);
                    ankiNote.setItem(k, v);
                }

                boolean allowDups = false;
                JSONObject jsonOptions = jsonNote.optJSONObject("options");
                if(jsonOptions != null) {
                    allowDups = jsonOptions.optBoolean("allowDuplicate", false);
                }
                Integer duplicateOrEmpty = ankiNote.dupeOrEmpty();
                if(duplicateOrEmpty != null) {
                    if(duplicateOrEmpty == 1) { // Do something because empty
                        r.put("error", "Cannot add empty note");
                        return r.toString();
                    } else if(duplicateOrEmpty == 2) { // Duplicate
                        if (!allowDups) {
                            r.put("error", "Cannot add duplicate note");
                            return r.toString();
                        }
                    } else { // Unknown thing
                        r.put("error", "An unknown error occurred");
                        return r.toString();
                    }
                }

                coll.addNote(ankiNote);
                coll.save();
                r.put("result", ankiNote.getId());
                return r.toString();
            } else {
                r.put("error", "Cannot add null note");
                return r.toString();
            }
        } catch (JSONException e) {
            return "{\"result\":null,\"error\":\"" + e.getLocalizedMessage() + "\"}";
        }
    }
}
