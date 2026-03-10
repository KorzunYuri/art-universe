package yurykorzun.art.universe.music.data.raw.spotify.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class MasterDataBindingClientConfig {

    @Value("${spotify.integration.music-data-master.base-url}")
    private String baseUrl;

    @Bean
    public MasterDataBindingClient masterDataBindingClient() {
        RestClient restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .build();
        return new MasterDataBindingClient(restClient);
    }
}
