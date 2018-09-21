package com.todo.autocollect;

import com.squareup.javapoet.CodeBlock;

/**
 * Created by TCG on 2018/9/11.
 */

class NonArrayFieldModel extends EmptyModel {

    String fieldName;
    String targetCls;

    public NonArrayFieldModel(String fieldName, String targetCls) {
        this.fieldName = fieldName;
        this.targetCls = targetCls;
    }

    @Override
    public CodeBlock block() {
        CodeBlock.Builder builder = CodeBlock.builder();

        builder.addStatement("target.$N = findByField($S,$N.class)", fieldName, fieldName, targetCls);

        return builder.build();
    }
}
