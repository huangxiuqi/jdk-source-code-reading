/*
* Copyright (c) 1997, 2014, Oracle and/or its affiliates. All rights reserved.
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

import java.io.Serializable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
* A Red-Black tree based {@link NavigableMap} implementation.
* The map is sorted according to the {@linkplain Comparable natural
* ordering} of its keys, or by a {@link Comparator} provided at map
* creation time, depending on which constructor is used.
*
* <p>This implementation provides guaranteed log(n) time cost for the
* {@code containsKey}, {@code get}, {@code put} and {@code remove}
* operations.  Algorithms are adaptations of those in Cormen, Leiserson, and
* Rivest's <em>Introduction to Algorithms</em>.
*
* <p>Note that the ordering maintained by a tree map, like any sorted map, and
* whether or not an explicit comparator is provided, must be <em>consistent
* with {@code equals}</em> if this sorted map is to correctly implement the
* {@code Map} interface.  (See {@code Comparable} or {@code Comparator} for a
* precise definition of <em>consistent with equals</em>.)  This is so because
* the {@code Map} interface is defined in terms of the {@code equals}
* operation, but a sorted map performs all key comparisons using its {@code
* compareTo} (or {@code compare}) method, so two keys that are deemed equal by
* this method are, from the standpoint of the sorted map, equal.  The behavior
* of a sorted map <em>is</em> well-defined even if its ordering is
* inconsistent with {@code equals}; it just fails to obey the general contract
* of the {@code Map} interface.
*
* <p><strong>Note that this implementation is not synchronized.</strong>
* If multiple threads access a map concurrently, and at least one of the
* threads modifies the map structurally, it <em>must</em> be synchronized
* externally.  (A structural modification is any operation that adds or
* deletes one or more mappings; merely changing the value associated
* with an existing key is not a structural modification.)  This is
* typically accomplished by synchronizing on some object that naturally
* encapsulates the map.
* If no such object exists, the map should be "wrapped" using the
* {@link Collections#synchronizedSortedMap Collections.synchronizedSortedMap}
* method.  This is best done at creation time, to prevent accidental
* unsynchronized access to the map: <pre>
*   SortedMap m = Collections.synchronizedSortedMap(new TreeMap(...));</pre>
*
* <p>The iterators returned by the {@code iterator} method of the collections
* returned by all of this class's "collection view methods" are
* <em>fail-fast</em>: if the map is structurally modified at any time after
* the iterator is created, in any way except through the iterator's own
* {@code remove} method, the iterator will throw a {@link
* ConcurrentModificationException}.  Thus, in the face of concurrent
* modification, the iterator fails quickly and cleanly, rather than risking
* arbitrary, non-deterministic behavior at an undetermined time in the future.
*
* <p>Note that the fail-fast behavior of an iterator cannot be guaranteed
* as it is, generally speaking, impossible to make any hard guarantees in the
* presence of unsynchronized concurrent modification.  Fail-fast iterators
* throw {@code ConcurrentModificationException} on a best-effort basis.
* Therefore, it would be wrong to write a program that depended on this
* exception for its correctness:   <em>the fail-fast behavior of iterators
* should be used only to detect bugs.</em>
*
* <p>All {@code Map.Entry} pairs returned by methods in this class
* and its views represent snapshots of mappings at the time they were
* produced. They do <strong>not</strong> support the {@code Entry.setValue}
* method. (Note however that it is possible to change mappings in the
* associated map using {@code put}.)
*
* <p>This class is a member of the
* <a href="{@docRoot}/../technotes/guides/collections/index.html">
* Java Collections Framework</a>.
*
* @param <K> the type of keys maintained by this map
* @param <V> the type of mapped values
*
* @author  Josh Bloch and Doug Lea
* @see Map
* @see HashMap
* @see Hashtable
* @see Comparable
* @see Comparator
* @see Collection
* @since 1.2
*/

