import { keyBy, omit } from "lodash";
import { FC, PropsWithChildren, useEffect } from "react";

import { Dataset } from "../types";
import FA2Layout from "graphology-layout-forceatlas2/worker";
import louvain from 'graphology-communities-louvain';
import { useSigma } from "@react-sigma/core";

const GraphDataController: FC<PropsWithChildren<{ dataset: Dataset; nodePositions; onGraphReady: (graph: any) => void }>> = ({
  dataset,
  children,
  nodePositions,
  onGraphReady,
}) => {

  const sigma = useSigma();
  const graph = sigma.getGraph();

  useEffect(() => {
    if (!graph || !dataset) return;

    const clusters = keyBy(dataset.clusters, "key");

    // Predefine some cluster centroids, one for each cluster
    const clusterCenters = {};

    // Function to generate a random position around a cluster center
    function getRandomPositionNear(centerX, centerY) {
      const variance = 300; // Adjust the variance to control spread within the cluster
      return {
        x: centerX + (Math.random() - 0.5) * variance,
        y: centerY + (Math.random() - 0.5) * variance
      };
    }

    // Add nodes to the graph if they do not already exist
    dataset.nodes.forEach((node) => {
      if (!graph.hasNode(node.key)) {
        // Create a cluster center if encountering the cluster for the first time
        if (!clusterCenters[node.cluster]) {
          clusterCenters[node.cluster] = {
            x: Math.random() * 500, // Assign random position to cluster center
            y: Math.random() * 500
          };
        }

        // Get the cluster center for this node's cluster
        const center = clusterCenters[node.cluster];

        // Position the node near its cluster center
        const position = getRandomPositionNear(center.x, center.y);

        // Add the node to the graph with its initial position
        graph.addNode(node.key, {
          ...node,
          ...omit(clusters[node.cluster], "key"),
          x: position.x,
          y: position.y,
          size: ((node.linkCounter - 10) / (300000 - 10)) * (50 - 2) + 2,  // Use precomputed linkCounter to set size
        });
      }
    });

    // Add edges, checking if they already exist
    dataset.edges.forEach(([source, target]) => {
      if (!graph.hasEdge(source, target)) {
        graph.addEdge(source, target, { size: 0 });
      }
    });

    // Louvain clustering
    louvain.assign(graph, { fastLocalMoves: true, resolution: 0.1, randomWalk: true });

    // Strengthen intra-cluster edges
    graph.forEachEdge((edge, attributes, source, target) => {
      const sourceCluster = graph.getNodeAttribute(source, 'cluster');
      const targetCluster = graph.getNodeAttribute(target, 'cluster');

      if (sourceCluster === targetCluster) {
        graph.setEdgeAttribute(edge, 'weight', attributes.weight * 1.5); // Intra-cluster edges
        graph.setEdgeAttribute(edge, 'color', 'rgba(110, 110, 110, 1)');
      } else {
        graph.setEdgeAttribute(edge, 'weight', attributes.weight * 1); // Inter-cluster edges
        graph.setEdgeAttribute(edge, 'color', 'rgba(110, 110, 110, 1)');
      }
    });

    // Initialize ForceAtlas2 layout
    const fa2Layout = new FA2Layout(graph, {
      settings: {
        barnesHutOptimize: true,    // Enable Barnes-Hut optimization for faster layout
        barnesHutTheta: 0.7,        // Higher value for faster layout at the cost of precision
        scalingRatio: 150000,       // Increase repulsive forces, spreading nodes out faster
        gravity: 0.01,              // Reduce gravity to prevent over-concentration of nodes
        strongGravityMode: true,    // Ensure some pull for isolated nodes
        adjustSizes: true,          // Adjust based on node size (calculated from linkCounter)
        outboundAttractionDistribution: false,
        linLogMode: true,           // Enable LinLog mode for faster convergence
        edgeWeightInfluence: 3,     // Consider edge weight in layout
      },
    });

    onGraphReady(graph);

    // Apply precomputed node positions if provided
    if (nodePositions) {
      Object.entries(nodePositions).forEach(([key, { x, y }]) => {
        if (graph.hasNode(key)) {
          graph.setNodeAttribute(key, 'x', x);
          graph.setNodeAttribute(key, 'y', y);
        }
      });
    }

    fa2Layout.start();

    return () => {
      fa2Layout.stop();
      graph.clear();
    };
  }, [graph, dataset, nodePositions]);

  return <>{children}</>;
};

export default GraphDataController;
