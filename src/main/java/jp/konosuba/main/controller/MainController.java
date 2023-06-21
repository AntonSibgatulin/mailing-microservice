package jp.konosuba.main.controller;

import jp.konosuba.data.contact.Contacts;
import jp.konosuba.main.task.MainTask;
import jp.konosuba.data.mapper.JSONArrayMapper;
import jp.konosuba.data.mapper.JsonArrayImpl;
import jp.konosuba.data.message.MessageAction;
import jp.konosuba.data.message.MessageObject;
import org.apache.kafka.common.protocol.types.Field;
import org.json.JSONObject;

public class MainController {

    private JSONArrayMapper jsonArrayMapper = new JsonArrayImpl();
    private MainTask mainTask;


    public MainController(MainTask mainTask){
        this.mainTask = mainTask;



    }


    public void execute(String text){
        System.out.println(text);
        JSONObject jsonObject = new JSONObject(text);

        if(!jsonObject.has("typeOperation"))return;

        String typeOperation = jsonObject.getString("typeOperation");

        if (typeOperation.equals("send")){
            if(!jsonObject.has("contact"))return;
           // if(true==true)
           // return ;
            Contacts contact = jsonArrayMapper.fromStringToContacts(jsonObject.getJSONObject("contact"));
            if(jsonObject.get("messageId") instanceof Integer) return;
            String messageId = jsonObject.getString("messageId");
            String type = jsonObject.getString("type");
            Long userId = jsonObject.getLong("userId");

            //////////////////////////////////////

            MessageObject messageObject =  mainTask.getMessageObject(messageId);
            if (messageObject == null) {

                String split_ = mainTask.getMessage(messageId);
                String split[] = split_.split(";");

                messageObject = new MessageObject();
                messageObject.setMessage(split_.replace(split[0] + ";", ""));
                messageObject.setType(type);
                messageObject.setTypeMessage(split[0]);
                messageObject.setHashId(messageId);
                mainTask.saveMessageObject(messageId,messageObject);
            }

            MessageAction messageAction = new MessageAction();
            messageAction.setMessageId(messageId);
            messageAction.setUserId(userId);
            messageAction.setMessageObject(messageObject);
            messageAction.setContacts(contact);
            //if(jsonObject.has("lastOne")){
            //    messageAction.setLastOne(jsonObject.getBoolean("lastOne"));
            //}
            mainTask.put(messageAction);

        }
        if(typeOperation.equals("end_send")){
            jsonObject.put("typeOperation","end_send_confirm");
            mainTask.removeMessageFromCache("messageId");
            mainTask.sendMessageInKafka(jsonObject.toString());
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

   }
