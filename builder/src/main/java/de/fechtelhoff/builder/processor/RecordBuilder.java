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
 *
 * This class is used to process the {@code de.fechtelhoff.builder.annotation.Builder} annotation.
 * It will create a builder class for every record on the classpath that is annotated with that annotation.
 */
@SupportedAnnotationTypes("de.fechtelhoff.builder.annotation.Builder")
public class RecordBuilder extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		try {
			for (TypeElement annotation : annotations) {
				for (Element o : roundEnv.getElementsAnnotatedWith(annotation)) {
					if (o.getKind().equals(ElementKind.RECORD)) {
						writeSourceFile(o);
					}
				}
			}
		} catch (IOException ex) {
			return false;
		}
		return true;
	}

	private void writeSourceFile(Element element) throws IOException {
		String fqcn = element.asType().toString();
		int lastDotIndex = fqcn.lastIndexOf('.');
		String packageName = fqcn.substring(0, lastDotIndex);

		String className = "%sBuilder".formatted(element.getSimpleName());

		JavaFileObject builderFile = processingEnv.getFiler().createSourceFile("%sBuilder".formatted(fqcn));

		List<Element> recordComponents = element.getEnclosedElements()
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
			renderFields(recordComponents, out);
			renderPrivateConstructor(className, out);
			renderBuilderInitializer(className, out);
			renderBuildMethod(element, recordComponents, out);
			renderBuilderMethods(className, recordComponents, out);

			out.print("}");
		}
	}

	private void renderDocumentationHeader(String recordName, PrintWriter out) {
		out.println("""
			/**
			 * This class is an implementation of the builder pattern for the record %s.
			 * It was automatically generated on %s in timezone %s.
			 */
			""".formatted(recordName, LocalDateTime.now(ZoneId.systemDefault())
				.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
			ZoneId.systemDefault().toString()));
	}

	private void renderPrivateConstructor(String className, PrintWriter out) {
		out.println("""
			    private %s() {
			        //Instances of this class should be created with the static method 'build()', not the constructor
			    }
			""".formatted(className));
	}

	private void renderBuilderMethods(String className, List<Element> recordComponents, PrintWriter out) {
		final String formatString = """
			    public %s %s(%s %s) {
			        this.%s = %s;
			        return this;
			    }
			""";
		recordComponents.forEach(
			component -> out.println(
				formatString.formatted(
					className,
					component.getSimpleName(),
					component.asType().toString(),
					component.getSimpleName(),
					component.getSimpleName(),
					component.getSimpleName()
				)
			)
		);
	}

	private void renderBuildMethod(Element element, List<Element> recordComponents, PrintWriter out) {
		out.println("    public %s build() {".formatted(element.getSimpleName()));
		out.print("        return new %s(".formatted(element.getSimpleName()));
		out.print(recordComponents.stream()
			.map(Element::getSimpleName)
			.collect(Collectors.joining(", ")));
		out.println(");");
		out.println("    }");
		out.println();
	}

	private void renderBuilderInitializer(String className, PrintWriter out) {
		out.println("""
			    public static %s builder() {
			        return new %s();
			    }
			""".formatted(className, className));
	}

	private void renderFields(List<Element> recordComponents, PrintWriter out) {
		recordComponents.forEach(
			element -> out.println("    private " + element.asType().toString() + " " + element.getSimpleName() + ";")
		);
		out.println();
	}
}