public class TreeMap<K,V>
extends AbstractMap<K,V>
implements NavigableMap<K,V>, Cloneable, Serializable
{
/**
 * 比较器，实例化时传入，若为null，则使用键的自然序
 * @serial
 */
private final Comparator<? super K> comparator;

/**
 * 红黑树根节点
 */
private transient Entry<K,V> root;

/**
 * 元素个数
 */
private transient int size = 0;

/**
 * 修改次数，用作快速失败检查
 */
private transient int modCount = 0;

/**
 * 使用键自然序做比较器实例化TreeMap
 */
public TreeMap() {
    comparator = null;
}

/**
 * 使用给定的比较器实例化TreeMap
 */
public TreeMap(Comparator<? super K> comparator) {
    this.comparator = comparator;
}

/**
 * 使用键自然序，将给定Map中所有元素都复制过来
 */
public TreeMap(Map<? extends K, ? extends V> m) {
    comparator = null;
    putAll(m);
}

/**
 * 比较器使用有序Map的比较器，将有序Map的所有元素都复制过来
 */
public TreeMap(SortedMap<K, ? extends V> m) {
    comparator = m.comparator();
    try {
        buildFromSorted(m.size(), m.entrySet().iterator(), null, null);
    } catch (java.io.IOException cannotHappen) {
    } catch (ClassNotFoundException cannotHappen) {
    }
}


// Query Operations

/**
 * Returns the number of key-value mappings in this map.
 *
 * @return the number of key-value mappings in this map
 */
public int size() {
    return size;
}

/**
 * 判断给定的Key是否存在
 */
public boolean containsKey(Object key) {
    return getEntry(key) != null;
}

/**
 * 判断给定的value是否存在
 */
public boolean containsValue(Object value) {
    // 中序遍历整棵树
    for (Entry<K,V> e = getFirstEntry(); e != null; e = successor(e))
        if (valEquals(value, e.value))
            return true;
    return false;
}

/**
 * 根据key查找对应的值，若key不存在，则返回null
 */
public V get(Object key) {
    Entry<K,V> p = getEntry(key);
    return (p==null ? null : p.value);
}

public Comparator<? super K> comparator() {
    return comparator;
}

/**
 * 找到最小键
 */
public K firstKey() {
    return key(getFirstEntry());
}

/**
 * 找到最大键
 */
public K lastKey() {
    return key(getLastEntry());
}

/**
 * 将给定map中的所有元素复制过来
 */
public void putAll(Map<? extends K, ? extends V> map) {
    int mapSize = map.size();

    // 如果当前map为空且传入map是有序map、比较器相同，则使用buildFromSorted方法直接生成树
    // 否则，循环插入每一个元素
    if (size==0 && mapSize!=0 && map instanceof SortedMap) {
        Comparator<?> c = ((SortedMap<?,?>)map).comparator();
        if (c == comparator || (c != null && c.equals(comparator))) {
            ++modCount;
            try {
                buildFromSorted(mapSize, map.entrySet().iterator(),
                                null, null);
            } catch (java.io.IOException cannotHappen) {
            } catch (ClassNotFoundException cannotHappen) {
            }
            return;
        }
    }
    super.putAll(map);
}

/**
 * 根据给定的key查找节点，找不到返回null
 * 若设置了comparator，则使用getEntryUsingComparator(Object key)方法进行查找
 * 否则使用键的自然序做比较器，进行二叉搜索树查找
 */
final Entry<K,V> getEntry(Object key) {

    // 如果设置了自定义比较器，则
    if (comparator != null)
        return getEntryUsingComparator(key);
    if (key == null)
        throw new NullPointerException();
    @SuppressWarnings("unchecked")
        Comparable<? super K> k = (Comparable<? super K>) key;

    // 二叉搜索树的查找
    Entry<K,V> p = root;
    while (p != null) {
        int cmp = k.compareTo(p.key);
        if (cmp < 0)
            p = p.left;
        else if (cmp > 0)
            p = p.right;
        else
            return p;
    }
    return null;
}

/**
 * 使用自定义比较器查找元素，逻辑与getEntry(Object key)一致
 */
final Entry<K,V> getEntryUsingComparator(Object key) {
    @SuppressWarnings("unchecked")
        K k = (K) key;
    Comparator<? super K> cpr = comparator;
    if (cpr != null) {
        Entry<K,V> p = root;
        while (p != null) {
            int cmp = cpr.compare(k, p.key);
            if (cmp < 0)
                p = p.left;
            else if (cmp > 0)
                p = p.right;
            else
                return p;
        }
    }
    return null;
}

/**
 * 返回大于等于给定key的最小节点
 * 二叉搜索树中，一个节点是大于其左子树所有节点的最小节点
 * 所以本方法就是找到给定key所在的最小左子树的父节点
 * 最小树指节点个数最少的树
 */
final Entry<K,V> getCeilingEntry(K key) {

    // 从根节点开始查找
    Entry<K,V> p = root;
    while (p != null) {
        int cmp = compare(key, p.key);
        if (cmp < 0) {
            if (p.left != null)
                // 给定key小于当前节点且当前节点存在左子树，继续去当前节点的左子树查找
                p = p.left;
            else
                // 当前节点不存在左子树，则当前节点为大于给定key的最小节点
                return p;
        } else if (cmp > 0) {
            if (p.right != null) {
                // 给定key大于当前节点且当前节点存在右子树，继续去当前节点的右子树查找
                p = p.right;
            } else {
                // 当前节点不存在右子树，则向上返回
                Entry<K,V> parent = p.parent;
                Entry<K,V> ch = p;
                // 向上返回直到当前节点ch不是其父节点parent的右孩子，返回其父节点
                // 此时，当前节点ch是其父节点parent的左子树的根节点，而父节点是大于左子树所有节点的最小节点
                // 所以这个父节点parent即为大于给定key的最小节点
                // 这个当前节点ch也可能是根节点，说明没有大于给定key的节点，返回根节点的父节点（null）
                while (parent != null && ch == parent.right) {
                    ch = parent;
                    parent = parent.parent;
                }
                return parent;
            }
        } else
            // 当前节点等于给定key，直接返回
            return p;
    }

    // 找不到返回null
    return null;
}

/**
 * 返回小于等于给定key的最大节点
 * 二叉搜索树中，一个节点是小于其右子树所有节点的最大节点
 * 所以本方法就是找到给定key所在的最小右子树的父节点
 * 最小树指节点个数最少的树
 */
final Entry<K,V> getFloorEntry(K key) {

    // 从根节点开始查找
    Entry<K,V> p = root;
    while (p != null) {
        int cmp = compare(key, p.key);
        if (cmp > 0) {
            if (p.right != null)
                // 若key大于当前节点且当前节点存在右子树，继续去当前节点的右子树查找
                p = p.right;
            else
                // 当前节点不存在右子树，则当前节点为小于等于给定key的最大节点
                return p;
        } else if (cmp < 0) {
            if (p.left != null) {
                // 若key小于当前节点且当前节点存在左子树，继续去当前节点的左子树查找
                p = p.left;
            } else {
                // 当前节点不存在左子树，向上返回
                Entry<K,V> parent = p.parent;
                Entry<K,V> ch = p;
                // 向上返回直到当前节点ch不是其父节点parent的左孩子，返回其父节点
                // 此时，当前节点ch是其父节点parent的右子树的根节点，而父节点是小于右子树所有节点的最大节点
                // 所以这个父节点parent即为小于给定key的最大节点
                // 这个当前节点ch也可能是根节点，说明没有小于给定key的节点，返回根节点的父节点（null）
                while (parent != null && ch == parent.left) {
                    ch = parent;
                    parent = parent.parent;
                }
                return parent;
            }
        } else
            // 当前节点等于给定key，直接返回
            return p;

    }
    return null;
}

/**
 * 返回大于给定key的最小节点
 * 逻辑与getCeilingEntry方法基本相同，只是去除了
 * 等于的比较，必须返回大于给定key的节点
 */
final Entry<K,V> getHigherEntry(K key) {
    Entry<K,V> p = root;
    while (p != null) {
        int cmp = compare(key, p.key);
        if (cmp < 0) {
            if (p.left != null)
                p = p.left;
            else
                return p;
        } else {
            if (p.right != null) {
                p = p.right;
            } else {
                Entry<K,V> parent = p.parent;
                Entry<K,V> ch = p;
                while (parent != null && ch == parent.right) {
                    ch = parent;
                    parent = parent.parent;
                }
                return parent;
            }
        }
    }
    return null;
}

/**
 * 返回小于给定key的最大节点
 * 逻辑与getFloorEntry方法基本相同，只是去除了
 * 等于的比较，必须返回小于给定key的节点
 */
final Entry<K,V> getLowerEntry(K key) {
    Entry<K,V> p = root;
    while (p != null) {
        int cmp = compare(key, p.key);
        if (cmp > 0) {
            if (p.right != null)
                p = p.right;
            else
                return p;
        } else {
            if (p.left != null) {
                p = p.left;
            } else {
                Entry<K,V> parent = p.parent;
                Entry<K,V> ch = p;
                while (parent != null && ch == parent.left) {
                    ch = parent;
                    parent = parent.parent;
                }
                return parent;
            }
        }
    }
    return null;
}

/**
 * 插入给定的键值对，若键之前存在，则更新其值
 */
public V put(K key, V value) {
    Entry<K,V> t = root;
    if (t == null) {
        // 第一个插入的键值对，直接作为根节点

        // 类型检查
        compare(key, key);
        root = new Entry<>(key, value, null);
        size = 1;
        modCount++;
        return null;
    }


    int cmp;
    Entry<K,V> parent;
    Comparator<? super K> cpr = comparator;
    if (cpr != null) {
        // 使用自定义比较器
        // 找到要插入的位置的父节点parent
        do {
            parent = t;
            cmp = cpr.compare(key, t.key);
            if (cmp < 0)
                t = t.left;
            else if (cmp > 0)
                t = t.right;
            else
                // key已存在，更新值后返回
                return t.setValue(value);
        } while (t != null);
    }
    else {
        // 使用键自然序
        if (key == null)
            throw new NullPointerException();
        @SuppressWarnings("unchecked")
            Comparable<? super K> k = (Comparable<? super K>) key;

        // 同样，找到要插入的位置的父节点parent
        do {
            parent = t;
            cmp = k.compareTo(t.key);
            if (cmp < 0)
                t = t.left;
            else if (cmp > 0)
                t = t.right;
            else
                return t.setValue(value);
        } while (t != null);
    }

    // 创建节点，插入对应位置
    Entry<K,V> e = new Entry<>(key, value, parent);
    if (cmp < 0)
        parent.left = e;
    else
        parent.right = e;

    // 插入后对树进行平衡
    fixAfterInsertion(e);
    size++;
    modCount++;
    return null;
}

/**
 * 根据给定的key删除对应的键值对
 */
public V remove(Object key) {

    // 找到对应的节点
    Entry<K,V> p = getEntry(key);
    if (p == null)
        // key不存在直接返回
        return null;

    // 删除节点
    V oldValue = p.value;
    deleteEntry(p);
    return oldValue;
}

/**
 * 清空map
 */
public void clear() {
    modCount++;
    size = 0;
    root = null;
}

/**
 * Returns a shallow copy of this {@code TreeMap} instance. (The keys and
 * values themselves are not cloned.)
 *
 * @return a shallow copy of this map
 */
public Object clone() {
    TreeMap<?,?> clone;
    try {
        clone = (TreeMap<?,?>) super.clone();
    } catch (CloneNotSupportedException e) {
        throw new InternalError(e);
    }

    // Put clone into "virgin" state (except for comparator)
    clone.root = null;
    clone.size = 0;
    clone.modCount = 0;
    clone.entrySet = null;
    clone.navigableKeySet = null;
    clone.descendingMap = null;

    // Initialize clone with our mappings
    try {
        clone.buildFromSorted(size, entrySet().iterator(), null, null);
    } catch (java.io.IOException cannotHappen) {
    } catch (ClassNotFoundException cannotHappen) {
    }

    return clone;
}

// NavigableMap API methods

/**
 * 查找最小的键对应的键值对
 */
public Map.Entry<K,V> firstEntry() {
    return exportEntry(getFirstEntry());
}

/**
 * 查找最大的键对应的键值对
 */
public Map.Entry<K,V> lastEntry() {
    return exportEntry(getLastEntry());
}

/**
 * 查找并移除最小的键对应的键值对
 */
public Map.Entry<K,V> pollFirstEntry() {
    Entry<K,V> p = getFirstEntry();
    Map.Entry<K,V> result = exportEntry(p);
    if (p != null)
        deleteEntry(p);
    return result;
}

/**
 * 查找并移除最大的键对应的键值对
 */
public Map.Entry<K,V> pollLastEntry() {
    Entry<K,V> p = getLastEntry();
    Map.Entry<K,V> result = exportEntry(p);
    if (p != null)
        deleteEntry(p);
    return result;
}

/**
 * 查找小于给定key的最大键对应的键值对
 */
public Map.Entry<K,V> lowerEntry(K key) {
    return exportEntry(getLowerEntry(key));
}

/**
 * 查找小于给定key的最大键
 */
public K lowerKey(K key) {
    return keyOrNull(getLowerEntry(key));
}

/**
 * 查找小于等于给定key的最大键对应的键值对
 */
public Map.Entry<K,V> floorEntry(K key) {
    return exportEntry(getFloorEntry(key));
}

/**
 * 查找小于等于给定key的最大键
 */
public K floorKey(K key) {
    return keyOrNull(getFloorEntry(key));
}

/**
 * 查找大于等于给定key的最小键对应的键值对
 */
public Map.Entry<K,V> ceilingEntry(K key) {
    return exportEntry(getCeilingEntry(key));
}

/**
 * 查找大于等于给定key的最小键
 */
public K ceilingKey(K key) {
    return keyOrNull(getCeilingEntry(key));
}

/**
 * 查找大于给定key的最小键对应的键值对
 */
public Map.Entry<K,V> higherEntry(K key) {
    return exportEntry(getHigherEntry(key));
}

/**
 * 查找大于给定key的最小键
 */
public K higherKey(K key) {
    return keyOrNull(getHigherEntry(key));
}

// Views

/**
 * Fields initialized to contain an instance of the entry set view
 * the first time this view is requested.  Views are stateless, so
 * there's no reason to create more than one.
 */
private transient EntrySet entrySet;
private transient KeySet<K> navigableKeySet;
private transient NavigableMap<K,V> descendingMap;

/**
 * Returns a {@link Set} view of the keys contained in this map.
 *
 * <p>The set's iterator returns the keys in ascending order.
 * The set's spliterator is
 * <em><a href="Spliterator.html#binding">late-binding</a></em>,
 * <em>fail-fast</em>, and additionally reports {@link Spliterator#SORTED}
 * and {@link Spliterator#ORDERED} with an encounter order that is ascending
 * key order.  The spliterator's comparator (see
 * {@link Spliterator#getComparator()}) is {@code null} if
 * the tree map's comparator (see {@link #comparator()}) is {@code null}.
 * Otherwise, the spliterator's comparator is the same as or imposes the
 * same total ordering as the tree map's comparator.
 *
 * <p>The set is backed by the map, so changes to the map are
 * reflected in the set, and vice-versa.  If the map is modified
 * while an iteration over the set is in progress (except through
 * the iterator's own {@code remove} operation), the results of
 * the iteration are undefined.  The set supports element removal,
 * which removes the corresponding mapping from the map, via the
 * {@code Iterator.remove}, {@code Set.remove},
 * {@code removeAll}, {@code retainAll}, and {@code clear}
 * operations.  It does not support the {@code add} or {@code addAll}
 * operations.
 */
public Set<K> keySet() {
    return navigableKeySet();
}

/**
 * @since 1.6
 */
public NavigableSet<K> navigableKeySet() {
    KeySet<K> nks = navigableKeySet;
    return (nks != null) ? nks : (navigableKeySet = new KeySet<>(this));
}

/**
 * @since 1.6
 */
public NavigableSet<K> descendingKeySet() {
    return descendingMap().navigableKeySet();
}

/**
 * Returns a {@link Collection} view of the values contained in this map.
 *
 * <p>The collection's iterator returns the values in ascending order
 * of the corresponding keys. The collection's spliterator is
 * <em><a href="Spliterator.html#binding">late-binding</a></em>,
 * <em>fail-fast</em>, and additionally reports {@link Spliterator#ORDERED}
 * with an encounter order that is ascending order of the corresponding
 * keys.
 *
 * <p>The collection is backed by the map, so changes to the map are
 * reflected in the collection, and vice-versa.  If the map is
 * modified while an iteration over the collection is in progress
 * (except through the iterator's own {@code remove} operation),
 * the results of the iteration are undefined.  The collection
 * supports element removal, which removes the corresponding
 * mapping from the map, via the {@code Iterator.remove},
 * {@code Collection.remove}, {@code removeAll},
 * {@code retainAll} and {@code clear} operations.  It does not
 * support the {@code add} or {@code addAll} operations.
 */
public Collection<V> values() {
    Collection<V> vs = values;
    if (vs == null) {
        vs = new Values();
        values = vs;
    }
    return vs;
}

/**
 * Returns a {@link Set} view of the mappings contained in this map.
 *
 * <p>The set's iterator returns the entries in ascending key order. The
 * sets's spliterator is
 * <em><a href="Spliterator.html#binding">late-binding</a></em>,
 * <em>fail-fast</em>, and additionally reports {@link Spliterator#SORTED} and
 * {@link Spliterator#ORDERED} with an encounter order that is ascending key
 * order.
 *
 * <p>The set is backed by the map, so changes to the map are
 * reflected in the set, and vice-versa.  If the map is modified
 * while an iteration over the set is in progress (except through
 * the iterator's own {@code remove} operation, or through the
 * {@code setValue} operation on a map entry returned by the
 * iterator) the results of the iteration are undefined.  The set
 * supports element removal, which removes the corresponding
 * mapping from the map, via the {@code Iterator.remove},
 * {@code Set.remove}, {@code removeAll}, {@code retainAll} and
 * {@code clear} operations.  It does not support the
 * {@code add} or {@code addAll} operations.
 */
public Set<Map.Entry<K,V>> entrySet() {
    EntrySet es = entrySet;
    return (es != null) ? es : (entrySet = new EntrySet());
}

/**
 * @since 1.6
 */
public NavigableMap<K, V> descendingMap() {
    NavigableMap<K, V> km = descendingMap;
    return (km != null) ? km :
        (descendingMap = new DescendingSubMap<>(this,
                                                true, null, true,
                                                true, null, true));
}

/**
 * @throws ClassCastException       {@inheritDoc}
 * @throws NullPointerException if {@code fromKey} or {@code toKey} is
 *         null and this map uses natural ordering, or its comparator
 *         does not permit null keys
 * @throws IllegalArgumentException {@inheritDoc}
 * @since 1.6
 */
public NavigableMap<K,V> subMap(K fromKey, boolean fromInclusive,
                                K toKey,   boolean toInclusive) {
    return new AscendingSubMap<>(this,
                                 false, fromKey, fromInclusive,
                                 false, toKey,   toInclusive);
}

/**
 * @throws ClassCastException       {@inheritDoc}
 * @throws NullPointerException if {@code toKey} is null
 *         and this map uses natural ordering, or its comparator
 *         does not permit null keys
 * @throws IllegalArgumentException {@inheritDoc}
 * @since 1.6
 */
public NavigableMap<K,V> headMap(K toKey, boolean inclusive) {
    return new AscendingSubMap<>(this,
                                 true,  null,  true,
                                 false, toKey, inclusive);
}

/**
 * @throws ClassCastException       {@inheritDoc}
 * @throws NullPointerException if {@code fromKey} is null
 *         and this map uses natural ordering, or its comparator
 *         does not permit null keys
 * @throws IllegalArgumentException {@inheritDoc}
 * @since 1.6
 */
public NavigableMap<K,V> tailMap(K fromKey, boolean inclusive) {
    return new AscendingSubMap<>(this,
                                 false, fromKey, inclusive,
                                 true,  null,    true);
}

/**
 * @throws ClassCastException       {@inheritDoc}
 * @throws NullPointerException if {@code fromKey} or {@code toKey} is
 *         null and this map uses natural ordering, or its comparator
 *         does not permit null keys
 * @throws IllegalArgumentException {@inheritDoc}
 */
public SortedMap<K,V> subMap(K fromKey, K toKey) {
    return subMap(fromKey, true, toKey, false);
}

/**
 * @throws ClassCastException       {@inheritDoc}
 * @throws NullPointerException if {@code toKey} is null
 *         and this map uses natural ordering, or its comparator
 *         does not permit null keys
 * @throws IllegalArgumentException {@inheritDoc}
 */
public SortedMap<K,V> headMap(K toKey) {
    return headMap(toKey, false);
}

/**
 * @throws ClassCastException       {@inheritDoc}
 * @throws NullPointerException if {@code fromKey} is null
 *         and this map uses natural ordering, or its comparator
 *         does not permit null keys
 * @throws IllegalArgumentException {@inheritDoc}
 */
public SortedMap<K,V> tailMap(K fromKey) {
    return tailMap(fromKey, true);
}

@Override
public boolean replace(K key, V oldValue, V newValue) {
    Entry<K,V> p = getEntry(key);
    if (p!=null && Objects.equals(oldValue, p.value)) {
        p.value = newValue;
        return true;
    }
    return false;
}

@Override
public V replace(K key, V value) {
    Entry<K,V> p = getEntry(key);
    if (p!=null) {
        V oldValue = p.value;
        p.value = value;
        return oldValue;
    }
    return null;
}

@Override
public void forEach(BiConsumer<? super K, ? super V> action) {
    Objects.requireNonNull(action);
    int expectedModCount = modCount;
    for (Entry<K, V> e = getFirstEntry(); e != null; e = successor(e)) {
        action.accept(e.key, e.value);

        if (expectedModCount != modCount) {
            throw new ConcurrentModificationException();
        }
    }
}

@Override
public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
    Objects.requireNonNull(function);
    int expectedModCount = modCount;

    for (Entry<K, V> e = getFirstEntry(); e != null; e = successor(e)) {
        e.value = function.apply(e.key, e.value);

        if (expectedModCount != modCount) {
            throw new ConcurrentModificationException();
        }
    }
}

