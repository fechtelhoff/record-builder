package de.fechtelhoff;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.time.LocalDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PersonTest {

	public static final String FIRST_NAME = "Donald";
	public static final String LAST_NAME = "Duck";
	private static final LocalDate BIRTHDAY = LocalDate.of(2000, 1, 1);
	private static final int HEIGHT = 75;

	@Test
	void Test_with_Constructor_Call() {
		final Person person = new Person(FIRST_NAME, LAST_NAME, BIRTHDAY, HEIGHT);
		checkPerson(person);
	}

	@Test
	void Test_with_generated_Builder() {
		final Person person = new PersonBuilder()
			.withFirstName(FIRST_NAME)
			.withLastName(LAST_NAME)
			.withBirthDate(BIRTHDAY)
			.withHeightInCentimeters(HEIGHT)
			.build();
		checkPerson(person);
	}

	private void checkPerson(final Person person) {
		assertNotNull(person);
		assertAll(
			() -> Assertions.assertEquals(FIRST_NAME, person.firstName()),
			() -> Assertions.assertEquals(LAST_NAME, person.lastName()),
			() -> Assertions.assertEquals(BIRTHDAY, person.birthDate()),
			() -> Assertions.assertEquals(HEIGHT, person.heightInCentimeters())
		);
	}
}
