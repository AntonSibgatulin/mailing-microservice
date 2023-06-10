package jp.konosuba.data.mapper;

import jp.konosuba.config.Config;
import jp.konosuba.data.contact.Contacts;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public interface JSONArrayMapper {
    List<Contacts> fromJSONArrayToContacts(JSONArray jsonArray);
    Config fromJsonToConfig(JSONObject jsonObject);

    Contacts fromStringToContacts(JSONObject element);
}