// View class support

class Values extends AbstractCollection<V> {
    public Iterator<V> iterator() {
        return new ValueIterator(getFirstEntry());
    }

    public int size() {
        return TreeMap.this.size();
    }

    public boolean contains(Object o) {
        return TreeMap.this.containsValue(o);
    }

    public boolean remove(Object o) {
        for (Entry<K,V> e = getFirstEntry(); e != null; e = successor(e)) {
            if (valEquals(e.getValue(), o)) {
                deleteEntry(e);
                return true;
            }
        }
        return false;
    }

    public void clear() {
        TreeMap.this.clear();
    }

    public Spliterator<V> spliterator() {
        return new ValueSpliterator<K,V>(TreeMap.this, null, null, 0, -1, 0);
    }
}

class EntrySet extends AbstractSet<Map.Entry<K,V>> {
    public Iterator<Map.Entry<K,V>> iterator() {
        return new EntryIterator(getFirstEntry());
    }

    public boolean contains(Object o) {
        if (!(o instanceof Map.Entry))
            return false;
        Map.Entry<?,?> entry = (Map.Entry<?,?>) o;
        Object value = entry.getValue();
        Entry<K,V> p = getEntry(entry.getKey());
        return p != null && valEquals(p.getValue(), value);
    }

    public boolean remove(Object o) {
        if (!(o instanceof Map.Entry))
            return false;
        Map.Entry<?,?> entry = (Map.Entry<?,?>) o;
        Object value = entry.getValue();
        Entry<K,V> p = getEntry(entry.getKey());
        if (p != null && valEquals(p.getValue(), value)) {
            deleteEntry(p);
            return true;
        }
        return false;
    }

    public int size() {
        return TreeMap.this.size();
    }

    public void clear() {
        TreeMap.this.clear();
    }

    public Spliterator<Map.Entry<K,V>> spliterator() {
        return new EntrySpliterator<K,V>(TreeMap.this, null, null, 0, -1, 0);
    }
}

/*
 * Unlike Values and EntrySet, the KeySet class is static,
 * delegating to a NavigableMap to allow use by SubMaps, which
 * outweighs the ugliness of needing type-tests for the following
 * Iterator methods that are defined appropriately in main versus
 * submap classes.
 */

Iterator<K> keyIterator() {
    return new KeyIterator(getFirstEntry());
}

Iterator<K> descendingKeyIterator() {
    return new DescendingKeyIterator(getLastEntry());
}

static final class KeySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private final NavigableMap<E, ?> m;
    KeySet(NavigableMap<E,?> map) { m = map; }

    public Iterator<E> iterator() {
        if (m instanceof TreeMap)
            return ((TreeMap<E,?>)m).keyIterator();
        else
            return ((NavigableSubMap<E,?>)m).keyIterator();
    }

    public Iterator<E> descendingIterator() {
        if (m instanceof TreeMap)
            return ((TreeMap<E,?>)m).descendingKeyIterator();
        else
            return ((NavigableSubMap<E,?>)m).descendingKeyIterator();
    }

    public int size() { return m.size(); }
    public boolean isEmpty() { return m.isEmpty(); }
    public boolean contains(Object o) { return m.containsKey(o); }
    public void clear() { m.clear(); }
    public E lower(E e) { return m.lowerKey(e); }
    public E floor(E e) { return m.floorKey(e); }
    public E ceiling(E e) { return m.ceilingKey(e); }
    public E higher(E e) { return m.higherKey(e); }
    public E first() { return m.firstKey(); }
    public E last() { return m.lastKey(); }
    public Comparator<? super E> comparator() { return m.comparator(); }
    public E pollFirst() {
        Map.Entry<E,?> e = m.pollFirstEntry();
        return (e == null) ? null : e.getKey();
    }
    public E pollLast() {
        Map.Entry<E,?> e = m.pollLastEntry();
        return (e == null) ? null : e.getKey();
    }
    public boolean remove(Object o) {
        int oldSize = size();
        m.remove(o);
        return size() != oldSize;
    }
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive,
                                  E toElement,   boolean toInclusive) {
        return new KeySet<>(m.subMap(fromElement, fromInclusive,
                                      toElement,   toInclusive));
    }
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return new KeySet<>(m.headMap(toElement, inclusive));
    }
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return new KeySet<>(m.tailMap(fromElement, inclusive));
    }
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }
    public NavigableSet<E> descendingSet() {
        return new KeySet<>(m.descendingMap());
    }

    public Spliterator<E> spliterator() {
        return keySpliteratorFor(m);
    }
}

/**
 * Base class for TreeMap Iterators
 */
abstract class PrivateEntryIterator<T> implements Iterator<T> {
    Entry<K,V> next;
    Entry<K,V> lastReturned;
    int expectedModCount;

    PrivateEntryIterator(Entry<K,V> first) {
        expectedModCount = modCount;
        lastReturned = null;
        next = first;
    }

    public final boolean hasNext() {
        return next != null;
    }

    final Entry<K,V> nextEntry() {
        Entry<K,V> e = next;
        if (e == null)
            throw new NoSuchElementException();
        if (modCount != expectedModCount)
            throw new ConcurrentModificationException();
        next = successor(e);
        lastReturned = e;
        return e;
    }

    final Entry<K,V> prevEntry() {
        Entry<K,V> e = next;
        if (e == null)
            throw new NoSuchElementException();
        if (modCount != expectedModCount)
            throw new ConcurrentModificationException();
        next = predecessor(e);
        lastReturned = e;
        return e;
    }

    public void remove() {
        if (lastReturned == null)
            throw new IllegalStateException();
        if (modCount != expectedModCount)
            throw new ConcurrentModificationException();
        // deleted entries are replaced by their successors
        if (lastReturned.left != null && lastReturned.right != null)
            next = lastReturned;
        deleteEntry(lastReturned);
        expectedModCount = modCount;
        lastReturned = null;
    }
}

final class EntryIterator extends PrivateEntryIterator<Map.Entry<K,V>> {
    EntryIterator(Entry<K,V> first) {
        super(first);
    }
    public Map.Entry<K,V> next() {
        return nextEntry();
    }
}

final class ValueIterator extends PrivateEntryIterator<V> {
    ValueIterator(Entry<K,V> first) {
        super(first);
    }
    public V next() {
        return nextEntry().value;
    }
}

final class KeyIterator extends PrivateEntryIterator<K> {
    KeyIterator(Entry<K,V> first) {
        super(first);
    }
    public K next() {
        return nextEntry().key;
    }
}

final class DescendingKeyIterator extends PrivateEntryIterator<K> {
    DescendingKeyIterator(Entry<K,V> first) {
        super(first);
    }
    public K next() {
        return prevEntry().key;
    }
    public void remove() {
        if (lastReturned == null)
            throw new IllegalStateException();
        if (modCount != expectedModCount)
            throw new ConcurrentModificationException();
        deleteEntry(lastReturned);
        lastReturned = null;
        expectedModCount = modCount;
    }
}

// Little utilities

/**
 * 比较两个元素
 * 若设置了比较器，则使用设置的比较器，否则使用元素自然序
 */
@SuppressWarnings("unchecked")
final int compare(Object k1, Object k2) {
    return comparator==null ? ((Comparable<? super K>)k1).compareTo((K)k2)
        : comparator.compare((K)k1, (K)k2);
}

/**
 * 判断俩元素是否相同，o1=o2=null或o1 equals o2
 */
static final boolean valEquals(Object o1, Object o2) {
    return (o1==null ? o2==null : o1.equals(o2));
}

/**
 * 将entry包装为不可变对象
 */
static <K,V> Map.Entry<K,V> exportEntry(Entry<K,V> e) {
    return (e == null) ? null :
        new SimpleImmutableEntry<>(e);
}

/**
 * 返回entry节点对应的键
 */
static <K,V> K keyOrNull(Entry<K,V> e) {
    return (e == null) ? null : e.key;
}

/**
 * 返回节点的key，key不存在抛出NoSuchElementException异常
 */
static <K> K key(Entry<K,?> e) {
    if (e==null)
        throw new NoSuchElementException();
    return e.key;
}


// SubMaps

/**
 * Dummy value serving as unmatchable fence key for unbounded
 * SubMapIterators
 */
private static final Object UNBOUNDED = new Object();

/**
 * @serial include
 */
