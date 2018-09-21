package com.todo.autocollect;

import com.google.auto.service.AutoService;
import com.google.common.base.Optional;
import com.squareup.javapoet.TypeName;
import com.todo.autocollect.annotation.CollectionRegister;
import com.todo.autocollect.annotation.Collector;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import static com.google.auto.common.MoreElements.asType;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

@AutoService(Processor.class)
public final class CollectProcessor extends AbstractProcessor {

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        BrewJava.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<TypeElement, CollectModel> targetModelMap = processImpl(annotations, roundEnv);

        for (Map.Entry<TypeElement, CollectModel> model : targetModelMap.entrySet()) {
            TypeElement typeElement = model.getKey();
            TypeMirror mirror = typeElement.asType();
            String pkgName = getTypeElementPkgName(typeElement);
            String className = typeElement.getSimpleName().toString();

            CollectModel collect = model.getValue();

            BrewJava.builder().cls(TypeName.get(mirror)).clsName(className)
                    .pkg(pkgName)
                    .target(collect.field.values())
                    .register(collect.val).create();
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"Auto collect finished.");
        return true;
    }

    private Map<TypeElement, CollectModel> processImpl(Set<? extends TypeElement> annotations,
                                                       RoundEnvironment roundEnv) {
        Map<TypeElement, CollectModel> collected = new LinkedHashMap<>();
        Map<String, List<TargetModel>> register = new LinkedHashMap<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(CollectionRegister.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                try {
                    collectProviders(element, roundEnv, register);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        //....
        //do this finally
        for (Element element : roundEnv.getElementsAnnotatedWith(Collector.class)) {
            try {
                parseCollector(element, roundEnv, collected, register);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return collected;
    }

    private static class CollectModel extends EmptyModel {
        Map<String,TargetModel> field;
        List<TargetModel> val;

        CollectModel() {
        }

        void addField(String target, TargetModel m) {
            if (field == null) {
                field = new HashMap<>(16);
            }
            if (field.get(target) == null) {
                field.put(target, m);
            }
        }

        void addVal(TargetModel m) {
            if (val == null) {
                val = new ArrayList<>(16);
            }
            val.add(m);
        }
    }

    private static class CommonModel extends EmptyModel {
        String key;
        String target;
        String srcCls;
    }

    private void collectProviders(Element element, RoundEnvironment roundEnv, Map<String, List<TargetModel>> register) {
        TypeElement e = (TypeElement) element;
        CollectionRegister render = element.getAnnotation(CollectionRegister.class);
        CommonModel model = new CommonModel();
        model.key = render.type();
        try {
            model.target = render.target().getName();
        } catch (MirroredTypeException e1) {
            TypeMirror classTypeMirror = e1.getTypeMirror();
            TypeElement classTypeElement = (TypeElement) processingEnv.getTypeUtils().asElement(classTypeMirror);
            model.target = classTypeElement.getQualifiedName().toString();
        }
        model.srcCls = e.getQualifiedName().toString();
        String key = model.target;
        List<TargetModel> models = register.get(key);
        if (models == null) {
            models = new ArrayList<>(16);
            register.put(key, models);
        }
        models.add(model);
    }

    private void parseCollector(Element element, RoundEnvironment roundEnv
            , Map<TypeElement, CollectModel> collected, Map<String, List<TargetModel>> register) {
        TypeElement e = (TypeElement) element.getEnclosingElement();
        Collector collector = element.getAnnotation(Collector.class);
        String target;

        try {
            target = collector.value().getName();
        } catch (MirroredTypeException e1) {
            TypeMirror classTypeMirror = e1.getTypeMirror();
            TypeElement classTypeElement = (TypeElement) processingEnv.getTypeUtils().asElement(classTypeMirror);
            target = classTypeElement.getQualifiedName().toString();
        }
        List<TargetModel> registerList = register.get(target);

        if (registerList == null
                || isCollectInWrongPackage(Collector.class, element)
                || isInaccessibleViaGeneratedCode(Collector.class, target, element)) {
            return;
        }

        String fieldType = element.asType().toString();
        int i = fieldType.indexOf("<");
        boolean arrays = i > -1;
        if (registerList.size() > 1 && !arrays) {
            error(element, "Do not allow inject to an non-array field when data more than 1.");
            return;
        }
        if (!arrays) {
            CommonModel temp = (CommonModel) registerList.get(0);
            NonArrayTargetModel model = new NonArrayTargetModel(element.getSimpleName().toString(),
                    temp.target, temp.srcCls);
            addOrCreate(collected, e, temp, model);
            return;
        }
        ClassLoader loader = Collector.class.getClassLoader();
        String arrayStr = fieldType.substring(0, i);
        Class<?> array = null;

        try {
            array = loader.loadClass(arrayStr);
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        if (array == null) {
            error(element, "Not support type --> to array failed ", fieldType);
            return;
        }
        if (isCollection(array)) {
            for (TargetModel m : registerList) {
                CommonModel temp = (CommonModel) m;
                CollectionTargetModel collectionModel = new CollectionTargetModel(element.getSimpleName().toString(),
                        temp.target, temp.srcCls, array);
                addOrCreate(collected, e,temp,  collectionModel);
            }

        } else if (isMap(array)) {
            for (TargetModel m : registerList) {
                CommonModel temp = (CommonModel) m;
                MapTargetModel targetMapModel = new MapTargetModel(element.getSimpleName().toString(),
                        temp.target, temp.srcCls, temp.key);
                addOrCreate(collected, e, temp, targetMapModel);
            }
        } else {
            error(element, "Not support type ", fieldType);
        }
    }

    private void addOrCreate(Map<TypeElement, CollectModel> collected, TypeElement e,
                             CommonModel temp, TargetModel targetMapModel) {
        CollectModel model = collected.get(e);
        if (model == null) {
            model = new CollectModel();
            collected.put(e,model);
        }
        model.addField(temp.target, targetMapModel.generateFieldModel());
        model.addVal(targetMapModel);
    }

    private boolean isMap(Class<?> array) {
        return Map.class.isAssignableFrom(array);
    }

    private boolean isCollection(Class<?> array) {
        return Collection.class.isAssignableFrom(array);
    }

    private String getTypeElementPkgName(TypeElement e) {
        String qualify = e.getQualifiedName().toString();
        int i = qualify.lastIndexOf(".");
        if (i > 0) {
            qualify = qualify.substring(0, i);
        } else {
            qualify = "";
        }
        return qualify;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>(8);
        types.add(CollectionRegister.class.getName());
        types.add(Collector.class.getName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private boolean isInaccessibleViaGeneratedCode(Class<? extends Annotation> annotationClass,
                                                   String targetThing, Element element) {
        boolean hasError = false;
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        // Verify method modifiers.
        Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.contains(PRIVATE) || modifiers.contains(STATIC)) {
            error(element, "@%s %s must not be private or static. (%s.%s)",
                    annotationClass.getSimpleName(), targetThing, enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            hasError = true;
        }

        // Verify containing type.
        if (enclosingElement.getKind() != CLASS) {
            error(enclosingElement, "@%s %s may only be contained in classes. (%s.%s)",
                    annotationClass.getSimpleName(), targetThing, enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            hasError = true;
        }

        // Verify containing class visibility is not private.
        if (enclosingElement.getModifiers().contains(PRIVATE)) {
            error(enclosingElement, "@%s %s may not be contained in private classes. (%s.%s)",
                    annotationClass.getSimpleName(), targetThing, enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            hasError = true;
        }

        return hasError;
    }

    private boolean isCollectInWrongPackage(Class<? extends Annotation> annotationClass,
                                            Element element) {
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        String qualifiedName = enclosingElement.getQualifiedName().toString();

        if (qualifiedName.startsWith("android.")) {
            error(element, "@%s-annotated class incorrectly in Android framework package. (%s)",
                    annotationClass.getSimpleName(), qualifiedName);
            return true;
        }
        if (qualifiedName.startsWith("java.")) {
            error(element, "@%s-annotated class incorrectly in Java framework package. (%s)",
                    annotationClass.getSimpleName(), qualifiedName);
            return true;
        }

        return false;
    }

    private void error(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.ERROR, element, message, args);
    }

    private void note(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.NOTE, element, message, args);
    }

    private void printMessage(Diagnostic.Kind kind, Element element, String message, Object[] args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }

        processingEnv.getMessager().printMessage(kind, message, element);
    }

    public static Optional<AnnotationMirror> getAnnotationMirror(Element element,
                                                                 Class<? extends Annotation> annotationClass) {
        String annotationClassName = annotationClass.getCanonicalName();
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            TypeElement annotationTypeElement = asType(annotationMirror.getAnnotationType().asElement());
            if (annotationTypeElement.getQualifiedName().contentEquals(annotationClassName)) {
                return Optional.of(annotationMirror);
            }
        }
        return Optional.absent();
    }

    private DeclaredType getProviderInterface(AnnotationMirror providerAnnotation) {

        // The very simplest of way of doing this, is also unfortunately unworkable.
        // We'd like to do:
        //    ServiceProvider provider = e.getAnnotation(ServiceProvider.class);
        //    Class<?> providerInterface = provider.value();
        //
        // but unfortunately we can't load the arbitrary class at annotation
        // processing time. So, instead, we have to use the mirror to get at the
        // value (much more painful).

        Map<? extends ExecutableElement, ? extends AnnotationValue> valueIndex =
                providerAnnotation.getElementValues();

        AnnotationValue value = valueIndex.values().iterator().next();
        return (DeclaredType) value.getValue();
    }

    private String getBinaryName(TypeElement element) {
        return getBinaryNameImpl(element, element.getSimpleName().toString());
    }

    private String getBinaryNameImpl(TypeElement element, String className) {
        Element enclosingElement = element.getEnclosingElement();

        if (enclosingElement instanceof PackageElement) {
            PackageElement pkg = (PackageElement) enclosingElement;
            if (pkg.isUnnamed()) {
                return className;
            }
            return pkg.getQualifiedName() + "." + className;
        }

        TypeElement typeElement = (TypeElement) enclosingElement;
        return getBinaryNameImpl(typeElement, typeElement.getSimpleName() + "$" + className);
    }
}
