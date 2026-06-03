package software.coley.sourcesolver.model;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility for querying variables visible in the local scope of a source position.
 *
 * @author Matt Coley
 */
public class ScopeLookup {
	private ScopeLookup() {}

	/**
	 * Collect variables visible at the given position.
	 *
	 * @param unit
	 * 		The compilation unit to search.
	 * @param position
	 * 		The position within the AST to find variables for.
	 *
	 * @return Variables visible at the given position, including method parameters and local variables.
	 * Parameters are listed first, then locals from inner scopes to outer scopes, with shadowed names removed.
	 */
	@Nonnull
	public static List<VariableModel> collectVisibleVariables(@Nonnull CompilationUnitModel unit, int position) {
		// Get the containing method at this position.
		Model leaf = unit.getDeepestNonErroneousChildAtPosition(position);
		MethodModel containingMethod = leaf instanceof MethodModel method ? method : leaf.getParentOfType(MethodModel.class);
		if (containingMethod == null)
			return List.of();

		// Collect parameters first since they are always visible in the method scope, even if declared after the position.
		Map<String, VariableModel> variables = new LinkedHashMap<>(); // Ordered to preserve parameter declaration order.
		for (VariableModel parameter : containingMethod.getParameters())
			if (parameter.getRange().begin() <= position)
				variables.putIfAbsent(parameter.getName(), parameter);

		// Check local variables in the scope, but skip parameters since they were already checked.
		Set<VariableModel> parameters = new HashSet<>(containingMethod.getParameters());
		Model scope = leaf;
		while (scope != null && scope != containingMethod.getParent()) {
			for (VariableModel variable : scope.getRecursiveChildrenOfType(VariableModel.class)) {
				if (parameters.contains(variable))
					continue;
				if (variable.getRange().end() <= position)
					variables.putIfAbsent(variable.getName(), variable);
			}
			scope = scope.getParent();
		}

		// Yield all variables in declaration order.
		return new ArrayList<>(variables.values());
	}

	/**
	 * Find a variable with the given name that is visible at the given position.
	 *
	 * @param unit
	 * 		The compilation unit to search.
	 * @param position
	 * 		The position within the AST to find the variable for.
	 * @param name
	 * 		The name of the variable to find.
	 *
	 * @return The visible variable with the given name, or {@code null} if no such variable exists.
	 * If multiple variables with the same name are visible, the nearest declaration is returned.
	 */
	@Nullable
	public static VariableModel findVisibleVariable(@Nonnull CompilationUnitModel unit, int position, @Nonnull String name) {
		// Get the containing method at this position.
		Model leaf = unit.getDeepestNonErroneousChildAtPosition(position);
		MethodModel containingMethod = leaf instanceof MethodModel method ? method : leaf.getParentOfType(MethodModel.class);
		if (containingMethod == null)
			return null;

		// Check parameters first since they are always visible in the method scope, even if declared after the position.
		for (VariableModel parameter : containingMethod.getParameters())
			if (parameter.getRange().begin() <= position && parameter.getName().equals(name))
				return parameter;

		// Check local variables in the scope, but skip parameters since they were already checked.
		Set<VariableModel> parameters = new HashSet<>(containingMethod.getParameters());
		Model scope = leaf;
		while (scope != null && scope != containingMethod.getParent()) {
			for (VariableModel variable : scope.getRecursiveChildrenOfType(VariableModel.class)) {
				if (parameters.contains(variable))
					continue;
				if (variable.getRange().end() <= position && variable.getName().equals(name))
					return variable;
			}
			scope = scope.getParent();
		}

		// No matching variable.
		return null;
	}
}
