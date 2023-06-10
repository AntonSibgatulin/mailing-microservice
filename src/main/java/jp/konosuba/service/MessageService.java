package jp.konosuba.service;

import jp.konosuba.data.message.MessageAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {

    @Autowired
    private MessageActionRepository messageActionRepository;

    @Transactional
    public void saveMessageAction(MessageAction messageAction) {
        messageActionRepository.save(messageAction);
    }


}
