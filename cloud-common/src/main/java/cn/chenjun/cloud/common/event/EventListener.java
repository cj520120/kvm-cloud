package cn.chenjun.cloud.common.event;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class EventListener<T> {
    private final List<EventHandler<T>> listener = Collections.synchronizedList(new ArrayList<>());
    private final ReadWriteLock lock = new ReentrantReadWriteLock(false);

    public void fire(Object sender, T target) {
        try {
            this.lock.readLock().lock();
            Collection<EventHandler<T>> listener = this.listener;
            final EventObject<T> model = new EventObject<T>(target);
            for (EventHandler<T> ev : listener) {
                this.fire(ev, sender, model);
            }

        } finally {
            this.lock.readLock().unlock();
        }
    }

    protected void fire(EventHandler<T> ev, Object sender, EventObject<T> target) {
        try {
            ev.fire(sender, target);
        } catch (Exception err) {
            log.error("notify event fail.data={}", target, err);
        }
    }

    public void addEvent(EventHandler<T> event) {
        try {
            this.lock.writeLock().lock();
            this.listener.add(event);
        } finally {
            this.lock.writeLock().unlock();
        }

    }

    public void removeEvent(EventHandler<T> event) {
        try {
            this.lock.writeLock().lock();
            this.listener.remove(event);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

}
