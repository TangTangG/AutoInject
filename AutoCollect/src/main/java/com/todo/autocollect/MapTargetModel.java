package com.todo.autocollect;

import com.squareup.javapoet.CodeBlock;

/**
 * Created by TCG on 2018/7/24.
 */

class MapTargetModel extends NonArrayTargetModel {

    private Object key;

    public MapTargetModel(String fieldName, String targetCls, String srcCls, String key) {
        super(fieldName, targetCls, srcCls);
        this.key = key;
    }

    @Override
    public CodeBlock block() {
        CodeBlock.Builder builder = CodeBlock.builder();
        if (key == null){
            return builder.build();
        }
        builder.add("addToMap($S,$S,newFromCls($N.class", fieldName, key, srcCls);
        if (args != null && args.length > 0) {
            builder.add(",$T", args);
        }
        builder.addStatement("),$T.class,$N.class)", key.getClass(), targetCls);
        return builder.build();
    }

    @Override
    public TargetModel generateFieldModel() {
        return new MapFieldModel(fieldName,targetCls);
    }
}
