#!/usr/bin/env node
/**
 * intermediary MCP server.
 *
 * Lets Claude Desktop, Claude Code, or any MCP client read your plan and log reality directly,
 * while changes to intentions go through a proposal that you review in the app. Reads and
 * session logging are safe to do straight away; rewriting the plan is not, so it isn't allowed here.
 *
 * Configuration (environment):
 *   INTERMEDIARY_API_URL   e.g. https://intermediary-loxn.onrender.com  (default http://localhost:8080)
 *   INTERMEDIARY_USERNAME  the app's single account
 *   INTERMEDIARY_PASSWORD
 */
import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js'
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js'
import { z } from 'zod'

const API_URL = (process.env.INTERMEDIARY_API_URL ?? 'http://localhost:8080').replace(/\/$/, '')
const USERNAME = process.env.INTERMEDIARY_USERNAME
const PASSWORD = process.env.INTERMEDIARY_PASSWORD

// ---- API client (same contract as the frontend) ---------------------------------------------

let token: string | null = null
let tokenExpires = 0

async function login(): Promise<string> {
    if (!USERNAME || !PASSWORD) throw new Error('Set INTERMEDIARY_USERNAME and INTERMEDIARY_PASSWORD in the MCP server environment.')
    const res = await fetch(`${API_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: USERNAME, password: PASSWORD }),
    })
    if (!res.ok) throw new Error(`Login failed (${res.status}). Check INTERMEDIARY_USERNAME / INTERMEDIARY_PASSWORD.`)
    const body = (await res.json()) as { token: string; expiresAt: string }
    token = body.token
    tokenExpires = new Date(body.expiresAt).getTime() - 60_000
    return token
}

async function api<T>(path: string, init?: RequestInit): Promise<T> {
    if (!token || Date.now() > tokenExpires) await login()
    const res = await fetch(`${API_URL}${path}`, {
        ...init,
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}`, ...(init?.headers ?? {}) },
    })
    if (res.status === 401) {
        await login()
        return api<T>(path, init)
    }
    const text = await res.text()
    if (!res.ok) throw new Error(`${init?.method ?? 'GET'} ${path} -> ${res.status}: ${text.slice(0, 300)}`)
    return (text ? JSON.parse(text) : undefined) as T
}

// ---- Shapes -----------------------------------------------------------------------------------

const Intent = z.enum(['STUDY', 'EXERCISE', 'APPLY', 'READ', 'WRITE', 'OTHER'])
const Status = z.enum(['PLANNED', 'IN_PROGRESS', 'DONE', 'DEFERRED', 'CANCELED'])
const IsoDate = z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'YYYY-MM-DD')
const IsoDateTime = z.string().regex(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2})?$/, 'YYYY-MM-DDTHH:mm')

const ChangeShape = z.object({
    op: z.enum(['update', 'create']),
    id: z.number().int().positive().nullable().describe('Plan item id for update; null for create'),
    fields: z.object({
        title: z.string().optional(),
        intent: Intent.optional(),
        status: Status.optional(),
        targetDate: z.union([IsoDate, z.literal('CLEAR')]).optional().describe('YYYY-MM-DD to move, or CLEAR to take off the calendar'),
        notes: z.string().optional(),
    }),
    reason: z.string().optional().describe('One sentence, referencing the data'),
})

type PlanItem = {
    id: number; title: string; intent: string; status: string; targetDate: string | null
    referenceEntityType: string | null; referenceEntityId: number | null; recurringPlanId: number | null; notes: string | null; updatedAt: string
}

const text = (data: unknown) => ({ content: [{ type: 'text' as const, text: typeof data === 'string' ? data : JSON.stringify(data, null, 2) }] })

// ---- Server -----------------------------------------------------------------------------------

const server = new McpServer({ name: 'intermediary', version: '0.1.0' })

