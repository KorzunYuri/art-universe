# Claude core instructions

The project is a master data management system for art domain.

## Documentation

The project has two documentation types:

- docs/*.md - Documentation dedicated primarily to be read by humans.
- docs/kb/**/* - LLM-optimized Knowledge Base with the root in [docs/kb/README.md](docs/kb/README.md)

When working on a feature, always start from navigating the [Knowledge Base](docs/kb/README.md) to gather important context.

## Agents

You can use the following agents depending on situation:

- gradle-task-executor - use this agent to execute gradle build tasks. You can then pass its output to gradle-log-analyser for analysis in case of failure
- gradle-log-analyzer - use this agent to extract valuable information from the log of failed gradle build 
- gradle-test-analyzer - use this agent to analyse logs of a failed gradle produced by gradle-task-executor or passed to you by user
- deployment-executor - use this agent whenever user asks you to deploy the project to local or prod environment. You can then pass the output to - deployment-log-analyzer in case of a failure
- deployment-log-analyzer - use this agent to analyse logs of a failed deployment produced by deployment-executor or passed to you by user
- code-reviewer - whenever user asks you to provide a code review for a feature/module etc.

## MCP

You can use the following MCP-servers:

- art-universe-mcp - whenever you want to understand current database schema, you should use this MCP server which provides access to dev/local/prod DB instances. 
Production database is always up and running but may miss the intermediate development changes, so always try dev database first, or execute list_connections to understand what is up and what is down.
- art-universe-memory-bank - Whenever you want to find out something about the project, search this server first. Whenever you discover information worth noting - update this server with this info.

## Golden rules

Always follow these rules:
- Don't run any modifying Git commands. You are only allowed to examine the Git history if it is required by the task.
- Whenever you want to get familiar with DB schema or data - use art-universe-mcp
- Keep art-universe-memory-bank updated. 
