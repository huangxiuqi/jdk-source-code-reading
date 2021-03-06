/*
 * Copyright (c) 2003, 2017, Oracle and/or its affiliates. All rights reserved.
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

import sun.misc.SharedSecrets;

import java.util.function.Consumer;

/**
 * An unbounded priority {@linkplain Queue queue} based on a priority heap.
 * The elements of the priority queue are ordered according to their
 * {@linkplain Comparable natural ordering}, or by a {@link Comparator}
 * provided at queue construction time, depending on which constructor is
 * used.  A priority queue does not permit {@code null} elements.
 * A priority queue relying on natural ordering also does not permit
 * insertion of non-comparable objects (doing so may result in
 * {@code ClassCastException}).
 *
 * <p>The <em>head</em> of this queue is the <em>least</em> element
 * with respect to the specified ordering.  If multiple elements are
 * tied for least value, the head is one of those elements -- ties are
 * broken arbitrarily.  The queue retrieval operations {@code poll},
 * {@code remove}, {@code peek}, and {@code element} access the
 * element at the head of the queue.
 *
 * <p>A priority queue is unbounded, but has an internal
 * <i>capacity</i> governing the size of an array used to store the
 * elements on the queue.  It is always at least as large as the queue
 * size.  As elements are added to a priority queue, its capacity
 * grows automatically.  The details of the growth policy are not
 * specified.
 *
 * <p>This class and its iterator implement all of the
 * <em>optional</em> methods of the {@link Collection} and {@link
 * Iterator} interfaces.  The Iterator provided in method {@link
 * #iterator()} is <em>not</em> guaranteed to traverse the elements of
 * the priority queue in any particular order. If you need ordered
 * traversal, consider using {@code Arrays.sort(pq.toArray())}.
 *
 * <p><strong>Note that this implementation is not synchronized.</strong>
 * Multiple threads should not access a {@code PriorityQueue}
 * instance concurrently if any of the threads modifies the queue.
 * Instead, use the thread-safe {@link
 * java.util.concurrent.PriorityBlockingQueue} class.
 *
 * <p>Implementation note: this implementation provides
 * O(log(n)) time for the enqueuing and dequeuing methods
 * ({@code offer}, {@code poll}, {@code remove()} and {@code add});
 * linear time for the {@code remove(Object)} and {@code contains(Object)}
 * methods; and constant time for the retrieval methods
 * ({@code peek}, {@code element}, and {@code size}).
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @since 1.5
 * @author Josh Bloch, Doug Lea
 * @param <E> the type of elements held in this collection
 */
