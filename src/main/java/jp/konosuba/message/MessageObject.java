package jp.konosuba.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class MessageObject {
    private String type;
    private String typeMessage;
    private String message;
}
