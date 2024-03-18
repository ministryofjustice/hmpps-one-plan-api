// noinspection JSUnusedGlobalSymbols

import http from 'k6/http'
import {getBaseUrl, getToken} from "./setup.js"

export const options = {
    vus: 100,
    duration: '30s',
};

export function setup() {
    const token = getToken()

    const baseUrl = getBaseUrl()

    const objectiveRefs = []
    for (let i = 0; i < 5; i++) {
        const res = http.post(`${baseUrl}/person/perf-1/objectives`, JSON.stringify({
            "title": "title",
            "targetCompletionDate": "2024-02-01",
            "status": "IN_PROGRESS",
            "note": "note",
            "outcome": "outcome"
        }), {
            headers: {
                Authorization: `Bearer ${token}`,
                'content-type': 'application/json',
            }
        })
        objectiveRefs.push(res.json().reference)
    }

    return {
        baseUrl: baseUrl,
        authHeaders: {
            headers: {
                Authorization: `Bearer ${token}`
            }
        },
        objectiveRefs
    }
}

export default function (data) {
    const r = http.get(`${data.baseUrl}/person/perf-1/objectives`, data.authHeaders)
    if(r.status !== 200) {
        console.log(r.status, r.json())
    }
}

export function teardown(data) {
    data.objectiveRefs.forEach((ref) => {
        http.del(`${data.baseUrl}/person/perf-1/objectives/${ref}`, data.authHeaders)
    })
}
