package sample.kotlin

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class KotlinClassTest {
	private lateinit var subject: KotlinClass

	@BeforeEach
	fun setUp() {
		subject = KotlinClass()
	}

	@Test
	fun setter() {
	}
}
