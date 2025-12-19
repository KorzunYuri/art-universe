# Binding Master Entities to Quiz

Binding master entity to quiz means allowing it to participate in quiz track packs generation.

Current bindings are global and don't belong to any 'user' (at the moment of writing this there are no users in the system).

## Modules Involved

- [Music Master Data Service](../../../../music/data/master/README.md) - provides read access to master entities 
- [Music Quiz service](../../../../music/quiz/README.md) - Creates and stores bindings (basically just master entity ids)
- [Music UI](../../../../music/ui/README.md) - provides user interface for 

## Planned Features

- add users
- allow users to have their own public, private or restricted data sources for quiz packs generation, including basing them on other public data sources