abstract static class NavigableSubMap<K,V> extends AbstractMap<K,V>
    implements NavigableMap<K,V>, Serializable {
    private static final long serialVersionUID = -2102997345730753016L;
    /**
     * The backing map.
     */
    final TreeMap<K,V> m;

    /**
     * Endpoints are represented as triples (fromStart, lo,
     * loInclusive) and (toEnd, hi, hiInclusive). If fromStart is
     * true, then the low (absolute) bound is the start of the
     * backing map, and the other values are ignored. Otherwise,
     * if loInclusive is true, lo is the inclusive bound, else lo
     * is the exclusive bound. Similarly for the upper bound.
     */
    final K lo, hi;
    final boolean fromStart, toEnd;
    final boolean loInclusive, hiInclusive;

    NavigableSubMap(TreeMap<K,V> m,
                    boolean fromStart, K lo, boolean loInclusive,
                    boolean toEnd,     K hi, boolean hiInclusive) {
        if (!fromStart && !toEnd) {
            if (m.compare(lo, hi) > 0)
                throw new IllegalArgumentException("fromKey > toKey");
        } else {
            if (!fromStart) // type check
                m.compare(lo, lo);
            if (!toEnd)
                m.compare(hi, hi);
        }

        this.m = m;
        this.fromStart = fromStart;
        this.lo = lo;
        this.loInclusive = loInclusive;
        this.toEnd = toEnd;
        this.hi = hi;
        this.hiInclusive = hiInclusive;
    }

    // internal utilities

    final boolean tooLow(Object key) {
        if (!fromStart) {
            int c = m.compare(key, lo);
            if (c < 0 || (c == 0 && !loInclusive))
                return true;
        }
        return false;
    }

    final boolean tooHigh(Object key) {
        if (!toEnd) {
            int c = m.compare(key, hi);
            if (c > 0 || (c == 0 && !hiInclusive))
                return true;
        }
        return false;
    }

    final boolean inRange(Object key) {
        return !tooLow(key) && !tooHigh(key);
    }

    final boolean inClosedRange(Object key) {
        return (fromStart || m.compare(key, lo) >= 0)
            && (toEnd || m.compare(hi, key) >= 0);
    }

    final boolean inRange(Object key, boolean inclusive) {
        return inclusive ? inRange(key) : inClosedRange(key);
    }

    /*
     * Absolute versions of relation operations.
     * Subclasses map to these using like-named "sub"
     * versions that invert senses for descending maps
     */

    final TreeMap.Entry<K,V> absLowest() {
        TreeMap.Entry<K,V> e =
            (fromStart ?  m.getFirstEntry() :
             (loInclusive ? m.getCeilingEntry(lo) :
                            m.getHigherEntry(lo)));
        return (e == null || tooHigh(e.key)) ? null : e;
    }

    final TreeMap.Entry<K,V> absHighest() {
        TreeMap.Entry<K,V> e =
            (toEnd ?  m.getLastEntry() :
             (hiInclusive ?  m.getFloorEntry(hi) :
                             m.getLowerEntry(hi)));
        return (e == null || tooLow(e.key)) ? null : e;
    }

    final TreeMap.Entry<K,V> absCeiling(K key) {
        if (tooLow(key))
            return absLowest();
        TreeMap.Entry<K,V> e = m.getCeilingEntry(key);
        return (e == null || tooHigh(e.key)) ? null : e;
    }

    final TreeMap.Entry<K,V> absHigher(K key) {
        if (tooLow(key))
            return absLowest();
        TreeMap.Entry<K,V> e = m.getHigherEntry(key);
        return (e == null || tooHigh(e.key)) ? null : e;
    }

    final TreeMap.Entry<K,V> absFloor(K key) {
        if (tooHigh(key))
            return absHighest();
        TreeMap.Entry<K,V> e = m.getFloorEntry(key);
        return (e == null || tooLow(e.key)) ? null : e;
    }

    final TreeMap.Entry<K,V> absLower(K key) {
        if (tooHigh(key))
            return absHighest();
        TreeMap.Entry<K,V> e = m.getLowerEntry(key);
        return (e == null || tooLow(e.key)) ? null : e;
    }

    /** Returns the absolute high fence for ascending traversal */
    final TreeMap.Entry<K,V> absHighFence() {
        return (toEnd ? null : (hiInclusive ?
                                m.getHigherEntry(hi) :
                                m.getCeilingEntry(hi)));
    }

    /** Return the absolute low fence for descending traversal  */
    final TreeMap.Entry<K,V> absLowFence() {
        return (fromStart ? null : (loInclusive ?
                                    m.getLowerEntry(lo) :
                                    m.getFloorEntry(lo)));
    }

    // Abstract methods defined in ascending vs descending classes
    // These relay to the appropriate absolute versions

    abstract TreeMap.Entry<K,V> subLowest();
    abstract TreeMap.Entry<K,V> subHighest();
    abstract TreeMap.Entry<K,V> subCeiling(K key);
    abstract TreeMap.Entry<K,V> subHigher(K key);
    abstract TreeMap.Entry<K,V> subFloor(K key);
    abstract TreeMap.Entry<K,V> subLower(K key);

    /** Returns ascending iterator from the perspective of this submap */
    abstract Iterator<K> keyIterator();

    abstract Spliterator<K> keySpliterator();

    /** Returns descending iterator from the perspective of this submap */
    abstract Iterator<K> descendingKeyIterator();

    // public methods

    public boolean isEmpty() {
        return (fromStart && toEnd) ? m.isEmpty() : entrySet().isEmpty();
    }

    public int size() {
        return (fromStart && toEnd) ? m.size() : entrySet().size();
    }

    public final boolean containsKey(Object key) {
        return inRange(key) && m.containsKey(key);
    }

    public final V put(K key, V value) {
        if (!inRange(key))
            throw new IllegalArgumentException("key out of range");
        return m.put(key, value);
    }

    public final V get(Object key) {
        return !inRange(key) ? null :  m.get(key);
    }

    public final V remove(Object key) {
        return !inRange(key) ? null : m.remove(key);
    }

    public final Entry<K,V> ceilingEntry(K key) {
        return exportEntry(subCeiling(key));
    }

    public final K ceilingKey(K key) {
        return keyOrNull(subCeiling(key));
    }

    public final Entry<K,V> higherEntry(K key) {
        return exportEntry(subHigher(key));
    }

    public final K higherKey(K key) {
        return keyOrNull(subHigher(key));
    }

    public final Entry<K,V> floorEntry(K key) {
        return exportEntry(subFloor(key));
    }

    public final K floorKey(K key) {
        return keyOrNull(subFloor(key));
    }

    public final Entry<K,V> lowerEntry(K key) {
        return exportEntry(subLower(key));
    }

    public final K lowerKey(K key) {
        return keyOrNull(subLower(key));
    }

    public final K firstKey() {
        return key(subLowest());
    }

    public final K lastKey() {
        return key(subHighest());
    }

    public final Entry<K,V> firstEntry() {
        return exportEntry(subLowest());
    }

    public final Entry<K,V> lastEntry() {
        return exportEntry(subHighest());
    }

    public final Entry<K,V> pollFirstEntry() {
        TreeMap.Entry<K,V> e = subLowest();
        Entry<K,V> result = exportEntry(e);
        if (e != null)
            m.deleteEntry(e);
        return result;
    }

    public final Entry<K,V> pollLastEntry() {
        TreeMap.Entry<K,V> e = subHighest();
        Entry<K,V> result = exportEntry(e);
        if (e != null)
            m.deleteEntry(e);
        return result;
    }

    // Views
    transient NavigableMap<K,V> descendingMapView;
    transient EntrySetView entrySetView;
    transient KeySet<K> navigableKeySetView;

    public final NavigableSet<K> navigableKeySet() {
        KeySet<K> nksv = navigableKeySetView;
        return (nksv != null) ? nksv :
            (navigableKeySetView = new KeySet<>(this));
    }

    public final Set<K> keySet() {
        return navigableKeySet();
    }

    public NavigableSet<K> descendingKeySet() {
        return descendingMap().navigableKeySet();
    }

    public final SortedMap<K,V> subMap(K fromKey, K toKey) {
        return subMap(fromKey, true, toKey, false);
    }

    public final SortedMap<K,V> headMap(K toKey) {
        return headMap(toKey, false);
    }

    public final SortedMap<K,V> tailMap(K fromKey) {
        return tailMap(fromKey, true);
    }

    // View classes

    abstract class EntrySetView extends AbstractSet<Entry<K,V>> {
        private transient int size = -1, sizeModCount;

        public int size() {
            if (fromStart && toEnd)
                return m.size();
            if (size == -1 || sizeModCount != m.modCount) {
                sizeModCount = m.modCount;
                size = 0;
                Iterator<?> i = iterator();
                while (i.hasNext()) {
                    size++;
                    i.next();
                }
            }
            return size;
        }

        public boolean isEmpty() {
            TreeMap.Entry<K,V> n = absLowest();
            return n == null || tooHigh(n.key);
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Entry<?,?> entry = (Entry<?,?>) o;
            Object key = entry.getKey();
            if (!inRange(key))
                return false;
            TreeMap.Entry<?,?> node = m.getEntry(key);
            return node != null &&
                valEquals(node.getValue(), entry.getValue());
        }

        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Entry<?,?> entry = (Entry<?,?>) o;
            Object key = entry.getKey();
            if (!inRange(key))
                return false;
            TreeMap.Entry<K,V> node = m.getEntry(key);
            if (node!=null && valEquals(node.getValue(),
                                        entry.getValue())) {
                m.deleteEntry(node);
                return true;
            }
            return false;
        }
    }

    /**
     * Iterators for SubMaps
     */
    abstract class SubMapIterator<T> implements Iterator<T> {
        TreeMap.Entry<K,V> lastReturned;
        TreeMap.Entry<K,V> next;
        final Object fenceKey;
        int expectedModCount;

        SubMapIterator(TreeMap.Entry<K,V> first,
                       TreeMap.Entry<K,V> fence) {
            expectedModCount = m.modCount;
            lastReturned = null;
            next = first;
            fenceKey = fence == null ? UNBOUNDED : fence.key;
        }

        public final boolean hasNext() {
            return next != null && next.key != fenceKey;
        }

        final TreeMap.Entry<K,V> nextEntry() {
            TreeMap.Entry<K,V> e = next;
            if (e == null || e.key == fenceKey)
                throw new NoSuchElementException();
            if (m.modCount != expectedModCount)
                throw new ConcurrentModificationException();
            next = successor(e);
            lastReturned = e;
            return e;
        }

        final TreeMap.Entry<K,V> prevEntry() {
            TreeMap.Entry<K,V> e = next;
            if (e == null || e.key == fenceKey)
                throw new NoSuchElementException();
            if (m.modCount != expectedModCount)
                throw new ConcurrentModificationException();
            next = predecessor(e);
            lastReturned = e;
            return e;
        }

        final void removeAscending() {
            if (lastReturned == null)
                throw new IllegalStateException();
            if (m.modCount != expectedModCount)
                throw new ConcurrentModificationException();
            // deleted entries are replaced by their successors
            if (lastReturned.left != null && lastReturned.right != null)
                next = lastReturned;
            m.deleteEntry(lastReturned);
            lastReturned = null;
            expectedModCount = m.modCount;
        }

        final void removeDescending() {
            if (lastReturned == null)
                throw new IllegalStateException();
            if (m.modCount != expectedModCount)
                throw new ConcurrentModificationException();
            m.deleteEntry(lastReturned);
            lastReturned = null;
            expectedModCount = m.modCount;
        }

    }

    final class SubMapEntryIterator extends SubMapIterator<Entry<K,V>> {
        SubMapEntryIterator(TreeMap.Entry<K,V> first,
                            TreeMap.Entry<K,V> fence) {
            super(first, fence);
        }
        public Entry<K,V> next() {
            return nextEntry();
        }
        public void remove() {
            removeAscending();
        }
    }

    final class DescendingSubMapEntryIterator extends SubMapIterator<Entry<K,V>> {
        DescendingSubMapEntryIterator(TreeMap.Entry<K,V> last,
                                      TreeMap.Entry<K,V> fence) {
            super(last, fence);
        }

        public Entry<K,V> next() {
            return prevEntry();
        }
        public void remove() {
            removeDescending();
        }
    }

    // Implement minimal Spliterator as KeySpliterator backup
    final class SubMapKeyIterator extends SubMapIterator<K>
        implements Spliterator<K> {
        SubMapKeyIterator(TreeMap.Entry<K,V> first,
                          TreeMap.Entry<K,V> fence) {
            super(first, fence);
        }
        public K next() {
            return nextEntry().key;
        }
        public void remove() {
            removeAscending();
        }
        public Spliterator<K> trySplit() {
            return null;
        }
        public void forEachRemaining(Consumer<? super K> action) {
            while (hasNext())
                action.accept(next());
        }
        public boolean tryAdvance(Consumer<? super K> action) {
            if (hasNext()) {
                action.accept(next());
                return true;
            }
            return false;
        }
        public long estimateSize() {
            return Long.MAX_VALUE;
        }
        public int characteristics() {
            return Spliterator.DISTINCT | Spliterator.ORDERED |
                Spliterator.SORTED;
        }
        public final Comparator<? super K>  getComparator() {
            return NavigableSubMap.this.comparator();
        }
    }

    final class DescendingSubMapKeyIterator extends SubMapIterator<K>
        implements Spliterator<K> {
        DescendingSubMapKeyIterator(TreeMap.Entry<K,V> last,
                                    TreeMap.Entry<K,V> fence) {
            super(last, fence);
        }
        public K next() {
            return prevEntry().key;
        }
        public void remove() {
            removeDescending();
        }
        public Spliterator<K> trySplit() {
            return null;
        }
        public void forEachRemaining(Consumer<? super K> action) {
            while (hasNext())
                action.accept(next());
        }
        public boolean tryAdvance(Consumer<? super K> action) {
            if (hasNext()) {
                action.accept(next());
                return true;
            }
            return false;
        }
        public long estimateSize() {
            return Long.MAX_VALUE;
        }
        public int characteristics() {
            return Spliterator.DISTINCT | Spliterator.ORDERED;
        }
    }
}

