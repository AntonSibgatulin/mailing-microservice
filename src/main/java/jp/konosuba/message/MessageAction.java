package jp.konosuba.message;

import jdk.jfr.Enabled;
import jp.konosuba.contact.Contacts;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Data
@Entity
public class MessageAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @ManyToOne
    private Contacts contacts;

    @Transient
    private MessageObject messageObject;
}
