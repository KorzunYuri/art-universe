# Ticket Intake Service

Lightweight REST service written in **Go with Gin** framework. Receives analysis tickets,
validates them, checks for duplicates, and persists to the `mu_semantic_analysis.analysis_ticket` table.

This service decouples ticket creators (Java trigger services) from the ticket storage.
The semantic analyzer polls the table asynchronously.


## How It Works

1. Ticket creators POST to `/api/v1/tickets` (single) or `/api/v1/tickets/batch`
2. [`TicketService`](internal/service/ticket_service.go) validates and resolves string codes
   (data_source, entity_type, analysis_mode) to integer codes using maps in
   [`ticket.go`](internal/model/ticket.go)
3. Deduplication: checks for existing PENDING/PROCESSING tickets with the same
   `(data_source, subject_type, subject_id, analysis_mode, md5(text_samples))`.
   Same subject under different analysis modes is NOT a duplicate
4. [`TicketRepo`](internal/repository/ticket_repo.go) persists to `mu_semantic_analysis.analysis_ticket`

### Code Mappings

| Field | String -> Code |
|-------|---------------|
| entity_type | artist(1), album(2), track(3), category(4), person(101) |
| data_source | lastfm(1), spotify(2), musicbrainz(3), master(4) |
| analysis_mode | full_extraction(1), creative_categorization(2) |


## Ticket Request Example

```json
{
  "data_source": "lastfm",
  "analysis_mode": "full_extraction",
  "subject": { "entity_type": "artist", "entity_id": 42, "name": "ABBA" },
  "text_samples": [
    { "content": "ABBA were a Swedish pop group...", "comment": "bio_content" }
  ],
  "expected_proposal_types": [1, 2, 3, 6, 7, 9],
  "expected_entity_types": [1, 2, 101]
}
```


## See Also

- [Semantic Analysis Service](../../../docs/kb/features/semantic-analysis-service.md) -- feature overview
- [Semantic Analyzer](../semantic-analyzer/README.md) -- downstream consumer of tickets
- [Semantic Models](../semantic-models/README.md) -- integer code definitions matching this service's code maps
