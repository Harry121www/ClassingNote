# Project Analysis Feature — Design Spec

**Date:** 2026-05-20
**Status:** Approved

## Overview

Add a feature that lets users drag-and-drop a project folder (e.g., a Java project) into ClassingNote. The system sends source files to a configurable LLM API, which generates structured knowledge points organized by layer (per-file → per-package → project overview). Results can be saved as a Markdown note or as structured knowledge base entries.

## Architecture

```
Frontend (Vue 3)
  Drag Zone → File Tree → Analysis Panel (SSE) → Save Dialog

Backend (Spring Boot)
  AnalysisController (SSE) → AnalysisService → LLMService (interface)
                                                   ├── OpenAIServiceImpl
                                                   ├── AnthropicServiceImpl
                                                   └── OllamaServiceImpl
```

### Data Flow

1. User drags a folder into the browser; File API reads directory structure recursively
2. Frontend renders a file tree; user selects which files to analyze
3. `POST /api/v1/analysis/stream` sends `[{path, content}]` to backend
4. Backend builds prompts in three phases, calls LLM with streaming
5. SSE events stream back to frontend; result renders progressively in Markdown
6. User saves result as a Note or Knowledge Base entry

## Components

### Backend — LLM Service Abstraction

```
src/main/java/com/classnote/llm/
├── LLMProvider.java          # Enum: OPENAI, ANTHROPIC, OLLAMA, DEEPSEEK, QWEN, CUSTOM
├── LLMConfig.java            # Provider, apiKey, baseUrl, model
├── LLMConfigService.java     # CRUD for llm_config table
├── LLMService.java           # Flux<String> streamChat(systemPrompt, userPrompt)
├── OpenAIServiceImpl.java    # OpenAI-compatible (covers DeepSeek, Qwen, etc.)
├── AnthropicServiceImpl.java # Anthropic Messages API
├── OllamaServiceImpl.java    # Local Ollama
└── LLMServiceFactory.java    # Returns LLMService for given config
```

- All implementations share the same `LLMService` interface returning `Flux<String>`
- `LLMServiceFactory` selects the impl based on `LLMConfig.provider`
- Adding a new provider requires only one new impl class
- 120s timeout + 2 retries handled at the interface level
- Config stored in database table `llm_config`

### Backend — Analysis Service

`AnalysisService` drives the three-phase prompt strategy:

1. **Per-file analysis** — For each selected source file, prompt: "Analyze the following Java class, summarize its responsibility, key methods, and dependencies: [path]\n{content}". Files can be processed in parallel.
2. **Package-level summary** — Group per-file results by package/directory. Prompt: "Here are summaries of classes in package {pkg}. Generate knowledge points for this package."
3. **Project overview** — Aggregate all package summaries. Prompt: "Generate an overall architecture overview from the following module summaries."

Each phase emits `AnalysisEvent` to the SSE stream:

```json
{
  "phase": "PER_FILE | PACKAGE | OVERVIEW | ERROR | DONE",
  "path": "com.example.service",
  "content": "streaming chunk...",
  "complete": false
}
```

### Backend — New Entities & APIs

**Knowledge Base entity** (`KnowledgeEntry`):

| Field | Type | Notes |
|-------|------|-------|
| id | Long | PK |
| title | String | |
| content | Text (LONGTEXT) | Markdown |
| projectName | String | Source project name |
| category | Enum | PER_FILE, PACKAGE, OVERVIEW |
| filePath | String | nullable, for per-file entries |
| packageName | String | nullable, for package-level entries |
| sourceNoteId | Long | nullable, if also saved as note |
| createdAt / updatedAt | Timestamp | |

**New REST endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/analysis/stream` | Start streaming analysis (SSE) |
| GET | `/api/v1/llm/config` | Get current LLM config |
| PUT | `/api/v1/llm/config` | Update LLM config |
| POST | `/api/v1/knowledge` | Create knowledge entry |
| GET | `/api/v1/knowledge?project=X` | List entries by project |
| GET | `/api/v1/knowledge/{id}` | Get single entry |
| DELETE | `/api/v1/knowledge/{id}` | Delete entry |

### Frontend

**New view:** `ProjectAnalyzer.vue` at route `/analyzer`

**New components:**
- `FolderDropZone.vue` — Drag-and-drop zone using `DataTransferItem.webkitGetAsEntry()`
- `FileTreeSelector.vue` — Renders directory tree with checkboxes; supports select-all / deselect-all
- `AnalysisProgress.vue` — Phase indicator + progress bar
- `AnalysisResult.vue` — Renders streaming Markdown output
- `SaveResultDialog.vue` — Choose save target (note or knowledge base), title, notebook

**State management** (`stores/analysis.js`):

```javascript
{
  phase: 'idle' | 'uploading' | 'analyzing' | 'done' | 'error',
  currentStage: 'per_file' | 'package' | 'overview',
  currentTarget: 'UserService.java',
  streamContent: '',
  error: null,
}
```

**API layer** (`api/analysis.js`): Uses `fetch` + `ReadableStream` to consume SSE events.

**Settings page:** Add an "LLM Configuration" card with provider selector, API key (masked), base URL, model name, and a "Test Connection" button.

**Sidebar:** Add "Project Analyzer" and "Knowledge Base" navigation items.

## Error Handling

- **File too large** (>500KB per file or >100 files): show warning, suggest filtering
- **LLM API error**: propagate via SSE `ERROR` event; show retry button in UI
- **Network interruption**: auto-reconnect SSE; resume from last completed phase
- **Empty file selection**: disable "Start Analysis" button

## Security

- LLM API key stored server-side only (database or application.yml)
- API key never exposed to frontend; `GET /api/v1/llm/config` returns config with key masked
- Analysis endpoint validates file size and count limits server-side
- Only text files accepted; binary rejected based on extension whitelist: `.java, .py, .js, .ts, .jsx, .tsx, .vue, .kt, .swift, .go, .rs, .c, .cpp, .cs, .rb, .php, .scala, .xml, .yml, .yaml, .json, .properties, .gradle, .md, .txt, .css, .scss, .html, .sql`
- `KnowledgeEntry.category` enum values match `AnalysisEvent.phase`: `PER_FILE`, `PACKAGE`, `OVERVIEW`
- `KnowledgeEntry.sourceNoteId` is a nullable reference to the `Note` entity, set only when the user also saves the result as a note
