package com.github.davidfantasy.fastrule;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 默认的规则管理器，支持规则按优先级遍历，规则的维护都是线程安全的
 */
public class DefaultRuleManager implements RuleManager {

    private final TreeSet<Rule> rules = new TreeSet<>();

    private final Map<String, Rule> ruleMap = new HashMap<>();

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final Lock readLock = readWriteLock.readLock();

    private final Lock writeLock = readWriteLock.writeLock();

    @Override
    public void add(Rule rule) {
        writeLock.lock();
        try {
            if (ruleMap.containsKey(rule.getId())) {
                throw new IllegalArgumentException("rule id already exists:" + rule.getId());
            }
            ruleMap.put(rule.getId(), rule);
            rules.add(rule);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Rule remove(String ruleId) {
        writeLock.lock();
        try {
            if (!ruleMap.containsKey(ruleId)) {
                return null;
            }
            rules.removeIf(r -> r.getId().equals(ruleId));
            return ruleMap.remove(ruleId);
        } finally {
            writeLock.unlock();
        }
    }


    @Override
    public Rule get(String ruleId) {
        readLock.lock();
        Rule rule = ruleMap.get(ruleId);
        readLock.unlock();
        return rule;
    }

    @Override
    public void forEach(RuleConsumer consumer) {
        readLock.lock();
        try {
            for (Rule rule : rules) {
                if (!consumer.accept(rule)) {
                    break;
                }
            }
        } finally {
            readLock.unlock();
        }
    }


    @Override
    public void clear() {
        writeLock.lock();
        try {
            rules.clear();
            ruleMap.clear();
        } finally {
            writeLock.unlock();
        }
    }

}
