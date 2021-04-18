/*
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

/*
* This file is available under and governed by the GNU General Public
* License version 2 only, as published by the Free Software Foundation.
* However, the following notice accompanied the original version of this
* file:
*
* Written by Doug Lea with assistance from members of JCP JSR-166
* Expert Group and released to the public domain, as explained at
* http://creativecommons.org/publicdomain/zero/1.0/
*/

package java.util.concurrent.atomic;

import sun.misc.Unsafe;

import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

/**
* An {@code int} value that may be updated atomically.  See the
* {@link java.util.concurrent.atomic} package specification for
* description of the properties of atomic variables. An
* {@code AtomicInteger} is used in applications such as atomically
* incremented counters, and cannot be used as a replacement for an
* {@link Integer}. However, this class does extend
* {@code Number} to allow uniform access by tools and utilities that
* deal with numerically-based classes.
*
* @since 1.5
* @author Doug Lea
*/
public class AtomicInteger extends Number implements java.io.Serializable {
    private static final long serialVersionUID = 6214790243416807050L;

    /**
     * Unsafe实例，AtomicInteger依赖Unsafe提供的CAS能力
     */
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    /**
     * value属性偏移量
     */
    private static final long valueOffset;

    /**
     * 设置value字段的偏移
     */
    static {
        try {
            valueOffset = unsafe.objectFieldOffset
                (AtomicInteger.class.getDeclaredField("value"));
        } catch (Exception ex) { throw new Error(ex); }
    }

    /**
     * 储存实际值
     */
    private volatile int value;

    /**
     * 使用给定初始值实例化
     */
    public AtomicInteger(int initialValue) {
        value = initialValue;
    }

    /**
     * 使用默认初始值（0）实例化
     */
    public AtomicInteger() {
    }

    /**
     * 获取值
     */
    public final int get() {
        return value;
    }

    /**
     * Sets to the given value.
     *
     * @param newValue the new value
     */
    public final void set(int newValue) {
        value = newValue;
    }

    /**
     * Eventually sets to the given value.
     *
     * @param newValue the new value
     * @since 1.6
     */
    public final void lazySet(int newValue) {
        unsafe.putOrderedInt(this, valueOffset, newValue);
    }

    /**
     * 更新并返回旧值
     */
    public final int getAndSet(int newValue) {
        return unsafe.getAndSetInt(this, valueOffset, newValue);
    }

    /**
     * 当旧值与期望值相同时，将其原子更新为新值
     * CAS
     */
    public final boolean compareAndSet(int expect, int update) {
        return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
    }

    /**
     * Atomically sets the value to the given updated value
     * if the current value {@code ==} the expected value.
     *
     * <p><a href="package-summary.html#weakCompareAndSet">May fail
     * spuriously and does not provide ordering guarantees</a>, so is
     * only rarely an appropriate alternative to {@code compareAndSet}.
     *
     * @param expect the expected value
     * @param update the new value
     * @return {@code true} if successful
     */
    public final boolean weakCompareAndSet(int expect, int update) {
        return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
    }

    /**
     * 原子递增，返回旧值
     * i++的原子操作版本
     */
    public final int getAndIncrement() {
        return unsafe.getAndAddInt(this, valueOffset, 1);
    }

    /**
     * 原子递减，返回旧值
     * i--的原子操作版本
     */
    public final int getAndDecrement() {
        return unsafe.getAndAddInt(this, valueOffset, -1);
    }

    /**
     * 将value加上delta，返回相加之前的value
     */
    public final int getAndAdd(int delta) {
        return unsafe.getAndAddInt(this, valueOffset, delta);
    }

    /**
     * 原子递增，返回新值
     * ++i的原子操作版本
     */
    public final int incrementAndGet() {
        return unsafe.getAndAddInt(this, valueOffset, 1) + 1;
    }

    /**
     * 原子递减，返回旧值
     * --i的原子操作版本
     */
    public final int decrementAndGet() {
        return unsafe.getAndAddInt(this, valueOffset, -1) - 1;
    }

    /**
     * 将value加上delta，返回相加之后的value
     */
    public final int addAndGet(int delta) {
        return unsafe.getAndAddInt(this, valueOffset, delta) + delta;
    }

    /**
     * 以当前值为参数调用updateFunction，将值更新为计算的结果
     * 返回更新前的值
     */
    public final int getAndUpdate(IntUnaryOperator updateFunction) {
        int prev, next;
        do {
            // 获取当前值
            prev = get();
            // 计算
            next = updateFunction.applyAsInt(prev);
            // CAS更新为计算的结果
        } while (!compareAndSet(prev, next));
        // 返回更新前的值
        return prev;
    }

    /**
     * 以当前值为参数调用updateFunction，将值更新为计算的结果
     * 返回更新后的值
     */
    public final int updateAndGet(IntUnaryOperator updateFunction) {
        int prev, next;
        do {
            // 获取当前值
            prev = get();
            // 计算
            next = updateFunction.applyAsInt(prev);
            // CAS更新为计算的结果
        } while (!compareAndSet(prev, next));
        // 返回更新前的值
        return next;
    }

    /**
     * 以当前值和传入x为参数调用accumulatorFunction，将值更新为计算的结果
     * 返回计算前的值
     */
    public final int getAndAccumulate(int x,
                                      IntBinaryOperator accumulatorFunction) {
        int prev, next;
        do {
            // 获取旧值
            prev = get();
            // 计算
            next = accumulatorFunction.applyAsInt(prev, x);
            // CAS更新为计算后的值
        } while (!compareAndSet(prev, next));
        // 返回更新前的值
        return prev;
    }

    /**
     * 以当前值和传入x为参数调用accumulatorFunction，将值更新为计算的结果
     * 返回计算后的值
     */
    public final int accumulateAndGet(int x,
                                      IntBinaryOperator accumulatorFunction) {
        int prev, next;
        do {
            // 获取旧值
            prev = get();
            // 计算
            next = accumulatorFunction.applyAsInt(prev, x);
            // CAS更新为计算后的值
        } while (!compareAndSet(prev, next));
        // 返回更新后的值
        return next;
    }

    /**
     * Returns the String representation of the current value.
     * @return the String representation of the current value
     */
    public String toString() {
        return Integer.toString(get());
    }

    /**
     * Returns the value of this {@code AtomicInteger} as an {@code int}.
     */
    public int intValue() {
        return get();
    }

    /**
     * Returns the value of this {@code AtomicInteger} as a {@code long}
     * after a widening primitive conversion.
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public long longValue() {
        return (long)get();
    }

    /**
     * Returns the value of this {@code AtomicInteger} as a {@code float}
     * after a widening primitive conversion.
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public float floatValue() {
        return (float)get();
    }

    /**
     * Returns the value of this {@code AtomicInteger} as a {@code double}
     * after a widening primitive conversion.
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public double doubleValue() {
        return (double)get();
    }

}
