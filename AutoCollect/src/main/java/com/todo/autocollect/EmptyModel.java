package com.todo.autocollect;

import com.squareup.javapoet.CodeBlock;

/**
 * Created by TCG on 2018/9/11.
 */

class EmptyModel implements TargetModel {
    @Override
    public TargetModel generateFieldModel() {
        return null;
    }

    @Override
    public CodeBlock block() {
        return null;
    }
}
