package dev.thilanka.netrics.entity.common;

import dev.thilanka.netrics.repository.OperationLogRepository;
import dev.thilanka.netrics.util.SecurityUtils;
import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
public class OperationLogListener {

    //-- Capture old state from Hibernate before Flushing for updating
    private static final ThreadLocal<Map<Object, Map<String, Object>>> PRE_STATE =
            ThreadLocal.withInitial(IdentityHashMap::new);

    //-- Fetch the CURRENT committed state from DB using a separate session,
    private Map<String, Object> loadCommittedState(Object entity) {
        ApplicationContext ctx = SpringContextHolder.getContext();
        EntityManagerFactory emf = ctx.getBean(EntityManagerFactory.class);

        // Open a fresh session — completely isolated from the current transaction
        EntityManager freshEm = emf.createEntityManager();
        try {
            Object id = extractId(entity);
            Object committed = freshEm.find(entity.getClass(), parseId(entity, id));
            if (committed == null) return Collections.emptyMap();
            return extractFields(committed);
        } catch (Exception e) {
            log.error("OperationLogListener: failed to load committed state for {}",
                    entity.getClass().getSimpleName(), e);
            return Collections.emptyMap();
        } finally {
            freshEm.close(); // always close the fresh session
        }
    }

    // Parse the ID back to its original type (Long, Integer, etc.)
    private Object parseId(Object entity, Object stringId) {
        try {
            Class<?> clazz = entity.getClass();
            while (clazz != null && clazz != Object.class) {
                for (Field f : clazz.getDeclaredFields()) {
                    if (f.isAnnotationPresent(Id.class)) {
                        f.setAccessible(true);
                        Class<?> idType = f.getType();
                        if (idType == Long.class || idType == long.class)
                            return Long.parseLong(stringId.toString());
                        if (idType == Integer.class || idType == int.class)
                            return Integer.parseInt(stringId.toString());
                        return stringId; // String PK or other
                    }
                }
                clazz = clazz.getSuperclass();
            }
        } catch (Exception ignored) {}
        return stringId;
    }

    //-- Lifecycle Callbacks
    @PostPersist
    public void onPostPersist(Object entity) {
        if (!isAudited(entity)) return;
        try {
            Map<String, Map<String, Object>> changes = buildCreateChanges(entity);
            persist(entity, OperationLog.OperationType.CREATE, changes);
        } catch (Exception e) {
            log.error("OperationLogListener: failed to log CREATE for {}",
                    entity.getClass().getSimpleName(), e);
        }
    }

    @PreUpdate
    public void onPreUpdate(Object entity) {
        if (!isAudited(entity)) return;
        try {
            // Load the committed DB state — NOT the in-memory dirty state
            PRE_STATE.get().put(entity, loadCommittedState(entity));
        } catch (Exception e) {
            log.error("OperationLogListener: failed to snapshot for UPDATE on {}",
                    entity.getClass().getSimpleName(), e);
        }
    }

    @PostUpdate
    public void onPostUpdate(Object entity) {
        if (!isAudited(entity)) return;
        try {
            Map<String, Object> oldState = PRE_STATE.get().remove(entity);
            Map<String, Object> newState = extractFields(entity);
            Map<String, Map<String, Object>> changes = buildUpdateChanges(oldState, newState);

            if (changes.isEmpty()) return;
            persist(entity, OperationLog.OperationType.UPDATE, changes);
        } catch (Exception e) {
            log.error("OperationLogListener: failed to log UPDATE for {}",
                    entity.getClass().getSimpleName(), e);
        }
    }

    @PreRemove
    public void onPreRemove(Object entity) {
        if (!isAudited(entity)) return;
        try {
            // Snapshot before the row disappears.
            PRE_STATE.get().put(entity, extractFields(entity));
        } catch (Exception e) {
            log.error("OperationLogListener: failed to snapshot for DELETE on {}", entity.getClass().getSimpleName(), e);
        }
    }

    @PostRemove
    public void onPostRemove(Object entity) {
        if (!isAudited(entity)) return;
        try {
            Map<String, Object> oldState = PRE_STATE.get().remove(entity);
            Map<String, Map<String, Object>> changes = buildDeleteChanges(oldState);
            persist(entity, OperationLog.OperationType.DELETE, changes);
        } catch (Exception e) {
            log.error("OperationLogListener: failed to log DELETE for {}", entity.getClass().getSimpleName(), e);
        }
    }

    //--- HELPERS ---

