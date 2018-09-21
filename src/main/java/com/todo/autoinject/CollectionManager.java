package com.todo.autoinject;

import com.todo.autocollect.annotation.Collector;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by TCG on 2018/9/21.
 */

public class CollectionManager {

    @Collector(CollectionIface.class)
    Map<String,CollectionIface> collections = new HashMap<>(16);

}
