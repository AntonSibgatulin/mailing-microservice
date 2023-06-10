package jp.konosuba.data.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@NoArgsConstructor
public class MessageObject {

    private Long id;
    private String type;
    private String typeMessage;
    private String message;

    private String hashId;

    public MessageObject(String type, String typeMessage, String message) {
        this.type = type;
        this.typeMessage = typeMessage;
        this.message = message;
    }
}