public class PriorityQueue<E> extends AbstractQueue<E>
    implements java.io.Serializable {

    private static final long serialVersionUID = -7720805057305804111L;

    /**
     * 默认初始容量
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 11;

    /**
     * 二叉堆存放元素的数组，若queue[n]为父节点，则其两个子节点为queue[2 * (n + 1)]和queue[2 * (n + 2)]，
     * 若queue[n]为子节点，则其父节点为queue[(n - 1) / 2]
     * 索引为0处称为堆顶
     */
    transient Object[] queue;

    /**
     * 元素个数
     */
    private int size = 0;

    /**
     * 比较器，未设置时会使用元素的自然序
     */
    private final Comparator<? super E> comparator;

    /**
     * 修改次数，用作快速失败检查
     */
    transient int modCount = 0;

    /**
     * 使用默认初始容量实例化PriorityQueue
     */
    public PriorityQueue() {
        this(DEFAULT_INITIAL_CAPACITY, null);
    }

    /**
     * 使用给定的初始容量实例化PriorityQueue
     */
    public PriorityQueue(int initialCapacity) {
        this(initialCapacity, null);
    }

    /**
     * 使用默认初始容量和给定的比较器实例化PriorityQueue
     */
    public PriorityQueue(Comparator<? super E> comparator) {
        this(DEFAULT_INITIAL_CAPACITY, comparator);
    }

    /**
     * 使用给定的初始容量和比较器实例化PriorityQueue
     */
    public PriorityQueue(int initialCapacity,
                         Comparator<? super E> comparator) {
        // 初始容量必须大于1
        if (initialCapacity < 1)
            throw new IllegalArgumentException();

        // 属性赋值
        this.queue = new Object[initialCapacity];
        this.comparator = comparator;
    }

    /**
     * 使用给定集合实例化PriorityQueue
     * 若给定集合是SortedSet的一个实例或是另一个PriorityQueue，则元素会按相同的顺序保存，
     * 否则会按元素自然序保存
     */
    @SuppressWarnings("unchecked")
    public PriorityQueue(Collection<? extends E> c) {
        if (c instanceof SortedSet<?>) {
            SortedSet<? extends E> ss = (SortedSet<? extends E>) c;
            // 比较器配置为SortedSet的比较器
            this.comparator = (Comparator<? super E>) ss.comparator();
            // 复制元素
            initElementsFromCollection(ss);
        }
        else if (c instanceof PriorityQueue<?>) {
            PriorityQueue<? extends E> pq = (PriorityQueue<? extends E>) c;
            // 比较器配置为给定的PriorityQueue的比较器
            this.comparator = (Comparator<? super E>) pq.comparator();
            // 复制元素
            initFromPriorityQueue(pq);
        }
        else {
            // 比较器设置为null
            this.comparator = null;
            // 复制元素
            initFromCollection(c);
        }
    }

    /**
     * 根据给定的PriorityQueue实例化，同时保持元素的顺序
     */
    @SuppressWarnings("unchecked")
    public PriorityQueue(PriorityQueue<? extends E> c) {
        // 比较器配置为给定的PriorityQueue的比较器
        this.comparator = (Comparator<? super E>) c.comparator();
        // 复制元素
        initFromPriorityQueue(c);
    }

    /**
     * 根据给定的SortedSet实例化，同时保持元素的顺序
     */
    @SuppressWarnings("unchecked")
    public PriorityQueue(SortedSet<? extends E> c) {
        // 比较器配置为SortedSet的比较器
        this.comparator = (Comparator<? super E>) c.comparator();
        // 复制元素
        initElementsFromCollection(c);
    }

    /**
     * 将给定PriorityQueue中的元素复制过来
     */
    private void initFromPriorityQueue(PriorityQueue<? extends E> c) {
        if (c.getClass() == PriorityQueue.class) {
            // 类型完全相同，则直接复制数组和大小
            this.queue = c.toArray();
            this.size = c.size();
        } else {
            // c是PriorityQueue的子类，则按普通集合来复制
            initFromCollection(c);
        }
    }

    /**
     * 将给定集合中的元素复制过来
     */
    private void initElementsFromCollection(Collection<? extends E> c) {
        // 集合转数组
        Object[] a = c.toArray();

        // 集合不是ArrayList类型，则数组可能不是Object[]类型，需要做强制类型转换
        if (c.getClass() != ArrayList.class)
            a = Arrays.copyOf(a, a.length, Object[].class);

        int len = a.length;

        // 集合中不能有null元素
        if (len == 1 || this.comparator != null)
            for (int i = 0; i < len; i++)
                if (a[i] == null)
                    throw new NullPointerException();

        // 属性赋值
        this.queue = a;
        this.size = a.length;
    }

    /**
     * 将给定集合中的元素复制过来
     * 给定集合可能不是有序的，复制后需要重新堆化
     */
    private void initFromCollection(Collection<? extends E> c) {
        // 复制元素
        initElementsFromCollection(c);
        // 堆化
        heapify();
    }

    /**
     * 数组的最大长度
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * 扩容
     */
    private void grow(int minCapacity) {
        int oldCapacity = queue.length;
        // 若旧容量小于64，新容量为原来的2倍+2，否则新容量为原来的1.5倍
        int newCapacity = oldCapacity + ((oldCapacity < 64) ?
                                         (oldCapacity + 2) :
                                         (oldCapacity >> 1));
        // 新容量不能超过一个上限
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);

        // 对数组进行扩容
        queue = Arrays.copyOf(queue, newCapacity);
    }

    /**
     * 计算容量上限
     */
    private static int hugeCapacity(int minCapacity) {
        // 容量太大，溢出，抛出异常
        if (minCapacity < 0)
            throw new OutOfMemoryError();

        // 若需要的容量大于MAX_ARRAY_SIZE则返回Integer.MAX_VALUE，
        // 否则返回MAX_ARRAY_SIZE
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }

    /**
     * 插入元素
     */
    public boolean add(E e) {
        return offer(e);
    }

    /**
     * 插入元素
     * 以最小堆为例，插入流程如下
     * 1.将元素放在[size - 1]处，即最后一个元素，此时索引记做k
     * 2.找到k的父节点，父节点索引parent为(k - 1) / 2
     * 3.比较queue[k]与queue[parent]的大小，若queue[k] >= queue[parent]，
     *   满足父节点小于等于子节点的条件，则k是正确的位置，插入完成。
     * 4.若queue[k] < queue[parent]，不满足父节点小于等于子节点的条件，交换这两个节点，
     *   然后令k = parent，之后回到第2步重复。
     *
     * 简单来说就是将元素插入二叉堆的末尾，然后与父节点比较，若子节点小于父节点，交换这两个父子节点，
     * 再与父节点比较大小，直到找到一个满足子节点大于等于父节点的位置，插入完成
     */
    public boolean offer(E e) {
        // 插入元素不能为空
        if (e == null)
            throw new NullPointerException();

        // 修改次数加一
        modCount++;

        // 若数组已满，则先进行扩容
        int i = size;
        if (i >= queue.length)
            grow(i + 1);

        size = i + 1;

        if (i == 0)
            // 如果是第一个插入的元素，直接放在数组开头
            queue[0] = e;
        else
            // 上浮到正确位置
            siftUp(i, e);
        return true;
    }

    /**
     * 若队列不为空，则返回堆顶（索引为0的元素）元素
     * @return
     */
    @SuppressWarnings("unchecked")
    public E peek() {
        return (size == 0) ? null : (E) queue[0];
    }

    /**
     * 查找给定元素的索引，若元素不存在则返回-1
     */
    private int indexOf(Object o) {
        if (o != null) {
            // 遍历元素，返回第一个equals的元素的索引
            for (int i = 0; i < size; i++)
                if (o.equals(queue[i]))
                    return i;
        }

        // 找不到返回-1
        return -1;
    }

    /**
     * 删除给定的元素
     * 使用equals比较
     */
    public boolean remove(Object o) {
        // 查找给定元素的索引
        int i = indexOf(o);

        if (i == -1)
            // 给定元素不存在
            return false;
        else {
            // 删除
            removeAt(i);
            return true;
        }
    }

    /**
     * 删除给定的元素
     * 使用==比较
     * 迭代器中移除forgetMeNot队列中的元素时使用
     */
    boolean removeEq(Object o) {
        for (int i = 0; i < size; i++) {
            if (o == queue[i]) {
                removeAt(i);
                return true;
            }
        }
        return false;
    }

    /**
     * 判断队列中是否包含给定元素
     */
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    /**
     * Returns an array containing all of the elements in this queue.
     * The elements are in no particular order.
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this queue.  (In other words, this method must allocate
     * a new array).  The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this queue
     */
    public Object[] toArray() {
        return Arrays.copyOf(queue, size);
    }

    /**
     * Returns an array containing all of the elements in this queue; the
     * runtime type of the returned array is that of the specified array.
     * The returned array elements are in no particular order.
     * If the queue fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this queue.
     *
     * <p>If the queue fits in the specified array with room to spare
     * (i.e., the array has more elements than the queue), the element in
     * the array immediately following the end of the collection is set to
     * {@code null}.
     *
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     *
     * <p>Suppose {@code x} is a queue known to contain only strings.
     * The following code can be used to dump the queue into a newly
     * allocated array of {@code String}:
     *
     *  <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
     *
     * Note that {@code toArray(new Object[0])} is identical in function to
     * {@code toArray()}.
     *
     * @param a the array into which the elements of the queue are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose.
     * @return an array containing all of the elements in this queue
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in
     *         this queue
     * @throws NullPointerException if the specified array is null
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        final int size = this.size;
        if (a.length < size)
            // Make a new array of a's runtime type, but my contents:
            return (T[]) Arrays.copyOf(queue, size, a.getClass());
        System.arraycopy(queue, 0, a, 0, size);
        if (a.length > size)
            a[size] = null;
        return a;
    }

    /**
     * Returns an iterator over the elements in this queue. The iterator
     * does not return the elements in any particular order.
     *
     * @return an iterator over the elements in this queue
     */
    public Iterator<E> iterator() {
        return new Itr();
    }

    private final class Itr implements Iterator<E> {
        /**
         * Index (into queue array) of element to be returned by
         * subsequent call to next.
         */
        private int cursor = 0;

        /**
         * Index of element returned by most recent call to next,
         * unless that element came from the forgetMeNot list.
         * Set to -1 if element is deleted by a call to remove.
         */
        private int lastRet = -1;

        /**
         * A queue of elements that were moved from the unvisited portion of
         * the heap into the visited portion as a result of "unlucky" element
         * removals during the iteration.  (Unlucky element removals are those
         * that require a siftup instead of a siftdown.)  We must visit all of
         * the elements in this list to complete the iteration.  We do this
         * after we've completed the "normal" iteration.
         *
         * We expect that most iterations, even those involving removals,
         * will not need to store elements in this field.
         */
        private ArrayDeque<E> forgetMeNot = null;

        /**
         * Element returned by the most recent call to next iff that
         * element was drawn from the forgetMeNot list.
         */
        private E lastRetElt = null;

        /**
         * The modCount value that the iterator believes that the backing
         * Queue should have.  If this expectation is violated, the iterator
         * has detected concurrent modification.
         */
        private int expectedModCount = modCount;

        public boolean hasNext() {
            return cursor < size ||
                (forgetMeNot != null && !forgetMeNot.isEmpty());
        }

        @SuppressWarnings("unchecked")
        public E next() {
            if (expectedModCount != modCount)
                throw new ConcurrentModificationException();
            if (cursor < size)
                return (E) queue[lastRet = cursor++];
            if (forgetMeNot != null) {
                lastRet = -1;
                lastRetElt = forgetMeNot.poll();
                if (lastRetElt != null)
                    return lastRetElt;
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            if (expectedModCount != modCount)
                throw new ConcurrentModificationException();
            if (lastRet != -1) {
                E moved = PriorityQueue.this.removeAt(lastRet);
                lastRet = -1;
                if (moved == null)
                    cursor--;
                else {
                    if (forgetMeNot == null)
                        forgetMeNot = new ArrayDeque<>();
                    forgetMeNot.add(moved);
                }
            } else if (lastRetElt != null) {
                PriorityQueue.this.removeEq(lastRetElt);
                lastRetElt = null;
            } else {
                throw new IllegalStateException();
            }
            expectedModCount = modCount;
        }
    }

    public int size() {
        return size;
    }

    /**
     * 清空队列
     */
    public void clear() {
        // 修改次数加一
        modCount++;

        // 将数组所有位置置为null
        for (int i = 0; i < size; i++)
            queue[i] = null;

        // 元素个数置为0
        size = 0;
    }

    /**
     * 弹出队首元素
     */
    @SuppressWarnings("unchecked")
    public E poll() {
        // 队列为空返回null
        if (size == 0)
            return null;

        // 元素个数减一
        int s = --size;

        // 修改次数加一
        modCount++;

        // 堆顶元素
        E result = (E) queue[0];

        // 堆底元素
        E x = (E) queue[s];
        queue[s] = null;
        if (s != 0)
            // 将堆底元素放至堆顶，然后下沉到正确位置
            siftDown(0, x);
        return result;
    }

    /**
     * 删除给定索引处的元素，删除后可能会留下一个空位，
     * 会将最后一个元素替换到删除位置，经过下沉和上浮（可能）操作后，使树重新平衡
     */
    @SuppressWarnings("unchecked")
    private E removeAt(int i) {
        // 修改次数加一
        modCount++;

        // 元素个数减一
        int s = --size;
        if (s == i)
            // 删除的最后一个元素，则直接将索引i处置为null即可
            queue[i] = null;
        else {
            // 将队列最后一个元素插入i处，并下沉到正确的位置
            E moved = (E) queue[s];
            queue[s] = null;
            siftDown(i, moved);

            // 如果此元素没有下沉，则将其上浮
            if (queue[i] == moved) {
                siftUp(i, moved);

                // 如果元素上浮移动过位置，那么返回这个元素
                // 返回值是在迭代器中使用，因为元素上浮后可能已经错过迭代器的迭代指针，
                // 需要在另外的队列中保存，待指针遍历完数组后，再对这个队列中的值进行遍历
                if (queue[i] != moved)
                    return moved;
            }
        }
        return null;
    }

    /**
     * 在索引k处插入元素，并上浮到正确的位置
     */
    private void siftUp(int k, E x) {
        if (comparator != null)
            siftUpUsingComparator(k, x);
        else
            siftUpComparable(k, x);
    }

    /**
     * 使用元素自然序
     */
    @SuppressWarnings("unchecked")
    private void siftUpComparable(int k, E x) {
        Comparable<? super E> key = (Comparable<? super E>) x;

        // 节点交换到堆顶则停止上浮
        while (k > 0) {

            // 获取父节点
            int parent = (k - 1) >>> 1;
            Object e = queue[parent];

            // 若当前位置满足二叉堆性质，跳出循环
            // 否则交换两节点的值，准备下一次比较
            if (key.compareTo((E) e) >= 0)
                break;

            // 位置不正确，交换父子节点
            queue[k] = e;
            k = parent;
        }

        // 在正确的位置插入新元素
        queue[k] = key;
    }

    /**
     * 使用给定的比较器
     * 逻辑与siftUpComparable相同，只是更换了比较器
     */
    @SuppressWarnings("unchecked")
    private void siftUpUsingComparator(int k, E x) {
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Object e = queue[parent];
            if (comparator.compare(x, (E) e) >= 0)
                break;
            queue[k] = e;
            k = parent;
        }
        queue[k] = x;
    }

    /**
     * 在索引k处插入元素，并下沉到正确的位置
     */
    private void siftDown(int k, E x) {
        if (comparator != null)
            siftDownUsingComparator(k, x);
        else
            siftDownComparable(k, x);
    }

    /**
     * 使用元素自然序
     */
    @SuppressWarnings("unchecked")
    private void siftDownComparable(int k, E x) {
        Comparable<? super E> key = (Comparable<? super E>)x;

        // 处理到最后一个非叶子节点，叶子节点不需要比较了
        int half = size >>> 1;
        while (k < half) {
            // 左孩子索引为2k + 1
            int child = (k << 1) + 1;
            // 左孩子赋给变量c
            Object c = queue[child];

            // 右孩子索引为2k + 2
            int right = child + 1;

            // 如果右孩子存在且左孩子大于右孩子
            // 则将c赋值为右孩子
            if (right < size &&
                ((Comparable<? super E>) c).compareTo((E) queue[right]) > 0)
                c = queue[child = right];

            // 此时，变量c为左右孩子中最小的那个
            // 若给定节点小于等于c，则说明已经下沉到正确的位置，停止循环
            if (key.compareTo((E) c) <= 0)
                break;

            // 节点比左右孩子中最小的那个还大，则交换父子节点，继续下次比较
            queue[k] = c;
            k = child;
        }

        // 在正确的位置插入给定元素
        queue[k] = key;
    }

    /**
     * 使用给定的比较器
     * 逻辑与siftDownComparable方法相同，只是更换了比较器
     */
    @SuppressWarnings("unchecked")
    private void siftDownUsingComparator(int k, E x) {
        int half = size >>> 1;
        while (k < half) {
            int child = (k << 1) + 1;
            Object c = queue[child];
            int right = child + 1;
            if (right < size &&
                comparator.compare((E) c, (E) queue[right]) > 0)
                c = queue[child = right];
            if (comparator.compare(x, (E) c) <= 0)
                break;
            queue[k] = c;
            k = child;
        }
        queue[k] = x;
    }

    /**
     * 将无序数组调整为二叉堆
     * 从最后一个非叶子节点开始向根节点遍历，将每个节点下沉到正确的位置
     */
    @SuppressWarnings("unchecked")
    private void heapify() {
        // (size / 2) - 1 是最后一个非叶子节点的索引
        for (int i = (size >>> 1) - 1; i >= 0; i--)
            siftDown(i, (E) queue[i]);
    }

    /**
     * Returns the comparator used to order the elements in this
     * queue, or {@code null} if this queue is sorted according to
     * the {@linkplain Comparable natural ordering} of its elements.
     *
     * @return the comparator used to order this queue, or
     *         {@code null} if this queue is sorted according to the
     *         natural ordering of its elements
     */
    public Comparator<? super E> comparator() {
        return comparator;
    }

    /**
     * Saves this queue to a stream (that is, serializes it).
     *
     * @serialData The length of the array backing the instance is
     *             emitted (int), followed by all of its elements
     *             (each an {@code Object}) in the proper order.
     * @param s the stream
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        // Write out element count, and any hidden stuff
        s.defaultWriteObject();

        // Write out array length, for compatibility with 1.5 version
        s.writeInt(Math.max(2, size + 1));

        // Write out all elements in the "proper order".
        for (int i = 0; i < size; i++)
            s.writeObject(queue[i]);
    }

    /**
     * Reconstitutes the {@code PriorityQueue} instance from a stream
     * (that is, deserializes it).
     *
     * @param s the stream
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // Read in size, and any hidden stuff
        s.defaultReadObject();

        // Read in (and discard) array length
        s.readInt();

        SharedSecrets.getJavaOISAccess().checkArray(s, Object[].class, size);
        queue = new Object[size];

        // Read in all elements.
        for (int i = 0; i < size; i++)
            queue[i] = s.readObject();

        // Elements are guaranteed to be in "proper order", but the
        // spec has never explained what that might be.
        heapify();
    }

    /**
     * Creates a <em><a href="Spliterator.html#binding">late-binding</a></em>
     * and <em>fail-fast</em> {@link Spliterator} over the elements in this
     * queue.
     *
     * <p>The {@code Spliterator} reports {@link Spliterator#SIZED},
     * {@link Spliterator#SUBSIZED}, and {@link Spliterator#NONNULL}.
     * Overriding implementations should document the reporting of additional
     * characteristic values.
     *
     * @return a {@code Spliterator} over the elements in this queue
     * @since 1.8
     */
    public final Spliterator<E> spliterator() {
        return new PriorityQueueSpliterator<E>(this, 0, -1, 0);
    }

    static final class PriorityQueueSpliterator<E> implements Spliterator<E> {
        /*
         * This is very similar to ArrayList Spliterator, except for
         * extra null checks.
         */
        private final PriorityQueue<E> pq;
        private int index;            // current index, modified on advance/split
        private int fence;            // -1 until first use
        private int expectedModCount; // initialized when fence set

        /** Creates new spliterator covering the given range */
        PriorityQueueSpliterator(PriorityQueue<E> pq, int origin, int fence,
                             int expectedModCount) {
            this.pq = pq;
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }

        private int getFence() { // initialize fence to size on first use
            int hi;
            if ((hi = fence) < 0) {
                expectedModCount = pq.modCount;
                hi = fence = pq.size;
            }
            return hi;
        }

        public PriorityQueueSpliterator<E> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null :
                new PriorityQueueSpliterator<E>(pq, lo, index = mid,
                                                expectedModCount);
        }

        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> action) {
            int i, hi, mc; // hoist accesses and checks from loop
            PriorityQueue<E> q; Object[] a;
            if (action == null)
                throw new NullPointerException();
            if ((q = pq) != null && (a = q.queue) != null) {
                if ((hi = fence) < 0) {
                    mc = q.modCount;
                    hi = q.size;
                }
                else
                    mc = expectedModCount;
                if ((i = index) >= 0 && (index = hi) <= a.length) {
                    for (E e;; ++i) {
                        if (i < hi) {
                            if ((e = (E) a[i]) == null) // must be CME
                                break;
                            action.accept(e);
                        }
                        else if (q.modCount != mc)
                            break;
                        else
                            return;
                    }
                }
            }
            throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null)
                throw new NullPointerException();
            int hi = getFence(), lo = index;
            if (lo >= 0 && lo < hi) {
                index = lo + 1;
                @SuppressWarnings("unchecked") E e = (E)pq.queue[lo];
                if (e == null)
                    throw new ConcurrentModificationException();
                action.accept(e);
                if (pq.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        public long estimateSize() {
            return (long) (getFence() - index);
        }

        public int characteristics() {
            return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.NONNULL;
        }
    }
}
