using MathNet.Numerics.Distributions;
using QuickGraph;
using QuickGraph.Algorithms.ShortestPath;
using StudMap.Core;
using StudMap.Core.Graph;
using StudMap.Core.WLAN;
using StudMap.Data.Entities;
using StudMap.Service.CacheObjects;
using System;
using System.Collections.Generic;
using System.Linq;

namespace StudMap.Service.Services
{
    public class FingerprintService
    {
        public static Dictionary<int, Dictionary<int, Normal>> ComputeNodeDistribution(MapsEntities entities)
        {
            var nodeDistributions = new Dictionary<int, Dictionary<int, Normal>>();
            foreach (var dist in entities.RSSDistribution)
            {
                if (!nodeDistributions.ContainsKey(dist.NodeId))
                    nodeDistributions.Add(dist.NodeId, new Dictionary<int, Normal>());

                nodeDistributions[dist.NodeId].Add(dist.AccessPointId,
                    Normal.WithMeanStdDev(dist.AvgRSS ?? 0, dist.StDevRSS ?? 0.0));
            }
            return nodeDistributions;
        }

        public static Dictionary<string, int> CreateMACtoAPLookup(MapsEntities entities)
        {
            return entities.AccessPoints.ToDictionary(ap => ap.MAC, ap => ap.Id);
        }

        public static void SaveFingerprintForNode(MapsEntities entities, int nodeId, Fingerprint fingerprint)
        {
            if (fingerprint == null)
                throw new ServiceException(ResponseError.FingeprintIsNotDefined);

            if (fingerprint.AccessPointScans == null || !fingerprint.AccessPointScans.Any())
                return;

            var node = entities.Nodes.Find(nodeId);
            if (node == null)
                throw new ServiceException(ResponseError.NodeIdDoesNotExist);
            
            // Fingerprint in DB einfügen
            var fingerprints = entities.Fingerprints.Add(new Fingerprints 
            {
                NodeId = node.Id
            });
            entities.SaveChanges();
            
            
            foreach (var apScan in fingerprint.AccessPointScans)
            {
                // Fehlenden AccessPoint ggf. anlegen
                var ap = entities.AccessPoints.FirstOrDefault(x => x.MAC == apScan.AccessPoint.MAC);
                if (ap == null)
                {
                    ap = entities.AccessPoints.Add(new AccessPoints
                    {
                        MAC = apScan.AccessPoint.MAC
                    });
                    entities.SaveChanges();
                }
                
                // Einzelmessung speichern
                entities.AccessPointScans.Add(new AccessPointScans
                {
                    AccessPointId = ap.Id,
                    RecievedSignalStrength = apScan.ReceivedSignalStrength,
                    FingerprintId = fingerprints.Id
                });
            }
            entities.SaveChanges();
            
            StudMapCache.RemoveFingerprint(node.Floors.MapId);
        }

        public static List<NodeProbability> GetNodeProbabiltyForScan(MapsEntities entities, LocationRequest request)
        {
            // Gesammelte WLAN-Fingerprints aus DB auswerten und Verteilung bestimmen
            // Jetzt: gecachet!
            var cache = StudMapCache.Fingerprint(request.MapId);
            var mapCache = StudMapCache.Map(request.MapId);
            var previousNode = mapCache.Nodes.ContainsKey(request.PreviousNodeId) ? 
                mapCache.Nodes[request.PreviousNodeId] : 
                new Node { Id = 0 };

            // AccessPoint-Messung nach APs aufteilen
            var apScans = AnalyseInputScan(request.Scans, cache.MACtoAP);

            // W'keit bestimmen, dass RSS-Werte an Knoten gemessen werden
            Func<int, double> getDistance;
            if (request.PreviousNodeId == 0)
                getDistance = nodeId => 1.0;
            else
                getDistance = nodeId => {
                    double distance;
                    if (mapCache.PathFinder.TryGetDistance(previousNode.Id, nodeId, out distance))
                        return distance;
                    else
                        return -1;
                };

            var nodeProbs = CalculateNodeProbabilities(cache.NodeDistributions, apScans, getDistance);

            // Absteigend nach W'keit sortieren
            nodeProbs.Sort((m, n) => n.Probabilty.CompareTo(m.Probabilty));

            // Maximal die angeforderte Anzahl an Knoten zurückliefern
            int count = Math.Min(request.NodeCount, nodeProbs.Count);
            return nodeProbs.GetRange(0, count);
        }

        private static Dictionary<int, int> AnalyseInputScan(IEnumerable<LocationAPScan> scans, Dictionary<string, int> macToAp)
        {
            var apScans = new Dictionary<int, int>();
            foreach (var scan in scans)
            {
                int apId;
                if (macToAp.TryGetValue(scan.MAC, out apId))
                    apScans.Add(apId, scan.RSS);
            }
            return apScans;
        }

