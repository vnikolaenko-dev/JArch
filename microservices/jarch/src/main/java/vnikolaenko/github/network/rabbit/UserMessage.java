package vnikolaenko.github.network.rabbit;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userName;
    private String action;
    private String email;
}
