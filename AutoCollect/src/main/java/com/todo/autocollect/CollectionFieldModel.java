package com.todo.autocollect;

import com.squareup.javapoet.CodeBlock;

/**
 * Created by TCG on 2018/9/11.
 */

class CollectionFieldModel extends NonArrayFieldModel {

    public CollectionFieldModel(String fieldName, String targetCls) {
        super(fieldName, targetCls);
    }

    @Override
    public CodeBlock block() {
        CodeBlock.Builder builder = CodeBlock.builder();
        builder.addStatement("target.$N = findByField($S,target.$N)", fieldName, fieldName, fieldName);
        return builder.build();
    }
}
