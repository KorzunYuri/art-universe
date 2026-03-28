package yurykorzun.art.universe.music.data.master.category.scanner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.master.category.client.TicketIntakeClient;
import yurykorzun.art.universe.music.data.master.category.config.MasterTriggerProperty;

import java.util.ArrayList;
import java.util.List;

@Component
public class MasterEntityScanner {

    private static final Logger log = LoggerFactory.getLogger(MasterEntityScanner.class);

    private final JdbcTemplate jdbcTemplate;
    private final TicketIntakeClient intakeClient;
    private final ConfigPropertyHolder configPropertyHolder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MasterEntityScanner(
        JdbcTemplate jdbcTemplate,
        TicketIntakeClient intakeClient,
        ConfigPropertyHolder configPropertyHolder
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.intakeClient = intakeClient;
        this.configPropertyHolder = configPropertyHolder;
    }

    public int scanArtists() {
        return scanEntities("artist", "ARTIST", 1);
    }

    public int scanAlbums() {
        return scanEntities("album", "ALBUM", 2);
    }

    public int scanTracks() {
        return scanEntities("track", "TRACK", 3);
    }

    private int scanEntities(String tableName, String entityTypeName, int entityTypeCode) {
        int batchSize = configPropertyHolder.getInt(MasterTriggerProperty.BATCH_SIZE);
        List<ObjectNode> tickets = new ArrayList<>();

        jdbcTemplate.query(
            String.format(
                "SELECT id, name FROM mu.%s " +
                "WHERE id NOT IN (SELECT ac.%s_id FROM mu.artist_category ac) " +
                "ORDER BY id LIMIT ?",
                tableName, tableName.equals("artist") ? "artist" : "artist"
            ),
            rs -> {
                ObjectNode ticket = objectMapper.createObjectNode();
                ticket.put("data_source", "MASTER");

                ObjectNode subject = ticket.putObject("subject");
                subject.put("entity_type", entityTypeName);
                subject.put("entity_id", rs.getLong("id"));
                subject.put("name", rs.getString("name"));

                ArrayNode samples = ticket.putArray("text_samples");
                ObjectNode sample = samples.addObject();
                sample.put("content", rs.getString("name"));
                sample.put("comment", entityTypeName.toLowerCase() + "_name");

                ArrayNode proposalTypes = ticket.putArray("expected_proposal_types");
                proposalTypes.add(6).add(7);

                tickets.add(ticket);
            },
            batchSize
        );

        if (!tickets.isEmpty()) {
            log.info("Scanned {} {} entities without category bindings", tickets.size(), entityTypeName);
            intakeClient.submitBatch(tickets);
        }

        return tickets.size();
    }
}
