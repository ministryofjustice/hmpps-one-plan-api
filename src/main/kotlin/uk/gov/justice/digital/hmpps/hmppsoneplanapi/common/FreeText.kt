package uk.gov.justice.digital.hmpps.hmppsoneplanapi.common

import org.jsoup.Jsoup
import org.jsoup.safety.Safelist

private val safeList: Safelist by lazy { Safelist.relaxed() }

fun String.sanitise(): String = Jsoup.clean(this, safeList)
