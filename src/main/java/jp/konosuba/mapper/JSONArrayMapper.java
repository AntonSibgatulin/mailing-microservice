package jp.konosuba.mapper;

import jp.konosuba.config.Config;
import jp.konosuba.contact.Contacts;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public interface JSONArrayMapper {
    List<Contacts> fromJSONArrayToContacts(JSONArray jsonArray);
    Config fromJsonToConfig(JSONObject jsonObject);

    Contacts fromStringToContacts(JSONObject element);
}
