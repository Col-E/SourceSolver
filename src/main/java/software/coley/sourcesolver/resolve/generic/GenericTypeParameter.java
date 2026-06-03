package software.coley.sourcesolver.resolve.generic;

import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.resolve.entry.DescribableEntry;

/**
 * Metadata for a generic type parameter declaration.
 *
 * @param ownerId
 * 		Unique identifier for the declaring class, method, or constructor.
 * @param name
 * 		Parameter name.
 * @param upperBound
 * 		Erased upper bound of the parameter.
 * @author Matt Coley
 */
public record GenericTypeParameter(@Nonnull String ownerId, @Nonnull String name,
                                   @Nonnull DescribableEntry upperBound) {}
