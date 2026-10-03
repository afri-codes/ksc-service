package ksc.go.tz.common.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfiguration {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI defineOpenApi() {
        Server server = new Server();
        // Relative, so "Try it out" calls whichever host serves this page
        server.setUrl("/");
        server.setDescription("Development");
        Contact cont = new Contact();
        cont.setName("KSC Limited");
        cont.setEmail("ksc@gmail.com");

        Info information = new Info()
                .title("Cleaning, Fumigation & Property Management Platform")
                .version("0.1")
                .description("This is the API documentation for the Cleaning, Fumigation & Property Management Platform. It provides endpoints for managing users, properties, and related services.")
                .contact(cont);
        return new OpenAPI().info(information).servers(List.of(server))
                .components(new Components().addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                        .description("Platform JWT of the logged-in user")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}
