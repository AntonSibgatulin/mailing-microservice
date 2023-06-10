package jp.konosuba.data.mapper;

import jp.konosuba.config.Config;
import jp.konosuba.data.contact.Contacts;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonArrayImpl implements JSONArrayMapper {

    @Override
    public List<Contacts> fromJSONArrayToContacts(JSONArray jsonArray) {
        List<Contacts> contacts = new ArrayList<>();

        for(var i = 0;i<jsonArray.length();i++){
            var element = jsonArray.getJSONObject(i);
            Contacts contact = new Contacts();
            contact.setId(element.getLong("id"));
            contact.setEmail(element.getString("email"));
            contact.setPhone(element.getString("phone"));
            contact.setName(element.getString("name"));
            contact.setRelative(element.getBoolean("relative"));
            contact.setTg(element.getBoolean("tg"));
            contact.setVk(element.getBoolean("vk"));
            contact.setWs(element.getBoolean("ws"));
            contacts.add(contact);
        }
        return contacts;
    }

    @Override
    public Config fromJsonToConfig(JSONObject jsonObject) {
        //Config config = new Config(jsonObject.getInt("countThreadInPoll"));
       // return config;
        return null;

    }

    @Override
    public Contacts fromStringToContacts(JSONObject element) {
        var contact = new Contacts();
        contact.setId(element.getLong("id"));
        contact.setEmail(element.getString("email"));
        contact.setPhone(element.getString("phone"));
        contact.setName(element.getString("name"));
        contact.setRelative(element.getBoolean("relative"));
        contact.setTg(element.getBoolean("tg"));
        contact.setVk(element.getBoolean("vk"));
        contact.setWs(element.getBoolean("ws"));

        return contact;
    }
}
