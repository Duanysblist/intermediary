# intermediary MCP server

Gives Claude Desktop, Claude Code, or any MCP client a direct line to your plan, with one rule:
**reads and session logging happen immediately; changes to intentions become proposals** that you
review in the app before anything is applied.

| Tool | What it does |
|---|---|
| `get_plan_overview` | Snapshot: open plan items, items closed in the recent window, recent sessions and events (each naming its plan item), certifications, active applications. |
| `list_plan_items` | All plan items (optionally by status), for ids. |
| `propose_plan_changes` | Submits a change set (`update` / `create`, same JSON contract as the app). Lands in the app's Prompt page inbox. |
| `log_study_session` / `log_fitness_session` | Records reality directly, optionally linked to a plan item. |
| `list_pending_proposals` | What's already waiting, to avoid duplicates. |

## Setup

```bash
cd mcp
npm install
npm run build
```

Then register it with your client. For Claude Desktop, add to `claude_desktop_config.json`
(Settings → Developer → Edit Config):

```json
{
  "mcpServers": {
    "intermediary": {
      "command": "node",
      "args": ["C:/Users/User/coding/intermediary/mcp/dist/index.js"],
      "env": {
        "INTERMEDIARY_API_URL": "https://intermediary-loxn.onrender.com",
        "INTERMEDIARY_USERNAME": "your-username",
        "INTERMEDIARY_PASSWORD": "your-password"
      }
    }
  }
}
```

For Claude Code:

```bash
claude mcp add intermediary -e INTERMEDIARY_API_URL=https://intermediary-loxn.onrender.com -e INTERMEDIARY_USERNAME=you -e INTERMEDIARY_PASSWORD=secret -- node C:/Users/User/coding/intermediary/mcp/dist/index.js
```

Point `INTERMEDIARY_API_URL` at `http://localhost:8080` to work against a local backend.

## Try it

Ask Claude: *"Look at my plan and propose how next week should go."* It calls
`get_plan_overview`, reasons, then calls `propose_plan_changes`. Open the app's **Prompt** page:
the proposal is in the inbox with a Review button. Tick what you accept, Apply, done. The
proposal is marked applied (or dismissed) so it doesn't come back.

The server logs in with the account's username and password and keeps the token in memory; it
re-authenticates when the token expires. Credentials are read from the environment only.
