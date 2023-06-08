package jp.konosuba.contact;

import jp.konosuba.message.MessageAction;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Data
@NoArgsConstructor
public class ContactGetter {
    private Long id;
    @ManyToOne
    @JoinColumn(name = "contact_id")
    private Contacts contact;
    private String email;
    @ManyToOne
    @JoinColumn(name = "message_action_id")
    private MessageAction messageAction;
}