/**
 * @serial include
 */
static final class AscendingSubMap<K,V> extends NavigableSubMap<K,V> {
    private static final long serialVersionUID = 912986545866124060L;

    AscendingSubMap(TreeMap<K,V> m,
                    boolean fromStart, K lo, boolean loInclusive,
                    boolean toEnd,     K hi, boolean hiInclusive) {
        super(m, fromStart, lo, loInclusive, toEnd, hi, hiInclusive);
    }

    public Comparator<? super K> comparator() {
        return m.comparator();
    }

    public NavigableMap<K,V> subMap(K fromKey, boolean fromInclusive,
                                    K toKey,   boolean toInclusive) {
        if (!inRange(fromKey, fromInclusive))
            throw new IllegalArgumentException("fromKey out of range");
        if (!inRange(toKey, toInclusive))
            throw new IllegalArgumentException("toKey out of range");
        return new AscendingSubMap<>(m,
                                     false, fromKey, fromInclusive,
                                     false, toKey,   toInclusive);
    }

    public NavigableMap<K,V> headMap(K toKey, boolean inclusive) {
        if (!inRange(toKey, inclusive))
            throw new IllegalArgumentException("toKey out of range");
        return new AscendingSubMap<>(m,
                                     fromStart, lo,    loInclusive,
                                     false,     toKey, inclusive);
    }

    public NavigableMap<K,V> tailMap(K fromKey, boolean inclusive) {
        if (!inRange(fromKey, inclusive))
            throw new IllegalArgumentException("fromKey out of range");
        return new AscendingSubMap<>(m,
                                     false, fromKey, inclusive,
                                     toEnd, hi,      hiInclusive);
    }

    public NavigableMap<K,V> descendingMap() {
        NavigableMap<K,V> mv = descendingMapView;
        return (mv != null) ? mv :
            (descendingMapView =
             new DescendingSubMap<>(m,
                                    fromStart, lo, loInclusive,
                                    toEnd,     hi, hiInclusive));
    }

    Iterator<K> keyIterator() {
        return new SubMapKeyIterator(absLowest(), absHighFence());
    }

    Spliterator<K> keySpliterator() {
        return new SubMapKeyIterator(absLowest(), absHighFence());
    }

    Iterator<K> descendingKeyIterator() {
        return new DescendingSubMapKeyIterator(absHighest(), absLowFence());
    }

    final class AscendingEntrySetView extends EntrySetView {
        public Iterator<Entry<K,V>> iterator() {
            return new SubMapEntryIterator(absLowest(), absHighFence());
        }
    }

    public Set<Entry<K,V>> entrySet() {
        EntrySetView es = entrySetView;
        return (es != null) ? es : (entrySetView = new AscendingEntrySetView());
    }

    TreeMap.Entry<K,V> subLowest()       { return absLowest(); }
    TreeMap.Entry<K,V> subHighest()      { return absHighest(); }
    TreeMap.Entry<K,V> subCeiling(K key) { return absCeiling(key); }
    TreeMap.Entry<K,V> subHigher(K key)  { return absHigher(key); }
    TreeMap.Entry<K,V> subFloor(K key)   { return absFloor(key); }
    TreeMap.Entry<K,V> subLower(K key)   { return absLower(key); }
}

/**
 * @serial include
 */
static final class DescendingSubMap<K,V>  extends NavigableSubMap<K,V> {
    private static final long serialVersionUID = 912986545866120460L;
    DescendingSubMap(TreeMap<K,V> m,
                    boolean fromStart, K lo, boolean loInclusive,
                    boolean toEnd,     K hi, boolean hiInclusive) {
        super(m, fromStart, lo, loInclusive, toEnd, hi, hiInclusive);
    }

    private final Comparator<? super K> reverseComparator =
        Collections.reverseOrder(m.comparator);

    public Comparator<? super K> comparator() {
        return reverseComparator;
    }

    public NavigableMap<K,V> subMap(K fromKey, boolean fromInclusive,
                                    K toKey,   boolean toInclusive) {
        if (!inRange(fromKey, fromInclusive))
            throw new IllegalArgumentException("fromKey out of range");
        if (!inRange(toKey, toInclusive))
            throw new IllegalArgumentException("toKey out of range");
        return new DescendingSubMap<>(m,
                                      false, toKey,   toInclusive,
                                      false, fromKey, fromInclusive);
    }

    public NavigableMap<K,V> headMap(K toKey, boolean inclusive) {
        if (!inRange(toKey, inclusive))
            throw new IllegalArgumentException("toKey out of range");
        return new DescendingSubMap<>(m,
                                      false, toKey, inclusive,
                                      toEnd, hi,    hiInclusive);
    }

    public NavigableMap<K,V> tailMap(K fromKey, boolean inclusive) {
        if (!inRange(fromKey, inclusive))
            throw new IllegalArgumentException("fromKey out of range");
        return new DescendingSubMap<>(m,
                                      fromStart, lo, loInclusive,
                                      false, fromKey, inclusive);
    }

    public NavigableMap<K,V> descendingMap() {
        NavigableMap<K,V> mv = descendingMapView;
        return (mv != null) ? mv :
            (descendingMapView =
             new AscendingSubMap<>(m,
                                   fromStart, lo, loInclusive,
                                   toEnd,     hi, hiInclusive));
    }

    Iterator<K> keyIterator() {
        return new DescendingSubMapKeyIterator(absHighest(), absLowFence());
    }

    Spliterator<K> keySpliterator() {
        return new DescendingSubMapKeyIterator(absHighest(), absLowFence());
    }

    Iterator<K> descendingKeyIterator() {
        return new SubMapKeyIterator(absLowest(), absHighFence());
    }

    final class DescendingEntrySetView extends EntrySetView {
        public Iterator<Entry<K,V>> iterator() {
            return new DescendingSubMapEntryIterator(absHighest(), absLowFence());
        }
    }

    public Set<Entry<K,V>> entrySet() {
        EntrySetView es = entrySetView;
        return (es != null) ? es : (entrySetView = new DescendingEntrySetView());
    }

    TreeMap.Entry<K,V> subLowest()       { return absHighest(); }
    TreeMap.Entry<K,V> subHighest()      { return absLowest(); }
    TreeMap.Entry<K,V> subCeiling(K key) { return absFloor(key); }
    TreeMap.Entry<K,V> subHigher(K key)  { return absLower(key); }
    TreeMap.Entry<K,V> subFloor(K key)   { return absCeiling(key); }
    TreeMap.Entry<K,V> subLower(K key)   { return absHigher(key); }
}

/**
 * This class exists solely for the sake of serialization
 * compatibility with previous releases of TreeMap that did not
 * support NavigableMap.  It translates an old-version SubMap into
 * a new-version AscendingSubMap. This class is never otherwise
 * used.
 *
 * @serial include
 */
private class SubMap extends AbstractMap<K,V>
    implements SortedMap<K,V>, Serializable {
    private static final long serialVersionUID = -6520786458950516097L;
    private boolean fromStart = false, toEnd = false;
    private K fromKey, toKey;
    private Object readResolve() {
        return new AscendingSubMap<>(TreeMap.this,
                                     fromStart, fromKey, true,
                                     toEnd, toKey, false);
    }
    public Set<Entry<K,V>> entrySet() { throw new InternalError(); }
    public K lastKey() { throw new InternalError(); }
    public K firstKey() { throw new InternalError(); }
    public SortedMap<K,V> subMap(K fromKey, K toKey) { throw new InternalError(); }
    public SortedMap<K,V> headMap(K toKey) { throw new InternalError(); }
    public SortedMap<K,V> tailMap(K fromKey) { throw new InternalError(); }
    public Comparator<? super K> comparator() { throw new InternalError(); }
}


// Red-black mechanics

private static final boolean RED   = false;
private static final boolean BLACK = true;

/**
 * 红黑树节点
 */

static final class Entry<K,V> implements Map.Entry<K,V> {
    K key;
    V value;
    Entry<K,V> left;
    Entry<K,V> right;
    Entry<K,V> parent;
    boolean color = BLACK;

    /**
     * Make a new cell with given key, value, and parent, and with
     * {@code null} child links, and BLACK color.
     */
    Entry(K key, V value, Entry<K,V> parent) {
        this.key = key;
        this.value = value;
        this.parent = parent;
    }

    /**
     * Returns the key.
     *
     * @return the key
     */
    public K getKey() {
        return key;
    }

    /**
     * Returns the value associated with the key.
     *
     * @return the value associated with the key
     */
    public V getValue() {
        return value;
    }

    /**
     * Replaces the value currently associated with the key with the given
     * value.
     *
     * @return the value associated with the key before this method was
     *         called
     */
    public V setValue(V value) {
        V oldValue = this.value;
        this.value = value;
        return oldValue;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Map.Entry))
            return false;
        Map.Entry<?,?> e = (Map.Entry<?,?>)o;

        return valEquals(key,e.getKey()) && valEquals(value,e.getValue());
    }

    public int hashCode() {
        int keyHash = (key==null ? 0 : key.hashCode());
        int valueHash = (value==null ? 0 : value.hashCode());
        return keyHash ^ valueHash;
    }

    public String toString() {
        return key + "=" + value;
    }
}

