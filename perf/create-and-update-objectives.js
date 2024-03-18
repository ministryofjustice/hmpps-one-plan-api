// noinspection JSUnusedGlobalSymbols

import http from 'k6/http'
import {getBaseUrl, getToken} from "./setup.js"
import {check} from 'k6'

export const options = {
    vus: 100,
    duration: '30s',
};

export function setup() {
    const token = getToken()
    const baseUrl = getBaseUrl()

    return {
        baseUrl: baseUrl,
        authHeaders: {
            headers: {
                Authorization: `Bearer ${token}`,
                'content-type': 'application/json',
            }
        },
    }
}

export default function (data) {
    const {baseUrl, authHeaders} = data

    const createRes = http.post(http.url`${baseUrl}/person/perf-2/objectives`, JSON.stringify({
        "title": "title",
        "targetCompletionDate": "2024-02-01",
        "status": "IN_PROGRESS",
        "note": "note",
        "outcome": "outcome"
    }), authHeaders)

    check(createRes, {
        'status': (r) => r.status === 200,
        'reference': (r) => r.json().reference
    })
    const ref = createRes.json().reference

    const updateRes = http.put(http.url`${baseUrl}/person/perf-2/objectives/${ref}`, JSON.stringify({
        "title": "title1",
        "targetCompletionDate": "2024-02-02",
        "status": "NOT_STARTED",
        "note": "note2",
        "outcome": "outcome2",
        "reasonForChange": "Just testing"
    }), authHeaders)


    check(updateRes, {
        'status': (r) => r.status === 200,
        'note': (r) => r.json().note === 'note2'
    })

    const deleteRes = http.del(http.url`${baseUrl}/person/perf-2/objectives/${ref}`, null, authHeaders)

    check(deleteRes, {
        'status': (r) => r.status === 204
    })


}
