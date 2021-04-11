/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;


/**
 * Hash table based implementation of the <tt>Map</tt> interface, with
 * <em>weak keys</em>.
 * An entry in a <tt>WeakHashMap</tt> will automatically be removed when
 * its key is no longer in ordinary use.  More precisely, the presence of a
 * mapping for a given key will not prevent the key from being discarded by the
 * garbage collector, that is, made finalizable, finalized, and then reclaimed.
 * When a key has been discarded its entry is effectively removed from the map,
 * so this class behaves somewhat differently from other <tt>Map</tt>
 * implementations.
 *
 * <p> Both null values and the null key are supported. This class has
 * performance characteristics similar to those of the <tt>HashMap</tt>
 * class, and has the same efficiency parameters of <em>initial capacity</em>
 * and <em>load factor</em>.
 *
 * <p> Like most collection classes, this class is not synchronized.
 * A synchronized <tt>WeakHashMap</tt> may be constructed using the
 * {@link Collections#synchronizedMap Collections.synchronizedMap}
 * method.
 *
 * <p> This class is intended primarily for use with key objects whose
 * <tt>equals</tt> methods test for object identity using the
 * <tt>==</tt> operator.  Once such a key is discarded it can never be
 * recreated, so it is impossible to do a lookup of that key in a
 * <tt>WeakHashMap</tt> at some later time and be surprised that its entry
 * has been removed.  This class will work perfectly well with key objects
 * whose <tt>equals</tt> methods are not based upon object identity, such
 * as <tt>String</tt> instances.  With such recreatable key objects,
 * however, the automatic removal of <tt>WeakHashMap</tt> entries whose
 * keys have been discarded may prove to be confusing.
 *
 * <p> The behavior of the <tt>WeakHashMap</tt> class depends in part upon
 * the actions of the garbage collector, so several familiar (though not
 * required) <tt>Map</tt> invariants do not hold for this class.  Because
 * the garbage collector may discard keys at any time, a
 * <tt>WeakHashMap</tt> may behave as though an unknown thread is silently
 * removing entries.  In particular, even if you synchronize on a
 * <tt>WeakHashMap</tt> instance and invoke none of its mutator methods, it
 * is possible for the <tt>size</tt> method to return smaller values over
 * time, for the <tt>isEmpty</tt> method to return <tt>false</tt> and
 * then <tt>true</tt>, for the <tt>containsKey</tt> method to return
 * <tt>true</tt> and later <tt>false</tt> for a given key, for the
 * <tt>get</tt> method to return a value for a given key but later return
 * <tt>null</tt>, for the <tt>put</tt> method to return
 * <tt>null</tt> and the <tt>remove</tt> method to return
 * <tt>false</tt> for a key that previously appeared to be in the map, and
 * for successive examinations of the key set, the value collection, and
 * the entry set to yield successively smaller numbers of elements.
 *
 * <p> Each key object in a <tt>WeakHashMap</tt> is stored indirectly as
 * the referent of a weak reference.  Therefore a key will automatically be
 * removed only after the weak references to it, both inside and outside of the
 * map, have been cleared by the garbage collector.
 *
 * <p> <strong>Implementation note:</strong> The value objects in a
 * <tt>WeakHashMap</tt> are held by ordinary strong references.  Thus care
 * should be taken to ensure that value objects do not strongly refer to their
 * own keys, either directly or indirectly, since that will prevent the keys
 * from being discarded.  Note that a value object may refer indirectly to its
 * key via the <tt>WeakHashMap</tt> itself; that is, a value object may
 * strongly refer to some other key object whose associated value object, in
 * turn, strongly refers to the key of the first value object.  If the values
 * in the map do not rely on the map holding strong references to them, one way
 * to deal with this is to wrap values themselves within
 * <tt>WeakReferences</tt> before
 * inserting, as in: <tt>m.put(key, new WeakReference(value))</tt>,
 * and then unwrapping upon each <tt>get</tt>.
 *
 * <p>The iterators returned by the <tt>iterator</tt> method of the collections
 * returned by all of this class's "collection view methods" are
 * <i>fail-fast</i>: if the map is structurally modified at any time after the
 * iterator is created, in any way except through the iterator's own
 * <tt>remove</tt> method, the iterator will throw a {@link
 * ConcurrentModificationException}.  Thus, in the face of concurrent
 * modification, the iterator fails quickly and cleanly, rather than risking
 * arbitrary, non-deterministic behavior at an undetermined time in the future.
 *
 * <p>Note that the fail-fast behavior of an iterator cannot be guaranteed
 * as it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification.  Fail-fast iterators
 * throw <tt>ConcurrentModificationException</tt> on a best-effort basis.
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness:  <i>the fail-fast behavior of iterators
 * should be used only to detect bugs.</i>
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 *
 * @author      Doug Lea
 * @author      Josh Bloch
 * @author      Mark Reinhold
 * @since       1.2
 * @see         HashMap
 * @see         WeakReference
 */