/**
 * 返回最小的元素
 */
final Entry<K,V> getFirstEntry() {
    Entry<K,V> p = root;
    if (p != null)
        // 递归左孩子，直到整棵树最左边的那个节点，此节点为最小节点
        while (p.left != null)
            p = p.left;
    return p;
}

/**
 * 返回最大的元素
 */
final Entry<K,V> getLastEntry() {
    Entry<K,V> p = root;
    if (p != null)
        // 递归右孩子，直到整棵树最右边的那个节点，此节点为最大节点
        while (p.right != null)
            p = p.right;
    return p;
}

/**
 * 返回按中序遍历顺序传入节点的下一个节点
 * 中序遍历：左孩子->当前节点->右孩子
 */
static <K,V> Entry<K,V> successor(Entry<K,V> t) {
    if (t == null)
        return null;
    else if (t.right != null) {
        // 传入节点的右子树不为空，则返回右子树最小的节点
        // 例如传入D，查找下一个元素
        //        D(t)
        //       / \
        //      B   F
        //     / \ / \
        //    A C E(p) F
        //
        // 最终返回E
        Entry<K,V> p = t.right;
        while (p.left != null)
            p = p.left;
        return p;
    } else {
        // 传入节点没有右子树，返回传入节点所在树为左子树的第一个根节点
        // 例如传入C，查找下一个元素
        //         D(p)
        //        / \
        //       B   E
        //      / \
        //     A  C(t)
        //
        // 最终返回D
        Entry<K,V> p = t.parent;
        Entry<K,V> ch = t;
        while (p != null && ch == p.right) {
            ch = p;
            p = p.parent;
        }
        return p;
    }
}

/**
 * Returns the predecessor of the specified Entry, or null if no such.
 */
static <K,V> Entry<K,V> predecessor(Entry<K,V> t) {
    if (t == null)
        return null;
    else if (t.left != null) {
        Entry<K,V> p = t.left;
        while (p.right != null)
            p = p.right;
        return p;
    } else {
        Entry<K,V> p = t.parent;
        Entry<K,V> ch = t;
        while (p != null && ch == p.left) {
            ch = p;
            p = p.parent;
        }
        return p;
    }
}

/**
 * Balancing operations.
 *
 * Implementations of rebalancings during insertion and deletion are
 * slightly different than the CLR version.  Rather than using dummy
 * nilnodes, we use a set of accessors that deal properly with null.  They
 * are used to avoid messiness surrounding nullness checks in the main
 * algorithms.
 */

/**
 * 返回节点的颜色，null节点返回黑色
 */
private static <K,V> boolean colorOf(Entry<K,V> p) {
    return (p == null ? BLACK : p.color);
}

/**
 * 返回给定节点的父节点
 */
private static <K,V> Entry<K,V> parentOf(Entry<K,V> p) {
    return (p == null ? null: p.parent);
}

/**
 * 设置节点颜色
 */
private static <K,V> void setColor(Entry<K,V> p, boolean c) {
    if (p != null)
        p.color = c;
}

/**
 * 返回给定节点的左孩子
 */
private static <K,V> Entry<K,V> leftOf(Entry<K,V> p) {
    return (p == null) ? null: p.left;
}

/**
 * 返回给定节点的右孩子
 */
private static <K,V> Entry<K,V> rightOf(Entry<K,V> p) {
    return (p == null) ? null: p.right;
}

/**
 * 左旋
 * 左旋操作不会破坏二叉搜索树的性质
 *
 * 例如：
 *         D
 *        / \
 *       B   F
 *      / \ / \
 *     A  C E G
 *
 * 经旋转后变为：
 *         F
 *        / \
 *       D   G
 *      / \
 *     B   E
 *    / \
 *   A  C
 */
private void rotateLeft(Entry<K,V> p) {
    if (p != null) {
        // r是p节点右孩子
        Entry<K,V> r = p.right;

        // 将p的右指针指向r的左子树
        p.right = r.left;
        if (r.left != null)
            r.left.parent = p;

        // r的父节点设置为p的父节点
        r.parent = p.parent;

        if (p.parent == null)
            // p节点是根节点，将根节点设置为r
            root = r;
        else if (p.parent.left == p)
            // p是其父节点的左孩子，将其父节点的左指针指向r
            p.parent.left = r;
        else
            // p是其父节点的右孩子，将其父节点的右指针指向r
            p.parent.right = r;

        // r的左指针指向p，p成为r的左孩子
        r.left = p;
        p.parent = r;
    }
}

/**
 * 右旋
 * 右旋操作不会破坏二叉搜索树的性质
 *
 * 例如：
 *         D
 *        / \
 *       B   F
 *      / \ / \
 *     A  C E G
 *
 * 经旋转后变为：
 *         B
 *        / \
 *       A   D
 *          / \
 *         C  F
 *           / \
 *          E  G
 */
private void rotateRight(Entry<K,V> p) {
    if (p != null) {
        // l是p节点的左孩子
        Entry<K,V> l = p.left;

        // 将p的左指针指向l的右子树
        p.left = l.right;
        if (l.right != null) l.right.parent = p;

        // l的父节点设置为p的父节点
        l.parent = p.parent;

        if (p.parent == null)
            // p节点是根节点，将根节点设置为l
            root = l;
        else if (p.parent.right == p)
            // p是其父节点的右孩子，将其父节点的右指针指向l
            p.parent.right = l;
        else
            // p是其父节点的左孩子，将其父节点的左指针指向l
            p.parent.left = l;

        // l的右指针指向p，p成为l的右孩子
        l.right = p;
        p.parent = l;
    }
}

/**
 * 插入节点后使树重新平衡
 * 算法导论中的平衡步骤
 * 详细推导可查看算法导论13.3章节
 */
private void fixAfterInsertion(Entry<K,V> x) {
    // 插入节点颜色设置为红色
    x.color = RED;

    // 如果路径上有两个连续节点都是红色，则进行处理
    // 根节点不需要处理
    while (x != null && x != root && x.parent.color == RED) {

        if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
            // 如果节点x的父节点p是其父节点的左孩子，如下两种情况
            //       祖父                祖父
            //       /                   /
            //     父节点      或       父节点
            //     /                      \
            //   节点x                    节点x

            // 节点y是x的叔叔节点
            Entry<K,V> y = rightOf(parentOf(parentOf(x)));

            if (colorOf(y) == RED) {
                // 情况一：x的叔叔y是红色的
                //       祖父(黑色)
                //      /   \
                //  p(红色)  y(红色)
                //    /
                //  x(红色)
                //
                // 将父节点和父节点的兄弟节点都设置为黑色
                // 祖父节点设置为红色
                // 调整为
                //       祖父(红色)
                //      /   \
                //  p(黑色)  y(黑色)
                //    /
                //  x(红色)
                //
                // 因为祖父节点设置成了红色，可能其父节点也是红色，
                // 所以需要将x设置为祖父节点，进行下一次检查处理
                // 如果祖父节点是根节点，那么方法最后会将根节点设置为黑色
                setColor(parentOf(x), BLACK);
                setColor(y, BLACK);
                setColor(parentOf(parentOf(x)), RED);
                x = parentOf(parentOf(x));
            } else {
                // 情况二：x的叔叔y是黑色的，而且x是右孩子
                //       祖父(黑色)
                //        /    \
                //    p(红色)  y(黑色)
                //       \
                //       x(红色)
                //
                // 如果x是p的右孩子，则对p进行左旋，使其变为如下结构
                //       祖父(黑色)
                //        /    \
                //    p(红色)  y(黑色)
                //      /
                //   x(红色)
                //
                // 此时转变为情况三：x的叔叔y是黑色的，而且x是左孩子
                //
                // 将x的父节点设置为黑色，祖父节点设置为红色，如下
                //       祖父(红色)
                //        /    \
                //    p(黑色)  y(黑色)
                //      /
                //   x(红色)
                // 然后对祖父节点进行右旋，使其变为如下结构
                //        新父p(黑色)
                //        /    \
                //    x(红色)  原祖父(红色)
                //              \
                //              y(黑色)
                // 旋转后的新父节点是黑色，不需要向上处理了，调整完成
                if (x == rightOf(parentOf(x))) {
                    x = parentOf(x);
                    rotateLeft(x);
                }
                setColor(parentOf(x), BLACK);
                setColor(parentOf(parentOf(x)), RED);
                rotateRight(parentOf(parentOf(x)));
            }
        } else {
            // 如果节点x的父节点p是其父节点的左孩子，逻辑与上面类似，互相对称，不再详细说明
            Entry<K,V> y = leftOf(parentOf(parentOf(x)));
            if (colorOf(y) == RED) {
                setColor(parentOf(x), BLACK);
                setColor(y, BLACK);
                setColor(parentOf(parentOf(x)), RED);
                x = parentOf(parentOf(x));
            } else {
                if (x == leftOf(parentOf(x))) {
                    x = parentOf(x);
                    rotateRight(x);
                }
                setColor(parentOf(x), BLACK);
                setColor(parentOf(parentOf(x)), RED);
                rotateLeft(parentOf(parentOf(x)));
            }
        }
    }

    // 调整后根节点可能变成红色，需要将其重新设置为黑色
    root.color = BLACK;
}

/**
 * 删除节点，然后使树重新平衡
 */
private void deleteEntry(Entry<K,V> p) {
    modCount++;
    size--;

    // 被删除节点有两个孩子，则与下一个中序遍历节点替换，有以下1、2两种情况
    // 1.
    //         4(p)               5
    //        / \                / \
    //       2   6      替换后  2     6
    //      / \  / \          / \   / \
    //     1  3  5  7        1  3 4(p) 7
    //
    // 2.
    //         4(p)                5
    //        / \                 / \
    //       2   5      替换后    2   4(p)
    //      / \   \             / \   \
    //     1  3    6           1  3    6(r)
    //
    // 3.
    //         4(p)             4(p)
    //        /       替换后    /
    //       2                2(r)
    if (p.left != null && p.right != null) {
        Entry<K,V> s = successor(p);
        p.key = s.key;
        p.value = s.value;
        p = s;
    }

    // 如果左孩子存在，则使用左孩子替换p后删除p，如上面的情形3
    // 如果左孩子不存在而右孩子存在，则使用右孩子替换p后删除p，如上面的情形2
    // 左右孩子都不存在，不需要替换，直接删掉p，如上面的情形1
    Entry<K,V> replacement = (p.left != null ? p.left : p.right);

    if (replacement != null) {
        // 存在左孩子或右孩子，替换掉p后将p删除
        replacement.parent = p.parent;
        if (p.parent == null)
            root = replacement;
        else if (p == p.parent.left)
            p.parent.left  = replacement;
        else
            p.parent.right = replacement;

        p.left = p.right = p.parent = null;

        // 被删除节点是黑色，删除后可能不满足红黑树性质，需要使其重新平衡
        if (p.color == BLACK)
            fixAfterDeletion(replacement);
    } else if (p.parent == null) {
        // p是根节点，则直接将树的根节点指针置空
        root = null;
    } else {
        // 被删除节点不存在子节点，直接删除

        // 若节点为黑色，删除后可能不满足红黑树性质，需要使其重新平衡
        if (p.color == BLACK)
            fixAfterDeletion(p);

        // 重建链接
        if (p.parent != null) {
            if (p == p.parent.left)
                p.parent.left = null;
            else if (p == p.parent.right)
                p.parent.right = null;
            p.parent = null;
        }
    }
}

