package jp.konosuba.controller;

import jp.konosuba.contact.Contacts;
import jp.konosuba.main.MainTask;
import jp.konosuba.mapper.JSONArrayMapper;
import jp.konosuba.mapper.JsonArrayImpl;
import jp.konosuba.message.MessageAction;
import jp.konosuba.message.MessageObject;
import jp.konosuba.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class MainController {

    private JSONArrayMapper jsonArrayMapper = new JsonArrayImpl();
    private MainTask mainTask;

    public MainController(MainTask mainTask){
        this.mainTask = mainTask;
    }


    public void execute(String text){
        System.out.println(text);
        JSONObject jsonObject = new JSONObject(text);
        String typeOperation = jsonObject.getString("typeOperation");

        if (typeOperation.equals("send")){
           // if(true==true)
           // return ;
            Contacts contact = jsonArrayMapper.fromStringToContacts(jsonObject.getJSONObject("contact"));
            String messageId = jsonObject.getString("messageId");
            String type = jsonObject.getString("type");


            //////////////////////////////////////
            String split_ = getMessage(messageId);
            String split[] = split_.split(";");

            MessageObject messageObject = new MessageObject();
            messageObject.setMessage(split_.replace(split[0]+";",""));
            messageObject.setType(type);
            messageObject.setTypeMessage(split[0]);
            /////////////////////////////////////

            MessageAction messageAction = new MessageAction();
            messageAction.setMessageObject(messageObject);
            messageAction.setContacts(contact);
            mainTask.put(messageAction);

        }

    }


    public Contacts getContact(String hash,Long id){
        String data = mainTask.getJedis().rpop(hash+id);
        if (data==null){
            return null;
        }
        String split [] = data.split(";");
        Contacts contacts = new Contacts();
        contacts.setId(Long.valueOf(split[0]));
        contacts.setPhone(split[1]);
        contacts.setEmail(split[2]);
        contacts.setTg(Boolean.valueOf(split[3]));
        contacts.setVk(Boolean.valueOf(split[4]));
        contacts.setWs(Boolean.valueOf(split[5]));
        return contacts;
    }


    public String getMessage(String hash) {
        return mainTask.getJedis().get(hash);
    }
}
