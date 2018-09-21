package com.todo.autocollect;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

/**
 * Created by TCG on 2018/7/24.
 */

class NonArrayTargetModel implements TargetModel {

    String fieldName;
    TypeName type;
    String targetCls;
    String srcCls;
    Object[] args;

    public NonArrayTargetModel(String fieldName, String targetCls, String srcCls) {
        this.fieldName = fieldName;
        this.targetCls = targetCls;
        this.srcCls = srcCls;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    @Override
    public CodeBlock block() {
        CodeBlock.Builder builder = CodeBlock.builder();
        builder.add("addNoArray($S,newFromCls($N.class", fieldName, srcCls);
        if (args != null && args.length > 0) {
            builder.add(",$T", args);
        }
        builder.addStatement("),$N.class)", targetCls);
        return builder.build();
    }

    @Override
    public TargetModel generateFieldModel() {
        return new NonArrayFieldModel(fieldName,targetCls);
    }
}
