import {check} from 'k6'
import http from 'k6/http'
export function getToken() {
    const authData = {
        redirectUri: null,
        username: `${__ENV.USERNAME}`,
        password: `${__ENV.PASSWORD}`
    }
    const authRes = http.post('https://sign-in-dev.hmpps.service.justice.gov.uk/auth/sign-in', authData, {redirects: 0})
    check(authRes, {
        'setCookie': (r) => {
            return r.headers['Set-Cookie']
        }
    })
    const cookie = authRes.headers['Set-Cookie']
    return cookie.split(';')[0].split('=')[1]
}

export function getBaseUrl() {
    return __ENV.BASE_URL || 'http://localhost:8080'
}
