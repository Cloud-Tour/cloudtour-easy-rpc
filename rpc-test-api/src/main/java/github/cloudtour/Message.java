package github.cloudtour;

import lombok.*;

import java.io.Serializable;

/**
 * 传输的对象
 *
 * @author cloudtour
 * @version 1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Message implements Serializable {
    private String message;
    private String description;
}