public class WeakHashMap<K,V>
    extends AbstractMap<K,V>
    implements Map<K,V> {

    /**
     * 默认初始容量，必须是2的幂
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * 最大容量，必须是小于等于2的30次方的一个2的幂
     */
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * 默认负载系数
     */
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * 桶数组，长度必须始终为2的幂
     */
    Entry<K,V>[] table;

    /**
     * 键值对的数量
     */
    private int size;

    /**
     * 扩容阈值，当元素个数超过此阈值时，对桶数组进行扩容
     */
    private int threshold;

    /**
     * 负载系数
     */
    private final float loadFactor;

    /**
     * 弱引用的清除队列
     */
    private final ReferenceQueue<Object> queue = new ReferenceQueue<>();

    /**
     * 修改次数，用在迭代器中进行快速失败检查（fail-fast）
     */
    int modCount;

    @SuppressWarnings("unchecked")
    private Entry<K,V>[] newTable(int n) {
        return (Entry<K,V>[]) new Entry<?,?>[n];
    }

    /**
     * 使用给定的初始容量和负载系数实例化WeakHashMap
     */
    public WeakHashMap(int initialCapacity, float loadFactor) {
        // 校验初始容量
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Initial Capacity: "+
                                               initialCapacity);

        // 若初始容量大于最大容量，将其设置为最大容量
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;

        // 校验负载系数
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal Load factor: "+
                                               loadFactor);

        // 从1、2、4、8、16...中选择一个大于等于initialCapacity的最小的2的幂
        int capacity = 1;
        while (capacity < initialCapacity)
            capacity <<= 1;

        // 申请数组空间
        table = newTable(capacity);
        this.loadFactor = loadFactor;

        // 计算扩容阈值(当前容量*负载系数)
        threshold = (int)(capacity * loadFactor);
    }

    /**
     * 使用给定的初始容量和默认负载系数（0.75）实例化WeakHashMap
     */
    public WeakHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 使用默认初始容量（16）和默认负载系数（0.75）实例化WeakHashMap
     */
    public WeakHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 使用给定的Map实例化WeakHashMap，容量根据传入Map的大小计算，负载系数为默认（0.75）
     * 并将传入Map中的所有键值对添加进WeakHashMap中
     */
    public WeakHashMap(Map<? extends K, ? extends V> m) {
        this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1,
                DEFAULT_INITIAL_CAPACITY),
             DEFAULT_LOAD_FACTOR);
        putAll(m);
    }

    // internal utilities

    /**
     * key为null时的替换对象
     */
    private static final Object NULL_KEY = new Object();

    /**
     * 当key为null时，将key替换成NULL_KEY对象
     */
    private static Object maskNull(Object key) {
        return (key == null) ? NULL_KEY : key;
    }

    /**
     * 若key为NULL_KEY，将其还原为null
     */
    static Object unmaskNull(Object key) {
        return (key == NULL_KEY) ? null : key;
    }

    /**
     * Checks for equality of non-null reference x and possibly-null y.  By
     * default uses Object.equals.
     */
    private static boolean eq(Object x, Object y) {
        return x == y || x.equals(y);
    }

    /**
     * 扰动函数
     */
    final int hash(Object k) {
        int h = k.hashCode();

        // 对hashCode进行4次扰动
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    /**
     * 找到hash在桶数组中对应的索引
     */
    private static int indexFor(int h, int length) {
        // 当length为2的幂时，相当于对length取模
        return h & (length-1);
    }

    /**
     * 清除已经被回收的键值对
     */
    private void expungeStaleEntries() {
        // 遍历清除队列中的元素
        for (Object x; (x = queue.poll()) != null; ) {
            synchronized (queue) {
                @SuppressWarnings("unchecked")
                    Entry<K,V> e = (Entry<K,V>) x;

                // 找到此节点对应的索引
                int i = indexFor(e.hash, table.length);

                Entry<K,V> prev = table[i];
                Entry<K,V> p = prev;

                // 遍历链表
                while (p != null) {
                    // 保存后继节点
                    Entry<K,V> next = p.next;

                    // 找到key相同的节点
                    if (p == e) {
                        if (prev == e)
                            // 如果是链表头，将链表头指针指向下一个节点
                            table[i] = next;
                        else
                            // 将前驱节点和后继节点链接起来，删除当前节点
                            prev.next = next;

                        e.value = null;
                        size--;
                        break;
                    }
                    prev = p;
                    p = next;
                }
            }
        }
    }

    /**
     * 获取桶数组前先对已回收元素进行清理
     */
    private Entry<K,V>[] getTable() {
        expungeStaleEntries();
        return table;
    }

    /**
     * 若Map不为空，则返回元素数量前先清理掉已被回收的元素
     */
    public int size() {
        if (size == 0)
            return 0;
        expungeStaleEntries();
        return size;
    }

    /**
     * 先清理已被回收的元素，在判断是否为空
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * 若key还存在，则返回其对应的value值
     */
    public V get(Object key) {
        // 若key为null，将其替换为NULL_KEY
        Object k = maskNull(key);
        // 对key的hashCode进行扰动
        int h = hash(k);
        // 清理已被回收的元素
        Entry<K,V>[] tab = getTable();
        // 找到hash对应的索引
        int index = indexFor(h, tab.length);
        // 遍历链表
        Entry<K,V> e = tab[index];
        while (e != null) {
            // 如果找到key相同的节点，返回其value值
            if (e.hash == h && eq(k, e.get()))
                return e.value;
            e = e.next;
        }
        // 找不到返回null
        return null;
    }

    /**
     * 检查是否包含key
     */
    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    /**
     * 根据key查找对应的节点，与get(key)逻辑基本相同
     */
    Entry<K,V> getEntry(Object key) {
        Object k = maskNull(key);
        int h = hash(k);
        Entry<K,V>[] tab = getTable();
        int index = indexFor(h, tab.length);
        Entry<K,V> e = tab[index];
        while (e != null && !(e.hash == h && eq(k, e.get())))
            e = e.next;
        return e;
    }

    /**
     * 将键值对添加进Map，或者key之前存在，则将其对应的值更新为value
     */
    public V put(K key, V value) {
        // 若key为null，将其替换为NULL_KEY
        Object k = maskNull(key);
        // 对key的hashCode进行扰动
        int h = hash(k);
        // 清理已被回收的元素
        Entry<K,V>[] tab = getTable();
        // 找到hash对应的索引
        int i = indexFor(h, tab.length);

        // 遍历链表
        for (Entry<K,V> e = tab[i]; e != null; e = e.next) {
            // 如果key之前存在，且保存的值不等于value，将其值更新为value
            if (h == e.hash && eq(k, e.get())) {
                V oldValue = e.value;
                if (value != oldValue)
                    e.value = value;
                return oldValue;
            }
        }

        // key之前不存在，创建一个新的节点，将其插入链表头部
        modCount++;
        Entry<K,V> e = tab[i];
        tab[i] = new Entry<>(k, value, queue, h, e);

        // 若插入后元素个数大于等于扩容阈值，将桶的容量扩大为2倍
        if (++size >= threshold)
            resize(tab.length * 2);
        return null;
    }

    /**
     * 对桶数组进行扩容，并将元素复制到新的桶中
     */
    void resize(int newCapacity) {
        Entry<K,V>[] oldTable = getTable();
        int oldCapacity = oldTable.length;

        // 如果旧容量已经到最大容量，则不对桶进行扩容
        // 将扩容阈值设置为Integer.MAX_VALUE
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }

        // 创建一个新的数组
        Entry<K,V>[] newTable = newTable(newCapacity);

        // 将旧桶中的元素重新映射到新桶中
        transfer(oldTable, newTable);
        table = newTable;

        /*
         * If ignoring null elements and processing ref queue caused massive
         * shrinkage, then restore old table.  This should be rare, but avoids
         * unbounded expansion of garbage-filled tables.
         */
        if (size >= threshold / 2) {
            // 如果当前元素个数不小于(threshold / 2)，重新计算threshold
            threshold = (int)(newCapacity * loadFactor);
        } else {
            // 如果回收了元素之后，元素个数低于(threshold / 2)，则回复旧桶
            expungeStaleEntries();
            transfer(newTable, oldTable);
            table = oldTable;
        }
    }

    /**
     * 将旧数组中的所有元素转移至新数组
     */
    private void transfer(Entry<K,V>[] src, Entry<K,V>[] dest) {
        // 遍历旧桶
        for (int j = 0; j < src.length; ++j) {
            Entry<K,V> e = src[j];
            src[j] = null;

            // 遍历链表
            while (e != null) {
                Entry<K,V> next = e.next;
                Object key = e.get();
                if (key == null) {
                    // 若key已被回收，则删除此节点
                    e.next = null;  // Help GC
                    e.value = null; //  "   "
                    size--;
                } else {
                    // 将节点重新映射，并插入到对应链表的头部
                    int i = indexFor(e.hash, dest.length);
                    e.next = dest[i];
                    dest[i] = e;
                }
                e = next;
            }
        }
    }

    /**
     * 将给定Map中的所有元素拷贝到WeakHashMap中
     */
    public void putAll(Map<? extends K, ? extends V> m) {

        // 若传入Map为空，则不进行处理
        int numKeysToBeAdded = m.size();
        if (numKeysToBeAdded == 0)
            return;

        // 若传入map大小超过扩容阈值，对桶进行扩容
        if (numKeysToBeAdded > threshold) {

            // 计算新容量
            int targetCapacity = (int)(numKeysToBeAdded / loadFactor + 1);

            // 新容量不能超过最大容量
            if (targetCapacity > MAXIMUM_CAPACITY)
                targetCapacity = MAXIMUM_CAPACITY;
            int newCapacity = table.length;

            // 保证新容量为2的幂
            while (newCapacity < targetCapacity)
                newCapacity <<= 1;

            // 若新容量大于当前容量，进行扩容
            if (newCapacity > table.length)
                resize(newCapacity);
        }

        // 遍历Map的元素，将其添加到WeakHashMap
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
            put(e.getKey(), e.getValue());
    }

    /**
     * 删除给定的key
     */
    public V remove(Object key) {
        // 若key为null，将其替换为NULL_KEY
        Object k = maskNull(key);
        // 对key的hashCode进行扰动
        int h = hash(k);
        // 清理已被回收的元素
        Entry<K,V>[] tab = getTable();
        // 找到hash对应的索引
        int i = indexFor(h, tab.length);
        Entry<K,V> prev = tab[i];
        Entry<K,V> e = prev;

        // 遍历链表
        while (e != null) {
            // 保存后继节点
            Entry<K,V> next = e.next;

            // 如果此节点key与给定key相同，则将其删除
            if (h == e.hash && eq(k, e.get())) {
                // 修改次数加一
                modCount++;

                // 元素个数减一
                size--;

                if (prev == e)
                    // 如果是头结点，则将链表头指向下一个节点
                    tab[i] = next;
                else
                    // 将前驱节点和后继节点链接起来，删除当前节点
                    prev.next = next;
                return e.value;
            }
            prev = e;
            e = next;
        }

        return null;
    }

    /** Special version of remove needed by Entry set */
    boolean removeMapping(Object o) {
        if (!(o instanceof Map.Entry))
            return false;
        Entry<K,V>[] tab = getTable();
        Map.Entry<?,?> entry = (Map.Entry<?,?>)o;
        Object k = maskNull(entry.getKey());
        int h = hash(k);
        int i = indexFor(h, tab.length);
        Entry<K,V> prev = tab[i];
        Entry<K,V> e = prev;

        while (e != null) {
            Entry<K,V> next = e.next;
            if (h == e.hash && e.equals(entry)) {
                modCount++;
                size--;
                if (prev == e)
                    tab[i] = next;
                else
                    prev.next = next;
                return true;
            }
            prev = e;
            e = next;
        }

        return false;
    }

    /**
     * 清空Map
     */
    public void clear() {
        // 清空引用队列
        while (queue.poll() != null)
            ;

        // 修改次数加一
        modCount++;

        // 桶数组所有位置都置为null
        Arrays.fill(table, null);

        // 元素个数置为0
        size = 0;

        // 清空数组后可能有GC回收，再次清空引用队列
        while (queue.poll() != null)
            ;
    }

    /**
     * 检查Map是否包含指定value值
     */
    public boolean containsValue(Object value) {
        // value为null，跳转至containsNullValue方法
        if (value==null)
            return containsNullValue();

        Entry<K,V>[] tab = getTable();
        // 遍历桶数组
        for (int i = tab.length; i-- > 0;)
            // 遍历链表
            for (Entry<K,V> e = tab[i]; e != null; e = e.next)
                if (value.equals(e.value))
                    return true;
        return false;
    }

    /**
     * 是否包含value为null的键值对
     */
    private boolean containsNullValue() {
        Entry<K,V>[] tab = getTable();
        // 遍历桶数组
        for (int i = tab.length; i-- > 0;)
            // 遍历链表
            for (Entry<K,V> e = tab[i]; e != null; e = e.next)
                if (e.value==null)
                    return true;
        return false;
    }

    /**
     * 储存元素的节点
     * 继承自WeakReference，对key保存的是其弱引用
     */
    private static class Entry<K,V> extends WeakReference<Object> implements Map.Entry<K,V> {
        V value;
        final int hash;
        Entry<K,V> next;

        /**
         * Creates new entry.
         */
        Entry(Object key, V value,
              ReferenceQueue<Object> queue,
              int hash, Entry<K,V> next) {
            super(key, queue);
            this.value = value;
            this.hash  = hash;
            this.next  = next;
        }

        @SuppressWarnings("unchecked")
        public K getKey() {
            return (K) WeakHashMap.unmaskNull(get());
        }

        public V getValue() {
            return value;
        }

        public V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>)o;
            K k1 = getKey();
            Object k2 = e.getKey();
            if (k1 == k2 || (k1 != null && k1.equals(k2))) {
                V v1 = getValue();
                Object v2 = e.getValue();
                if (v1 == v2 || (v1 != null && v1.equals(v2)))
                    return true;
            }
            return false;
        }

        public int hashCode() {
            K k = getKey();
            V v = getValue();
            return Objects.hashCode(k) ^ Objects.hashCode(v);
        }

        public String toString() {
            return getKey() + "=" + getValue();
        }
    }

    private abstract class HashIterator<T> implements Iterator<T> {
        private int index;
        private Entry<K,V> entry;
        private Entry<K,V> lastReturned;
        private int expectedModCount = modCount;

        /**
         * Strong reference needed to avoid disappearance of key
         * between hasNext and next
         */
        private Object nextKey;

        /**
         * Strong reference needed to avoid disappearance of key
         * between nextEntry() and any use of the entry
         */
        private Object currentKey;

        HashIterator() {
            index = isEmpty() ? 0 : table.length;
        }

        public boolean hasNext() {
            Entry<K,V>[] t = table;

            while (nextKey == null) {
                Entry<K,V> e = entry;
                int i = index;
                while (e == null && i > 0)
                    e = t[--i];
                entry = e;
                index = i;
                if (e == null) {
                    currentKey = null;
                    return false;
                }
                nextKey = e.get(); // hold on to key in strong ref
                if (nextKey == null)
                    entry = entry.next;
            }
            return true;
        }

        /** The common parts of next() across different types of iterators */
        protected Entry<K,V> nextEntry() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (nextKey == null && !hasNext())
                throw new NoSuchElementException();

            lastReturned = entry;
            entry = entry.next;
            currentKey = nextKey;
            nextKey = null;
            return lastReturned;
        }

        public void remove() {
            if (lastReturned == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();

            WeakHashMap.this.remove(currentKey);
            expectedModCount = modCount;
            lastReturned = null;
            currentKey = null;
        }

    }

    private class ValueIterator extends HashIterator<V> {
        public V next() {
            return nextEntry().value;
        }
    }

    private class KeyIterator extends HashIterator<K> {
        public K next() {
            return nextEntry().getKey();
        }
    }

    private class EntryIterator extends HashIterator<Map.Entry<K,V>> {
        public Map.Entry<K,V> next() {
            return nextEntry();
        }
    }

    // Views

    private transient Set<Map.Entry<K,V>> entrySet;

    /**
     * Returns a {@link Set} view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation), the results of
     * the iteration are undefined.  The set supports element removal,
     * which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations.  It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     */
    public Set<K> keySet() {
        Set<K> ks = keySet;
        if (ks == null) {
            ks = new KeySet();
            keySet = ks;
        }
        return ks;
    }

    private class KeySet extends AbstractSet<K> {
        public Iterator<K> iterator() {
            return new KeyIterator();
        }

        public int size() {
            return WeakHashMap.this.size();
        }

        public boolean contains(Object o) {
            return containsKey(o);
        }

        public boolean remove(Object o) {
            if (containsKey(o)) {
                WeakHashMap.this.remove(o);
                return true;
            }
            else
                return false;
        }

        public void clear() {
            WeakHashMap.this.clear();
        }

        public Spliterator<K> spliterator() {
            return new KeySpliterator<>(WeakHashMap.this, 0, -1, 0, 0);
        }
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa.  If the map is
     * modified while an iteration over the collection is in progress
     * (except through the iterator's own <tt>remove</tt> operation),
     * the results of the iteration are undefined.  The collection
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Collection.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not
     * support the <tt>add</tt> or <tt>addAll</tt> operations.
     */
    public Collection<V> values() {
        Collection<V> vs = values;
        if (vs == null) {
            vs = new Values();
            values = vs;
        }
        return vs;
    }

    private class Values extends AbstractCollection<V> {
        public Iterator<V> iterator() {
            return new ValueIterator();
        }

        public int size() {
            return WeakHashMap.this.size();
        }

        public boolean contains(Object o) {
            return containsValue(o);
        }

        public void clear() {
            WeakHashMap.this.clear();
        }

        public Spliterator<V> spliterator() {
            return new ValueSpliterator<>(WeakHashMap.this, 0, -1, 0, 0);
        }
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation, or through the
     * <tt>setValue</tt> operation on a map entry returned by the
     * iterator) the results of the iteration are undefined.  The set
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
     * <tt>clear</tt> operations.  It does not support the
     * <tt>add</tt> or <tt>addAll</tt> operations.
     */
    public Set<Map.Entry<K,V>> entrySet() {
        Set<Map.Entry<K,V>> es = entrySet;
        return es != null ? es : (entrySet = new EntrySet());
    }

    private class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        public Iterator<Map.Entry<K,V>> iterator() {
            return new EntryIterator();
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>)o;
            Entry<K,V> candidate = getEntry(e.getKey());
            return candidate != null && candidate.equals(e);
        }

        public boolean remove(Object o) {
            return removeMapping(o);
        }

        public int size() {
            return WeakHashMap.this.size();
        }

        public void clear() {
            WeakHashMap.this.clear();
        }

        private List<Map.Entry<K,V>> deepCopy() {
            List<Map.Entry<K,V>> list = new ArrayList<>(size());
            for (Map.Entry<K,V> e : this)
                list.add(new SimpleEntry<>(e));
            return list;
        }

        public Object[] toArray() {
            return deepCopy().toArray();
        }

        public <T> T[] toArray(T[] a) {
            return deepCopy().toArray(a);
        }

        public Spliterator<Map.Entry<K,V>> spliterator() {
            return new EntrySpliterator<>(WeakHashMap.this, 0, -1, 0, 0);
        }
    }

    /**
     * 遍历所有元素
     */
    @SuppressWarnings("unchecked")
    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        int expectedModCount = modCount;

        Entry<K, V>[] tab = getTable();
        // 遍历桶数组
        for (Entry<K, V> entry : tab) {
            // 遍历链表
            while (entry != null) {
                Object key = entry.get();

                // 若key不为null，传给action处理
                if (key != null) {
                    action.accept((K)WeakHashMap.unmaskNull(key), entry.value);
                }
                entry = entry.next;

                if (expectedModCount != modCount) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    /**
     * 遍历所有元素，根据给定的function替换值
     * @param function
     */
    @SuppressWarnings("unchecked")
    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        int expectedModCount = modCount;

        Entry<K, V>[] tab = getTable();;
        // 遍历桶数组
        for (Entry<K, V> entry : tab) {
            // 遍历链表
            while (entry != null) {
                Object key = entry.get();
                // 若key不为null，传递给function，将返回的新值赋给value
                if (key != null) {
                    entry.value = function.apply((K)WeakHashMap.unmaskNull(key), entry.value);
                }
                entry = entry.next;

                if (expectedModCount != modCount) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    /**
     * Similar form as other hash Spliterators, but skips dead
     * elements.
     */
    static class WeakHashMapSpliterator<K,V> {
        final WeakHashMap<K,V> map;
        Entry<K,V> current; // current node
        int index;             // current index, modified on advance/split
        int fence;             // -1 until first use; then one past last index
        int est;               // size estimate
        int expectedModCount;  // for comodification checks

        WeakHashMapSpliterator(WeakHashMap<K,V> m, int origin,
                               int fence, int est,
                               int expectedModCount) {
            this.map = m;
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getFence() { // initialize fence and size on first use
            int hi;
            if ((hi = fence) < 0) {
                WeakHashMap<K,V> m = map;
                est = m.size();
                expectedModCount = m.modCount;
                hi = fence = m.table.length;
            }
            return hi;
        }

        public final long estimateSize() {
            getFence(); // force init
            return (long) est;
        }
    }

    static final class KeySpliterator<K,V>
        extends WeakHashMapSpliterator<K,V>
        implements Spliterator<K> {
        KeySpliterator(WeakHashMap<K,V> m, int origin, int fence, int est,
                       int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public KeySpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null :
                new KeySpliterator<K,V>(map, lo, index = mid, est >>>= 1,
                                        expectedModCount);
        }

        public void forEachRemaining(Consumer<? super K> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            WeakHashMap<K,V> m = map;
            Entry<K,V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = tab.length;
            }
            else
                mc = expectedModCount;
            if (tab.length >= hi && (i = index) >= 0 &&
                (i < (index = hi) || current != null)) {
                Entry<K,V> p = current;
                current = null; // exhaust
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        Object x = p.get();
                        p = p.next;
                        if (x != null) {
                            @SuppressWarnings("unchecked") K k =
                                (K) WeakHashMap.unmaskNull(x);
                            action.accept(k);
                        }
                    }
                } while (p != null || i < hi);
            }
            if (m.modCount != mc)
                throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Entry<K,V>[] tab = map.table;
            if (tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        Object x = current.get();
                        current = current.next;
                        if (x != null) {
                            @SuppressWarnings("unchecked") K k =
                                (K) WeakHashMap.unmaskNull(x);
                            action.accept(k);
                            if (map.modCount != expectedModCount)
                                throw new ConcurrentModificationException();
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return Spliterator.DISTINCT;
        }
    }

    static final class ValueSpliterator<K,V>
        extends WeakHashMapSpliterator<K,V>
        implements Spliterator<V> {
        ValueSpliterator(WeakHashMap<K,V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public ValueSpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null :
                new ValueSpliterator<K,V>(map, lo, index = mid, est >>>= 1,
                                          expectedModCount);
        }

        public void forEachRemaining(Consumer<? super V> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            WeakHashMap<K,V> m = map;
            Entry<K,V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = tab.length;
            }
            else
                mc = expectedModCount;
            if (tab.length >= hi && (i = index) >= 0 &&
                (i < (index = hi) || current != null)) {
                Entry<K,V> p = current;
                current = null; // exhaust
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        Object x = p.get();
                        V v = p.value;
                        p = p.next;
                        if (x != null)
                            action.accept(v);
                    }
                } while (p != null || i < hi);
            }
            if (m.modCount != mc)
                throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Entry<K,V>[] tab = map.table;
            if (tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        Object x = current.get();
                        V v = current.value;
                        current = current.next;
                        if (x != null) {
                            action.accept(v);
                            if (map.modCount != expectedModCount)
                                throw new ConcurrentModificationException();
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return 0;
        }
    }

    static final class EntrySpliterator<K,V>
        extends WeakHashMapSpliterator<K,V>
        implements Spliterator<Map.Entry<K,V>> {
        EntrySpliterator(WeakHashMap<K,V> m, int origin, int fence, int est,
                       int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public EntrySpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null :
                new EntrySpliterator<K,V>(map, lo, index = mid, est >>>= 1,
                                          expectedModCount);
        }


        public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            WeakHashMap<K,V> m = map;
            Entry<K,V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = tab.length;
            }
            else
                mc = expectedModCount;
            if (tab.length >= hi && (i = index) >= 0 &&
                (i < (index = hi) || current != null)) {
                Entry<K,V> p = current;
                current = null; // exhaust
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        Object x = p.get();
                        V v = p.value;
                        p = p.next;
                        if (x != null) {
                            @SuppressWarnings("unchecked") K k =
                                (K) WeakHashMap.unmaskNull(x);
                            action.accept
                                (new SimpleImmutableEntry<K,V>(k, v));
                        }
                    }
                } while (p != null || i < hi);
            }
            if (m.modCount != mc)
                throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(Consumer<? super Map.Entry<K,V>> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Entry<K,V>[] tab = map.table;
            if (tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        Object x = current.get();
                        V v = current.value;
                        current = current.next;
                        if (x != null) {
                            @SuppressWarnings("unchecked") K k =
                                (K) WeakHashMap.unmaskNull(x);
                            action.accept
                                (new SimpleImmutableEntry<K,V>(k, v));
                            if (map.modCount != expectedModCount)
                                throw new ConcurrentModificationException();
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return Spliterator.DISTINCT;
        }
    }

}
