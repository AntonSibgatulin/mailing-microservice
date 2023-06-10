package jp.konosuba.data.contact;

import jp.konosuba.data.message.MessageAction;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
public class ContactGetter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "contact_id")
    private Contacts contact;
    private String email;
    @ManyToOne
    @JoinColumn(name = "message_action_id")
    private MessageAction messageAction;

}
