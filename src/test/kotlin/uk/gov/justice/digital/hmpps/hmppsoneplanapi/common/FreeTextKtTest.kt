package uk.gov.justice.digital.hmpps.hmppsoneplanapi.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FreeTextKtTest {
  @Test
  fun `Removes a script tag`() {
    assertThat("Some harmless <script>bad</script>text".sanitise())
      .isEqualTo("Some harmless text")
  }

  @Test
  fun `allows common markup tags`() {
    val textWithTags = "<li>An item in a list</li>"
    assertThat(textWithTags.sanitise()).isEqualTo(textWithTags)
  }
}
