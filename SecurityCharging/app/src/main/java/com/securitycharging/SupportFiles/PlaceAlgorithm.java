package com.securitycharging.SupportFiles;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;
import com.google.maps.android.clustering.algo.StaticCluster;
import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;
import com.google.maps.android.quadtree.PointQuadTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * support libs form google maps unity
 */

public class PlaceAlgorithm extends NonHierarchicalDistanceBasedAlgorithm<ClusterMarkerLocation> {
    public static final int MAX_DISTANCE_AT_ZOOM = 20; // essentially 50 dp.
    private static final SphericalMercatorProjection PROJECTION = new SphericalMercatorProjection(1);
    /**
     * Any modifications should be synchronized on mQuadTree.
     */
    private final Collection<QuadItem<ClusterMarkerLocation>> mItems = new ArrayList<>();
    /**
     * Any modifications should be synchronized on mQuadTree.
     */
    private final PointQuadTree<QuadItem<ClusterMarkerLocation>> mQuadTree = new PointQuadTree<>(0, 1, 0, 1);

    /**
     * @param item
     */
    @Override
    public void addItem(ClusterMarkerLocation item) {
        final QuadItem<ClusterMarkerLocation> quadItem = new QuadItem<>(item);
        synchronized (mQuadTree) {
            mItems.add(quadItem);
            mQuadTree.add(quadItem);
        }
    }

    /**
     * @param items
     */
    @Override
    public void addItems(Collection<ClusterMarkerLocation> items) {
        for (ClusterMarkerLocation item : items) {
            addItem(item);
        }
    }

    @Override
    public void clearItems() {
        synchronized (mQuadTree) {
            mItems.clear();
            mQuadTree.clear();
        }
    }

    /**
     * @param item
     */
    @Override
    public void removeItem(ClusterMarkerLocation item) {
        // QuadItem delegates hashcode() and equals() to its item so,
        //   removing any QuadItem to that item will remove the item
        final QuadItem<ClusterMarkerLocation> quadItem = new QuadItem<>(item);
        synchronized (mQuadTree) {
            mItems.remove(quadItem);
            mQuadTree.remove(quadItem);
        }
    }

    /**
     * @param zoom
     * @return
     */
    @Override
    public Set<? extends Cluster<ClusterMarkerLocation>> getClusters(double zoom) {
        final int discreteZoom = (int) zoom;

        final double zoomSpecificSpan = MAX_DISTANCE_AT_ZOOM / Math.pow(2, discreteZoom) / 256;

        final Set<QuadItem<ClusterMarkerLocation>> visitedCandidates = new HashSet<>();
        final Set<Cluster<ClusterMarkerLocation>> results = new HashSet<>();
        final Map<QuadItem<ClusterMarkerLocation>, Double> distanceToCluster = new HashMap<>();
        final Map<QuadItem<ClusterMarkerLocation>, StaticCluster<ClusterMarkerLocation>> itemToCluster = new HashMap<>();

        synchronized (mQuadTree) {
            for (QuadItem<ClusterMarkerLocation> candidate : mItems) {
                if (visitedCandidates.contains(candidate)) {
                    // Candidate is already part of another cluster.
                    continue;
                }

                Bounds searchBounds = createBoundsFromSpan(candidate.getPoint(), zoomSpecificSpan);
                Collection<QuadItem<ClusterMarkerLocation>> clusterItems;
                clusterItems = mQuadTree.search(searchBounds);
                if (clusterItems.size() == 1) {
                    // Only the current marker is in range. Just add the single item to the results.
                    results.add(candidate);
                    visitedCandidates.add(candidate);
                    distanceToCluster.put(candidate, 0d);
                    continue;
                }
                StaticCluster<ClusterMarkerLocation> cluster = new StaticCluster<>(candidate.mClusterItem.getPosition());
                results.add(cluster);

                for (QuadItem<ClusterMarkerLocation> clusterItem : clusterItems) {
                    Double existingDistance = distanceToCluster.get(clusterItem);
                    double distance = distanceSquared(clusterItem.getPoint(), candidate.getPoint());
                    if (existingDistance != null) {
                        // Item already belongs to another cluster. Check if it's closer to this cluster.
                        if (existingDistance < distance) {
                            continue;
                        }
                        // Move item to the closer cluster.
                        itemToCluster.get(clusterItem).remove(clusterItem.mClusterItem);
                    }
                    distanceToCluster.put(clusterItem, distance);
                    cluster.add(clusterItem.mClusterItem);
                    itemToCluster.put(clusterItem, cluster);
                }
                visitedCandidates.addAll(clusterItems);
            }
        }
        return results;
    }

    /**
     * @return
     */
    @Override
    public Collection<ClusterMarkerLocation> getItems() {
        final List<ClusterMarkerLocation> items = new ArrayList<>();
        synchronized (mQuadTree) {
            for (QuadItem<ClusterMarkerLocation> quadItem : mItems) {
                items.add(quadItem.mClusterItem);
            }
        }
        return items;
    }

    /**
     * @param a
     * @param b
     * @return
     */
    private double distanceSquared(Point a, Point b) {
        return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
    }

    /**
     * @param p
     * @param span
     * @return
     */
    private Bounds createBoundsFromSpan(Point p, double span) {
        double halfSpan = span / 2;
        return new Bounds(
                p.x - halfSpan, p.x + halfSpan,
                p.y - halfSpan, p.y + halfSpan);
    }

    /**
     * @param <T>
     */
    private static class QuadItem<T extends ClusterItem> implements PointQuadTree.Item, Cluster<T> {
        private final T mClusterItem;
        private final Point mPoint;
        private final LatLng mPosition;
        private Set<T> singletonSet;

        private QuadItem(T item) {
            mClusterItem = item;
            mPosition = item.getPosition();
            mPoint = PROJECTION.toPoint(mPosition);
            singletonSet = Collections.singleton(mClusterItem);
        }

        /**
         * @return
         */
        @Override
        public Point getPoint() {
            return mPoint;
        }

        /**
         * @return
         */
        @Override
        public LatLng getPosition() {
            return mPosition;
        }

        /**
         * @return
         */
        @Override
        public Set<T> getItems() {
            return singletonSet;
        }

        /**
         * @return
         */
        @Override
        public int getSize() {
            return 1;
        }

        /**
         * @return
         */
        @Override
        public int hashCode() {
            return mClusterItem.hashCode();
        }

        /**
         * @param other
         * @return
         */
        @Override
        public boolean equals(Object other) {
            return other instanceof QuadItem<?> && ((QuadItem<?>) other).mClusterItem.equals(mClusterItem);

        }
    }
}
