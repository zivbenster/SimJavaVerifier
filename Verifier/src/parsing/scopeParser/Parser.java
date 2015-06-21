/**
 * Created by rooty on 6/11/15.
 */
package parsing.scopeParser;

import dataStructures.scope.Method;
import dataStructures.scope.Scope;
import dataStructures.vars.VariableObject;
import parsing.RegexDepot;
import parsing.exceptions.DuplicateAssignmentException;
import parsing.exceptions.invalidMethodException;

import java.util.ArrayList;
import java.util.regex.Matcher;

public class Parser {
	private static final String ARG_DELIM = ",";

	// recursive call root --> leaves, iterate throughout tree of scopes
	public static void startParsing(Scope scope) throws DuplicateAssignmentException, invalidMethodException {
		ArrayList<String> fileLines = scope.getSrc();

		// parse all methods - store them in memory to be ready for method calls during procedural reading.
		for (Method temp : scope.getAllMethods()){
			methodArgParse(temp);
			startParsing(temp);
		}
		for (String line : fileLines){
			// read line

			// cases, var declare, var assign, method declare/if while scope,
			Matcher variableDeclarationMatch = RegexDepot.VARIABLE_DECLARATION_PATTERN.matcher(line);
			Matcher variableAssignMatch = RegexDepot.VARIABLE_ASSIGNEMENT_PATTERN.matcher(line);
			Matcher methodScopeMatch = RegexDepot.METHOD_PATTERN.matcher(line);
			Matcher conditionScopeMatch = RegexDepot.CONDITION_PATTERN.matcher(line);
			Matcher methodCallMatch = RegexDepot.METHOD_CALL_PATTERN.matcher(line);

			if (variableDeclarationMatch.matches()){
				variableDeclareLine(scope, line);
			} else if (variableAssignMatch.matches()){
				variableAssignLine(scope, line); // TODO will this cover method call line?
			} else if (methodCallMatch.matches()) {
				methodCallChecker(scope, methodCallMatch);
			} else if (methodScopeMatch.matches()) { // should match the pattern left in line
				// skip, this is parsed at end.
				continue; // TODO maybe check that the method scope exists
			} else if (conditionScopeMatch.matches()) {
				startParsing(scope.getAllChildren().pollFirst()); // FIFO
			} else {
				// throw new parsing exception
			}

		} // finished running over all lines but the method lines
	}

	private static void methodArgParse(Method method) throws DuplicateAssignmentException {
		String[] arguments = method.getArgString().split(ARG_DELIM);
		for (String argument : arguments){
			variableDeclareLine(method,argument+";");
		}
	}

	private static void variableDeclareLine(Scope scope, String line) throws DuplicateAssignmentException {
		// get the type (make sure legal), for each sections between the comma's, check if assignment,
		// make varObject and try to add it.
		boolean isFinal = false;
		Matcher variableDeclarationMatch = RegexDepot.VARIABLE_DECLARATION_PATTERN.matcher(line);
		variableDeclarationMatch.find();
		String type = variableDeclarationMatch.group(2);
		if (variableDeclarationMatch.group(1)!=null){ // if grp 1 exists it's final, else null
			isFinal = true;
		}

		// split varNames and values by commas and keep without final&type
		String[] namesAndAssignment = variableDeclarationMatch.group(3).split(",");
		for (String var : namesAndAssignment){
			Matcher assignment = RegexDepot.VARIABLE_ASSIGNEMENT_PATTERN.matcher(var);
			Matcher varWithoutAssignment = RegexDepot.VARIABLE_PATTERN.matcher(var);
			if (assignment.matches()){
				String varName = assignment.group(1);
				String value = assignment.group(2);
				// TODO what if value is another var? handle this in variableObject/Store
				VariableObject varObject = new VariableObject(varName, type, value, isFinal);
				try {
					scope.addVar(varObject);
				} catch (DuplicateAssignmentException e) {
					// TODO this isnt necessarily a duplicate assignment
					throw new DuplicateAssignmentException(varObject);
				}
			}
			else if (varWithoutAssignment.matches()){
				if (isFinal){
					// throw new exception cannot declare final var w/o assignment
				}
				String name = varWithoutAssignment.group(0); // TODO no group == matched pattern?
				VariableObject varObject = new VariableObject(name, type);
				try {
					scope.addVar(varObject);
				} catch (DuplicateAssignmentException e) {
					throw new DuplicateAssignmentException(varObject);
				}
			} else {
				// throw new not valid declaration.
			}
		}
	}

	private static void variableAssignLine(Scope scope, String line) {
	//
	}

	private static void parseMethod(Scope scope) {
		// var names in the arguments may be a general name
		// dont forget to check for return; right before the }
	}

	private static void methodCallChecker(Scope scope, Matcher lineMatcher) throws invalidMethodException {
		String methodName = lineMatcher.group(1);
		String[] arguments = lineMatcher.group(2).split(ARG_DELIM);


		Scope methodMatched = null;

		while (scope!= null) {
			for (Scope temp : scope.getAllMethods()) {
				if (temp.toString().equals(methodName)) {
					methodMatched = temp;

					return;
				}
			}
			scope = scope.getParent();

		}

		if (methodMatched==null){
			throw new invalidMethodException(methodName);
		}

		String[] methodArguments = methodMatched.getConditions().split(ARG_DELIM);


	}


}
