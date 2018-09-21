package com.todo.autocollect;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

/**
 * Created by TCG on 2018/7/24.
 */

final class BrewJava {

    private static Filer filer;

    static void init(ProcessingEnvironment env) {
        filer = env.getFiler();
    }

    private static Class<?> superclass;

    static {
        try {
            superclass = Class.forName(BrewJava.class.getPackage().getName() + ".Collector");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    static class Builder {
        Builder() {
        }

        private String pkg;
        private TypeName annCls;
        private String clsName;
        private List<TargetModel> register;
        private List<TargetModel> target;

        Builder cls(TypeName annCls) {
            this.annCls = annCls;
            return this;
        }

        Builder clsName(String clsName) {
            this.clsName = clsName;
            return this;
        }

        Builder pkg(String pkg) {
            this.pkg = pkg;
            return this;
        }

        Builder register(List<TargetModel> models) {
            if (models == null || models.isEmpty()) {
                return this;
            }
            checkData();
            this.register.addAll(models);
            return this;
        }

        Builder target(Collection<TargetModel> models) {
            if (models == null || models.isEmpty()) {
                return this;
            }
            checkData();
            this.target.addAll(models);
            return this;
        }


        void create() {
            if (superclass == null) {
                return;
            }

            MethodSpec doFinal = MethodSpec.methodBuilder("doFinal")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PROTECTED)
                    .returns(TypeName.VOID)
                    .addStatement("super.doFinal()")
                    .addStatement("target = null")
                    .build();

            MethodSpec.Builder collectData = MethodSpec.methodBuilder("collectData")
                    .addModifiers(Modifier.PRIVATE)
                    .returns(TypeName.VOID);

            MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(annCls, "target")
                    .addStatement("this.target = target")
                    .addCode("\n")
                    .addStatement("collectData()");


            if (target != null){
                for (TargetModel m : target) {
                    constructor.addCode(m.block());
                    constructor.addCode("\n");
                }
            }

            if (register != null){
                for (TargetModel m : register) {
                    collectData.addCode(m.block());
                    collectData.addCode("\n");
                }
            }

            constructor.addStatement("doFinal()");

            TypeSpec collector = TypeSpec.classBuilder(clsName + "_Collector")
                    .addModifiers(Modifier.FINAL,Modifier.PUBLIC)
                    .superclass(ParameterizedTypeName.get(superclass))
                    .addField(annCls, "target", Modifier.PRIVATE)
                    .addMethod(constructor.build())
                    .addMethod(collectData.build())
                    .addMethod(doFinal)
                    .build();
            try {
                JavaFile javaFile = JavaFile.builder(pkg, collector)
                        .addFileComment("Generated code from Auto Collector. Do not modify!")
                        .build();
                javaFile.writeTo(filer);
            } catch (IOException e) {
            }
        }

        private synchronized void checkData() {
            if (register == null) {
                register = new ArrayList<>(16);
            }
            if (target == null){
                target = new ArrayList<>(16);
            }
        }
    }

    static Builder builder() {
        return new Builder();
    }
}
