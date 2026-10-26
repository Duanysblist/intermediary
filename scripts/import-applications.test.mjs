import { test } from 'node:test'
import assert from 'node:assert/strict'
import http from 'node:http'
import { parseCsv, run, toRequest } from './import-applications.mjs'

const HEADER = '#,Company,Role,Lane,ATS,Resume variant,Status,Submitted,Contact or referral,Follow up on,Outcome date,Note'
const csv = [
    HEADER,
    '1,Acme,"Engineer, Backend",Java backend,Greenhouse,A Java Backend,Submitted,2026-09-30,,2026-10-14,,',
    '2,Globex,SRE,Platform and SRE,Ashby,C Platform and Linux SRE,Rejected,2026-09-30,,,2026-10-01,"automated, no reason given"',
    '3,Initech,Dev,Full stack,Ashby,D Full Stack,Skipped: form bans AI,,,,,',
    '',
].join('\r\n')

test('parseCsv handles quoted commas, doubled quotes, embedded newlines and CRLF', () => {
    assert.deepEqual(parseCsv('a,"b,c","d ""e"""\r\n\r\nx,"y\nz",\r\n'), [['a', 'b,c', 'd "e"'], ['x', 'y\nz', '']])
})

const asRows = (text) => {
    const [header, ...body] = parseCsv(text)
    return body.map((f) => Object.fromEntries(header.map((h, i) => [h, f[i] ?? ''])))
}

test('toRequest imports submitted and rejected rows and skips the rest', () => {
    const [acme, globex, initech] = asRows(csv).map(toRequest)
    assert.equal(acme.request.role, 'Engineer, Backend')
    assert.equal(acme.request.status, 'APPLIED')
    assert.equal(acme.request.nextActionDate, null, 'no contact, so the sheet follow-up date is not a task')
    assert.equal(globex.request.status, 'REJECTED')
    assert.match(globex.request.notes, /Outcome 2026-10-01/)
    assert.match(initech.skip, /not an application/)
})

test('a named contact makes the row a referral with its follow-up date', () => {
    const [row] = asRows(`${HEADER}\n1,Acme,Dev,L,Gh,A,Submitted,2026-09-30,Pat Lee,2026-10-14,,`)
    const { request } = toRequest(row)
    assert.equal(request.source, 'REFERRAL')
    assert.equal(request.contactName, 'Pat Lee')
    assert.equal(request.nextActionDate, '2026-10-14')
})

async function withServer(fn) {
    const posted = []
    const stored = []
    const server = http.createServer((req, res) => {
        let body = ''
        req.on('data', (c) => (body += c))
        req.on('end', () => {
            res.setHeader('Content-Type', 'application/json')
            if (req.url === '/auth/login') return res.end(JSON.stringify({ token: 'tok' }))
            if (req.headers.authorization !== 'Bearer tok') { res.statusCode = 401; return res.end('{}') }
            if (req.url === '/applications' && req.method === 'GET') return res.end(JSON.stringify(stored))
            if (req.url === '/applications' && req.method === 'POST') {
                const a = JSON.parse(body)
                posted.push(a)
                stored.push(a)
                res.statusCode = 201
                return res.end(JSON.stringify(a))
            }
            res.statusCode = 404
            res.end('{}')
        })
    })
    await new Promise((resolve) => server.listen(0, resolve))
    try {
        await fn(`http://localhost:${server.address().port}`, posted)
    } finally {
        server.close()
    }
}

test('a dry run sends nothing; --apply imports once and a re-run imports nothing', async () => {
    await withServer(async (api, posted) => {
        const common = { csvText: csv, api, username: 'u', password: 'p', log: () => {} }
        assert.deepEqual(await run({ ...common, apply: false }), { imported: 0, existing: 0, failed: 0, skipped: 1 })
        assert.equal(posted.length, 0)
        assert.deepEqual(await run({ ...common, apply: true }), { imported: 2, existing: 0, failed: 0, skipped: 1 })
        assert.equal(posted.length, 2)
        assert.deepEqual(await run({ ...common, apply: true }), { imported: 0, existing: 2, failed: 0, skipped: 1 })
        assert.equal(posted.length, 2)
    })
})
