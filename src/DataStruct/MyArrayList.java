package DataStruct;
import java.util.Arrays;

/**
 * 简易的ArrayList扩容机制
 * @param <T>
 */
public class MyArrayList<T> {
    private Object[] elementData;
    private int size;
    private static final int DEFAULT_CAPACITY = 10;

    public MyArrayList() {
        elementData = new Object[DEFAULT_CAPACITY];
    }

    public void add(T item) {
        if (size == elementData.length) {
            grow();
        }
        elementData[size++] = item;
    }

    private void grow() {
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1); // 增加1.5倍
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    public T get(int index) {
        if (index <0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        return (T) elementData[index];
    }
}
