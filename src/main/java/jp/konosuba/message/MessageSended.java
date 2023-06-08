package jp.konosuba.message;

import jp.konosuba.contact.ContactGetter;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Data
@Entity
@NoArgsConstructor
public class MessageSended {
    private Long id;
    @ManyToOne
    @JoinColumn(name = "contact_getter_id")
    private ContactGetter contactGetter;
    @ManyToOne
    @JoinColumn(name = "message_action_id")
    private MessageAction messageAction;

}