    private boolean isAudited(Object entity) {
        return entity.getClass().isAnnotationPresent(AuditLog.class);
    }

    private Map<String, Object> extractFields(Object entity) throws IllegalAccessException {
        Map<String, Object> state = new LinkedHashMap<>();
        Class<?> clazz = entity.getClass();

        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (shouldSkip(field)) continue;
                field.setAccessible(true);
                Object value = field.get(entity);
                state.put(field.getName(), sanitize(field, value));
            }
            clazz = clazz.getSuperclass();
        }
        return state;
    }

    private boolean shouldSkip(Field field) {
        if (field.isAnnotationPresent(AuditLogIgnore.class))        return true;
        if (field.isAnnotationPresent(OneToMany.class))             return true;
        if (field.isAnnotationPresent(ManyToMany.class))            return true;
        // For @ManyToOne / @OneToOne we only store the FK id, not the whole object.
        // We capture them below in sanitize(), so we don't skip them here.
        return false;
    }

    private Object sanitize(Field field, Object value) {
        if (value == null) return null;

        if (field.isAnnotationPresent(ManyToOne.class)
                || field.isAnnotationPresent(OneToOne.class)) {
            // Extract the @Id field of the related entity
            return extractId(value);
        }

        // Primitives, Strings, Enums, Numbers, LocalDateTime, etc. are fine as-is.
        return value.toString();
    }

    private Object extractId(Object relatedEntity) {
        try {
            Class<?> clazz = relatedEntity.getClass();
            while (clazz != null && clazz != Object.class) {
                for (Field f : clazz.getDeclaredFields()) {
                    if (f.isAnnotationPresent(Id.class)) {
                        f.setAccessible(true);
                        Object id = f.get(relatedEntity);
                        return id != null ? id.toString() : null;
                    }
                }
                clazz = clazz.getSuperclass();
            }
        } catch (Exception ignored) {}
        return relatedEntity.toString();
    }

    private Map<String, Map<String, Object>> buildCreateChanges(Object entity)
            throws IllegalAccessException {
        Map<String, Object> fields = extractFields(entity);
        Map<String, Map<String, Object>> changes = new LinkedHashMap<>();
        fields.forEach((k, v) -> changes.put(k, mapOf("new", v)));
        return changes;
    }

    private Map<String, Map<String, Object>> buildUpdateChanges(
            Map<String, Object> oldState, Map<String, Object> newState) {

        Map<String, Map<String, Object>> changes = new LinkedHashMap<>();
        Set<String> allKeys = new LinkedHashSet<>(oldState.keySet());
        allKeys.addAll(newState.keySet());

        for (String key : allKeys) {
            Object oldVal = oldState.get(key);
            Object newVal = newState.get(key);
            if (!Objects.equals(oldVal, newVal)) {
                Map<String, Object> diff = new LinkedHashMap<>();
                diff.put("old", oldVal);
                diff.put("new", newVal);
                changes.put(key, diff);
            }
        }
        return changes;
    }

    private Map<String, Map<String, Object>> buildDeleteChanges(Map<String, Object> oldState) {
        Map<String, Map<String, Object>> changes = new LinkedHashMap<>();
        oldState.forEach((k, v) -> changes.put(k, mapOf("old", v)));
        return changes;
    }

    private void persist(Object entity,
                         OperationLog.OperationType operation,
                         Map<String, Map<String, Object>> changes) {
        try {
            ApplicationContext ctx = SpringContextHolder.getContext();
            OperationLogRepository repo     = ctx.getBean(OperationLogRepository.class);
            SecurityUtils          secUtils = ctx.getBean(SecurityUtils.class);

            String actor;
            try {
                actor = secUtils.getCurrentUserId();
            } catch (Exception e) {
                actor = "SYSTEM";
            }

            OperationLog log = OperationLog.builder()
                    .entityName(entity.getClass().getSimpleName())
                    .entityId(String.valueOf(extractId(entity)))
                    .operation(operation)
                    .performedAt(LocalDateTime.now())
                    .performedBy(actor)
                    .changes(changes)
                    .build();

            repo.save(log);

        } catch (Exception e) {
            // ← THIS is what's likely silently failing right now
            log.error("OperationLogListener: failed to persist operation log for {} [{}]",
                    entity.getClass().getSimpleName(), operation, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Map<K, V> mapOf(K key, V value) {
        Map<K, V> m = new LinkedHashMap<>();
        m.put(key, value);
        return m;
    }

}
