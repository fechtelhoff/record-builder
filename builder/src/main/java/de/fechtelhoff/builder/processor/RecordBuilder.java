package de.fechtelhoff.builder.processor;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

/**
 * Cloned from https://github.com/javahippie/jukebox (THX Tim!)
 * <p>
 * This class is used to process the {@code de.fechtelhoff.builder.annotation.Builder} annotation.
 * It will create a builder class for every record on the classpath that is annotated with that annotation.
 */
@SupportedAnnotationTypes("de.fechtelhoff.builder.annotation.Builder")
public class RecordBuilder extends AbstractProcessor {

	private static final int INDENTATION = 4;
	private static final int DOUBLE_INDENTATION = 2 * INDENTATION;

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
		try {
			for (TypeElement annotation : annotations) {
				for (Element element : roundEnvironment.getElementsAnnotatedWith(annotation)) {
					if (element.getKind().equals(ElementKind.RECORD)) {
						writeSourceFile(element);
					}
				}
			}
		} catch (IOException exception) {
			return false;
		}
		return true;
	}

	private void writeSourceFile(Element element) throws IOException {
		String fullQualifiedClassName = element.asType().toString();
		int lastDotIndex = fullQualifiedClassName.lastIndexOf('.');
		String packageName = fullQualifiedClassName.substring(0, lastDotIndex);
		String className = "%sBuilder".formatted(element.getSimpleName());

		JavaFileObject builderFile = processingEnv.getFiler().createSourceFile("%sBuilder".formatted(fullQualifiedClassName));

		List<Element> elements = element.getEnclosedElements()
			.stream()
			.filter(e -> e.getKind().equals(ElementKind.RECORD_COMPONENT))
			.map(Element.class::cast)
			.toList();

		try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
			out.println("package %s;".formatted(packageName));
			out.println();
			renderDocumentationHeader(element.toString(), out);
			out.println("public class %s {".formatted(className));
			out.println();
			renderFields(elements, out);
			renderBuildMethod(element, elements, out);
			renderBuilderMethods(className, elements, out);
			out.println("}");
		}
	}

	private void renderDocumentationHeader(String recordName, PrintWriter out) {
		out.print("""
			/**
			 * This class is an implementation of the builder pattern for the record %s.
			 * It was automatically generated on %s in timezone %s.
			 */
			""".formatted(recordName, LocalDateTime.now(ZoneId.systemDefault())
				.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
			ZoneId.systemDefault().toString()));
	}

	private void renderFields(List<Element> elements, PrintWriter out) {
		elements.forEach(element -> printField(element, out));
		out.println();
	}

	private void printField(Element element, PrintWriter out) {
		final String type = element.asType().toString();
		final String simpleName = element.getSimpleName().toString();
		out.print(("private " + type + " " + simpleName + ";").indent(INDENTATION));
	}

	private void renderBuildMethod(Element element, List<Element> elements, PrintWriter out) {
		out.print("public %s build() {".formatted(element.getSimpleName()).indent(INDENTATION));
		out.print(" ".repeat(DOUBLE_INDENTATION) +"return new %s(".formatted(element.getSimpleName()));
		out.print(elements.stream()
			.map(Element::getSimpleName)
			.collect(Collectors.joining(", ")));
		out.println(");");
		out.println("}".indent(INDENTATION));
	}

	private void renderBuilderMethods(String className, List<Element> elements, PrintWriter out) {
		elements.forEach(component -> printBuilderMethod(className, component, out));
	}

	private void printBuilderMethod(String className, Element element, PrintWriter out) {
		final String simpleName = element.getSimpleName().toString();
		final String methodName = "with" + capitalize(simpleName);
		final String type = element.asType().toString();
		out.print(("public " + className + " " + methodName + "(" + type + " " + simpleName + ") {").indent(INDENTATION));
		out.print(("this." + simpleName + " = " + simpleName + ";").indent(DOUBLE_INDENTATION));
		out.print(("return this;").indent(DOUBLE_INDENTATION));
		out.println(("}").indent(INDENTATION));
	}

	private String capitalize(final String input) {
		return input.substring(0, 1).toUpperCase() + input.substring(1);
	}
}
