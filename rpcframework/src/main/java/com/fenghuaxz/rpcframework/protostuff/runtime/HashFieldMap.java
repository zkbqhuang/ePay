package com.fenghuaxz.rpcframework.protostuff.runtime;

import java.util.*;

/**
 * Field mapping implemented on top of hash for field lookup by number.
 * <p>
 * This is the less efficient than {@code ArrayFieldMap} for almost all cases.
 * But in case when field numbers are sparse and especially when max field
 * number is big - this mapping should be used.
 *
 * @author Kostiantyn Shchepanovskyi
 * @see ArrayFieldMap
 */
final class HashFieldMap<T> implements FieldMap<T> {
    private static final FieldComparator FIELD_COMPARATOR = new FieldComparator();
    private final List<Field<T>> fields;
    private final Map<Integer, Field<T>> fieldsByNumber;
    private final Map<String, Field<T>> fieldsByName;

    public HashFieldMap(Collection<Field<T>> fields) {
        fieldsByName = new HashMap<>();
        fieldsByNumber = new HashMap<>();
        for (Field<T> f : fields) {
            if (fieldsByName.containsKey(f.name)) {
                Field<T> prev = fieldsByName.get(f.name);
                throw new IllegalStateException(prev + " and " + f + " cannot have the same name.");
            }
            if (fieldsByNumber.containsKey(f.number)) {
                Field<T> prev = fieldsByNumber.get(f.number);
                throw new IllegalStateException(prev + " and " + f + " cannot have the same number.");
            }
            this.fieldsByNumber.put(f.number, f);
            this.fieldsByName.put(f.name, f);
        }

        List<Field<T>> fieldList = new ArrayList<Field<T>>(fields.size());
        fieldList.addAll(fields);
        Collections.sort(fieldList, FIELD_COMPARATOR);
        this.fields = Collections.unmodifiableList(fieldList);
    }

    @Override
    public Field<T> getFieldByNumber(int n) {
        return fieldsByNumber.get(n);
    }

    @Override
    public Field<T> getFieldByName(String fieldName) {
        return fieldsByName.get(fieldName);
    }

    @Override
    public int getFieldCount() {
        return fields.size();
    }

    @Override
    public List<Field<T>> getFields() {
        return fields;
    }

    private static class FieldComparator implements Comparator<Field<?>> {
        @Override
        public int compare(Field<?> o1, Field<?> o2) {
            return compare(o1.number, o2.number);
        }

        public static int compare(int x, int y) {
            return (x < y) ? -1 : ((x == y) ? 0 : 1);
        }
    }
}
