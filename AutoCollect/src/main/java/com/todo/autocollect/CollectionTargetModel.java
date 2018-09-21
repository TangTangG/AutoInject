package com.todo.autocollect;

import com.squareup.javapoet.CodeBlock;

/**
 * Created by TCG on 2018/7/24.
 */

class CollectionTargetModel extends NonArrayTargetModel {

    private Class<?> collectionCls;

    public CollectionTargetModel(String fieldName, String targetCls, String srcCls, Class<?> collectionCls) {
        super(fieldName, targetCls, srcCls);
        this.collectionCls = collectionCls;
    }


    @Override
    public CodeBlock block() {
        CodeBlock.Builder builder = CodeBlock.builder();

        builder.add("addToCollection($T.class,$S,newFromCls($N.class", collectionCls, fieldName, srcCls);
        if (args != null && args.length > 0) {
            builder.add(",$T", args);
        }
        builder.addStatement("),$N.class)", targetCls);

        return builder.build();
    }

    @Override
    public TargetModel generateFieldModel() {
        return new CollectionFieldModel(fieldName,targetCls);
    }
}
