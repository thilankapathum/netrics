package dev.thilanka.netrics.util;

import dev.thilanka.netrics.dto.BoundingBox;
import org.springframework.stereotype.Component;

@Component
public class TileUtils {

    public BoundingBox tileToBoundingBox(int x, int y, int z){
        double minLng = tileXToLng(x, z);
        double maxLng = tileXToLng(x + 1, z);
        double maxLat = tileYToLat(y, z);
        double minLat = tileYToLat(y + 1, z);
        return new BoundingBox(minLng, minLat, maxLng, maxLat);
    }

    public int[] latLngToTile(double lat, double lng, int zoom){
        int x = (int) Math.floor((lng + 180.0) / 360.0 * (1 << zoom));
        int y = (int) Math.floor(
                (1 - Math.log(Math.tan(Math.toRadians(lat)) +
                        1.0 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom)
        );
        return new int[]{x, y};
    }

    private double tileXToLng(int x, int z) {
        return x / Math.pow(2, z) * 360.0 - 180;
    }

    private double tileYToLat(int y, int z) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

}