/**
 * 删除节点后重新调整平衡
 * 算法导论中的平衡步骤
 * 详细推到可查看算法导论13.4章节
 */
private void fixAfterDeletion(Entry<K,V> x) {

    // 调整节点为根节点或红色节点不需要处理
    while (x != root && colorOf(x) == BLACK) {
        if (x == leftOf(parentOf(x))) {
            // x是其父节点的左孩子
            Entry<K,V> sib = rightOf(parentOf(x));

            if (colorOf(sib) == RED) {
                // 情况1：x的兄弟节点s是红色的
                //          B(黑)
                //        /   \
                //   A(黑)(x)  D(红)(s)
                //            / \
                //        C(黑)  E(黑)
                //
                // 调整为
                //
                //             D(黑)
                //            /    \
                //         B(红)   E(黑)
                //         /  \
                //    A(黑)(X) C(黑)(s)
                //
                // 此时，会转换为情况二、三、四中的一种
                setColor(sib, BLACK);
                setColor(parentOf(x), RED);
                rotateLeft(parentOf(x));
                sib = rightOf(parentOf(x));
            }

            if (colorOf(leftOf(sib))  == BLACK &&
                colorOf(rightOf(sib)) == BLACK) {
                // 情况2：x的兄弟节点是黑色的，且s的两个孩子都是黑色的
                //             B
                //           /   \
                //     A(黑)(x)  D(黑)(s)
                //               /   \
                //             C(黑) E(黑)
                //
                // 调整为
                //
                //             B
                //           /   \
                //     A(黑)(x)  D(红)(s)
                //               /   \
                //             C(黑) E(黑)
                //
                // 若是从情况一进入则x的父节点必定为红色，此时结束循环，将父节点改为黑色即可平衡
                // 若父节点为黑色，则将x指向父节点继续下一轮调整
                setColor(sib, RED);
                x = parentOf(x);
            } else {
                if (colorOf(rightOf(sib)) == BLACK) {
                    // 情况三：x的兄弟s是黑色的，且s的右孩子是黑色的
                    //          B
                    //        /   \
                    //   A(黑)(x)  D(黑)(s)
                    //            / \
                    //           C  E(黑)
                    //
                    // 调整为
                    //          B
                    //        /   \
                    //   A(黑)(x)  C(黑)(s)
                    //              \
                    //              D(红)
                    //               \
                    //               E(黑)
                    //
                    // 此时，情况三转变为了情况四
                    setColor(leftOf(sib), BLACK);
                    setColor(sib, RED);
                    rotateRight(sib);
                    sib = rightOf(parentOf(x));
                }

                // 情况四：x的兄弟s是黑色的，且s的右孩子是红色的
                //          B
                //        /   \
                //   A(黑)(x)  D(黑)(s)
                //            / \
                //           C  E(红)
                //
                // 调整为
                //           D
                //         /   \
                //      B(黑)  E(黑)
                //     /   \
                //  A(黑)   C
                // 此时，树重新达到平衡，将x指向root退出循环
                setColor(sib, colorOf(parentOf(x)));
                setColor(parentOf(x), BLACK);
                setColor(rightOf(sib), BLACK);
                rotateLeft(parentOf(x));
                x = root;
            }
        } else {
            // x是其父节点的右孩子，与上面调整逻辑类似，互相对称，不再详细说明
            Entry<K,V> sib = leftOf(parentOf(x));

            if (colorOf(sib) == RED) {
                setColor(sib, BLACK);
                setColor(parentOf(x), RED);
                rotateRight(parentOf(x));
                sib = leftOf(parentOf(x));
            }

            if (colorOf(rightOf(sib)) == BLACK &&
                colorOf(leftOf(sib)) == BLACK) {
                setColor(sib, RED);
                x = parentOf(x);
            } else {
                if (colorOf(leftOf(sib)) == BLACK) {
                    setColor(rightOf(sib), BLACK);
                    setColor(sib, RED);
                    rotateLeft(sib);
                    sib = leftOf(parentOf(x));
                }
                setColor(sib, colorOf(parentOf(x)));
                setColor(parentOf(x), BLACK);
                setColor(leftOf(sib), BLACK);
                rotateRight(parentOf(x));
                x = root;
            }
        }
    }

    setColor(x, BLACK);
}

private static final long serialVersionUID = 919286545866124006L;

/**
 * Save the state of the {@code TreeMap} instance to a stream (i.e.,
 * serialize it).
 *
 * @serialData The <em>size</em> of the TreeMap (the number of key-value
 *             mappings) is emitted (int), followed by the key (Object)
 *             and value (Object) for each key-value mapping represented
 *             by the TreeMap. The key-value mappings are emitted in
 *             key-order (as determined by the TreeMap's Comparator,
 *             or by the keys' natural ordering if the TreeMap has no
 *             Comparator).
 */
private void writeObject(java.io.ObjectOutputStream s)
    throws java.io.IOException {
    // Write out the Comparator and any hidden stuff
    s.defaultWriteObject();

    // Write out size (number of Mappings)
    s.writeInt(size);

    // Write out keys and values (alternating)
    for (Iterator<Map.Entry<K,V>> i = entrySet().iterator(); i.hasNext(); ) {
        Map.Entry<K,V> e = i.next();
        s.writeObject(e.getKey());
        s.writeObject(e.getValue());
    }
}

/**
 * Reconstitute the {@code TreeMap} instance from a stream (i.e.,
 * deserialize it).
 */
private void readObject(final java.io.ObjectInputStream s)
    throws java.io.IOException, ClassNotFoundException {
    // Read in the Comparator and any hidden stuff
    s.defaultReadObject();

    // Read in size
    int size = s.readInt();

    buildFromSorted(size, null, s, null);
}

/** Intended to be called only from TreeSet.readObject */
void readTreeSet(int size, java.io.ObjectInputStream s, V defaultVal)
    throws java.io.IOException, ClassNotFoundException {
    buildFromSorted(size, null, s, defaultVal);
}

/** Intended to be called only from TreeSet.addAll */
void addAllForTreeSet(SortedSet<? extends K> set, V defaultVal) {
    try {
        buildFromSorted(set.size(), set.iterator(), null, defaultVal);
    } catch (java.io.IOException cannotHappen) {
    } catch (ClassNotFoundException cannotHappen) {
    }
}


/**
 * 根据有序数据构建出红黑树数据结构
 * 接受四种类型参数
 * 1.Map.Entries的迭代器(it != null, defaultVal == null)，将使用迭代器来
 *   获取键值对，构建出树，用于根据SortedMap来初始化TreeMap
 * 2.键的迭代器(it != null, defaultVal != null)，将使用迭代器来获取键，使用
 *   defaultVal作为值，用于根据SortedSet来初始化TreeSet
 * 3.交替序列化的键和值的流(it == null, defaultVal == null)，将使用
 *   ObjectInputStream来获取键值对，用于反序列化时生成TreeMap
 * 4.序列化的键的流(it == null, defaultVal != null)，将使用ObjectInputStream
 *   来获取键，使用defaultVal作为值，用于反序列化时生成TreeSet
 */
private void buildFromSorted(int size, Iterator<?> it,
                             java.io.ObjectInputStream str,
                             V defaultVal)
    throws  java.io.IOException, ClassNotFoundException {
    this.size = size;
    root = buildFromSorted(0, 0, size-1, computeRedLevel(size),
                           it, str, defaultVal);
}

/**
 * 将传入的数据从中间分为两部分，左半部分递归生成左子树，右半部分递归生成右子树
 * 最终将有序数据转为红黑树，lo和hi指明当前分割数据的起始和结束索引
 *
 * 递归helper方法，完成实际的构建工作
 * level 指明当前树的层级，初始为0
 * lo 指明当前子树的最小元素的索引，初始为0
 * hi 指明当前子树的最大元素的做阴，初始为size - 1
 * redLevel 节点应为红色的层，使用computeRedLevel方法计算
 */
@SuppressWarnings("unchecked")
private final Entry<K,V> buildFromSorted(int level, int lo, int hi,
                                         int redLevel,
                                         Iterator<?> it,
                                         java.io.ObjectInputStream str,
                                         V defaultVal)
    throws  java.io.IOException, ClassNotFoundException {

    // 递归终止条件
    if (hi < lo) return null;

    // 获取数据中点索引
    int mid = (lo + hi) >>> 1;

    Entry<K,V> left  = null;
    if (lo < mid)
        // 递归生成左子树
        left = buildFromSorted(level+1, lo, mid - 1, redLevel,
                               it, str, defaultVal);

    K key;
    V value;
    if (it != null) {
        // 使用迭代器获取元素
        if (defaultVal==null) {
            // 迭代器获取键值
            Map.Entry<?,?> entry = (Map.Entry<?,?>)it.next();
            key = (K)entry.getKey();
            value = (V)entry.getValue();
        } else {
            // 迭代器获取键，defaultVal做为值
            key = (K)it.next();
            value = defaultVal;
        }
    } else {
        // ObjectInputStream中获取键值
        key = (K) str.readObject();
        value = (defaultVal != null ? defaultVal : (V) str.readObject());
    }

    // 创建子树根节点
    Entry<K,V> middle =  new Entry<>(key, value, null);

    // 最后一层如果不是满的，将所有节点颜色设置为红色
    if (level == redLevel)
        middle.color = RED;

    // 左子树不为空，建立链接
    if (left != null) {
        middle.left = left;
        left.parent = middle;
    }

    if (mid < hi) {
        // 递归创建右子树
        Entry<K,V> right = buildFromSorted(level+1, mid+1, hi, redLevel,
                                           it, str, defaultVal);
        // 建立链接
        middle.right = right;
        right.parent = middle;
    }

    // 返回子树根节点
    return middle;
}

/**
 * 找到符合满二叉树的下一层，将满二叉树中的节点都置为黑色节点，剩下的节点都置为红色
 * 即如果最后一层不是满的，则将最后一层所有节点都置为红色
 */
private static int computeRedLevel(int sz) {
    int level = 0;
    for (int m = sz - 1; m >= 0; m = m / 2 - 1)
        level++;
    return level;
}

/**
 * Currently, we support Spliterator-based versions only for the
 * full map, in either plain of descending form, otherwise relying
 * on defaults because size estimation for submaps would dominate
 * costs. The type tests needed to check these for key views are
 * not very nice but avoid disrupting existing class
 * structures. Callers must use plain default spliterators if this
 * returns null.
 */
static <K> Spliterator<K> keySpliteratorFor(NavigableMap<K,?> m) {
    if (m instanceof TreeMap) {
        @SuppressWarnings("unchecked") TreeMap<K,Object> t =
            (TreeMap<K,Object>) m;
        return t.keySpliterator();
    }
    if (m instanceof DescendingSubMap) {
        @SuppressWarnings("unchecked") DescendingSubMap<K,?> dm =
            (DescendingSubMap<K,?>) m;
        TreeMap<K,?> tm = dm.m;
        if (dm == tm.descendingMap) {
            @SuppressWarnings("unchecked") TreeMap<K,Object> t =
                (TreeMap<K,Object>) tm;
            return t.descendingKeySpliterator();
        }
    }
    @SuppressWarnings("unchecked") NavigableSubMap<K,?> sm =
        (NavigableSubMap<K,?>) m;
    return sm.keySpliterator();
}

final Spliterator<K> keySpliterator() {
    return new KeySpliterator<K,V>(this, null, null, 0, -1, 0);
}

