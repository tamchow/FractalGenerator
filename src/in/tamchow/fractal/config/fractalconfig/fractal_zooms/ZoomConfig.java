package in.tamchow.fractal.config.fractalconfig.fractal_zooms;
import in.tamchow.fractal.helpers.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static in.tamchow.fractal.config.Strings.BLOCKS.ENDZOOMS;
import static in.tamchow.fractal.config.Strings.BLOCKS.ZOOMS;
/**
 * Holds a set of fractal zooms
 */
public class ZoomConfig implements Serializable, Iterable<ZoomParams> {
    private List<ZoomParams> zooms;
    public ZoomConfig() {
        zooms = new ArrayList<>();
    }
    public ZoomConfig(@NotNull ZoomConfig old) {
        this();
        setZooms(old.zooms);
    }
    @NotNull
    public static ZoomConfig fromString(@NotNull List<String> params) {
        @NotNull ZoomConfig zoom = new ZoomConfig();
        for (String param : params) {
            zoom.zooms.add(ZoomParams.fromString(param));
        }
        return zoom;
    }
    public boolean hasZoom(ZoomParams zoom) {
        return zooms.contains(zoom);
    }
    public boolean hasZooms() {
        return zooms != null && zooms.size() > 0;
    }
    public void setZooms(@NotNull ZoomParams[] zooms) {
        setZooms(new ArrayList<>(Arrays.asList(zooms)));
    }
    @NotNull
    @Override
    public String toString() {
        @NotNull String representation = ZOOMS + "\n";
        for (ZoomParams zoom : zooms) {
            representation += zoom + "\n";
        }
        return representation + ENDZOOMS;
    }
    public void addZoom(ZoomParams zoom) {
        if (this.zooms == null) {
            this.zooms = new ArrayList<>();
        }
        /*if (!hasZoom(zoom)) {
            zooms.add(zoom);
        }*/
        zooms.add(zoom);
    }
    public ZoomParams getZoom(int i) {
        return zooms.get(i);
    }
    @Override
    public Iterator<ZoomParams> iterator() {
        return zooms.listIterator();
    }
    public List<ZoomParams> getZooms() {
        return zooms;
    }
    public void setZooms(@NotNull List<ZoomParams> zooms) {
        if (this.zooms == null) {
            this.zooms = new ArrayList<>(zooms.size());
        }
        this.zooms.addAll(zooms);
    }
}