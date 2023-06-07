package github.cloudtour.extension;

/**
 * ExtensionLoader中使用的简易包装类型
 * @author cloudtour
 * @version 1.0
 */
public class Holder<T> {

    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
