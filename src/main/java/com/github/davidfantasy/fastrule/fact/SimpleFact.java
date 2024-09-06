package com.github.davidfantasy.fastrule.fact;

import lombok.Getter;
import lombok.ToString;

import java.util.*;

/**
 * 表示触发规则的业务事实对象，
 * 对仅单个事实数据的情况进行了特殊优化，避免创建大量的内部对象
 */
@ToString
public class SimpleFact implements Fact {

    @Getter
    private final String id;

    private Map<String, Object> delegateMap;

    private String name;

    private Object v;

    @Getter
    private final Long ts;


    public SimpleFact(String id, String name, Object value, Long ts) {
        Objects.requireNonNull(id, "id must not be null");
        this.id = id;
        this.name = name;
        this.v = value;
        this.ts = ts;
    }

    public void addValue(String name, Object value) {
        if (delegateMap != null) {
            delegateMap.put(name, value);
        } else {
            delegateMap = new LinkedHashMap<>();
            delegateMap.put(this.name, this.v);
            delegateMap.put(name, value);
            this.name = null;
            this.v = null;
        }
    }
    
    public Object getFirstValue() {
        return delegateMap != null ? delegateMap.values().iterator().next() : this.v;
    }

    @Override
    public Object getValue(String name) {
        if (delegateMap != null) {
            return delegateMap.get(name);
        } else {
            return name.equals(this.name) ? this.v : null;
        }
    }

    @Override
    public Collection<Object> getValues() {
        if (delegateMap != null) {
            return delegateMap.values();
        }
        return Collections.singletonList(this.v);
    }

}