server.registerTool(
    'get_plan_overview',
    {
        title: 'Plan overview',
        description:
            'A compact snapshot of the plan: open plan items (intentions with status and target date), ' +
            'recent study and fitness sessions (reality), certifications with exam dates, and active applications. ' +
            'Call this first before proposing anything.',
        inputSchema: { days: z.number().int().min(1).max(90).default(14).describe('How many past days of sessions and events to include') },
    },
    async ({ days }) => {
        const since = new Date(Date.now() - days * 86_400_000).toISOString().slice(0, 10)
        const [items, study, fitness, certs, apps, events] = await Promise.all([
            api<PlanItem[]>('/plan-items'),
            api<{ sessionDate: string }[]>('/study-sessions'),
            api<{ sessionDate: string }[]>('/fitness-sessions'),
            api<{ status: string }[]>('/certifications'),
            api<{ status: string }[]>('/applications'),
            api<{ eventTime: string }[]>('/plan-events'),
        ])
        const strip = <T extends object>(rows: T[]) => rows.map((r) => { const c = { ...r } as Record<string, unknown>; delete c.createdAt; delete c.updatedAt; return c })
        return text({
            today: new Date().toISOString().slice(0, 10),
            openPlanItems: strip(items.filter((p) => p.status === 'PLANNED' || p.status === 'IN_PROGRESS')),
            closedPlanItemsCount: items.length - items.filter((p) => p.status === 'PLANNED' || p.status === 'IN_PROGRESS').length,
            studySessions: strip(study.filter((s) => s.sessionDate >= since)),
            fitnessSessions: strip(fitness.filter((s) => s.sessionDate >= since)),
            planEvents: strip(events.filter((e) => e.eventTime >= since)),
            certifications: strip(certs),
            applications: strip(apps.filter((a) => !['REJECTED', 'WITHDRAWN', 'GHOSTED'].includes(a.status))),
        })
    },
)

server.registerTool(
    'list_plan_items',
    {
        title: 'List plan items',
        description: 'All plan items, optionally filtered by status. Ids from here are what proposals refer to.',
        inputSchema: { status: Status.optional() },
    },
    async ({ status }) => {
        const items = await api<PlanItem[]>('/plan-items')
        return text(status ? items.filter((p) => p.status === status) : items)
    },
)

server.registerTool(
    'propose_plan_changes',
    {
        title: 'Propose plan changes',
        description:
            'Submit a set of plan-item changes for the user to review in the app. Nothing is applied until they accept it. ' +
            'Use "update" with an existing id, or "create" with id null and at least title + intent. ' +
            'Never propose dates in the past; use targetDate "CLEAR" to unschedule.',
        inputSchema: {
            summary: z.string().max(2000).describe('One or two sentences on the overall recommendation'),
            changes: z.array(ChangeShape).min(1).max(50),
            source: z.string().max(200).default('mcp').describe('Where this comes from, e.g. "Claude Desktop"'),
        },
    },
    async ({ summary, changes, source }) => {
        const proposal = await api<{ id: number }>('/proposals', {
            method: 'POST',
            body: JSON.stringify({ source: `mcp:${source}`, summary, changes }),
        })
        return text(`Proposal #${proposal.id} submitted with ${changes.length} change(s). The user will see it in the app's Prompt page inbox and can apply or dismiss it.`)
    },
)

server.registerTool(
    'log_study_session',
    {
        title: 'Log a study session',
        description: 'Record study time that actually happened (reality, not intention). Optionally link it to a certification and the plan item it fulfilled.',
        inputSchema: {
            sessionDate: IsoDateTime.describe('Local start time, YYYY-MM-DDTHH:mm'),
            durationMinutes: z.number().int().positive(),
            certificationId: z.number().int().positive().optional(),
            planItemId: z.number().int().positive().optional(),
            notes: z.string().max(2000).optional(),
        },
    },
    async (input) => text(await api('/study-sessions', {
        method: 'POST',
        body: JSON.stringify({ ...input, certificationId: input.certificationId ?? null, planItemId: input.planItemId ?? null, notes: input.notes ?? null }),
    })),
)

server.registerTool(
    'log_fitness_session',
    {
        title: 'Log a workout',
        description: 'Record a workout that actually happened. Optionally link it to the plan item it fulfilled.',
        inputSchema: {
            sessionDate: IsoDateTime.describe('Local start time, YYYY-MM-DDTHH:mm'),
            durationMinutes: z.number().int().positive(),
            workoutType: z.enum(['WORKOUT_A', 'WORKOUT_B', 'WALK', 'OTHER']),
            planItemId: z.number().int().positive().optional(),
            notes: z.string().max(2000).optional(),
        },
    },
    async (input) => text(await api('/fitness-sessions', {
        method: 'POST',
        body: JSON.stringify({ ...input, planItemId: input.planItemId ?? null, notes: input.notes ?? null }),
    })),
)

server.registerTool(
    'list_pending_proposals',
    {
        title: 'List pending proposals',
        description: 'Proposals already waiting for the user, so you do not submit duplicates.',
        inputSchema: {},
    },
    async () => text(await api('/proposals?status=PENDING')),
)

const transport = new StdioServerTransport()
await server.connect(transport)
