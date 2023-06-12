package jp.konosuba.data.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jp.konosuba.data.contact.Contacts;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Data

public class MessageAction {

    private Long userId;
    private String messageId;
    private Contacts contacts;

    @JsonIgnore
    private MessageObject messageObject;
    //private Boolean lastOne = false;
}
