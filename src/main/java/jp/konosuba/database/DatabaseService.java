package jp.konosuba.database;

import jp.konosuba.hibernate.HibernateUtils;
import jp.konosuba.data.message.MessageAction;
import org.hibernate.Session;

public class DatabaseService {

    public DatabaseService(){

    }
    public void saveMessageAction(MessageAction messageAction){
        Session session = HibernateUtils.getSessionFactory().openSession();
        session.save(messageAction);
        session.close();
    }
}
