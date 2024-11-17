package example

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.testing.runMosaicTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class CounterTest {
	@Test
	fun counter() = runTest {
		runMosaicTest {
			setContent {
				Counter()
			}
			for (count in 0..20) {
				assertThat(awaitSnapshot()).isEqualTo("The count is: $count")
			}
		}
	}
}