        private static List<NodeProbability> CalculateNodeProbabilities(
            Dictionary<int, Dictionary<int, Normal>> nodeDistributions, 
            IReadOnlyDictionary<int, int> apScans, 
            Func<int, double> getDistance)
        {
            var analysedNodes = new List<AnalysedNode>();
            foreach (var nodeId in nodeDistributions.Keys)
            {
                var apDistributions = nodeDistributions[nodeId];
                double scanProb = 1.0;
                int relevantApCount = 0;
                foreach (var apId in apDistributions.Keys)
                {
                    int scannedValue;
                    // Befindet sich der in der DB hinterlegte AP im aktuellen Fingerprint?
                    if (apScans.TryGetValue(apId, out scannedValue))
                    {
                        // Wenn ja, dann kann die W'keit für den gemessenen
                        // Wert aus der Normalverteilung bestimmt werden
                        var dist = apDistributions[apId];
                        double before = dist.CumulativeDistribution(scannedValue - 0.5);
                        double after = dist.CumulativeDistribution(scannedValue + 0.5);

                        // Wie wahrscheinlich ist es, am aktuellen Knoten & AP, den gemessenen RSS-Wert vorzufinden?
                        double apProb = after - before;

                        // In die Gesamtw'keit für den Fingeprint einrechnen
                        scanProb *= apProb;
                        relevantApCount += 1;
                    }
                }

                // Wenn keine übereinstimmende APs gefunden wurden, dann wird
                // der Knoten ignoriert
                if (relevantApCount == 0)
                    continue;

                // Gesamtw'keit berechnen, am aktuellen Knoten, die gemessenen RSS-Werte
                // für alle AccessPoints vorzufinden
                // Mathematisch: Geometrisches Mittel über die Einzelw'keiten
                scanProb = Math.Pow(scanProb, 1.0 / relevantApCount);

                // Die hier berechnete bedingte W'keit ist noch "verkehrt herum".
                // Wir haben berechnet:
                //  p(RSS/n):
                //  W'keit, dass die RSS Werte gemessen werden, wenn die Messung
                //  an dem Knoten n durchgeführt wurde
                //
                // Wir suchen:
                //  p(n/RSS):
                //  W'keit, dass wir uns an dem Knoten n befinden, wenn die
                //  Messung die angegebenen RSS-Werte lieferte
                //
                // Umrechung:
                //             p(RSS/n) * p(n)
                //  p(n/RSS) = ---------------
                //                 p(RSS)
                // Wobei:
                //  p(n): W'keit, sich an einem Knoten zu befinden, unabhängig von RSS-Werten
                //    Hier macht initial so etwas wie 1/(Anzahl Knoten) Sinn (Gleichverteilung)
                //    Später können "nahe" Knoten zur vorherigen Position höhere W'keiten
                //    bekommen und so den Algorithmus verbessern
                //
                //  p(RSS): W'keit, dass die angegebenen RSS-Werte gemessen werden, 
                //    unabhängig von irgendwelchen Knoten/Positionen
                //  TODO: Prüfen, ob es Sinn macht, hier AVG + STDEV über alle Datensätze
                //        zu verwenden und für jeden AP eine Normalverteilung zu verwenden
                // 
                // ACHTUNG: Die oben beschriebene Korrektur ist ohne positionsabhängige p(n)
                //  nur aus mathematischen Gründen notwendig. Wenn p(n) = const, dann können
                //  diese Werte ignoriert werden, da sie einen konstanten Korrekturfaktor
                //  bilden, der die Größenrelation der W'keiten untereinander nicht ändert!
                //
                //   Wenn p(n) = const, dann p(n/RSS) = p(RSS/n) * const
                //
                //  Die errechneten W'keiten können somit nur zum Vergleich untereinander
                //  für eine Messung verwendet werden. Um zu bestimmen, welche Knoten am
                //  wahrscheinlichsten sind, reicht dies aus.

                // TODO: Wenn p(n) positionsabhängig wird, dann hier verwenden
                // double position = 1.0 / nodeDistributions.Count; // Konstant => ignorieren

                double distance = getDistance(nodeId);
                // Wenn es keine Verbindung von dem alten Knoten gibt, dann wird 
                // der Knoten nicht zurückgeliefert
                if (distance < 0.0)
                    continue;

                var analysedNode = new AnalysedNode 
                { 
                    NodeId = nodeId, 
                    ScanProbabilty = scanProb, 
                    Distance = distance 
                };

                analysedNodes.Add(analysedNode);
            }

            // Summe der Distanzen
            double sumDistance = analysedNodes.Sum(n => n.Distance);
            double sumReversedDistance = analysedNodes.Sum(n => sumDistance - n.Distance);

            return analysedNodes.Select(n => new NodeProbability
            {
                NodeId = n.NodeId,
                Probabilty = n.ScanProbabilty * (sumDistance - n.Distance) / sumReversedDistance
            }).ToList();
        }
    }
}