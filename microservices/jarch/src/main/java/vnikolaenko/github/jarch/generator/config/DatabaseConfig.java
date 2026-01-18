package vnikolaenko.github.jarch.generator.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DatabaseConfig {

    @JsonProperty("type")
    private ApplicationConfig.DatabaseType type = ApplicationConfig.DatabaseType.H2;

    @JsonProperty("host")
    private String host = "localhost";

    @JsonProperty("port")
    private int port = 5432;

    @JsonProperty("databaseName")
    private String databaseName = "appdb";

    @JsonProperty("username")
    private String username = "postgres";

    @JsonProperty("password")
    private String password = "password";

    @JsonProperty("ddlAuto")
    private String ddlAuto = "update";

    @JsonProperty("poolSize")
    private int poolSize = 10;

    public DatabaseConfig() {}

}