final Spliterator<K> descendingKeySpliterator() {
    return new DescendingKeySpliterator<K,V>(this, null, null, 0, -2, 0);
}

/**
 * Base class for spliterators.  Iteration starts at a given
 * origin and continues up to but not including a given fence (or
 * null for end).  At top-level, for ascending cases, the first
 * split uses the root as left-fence/right-origin. From there,
 * right-hand splits replace the current fence with its left
 * child, also serving as origin for the split-off spliterator.
 * Left-hands are symmetric. Descending versions place the origin
 * at the end and invert ascending split rules.  This base class
 * is non-commital about directionality, or whether the top-level
 * spliterator covers the whole tree. This means that the actual
 * split mechanics are located in subclasses. Some of the subclass
 * trySplit methods are identical (except for return types), but
 * not nicely factorable.
 *
 * Currently, subclass versions exist only for the full map
 * (including descending keys via its descendingMap).  Others are
 * possible but currently not worthwhile because submaps require
 * O(n) computations to determine size, which substantially limits
 * potential speed-ups of using custom Spliterators versus default
 * mechanics.
 *
 * To boostrap initialization, external constructors use
 * negative size estimates: -1 for ascend, -2 for descend.
 */
static class TreeMapSpliterator<K,V> {
    final TreeMap<K,V> tree;
    Entry<K,V> current; // traverser; initially first node in range
    Entry<K,V> fence;   // one past last, or null
    int side;                   // 0: top, -1: is a left split, +1: right
    int est;                    // size estimate (exact only for top-level)
    int expectedModCount;       // for CME checks

    TreeMapSpliterator(TreeMap<K,V> tree,
                       Entry<K,V> origin, Entry<K,V> fence,
                       int side, int est, int expectedModCount) {
        this.tree = tree;
        this.current = origin;
        this.fence = fence;
        this.side = side;
        this.est = est;
        this.expectedModCount = expectedModCount;
    }

    final int getEstimate() { // force initialization
        int s; TreeMap<K,V> t;
        if ((s = est) < 0) {
            if ((t = tree) != null) {
                current = (s == -1) ? t.getFirstEntry() : t.getLastEntry();
                s = est = t.size;
                expectedModCount = t.modCount;
            }
            else
                s = est = 0;
        }
        return s;
    }

    public final long estimateSize() {
        return (long)getEstimate();
    }
}

static final class KeySpliterator<K,V>
    extends TreeMapSpliterator<K,V>
    implements Spliterator<K> {
    KeySpliterator(TreeMap<K,V> tree,
                   Entry<K,V> origin, Entry<K,V> fence,
                   int side, int est, int expectedModCount) {
        super(tree, origin, fence, side, est, expectedModCount);
    }

    public KeySpliterator<K,V> trySplit() {
        if (est < 0)
            getEstimate(); // force initialization
        int d = side;
        Entry<K,V> e = current, f = fence,
            s = ((e == null || e == f) ? null :      // empty
                 (d == 0)              ? tree.root : // was top
                 (d >  0)              ? e.right :   // was right
                 (d <  0 && f != null) ? f.left :    // was left
                 null);
        if (s != null && s != e && s != f &&
            tree.compare(e.key, s.key) < 0) {        // e not already past s
            side = 1;
            return new KeySpliterator<>
                (tree, e, current = s, -1, est >>>= 1, expectedModCount);
        }
        return null;
    }

    public void forEachRemaining(Consumer<? super K> action) {
        if (action == null)
            throw new NullPointerException();
        if (est < 0)
            getEstimate(); // force initialization
        Entry<K,V> f = fence, e, p, pl;
        if ((e = current) != null && e != f) {
            current = f; // exhaust
            do {
                action.accept(e.key);
                if ((p = e.right) != null) {
                    while ((pl = p.left) != null)
                        p = pl;
                }
                else {
                    while ((p = e.parent) != null && e == p.right)
                        e = p;
                }
            } while ((e = p) != null && e != f);
            if (tree.modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    public boolean tryAdvance(Consumer<? super K> action) {
        Entry<K,V> e;
        if (action == null)
            throw new NullPointerException();
        if (est < 0)
            getEstimate(); // force initialization
        if ((e = current) == null || e == fence)
            return false;
        current = successor(e);
        action.accept(e.key);
        if (tree.modCount != expectedModCount)
            throw new ConcurrentModificationException();
        return true;
    }

    public int characteristics() {
        return (side == 0 ? Spliterator.SIZED : 0) |
            Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED;
    }

    public final Comparator<? super K>  getComparator() {
        return tree.comparator;
    }

}

static final class DescendingKeySpliterator<K,V>
    extends TreeMapSpliterator<K,V>
    implements Spliterator<K> {
    DescendingKeySpliterator(TreeMap<K,V> tree,
                             Entry<K,V> origin, Entry<K,V> fence,
                             int side, int est, int expectedModCount) {
        super(tree, origin, fence, side, est, expectedModCount);
    }

    public DescendingKeySpliterator<K,V> trySplit() {
        if (est < 0)
            getEstimate(); // force initialization
        int d = side;
        Entry<K,V> e = current, f = fence,
                s = ((e == null || e == f) ? null :      // empty
                     (d == 0)              ? tree.root : // was top
                     (d <  0)              ? e.left :    // was left
                     (d >  0 && f != null) ? f.right :   // was right
                     null);
        if (s != null && s != e && s != f &&
            tree.compare(e.key, s.key) > 0) {       // e not already past s
            side = 1;
            return new DescendingKeySpliterator<>
                    (tree, e, current = s, -1, est >>>= 1, expectedModCount);
        }
        return null;
    }

    public void forEachRemaining(Consumer<? super K> action) {
        if (action == null)
            throw new NullPointerException();
        if (est < 0)
            getEstimate(); // force initialization
        Entry<K,V> f = fence, e, p, pr;
        if ((e = current) != null && e != f) {
            current = f; // exhaust
            do {
                action.accept(e.key);
                if ((p = e.left) != null) {
                    while ((pr = p.right) != null)
                        p = pr;
                }
                else {
                    while ((p = e.parent) != null && e == p.left)
                        e = p;
                }
            } while ((e = p) != null && e != f);
            if (tree.modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    public boolean tryAdvance(Consumer<? super K> action) {
        Entry<K,V> e;
        if (action == null)
            throw new NullPointerException();
        if (est < 0)
            getEstimate(); // force initialization
        if ((e = current) == null || e == fence)
            return false;
        current = predecessor(e);
        action.accept(e.key);
        if (tree.modCount != expectedModCount)
            throw new ConcurrentModificationException();
        return true;
    }

    public int characteristics() {
        return (side == 0 ? Spliterator.SIZED : 0) |
            Spliterator.DISTINCT | Spliterator.ORDERED;
    }
}

static final class ValueSpliterator<K,V>
        extends TreeMapSpliterator<K,V>
        implements Spliterator<V> {
    ValueSpliterator(TreeMap<K,V> tree,
                     Entry<K,V> origin, Entry<K,V> fence,
                     int side, int est, int expectedModCount) {
        super(tree, origin, fence, side, est, expectedModCount);
    }

    public ValueSpliterator<K,V> trySplit() {
        if (est < 0)
            getEstimate(); // force initialization
        int d = side;
        Entry<K,V> e = current, f = fence,
                s = ((e == null || e == f) ? null :      // empty
                     (d == 0)              ? tree.root : // was top
                     (d >  0)              ? e.right :   // was right
                     (d <  0 && f != null) ? f.left :    // was left
                     null);
        if (s != null && s != e && s != f &&
            tree.compare(e.key, s.key) < 0) {        // e not already past s
            side = 1;
            return new ValueSpliterator<>
                    (tree, e, current = s, -1, est >>>= 1, expectedModCount);
        }
        return null;
    }

    public void forEachRemaining(Consumer<? super V> action) {
        if (action == null)
            throw new NullPointerException();
        if (est < 0)
            getEstimate(); // force initialization
        Entry<K,V> f = fence, e, p, pl;
        if ((e = current) != null && e != f) {
            current = f; // exhaust
            do {
                action.accept(e.value);
                if ((p = e.right) != null) {
                    while ((pl = p.left) != null)
                        p = pl;
                }
                else {
                    while ((p = e.parent) != null && e == p.right)
                        e = p;
                }
            } while ((e = p) != null && e != f);
            if (tree.modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    public boolean tryAdvance(Consumer<? super V> action) {
        Entry<K,V> e;
        if (action == null)
            throw new NullPointerException();
        if (est < 0)
            getEstimate(); // force initialization
        if ((e = current) == null || e == fence)
            return false;
        current = successor(e);
        action.accept(e.value);
        if (tree.modCount != expectedModCount)
            throw new ConcurrentModificationException();
        return true;
    }

    public int characteristics() {
        return (side == 0 ? Spliterator.SIZED : 0) | Spliterator.ORDERED;
    }
}

static final class EntrySpliterator<K,V>
    extends TreeMapSpliterator<K,V>
    implements Spliterator<Map.Entry<K,V>> {
    EntrySpliterator(TreeMap<K,V> tree,
                     Entry<K,V> origin, Entry<K,V> fence,
                     int side, int est, int expectedModCount) {
        super(tree, origin, fence, side, est, expectedModCount);
    }

    public EntrySpliterator<K,V> trySplit() {
        if (est < 0)
            getEstimate(); // force initialization
        int d = side;
        Entry<K,V> e = current, f = fence,
                s = ((e == null || e == f) ? null :      // empty
                     (d == 0)              ? tree.root : // was top
                     (d >  0)              ? e.right :   // was right
                     (d <  0 && f != null) ? f.left :    // was left
                     null);
        if (s != null && s != e && s != f &&
            tree.compare(e.key, s.key) < 0) {        // e not already past s
            side = 1;
            return new EntrySpliterator<>
                    (tree, e, current = s, -1, est >>>= 1, expectedModCount);
        }
        return null;
    }

    public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
        if (action == null)
            throw new NullPointerException();
        if (est < 0)
            getEstimate(); // force initialization
        Entry<K,V> f = fence, e, p, pl;
        if ((e = current) != null && e != f) {
            current = f; // exhaust
            do {
                action.accept(e);
                if ((p = e.right) != null) {
                    while ((pl = p.left) != null)
                        p = pl;
                }
                else {
                    while ((p = e.parent) != null && e == p.right)
                        e = p;
                }
            } while ((e = p) != null && e != f);
            if (tree.modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    public boolean tryAdvance(Consumer<? super Map.Entry<K,V>> action) {
        Entry<K,V> e;
        if (action == null)
            throw new NullPointerException();
        if (est < 0)
            getEstimate(); // force initialization
        if ((e = current) == null || e == fence)
            return false;
        current = successor(e);
        action.accept(e);
        if (tree.modCount != expectedModCount)
            throw new ConcurrentModificationException();
        return true;
    }

    public int characteristics() {
        return (side == 0 ? Spliterator.SIZED : 0) |
                Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED;
    }

    @Override
    public Comparator<Map.Entry<K, V>> getComparator() {
        // Adapt or create a key-based comparator
        if (tree.comparator != null) {
            return Map.Entry.comparingByKey(tree.comparator);
        }
        else {
            return (Comparator<Map.Entry<K, V>> & Serializable) (e1, e2) -> {
                @SuppressWarnings("unchecked")
                Comparable<? super K> k1 = (Comparable<? super K>) e1.getKey();
                return k1.compareTo(e2.getKey());
            };
        }
    }
}
}
