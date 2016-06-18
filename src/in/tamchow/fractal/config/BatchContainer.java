package in.tamchow.fractal.config;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
/**
 * Contains items for batch processing
 */
public class BatchContainer<E extends Config> implements Serializable, Iterable<E> {
    private List<E> items;
    public BatchContainer() {
        items = new ArrayList<>();
    }
    public BatchContainer(List<E> items) {
        this.items = new ArrayList<>(items);
    }
    public static <E extends Config> List<E> collectionFromArray(E[] items) {
        return new ArrayList<>(Arrays.asList(items));
    }
    public List<E> getItems() {
        return items;
    }
    public void setItems(List<E> items) {
        this.items = items;
    }
    public E getItem(int index) {
        return items.get(index);
    }
    public E firstItem() {
        return getItem(0);
    }
    public E lastItem() {
        return getItem(items.size() - 1);
    }
    public void addItems(List<E> items) {
        this.items.addAll(items);
    }
    public void addItem(E item) {
        items.add(item);
    }
    @Override
    public Iterator<E> iterator() {
        return items.listIterator();
    }
    public String getContainedType() {
        return items.get(0).name;
    }
    @Override
    public String toString() {
        String representation = getContainedType() + "\n";
        for (E item : items) {
            representation += item + "\n";
        }
        return representation;
    }
    public int size() {
        return items.size();
    }
}