// One-time import of the Application Log sheet (exported as CSV) into the intermediary API.
// Dry run unless --apply is given; safe to re-run, because rows already present are skipped.
//
//   INTERMEDIARY_API_URL=https://... INTERMEDIARY_USERNAME=... INTERMEDIARY_PASSWORD=... \
//     node scripts/import-applications.mjs application-log.csv [--apply]
import { readFileSync } from 'node:fs'
import { pathToFileURL } from 'node:url'

/** Minimal RFC 4180 parser: quoted fields, doubled quotes, CRLF, blank lines skipped. */
export function parseCsv(text) {
    const rows = []
    let row = []
    let field = ''
    let quoted = false
    const endRow = () => {
        row.push(field)
        field = ''
        if (row.some((f) => f !== '')) rows.push(row)
        row = []
    }
    for (let i = 0; i < text.length; i++) {
        const c = text[i]
        if (quoted) {
            if (c === '"' && text[i + 1] === '"') { field += '"'; i++ }
            else if (c === '"') quoted = false
            else field += c
        } else if (c === '"') quoted = true
        else if (c === ',') { row.push(field); field = '' }
        else if (c === '\n' || c === '\r') {
            if (c === '\r' && text[i + 1] === '\n') i++
            endRow()
        } else field += c
    }
    if (field !== '' || row.length) endRow()
    return rows
}

const STATUS = { Submitted: 'APPLIED', Rejected: 'REJECTED' }

/** One sheet row to an ApplicationRequest, or a reason it is not an application. */
export function toRequest(r) {
    const status = STATUS[r.Status]
    if (!status) return { skip: `status "${r.Status}" is not an application` }
    if (!/^\d{4}-\d{2}-\d{2}$/.test(r.Submitted)) return { skip: 'no submitted date' }
    const contact = r['Contact or referral']
    const notes = [`Lane: ${r.Lane}`, `ATS: ${r.ATS}`, r.Note, r['Outcome date'] && `Outcome ${r['Outcome date']}`]
        .filter(Boolean).join('. ').slice(0, 2000)
    return {
        request: {
            company: r.Company,
            role: r.Role,
            applicationDate: r.Submitted,
            status,
            source: contact ? 'REFERRAL' : 'COLD',
            resumeFile: r['Resume variant'],
            location: null,
            requisitionId: null,
            jobUrl: null,
            salaryRangeMin: null,
            salaryRangeMax: null,
            notes,
            contactName: contact || null,
            // The sheet's follow-up date is a prompt to look for a contact, not a task; only a named contact makes it one.
            nextActionDate: status === 'APPLIED' && contact && r['Follow up on'] ? r['Follow up on'] : null,
            interviewAt: null,
        },
    }
}

const key = (a) => `${a.company}|${a.role}|${a.applicationDate}`.toLowerCase()

export async function run({ csvText, api, username, password, apply, log = console.log }) {
    const [header, ...body] = parseCsv(csvText)
    const rows = body.map((f) => Object.fromEntries(header.map((h, i) => [h.trim(), (f[i] ?? '').trim()])))
    const plans = rows.map(toRequest)
    const ready = plans.filter((p) => p.request).map((p) => p.request)
    const skipped = plans.filter((p) => p.skip)
    log(`${rows.length} rows: ${ready.length} to import, ${skipped.length} skipped.`)
    for (const reason of [...new Set(skipped.map((p) => p.skip))]) {
        log(`  skipped ${skipped.filter((p) => p.skip === reason).length}: ${reason}`)
    }
    if (!apply) {
        log('Dry run. Nothing was sent. Add --apply to import.')
        return { imported: 0, existing: 0, failed: 0, skipped: skipped.length }
    }

    const base = api.replace(/\/$/, '')
    const call = async (path, init = {}, token) => {
        const res = await fetch(`${base}${path}`, {
            ...init,
            headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
        })
        if (!res.ok) throw new Error(`${init.method ?? 'GET'} ${path} failed: ${res.status}`)
        return res.json()
    }
    const { token } = await call('/auth/login', { method: 'POST', body: JSON.stringify({ username, password }) })
    const have = new Set((await call('/applications', {}, token)).map(key))
    let imported = 0
    let existing = 0
    let failed = 0
    for (const request of ready) {
        if (have.has(key(request))) { existing++; continue }
        try {
            await call('/applications', { method: 'POST', body: JSON.stringify(request) }, token)
            imported++
        } catch (err) {
            failed++
            log(`  failed: ${request.company}, ${request.role}: ${err.message}`)
        }
    }
    log(`Imported ${imported}, already present ${existing}, failed ${failed}.`)
    return { imported, existing, failed, skipped: skipped.length }
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
    const [csvPath, ...flags] = process.argv.slice(2)
    const need = (name) => process.env[name] || (() => { throw new Error(`Set ${name}.`) })()
    try {
        if (!csvPath) throw new Error('Usage: node scripts/import-applications.mjs application-log.csv [--apply]')
        const apply = flags.includes('--apply')
        await run({
            csvText: readFileSync(csvPath, 'utf8'),
            api: apply ? need('INTERMEDIARY_API_URL') : process.env.INTERMEDIARY_API_URL ?? '',
            username: apply ? need('INTERMEDIARY_USERNAME') : '',
            password: apply ? need('INTERMEDIARY_PASSWORD') : '',
            apply,
        })
    } catch (err) {
        console.error(err.message)
        process.exit(1)
    }
}
