package com.todo.autocollect;

import com.squareup.javapoet.CodeBlock;

/**
 * Created by TCG on 2018/9/11.
 */

class MapFieldModel extends NonArrayFieldModel {

    public MapFieldModel(String fieldName, String targetCls) {
        super(fieldName, targetCls);
    }

    @Override
    public CodeBlock block() {
        CodeBlock.Builder builder = CodeBlock.builder();
        builder.addStatement("target.$N = findByField($S)", fieldName, fieldName);
        return builder.build();
    }
}
