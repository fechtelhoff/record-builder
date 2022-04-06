package de.fechtelhoff;

import java.time.LocalDate;
import de.fechtelhoff.builder.annotation.Builder;

@Builder
public record Person(
	String firstName,
	String lastName,
	LocalDate birthDate,
	int heightInCentimeters) {

	public Person {
		if (heightInCentimeters <= 0) {
			throw new IllegalArgumentException("Component 'heightInCentimeters' must be > 0, was %s".formatted(heightInCentimeters));
		}
	}
}
